package com.codexahmar.urdu_keyboard.keyboard

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.emoji2.emojipicker.EmojiPickerView
import com.codexahmar.urdu_keyboard.R
import com.codexahmar.urdu_keyboard.utils.PreferenceHelper
import kotlin.math.max
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * KeyboardController — Ultra-fast production-grade IME controller.
 *
 * Gboard-Grade Performance Optimizations:
 *  1. Persistent Unified Root Container: Swapping between English, Urdu, Symbols, and
 *     Emoji occurs via instant in-memory layer visibility toggles (0ms) without calling
 *     setInputView() or tearing down the hardware rendering window.
 *  2. Zero-Relayout Shift & Key Presentation: Letters remain in fixed lowercase presentation
 *     matching Google Gboard standard. Shift toggles only the Shift button icon, completely
 *     eliminating main-thread layout thrashing across 26 TextView widgets during typing.
 *  3. Zero Runtime Reflection: All 80+ key views are mapped directly via compile-time R.id
 *     constants rather than resources.getIdentifier(), eliminating native AssetManager lock contention.
 *  4. Pre-Indexed Prefix Transliteration: PhoneticMapper uses pre-indexed prefix buckets for O(1)
 *     suggestions without iterating 500+ items or allocating redundant collections.
 *  5. Batch InputConnection Operations: Batched text insertions defer target app layout passes.
 */
class KeyboardController(
    private val context: Context,
    private val preferenceHelper: PreferenceHelper
) {
    private var rootContainer: FrameLayout? = null
    private var currentLayoutInflater: LayoutInflater? = null

    private val languageManager = LanguageManager(context, preferenceHelper)
    private lateinit var voiceInputManager: VoiceInputManager
    private lateinit var backspaceHandler: BackspaceHandler
    private val mainHandler = Handler(Looper.getMainLooper())
    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Cached layer views inside rootContainer
    private var englishView: View? = null
    private var urduView: View? = null
    private var symbolsView: View? = null
    private var emojiView: View? = null
    private var currentActiveView: View? = null

    private val currentInputConnection: InputConnection?
        get() = (context as? UrduKeyboardService)?.currentInputConnection

    private val englishButtons = mutableMapOf<String, Button>()
    private val urduButtons = mutableListOf<Button>()

    private var isUrduKeyboardLayout = false
    private var shiftState = ShiftState.OFF

    private val composingText = StringBuilder()
    private val phoneticMapper = PhoneticMapper()
    private var suggestionLeftBtn: Button? = null
    private var suggestionCenterBtn: Button? = null
    private var suggestionRightBtn: Button? = null
    private var lastShiftTapUptimeMs = 0L
    private var isSymbolMode = false
    private var lastKnownBottomInsetPx = 0

    private var isEmojiMode = false
    private var isHapticFeedbackEnabled = true
    private var spaceLongPressRunnable: Runnable? = null
    private var suggestionDebounceRunnable: Runnable? = null
    private var keyPopupOverlay: KeyPopupOverlay? = null
    private var pendingKeyView: View? = null
    private var pendingKeyCommit: (() -> Unit)? = null
    private var keyLongPressRunnable: Runnable? = null

    private val isNightMode: Boolean
        get() = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    private val emojiSearchQuery = StringBuilder()
    private var isEmojiSearchMode = false
    private var isCursorBlinkOn = true

    private val cursorBlinkRunnable = object : Runnable {
        override fun run() {
            if (!isEmojiSearchMode) return
            isCursorBlinkOn = !isCursorBlinkOn
            val cursor = currentActiveView?.findViewById<View>(R.id.emoji_search_cursor)
            cursor?.visibility = if (isCursorBlinkOn) View.VISIBLE else View.INVISIBLE
            mainHandler.postDelayed(this, 500L)
        }
    }

    private fun startCursorBlink() {
        mainHandler.removeCallbacks(cursorBlinkRunnable)
        isCursorBlinkOn = true
        val cursor = currentActiveView?.findViewById<View>(R.id.emoji_search_cursor)
        cursor?.visibility = View.VISIBLE
        mainHandler.postDelayed(cursorBlinkRunnable, 500L)
    }

    private fun stopCursorBlink() {
        mainHandler.removeCallbacks(cursorBlinkRunnable)
        val cursor = currentActiveView?.findViewById<View>(R.id.emoji_search_cursor)
        cursor?.visibility = View.GONE
    }


    private enum class ShiftState {
        OFF,
        SHIFT_ONCE,
        CAPS_LOCK
    }

    companion object {
        private const val TAG = "KeyboardController"
        private const val EXTRA_BOTTOM_GAP_DP = 8
        private const val SPACE_SWIPE_SWITCH_THRESHOLD_DP = 24
        private const val SUGGESTION_DEBOUNCE_MS = 25L
    }

    // ── Urdu Character Sets — Exact Google Gboard Layout (39 unique keys) ──────
    private val urduBaseLetters = arrayOf(
        // Row 1 (10 keys)
        "ض", "ص", "غ", "ڑ", "ٹ", "ث", "ح", "ئ", "ظ", "ط",
        // Row 2 (10 keys)
        "ق", "و", "ع", "ر", "ت", "ے", "ء", "ی", "ہ", "پ",
        // Row 3 (10 keys)
        "ا", "س", "ڈ", "د", "ف", "گ", "ھ", "ج", "ک", "ل",
        // Row 4 (9 keys)
        "ذ", "ز", "ش", "خ", "چ", "ب", "ں", "ن", "م"
    )

    private val urduLongPressVariants = mapOf(
        "ا" to arrayOf("آ", "أ"),
        "و" to arrayOf("ؤ"),
        "ہ" to arrayOf("ۂ", "ۃ"),
        "ے" to arrayOf("ۓ"),
        "ء" to arrayOf("ٔ"),
        "ز" to arrayOf("ژ"),
        "م" to arrayOf("؟"),
        // Row 1 Perso-Arabic digits on long-press (matching Gboard):
        "ض" to arrayOf("۱"), "ص" to arrayOf("۲"), "غ" to arrayOf("۳"),
        "ڑ" to arrayOf("۴"), "ٹ" to arrayOf("۵"), "ث" to arrayOf("۶"),
        "ح" to arrayOf("۷"), "ئ" to arrayOf("۸"), "ظ" to arrayOf("۹"),
        "ط" to arrayOf("۰")
    )

    private val englishLongPressVariants = mapOf(
        "q" to arrayOf("1"), "w" to arrayOf("2"), "e" to arrayOf("3"), "r" to arrayOf("4"),
        "t" to arrayOf("5"), "y" to arrayOf("6"), "u" to arrayOf("7"), "i" to arrayOf("8"),
        "o" to arrayOf("9"), "p" to arrayOf("0")
    )

    // ── Compile-Time IDs for Zero-Reflection Mapping ───────────────────────────
    private val englishKeyIdMap = intArrayOf(
        R.id.key_q, R.id.key_w, R.id.key_e, R.id.key_r, R.id.key_t,
        R.id.key_y, R.id.key_u, R.id.key_i, R.id.key_o, R.id.key_p,
        R.id.key_a, R.id.key_s, R.id.key_d, R.id.key_f, R.id.key_g,
        R.id.key_h, R.id.key_j, R.id.key_k, R.id.key_l,
        R.id.key_z, R.id.key_x, R.id.key_c, R.id.key_v, R.id.key_b,
        R.id.key_n, R.id.key_m
    )
    private val englishKeyCharMap = arrayOf(
        "q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
        "a", "s", "d", "f", "g", "h", "j", "k", "l",
        "z", "x", "c", "v", "b", "n", "m"
    )

    private val urduKeyIds = intArrayOf(
        R.id.urdu_key_1, R.id.urdu_key_2, R.id.urdu_key_3, R.id.urdu_key_4, R.id.urdu_key_5,
        R.id.urdu_key_6, R.id.urdu_key_7, R.id.urdu_key_8, R.id.urdu_key_9, R.id.urdu_key_10,
        R.id.urdu_key_11, R.id.urdu_key_12, R.id.urdu_key_13, R.id.urdu_key_14, R.id.urdu_key_15,
        R.id.urdu_key_16, R.id.urdu_key_17, R.id.urdu_key_18, R.id.urdu_key_19, R.id.urdu_key_20,
        R.id.urdu_key_21, R.id.urdu_key_22, R.id.urdu_key_23, R.id.urdu_key_24, R.id.urdu_key_25,
        R.id.urdu_key_26, R.id.urdu_key_27, R.id.urdu_key_28, R.id.urdu_key_29, R.id.urdu_key_30,
        R.id.urdu_key_31, R.id.urdu_key_32, R.id.urdu_key_33, R.id.urdu_key_34, R.id.urdu_key_35,
        R.id.urdu_key_36, R.id.urdu_key_37, R.id.urdu_key_38, R.id.urdu_key_39
    )

    private val symDigitIds = intArrayOf(
        R.id.sym_key_0, R.id.sym_key_1, R.id.sym_key_2, R.id.sym_key_3, R.id.sym_key_4,
        R.id.sym_key_5, R.id.sym_key_6, R.id.sym_key_7, R.id.sym_key_8, R.id.sym_key_9
    )

    init {
        val currentLang = languageManager.getCurrentLanguage()
        isUrduKeyboardLayout = (currentLang == "ur")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public Lifecycle & View Construction
    // ─────────────────────────────────────────────────────────────────────────

    fun createKeyboardView(inflater: LayoutInflater): View {
        currentLayoutInflater = inflater

        if (!::backspaceHandler.isInitialized) {
            backspaceHandler = BackspaceHandler(
                context = context,
                getInputConnection = { currentInputConnection },
                onDelete = { count -> handleBackspaceAction(count) }
            )
        }

        if (context is UrduKeyboardService && !::voiceInputManager.isInitialized) {
            voiceInputManager = VoiceInputManager(context, context).apply {
                onListeningStateChanged = { listening ->
                    mainHandler.post { updateVoiceButtonState(listening) }
                }
            }
        }

        var container = rootContainer
        if (container == null) {
            container = FrameLayout(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                isMotionEventSplittingEnabled = true
            }
            rootContainer = container
            applyBottomInsetPadding(container)

            val overlay = KeyPopupOverlay(context)
            keyPopupOverlay = overlay
            container.addView(overlay)
        }

        updateActiveLayer()
        return container
    }

    /**
     * Instantaneous layer visibility swap.
     * ZERO window detachment, ZERO setInputView() invocation, ZERO layout inflation on repeat visits.
     */
    fun updateActiveLayer() {
        commitPendingKey()
        keyPopupOverlay?.dismiss()

        val inflater = currentLayoutInflater ?: LayoutInflater.from(context)

        val targetView = when {
            isEmojiMode -> getOrCreateEmojiView(inflater)
            isSymbolMode -> getOrCreateSymbolsView(inflater)
            isUrduKeyboardLayout -> getOrCreateUrduView(inflater)
            else -> getOrCreateEnglishView(inflater)
        }

        currentActiveView = targetView

        englishView?.visibility = if (targetView === englishView) View.VISIBLE else View.GONE
        urduView?.visibility = if (targetView === urduView) View.VISIBLE else View.GONE
        symbolsView?.visibility = if (targetView === symbolsView) View.VISIBLE else View.GONE
        emojiView?.visibility = if (targetView === emojiView) View.VISIBLE else View.GONE

        // Point suggestions strip references to active view
        if (!isEmojiMode) {
            suggestionLeftBtn = targetView.findViewById(R.id.suggestion_left)
            suggestionCenterBtn = targetView.findViewById(R.id.suggestion_center)
            suggestionRightBtn = targetView.findViewById(R.id.suggestion_right)
            clearSuggestionsUI()
        }

        updateKeyboardLayout()
        updateShiftButtonIcon()
        syncEmojiSearchHeaderUI()
        if (isEmojiSearchMode) {
            updateEmojiSearchUI()
        }
        ensurePopupOverlayOnTop()
    }

    private fun ensurePopupOverlayOnTop() {
        val container = rootContainer ?: return
        val overlay = keyPopupOverlay ?: return
        if (overlay.parent === container) {
            container.bringChildToFront(overlay)
        } else {
            container.addView(overlay)
        }
    }

    private fun getOrCreateEnglishView(inflater: LayoutInflater): View {
        englishView?.let { return it }
        val view = inflater.inflate(R.layout.keyboard_view, rootContainer, false)
        enableSplitMotionEvents(view)
        setupEnglishViewButtons(view)
        setupSuggestionButtons(view)
        setupEmojiSearchHeader(view)
        rootContainer?.addView(view)
        englishView = view
        return view
    }

    private fun getOrCreateUrduView(inflater: LayoutInflater): View {
        urduView?.let { return it }
        val view = inflater.inflate(R.layout.keyboard_urdu, rootContainer, false)
        enableSplitMotionEvents(view)
        setupUrduViewButtons(view)
        setupSuggestionButtons(view)
        setupEmojiSearchHeader(view)
        rootContainer?.addView(view)
        urduView = view
        return view
    }

    private fun getOrCreateSymbolsView(inflater: LayoutInflater): View {
        symbolsView?.let { return it }
        val view = inflater.inflate(R.layout.keyboard_symbols, rootContainer, false)
        enableSplitMotionEvents(view)
        setupSymbolsViewButtons(view)
        setupSuggestionButtons(view)
        rootContainer?.addView(view)
        symbolsView = view
        return view
    }

    private fun getOrCreateEmojiView(inflater: LayoutInflater): View {
        emojiView?.let { return it }
        val view = inflater.inflate(R.layout.keyboard_emoji, rootContainer, false)
        enableSplitMotionEvents(view)
        setupEmojiViewButtons(view)
        rootContainer?.addView(view)
        emojiView = view
        return view
    }

    private fun enableSplitMotionEvents(view: View) {
        if (view is ViewGroup) {
            view.isMotionEventSplittingEnabled = true
            for (i in 0 until view.childCount) {
                enableSplitMotionEvents(view.getChildAt(i))
            }
        }
    }

    fun onStartInput(ic: InputConnection?, info: EditorInfo?) {
        isHapticFeedbackEnabled = preferenceHelper.isHapticFeedbackEnabled()
        val shouldCap = shouldAutoCap(info)
        shiftState = if (shouldCap) ShiftState.SHIFT_ONCE else ShiftState.OFF
        clearComposingState()
        updateShiftButtonIcon()
    }

    private fun shouldAutoCap(info: EditorInfo?): Boolean {
        if (info == null) return true
        val inputType = info.inputType
        if (inputType == 0) return true
        val clazz = inputType and android.text.InputType.TYPE_MASK_CLASS
        if (clazz != android.text.InputType.TYPE_CLASS_TEXT) return false
        val variation = inputType and android.text.InputType.TYPE_MASK_VARIATION
        if (variation == android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD ||
            variation == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
            variation == android.text.InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
            variation == android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
            variation == android.text.InputType.TYPE_TEXT_VARIATION_URI ||
            variation == android.text.InputType.TYPE_TEXT_VARIATION_FILTER) {
            return false
        }
        return true
    }

    fun onFinishInput() {
        commitPendingKey()
        keyPopupOverlay?.dismiss()
        shiftState = ShiftState.OFF
        clearComposingState()
        if (::voiceInputManager.isInitialized) {
            voiceInputManager.stopListening()
        }
    }

    /**
     * Synchronizes composing state when cursor or selection changes.
     * Prevents stale composing text from hijacking backspace at a new cursor position.
     */
    fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        if (composingText.isNotEmpty()) {
            val isOurComposing = candidatesStart >= 0 &&
                candidatesEnd >= 0 &&
                newSelStart == candidatesEnd &&
                newSelEnd == candidatesEnd &&
                (candidatesEnd - candidatesStart) == composingText.length

            if (!isOurComposing) {
                // User tapped elsewhere, selected a range, or composing span was ended
                clearComposingState()
            }
        }
    }

    fun handleKeyPress(key: String, ic: InputConnection) {
        when {
            isUrduKeyboardLayout -> onUrduKeyPressed(key)
            else -> onEnglishKeyPressed(key)
        }
    }

    fun switchLanguage() = onLanguageSwitch()
    fun startVoiceInput() = onVoiceInput()

    fun cleanup() {
        exitEmojiSearchMode()
        spaceLongPressRunnable?.let(mainHandler::removeCallbacks)
        spaceLongPressRunnable = null
        suggestionDebounceRunnable?.let(mainHandler::removeCallbacks)
        suggestionDebounceRunnable = null
        ioScope.cancel()
        if (::backspaceHandler.isInitialized) {
            backspaceHandler.cleanup()
        }
        if (::voiceInputManager.isInitialized) {
            voiceInputManager.cleanup()
        }
        rootContainer?.removeAllViews()
        rootContainer = null
        englishView = null
        urduView = null
        symbolsView = null
        emojiView = null
        englishButtons.clear()
        urduButtons.clear()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inset Handling
    // ─────────────────────────────────────────────────────────────────────────

    private fun applyBottomInsetPadding(targetView: View) {
        val extraGap = dpToPx(EXTRA_BOTTOM_GAP_DP)
        val minBottom = dpToPx(2)
        val baseLeft = targetView.paddingLeft
        val baseTop = targetView.paddingTop
        val baseRight = targetView.paddingRight

        fun applyInset(bottomInset: Int) {
            lastKnownBottomInsetPx = bottomInset
            targetView.setPadding(
                baseLeft, baseTop, baseRight,
                max(minBottom, bottomInset) + extraGap
            )
        }

        applyInset(lastKnownBottomInsetPx)

        ViewCompat.setOnApplyWindowInsetsListener(targetView) { _, insets ->
            val navBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val systemBarsBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            applyInset(max(navBottom, systemBarsBottom))
            insets
        }

        targetView.post { ViewCompat.requestApplyInsets(targetView) }
    }

    private fun dpToPx(dp: Int): Int =
        (dp * context.resources.displayMetrics.density).toInt()

    // ─────────────────────────────────────────────────────────────────────────
    // Button Wiring (Direct Compile-Time IDs — Zero Reflection)
    // ─────────────────────────────────────────────────────────────────────────

    private fun setupEnglishViewButtons(view: View) {
        englishButtons.clear()
        for (i in englishKeyIdMap.indices) {
            val key = englishKeyCharMap[i]
            val btn = view.findViewById<Button>(englishKeyIdMap[i]) ?: continue
            englishButtons[key] = btn
            val variants = englishLongPressVariants[key]
            bindSmoothKeyTouch(btn, key, variants) { chosenChar ->
                onEnglishKeyPressed(chosenChar)
            }
        }

        // Punctuation
        setFastTapListener(view.findViewById(R.id.key_comma)) { onPunctuationPressed(",") }
        setFastTapListener(view.findViewById(R.id.key_dot)) { onPunctuationPressed(".") }

        // Action Keys
        setupSpaceButton(view.findViewById(R.id.key_space))
        setupBackspaceButton(view.findViewById(R.id.key_backspace))
        setFastTapListener(view.findViewById(R.id.key_enter)) { onEnterPressed() }
        view.findViewById<Button>(R.id.key_voice)?.setOnClickListener { onVoiceInput() }
        view.findViewById<Button>(R.id.key_language)?.setOnClickListener { onLanguageSwitch() }
        view.findViewById<Button>(R.id.key_symbols)?.setOnClickListener { onSymbolsModeToggle() }
        view.findViewById<Button>(R.id.key_emoji)?.setOnClickListener { onEmojiModeToggle() }
        view.findViewById<Button>(R.id.key_shift)?.setOnClickListener { onShiftPressed() }
    }

    private fun setupUrduViewButtons(view: View) {
        urduButtons.clear()
        for (index in 0 until 39) {
            val btn = view.findViewById<Button>(urduKeyIds[index]) ?: continue
            urduButtons.add(btn)
            val baseChar = urduBaseLetters[index]
            val variants = urduLongPressVariants[baseChar]
            bindSmoothKeyTouch(btn, baseChar, variants) { char ->
                onUrduKeyPressed(char)
            }
        }

        // Punctuation
        setFastTapListener(view.findViewById(R.id.urdu_key_comma)) { onPunctuationPressed("،") }
        setFastTapListener(view.findViewById(R.id.urdu_key_dot)) { onPunctuationPressed("۔") }

        // Action Keys
        setupSpaceButton(view.findViewById(R.id.urdu_key_space))
        setupBackspaceButton(view.findViewById(R.id.urdu_key_backspace))
        setFastTapListener(view.findViewById(R.id.urdu_key_enter)) { onEnterPressed() }
        view.findViewById<Button>(R.id.key_voice)?.setOnClickListener { onVoiceInput() }
        view.findViewById<Button>(R.id.urdu_key_language)?.setOnClickListener { onLanguageSwitch() }
        view.findViewById<Button>(R.id.urdu_key_symbols)?.setOnClickListener { onSymbolsModeToggle() }
        view.findViewById<Button>(R.id.urdu_key_emoji)?.setOnClickListener { onEmojiModeToggle() }
    }

    private fun setupSymbolsViewButtons(view: View) {
        val urduDigits = listOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")

        // Digits 0..9
        for (i in 0..9) {
            val btn = view.findViewById<Button?>(symDigitIds[i]) ?: continue
            val symbol = if (isUrduKeyboardLayout) urduDigits[i] else i.toString()
            btn.text = symbol
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, if (isUrduKeyboardLayout) 22f else 19f)
            setFastTapListener(btn) { commitText(symbol) }
        }

        // Language-adaptive punctuation
        val punctPairs = if (isUrduKeyboardLayout) {
            arrayOf(
                Pair(R.id.sym_key_question, "؟"), Pair(R.id.sym_key_comma, "،"),
                Pair(R.id.sym_key_dot, "۔"), Pair(R.id.sym_key_semicolon, "؛"),
                Pair(R.id.sym_key_percent, "٪")
            )
        } else {
            arrayOf(
                Pair(R.id.sym_key_question, "?"), Pair(R.id.sym_key_comma, ","),
                Pair(R.id.sym_key_dot, "."), Pair(R.id.sym_key_semicolon, ";"),
                Pair(R.id.sym_key_percent, "%")
            )
        }

        for ((id, sym) in punctPairs) {
            val btn = view.findViewById<Button?>(id) ?: continue
            btn.text = sym
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, if (isUrduKeyboardLayout) 22f else 19f)
            setFastTapListener(btn) { commitText(sym) }
        }

        // Standard Symbols
        val commonSyms = arrayOf(
            Pair(R.id.sym_key_at, "@"), Pair(R.id.sym_key_hash, "#"), Pair(R.id.sym_key_dollar, "$"),
            Pair(R.id.sym_key_amp, "&"), Pair(R.id.sym_key_star, "*"), Pair(R.id.sym_key_minus, "-"),
            Pair(R.id.sym_key_plus, "+"), Pair(R.id.sym_key_lparen, "("), Pair(R.id.sym_key_rparen, ")"),
            Pair(R.id.sym_key_slash, "/"), Pair(R.id.sym_key_quote, "\""), Pair(R.id.sym_key_apostrophe, "'"),
            Pair(R.id.sym_key_colon, ":"), Pair(R.id.sym_key_exclamation, "!")
        )
        for ((id, sym) in commonSyms) {
            val btn = view.findViewById<Button?>(id) ?: continue
            btn.text = sym
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19f)
            setFastTapListener(btn) { commitText(sym) }
        }

        // Action Keys
        setupSpaceButton(view.findViewById(R.id.sym_key_space))
        setupBackspaceButton(view.findViewById(R.id.sym_backspace))
        setFastTapListener(view.findViewById(R.id.sym_key_enter)) { onEnterPressed() }
        view.findViewById<Button>(R.id.sym_key_voice)?.setOnClickListener { onVoiceInput() }
        view.findViewById<Button>(R.id.sym_key_language)?.setOnClickListener { onLanguageSwitch() }
        view.findViewById<Button>(R.id.sym_key_abc)?.setOnClickListener { onSymbolsModeToggle() }
        view.findViewById<Button>(R.id.sym_key_emoji)?.setOnClickListener { onEmojiModeToggle() }
    }

    private fun setupEmojiViewButtons(view: View) {
        val emojiPickerView = view.findViewById<EmojiPickerView>(R.id.emoji_picker_view)
        emojiPickerView?.setOnEmojiPickedListener { emojiViewItem ->
            applyTapFeedback(emojiPickerView)
            commitText(emojiViewItem.emoji)
        }

        styleEmojiPickerTitles(emojiPickerView)

        val abcBtn = view.findViewById<Button>(R.id.emoji_key_abc)
        abcBtn?.text = if (isUrduKeyboardLayout) "اردو" else "ABC"
        setFastTapListener(abcBtn) {
            clearComposingState()
            exitEmojiSearchMode()
            isEmojiMode = false
            updateActiveLayer()
        }

        setupBackspaceButton(view.findViewById(R.id.emoji_key_backspace))

        val searchPill = view.findViewById<View>(R.id.emoji_search_pill)
        setFastTapListener(searchPill) {
            enterEmojiSearchMode()
        }
    }

    private fun styleEmojiPickerTitles(picker: EmojiPickerView?) {
        picker ?: return
        val targetColor = if (isNightMode) 0xFFE8E8EA.toInt() else 0xFF1A1C1E.toInt()

        fun updateChildrenColors(root: View) {
            if (root is TextView) {
                if (root.text.isNotEmpty() && !Character.isSurrogate(root.text[0])) {
                    root.setTextColor(targetColor)
                }
            }
            if (root is ViewGroup) {
                for (i in 0 until root.childCount) {
                    updateChildrenColors(root.getChildAt(i))
                }
            }
        }

        picker.post { updateChildrenColors(picker) }
        picker.viewTreeObserver.addOnDrawListener {
            updateChildrenColors(picker)
        }
    }

    private fun setupEmojiSearchHeader(view: View) {
        val backBtn = view.findViewById<ImageButton?>(R.id.btn_emoji_search_back)
        backBtn?.setOnClickListener {
            applyTapFeedback(it)
            exitEmojiSearchMode(returnToEmojiPicker = true)
        }

        val clearBtn = view.findViewById<Button?>(R.id.btn_emoji_search_clear)
        clearBtn?.setOnClickListener {
            applyTapFeedback(it)
            emojiSearchQuery.setLength(0)
            updateEmojiSearchUI()
        }
    }

    private fun enterEmojiSearchMode() {
        isEmojiSearchMode = true
        emojiSearchQuery.setLength(0)
        isEmojiMode = false
        updateActiveLayer()
        syncEmojiSearchHeaderUI()
        updateEmojiSearchUI()
        startCursorBlink()
    }

    private fun exitEmojiSearchMode(returnToEmojiPicker: Boolean = false) {
        stopCursorBlink()
        isEmojiSearchMode = false
        emojiSearchQuery.setLength(0)
        syncEmojiSearchHeaderUI()
        if (returnToEmojiPicker) {
            isEmojiMode = true
            updateActiveLayer()
        }
    }

    private fun syncEmojiSearchHeaderUI() {
        val activeView = currentActiveView ?: return
        val suggestionsStrip = activeView.findViewById<View?>(R.id.suggestions_strip)
        val searchHeader = activeView.findViewById<View?>(R.id.emoji_search_header)

        if (isEmojiSearchMode) {
            suggestionsStrip?.visibility = View.GONE
            searchHeader?.visibility = View.VISIBLE
        } else {
            searchHeader?.visibility = View.GONE
            suggestionsStrip?.visibility = View.VISIBLE
        }
    }

    private fun updateEmojiSearchUI() {
        val activeView = currentActiveView ?: return
        val queryText = activeView.findViewById<TextView?>(R.id.tv_emoji_search_query) ?: return
        val clearBtn = activeView.findViewById<Button?>(R.id.btn_emoji_search_clear)
        val container = activeView.findViewById<LinearLayout?>(R.id.emoji_search_results_container) ?: return

        startCursorBlink()

        val query = emojiSearchQuery.toString()
        if (query.isEmpty()) {
            queryText.text = ""
            queryText.hint = if (isUrduKeyboardLayout) "ایموجی تلاش کریں" else "Search"
            clearBtn?.visibility = View.GONE
            populateEmojiResultsStrip(EmojiDictionary.popularEmojis, container)
        } else {
            queryText.text = query
            clearBtn?.visibility = View.VISIBLE
            val matches = EmojiDictionary.search(query)
            populateEmojiResultsStrip(if (matches.isEmpty()) EmojiDictionary.popularEmojis else matches, container)
        }
    }

    private fun populateEmojiResultsStrip(emojis: List<String>, container: LinearLayout) {
        container.removeAllViews()
        val itemSize = dpToPx(38)
        val marginPx = dpToPx(2)

        for (emoji in emojis) {
            val tv = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(itemSize, itemSize).apply {
                    marginStart = marginPx
                    marginEnd = marginPx
                }
                gravity = Gravity.CENTER
                text = emoji
                textSize = 21f
                isClickable = true
                isFocusable = true
                val outValue = TypedValue()
                if (context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)) {
                    setBackgroundResource(outValue.resourceId)
                }
                setOnClickListener {
                    applyTapFeedback(this)
                    commitText(emoji)
                }
            }
            container.addView(tv)
        }
    }

    fun isEmojiModeActive(): Boolean = isEmojiMode || isEmojiSearchMode

    fun handleEmojiSearchKeyEvent(keyCode: Int, event: KeyEvent): Boolean {
        if (!isEmojiMode && !isEmojiSearchMode) return false

        when (keyCode) {
            KeyEvent.KEYCODE_DEL -> {
                if (emojiSearchQuery.isNotEmpty()) {
                    emojiSearchQuery.deleteCharAt(emojiSearchQuery.length - 1)
                    updateEmojiSearchUI()
                } else {
                    exitEmojiSearchMode(returnToEmojiPicker = true)
                }
                return true
            }
            KeyEvent.KEYCODE_ESCAPE -> {
                exitEmojiSearchMode(returnToEmojiPicker = true)
                return true
            }
            KeyEvent.KEYCODE_ENTER -> {
                exitEmojiSearchMode(returnToEmojiPicker = false)
                return true
            }
            KeyEvent.KEYCODE_SPACE -> {
                emojiSearchQuery.append(" ")
                updateEmojiSearchUI()
                return true
            }
            else -> {
                val unicodeChar = event.unicodeChar
                if (unicodeChar > 0 && !Character.isISOControl(unicodeChar)) {
                    emojiSearchQuery.append(unicodeChar.toChar().toString())
                    updateEmojiSearchUI()
                    return true
                }
            }
        }
        return false
    }

    fun handleEmojiSearchKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DEL,
            KeyEvent.KEYCODE_ESCAPE,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_SPACE -> return true
            else -> {
                val unicodeChar = event.unicodeChar
                if (unicodeChar > 0 && !Character.isISOControl(unicodeChar)) {
                    return true
                }
            }
        }
        return false
    }

    private fun setupBackspaceButton(backspaceBtn: Button?) {
        backspaceBtn?.setOnTouchListener { view, event ->
            val action = event.actionMasked
            when (action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    commitPendingKey()
                    view.isPressed = true
                    applyTapFeedback(view)
                    backspaceHandler.onBackspaceDown()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                    view.isPressed = false
                    backspaceHandler.onBackspaceUp()
                    view.performClick()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupSpaceButton(spaceButton: Button?) {
        spaceButton ?: return

        val longPressTimeoutMs = ViewConfiguration.getLongPressTimeout().toLong()
        val scrubThresholdPx = dpToPx(14).toFloat()
        val stepDistancePx = dpToPx(12).toFloat()

        var isLongPressHandled = false
        var isScrubbing = false
        var downX = 0f
        var accumulatedX = 0f

        spaceButton.setOnTouchListener { view, event ->
            val action = event.actionMasked
            when (action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    commitPendingKey()

                    view.isPressed = true
                    applyTapFeedback(view)
                    isLongPressHandled = false
                    isScrubbing = false
                    downX = event.rawX
                    accumulatedX = 0f

                    spaceLongPressRunnable?.let(mainHandler::removeCallbacks)
                    spaceLongPressRunnable = Runnable {
                        if (!isScrubbing && view.isPressed) {
                            isLongPressHandled = true
                            onSpaceLongPressed(view)
                        }
                    }
                    mainHandler.postDelayed(spaceLongPressRunnable!!, longPressTimeoutMs)
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val totalDeltaX = event.rawX - downX
                    if (!isLongPressHandled) {
                        if (!isScrubbing && kotlin.math.abs(totalDeltaX) > scrubThresholdPx) {
                            isScrubbing = true
                            spaceLongPressRunnable?.let(mainHandler::removeCallbacks)
                            accumulatedX = 0f
                        }

                        if (isScrubbing) {
                            val deltaStep = event.rawX - (downX + accumulatedX)
                            if (kotlin.math.abs(deltaStep) >= stepDistancePx) {
                                val steps = (deltaStep / stepDistancePx).toInt()
                                accumulatedX += steps * stepDistancePx
                                moveCursorBySteps(steps)
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            }
                        }
                    }
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    spaceLongPressRunnable?.let(mainHandler::removeCallbacks)
                    view.isPressed = false

                    if (!isLongPressHandled && !isScrubbing) {
                        onSpacePressed()
                    }
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    spaceLongPressRunnable?.let(mainHandler::removeCallbacks)
                    view.isPressed = false
                    true
                }

                else -> false
            }
        }
    }

    private fun moveCursorBySteps(steps: Int) {
        val ic = currentInputConnection ?: return
        val keyCode = if (steps > 0) KeyEvent.KEYCODE_DPAD_RIGHT else KeyEvent.KEYCODE_DPAD_LEFT
        val count = kotlin.math.abs(steps)
        ic.beginBatchEdit()
        for (i in 0 until count) {
            (context as? android.inputmethodservice.InputMethodService)?.sendDownUpKeyEvents(keyCode)
        }
        ic.endBatchEdit()
    }

    private fun commitPendingKey() {
        keyLongPressRunnable?.let(mainHandler::removeCallbacks)
        keyLongPressRunnable = null
        pendingKeyCommit?.let { commit ->
            val ic = currentInputConnection
            ic?.beginBatchEdit()
            commit()
            ic?.endBatchEdit()
        }
        pendingKeyView?.isPressed = false
        pendingKeyView = null
        pendingKeyCommit = null
        keyPopupOverlay?.dismiss()
    }

    private fun bindSmoothKeyTouch(
        button: Button?,
        primaryChar: String,
        variants: Array<String>?,
        onCommitChar: (String) -> Unit
    ) {
        button ?: return
        val longPressTimeoutMs = 280L

        button.setOnTouchListener { view, event ->
            val action = event.actionMasked
            when (action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    // Commit any pending key from previous finger (multi-touch rollover)
                    if (pendingKeyView != null && pendingKeyView !== view) {
                        commitPendingKey()
                    }

                    view.isPressed = true
                    applyTapFeedback(view)
                    keyPopupOverlay?.showPreview(view, primaryChar, isNightMode)

                    pendingKeyView = view
                    pendingKeyCommit = { onCommitChar(primaryChar) }

                    keyLongPressRunnable?.let(mainHandler::removeCallbacks)
                    if (variants != null && variants.isNotEmpty()) {
                        keyLongPressRunnable = Runnable {
                            if (view.isPressed && pendingKeyView === view) {
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                keyPopupOverlay?.showVariants(view, variants, isNightMode)
                            }
                        }
                        mainHandler.postDelayed(keyLongPressRunnable!!, longPressTimeoutMs)
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (keyPopupOverlay?.isVariantsShowing() == true) {
                        keyPopupOverlay?.updateHover(event.rawX)
                    } else if (view.isPressed) {
                        val x = event.x
                        val y = event.y
                        val slop = dpToPx(24)
                        if (x < -slop || x > view.width + slop || y < -slop || y > view.height + slop) {
                            view.isPressed = false
                            keyPopupOverlay?.dismiss()
                            keyLongPressRunnable?.let(mainHandler::removeCallbacks)
                            if (pendingKeyView === view) {
                                pendingKeyView = null
                                pendingKeyCommit = null
                            }
                        }
                    }
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    keyLongPressRunnable?.let(mainHandler::removeCallbacks)
                    keyLongPressRunnable = null
                    view.isPressed = false

                    if (keyPopupOverlay?.isVariantsShowing() == true) {
                        val selected = keyPopupOverlay?.getSelectedVariant() ?: variants?.firstOrNull() ?: primaryChar
                        keyPopupOverlay?.dismiss()
                        pendingKeyView = null
                        pendingKeyCommit = null
                        onCommitChar(selected)
                    } else if (pendingKeyView === view) {
                        keyPopupOverlay?.dismiss()
                        val commit = pendingKeyCommit
                        pendingKeyView = null
                        pendingKeyCommit = null
                        commit?.invoke()
                    }
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    keyLongPressRunnable?.let(mainHandler::removeCallbacks)
                    keyLongPressRunnable = null
                    view.isPressed = false
                    keyPopupOverlay?.dismiss()
                    if (pendingKeyView === view) {
                        pendingKeyView = null
                        pendingKeyCommit = null
                    }
                    true
                }

                else -> false
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Instant Touch Feedback Listener
    // ─────────────────────────────────────────────────────────────────────────

    private fun setFastTapListener(button: View?, onTap: () -> Unit) {
        button?.setOnTouchListener { view, event ->
            val action = event.actionMasked
            when (action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    view.isPressed = true
                    applyTapFeedback(view)
                    onTap()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    view.isPressed = false
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    view.isPressed = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val x = event.x
                    val y = event.y
                    if (x < 0 || x > view.width || y < 0 || y > view.height) {
                        view.isPressed = false
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun applyTapFeedback(view: View) {
        if (isHapticFeedbackEnabled) {
            view.performHapticFeedback(
                HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Key Action Handlers (Zero-Relayout Fast Path)
    // ─────────────────────────────────────────────────────────────────────────

    private fun onEnglishKeyPressed(key: String) {
        val uppercase = shouldUseUppercase()
        val output = if (uppercase) key.uppercase() else key.lowercase()

        if (isEmojiSearchMode) {
            emojiSearchQuery.append(output)
            updateEmojiSearchUI()
            return
        }

        composingText.append(output)

        val ic = currentInputConnection
        ic?.beginBatchEdit()
        ic?.setComposingText(composingText.toString(), 1)
        ic?.endBatchEdit()
        updateSuggestions()

        // Shift turn-off: Updates only Shift icon, ZERO layout passes on 26 keys
        if (shiftState == ShiftState.SHIFT_ONCE) {
            shiftState = ShiftState.OFF
            updateShiftButtonIcon()
        }
    }

    private fun onUrduKeyPressed(urduLetter: String) {
        if (isEmojiSearchMode) {
            emojiSearchQuery.append(urduLetter)
            updateEmojiSearchUI()
            return
        }

        composingText.append(urduLetter)

        val ic = currentInputConnection
        ic?.beginBatchEdit()
        ic?.setComposingText(composingText.toString(), 1)
        ic?.endBatchEdit()
        updateSuggestions()
    }

    private fun onPunctuationPressed(symbol: String) {
        if (isEmojiSearchMode) {
            emojiSearchQuery.append(symbol)
            updateEmojiSearchUI()
            return
        }

        val ic = currentInputConnection
        ic?.beginBatchEdit()
        if (composingText.isNotEmpty()) {
            ic?.commitText(composingText.toString(), 1)
            clearComposingState()
        }
        ic?.commitText(symbol, 1)
        ic?.endBatchEdit()

        if (symbol == "." || symbol == "?" || symbol == "!" || symbol == "۔" || symbol == "؟") {
            shiftState = ShiftState.SHIFT_ONCE
            updateShiftButtonIcon()
        }
    }

    private fun onSpacePressed() {
        if (isEmojiSearchMode) {
            emojiSearchQuery.append(" ")
            updateEmojiSearchUI()
            return
        }

        val ic = currentInputConnection
        ic?.beginBatchEdit()
        if (composingText.isNotEmpty()) {
            ic?.commitText("${composingText} ", 1)
            clearComposingState()
        } else {
            ic?.commitText(" ", 1)
        }
        ic?.endBatchEdit()
    }

    private fun onEnterPressed() {
        if (isEmojiSearchMode) {
            exitEmojiSearchMode(returnToEmojiPicker = false)
            return
        }

        val ic = currentInputConnection
        ic?.beginBatchEdit()
        if (composingText.isNotEmpty()) {
            ic?.commitText(composingText.toString(), 1)
            clearComposingState()
        }
        ic?.commitText("\n", 1)
        ic?.endBatchEdit()

        shiftState = ShiftState.SHIFT_ONCE
        updateShiftButtonIcon()
    }

    private fun onShiftPressed() {
        if (isUrduKeyboardLayout) {
            return
        }

        if (shiftState == ShiftState.CAPS_LOCK) {
            shiftState = ShiftState.OFF
            updateShiftButtonIcon()
            return
        }

        val now = SystemClock.uptimeMillis()
        val doubleTapTimeoutMs = ViewConfiguration.getDoubleTapTimeout().toLong()
        shiftState = if (
            shiftState == ShiftState.SHIFT_ONCE &&
            (now - lastShiftTapUptimeMs) <= doubleTapTimeoutMs
        ) {
            ShiftState.CAPS_LOCK
        } else if (shiftState == ShiftState.OFF) {
            ShiftState.SHIFT_ONCE
        } else {
            ShiftState.OFF
        }
        lastShiftTapUptimeMs = now
        updateShiftButtonIcon()
    }

    private fun shouldUseUppercase() = shiftState != ShiftState.OFF

    private fun updateShiftButtonIcon() {
        if (isUrduKeyboardLayout || isSymbolMode) return

        val nightModeBits = context.resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK
        val isNight = nightModeBits == android.content.res.Configuration.UI_MODE_NIGHT_YES

        val shiftIcon = when (shiftState) {
            ShiftState.OFF -> if (isNight) R.drawable.ic_shift_dark else R.drawable.ic_shift
            ShiftState.SHIFT_ONCE -> if (isNight) R.drawable.ic_shift_active_dark else R.drawable.ic_shift_active
            ShiftState.CAPS_LOCK -> if (isNight) R.drawable.ic_shift_caps_dark else R.drawable.ic_shift_caps
        }

        val shiftButton = englishView?.findViewById<Button>(R.id.key_shift)
        shiftButton?.setCompoundDrawablesWithIntrinsicBounds(0, shiftIcon, 0, 0)
        shiftButton?.isActivated = (shiftState == ShiftState.CAPS_LOCK)
        shiftButton?.contentDescription = when (shiftState) {
            ShiftState.OFF -> "Shift off"
            ShiftState.SHIFT_ONCE -> "Shift on for next letter"
            ShiftState.CAPS_LOCK -> "Caps lock on"
        }
    }

    private fun onLanguageSwitch() {
        clearComposingState()
        languageManager.switchToNextLanguage()
        val newLang = languageManager.getCurrentLanguage()
        isUrduKeyboardLayout = (newLang == "ur")
        isSymbolMode = false
        isEmojiMode = false

        updateActiveLayer()
        shiftState = if (!isUrduKeyboardLayout) ShiftState.SHIFT_ONCE else ShiftState.OFF
        updateShiftButtonIcon()
    }

    private fun onVoiceInput() {
        if (::voiceInputManager.isInitialized) {
            val language = when (languageManager.getCurrentLanguage()) {
                "ur" -> "ur-PK"
                else -> "en-US"
            }
            voiceInputManager.startListening(language)
        }
    }

    private fun onSymbolsModeToggle() {
        clearComposingState()
        exitEmojiSearchMode()
        isSymbolMode = !isSymbolMode
        isEmojiMode = false
        updateActiveLayer()
    }

    private fun onEmojiModeToggle() {
        clearComposingState()
        exitEmojiSearchMode()
        isEmojiMode = !isEmojiMode
        isSymbolMode = false
        updateActiveLayer()
    }

    private fun updateKeyboardLayout() {
        val spaceText = if (isUrduKeyboardLayout) "اردو" else "English"
        val targetLangText = if (isUrduKeyboardLayout) "EN" else "اردو"

        englishView?.findViewById<Button>(R.id.key_space)?.text = spaceText
        englishView?.findViewById<Button>(R.id.key_language)?.text = targetLangText

        urduView?.findViewById<Button>(R.id.urdu_key_space)?.text = spaceText
        urduView?.findViewById<Button>(R.id.urdu_key_language)?.text = targetLangText

        symbolsView?.findViewById<Button>(R.id.sym_key_space)?.text = spaceText
        symbolsView?.findViewById<Button>(R.id.sym_key_language)?.text = targetLangText
        symbolsView?.findViewById<Button>(R.id.sym_key_abc)?.text = if (isUrduKeyboardLayout) "اردو" else "ABC"

        emojiView?.findViewById<Button>(R.id.emoji_key_abc)?.text = if (isUrduKeyboardLayout) "اردو" else "ABC"
        emojiView?.findViewById<TextView>(R.id.emoji_search_text)?.text =
            if (isUrduKeyboardLayout) "ایموجی تلاش کریں" else "Search emoji"
    }

    private fun onSpaceLongPressed(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        (context as? UrduKeyboardService)?.showSystemKeyboardPicker()
    }

    private fun commitText(text: String) {
        val ic = currentInputConnection
        ic?.beginBatchEdit()
        if (composingText.isNotEmpty()) {
            ic?.commitText(composingText.toString(), 1)
            clearComposingState()
        }
        ic?.commitText(text, 1)
        ic?.endBatchEdit()
    }

    fun commitVoiceText(text: String) {
        val ic = currentInputConnection
        ic?.beginBatchEdit()
        if (composingText.isNotEmpty()) {
            ic?.commitText(composingText.toString(), 1)
            clearComposingState()
        }
        ic?.commitText(text, 1)
        ic?.endBatchEdit()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Suggestions & Composing
    // ─────────────────────────────────────────────────────────────────────────

    private fun setupSuggestionButtons(view: View) {
        val left = view.findViewById<Button>(R.id.suggestion_left)
        val center = view.findViewById<Button>(R.id.suggestion_center)
        val right = view.findViewById<Button>(R.id.suggestion_right)

        setFastTapListener(left) { selectSuggestion(left?.text?.toString() ?: "") }
        setFastTapListener(center) { selectSuggestion(center?.text?.toString() ?: "") }
        setFastTapListener(right) { selectSuggestion(right?.text?.toString() ?: "") }
    }

    private fun selectSuggestion(suggestion: String) {
        if (suggestion.isEmpty()) return
        suggestionDebounceRunnable?.let(mainHandler::removeCallbacks)

        val ic = currentInputConnection
        ic?.beginBatchEdit()
        ic?.commitText("$suggestion ", 1)
        clearComposingState()
        ic?.endBatchEdit()

        ioScope.launch { preferenceHelper.incrementWordFrequency(suggestion) }
    }

    private fun clearComposingState() {
        composingText.setLength(0)
        currentInputConnection?.finishComposingText()
        clearSuggestionsUI()
    }

    private fun clearSuggestionsUI() {
        if (!suggestionLeftBtn?.text.isNullOrEmpty()) suggestionLeftBtn?.text = ""
        if (!suggestionCenterBtn?.text.isNullOrEmpty()) suggestionCenterBtn?.text = ""
        if (!suggestionRightBtn?.text.isNullOrEmpty()) suggestionRightBtn?.text = ""
    }

    fun handleBackspaceAction(count: Int = 1): Boolean {
        if (isEmojiSearchMode) {
            if (emojiSearchQuery.isNotEmpty()) {
                val toDelete = minOf(count, emojiSearchQuery.length)
                emojiSearchQuery.delete(emojiSearchQuery.length - toDelete, emojiSearchQuery.length)
                updateEmojiSearchUI()
            } else {
                exitEmojiSearchMode(returnToEmojiPicker = true)
            }
            return true
        }

        if (composingText.isNotEmpty()) {
            val ic = currentInputConnection
            // Real-time cursor validation: verify cursor is actually right after composing text
            val textBefore = ic?.getTextBeforeCursor(composingText.length, 0)?.toString()
            if (textBefore != null && textBefore != composingText.toString()) {
                // The cursor has moved away from our composing region
                clearComposingState()
                return false
            }

            val toDelete = minOf(count, composingText.length)
            composingText.delete(composingText.length - toDelete, composingText.length)
            ic?.beginBatchEdit()
            if (composingText.isEmpty()) {
                clearComposingState()
            } else {
                ic?.setComposingText(composingText.toString(), 1)
                updateSuggestions()
            }
            ic?.endBatchEdit()
            return true
        }
        return false
    }

    /**
     * Fallback and external backspace invocation with selection handling and Unicode code-point safety.
     */
    fun performBackspace() {
        if (!handleBackspaceAction(1)) {
            val ic = currentInputConnection ?: return
            val selectedText = ic.getSelectedText(0)
            if (!selectedText.isNullOrEmpty()) {
                ic.commitText("", 1)
                return
            }
            val deleted = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                ic.deleteSurroundingTextInCodePoints(1, 0)
            } else {
                ic.deleteSurroundingText(1, 0)
            }
            if (!deleted) {
                (context as? android.inputmethodservice.InputMethodService)
                    ?.sendDownUpKeyEvents(android.view.KeyEvent.KEYCODE_DEL)
            }
        }
    }

    private fun updateSuggestions() {
        val input = composingText.toString()
        if (input.isEmpty()) {
            clearSuggestionsUI()
            suggestionDebounceRunnable?.let(mainHandler::removeCallbacks)
            return
        }

        suggestionDebounceRunnable?.let(mainHandler::removeCallbacks)
        suggestionDebounceRunnable = Runnable {
            val snapshot = composingText.toString()
            if (snapshot.isEmpty()) return@Runnable
            ioScope.launch {
                val suggestions = getSuggestionsForInput(snapshot)
                withContext(Dispatchers.Main) {
                    if (composingText.toString() == snapshot) {
                        displaySuggestions(suggestions, snapshot)
                    }
                }
            }
        }
        mainHandler.postDelayed(suggestionDebounceRunnable!!, SUGGESTION_DEBOUNCE_MS)
    }

    private fun getSuggestionsForInput(input: String): List<String> {
        val freqMap = preferenceHelper.getTopFrequentWords(50)
            .associate { (word, count) -> word to count }
        return phoneticMapper.getUrduSuggestions(input, freqMap)
    }

    private fun displaySuggestions(suggestions: List<String>, rawInput: String) {
        val center = if (suggestions.isNotEmpty()) suggestions[0] else rawInput
        val left = if (suggestions.isNotEmpty() && suggestions[0] != rawInput) rawInput else (suggestions.getOrNull(1) ?: "")
        val right = if (suggestions.size > 2) suggestions[2] else (if (suggestions.size > 1 && left != suggestions[1]) suggestions[1] else "")

        if (suggestionLeftBtn?.text?.toString() != left) suggestionLeftBtn?.text = left
        if (suggestionCenterBtn?.text?.toString() != center) suggestionCenterBtn?.text = center
        if (suggestionRightBtn?.text?.toString() != right) suggestionRightBtn?.text = right
    }

    private fun updateVoiceButtonState(isListening: Boolean) {
        val voiceBtnIds = listOf(R.id.key_voice, R.id.sym_key_voice)
        for (id in voiceBtnIds) {
            val btn = currentActiveView?.findViewById<Button>(id) ?: continue
            btn.isSelected = isListening
            btn.alpha = if (isListening) 0.6f else 1.0f
        }
    }
}
