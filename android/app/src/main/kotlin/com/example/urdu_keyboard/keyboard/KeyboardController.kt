package com.example.urdu_keyboard.keyboard

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import com.example.urdu_keyboard.R
import com.example.urdu_keyboard.utils.PreferenceHelper

/**
 * Controller that manages keyboard view and input logic
 */
class KeyboardController(
    private val context: Context,
    private val preferenceHelper: PreferenceHelper
) {
    private lateinit var keyboardView: View
    private val languageManager = LanguageManager(context, preferenceHelper)
    private val phoneticMapper = PhoneticMapper()
    private lateinit var voiceInputManager: VoiceInputManager
    private lateinit var backspaceHandler: BackspaceHandler
    
    private var currentInputConnection: InputConnection? = null
    private var currentBuffer = StringBuilder()
    private var isUrduKeyboardLayout = false  // Track which layout is active
    
    companion object {
        private const val TAG = "KeyboardController"
    }
    private var isArabicKeyboardLayout = false  // Track Arabic layout

    // Urdu keyboard letters (40 common Urdu letters)
    private val urduLetters = arrayOf(
        // Row 1 (11 keys)
        "ض", "ص", "ث", "ق", "ف", "غ", "ع", "ہ", "خ", "ح", "ج",
        // Row 2 (10 keys) 
        "چ", "ظ", "ط", "ز", "ر", "ذ", "د", "ڈ", "خ", "ح",
        // Row 3 (8 keys)
        "ش", "س", "ژ", "ز", "ص", "ط", "ض", "ظ",
        // Row 4 (11 keys)
        "ف", "ق", "ک", "گ", "ل", "م", "ن", "و", "ہ", "ی", "ے"
    )

    // Arabic keyboard letters
    private val arabicLetters = arrayOf(
        // Row 1
        "ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج",
        // Row 2
        "ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك",
        // Row 3
        "ظ", "ط", "ذ", "د", "ز", "ر", "و",
        // Row 4
        "ئ", "ء", "ؤ", "ة", "ى", "لا", "آ"
    )

    init {
        // Initialize keyboard layout based on saved language
        val currentLang = languageManager.getCurrentLanguage()
        isUrduKeyboardLayout = (currentLang == "ur")
        isArabicKeyboardLayout = (currentLang == "ar")
    }

    /**
     * Create and initialize the keyboard view
     */
    fun createKeyboardView(inflater: LayoutInflater): View {
        // Choose layout based on current language
        val layoutId = when {
            isUrduKeyboardLayout -> R.layout.keyboard_urdu
            isArabicKeyboardLayout -> R.layout.keyboard_arabic
            else -> R.layout.keyboard_view
        }
        
        keyboardView = inflater.inflate(layoutId, null)
        
        // Initialize backspace handler with lambda to get current input connection
        backspaceHandler = BackspaceHandler(context) {
            currentInputConnection
        }
        
        // Initialize voice input manager
        if (context is UrduKeyboardService) {
            voiceInputManager = VoiceInputManager(context, context)
        }
        
        // Setup keyboard buttons
        setupKeyboardButtons()
        
        // Update keyboard layout based on current language
        updateKeyboardLayout()
        
        return keyboardView
    }

    /**
     * Setup all keyboard button click listeners
     */
    private fun setupKeyboardButtons() {
        when {
            isUrduKeyboardLayout -> setupUrduKeyboardButtons()
            isArabicKeyboardLayout -> setupArabicKeyboardButtons()
            else -> setupEnglishKeyboardButtons()
        }
        setupCommonButtons()
    }

    /**
     * Setup English/Roman keyboard buttons
     */
    private fun setupEnglishKeyboardButtons() {
        // Setup letter keys
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
                onKeyPressed(key)
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

    /**
     * Setup Arabic keyboard buttons
     */
    private fun setupArabicKeyboardButtons() {
        arabicLetters.forEachIndexed { index, letter ->
            val buttonId = context.resources.getIdentifier(
                "arabic_key_${index + 1}",
                "id",
                context.packageName
            )
            keyboardView.findViewById<Button>(buttonId)?.setOnClickListener {
                onArabicKeyPressed(letter)
            }
        }
    }

    /**
     * Setup common buttons (space, backspace, enter, language, voice)
     */
    private fun setupCommonButtons() {
        val prefix = when {
            isUrduKeyboardLayout -> "urdu_key_"
            isArabicKeyboardLayout -> "arabic_key_"
            else -> "key_"
        }
        
        // Backspace button ID is different - no "_key_" in the middle
        val backspacePrefix = when {
            isUrduKeyboardLayout -> "urdu_"
            isArabicKeyboardLayout -> "arabic_"
            else -> "key_"
        }
        
        // Space button
        keyboardView.findViewById<Button>(
            context.resources.getIdentifier("${prefix}space", "id", context.packageName)
        )?.setOnClickListener {
            onSpacePressed()
        }

        // Backspace with long-press support
        val backspaceId = context.resources.getIdentifier("${backspacePrefix}backspace", "id", context.packageName)
        Log.d(TAG, "Looking for backspace button with ID: ${backspacePrefix}backspace, resolved ID: $backspaceId")
        val backspaceButton = keyboardView.findViewById<Button>(backspaceId)
        Log.d(TAG, "Backspace button found: ${backspaceButton != null}")
        
        backspaceButton?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Log.d(TAG, "Backspace pressed, layout: Urdu=$isUrduKeyboardLayout, Arabic=$isArabicKeyboardLayout")
                    // Clear buffer if using phonetic typing (English keyboard with Urdu/Arabic language)
                    if (!isUrduKeyboardLayout && !isArabicKeyboardLayout && currentBuffer.isNotEmpty()) {
                        currentBuffer.deleteCharAt(currentBuffer.length - 1)
                    }
                    // Always trigger backspace in input connection
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

        // Enter button
        keyboardView.findViewById<Button>(
            context.resources.getIdentifier("${prefix}enter", "id", context.packageName)
        )?.setOnClickListener {
            onEnterPressed()
        }

        // Language switch button
        keyboardView.findViewById<Button>(
            context.resources.getIdentifier("${prefix}language", "id", context.packageName)
        )?.setOnClickListener {
            onLanguageSwitch()
        }

        // Voice input button
        keyboardView.findViewById<Button>(
            context.resources.getIdentifier("${prefix}voice", "id", context.packageName)
        )?.setOnClickListener {
            onVoiceInput()
        }
    }

    /**
     * Handle regular key press (English/Roman layout)
     */
    private fun onKeyPressed(key: String) {
        when (languageManager.getCurrentLanguage()) {
            "ur" -> {
                // Buffer the character for Urdu phonetic conversion
                currentBuffer.append(key)
                
                // Try to find a match in phonetic mapper
                val suggestions = phoneticMapper.getUrduSuggestions(currentBuffer.toString())
                
                if (suggestions.isNotEmpty()) {
                    // For now, auto-commit the first suggestion
                    // In production, you'd show suggestions in a suggestion strip
                    commitText(suggestions[0])
                    currentBuffer.clear()
                } else {
                    // Keep buffering until we find a match or space is pressed
                    // Show the Roman text temporarily
                    commitText(key)
                }
            }
            else -> {
                // English or other languages - direct input
                commitText(key)
            }
        }
    }

    /**
     * Handle Urdu key press (direct Urdu input)
     */
    private fun onUrduKeyPressed(urduLetter: String) {
        commitText(urduLetter)
    }

    /**
     * Handle Arabic key press (direct Arabic input)
     */
    private fun onArabicKeyPressed(arabicLetter: String) {
        commitText(arabicLetter)
    }

    /**
     * Handle space key
     */
    private fun onSpacePressed() {
        if (currentBuffer.isNotEmpty()) {
            // Check if buffered text has Urdu mapping
            val suggestions = phoneticMapper.getUrduSuggestions(currentBuffer.toString())
            if (suggestions.isNotEmpty()) {
                // Replace the buffered text with Urdu
                deleteCharacters(currentBuffer.length)
                commitText(suggestions[0])
            }
            currentBuffer.clear()
        }
        commitText(" ")
    }

    /**
     * Handle backspace
     */
    private fun onBackspacePressed() {
        if (currentBuffer.isNotEmpty()) {
            currentBuffer.deleteCharAt(currentBuffer.length - 1)
        }
        currentInputConnection?.deleteSurroundingText(1, 0)
    }

    /**
     * Handle enter key
     */
    private fun onEnterPressed() {
        currentInputConnection?.performEditorAction(EditorInfo.IME_ACTION_DONE)
        currentBuffer.clear()
    }

    /**
     * Handle language switch
     */
    private fun onLanguageSwitch() {
        // Save current language
        val currentLang = languageManager.getCurrentLanguage()
        
        // Switch to next language
        languageManager.switchToNextLanguage()
        val newLang = languageManager.getCurrentLanguage()
        
        // Update layout flags
        val oldUrdu = isUrduKeyboardLayout
        val oldArabic = isArabicKeyboardLayout
        
        isUrduKeyboardLayout = (newLang == "ur")
        isArabicKeyboardLayout = (newLang == "ar")
        
        // Check if layout needs to be recreated
        val needsLayoutChange = (oldUrdu != isUrduKeyboardLayout) || (oldArabic != isArabicKeyboardLayout)
        
        if (needsLayoutChange) {
            // Request keyboard to be recreated with new layout
            if (context is UrduKeyboardService) {
                context.recreateKeyboard()
            }
        } else {
            updateKeyboardLayout()
        }
        
        currentBuffer.clear()
        
        val langName = when(newLang) {
            "ur" -> "اردو"
            "ar" -> "العربية"
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
                "ur" -> "ur-PK"  // Urdu (Pakistan)
                "ar" -> "ar-SA"  // Arabic (Saudi Arabia)
                else -> "en-US"  // English (US)
            }
            voiceInputManager.startListening(language)
        }
    }

    /**
     * Update keyboard layout based on current language
     */
    private fun updateKeyboardLayout() {
        val languageButton = keyboardView.findViewById<Button>(R.id.key_language)
        languageButton?.text = when(languageManager.getCurrentLanguage()) {
            "ur" -> "اردو"
            "ar" -> "عربي"
            else -> "EN"
        }
    }

    /**
     * Commit text to input field
     */
    private fun commitText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    /**
     * Delete characters
     */
    private fun deleteCharacters(count: Int) {
        currentInputConnection?.deleteSurroundingText(count, 0)
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
        currentBuffer.clear()
        Log.d(TAG, "onStartInput: InputConnection = ${if (ic != null) "valid" else "null"}")
    }

    /**
     * Called when input finishes
     */
    fun onFinishInput() {
        currentInputConnection = null
        currentBuffer.clear()
    }

    /**
     * Handle key press from service
     */
    fun handleKeyPress(key: String, ic: InputConnection) {
        currentInputConnection = ic
        onKeyPressed(key)
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
