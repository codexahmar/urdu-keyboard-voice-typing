package com.codexahmar.urdu_keyboard.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper class for managing SharedPreferences
 * Handles persistence of keyboard settings
 */
class PreferenceHelper(context: Context) {

    companion object {
        private const val PREF_NAME = "FlutterSharedPreferences"  // Match Flutter's SharedPreferences name
        private const val KEY_SELECTED_LANGUAGE = "flutter.selected_language"  // Match Flutter key format
        private const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val DEFAULT_LANGUAGE = "ur" // Default to Urdu
    }

    private val prefs: SharedPreferences = 
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * Get selected language code
     */
    fun getSelectedLanguage(): String {
        return prefs.getString(KEY_SELECTED_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    /**
     * Set selected language code
     */
    fun setSelectedLanguage(languageCode: String) {
        prefs.edit().putString(KEY_SELECTED_LANGUAGE, languageCode).apply()
    }

    /**
     * Check if haptic feedback is enabled
     */
    fun isHapticFeedbackEnabled(): Boolean {
        return prefs.getBoolean(KEY_HAPTIC_FEEDBACK, true)
    }

    /**
     * Set haptic feedback preference
     */
    fun setHapticFeedback(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC_FEEDBACK, enabled).apply()
    }

    /**
     * Check if key sound is enabled
     */
    fun isSoundEnabled(): Boolean {
        return prefs.getBoolean(KEY_SOUND_ENABLED, false)
    }

    /**
     * Set sound preference
     */
    fun setSound(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    /**
     * Clear all preferences
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Word frequency tracking — powers Gboard-style learned suggestions
    // ─────────────────────────────────────────────────────────────────────────

    private val FREQ_PREFIX = "wf_"

    /**
     * Get how many times the user has committed this exact word.
     */
    fun getWordFrequency(word: String): Int {
        return prefs.getInt(FREQ_PREFIX + word.lowercase(), 0)
    }

    /**
     * Increment the usage counter for [word] by 1.
     * Cap at 9999 to keep pref data sane.
     */
    fun incrementWordFrequency(word: String) {
        val key = FREQ_PREFIX + word.lowercase()
        val current = prefs.getInt(key, 0)
        prefs.edit().putInt(key, minOf(current + 1, 9999)).apply()
    }

    /**
     * Return the top-N words (Urdu or Roman) sorted by frequency desc.
     * Used to pre-populate suggestion strip at the start of a word.
     */
    fun getTopFrequentWords(limit: Int = 30): List<Pair<String, Int>> {
        return prefs.all
            .filterKeys { it.startsWith(FREQ_PREFIX) }
            .mapNotNull { (key, value) ->
                val word = key.removePrefix(FREQ_PREFIX)
                val count = value as? Int ?: 0
                if (count > 0) word to count else null
            }
            .sortedByDescending { it.second }
            .take(limit)
    }
}
