package com.codexahmar.urdu_keyboard.keyboard

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.codexahmar.urdu_keyboard.R

/**
 * KeyPopupOverlay — High-performance floating key preview & long-press variant selector.
 *
 * Implements Google Gboard standard:
 *  - Floating rounded bubble directly above the touched key on down (0ms latency).
 *  - High-contrast 24sp typography for instant readability.
 *  - Seamless horizontal expansion on sustained hold (~280ms) for secondary letters/diacritics.
 *  - Zero WindowManager IPC / Zero extra window allocation: renders directly in the IME view tree.
 */
class KeyPopupOverlay(context: Context) : FrameLayout(context) {

    private val bubbleContainer: LinearLayout
    private val mainPreviewText: TextView
    private val variantsRow: LinearLayout
    private val variantViews = mutableListOf<TextView>()

    private var activeAnchor: View? = null
    private var activeVariants: Array<String>? = null
    private var selectedVariantIndex = -1
    private var isVariantsMode = false

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        isClickable = false
        isFocusable = false
        clipChildren = false
        clipToPadding = false

        bubbleContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            visibility = View.GONE
            elevation = dpToPx(8f)
            setPadding(dpToPx(8f).toInt(), dpToPx(6f).toInt(), dpToPx(8f).toInt(), dpToPx(6f).toInt())
        }

        mainPreviewText = TextView(context).apply {
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            typeface = Typeface.DEFAULT_BOLD
            includeFontPadding = false
        }
        bubbleContainer.addView(mainPreviewText)

        variantsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            visibility = View.GONE
            setPadding(0, dpToPx(4f).toInt(), 0, 0)
        }
        bubbleContainer.addView(variantsRow)

        addView(bubbleContainer)
    }

    /**
     * Show single key preview bubble directly above the touched key.
     */
    fun showPreview(anchor: View, text: String, isNight: Boolean) {
        activeAnchor = anchor
        activeVariants = null
        isVariantsMode = false
        selectedVariantIndex = -1

        val bgRes = if (isNight) R.drawable.key_popup_bg_dark else R.drawable.key_popup_bg
        bubbleContainer.background = ContextCompat.getDrawable(context, bgRes)

        val textColor = if (isNight) Color.parseColor("#E8E8EA") else Color.parseColor("#1A1C1E")
        mainPreviewText.setTextColor(textColor)
        mainPreviewText.text = text
        mainPreviewText.visibility = View.VISIBLE

        variantsRow.visibility = View.GONE
        variantsRow.removeAllViews()
        variantViews.clear()

        positionBubble(anchor, minWidthDp = 50f)
        bubbleContainer.visibility = View.VISIBLE
    }

    /**
     * Expand bubble to display long-press secondary variants.
     */
    fun showVariants(anchor: View, variants: Array<String>, isNight: Boolean) {
        if (variants.isEmpty()) return
        activeAnchor = anchor
        activeVariants = variants
        isVariantsMode = true
        selectedVariantIndex = 0

        val bgRes = if (isNight) R.drawable.key_popup_bg_dark else R.drawable.key_popup_bg
        bubbleContainer.background = ContextCompat.getDrawable(context, bgRes)

        variantsRow.removeAllViews()
        variantViews.clear()
        variantsRow.visibility = View.VISIBLE

        val textColor = if (isNight) Color.parseColor("#E8E8EA") else Color.parseColor("#1A1C1E")
        val accentColor = ContextCompat.getColor(context, R.color.kb_accent)

        for (i in variants.indices) {
            val variant = variants[i]
            val tv = TextView(context).apply {
                text = variant
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                includeFontPadding = false
                val padH = dpToPx(12f).toInt()
                val padV = dpToPx(6f).toInt()
                setPadding(padH, padV, padH, padV)
                background = ContextCompat.getDrawable(context, R.drawable.key_popup_variant_bg)
                setTextColor(if (i == 0) Color.WHITE else textColor)
                isSelected = (i == 0)
            }
            variantViews.add(tv)
            variantsRow.addView(tv)
        }

        val totalWidthDp = maxOf(60f, (variants.size * 44f) + 16f)
        positionBubble(anchor, minWidthDp = totalWidthDp)
    }

    /**
     * Update highlighted variant based on horizontal finger motion.
     */
    fun updateHover(rawX: Float) {
        if (!isVariantsMode || variantViews.isEmpty()) return

        val loc = IntArray(2)
        var closestIndex = 0
        var minDistance = Float.MAX_VALUE

        for (i in variantViews.indices) {
            val v = variantViews[i]
            v.getLocationOnScreen(loc)
            val centerX = loc[0] + v.width / 2f
            val dist = kotlin.math.abs(rawX - centerX)
            if (dist < minDistance) {
                minDistance = dist
                closestIndex = i
            }
        }

        if (closestIndex != selectedVariantIndex) {
            selectedVariantIndex = closestIndex
            val isNight = (context.resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
            val defaultColor = if (isNight) Color.parseColor("#E8E8EA") else Color.parseColor("#1A1C1E")

            for (i in variantViews.indices) {
                val v = variantViews[i]
                val selected = (i == selectedVariantIndex)
                v.isSelected = selected
                v.setTextColor(if (selected) Color.WHITE else defaultColor)
            }
        }
    }

    /**
     * Get currently selected variant character when lifted.
     */
    fun getSelectedVariant(): String? {
        if (!isVariantsMode) return null
        val vars = activeVariants ?: return null
        return if (selectedVariantIndex in vars.indices) {
            vars[selectedVariantIndex]
        } else {
            vars.firstOrNull()
        }
    }

    fun isShowing(): Boolean = bubbleContainer.visibility == View.VISIBLE

    fun isVariantsShowing(): Boolean = isVariantsMode && isShowing()

    /**
     * Dismiss popup bubble immediately.
     */
    fun dismiss() {
        bubbleContainer.visibility = View.GONE
        activeAnchor = null
        activeVariants = null
        isVariantsMode = false
        selectedVariantIndex = -1
    }

    private fun positionBubble(anchor: View, minWidthDp: Float) {
        val rootLoc = IntArray(2)
        getLocationInWindow(rootLoc)

        val anchorLoc = IntArray(2)
        anchor.getLocationInWindow(anchorLoc)

        val anchorX = anchorLoc[0] - rootLoc[0]
        val anchorY = anchorLoc[1] - rootLoc[1]

        val minWidthPx = dpToPx(minWidthDp).toInt()
        val bubbleWidth = maxOf(anchor.width + dpToPx(12f).toInt(), minWidthPx)
        val bubbleHeight = anchor.height + dpToPx(16f).toInt()

        // Center bubble over the anchor key
        var targetX = anchorX + (anchor.width - bubbleWidth) / 2
        // Position directly above the key with a 6dp gap
        val targetY = anchorY - bubbleHeight - dpToPx(6f).toInt()

        // Clamp inside parent container bounds with 4dp margin
        val margin = dpToPx(4f).toInt()
        val parentWidth = width
        if (parentWidth > 0) {
            targetX = targetX.coerceIn(margin, parentWidth - bubbleWidth - margin)
        }

        bubbleContainer.layoutParams = (bubbleContainer.layoutParams as LayoutParams).apply {
            width = bubbleWidth
            height = LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP or Gravity.START
            leftMargin = targetX
            topMargin = maxOf(0, targetY)
        }
    }

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    }
}

