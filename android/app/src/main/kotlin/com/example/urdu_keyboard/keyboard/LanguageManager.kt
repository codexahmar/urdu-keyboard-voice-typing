package com.example.urdu_keyboard.keyboard

import android.content.Context
import com.example.urdu_keyboard.utils.PreferenceHelper

/**
 * Manages keyboard language switching and persistence
 */
class LanguageManager(
    private val context: Context,
    private val preferenceHelper: PreferenceHelper
) {
    private val supportedLanguages = listOf("en", "ur", "ar")
    private var currentLanguageIndex = 0

    init {
        // Load saved language
        val savedLanguage = preferenceHelper.getSelectedLanguage()
        currentLanguageIndex = supportedLanguages.indexOf(savedLanguage)
        if (currentLanguageIndex < 0) {
            currentLanguageIndex = 0 // Default to English
        }
    }

    /**
     * Get current language code
     */
    fun getCurrentLanguage(): String {
        return supportedLanguages[currentLanguageIndex]
    }

    /**
     * Switch to next language in the list
     */
    fun switchToNextLanguage() {
        currentLanguageIndex = (currentLanguageIndex + 1) % supportedLanguages.size
        val newLanguage = getCurrentLanguage()
        preferenceHelper.setSelectedLanguage(newLanguage)
    }

    /**
     * Set language directly
     */
    fun setLanguage(languageCode: String) {
        val index = supportedLanguages.indexOf(languageCode)
        if (index >= 0) {
            currentLanguageIndex = index
            preferenceHelper.setSelectedLanguage(languageCode)
        }
    }

    /**
     * Get language display name
     */
    fun getLanguageName(): String {
        return when(getCurrentLanguage()) {
            "ur" -> "Urdu"
            "ar" -> "Arabic"
            else -> "English"
        }
    }

    /**
     * Get all supported languages
     */
    fun getSupportedLanguages(): List<String> {
        return supportedLanguages
    }
}
