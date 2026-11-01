package com.codexahmar.urdu_keyboard.keyboard

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.HorizontalScrollView
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
    private var isUrduShifted = false
    private var isHapticFeedbackEnabled = true
    private var spaceLongPressRunnable: Runnable? = null
    private var suggestionDebounceRunnable: Runnable? = null

    private val isNightMode: Boolean
        get() = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    private val englishEmojiCategories = listOf(
        "All" to "",
        "❤️ Love" to "love",
        "😂 Funny" to "laugh",
        "😊 Smile" to "smile",
        "🔥 Lit" to "fire",
        "😢 Sad" to "sad",
        "👍 Hands" to "thumb",
        "🤲 Dua" to "pray",
        "🇵🇰 Pak" to "pakistan",
        "🍕 Food" to "food",
        "☕ Chai" to "tea",
        "🎉 Party" to "party",
        "🚗 Travel" to "car",
        "🐱 Animals" to "cat",
        "🌹 Flowers" to "flower",
        "⭐ Stars" to "star",
        "💰 Money" to "money"
    )

    private val urduEmojiCategories = listOf(
        "سب" to "",
        "❤️ محبت" to "love",
        "😂 ہنسی" to "laugh",
        "😊 مسکراہٹ" to "smile",
        "🔥 زبردست" to "fire",
        "😢 اداس" to "sad",
        "👍 ہاتھ" to "thumb",
        "🤲 دعا" to "pray",
        "🇵🇰 پاکستان" to "pakistan",
        "🍕 کھانا" to "food",
        "☕ چائے" to "tea",
        "🎉 جشن" to "party",
        "🚗 سفر" to "car",
        "🐱 جانور" to "cat",
        "🌹 پھول" to "flower",
        "⭐ ستارے" to "star",
        "💰 پیسے" to "money"
    )

    private var selectedEmojiCategoryIndex = 0
    private val emojiChipViews = mutableListOf<TextView>()

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

    // ── Urdu Character Sets (38 unique keys) ──────────────────────────────────
    private val urduBaseLetters = arrayOf(
        "ض", "ص", "ث", "ق", "ف", "غ", "ع", "ہ", "خ", "ح",
        "ج", "چ", "ظ", "ط", "ز", "ر", "ڑ", "د", "ڈ", "ب",
        "پ", "ت", "ٹ", "ش", "س", "ک", "گ", "ل", "م", "ن",
        "و", "ے", "ی", "ں", "ھ", "ا", "آ", "ء"
    )

    private val urduShiftLetters = arrayOf(
        "َ", "ِ", "ُ", "ً", "ٍ", "ٌ", "ّ", "ْ", "ٰ", "ٖ",
        "ذ", "ژ", "ئ", "ؤ", "ۂ", "ﷲ", "ﷺ", "﷽", "ؑ", "ؒ",
        "؛", "؟", "٪", "«", "»", "ؓ", "ۖ", "ۗ", "ۘ", "؞",
        "۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷"
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
        R.id.urdu_key_36, R.id.urdu_key_37, R.id.urdu_key_38
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
        }

        updateActiveLayer()
        return container
    }

    /**
     * Instantaneous layer visibility swap.
     * ZERO window detachment, ZERO setInputView() invocation, ZERO layout inflation on repeat visits.
     */
    fun updateActiveLayer() {
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
    }

    private fun getOrCreateEnglishView(inflater: LayoutInflater): View {
        englishView?.let { return it }
        val view = inflater.inflate(R.layout.keyboard_view, rootContainer, false)
        enableSplitMotionEvents(view)
        setupEnglishViewButtons(view)
        setupSuggestionButtons(view)
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
        spaceLongPressRunnable?.let(mainHandler::removeCallbacks)
        spaceLongPressRunnable = null
        suggestionDebounceRunnable?.let(mainHandler::removeCallbacks)
        suggestionDebounceRunnable = null
        emojiChipViews.clear()
        selectedEmojiCategoryIndex = 0
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
        for (i in englishKeyIdMap.indices) {
            val key = englishKeyCharMap[i]
            val btn = view.findViewById<Button>(englishKeyIdMap[i]) ?: continue
            englishButtons[key] = btn
            setFastTapListener(btn) { onEnglishKeyPressed(key) }
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
        for (index in 0 until 38) {
            val btn = view.findViewById<Button>(urduKeyIds[index]) ?: continue
            urduButtons.add(btn)
            setFastTapListener(btn) {
                val currentLetter = if (isUrduShifted) urduShiftLetters[index] else urduBaseLetters[index]
                onUrduKeyPressed(currentLetter)
            }
        }

        // Punctuation
        setFastTapListener(view.findViewById(R.id.urdu_key_comma)) { onPunctuationPressed("،") }
        setFastTapListener(view.findViewById(R.id.urdu_key_dot)) { onPunctuationPressed("۔") }

        // Action Keys
        setupSpaceButton(view.findViewById(R.id.urdu_key_space))
        setupBackspaceButton(view.findViewById(R.id.urdu_backspace))
        setFastTapListener(view.findViewById(R.id.urdu_key_enter)) { onEnterPressed() }
        view.findViewById<Button>(R.id.urdu_key_voice)?.setOnClickListener { onVoiceInput() }
        view.findViewById<Button>(R.id.urdu_key_language)?.setOnClickListener { onLanguageSwitch() }
        view.findViewById<Button>(R.id.urdu_key_symbols)?.setOnClickListener { onSymbolsModeToggle() }
        view.findViewById<Button>(R.id.urdu_key_emoji)?.setOnClickListener { onEmojiModeToggle() }
        view.findViewById<Button>(R.id.urdu_key_shift)?.setOnClickListener { onShiftPressed() }

        refreshUrduKeyLabels()
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

        val abcBtn = view.findViewById<Button>(R.id.emoji_key_abc)
        abcBtn?.text = if (isUrduKeyboardLayout) "اردو" else "ABC"
        setFastTapListener(abcBtn) {
            clearComposingState()
            isEmojiMode = false
            updateActiveLayer()
        }

        setupBackspaceButton(view.findViewById(R.id.emoji_key_backspace))

        val searchPill = view.findViewById<View>(R.id.emoji_search_pill)
        val clearBtn = view.findViewById<Button>(R.id.emoji_search_clear_btn)
        val chipsScroll = view.findViewById<HorizontalScrollView>(R.id.emoji_search_chips_scroll)
        val chipsContainer = view.findViewById<ViewGroup>(R.id.emoji_search_chips_container)

        buildEmojiChips(chipsContainer)

        setFastTapListener(clearBtn) {
            selectEmojiCategory(0)
        }

        setFastTapListener(searchPill) {
            // Tapping search pill stays ON the emoji screen!
            if (selectedEmojiCategoryIndex != 0) {
                selectEmojiCategory(0)
            } else {
                selectEmojiCategory(1) // Quick jump to "❤️ Love"
                chipsScroll?.smoothScrollTo(0, 0)
            }
        }
    }

    private fun buildEmojiChips(container: ViewGroup?) {
        if (container == null) return
        container.removeAllViews()
        emojiChipViews.clear()

        val categories = if (isUrduKeyboardLayout) urduEmojiCategories else englishEmojiCategories
        val chipMargin = dpToPx(6)
        val horizontalPadding = dpToPx(12)
        val verticalPadding = dpToPx(4)

        for (i in categories.indices) {
            val (label, _) = categories[i]
            val chip = TextView(context).apply {
                text = label
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                isClickable = true
                isFocusable = true
                gravity = Gravity.CENTER
                setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
                val lp = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ).apply {
                    marginEnd = chipMargin
                }
                layoutParams = lp
            }
            setFastTapListener(chip) {
                selectEmojiCategory(i)
            }
            container.addView(chip)
            emojiChipViews.add(chip)
        }

        updateEmojiChipsAppearance()
    }

    private fun selectEmojiCategory(index: Int) {
        val categories = if (isUrduKeyboardLayout) urduEmojiCategories else englishEmojiCategories
        val safeIndex = index.coerceIn(0, categories.lastIndex)
        selectedEmojiCategoryIndex = safeIndex
        val (label, tag) = categories[safeIndex]

        updateEmojiChipsAppearance()

        val emojiViewRef = emojiView ?: return
        val emojiPicker = emojiViewRef.findViewById<View>(R.id.emoji_picker_view)
        val gridScroll = emojiViewRef.findViewById<View>(R.id.emoji_search_grid_scroll)
        val grid = emojiViewRef.findViewById<GridLayout>(R.id.emoji_search_grid)
        val clearBtn = emojiViewRef.findViewById<View>(R.id.emoji_search_clear_btn)
        val searchText = emojiViewRef.findViewById<TextView>(R.id.emoji_search_text)

        if (tag.isEmpty()) {
            gridScroll?.visibility = View.GONE
            emojiPicker?.visibility = View.VISIBLE
            clearBtn?.visibility = View.GONE
            searchText?.text = if (isUrduKeyboardLayout) "ایموجی تلاش کریں" else "Search emoji"
        } else {
            emojiPicker?.visibility = View.GONE
            gridScroll?.visibility = View.VISIBLE
            clearBtn?.visibility = View.VISIBLE
            searchText?.text = label
            if (grid != null) {
                val results = EmojiDictionary.search(tag)
                displayFilteredEmojis(results, grid)
            }
        }
    }

    private fun updateEmojiChipsAppearance() {
        val isDark = isNightMode
        val categories = if (isUrduKeyboardLayout) urduEmojiCategories else englishEmojiCategories
        for (i in emojiChipViews.indices) {
            val chip = emojiChipViews[i]
            if (i < categories.size) {
                chip.text = categories[i].first
            }
            val isSelected = (i == selectedEmojiCategoryIndex)
            chip.setBackgroundResource(
                when {
                    isSelected -> R.drawable.emoji_chip_bg_active
                    isDark -> R.drawable.emoji_chip_bg_dark
                    else -> R.drawable.emoji_chip_bg
                }
            )
            chip.setTextColor(
                when {
                    isSelected -> Color.WHITE
                    isDark -> Color.parseColor("#E8EAED")
                    else -> Color.parseColor("#202124")
                }
            )
        }
    }

    private fun displayFilteredEmojis(emojis: List<String>, grid: GridLayout) {
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val horizontalPadding = dpToPx(8)
        val availableWidth = (screenWidth - horizontalPadding).coerceAtLeast(dpToPx(280))
        val colWidth = availableWidth / 8
        val itemHeight = dpToPx(44)

        val currentCount = grid.childCount
        val targetCount = emojis.size

        for (i in 0 until max(currentCount, targetCount)) {
            if (i < targetCount) {
                val emoji = emojis[i]
                val cell = if (i < currentCount) {
                    grid.getChildAt(i) as TextView
                } else {
                    TextView(context).apply {
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
                        gravity = Gravity.CENTER
                        val outValue = TypedValue()
                        if (context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)) {
                            setBackgroundResource(outValue.resourceId)
                        }
                        isClickable = true
                        isFocusable = true
                        grid.addView(this)
                    }
                }
                cell.text = emoji
                val lp = cell.layoutParams as? GridLayout.LayoutParams ?: GridLayout.LayoutParams()
                lp.width = colWidth
                lp.height = itemHeight
                cell.layoutParams = lp
                cell.visibility = View.VISIBLE
                setFastTapListener(cell) {
                    applyTapFeedback(cell)
                    commitText(emoji)
                }
            } else {
                grid.getChildAt(i).visibility = View.GONE
            }
        }
    }

    private fun setupBackspaceButton(backspaceBtn: Button?) {
        backspaceBtn?.setOnTouchListener { view, event ->
            val action = event.actionMasked
            when (action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
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
        val swipeThresholdPx = dpToPx(SPACE_SWIPE_SWITCH_THRESHOLD_DP).toFloat()

        var isLongPressHandled = false
        var isSwipeSwitchHandled = false
        var downX = 0f
        var downY = 0f

        spaceButton.setOnTouchListener { view, event ->
            val action = event.actionMasked
            when (action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    view.isPressed = true
                    applyTapFeedback(view)
                    isLongPressHandled = false
                    isSwipeSwitchHandled = false
                    downX = event.x
                    downY = event.y

                    onSpacePressed()

                    spaceLongPressRunnable?.let(mainHandler::removeCallbacks)
                    spaceLongPressRunnable = Runnable {
                        isLongPressHandled = true
                        onSpaceLongPressed(view)
                    }
                    mainHandler.postDelayed(spaceLongPressRunnable!!, longPressTimeoutMs)
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!isLongPressHandled && !isSwipeSwitchHandled) {
                        val dx = event.x - downX
                        val dy = event.y - downY
                        if (kotlin.math.abs(dx) >= swipeThresholdPx && kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                            spaceLongPressRunnable?.let(mainHandler::removeCallbacks)
                            isSwipeSwitchHandled = true
                            currentInputConnection?.deleteSurroundingText(1, 0)
                            applyTapFeedback(view)
                            onLanguageSwitch()
                        }
                    }
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    view.isPressed = false
                    spaceLongPressRunnable?.let(mainHandler::removeCallbacks)
                    view.performClick()
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    view.isPressed = false
                    spaceLongPressRunnable?.let(mainHandler::removeCallbacks)
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
        composingText.append(urduLetter)

        val ic = currentInputConnection
        ic?.beginBatchEdit()
        ic?.setComposingText(composingText.toString(), 1)
        ic?.endBatchEdit()
        updateSuggestions()

        if (isUrduShifted) {
            isUrduShifted = false
            refreshUrduKeyLabels()
        }
    }

    private fun onPunctuationPressed(symbol: String) {
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
            isUrduShifted = !isUrduShifted
            refreshUrduKeyLabels()
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

    private fun refreshUrduKeyLabels() {
        if (!isUrduKeyboardLayout || urduButtons.isEmpty()) return
        val letters = if (isUrduShifted) urduShiftLetters else urduBaseLetters
        for (i in 0 until minOf(38, urduButtons.size)) {
            val btn = urduButtons[i]
            if (btn.text != letters[i]) {
                btn.text = letters[i]
            }
        }
        val shiftButton = urduView?.findViewById<Button>(R.id.urdu_key_shift)
        shiftButton?.isSelected = isUrduShifted
        shiftButton?.alpha = if (isUrduShifted) 0.6f else 1.0f
    }

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
        isUrduShifted = false

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
        isSymbolMode = !isSymbolMode
        isEmojiMode = false
        updateActiveLayer()
    }

    private fun onEmojiModeToggle() {
        clearComposingState()
        isEmojiMode = !isEmojiMode
        isSymbolMode = false
        updateActiveLayer()
    }

    private fun updateKeyboardLayout() {
        val spaceText = if (isUrduKeyboardLayout) "اردو" else "English"
        val langShortText = if (isUrduKeyboardLayout) "اردو" else "EN"

        englishView?.findViewById<Button>(R.id.key_space)?.text = spaceText
        englishView?.findViewById<Button>(R.id.key_language)?.text = langShortText

        urduView?.findViewById<Button>(R.id.urdu_key_space)?.text = spaceText
        urduView?.findViewById<Button>(R.id.urdu_key_language)?.text = langShortText

        symbolsView?.findViewById<Button>(R.id.sym_key_space)?.text = spaceText
        symbolsView?.findViewById<Button>(R.id.sym_key_language)?.text = langShortText
        symbolsView?.findViewById<Button>(R.id.sym_key_abc)?.text = if (isUrduKeyboardLayout) "اردو" else "ABC"

        emojiView?.findViewById<Button>(R.id.emoji_key_abc)?.text = if (isUrduKeyboardLayout) "اردو" else "ABC"
        emojiView?.let { view ->
            val chipsContainer = view.findViewById<ViewGroup>(R.id.emoji_search_chips_container)
            if (chipsContainer != null && emojiChipViews.isNotEmpty()) {
                val categories = if (isUrduKeyboardLayout) urduEmojiCategories else englishEmojiCategories
                for (i in emojiChipViews.indices) {
                    if (i < categories.size) {
                        emojiChipViews[i].text = categories[i].first
                    }
                }
            }
            if (selectedEmojiCategoryIndex == 0) {
                view.findViewById<TextView>(R.id.emoji_search_text)?.text =
                    if (isUrduKeyboardLayout) "ایموجی تلاش کریں" else "Search emoji"
            }
        }
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
        if (suggestions.isEmpty()) {
            clearSuggestionsUI()
            return
        }

        val left = rawInput
        val center = if (suggestions.isNotEmpty()) suggestions[0] else ""
        val right = if (suggestions.size > 1) suggestions[1] else ""

        if (suggestionLeftBtn?.text?.toString() != left) suggestionLeftBtn?.text = left
        if (suggestionCenterBtn?.text?.toString() != center) suggestionCenterBtn?.text = center
        if (suggestionRightBtn?.text?.toString() != right) suggestionRightBtn?.text = right
    }

    private fun updateVoiceButtonState(isListening: Boolean) {
        val voiceBtnIds = listOf(R.id.key_voice, R.id.urdu_key_voice, R.id.sym_key_voice)
        for (id in voiceBtnIds) {
            val btn = currentActiveView?.findViewById<Button>(id) ?: continue
            btn.isSelected = isListening
            btn.alpha = if (isListening) 0.6f else 1.0f
        }
    }
}
