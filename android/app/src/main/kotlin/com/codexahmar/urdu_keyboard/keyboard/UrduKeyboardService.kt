package com.codexahmar.urdu_keyboard.keyboard

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import com.codexahmar.urdu_keyboard.R
import com.codexahmar.urdu_keyboard.utils.PreferenceHelper

/**
 * Main InputMethodService for Urdu Keyboard
 * This service is the entry point for the system keyboard
 */
class UrduKeyboardService : InputMethodService() {

    private lateinit var keyboardController: KeyboardController
    private lateinit var preferenceHelper: PreferenceHelper

    override fun onCreate() {
        setTheme(R.style.KeyboardTheme)
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
     * Called when the cursor, selection, or composing span changes in the target editor.
     * Crucial for synchronizing composing text and preventing stale backspace deletions.
     */
    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(
            oldSelStart,
            oldSelEnd,
            newSelStart,
            newSelEnd,
            candidatesStart,
            candidatesEnd
        )
        if (::keyboardController.isInitialized) {
            keyboardController.onUpdateSelection(
                oldSelStart,
                oldSelEnd,
                newSelStart,
                newSelEnd,
                candidatesStart,
                candidatesEnd
            )
        }
    }

    /**
     * Intercept hardware key events from physical keyboards / emulators.
     * When emoji mode is active, routes typing and backspace directly into the in-place emoji search bar.
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (::keyboardController.isInitialized && keyboardController.isEmojiModeActive()) {
            if (keyboardController.handleEmojiSearchKeyEvent(keyCode, event)) {
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (::keyboardController.isInitialized && keyboardController.isEmojiModeActive()) {
            if (keyboardController.handleEmojiSearchKeyUp(keyCode, event)) {
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    /**
     * Recreate keyboard view (called when switching layouts)
     * Now performs an instant zero-window-detachment layer swap
     */
    fun recreateKeyboard() {
        keyboardController.updateActiveLayer()
    }

    /**
     * Insert text from voice recognition
     */
    fun insertVoiceText(text: String) {
        if (::keyboardController.isInitialized) {
            keyboardController.commitVoiceText(text)
        } else {
            val ic: InputConnection? = currentInputConnection
            ic?.commitText(text, 1)
        }
    }

    /**
     * Open keyboard settings
     */
    fun openKeyboardSettings() {
        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    /**
     * Open the system input method picker (list of installed keyboards).
     */
    fun showSystemKeyboardPicker() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showInputMethodPicker()
    }

    override fun onDestroy() {
        keyboardController.cleanup()
        super.onDestroy()
    }
}
