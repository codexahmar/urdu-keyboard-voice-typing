package com.codexahmar.urdu_keyboard.keyboard

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import com.codexahmar.urdu_keyboard.R
import com.codexahmar.urdu_keyboard.utils.PreferenceHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Manages the suggestion strip above the keyboard:
 *  - Asynchronously queries UrduSuggestionEngine, EnglishSuggestionEngine, and PhoneticMapper.
 *  - Blends English word completion with Roman-Urdu transliteration.
 *  - Computes contextual Next-Word predictions on space or word commit.
 *  - Updates candidate buttons with zero main-thread blocking.
 */
class KeyboardSuggestionManager(
    private val preferenceHelper: PreferenceHelper,
    private val mainHandler: Handler = Handler(Looper.getMainLooper()),
    private val touchHandler: KeyTouchHandler
) {
    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val urduSuggestionEngine = UrduSuggestionEngine()
    private val englishSuggestionEngine = EnglishSuggestionEngine()
    private val phoneticMapper = PhoneticMapper()

    private var suggestionLeftBtn: Button? = null
    private var suggestionCenterBtn: Button? = null
    private var suggestionRightBtn: Button? = null

    private var suggestionDebounceRunnable: Runnable? = null

    fun bindButtons(view: View, onSuggestionSelected: (String) -> Unit) {
        val left = view.findViewById<Button>(R.id.suggestion_left)
        val center = view.findViewById<Button>(R.id.suggestion_center)
        val right = view.findViewById<Button>(R.id.suggestion_right)

        suggestionLeftBtn = left
        suggestionCenterBtn = center
        suggestionRightBtn = right

        touchHandler.setFastTapListener(left) { onSuggestionSelected(left?.text?.toString() ?: "") }
        touchHandler.setFastTapListener(center) { onSuggestionSelected(center?.text?.toString() ?: "") }
        touchHandler.setFastTapListener(right) { onSuggestionSelected(right?.text?.toString() ?: "") }
    }

    fun updateActiveButtons(activeView: View) {
        suggestionLeftBtn = activeView.findViewById(R.id.suggestion_left)
        suggestionCenterBtn = activeView.findViewById(R.id.suggestion_center)
        suggestionRightBtn = activeView.findViewById(R.id.suggestion_right)
        clearSuggestionsUI()
    }

    fun cancelDebounce() {
        suggestionDebounceRunnable?.let(mainHandler::removeCallbacks)
        suggestionDebounceRunnable = null
    }

    fun onComposingEmpty(prevWord: String?) {
        cancelDebounce()
        if (!prevWord.isNullOrBlank()) {
            requestNextWordPredictions(prevWord, isUrdu = isUrduWord(prevWord))
        } else {
            clearSuggestionsUI()
        }
    }

    fun onComposingChanged(
        currentComposing: String,
        prevWord: String?,
        isUrduLayout: Boolean
    ) {
        cancelDebounce()
        if (currentComposing.isEmpty()) {
            onComposingEmpty(prevWord)
            return
        }

        suggestionDebounceRunnable = Runnable {
            val snapshot = currentComposing
            if (snapshot.isEmpty()) return@Runnable
            ioScope.launch {
                val suggestions = getSuggestionsForInput(snapshot, prevWord, isUrduLayout)
                withContext(Dispatchers.Main) {
                    displaySuggestions(suggestions, snapshot)
                }
            }
        }
        mainHandler.postDelayed(suggestionDebounceRunnable!!, KeyboardConstants.SUGGESTION_DEBOUNCE_MS)
    }

    fun requestNextWordPredictions(previousWord: String, isUrdu: Boolean) {
        val freqMap = preferenceHelper.getTopFrequentWords(50)
            .associate { (word, count) -> word to count }
        ioScope.launch {
            val predictions = if (isUrdu) {
                urduSuggestionEngine.getNextWordPredictions(previousWord, freqMap)
            } else {
                englishSuggestionEngine.getNextWordPredictions(previousWord, freqMap)
            }
            withContext(Dispatchers.Main) {
                displayNextWordSuggestions(predictions)
            }
        }
    }

    private fun getSuggestionsForInput(
        input: String,
        prevWord: String?,
        isUrduLayout: Boolean
    ): List<String> {
        val freqMap = preferenceHelper.getTopFrequentWords(50)
            .associate { (word, count) -> word to count }
        val isUrdu = isUrduWord(input) || isUrduLayout

        if (isUrdu) {
            return urduSuggestionEngine.getSuggestions(input, prevWord, freqMap)
        }

        // English layout: blend English dictionary auto-completions with Roman-Urdu transliterations
        val englishCandidates = englishSuggestionEngine.getSuggestions(input, prevWord, freqMap)
        val urduPhoneticCandidates = phoneticMapper.getUrduSuggestions(input, freqMap)

        val combined = LinkedHashSet<String>(4)
        if (urduPhoneticCandidates.isNotEmpty()) {
            combined.add(urduPhoneticCandidates[0])
        }
        for (eng in englishCandidates) {
            if (combined.size >= 3) break
            combined.add(eng)
        }
        for (ur in urduPhoneticCandidates.drop(1)) {
            if (combined.size >= 3) break
            combined.add(ur)
        }
        return combined.toList()
    }

    private fun isUrduWord(word: String): Boolean {
        val firstChar = word.trim().firstOrNull() ?: ' '
        return firstChar in '\u0600'..'\u06FF' ||
               firstChar in '\uFB50'..'\uFDFF' ||
               firstChar in '\uFE70'..'\uFEFF'
    }

    fun displayNextWordSuggestions(predictions: List<String>) {
        if (predictions.isEmpty()) {
            clearSuggestionsUI()
            return
        }
        val center = predictions.getOrNull(0) ?: ""
        val left = predictions.getOrNull(1) ?: ""
        val right = predictions.getOrNull(2) ?: ""

        if (suggestionCenterBtn?.text?.toString() != center) suggestionCenterBtn?.text = center
        if (suggestionLeftBtn?.text?.toString() != left) suggestionLeftBtn?.text = left
        if (suggestionRightBtn?.text?.toString() != right) suggestionRightBtn?.text = right
    }

    fun displaySuggestions(suggestions: List<String>, rawInput: String) {
        val center = if (suggestions.isNotEmpty()) suggestions[0] else rawInput
        val left = if (suggestions.isNotEmpty() && suggestions[0] != rawInput) rawInput else (suggestions.getOrNull(1) ?: "")
        val right = if (suggestions.size > 2) suggestions[2] else (if (suggestions.size > 1 && left != suggestions[1]) suggestions[1] else "")

        if (suggestionLeftBtn?.text?.toString() != left) suggestionLeftBtn?.text = left
        if (suggestionCenterBtn?.text?.toString() != center) suggestionCenterBtn?.text = center
        if (suggestionRightBtn?.text?.toString() != right) suggestionRightBtn?.text = right
    }

    fun clearSuggestionsUI() {
        if (!suggestionLeftBtn?.text.isNullOrEmpty()) suggestionLeftBtn?.text = ""
        if (!suggestionCenterBtn?.text.isNullOrEmpty()) suggestionCenterBtn?.text = ""
        if (!suggestionRightBtn?.text.isNullOrEmpty()) suggestionRightBtn?.text = ""
    }

    fun learnCommittedWord(word: String) {
        if (word.isBlank()) return
        ioScope.launch {
            preferenceHelper.incrementWordFrequency(word)
        }
    }

    fun cleanup() {
        cancelDebounce()
        ioScope.cancel()
        suggestionLeftBtn = null
        suggestionCenterBtn = null
        suggestionRightBtn = null
    }
}

