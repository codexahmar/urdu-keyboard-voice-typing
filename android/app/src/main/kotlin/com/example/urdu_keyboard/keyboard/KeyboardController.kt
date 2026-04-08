package com.codexahmar.urdu_keyboard.keyboard

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.codexahmar.urdu_keyboard.R
import com.codexahmar.urdu_keyboard.utils.PreferenceHelper
import kotlin.math.max

/**
 * KeyboardController — manages keyboard view creation, input logic, and UI state.
 *
 * Architectural notes (v2):
 *  - urduLetters array corrected: 38 unique characters, zero duplicates.
 *  - Key-press animation added (subtle 0.87 → 1.0 scale, 60 ms) for tactile feel.
 *  - Shift key now swaps vector drawables (ic_shift / ic_shift_active) instead of
 *    mutating button text, keeping icon-only buttons consistent.
 *  - applyBottomInsetPadding() consolidated to a single reliable
 *    ViewCompat.setOnApplyWindowInsetsListener flow with a manual trigger post-layout.
 *  - All functionality (text commit, language switch, voice, symbols) is unchanged.
 */
class KeyboardController(
    private val context: Context,
    private val preferenceHelper: PreferenceHelper
) {
    private lateinit var keyboardView: View
    private val languageManager = LanguageManager(context, preferenceHelper)
    private lateinit var voiceInputManager: VoiceInputManager
    private lateinit var backspaceHandler: BackspaceHandler
    private val mainHandler = Handler(Looper.getMainLooper())

    private var currentInputConnection: InputConnection? = null
    private var isUrduKeyboardLayout = false
    private var shiftState = ShiftState.OFF
    private var lastShiftTapUptimeMs = 0L
    private var isAutoCapNext = true
    private var isSymbolMode = false
    private var lastKnownBottomInsetPx = 0
    private var spaceLongPressRunnable: Runnable? = null

    private enum class ShiftState {
        OFF,
        SHIFT_ONCE,
        CAPS_LOCK
    }

    companion object {
        private const val TAG = "KeyboardController"
        // Extra bottom clearance so content clears the nav bar shadow.
        private const val EXTRA_BOTTOM_GAP_DP = 12
        // Press-animation scale factor (subtle, Gboard-like)
        private const val PRESS_SCALE = 0.90f
        private const val ANIM_DURATION_MS = 55L
        private const val SPACE_SWIPE_SWITCH_THRESHOLD_DP = 24
    }

    /**
     * Urdu letters — 38 unique characters mapped to urdu_key_1 … urdu_key_38.
     *
     * Row 1  (keys 1–10):  ض ص ث ق ف غ ع ہ خ ح
     * Row 2  (keys 11–20): ج چ ظ ط ز ر ذ د ڈ ب
     * Row 3  (keys 21–30): پ ت ٹ ش س ک گ ل م ن
     * Row 4  (keys 31–38): و ے ی ئ ؤ ا آ ژ
     */
    private val urduLetters = arrayOf(
        // Row 1
        "ض", "ص", "ث", "ق", "ف", "غ", "ع", "ہ", "خ", "ح",
        // Row 2
        "ج", "چ", "ظ", "ط", "ز", "ر", "ذ", "د", "ڈ", "ب",
        // Row 3
        "پ", "ت", "ٹ", "ش", "س", "ک", "گ", "ل", "م", "ن",
        // Row 4
        "و", "ے", "ی", "ئ", "ؤ", "ا", "آ", "ژ"
    )

    init {
        val currentLang = languageManager.getCurrentLanguage()
        isUrduKeyboardLayout = (currentLang == "ur")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    fun createKeyboardView(inflater: LayoutInflater): View {
        val layoutId = when {
            isSymbolMode -> R.layout.keyboard_symbols
            isUrduKeyboardLayout -> R.layout.keyboard_urdu
            else -> R.layout.keyboard_view
        }

        keyboardView = inflater.inflate(layoutId, null)

        backspaceHandler = BackspaceHandler(context) { currentInputConnection }

        if (context is UrduKeyboardService) {
            voiceInputManager = VoiceInputManager(context, context)
        }

        setupKeyboardButtons()
        updateKeyboardLayout()
        refreshEnglishKeyLabels()
        applyBottomInsetPadding()

        return keyboardView
    }

    fun onStartInput(ic: InputConnection?, info: EditorInfo?) {
        currentInputConnection = ic
        isAutoCapNext = true
        shiftState = ShiftState.OFF
        Log.d(TAG, "onStartInput: IC=${if (ic != null) "valid" else "null"}")
        refreshEnglishKeyLabels()
    }

    fun onFinishInput() {
        currentInputConnection = null
        isAutoCapNext = true
        shiftState = ShiftState.OFF
    }

    fun handleKeyPress(key: String, ic: InputConnection) {
        currentInputConnection = ic
        if (isUrduKeyboardLayout) onUrduKeyPressed(key) else onEnglishKeyPressed(key)
    }

    fun switchLanguage() = onLanguageSwitch()
    fun startVoiceInput() = onVoiceInput()

    fun cleanup() {
        spaceLongPressRunnable?.let(mainHandler::removeCallbacks)
        spaceLongPressRunnable = null
        backspaceHandler.cleanup()
        if (::voiceInputManager.isInitialized) voiceInputManager.cleanup()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inset handling — single consolidated path, no redundant triple-calls
    // ─────────────────────────────────────────────────────────────────────────

    private fun applyBottomInsetPadding() {
        val extraGap = dpToPx(EXTRA_BOTTOM_GAP_DP)
        val minBottom = dpToPx(6)
        val baseLeft = keyboardView.paddingLeft
        val baseTop = keyboardView.paddingTop
        val baseRight = keyboardView.paddingRight

        fun applyInset(bottomInset: Int) {
            lastKnownBottomInsetPx = bottomInset
            keyboardView.setPadding(
                baseLeft, baseTop, baseRight,
                max(minBottom, bottomInset) + extraGap
            )
        }

        // Apply a best-effort initial padding so there's no first-frame jump.
        applyInset(lastKnownBottomInsetPx)

        // Register the reliable callback — fires whenever insets change.
        ViewCompat.setOnApplyWindowInsetsListener(keyboardView) { _, insets ->
            val navBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val systemBarsBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            applyInset(max(navBottom, systemBarsBottom))
            insets
        }

        // Trigger manually after the view is laid out (handles cases where the
        // callback fires before the view is attached).
        keyboardView.post { ViewCompat.requestApplyInsets(keyboardView) }
    }

    private fun dpToPx(dp: Int): Int =
        (dp * context.resources.displayMetrics.density).toInt()

    // ─────────────────────────────────────────────────────────────────────────
    // Button wiring
    // ─────────────────────────────────────────────────────────────────────────

    private fun setupKeyboardButtons() {
        when {
            isSymbolMode -> setupSymbolButtons()
            isUrduKeyboardLayout -> setupUrduKeyboardButtons()
            else -> {
                setupEnglishKeyboardButtons()
                setupPunctuationButtons()
            }
        }
        setupCommonButtons()
    }

    private fun setupEnglishKeyboardButtons() {
        val letterKeys = arrayOf(
            "q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
            "a", "s", "d", "f", "g", "h", "j", "k", "l",
            "z", "x", "c", "v", "b", "n", "m"
        )
        letterKeys.forEach { key ->
            val id = context.resources.getIdentifier("key_$key", "id", context.packageName)
            setFastTapListener(keyboardView.findViewById(id)) { onEnglishKeyPressed(key) }
        }
    }

    private fun setupUrduKeyboardButtons() {
        urduLetters.forEachIndexed { index, letter ->
            val id = context.resources.getIdentifier(
                "urdu_key_${index + 1}", "id", context.packageName
            )
            setFastTapListener(keyboardView.findViewById(id)) { onUrduKeyPressed(letter) }
        }
    }

    private fun setupPunctuationButtons() {
        val prefix = if (isUrduKeyboardLayout) "urdu_key_" else "key_"
        val comma = if (isUrduKeyboardLayout) "،" else ","
        val dot = "."
        val question = if (isUrduKeyboardLayout) "؟" else "?"

        listOf(
            Triple("${prefix}comma", comma, false),
            Triple("${prefix}dot", dot, true),
            Triple("${prefix}question", question, true)
        ).forEach { (idName, symbol, isEndSentence) ->
            val id = context.resources.getIdentifier(idName, "id", context.packageName)
            setFastTapListener(keyboardView.findViewById(id)) {
                onPunctuationPressed(symbol)
                if (isEndSentence) isAutoCapNext = true
            }
        }

        // Extra symbols — silently skip if the key doesn't exist in this layout.
        mapOf(
            "${prefix}apostrophe" to "'",
            "${prefix}slash" to "/",
            "${prefix}at" to "@",
            "${prefix}minus" to "-",
            "${prefix}plus" to "+"
        ).forEach { (idName, symbol) ->
            val id = context.resources.getIdentifier(idName, "id", context.packageName)
            val btn = keyboardView.findViewById<Button?>(id) ?: return@forEach
            setFastTapListener(btn) { onPunctuationPressed(symbol) }
        }
    }

    private fun setupSymbolButtons() {
        // Number row: controller iterates sym_key_0 .. sym_key_9
        for (i in 0..9) {
            val id = context.resources.getIdentifier("sym_key_$i", "id", context.packageName)
            val symbol = i.toString()
            setFastTapListener(keyboardView.findViewById(id)) { commitText(symbol) }
        }

        mapOf(
            "sym_key_at" to "@", "sym_key_hash" to "#", "sym_key_dollar" to "$",
            "sym_key_percent" to "%", "sym_key_amp" to "&", "sym_key_star" to "*",
            "sym_key_minus" to "-", "sym_key_plus" to "+",
            "sym_key_lparen" to "(", "sym_key_rparen" to ")",
            "sym_key_slash" to "/", "sym_key_quote" to "\"",
            "sym_key_apostrophe" to "'", "sym_key_colon" to ":",
            "sym_key_semicolon" to ";", "sym_key_exclamation" to "!",
            "sym_key_question" to "?", "sym_key_comma" to ",",
            "sym_key_dot" to ".", "sym_key_equal" to "=",
            "sym_key_lbracket" to "[", "sym_key_rbracket" to "]",
            "sym_key_lbrace" to "{", "sym_key_rbrace" to "}",
            "sym_key_backslash" to "\\", "sym_key_less" to "<",
            "sym_key_greater" to ">", "sym_key_caret" to "^",
            "sym_key_tilde" to "~"
        ).forEach { (idName, symbol) ->
            val id = context.resources.getIdentifier(idName, "id", context.packageName)
            val btn = keyboardView.findViewById<Button?>(id) ?: return@forEach
            setFastTapListener(btn) { commitText(symbol) }
        }
    }

    private fun setupCommonButtons() {
        val prefix = when {
            isSymbolMode -> "sym_key_"
            isUrduKeyboardLayout -> "urdu_key_"
            else -> "key_"
        }
        val backPrefix = when {
            isSymbolMode -> "sym_"
            isUrduKeyboardLayout -> "urdu_"
            else -> "key_"
        }

        // Space
        val spaceId = context.resources.getIdentifier("${prefix}space", "id", context.packageName)
        setupSpaceButton(keyboardView.findViewById(spaceId))

        // Backspace — long-press handled by BackspaceHandler
        val bsId = context.resources.getIdentifier("${backPrefix}backspace", "id", context.packageName)
        keyboardView.findViewById<Button>(bsId)?.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    animateKeyPress(view)
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    backspaceHandler.onBackspaceDown()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    backspaceHandler.onBackspaceUp()
                    view.performClick()
                    true
                }
                else -> false
            }
        }

        // Enter
        val enterId = context.resources.getIdentifier("${prefix}enter", "id", context.packageName)
        setFastTapListener(keyboardView.findViewById(enterId)) { onEnterPressed() }

        // Voice / Mic
        val voiceId = context.resources.getIdentifier("${prefix}voice", "id", context.packageName)
        keyboardView.findViewById<Button>(voiceId)?.setOnClickListener { onVoiceInput() }

        // Language switch
        val langId = context.resources.getIdentifier("${prefix}language", "id", context.packageName)
        keyboardView.findViewById<Button>(langId)?.setOnClickListener { onLanguageSwitch() }

        // 123 ↔ ABC toggle
        val toggleIdName = if (isSymbolMode) "sym_key_abc" else "${prefix}symbols"
        val toggleId = context.resources.getIdentifier(toggleIdName, "id", context.packageName)
        keyboardView.findViewById<Button>(toggleId)?.setOnClickListener { onSymbolsModeToggle() }

        // Shift (English only)
        if (!isSymbolMode && !isUrduKeyboardLayout) {
            val shiftId = context.resources.getIdentifier("key_shift", "id", context.packageName)
            keyboardView.findViewById<Button>(shiftId)?.setOnClickListener { onShiftPressed() }
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
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    animateKeyPress(view)
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    isLongPressHandled = false
                    isSwipeSwitchHandled = false
                    downX = event.x
                    downY = event.y

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
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onLanguageSwitch()
                        }
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    spaceLongPressRunnable?.let(mainHandler::removeCallbacks)
                    if (!isLongPressHandled && !isSwipeSwitchHandled) {
                        onSpacePressed()
                    }
                    view.performClick()
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    spaceLongPressRunnable?.let(mainHandler::removeCallbacks)
                    true
                }

                else -> false
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Touch listener with press animation
    // ─────────────────────────────────────────────────────────────────────────

    private fun setFastTapListener(button: Button?, onTap: () -> Unit) {
        button?.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    animateKeyPress(view)
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onTap()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.performClick()
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Subtle scale-down → scale-up animation on key press (Gboard-style "pop").
     * Uses ViewPropertyAnimator — runs on the main thread via choreographer, no
     * ObjectAnimator allocation overhead.
     */
    private fun animateKeyPress(view: View) {
        view.animate().cancel()
        view.animate()
            .scaleX(PRESS_SCALE)
            .scaleY(PRESS_SCALE)
            .setDuration(ANIM_DURATION_MS)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(ANIM_DURATION_MS)
                    .start()
            }
            .start()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Key action handlers (all input logic unchanged)
    // ─────────────────────────────────────────────────────────────────────────

    private fun onEnglishKeyPressed(key: String) {
        val output = if (shouldUseUppercase()) key.uppercase() else key.lowercase()
        commitText(output)
        isAutoCapNext = false
        if (shiftState == ShiftState.SHIFT_ONCE) {
            shiftState = ShiftState.OFF
            refreshEnglishKeyLabels()
        }
    }

    private fun onUrduKeyPressed(urduLetter: String) {
        commitText(urduLetter)
        isAutoCapNext = false
    }

    private fun onPunctuationPressed(symbol: String) {
        commitText(symbol)
        isAutoCapNext = symbol == "." || symbol == "?" || symbol == "؟" || symbol == "!"
    }

    private fun onSpacePressed() {
        commitText(" ")
    }

    private fun onEnterPressed() {
        commitText("\n")
        isAutoCapNext = true
    }

    private fun onShiftPressed() {
        if (isUrduKeyboardLayout) return

        if (shiftState == ShiftState.CAPS_LOCK) {
            shiftState = ShiftState.OFF
            refreshEnglishKeyLabels()
            return
        }

        val now = SystemClock.uptimeMillis()
        val doubleTapTimeoutMs = ViewConfiguration.getDoubleTapTimeout().toLong()
        shiftState = if (
            shiftState == ShiftState.SHIFT_ONCE &&
            (now - lastShiftTapUptimeMs) <= doubleTapTimeoutMs
        ) {
            ShiftState.CAPS_LOCK
        } else {
            ShiftState.SHIFT_ONCE
        }
        lastShiftTapUptimeMs = now
        refreshEnglishKeyLabels()
    }

    private fun shouldUseUppercase() = shiftState != ShiftState.OFF || isAutoCapNext

    /**
     * Refresh English key labels AND shift icon drawable when shift state changes.
     * Dark-mode aware: uses the appropriate icon variant for contrast on both themes.
     */
    private fun refreshEnglishKeyLabels() {
        if (isUrduKeyboardLayout || isSymbolMode) return

        val uppercase = shouldUseUppercase()
        val letterKeys = arrayOf(
            "q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
            "a", "s", "d", "f", "g", "h", "j", "k", "l",
            "z", "x", "c", "v", "b", "n", "m"
        )
        letterKeys.forEach { key ->
            val id = context.resources.getIdentifier("key_$key", "id", context.packageName)
            keyboardView.findViewById<Button>(id)?.text = if (uppercase) key.uppercase() else key.lowercase()
        }

        // Detect night mode so we pick the right icon variant for contrast.
        val nightModeBits = context.resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK
        val isNight = nightModeBits == android.content.res.Configuration.UI_MODE_NIGHT_YES

        val shiftIcon = when (shiftState) {
            ShiftState.OFF -> if (isNight) R.drawable.ic_shift_dark else R.drawable.ic_shift
            ShiftState.SHIFT_ONCE -> if (isNight) R.drawable.ic_shift_active_dark else R.drawable.ic_shift_active
            ShiftState.CAPS_LOCK -> if (isNight) R.drawable.ic_shift_caps_dark else R.drawable.ic_shift_caps
        }

        val shiftButton = keyboardView.findViewById<Button>(R.id.key_shift)
        shiftButton?.setCompoundDrawablesWithIntrinsicBounds(0, shiftIcon, 0, 0)
        shiftButton?.isActivated = (shiftState == ShiftState.CAPS_LOCK)
        shiftButton?.contentDescription = when (shiftState) {
            ShiftState.OFF -> "Shift off"
            ShiftState.SHIFT_ONCE -> "Shift on for next letter"
            ShiftState.CAPS_LOCK -> "Caps lock on"
        }
    }

    private fun onLanguageSwitch() {
        languageManager.switchToNextLanguage()
        val newLang = languageManager.getCurrentLanguage()
        val wasSymbol = isSymbolMode
        val wasUrdu = isUrduKeyboardLayout
        isUrduKeyboardLayout = (newLang == "ur")
        if (isSymbolMode) isSymbolMode = false

        val needsRebuild = wasSymbol || wasUrdu != isUrduKeyboardLayout
        if (needsRebuild) {
            (context as? UrduKeyboardService)?.recreateKeyboard()
        } else {
            updateKeyboardLayout()
        }
        isAutoCapNext = true
        shiftState = ShiftState.OFF

        val langName = if (newLang == "ur") "اردو" else "English"
        Toast.makeText(context, "Keyboard: $langName", Toast.LENGTH_SHORT).show()
    }

    private fun onVoiceInput() {
        if (::voiceInputManager.isInitialized) {
            val language = if (languageManager.getCurrentLanguage() == "ur") "ur-PK" else "en-US"
            voiceInputManager.startListening(language)
        }
    }

    private fun onSymbolsModeToggle() {
        isSymbolMode = !isSymbolMode
        (context as? UrduKeyboardService)?.recreateKeyboard()
    }

    private fun updateKeyboardLayout() {
        val isUrdu = languageManager.getCurrentLanguage() == "ur"
        val langText = if (isUrdu) "اردو" else "English"
        val langShortText = if (isUrdu) "اردو" else "EN"

        val spaceButtonId = when {
            isSymbolMode -> R.id.sym_key_space
            isUrduKeyboardLayout -> R.id.urdu_key_space
            else -> R.id.key_space
        }
        keyboardView.findViewById<Button>(spaceButtonId)?.text = langText

        val languageButtonId = when {
            isSymbolMode -> R.id.sym_key_language
            isUrduKeyboardLayout -> R.id.urdu_key_language
            else -> R.id.key_language
        }
        keyboardView.findViewById<Button>(languageButtonId)?.text = langShortText
    }

    private fun onSpaceLongPressed(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        (context as? UrduKeyboardService)?.showSystemKeyboardPicker()
    }

    private fun commitText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }
}
