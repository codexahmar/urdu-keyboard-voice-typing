package com.codexahmar.urdu_keyboard.keyboard

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
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
 * Controller that manages keyboard view and input logic
 */
class KeyboardController(
    private val context: Context,
    private val preferenceHelper: PreferenceHelper
) {
    private lateinit var keyboardView: View
    private val languageManager = LanguageManager(context, preferenceHelper)
    private lateinit var voiceInputManager: VoiceInputManager
    private lateinit var backspaceHandler: BackspaceHandler
    
    private var currentInputConnection: InputConnection? = null
    private var isUrduKeyboardLayout = false
    private var isShiftEnabled = false
    private var isAutoCapNext = true
    private var isSymbolMode = false
    
    companion object {
        private const val TAG = "KeyboardController"
    }

    // Urdu keyboard letters (40 keys)
    private val urduLetters = arrayOf(
        "ض", "ص", "ث", "ق", "ف", "غ", "ع", "ہ", "خ", "ح", "ج",
        "چ", "ظ", "ط", "ز", "ر", "ذ", "د", "ڈ", "خ", "ح",
        "ش", "س", "ژ", "ز", "ص", "ط", "ض", "ظ",
        "ف", "ق", "ک", "گ", "ل", "م", "ن", "و", "ہ", "ی", "ے"
    )

    init {
        val currentLang = languageManager.getCurrentLanguage()
        isUrduKeyboardLayout = (currentLang == "ur")
    }

    /**
     * Create and initialize the keyboard view
     */
    fun createKeyboardView(inflater: LayoutInflater): View {
        val layoutId = when {
            isSymbolMode -> R.layout.keyboard_symbols
            isUrduKeyboardLayout -> R.layout.keyboard_urdu
            else -> R.layout.keyboard_view
        }
        
        keyboardView = inflater.inflate(layoutId, null)
        applyBottomInsetPadding()
        
        backspaceHandler = BackspaceHandler(context) {
            currentInputConnection
        }
        
        if (context is UrduKeyboardService) {
            voiceInputManager = VoiceInputManager(context, context)
        }
        
        setupKeyboardButtons()
        updateKeyboardLayout()
        refreshEnglishKeyLabels()
        
        return keyboardView
    }

    private fun applyBottomInsetPadding() {
        ViewCompat.setOnApplyWindowInsetsListener(keyboardView) { view, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            val minBottomPadding = dpToPx(6)
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                max(minBottomPadding, bottomInset)
            )
            insets
        }
        ViewCompat.requestApplyInsets(keyboardView)
    }

    private fun dpToPx(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

    /**
     * Setup all keyboard button click listeners
     */
    private fun setupKeyboardButtons() {
        if (isSymbolMode) {
            setupSymbolButtons()
        } else {
            if (isUrduKeyboardLayout) setupUrduKeyboardButtons() else setupEnglishKeyboardButtons()
            setupNumberButtons()
            setupPunctuationButtons()
        }
        setupCommonButtons()
    }

    /**
     * Setup English/Roman keyboard buttons
     */
    private fun setupEnglishKeyboardButtons() {
        val letterKeys = arrayOf(
            "q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
            "a", "s", "d", "f", "g", "h", "j", "k", "l",
            "z", "x", "c", "v", "b", "n", "m"
        )

        letterKeys.forEach { key ->
            val buttonId = context.resources.getIdentifier(
                "key_$key",
                "id",
                context.packageName
            )
            keyboardView.findViewById<Button>(buttonId)?.setOnClickListener {
                onEnglishKeyPressed(key)
            }
        }
    }

    /**
     * Setup Urdu keyboard buttons
     */
    private fun setupUrduKeyboardButtons() {
        urduLetters.forEachIndexed { index, letter ->
            val buttonId = context.resources.getIdentifier(
                "urdu_key_${index + 1}",
                "id",
                context.packageName
            )
            keyboardView.findViewById<Button>(buttonId)?.setOnClickListener {
                onUrduKeyPressed(letter)
            }
        }
    }

    private fun setupNumberButtons() {
        val prefix = if (isUrduKeyboardLayout) "urdu_key_" else "key_"
        val numerals = if (isUrduKeyboardLayout) {
            arrayOf("۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹", "۰")
        } else {
            arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        }

        val ids = arrayOf(
            "${prefix}num_1", "${prefix}num_2", "${prefix}num_3", "${prefix}num_4", "${prefix}num_5",
            "${prefix}num_6", "${prefix}num_7", "${prefix}num_8", "${prefix}num_9", "${prefix}num_0"
        )

        ids.forEachIndexed { index, idName ->
            val buttonId = context.resources.getIdentifier(idName, "id", context.packageName)
            keyboardView.findViewById<Button>(buttonId)?.setOnClickListener {
                commitText(numerals[index])
                isAutoCapNext = false
            }
        }
    }

    private fun setupPunctuationButtons() {
        val prefix = if (isUrduKeyboardLayout) "urdu_key_" else "key_"
        val comma = if (isUrduKeyboardLayout) "،" else ","
        val dot = "."
        val question = if (isUrduKeyboardLayout) "؟" else "?"

        keyboardView.findViewById<Button>(
            context.resources.getIdentifier("${prefix}comma", "id", context.packageName)
        )?.setOnClickListener {
            onPunctuationPressed(comma)
        }

        keyboardView.findViewById<Button>(
            context.resources.getIdentifier("${prefix}dot", "id", context.packageName)
        )?.setOnClickListener {
            onPunctuationPressed(dot)
        }

        keyboardView.findViewById<Button>(
            context.resources.getIdentifier("${prefix}question", "id", context.packageName)
        )?.setOnClickListener {
            onPunctuationPressed(question)
        }

        keyboardView.findViewById<Button>(
            context.resources.getIdentifier("${prefix}fullstop", "id", context.packageName)
        )?.setOnClickListener {
            onPunctuationPressed("۔")
        }

        val extraSymbols = arrayOf(
            Pair("${prefix}apostrophe", "'"),
            Pair("${prefix}slash", "/"),
            Pair("${prefix}at", "@"),
            Pair("${prefix}minus", "-"),
            Pair("${prefix}plus", "+")
        )

        extraSymbols.forEach { (idName, symbol) ->
            val buttonId = context.resources.getIdentifier(idName, "id", context.packageName)
            keyboardView.findViewById<Button>(buttonId)?.setOnClickListener {
                onPunctuationPressed(symbol)
            }
        }
    }

    /**
     * Setup symbol keyboard buttons (numbers and special characters)
     */
    private fun setupSymbolButtons() {
        // Number buttons (0-9)
        for (i in 0..9) {
            val symbolId = context.resources.getIdentifier("sym_key_$i", "id", context.packageName)
            val symbol = i.toString()
            keyboardView.findViewById<Button>(symbolId)?.setOnClickListener {
                currentInputConnection?.commitText(symbol, 1)
            }
        }

        // Symbol buttons
        val symbolMap = mapOf(
            "sym_key_at" to "@",
            "sym_key_hash" to "#",
            "sym_key_dollar" to "$",
            "sym_key_percent" to "%",
            "sym_key_amp" to "&",
            "sym_key_star" to "*",
            "sym_key_minus" to "-",
            "sym_key_plus" to "+",
            "sym_key_lparen" to "(",
            "sym_key_rparen" to ")",
            "sym_key_slash" to "/",
            "sym_key_quote" to "\"",
            "sym_key_apostrophe" to "'",
            "sym_key_colon" to ":",
            "sym_key_semicolon" to ";",
            "sym_key_exclamation" to "!",
            "sym_key_question" to "?",
            "sym_key_comma" to ",",
            "sym_key_dot" to ".",
            "sym_key_equal" to "=",
            "sym_key_lbracket" to "[",
            "sym_key_rbracket" to "]",
            "sym_key_lbrace" to "{",
            "sym_key_rbrace" to "}",
            "sym_key_backslash" to "\\",
            "sym_key_less" to "<",
            "sym_key_greater" to ">",
            "sym_key_caret" to "^",
            "sym_key_tilde" to "~"
        )

        symbolMap.forEach { (idName, symbol) ->
            val buttonId = context.resources.getIdentifier(idName, "id", context.packageName)
            keyboardView.findViewById<Button>(buttonId)?.setOnClickListener {
                currentInputConnection?.commitText(symbol, 1)
            }
        }
    }

    /**
     * Setup common buttons (space, backspace, enter, language, voice, shift)
     */
    private fun setupCommonButtons() {
        val prefix = when {
            isSymbolMode -> "sym_key_"
            isUrduKeyboardLayout -> "urdu_key_"
            else -> "key_"
        }
        val backspacePrefix = when {
            isSymbolMode -> "sym_"
            isUrduKeyboardLayout -> "urdu_"
            else -> "key_"
        }
        
        keyboardView.findViewById<Button>(
            context.resources.getIdentifier("${prefix}space", "id", context.packageName)
        )?.setOnClickListener {
            onSpacePressed()
        }

        val backspaceId = context.resources.getIdentifier(
            "${backspacePrefix}backspace",
            "id",
            context.packageName
        )
        val backspaceButton = keyboardView.findViewById<Button>(backspaceId)

        backspaceButton?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    backspaceHandler.onBackspaceDown()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    backspaceHandler.onBackspaceUp()
                    true
                }
                else -> false
            }
        }

        keyboardView.findViewById<Button>(
            context.resources.getIdentifier("${prefix}enter", "id", context.packageName)
        )?.setOnClickListener {
            onEnterPressed()
        }

        keyboardView.findViewById<Button>(
            context.resources.getIdentifier("${prefix}language", "id", context.packageName)
        )?.setOnClickListener {
            onLanguageSwitch()
        }

        keyboardView.findViewById<Button>(
            context.resources.getIdentifier("${prefix}voice", "id", context.packageName)
        )?.setOnClickListener {
            onVoiceInput()
        }

        // Handle symbols toggle button (123 in main layout, ABC in symbol layout)
        val toggleButtonId = if (isSymbolMode) {
            context.resources.getIdentifier("sym_key_abc", "id", context.packageName)
        } else {
            context.resources.getIdentifier("${prefix}symbols", "id", context.packageName)
        }
        keyboardView.findViewById<Button>(toggleButtonId)?.setOnClickListener {
            onSymbolsModeToggle()
        }

        if (!isSymbolMode) {
            keyboardView.findViewById<Button>(
                context.resources.getIdentifier("${prefix}shift", "id", context.packageName)
            )?.setOnClickListener {
                onShiftPressed()
            }
        }
    }

    /**
     * Handle English key press with shift/auto-cap behavior.
     */
    private fun onEnglishKeyPressed(key: String) {
        val output = if (shouldUseUppercase()) key.uppercase() else key.lowercase()
        commitText(output)
        isAutoCapNext = false
        if (isShiftEnabled) {
            isShiftEnabled = false
            refreshEnglishKeyLabels()
        }
    }

    /**
     * Handle Urdu key press (direct Urdu input)
     */
    private fun onUrduKeyPressed(urduLetter: String) {
        commitText(urduLetter)
        isAutoCapNext = false
    }

    private fun onPunctuationPressed(symbol: String) {
        commitText(symbol)
        isAutoCapNext = symbol == "." || symbol == "?" || symbol == "؟" || symbol == "!"
        if (isShiftEnabled) {
            isShiftEnabled = false
        }
        refreshEnglishKeyLabels()
    }

    /**
     * Handle space key
     */
    private fun onSpacePressed() {
        commitText(" ")
    }

    /**
     * Handle enter key
     */
    private fun onEnterPressed() {
        commitText("\n")
        isAutoCapNext = true
        if (isShiftEnabled) {
            isShiftEnabled = false
            refreshEnglishKeyLabels()
        }
    }

    private fun onShiftPressed() {
        if (isUrduKeyboardLayout) {
            return
        }
        isShiftEnabled = !isShiftEnabled
        refreshEnglishKeyLabels()
    }

    private fun shouldUseUppercase(): Boolean {
        return isShiftEnabled || isAutoCapNext
    }

    private fun refreshEnglishKeyLabels() {
        if (isUrduKeyboardLayout) {
            return
        }

        val uppercase = shouldUseUppercase()
        val letterKeys = arrayOf(
            "q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
            "a", "s", "d", "f", "g", "h", "j", "k", "l",
            "z", "x", "c", "v", "b", "n", "m"
        )

        letterKeys.forEach { key ->
            val buttonId = context.resources.getIdentifier("key_$key", "id", context.packageName)
            keyboardView.findViewById<Button>(buttonId)?.text = if (uppercase) {
                key.uppercase()
            } else {
                key.lowercase()
            }
        }

        val shiftButton = keyboardView.findViewById<Button>(R.id.key_shift)
        shiftButton?.alpha = if (isShiftEnabled) 1.0f else 0.8f
    }

    /**
     * Handle language switch
     */
    private fun onLanguageSwitch() {
        languageManager.switchToNextLanguage()
        val newLang = languageManager.getCurrentLanguage()
        val oldUrdu = isUrduKeyboardLayout
        isUrduKeyboardLayout = (newLang == "ur")
        val needsLayoutChange = oldUrdu != isUrduKeyboardLayout
        
        if (needsLayoutChange) {
            if (context is UrduKeyboardService) {
                context.recreateKeyboard()
            }
        } else {
            updateKeyboardLayout()
        }
        isAutoCapNext = true
        isShiftEnabled = false

        val langName = when(newLang) {
            "ur" -> "اردو"
            else -> "English"
        }
        Toast.makeText(context, "Keyboard: $langName", Toast.LENGTH_SHORT).show()
    }

    /**
     * Handle voice input button
     */
    private fun onVoiceInput() {
        if (::voiceInputManager.isInitialized) {
            val language = when(languageManager.getCurrentLanguage()) {
                "ur" -> "ur-PK"
                else -> "en-US"
            }
            voiceInputManager.startListening(language)
        }
    }

    /**
     * Toggle between alphabet and symbol modes
     */
    private fun onSymbolsModeToggle() {
        isSymbolMode = !isSymbolMode
        if (context is UrduKeyboardService) {
            context.recreateKeyboard()
        }
    }

    /**
     * Update keyboard layout based on current language
     */
    private fun updateKeyboardLayout() {
        if (isSymbolMode) {
            // Symbol layout always shows "EN" for language button
            val languageButtonId = R.id.sym_key_language
            val languageButton = keyboardView.findViewById<Button>(languageButtonId)
            languageButton?.text = "EN"
        } else {
            val languageButtonId = if (isUrduKeyboardLayout) R.id.urdu_key_language else R.id.key_language
            val languageButton = keyboardView.findViewById<Button>(languageButtonId)
            languageButton?.text = when(languageManager.getCurrentLanguage()) {
                "ur" -> "اردو"
                else -> "EN"
            }
        }
    }

    /**
     * Commit text to input field
     */
    private fun commitText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    /**
     * Switch language programmatically
     */
    fun switchLanguage() {
        onLanguageSwitch()
    }

    /**
     * Start voice input
     */
    fun startVoiceInput() {
        onVoiceInput()
    }

    /**
     * Called when input starts
     */
    fun onStartInput(ic: InputConnection?, info: EditorInfo?) {
        currentInputConnection = ic
        isAutoCapNext = true
        isShiftEnabled = false
        Log.d(TAG, "onStartInput: InputConnection = ${if (ic != null) "valid" else "null"}")
        refreshEnglishKeyLabels()
    }

    /**
     * Called when input finishes
     */
    fun onFinishInput() {
        currentInputConnection = null
        isAutoCapNext = true
        isShiftEnabled = false
    }

    /**
     * Handle key press from service
     */
    fun handleKeyPress(key: String, ic: InputConnection) {
        currentInputConnection = ic
        if (isUrduKeyboardLayout) {
            onUrduKeyPressed(key)
        } else {
            onEnglishKeyPressed(key)
        }
    }

    /**
     * Cleanup resources
     */
    fun cleanup() {
        backspaceHandler.cleanup()
        if (::voiceInputManager.isInitialized) {
            voiceInputManager.cleanup()
        }
    }
}
