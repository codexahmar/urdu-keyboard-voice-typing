package com.codexahmar.urdu_keyboard.keyboard

import android.content.Context
import android.content.res.Configuration
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.emoji2.emojipicker.EmojiPickerView
import com.codexahmar.urdu_keyboard.R
import kotlin.math.max

/**
 * Manages Keyboard Layer Views (English, Urdu, Symbols, Emoji).
 * Implements Gboard-grade instant in-memory layer visibility swapping (0ms latency,
 * zero window detachment, zero setInputView() recreation).
 */
class KeyboardLayerManager(
    private val context: Context,
    private val onKeyCommit: (String) -> Unit
) {
    var rootContainer: FrameLayout? = null
        private set

    var englishView: View? = null
        private set
    var urduView: View? = null
        private set
    var symbolsView: View? = null
        private set
    var emojiView: View? = null
        private set
    var currentActiveView: View? = null
        private set

    private var lastKnownBottomInsetPx = 0

    val isNightMode: Boolean
        get() = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    private fun dpToPx(dp: Int): Int =
        (dp * context.resources.displayMetrics.density).toInt()

    fun getOrCreateRootContainer(): FrameLayout {
        rootContainer?.let { return it }
        val container = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            isMotionEventSplittingEnabled = true
        }
        rootContainer = container
        applyBottomInsetPadding(container)
        return container
    }

    private fun applyBottomInsetPadding(targetView: View) {
        val extraGap = dpToPx(KeyboardConstants.EXTRA_BOTTOM_GAP_DP)
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

    fun getOrCreateEnglishView(
        inflater: LayoutInflater,
        onWireButtons: (View) -> Unit
    ): View {
        englishView?.let { return it }
        val container = getOrCreateRootContainer()
        val view = inflater.inflate(R.layout.keyboard_view, container, false)
        enableSplitMotionEvents(view)
        onWireButtons(view)
        container.addView(view)
        englishView = view
        return view
    }

    fun getOrCreateUrduView(
        inflater: LayoutInflater,
        onWireButtons: (View) -> Unit
    ): View {
        urduView?.let { return it }
        val container = getOrCreateRootContainer()
        val view = inflater.inflate(R.layout.keyboard_urdu, container, false)
        enableSplitMotionEvents(view)
        onWireButtons(view)
        container.addView(view)
        urduView = view
        return view
    }

    fun getOrCreateSymbolsView(
        inflater: LayoutInflater,
        onWireButtons: (View) -> Unit
    ): View {
        symbolsView?.let { return it }
        val container = getOrCreateRootContainer()
        val view = inflater.inflate(R.layout.keyboard_symbols, container, false)
        enableSplitMotionEvents(view)
        onWireButtons(view)
        container.addView(view)
        symbolsView = view
        return view
    }

    fun getOrCreateEmojiView(
        inflater: LayoutInflater,
        onWireButtons: (View) -> Unit
    ): View {
        emojiView?.let { return it }
        val container = getOrCreateRootContainer()
        val view = inflater.inflate(R.layout.keyboard_emoji, container, false)
        enableSplitMotionEvents(view)
        onWireButtons(view)
        container.addView(view)
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

    fun activateLayer(targetView: View) {
        currentActiveView = targetView
        englishView?.visibility = if (targetView === englishView) View.VISIBLE else View.GONE
        urduView?.visibility = if (targetView === urduView) View.VISIBLE else View.GONE
        symbolsView?.visibility = if (targetView === symbolsView) View.VISIBLE else View.GONE
        emojiView?.visibility = if (targetView === emojiView) View.VISIBLE else View.GONE
    }

    /**
     * Dynamically updates Row 1 digits, punctuation, and labels on Symbols layout
     * based on whether Urdu or English keyboard mode is active.
     */
    fun refreshSymbolsKeys(view: View, isUrduLayout: Boolean, touchHandler: KeyTouchHandler) {
        val digits = if (isUrduLayout) KeyboardConstants.URDU_DIGITS else KeyboardConstants.ENGLISH_DIGITS

        for (i in 0..9) {
            val btn = view.findViewById<Button?>(KeyboardConstants.SYM_DIGIT_IDS[i]) ?: continue
            val symbol = digits[i]
            btn.text = symbol
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, if (isUrduLayout) 22f else 19f)
            touchHandler.setFastTapListener(btn) { onKeyCommit(symbol) }
        }

        val punctPairs = if (isUrduLayout) KeyboardConstants.URDU_PUNCTUATION else KeyboardConstants.ENGLISH_PUNCTUATION
        for ((id, sym) in punctPairs) {
            val btn = view.findViewById<Button?>(id) ?: continue
            btn.text = sym
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, if (isUrduLayout) 22f else 19f)
            touchHandler.setFastTapListener(btn) { onKeyCommit(sym) }
        }

        val abcBtn = view.findViewById<Button?>(R.id.sym_key_abc)
        abcBtn?.text = if (isUrduLayout) "اردو" else "ABC"

        val langBtn = view.findViewById<Button?>(R.id.sym_key_language)
        langBtn?.text = if (isUrduLayout) "EN" else "اردو"
    }

    fun styleEmojiPickerTitles(picker: EmojiPickerView?) {
        picker ?: return
        val targetColor = if (isNightMode) 0xFFE8E8EA.toInt() else 0xFF1A1C1E.toInt()

        fun updateChildrenColors(root: View) {
            if (root is TextView) {
                if (root.text.isNotEmpty() && !Character.isSurrogate(root.text[0])) {
                    root.setTextColor(targetColor)
                }
            }
            if (root is ViewGroup) {
                for (i in 0 until root.childCount) {
                    updateChildrenColors(root.getChildAt(i))
                }
            }
        }

        picker.post { updateChildrenColors(picker) }
        picker.viewTreeObserver.addOnDrawListener {
            updateChildrenColors(picker)
        }
    }

    fun updateKeyboardLayoutLabels(isUrduLayout: Boolean) {
        val spaceText = if (isUrduLayout) "اردو" else "English"
        val targetLangText = if (isUrduLayout) "EN" else "اردو"

        englishView?.findViewById<Button>(R.id.key_space)?.text = spaceText
        englishView?.findViewById<Button>(R.id.key_language)?.text = targetLangText

        urduView?.findViewById<Button>(R.id.urdu_key_space)?.text = spaceText
        urduView?.findViewById<Button>(R.id.urdu_key_language)?.text = targetLangText

        symbolsView?.findViewById<Button>(R.id.sym_key_space)?.text = spaceText
        symbolsView?.findViewById<Button>(R.id.sym_key_language)?.text = targetLangText
        symbolsView?.findViewById<Button>(R.id.sym_key_abc)?.text = if (isUrduLayout) "اردو" else "ABC"

        emojiView?.findViewById<Button>(R.id.emoji_key_abc)?.text = if (isUrduLayout) "اردو" else "ABC"
        emojiView?.findViewById<TextView>(R.id.emoji_search_text)?.text =
            if (isUrduLayout) "ایموجی تلاش کریں" else "Search emoji"
    }

    fun cleanup() {
        rootContainer?.removeAllViews()
        rootContainer = null
        englishView = null
        urduView = null
        symbolsView = null
        emojiView = null
        currentActiveView = null
    }
}

