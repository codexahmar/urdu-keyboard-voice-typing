package com.codexahmar.urdu_keyboard.keyboard

import android.os.Handler
import android.os.Looper
import android.view.inputmethod.InputConnection
import android.widget.Toast
import android.content.Context
import android.util.Log

/**
 * Handles backspace key with long-press support for continuous deletion
 */
class BackspaceHandler(
    private val context: Context,
    private val getInputConnection: () -> InputConnection?
) {
    private var backspaceHandler: Handler = Handler(Looper.getMainLooper())
    private var isBackspacePressed = false
    private var backspaceRunnable: Runnable? = null
    
    // Configuration
    private val initialDelayMs = 500L  // Initial delay before continuous deletion starts
    private val repeatDelayMs = 50L    // Delay between each deletion during continuous delete

    companion object {
        private const val TAG = "BackspaceHandler"
    }

    /**
     * Handle backspace key down (start of press)
     */
    fun onBackspaceDown() {
        if (isBackspacePressed) return
        
        isBackspacePressed = true
        
        // Delete one character immediately on first press
        performDeletion()
        
        // Start the continuous deletion after initial delay
        backspaceRunnable = object : Runnable {
            override fun run() {
                performDeletion()
                backspaceHandler.postDelayed(this, repeatDelayMs)
            }
        }
        
        // Schedule continuous deletion after initial delay
        backspaceHandler.postDelayed(backspaceRunnable!!, initialDelayMs)
    }
    
    /**
     * Perform actual deletion
     */
    private fun performDeletion() {
        val ic = getInputConnection()
        if (ic == null) {
            Log.e(TAG, "InputConnection is null, cannot delete")
            return
        }
        
        try {
            ic.deleteSurroundingText(1, 0)
            Log.d(TAG, "Deleted character successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting character: ${e.message}")
        }
    }

    /**
     * Handle backspace key up (end of press)
     */
    fun onBackspaceUp() {
        isBackspacePressed = false
        
        backspaceRunnable?.let {
            backspaceHandler.removeCallbacks(it)
        }
        backspaceRunnable = null
    }

    /**
     * Check if backspace is currently pressed
     */
    fun isPressed(): Boolean {
        return isBackspacePressed
    }

    /**
     * Cleanup resources
     */
    fun cleanup() {
        backspaceRunnable?.let {
            backspaceHandler.removeCallbacks(it)
        }
        isBackspacePressed = false
    }
}
