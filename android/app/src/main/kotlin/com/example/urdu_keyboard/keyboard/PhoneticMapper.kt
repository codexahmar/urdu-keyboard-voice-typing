package com.example.urdu_keyboard.keyboard

/**
 * Maps Roman/English letters to Urdu script (phonetic mapping)
 * This is a simplified phonetic system for Urdu typing
 */
class PhoneticMapper {

    // Basic phonetic mapping dictionary
    // Format: Roman -> Urdu
    private val phoneticMap = mapOf(
        // Common words
        "mera" to "میرا",
        "tera" to "تیرا",
        "tumhara" to "تمہارا",
        "hamara" to "ہمارا",
        "khuda" to "خدا",
        "pakistan" to "پاکستان",
        "salam" to "سلام",
        "allah" to "اللہ",
        "namaz" to "نماز",
        "kitab" to "کتاب",
        "dost" to "دوست",
        "mohabbat" to "محبت",
        "zindagi" to "زندگی",
        "dunya" to "دنیا",
        "ghar" to "گھر",
        "khana" to "کھانا",
        "pani" to "پانی",
        "waqt" to "وقت",
        "raat" to "رات",
        "din" to "دن",
        "subah" to "صبح",
        "shaam" to "شام",
        "aap" to "آپ",
        "main" to "میں",
        "tum" to "تم",
        "woh" to "وہ",
        "kya" to "کیا",
        "kahan" to "کہاں",
        "kaise" to "کیسے",
        "kab" to "کب",
        "kyun" to "کیوں",
        "kaun" to "کون",
        "is" to "اس",
        "us" to "اس",
        "yeh" to "یہ",
        
        // Basic letters and combinations
        "a" to "ا",
        "aa" to "آ",
        "b" to "ب",
        "p" to "پ",
        "t" to "ت",
        "th" to "ٹھ",
        "j" to "ج",
        "ch" to "چ",
        "h" to "ح",
        "kh" to "خ",
        "d" to "د",
        "dh" to "ڈھ",
        "z" to "ذ",
        "r" to "ر",
        "s" to "س",
        "sh" to "ش",
        "k" to "ک",
        "g" to "گ",
        "gh" to "غ",
        "f" to "ف",
        "q" to "ق",
        "l" to "ل",
        "m" to "م",
        "n" to "ن",
        "w" to "و",
        "y" to "ی",
        "e" to "ے",
        
        // Common combinations
        "hai" to "ہے",
        "hain" to "ہیں",
        "ka" to "کا",
        "ki" to "کی",
        "ke" to "کے",
        "ko" to "کو",
        "se" to "سے",
        "ne" to "نے",
        "me" to "میں",
        "pe" to "پے",
        "par" to "پر",
        "aur" to "اور",
        "ya" to "یا",
        "nahi" to "نہیں",
        "nahin" to "نہیں",
        "han" to "ہاں",
        "ji" to "جی",
        "na" to "نا",
        "tha" to "تھا",
        "thi" to "تھی",
        "the" to "تھے",
        "ho" to "ہو",
        "hu" to "ہوں",
        "ga" to "گا",
        "gi" to "گی",
        "ge" to "گے"
    )

    /**
     * Get Urdu suggestions for a given Roman input
     * Returns list of possible Urdu words
     */
    fun getUrduSuggestions(input: String): List<String> {
        val lowerInput = input.lowercase()
        val suggestions = mutableListOf<String>()

        // Exact match
        phoneticMap[lowerInput]?.let { suggestions.add(it) }

        // Partial matches (for auto-complete)
        if (suggestions.isEmpty()) {
            phoneticMap.entries
                .filter { it.key.startsWith(lowerInput) && it.key != lowerInput }
                .take(3)
                .forEach { suggestions.add(it.value) }
        }

        return suggestions
    }

    /**
     * Check if input has a phonetic mapping
     */
    fun hasMapping(input: String): Boolean {
        return phoneticMap.containsKey(input.lowercase())
    }

    /**
     * Get exact Urdu text for input
     */
    fun getUrduText(input: String): String? {
        return phoneticMap[input.lowercase()]
    }

    /**
     * Get all mappings (for testing/debugging)
     */
    fun getAllMappings(): Map<String, String> {
        return phoneticMap
    }

    /**
     * Add custom mapping (for future extensibility)
     */
    fun addMapping(roman: String, urdu: String) {
        // In production, this would update a persistent dictionary
        // For now, it's a placeholder
    }
}
