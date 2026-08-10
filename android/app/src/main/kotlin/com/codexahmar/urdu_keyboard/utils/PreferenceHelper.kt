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

    /**
     * Get list of recently used emojis
     */
    fun getRecentEmojis(): List<String>? {
        val raw = prefs.getString("recent_emojis", null) ?: return null
        return raw.split(",")
    }

    /**
     * Save list of recently used emojis
     */
    fun saveRecentEmojis(emojis: List<String>) {
        prefs.edit().putString("recent_emojis", emojis.joinToString(",")).apply()
    }
}
