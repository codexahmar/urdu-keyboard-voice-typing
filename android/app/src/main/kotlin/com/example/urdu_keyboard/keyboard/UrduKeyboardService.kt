package com.codexahmar.urdu_keyboard.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.content.Intent
import android.provider.Settings
import com.codexahmar.urdu_keyboard.utils.PreferenceHelper

/**
 * Main InputMethodService for Urdu Smart Keyboard
 * This service is the entry point for the system keyboard
 */
class UrduKeyboardService : InputMethodService() {

    private lateinit var keyboardController: KeyboardController
    private lateinit var preferenceHelper: PreferenceHelper

    override fun onCreate() {
        super.onCreate()
        preferenceHelper = PreferenceHelper(this)
        keyboardController = KeyboardController(this, preferenceHelper)
    }

    /**
     * Called when the input view should be created
     * This is where we inflate our keyboard layout
     */
    override fun onCreateInputView(): View {
        return keyboardController.createKeyboardView(layoutInflater)
    }

    /**
     * Called when a new editor starts
     * We can customize keyboard based on input type
     */
    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        
        // Get current input connection
        val ic: InputConnection? = currentInputConnection
        keyboardController.onStartInput(ic, info)
    }

    /**
     * Called when keyboard is finishing
     */
    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        keyboardController.onFinishInput()
    }

    /**
     * Handle key press events
     */
    fun handleKeyPress(key: String) {
        val ic: InputConnection? = currentInputConnection
        ic?.let {
            keyboardController.handleKeyPress(key, it)
        }
    }

    /**
     * Handle backspace
     */
    fun handleBackspace() {
        val ic: InputConnection? = currentInputConnection
        ic?.deleteSurroundingText(1, 0)
    }

    /**
     * Handle space key
     */
    fun handleSpace() {
        val ic: InputConnection? = currentInputConnection
        ic?.commitText(" ", 1)
    }

    /**
     * Handle enter/return key
     */
    fun handleEnter() {
        val ic: InputConnection? = currentInputConnection
        ic?.commitText("\n", 1)
    }

    /**
     * Switch language
     */
    fun switchLanguage() {
        keyboardController.switchLanguage()
    }

    /**
     * Start voice input
     */
    fun startVoiceInput() {
        keyboardController.startVoiceInput()
    }

    /**
     * Recreate keyboard view (called when switching layouts)
     */
    fun recreateKeyboard() {
        setInputView(onCreateInputView())
    }

    /**
     * Insert text from voice recognition
     */
    fun insertVoiceText(text: String) {
        val ic: InputConnection? = currentInputConnection
        ic?.commitText(text, 1)
    }

    /**
     * Open keyboard settings
     */
    fun openKeyboardSettings() {
        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    override fun onDestroy() {
        keyboardController.cleanup()
        super.onDestroy()
    }
}
