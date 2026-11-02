package com.codexahmar.urdu_keyboard.keyboard

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.codexahmar.urdu_keyboard.R

/**
 * Manages the in-place Emoji Search bar, live search query, blinking cursor,
 * and horizontal emoji suggestions strip.
 */
class EmojiSearchController(
    private val context: Context,
    private val mainHandler: Handler = Handler(Looper.getMainLooper()),
    private val applyTapFeedback: (View) -> Unit
) {
    val searchQuery = StringBuilder()
    var isSearchMode = false
        private set

    private var isCursorBlinkOn = true

    private val cursorBlinkRunnable = object : Runnable {
        override fun run() {
            if (!isSearchMode) return
            isCursorBlinkOn = !isCursorBlinkOn
            val cursor = activeCursorView?.get()
            cursor?.visibility = if (isCursorBlinkOn) View.VISIBLE else View.INVISIBLE
            mainHandler.postDelayed(this, 500L)
        }
    }

    private var activeCursorView: java.lang.ref.WeakReference<View>? = null

    private fun dpToPx(dp: Int): Int =
        (dp * context.resources.displayMetrics.density).toInt()

    fun startCursorBlink(cursorView: View?) {
        mainHandler.removeCallbacks(cursorBlinkRunnable)
        isCursorBlinkOn = true
        if (cursorView != null) {
            activeCursorView = java.lang.ref.WeakReference(cursorView)
            cursorView.visibility = View.VISIBLE
        }
        mainHandler.postDelayed(cursorBlinkRunnable, 500L)
    }

    fun stopCursorBlink(cursorView: View? = null) {
        mainHandler.removeCallbacks(cursorBlinkRunnable)
        val cursor = cursorView ?: activeCursorView?.get()
        cursor?.visibility = View.GONE
        activeCursorView = null
    }

    fun setupHeader(
        view: View,
        onBack: () -> Unit,
        onClear: () -> Unit
    ) {
        val backBtn = view.findViewById<ImageButton?>(R.id.btn_emoji_search_back)
        backBtn?.setOnClickListener {
            applyTapFeedback(it)
            onBack()
        }

        val clearBtn = view.findViewById<Button?>(R.id.btn_emoji_search_clear)
        clearBtn?.setOnClickListener {
            applyTapFeedback(it)
            searchQuery.setLength(0)
            onClear()
        }
    }

    fun enter() {
        isSearchMode = true
        searchQuery.setLength(0)
    }

    fun exit() {
        stopCursorBlink()
        isSearchMode = false
        searchQuery.setLength(0)
    }

    fun syncHeaderUI(activeView: View?) {
        activeView ?: return
        val suggestionsStrip = activeView.findViewById<View?>(R.id.suggestions_strip)
        val searchHeader = activeView.findViewById<View?>(R.id.emoji_search_header)

        if (isSearchMode) {
            suggestionsStrip?.visibility = View.GONE
            searchHeader?.visibility = View.VISIBLE
        } else {
            searchHeader?.visibility = View.GONE
            suggestionsStrip?.visibility = View.VISIBLE
        }
    }

    fun updateUI(activeView: View?, isUrduLayout: Boolean, onEmojiSelected: (String) -> Unit) {
        activeView ?: return
        val queryText = activeView.findViewById<TextView?>(R.id.tv_emoji_search_query) ?: return
        val clearBtn = activeView.findViewById<Button?>(R.id.btn_emoji_search_clear)
        val container = activeView.findViewById<LinearLayout?>(R.id.emoji_search_results_container) ?: return
        val cursor = activeView.findViewById<View?>(R.id.emoji_search_cursor)

        startCursorBlink(cursor)

        val query = searchQuery.toString()
        if (query.isEmpty()) {
            queryText.text = ""
            queryText.hint = if (isUrduLayout) "ایموجی تلاش کریں" else "Search"
            clearBtn?.visibility = View.GONE
            populateResultsStrip(EmojiDictionary.popularEmojis, container, onEmojiSelected)
        } else {
            queryText.text = query
            clearBtn?.visibility = View.VISIBLE
            val matches = EmojiDictionary.search(query)
            populateResultsStrip(if (matches.isEmpty()) EmojiDictionary.popularEmojis else matches, container, onEmojiSelected)
        }
    }

    private fun populateResultsStrip(
        emojis: List<String>,
        container: LinearLayout,
        onEmojiSelected: (String) -> Unit
    ) {
        container.removeAllViews()
        val itemSize = dpToPx(38)
        val marginPx = dpToPx(2)

        for (emoji in emojis) {
            val tv = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(itemSize, itemSize).apply {
                    marginStart = marginPx
                    marginEnd = marginPx
                }
                gravity = Gravity.CENTER
                text = emoji
                textSize = 21f
                isClickable = true
                isFocusable = true
                val outValue = TypedValue()
                if (context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)) {
                    setBackgroundResource(outValue.resourceId)
                }
                setOnClickListener {
                    applyTapFeedback(this)
                    onEmojiSelected(emoji)
                }
            }
            container.addView(tv)
        }
    }

    fun handleKeyEvent(
        keyCode: Int,
        event: KeyEvent,
        onUpdateUI: () -> Unit,
        onExitToPicker: () -> Unit,
        onExitSearch: () -> Unit
    ): Boolean {
        if (!isSearchMode) return false

        when (keyCode) {
            KeyEvent.KEYCODE_DEL -> {
                if (searchQuery.isNotEmpty()) {
                    searchQuery.deleteCharAt(searchQuery.length - 1)
                    onUpdateUI()
                } else {
                    onExitToPicker()
                }
                return true
            }
            KeyEvent.KEYCODE_ESCAPE -> {
                onExitToPicker()
                return true
            }
            KeyEvent.KEYCODE_ENTER -> {
                onExitSearch()
                return true
            }
            KeyEvent.KEYCODE_SPACE -> {
                searchQuery.append(" ")
                onUpdateUI()
                return true
            }
            else -> {
                val unicodeChar = event.unicodeChar
                if (unicodeChar > 0 && !Character.isISOControl(unicodeChar)) {
                    searchQuery.append(unicodeChar.toChar().toString())
                    onUpdateUI()
                    return true
                }
            }
        }
        return false
    }

    fun handleKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DEL,
            KeyEvent.KEYCODE_ESCAPE,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_SPACE -> return true
            else -> {
                val unicodeChar = event.unicodeChar
                if (unicodeChar > 0 && !Character.isISOControl(unicodeChar)) {
                    return true
                }
            }
        }
        return false
    }

    fun handleBackspace(count: Int, onUpdateUI: () -> Unit, onExitToPicker: () -> Unit): Boolean {
        if (!isSearchMode) return false
        if (searchQuery.isNotEmpty()) {
            val toDelete = minOf(count, searchQuery.length)
            searchQuery.delete(searchQuery.length - toDelete, searchQuery.length)
            onUpdateUI()
        } else {
            onExitToPicker()
        }
        return true
    }
}

