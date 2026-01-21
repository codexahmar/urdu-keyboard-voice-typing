package com.example.urdu_keyboard.keyboard

import android.content.Context
import android.view.LayoutInflater
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
    
    private var currentInputConnection: InputConnection? = null
    private var currentBuffer = StringBuilder()

    /**
     * Create and initialize the keyboard view
     */
    fun createKeyboardView(inflater: LayoutInflater): View {
        keyboardView = inflater.inflate(R.layout.keyboard_view, null)
        
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

        // Setup special keys
        keyboardView.findViewById<Button>(R.id.key_space)?.setOnClickListener {
            onSpacePressed()
        }

        keyboardView.findViewById<Button>(R.id.key_backspace)?.setOnClickListener {
            onBackspacePressed()
        }

        keyboardView.findViewById<Button>(R.id.key_enter)?.setOnClickListener {
            onEnterPressed()
        }

        keyboardView.findViewById<Button>(R.id.key_language)?.setOnClickListener {
            onLanguageSwitch()
        }

        keyboardView.findViewById<Button>(R.id.key_voice)?.setOnClickListener {
            onVoiceInput()
        }
    }

    /**
     * Handle regular key press
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
        languageManager.switchToNextLanguage()
        updateKeyboardLayout()
        currentBuffer.clear()
        
        val langName = when(languageManager.getCurrentLanguage()) {
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
        if (::voiceInputManager.isInitialized) {
            voiceInputManager.cleanup()
        }
    }
}
