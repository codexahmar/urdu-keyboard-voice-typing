package com.codexahmar.urdu_keyboard.keyboard

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.inputmethod.InputConnection
import android.widget.Button

/**
 * High-performance Touch & Gesture Handler for Keyboard Keys.
 * Encapsulates:
 *  - 0ms instant fast-tap listeners for action/mode keys.
 *  - Multi-touch rollover key tracking with floating preview bubble & long-press variants.
 *  - Accelerated Backspace touch processing.
 *  - Space bar swipe scrubbing / cursor glide and long-press IME switcher.
 */
class KeyTouchHandler(
    private val context: Context,
    private val mainHandler: Handler = Handler(Looper.getMainLooper()),
    private val isHapticFeedbackEnabled: () -> Boolean,
    private val isNightMode: () -> Boolean,
    private val getPopupOverlay: () -> KeyPopupOverlay?,
    private val getInputConnection: () -> InputConnection?
) {
    private var pendingKeyView: View? = null
    private var pendingKeyCommit: (() -> Unit)? = null
    private var keyLongPressRunnable: Runnable? = null
    private var spaceLongPressRunnable: Runnable? = null

    private fun dpToPx(dp: Int): Int =
        (dp * context.resources.displayMetrics.density).toInt()

    @Suppress("DEPRECATION")
    fun applyTapFeedback(view: View) {
        if (isHapticFeedbackEnabled()) {
            view.performHapticFeedback(
                HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        }
    }

    /**
     * Instantaneous touch trigger firing on ACTION_DOWN for zero perceived typing latency.
     */
    fun setFastTapListener(button: View?, onTap: () -> Unit) {
        button?.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    view.isPressed = true
                    applyTapFeedback(view)
                    onTap()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
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

    /**
     * Binds standard letter key touch handling:
     * - Immediate preview bubble on ACTION_DOWN.
     * - Long-press timer (280ms) to expand secondary diacritics/variants.
     * - Multi-touch rollover: commits active key if another finger lands.
     * - Slop tolerance (24dp) to avoid accidental drops during fast typing.
     */
    fun bindSmoothKeyTouch(
        button: Button?,
        primaryChar: String,
        variants: Array<String>?,
        onCommitChar: (String) -> Unit
    ) {
        button ?: return
        val overlay = getPopupOverlay()

        button.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    // Multi-touch rollover: commit pending key from previous finger
                    if (pendingKeyView != null && pendingKeyView !== view) {
                        commitPendingKey()
                    }

                    view.isPressed = true
                    applyTapFeedback(view)
                    overlay?.showPreview(view, primaryChar, isNightMode())

                    pendingKeyView = view
                    pendingKeyCommit = { onCommitChar(primaryChar) }

                    keyLongPressRunnable?.let(mainHandler::removeCallbacks)
                    if (!variants.isNullOrEmpty()) {
                        keyLongPressRunnable = Runnable {
                            if (view.isPressed && pendingKeyView === view) {
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                overlay?.showVariants(view, variants, isNightMode())
                            }
                        }
                        mainHandler.postDelayed(keyLongPressRunnable!!, KeyboardConstants.LONG_PRESS_TIMEOUT_MS)
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (overlay?.isVariantsShowing() == true) {
                        overlay.updateHover(event.rawX)
                    } else if (view.isPressed) {
                        val x = event.x
                        val y = event.y
                        val slop = dpToPx(24)
                        if (x < -slop || x > view.width + slop || y < -slop || y > view.height + slop) {
                            view.isPressed = false
                            overlay?.dismiss()
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

                    if (overlay?.isVariantsShowing() == true) {
                        val selected = overlay.getSelectedVariant() ?: variants?.firstOrNull() ?: primaryChar
                        overlay.dismiss()
                        pendingKeyView = null
                        pendingKeyCommit = null
                        onCommitChar(selected)
                    } else if (pendingKeyView === view) {
                        overlay?.dismiss()
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
                    overlay?.dismiss()
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

    fun commitPendingKey() {
        keyLongPressRunnable?.let(mainHandler::removeCallbacks)
        keyLongPressRunnable = null
        pendingKeyCommit?.let { commit ->
            val ic = getInputConnection()
            ic?.beginBatchEdit()
            commit()
            ic?.endBatchEdit()
        }
        pendingKeyView?.isPressed = false
        pendingKeyView = null
        pendingKeyCommit = null
        getPopupOverlay()?.dismiss()
    }

    /**
     * Sets up space bar with:
     * - Tap to insert space or commit composing word.
     * - Horizontal swipe scrubbing to glide the cursor with tick haptics.
     * - Long press to invoke system input method picker.
     */
    fun setupSpaceButton(
        spaceButton: Button?,
        onSpaceTap: () -> Unit,
        onSpaceLongPress: (View) -> Unit
    ) {
        spaceButton ?: return

        val longPressTimeoutMs = ViewConfiguration.getLongPressTimeout().toLong()
        val scrubThresholdPx = dpToPx(14).toFloat()
        val stepDistancePx = dpToPx(12).toFloat()

        var isLongPressHandled = false
        var isScrubbing = false
        var downX = 0f
        var accumulatedX = 0f

        spaceButton.setOnTouchListener { view, event ->
            when (event.actionMasked) {
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
                            onSpaceLongPress(view)
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
                        onSpaceTap()
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
        val ic = getInputConnection() ?: return
        val keyCode = if (steps > 0) KeyEvent.KEYCODE_DPAD_RIGHT else KeyEvent.KEYCODE_DPAD_LEFT
        val count = kotlin.math.abs(steps)
        ic.beginBatchEdit()
        for (i in 0 until count) {
            (context as? android.inputmethodservice.InputMethodService)?.sendDownUpKeyEvents(keyCode)
        }
        ic.endBatchEdit()
    }

    /**
     * Binds backspace button to BackspaceHandler with instant press feedback.
     */
    fun setupBackspaceButton(backspaceBtn: Button?, backspaceHandler: BackspaceHandler) {
        backspaceBtn?.setOnTouchListener { view, event ->
            when (event.actionMasked) {
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

    fun cleanup() {
        commitPendingKey()
        spaceLongPressRunnable?.let(mainHandler::removeCallbacks)
        spaceLongPressRunnable = null
        keyLongPressRunnable?.let(mainHandler::removeCallbacks)
        keyLongPressRunnable = null
        pendingKeyView = null
        pendingKeyCommit = null
    }
}

