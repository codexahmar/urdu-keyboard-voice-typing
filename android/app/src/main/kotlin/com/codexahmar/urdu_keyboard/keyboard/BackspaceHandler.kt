package com.codexahmar.urdu_keyboard.keyboard

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.InputConnection

/**
 * Handles backspace key with dynamic acceleration for ultra-smooth and fast deletion.
 * Implements standard flagship keyboard (Gboard/SwiftKey) acceleration curves:
 *  - Instant single-character deletion on initial touch.
 *  - 320 ms responsive initial hold delay.
 *  - Smooth exponential ramp from 65 ms down to 25 ms repeat interval.
 *  - Turbo multi-character deletion after sustained hold (> 1.2s).
 */
class BackspaceHandler(
    private val context: Context,
    private val getInputConnection: () -> InputConnection?,
    private val onDelete: ((Int) -> Boolean)? = null
) {
    private val backspaceHandler = Handler(Looper.getMainLooper())
    private var isBackspacePressed = false
    private var backspaceRunnable: Runnable? = null
    private var currentDelay = INITIAL_DELAY_MS
    private var repeatCount = 0

    companion object {
        private const val TAG = "BackspaceHandler"
        private const val INITIAL_DELAY_MS = 320L
        private const val BASE_REPEAT_DELAY_MS = 65L
        private const val MIN_REPEAT_DELAY_MS = 25L
        private const val ACCELERATION_FACTOR = 0.88f
        // After this many repeats (~1.2 seconds), delete 2 characters per tick for fast paragraph clearing
        private const val TURBO_REPEAT_THRESHOLD = 18
    }

    /**
     * Handle backspace key down (start of press)
     */
    fun onBackspaceDown() {
        if (isBackspacePressed) return
        isBackspacePressed = true
        repeatCount = 0
        currentDelay = BASE_REPEAT_DELAY_MS

        // Delete one character immediately on first tap
        performDeletion(charCount = 1)

        // Schedule accelerated continuous deletion
        backspaceRunnable = object : Runnable {
            override fun run() {
                if (!isBackspacePressed) return

                repeatCount++
                val deleteCount = if (repeatCount > TURBO_REPEAT_THRESHOLD) 2 else 1
                performDeletion(charCount = deleteCount)

                // Accelerate smoothly towards MIN_REPEAT_DELAY_MS
                currentDelay = maxOf(
                    MIN_REPEAT_DELAY_MS,
                    (currentDelay * ACCELERATION_FACTOR).toLong()
                )

                backspaceHandler.postDelayed(this, currentDelay)
            }
        }

        backspaceHandler.postDelayed(backspaceRunnable!!, INITIAL_DELAY_MS)
    }

    /**
     * Perform actual deletion
     */
    private fun performDeletion(charCount: Int = 1) {
        if (onDelete?.invoke(charCount) == true) {
            return
        }

        val ic = getInputConnection()
        if (ic == null) {
            Log.e(TAG, "InputConnection is null, cannot delete")
            return
        }

        try {
            // 1. If text is currently selected, commit empty string to delete the selection
            val selectedText = ic.getSelectedText(0)
            if (!selectedText.isNullOrEmpty()) {
                ic.commitText("", 1)
                return
            }

            // 2. Delete charCount code points before the cursor (RTL- and Unicode-safe)
            val deleted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ic.deleteSurroundingTextInCodePoints(charCount, 0)
            } else {
                ic.deleteSurroundingText(charCount, 0)
            }

            // 3. Fallback to native hardware KEYCODE_DEL if deleteSurroundingText was unhandled
            if (!deleted) {
                val ims = context as? InputMethodService
                for (i in 0 until charCount) {
                    ims?.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting text: ${e.message}")
            try {
                val ims = context as? InputMethodService
                for (i in 0 until charCount) {
                    ims?.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
                }
            } catch (_: Exception) {}
        }
    }

    /**
     * Handle backspace key up (end of press)
     */
    fun onBackspaceUp() {
        isBackspacePressed = false
        repeatCount = 0
        backspaceRunnable?.let(backspaceHandler::removeCallbacks)
        backspaceRunnable = null
    }

    /**
     * Check if backspace is currently pressed
     */
    fun isPressed(): Boolean = isBackspacePressed

    /**
     * Cleanup resources
     */
    fun cleanup() {
        isBackspacePressed = false
        repeatCount = 0
        backspaceRunnable?.let(backspaceHandler::removeCallbacks)
        backspaceHandler.removeCallbacksAndMessages(null)
        backspaceRunnable = null
    }
}
