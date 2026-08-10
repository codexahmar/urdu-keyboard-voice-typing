package com.codexahmar.urdu_keyboard.keyboard

/**
 * Maps Roman/English letters to Urdu script (phonetic mapping)
 * This is a highly optimized phonetic transliteration system for Urdu typing.
 */
class PhoneticMapper {

    // Basic phonetic mapping dictionary
    // Format: Roman -> Urdu
    private val phoneticMap = mapOf(
        // Common greetings and religious terms
        "salam" to "سلام",
        "salaam" to "سلام",
        "assalam" to "السلام",
        "assalamualaikum" to "السلام علیکم",
        "allah" to "اللہ",
        "khuda" to "خدا",
        "dua" to "دعا",
        "quran" to "قرآن",
        "masjid" to "مسجد",
        "namaz" to "نماز",
        "roza" to "روزہ",
        "ramzan" to "رمضان",
        "eid" to "عید",
        "muhammad" to "محمد",
        "ahmed" to "احمد",
        "ali" to "علی",
        "hussain" to "حسین",

        // Common pronouns & pronouns variations
        "main" to "میں",
        "me" to "میں",
        "mujhe" to "مجھے",
        "mujhh" to "مجھے",
        "mera" to "میرا",
        "meri" to "میری",
        "mere" to "میرے",
        "aap" to "آپ",
        "ap" to "آپ",
        "apka" to "آپ کا",
        "apki" to "آپ کی",
        "apke" to "آپ کے",
        "tum" to "تم",
        "tumhara" to "تمہارا",
        "tumhari" to "تمہاری",
        "tumhare" to "تمہارے",
        "hum" to "ہم",
        "hamara" to "ہمارا",
        "hamari" to "ہمارے",
        "hamare" to "ہمارے",
        "woh" to "وہ",
        "wo" to "وہ",
        "us" to "اس",
        "is" to "اس",
        "un" to "ان",
        "in" to "ان",
        "unka" to "ان کا",
        "inka" to "ان کا",

        // Question words
        "kya" to "کیا",
        "kia" to "کیا",
        "kahan" to "کہاں",
        "kaha" to "کہاں",
        "kaise" to "کیسے",
        "kese" to "کیسے",
        "kab" to "کب",
        "kyun" to "کیوں",
        "kyon" to "کیوں",
        "kaun" to "کون",
        "kon" to "کون",
        "kis" to "کس",
        "kidhar" to "کدھر",

        // Auxiliary verbs / particles
        "hai" to "ہے",
        "hain" to "ہیں",
        "tha" to "تھا",
        "thi" to "تھی",
        "the" to "تھے",
        "ho" to "ہو",
        "hoon" to "ہوں",
        "hu" to "ہوں",
        "ga" to "گا",
        "gi" to "گی",
        "ge" to "گے",
        "ka" to "کا",
        "ki" to "کی",
        "ke" to "کے",
        "ko" to "کو",
        "se" to "سے",
        "ne" to "نے",
        "pe" to "پے",
        "par" to "پر",
        "aur" to "اور",
        "or" to "اور",
        "ya" to "یا",
        "bhi" to "بھی",
        "toh" to "تو",
        "to" to "تو",
        "na" to "نا",
        "nahi" to "نہیں",
        "nahin" to "نہیں",
        "han" to "ہاں",
        "haan" to "ہاں",
        "ji" to "جی",

        // Adjectives and adverbs
        "acha" to "اچھا",
        "achha" to "اچھا",
        "theek" to "ٹھیک",
        "thik" to "ٹھیک",
        "sahi" to "صحیح",
        "galat" to "غلط",
        "bohot" to "بہت",
        "bohat" to "بہت",
        "bahut" to "بہت",
        "ziada" to "زیادہ",
        "zyada" to "زیادہ",
        "thoda" to "تھوڑا",
        "thora" to "تھوڑا",
        "kam" to "کم",
        "saath" to "ساتھ",
        "sat" to "ساتھ",

        // Nouns & Verbs
        "naam" to "نام",
        "nam" to "نام",
        "ahmar" to "احمر",
        "pakistan" to "پاکستان",
        "dost" to "دوست",
        "kitab" to "کتاب",
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
        "aaj" to "آج",
        "aj" to "آج",
        "kal" to "کل",
        "parso" to "پرسوں",
        "log" to "لوگ",
        "baat" to "بات",
        "bat" to "بات",
        "dil" to "دل",
        "pyar" to "پیار",
        "yaar" to "یار",
        "gari" to "گاڑی",
        "kaam" to "کام",
        "gaya" to "گیا",
        "gai" to "گئی",
        "gaye" to "گئے",
        "jana" to "جانا",
        "kar" to "کر",
        "karna" to "کرنا",
        "raha" to "رہا",
        "rahi" to "رہی",
        "rahe" to "رہے",

        // Numbers
        "ek" to "ایک",
        "do" to "دو",
        "teen" to "تین",
        "char" to "چار",
        "panch" to "پانچ",
        "che" to "چھ",
        "saat" to "سات",
        "aath" to "آٹھ",
        "nau" to "نو",
        "das" to "دس"
    )

    // Double-letter syllable mappings (parsed first for match precedence)
    private val doublePhoneticRules = mapOf(
        "aa" to "آ",
        "kh" to "خ",
        "gh" to "غ",
        "sh" to "ش",
        "ch" to "چ",
        "th" to "ت",
        "dh" to "د",
        "ph" to "پ",
        "bh" to "ب",
        "oo" to "و",
        "ee" to "ی"
    )

    // Single-letter syllable mappings
    private val singlePhoneticRules = mapOf(
        "a" to "ا",
        "b" to "ب",
        "p" to "پ",
        "t" to "ت",
        "j" to "ج",
        "h" to "ہ",
        "d" to "د",
        "r" to "ر",
        "z" to "ز",
        "s" to "س",
        "f" to "ف",
        "q" to "ق",
        "k" to "ک",
        "g" to "گ",
        "l" to "ل",
        "m" to "م",
        "n" to "ن",
        "w" to "و",
        "y" to "ی",
        "e" to "ے",
        "i" to "ی",
        "u" to "و",
        "o" to "و",
        "v" to "و",
        "x" to "کس",
        "c" to "ک"
    )

    /**
     * Get Urdu suggestions for a given Roman input
     * Returns list of possible Urdu words, prioritizing direct matches
     */
    fun getUrduSuggestions(input: String): List<String> {
        val lowerInput = input.lowercase().trim()
        if (lowerInput.isEmpty()) return emptyList()

        val suggestions = LinkedHashSet<String>()

        // 1. Exact match
        phoneticMap[lowerInput]?.let { suggestions.add(it) }

        // 2. Fallback phonetic spelling
        val fallback = getPhoneticFallback(lowerInput)
        if (fallback.isNotEmpty()) {
            suggestions.add(fallback)
        }

        // 3. Prefix matches from dictionary (auto-completions)
        phoneticMap.entries
            .filter { it.key.startsWith(lowerInput) && it.key != lowerInput }
            .take(5)
            .forEach { suggestions.add(it.value) }

        return suggestions.toList()
    }

    /**
     * Character-by-character transliteration fallback generator
     */
    fun getPhoneticFallback(input: String): String {
        val result = StringBuilder()
        var i = 0
        val lower = input.lowercase()
        while (i < lower.length) {
            // Check double character patterns first
            if (i + 1 < lower.length) {
                val doubleChar = lower.substring(i, i + 2)
                val mapped = doublePhoneticRules[doubleChar]
                if (mapped != null) {
                    result.append(mapped)
                    i += 2
                    continue
                }
            }
            // Fallback to single character
            val singleChar = lower[i].toString()
            val mapped = singlePhoneticRules[singleChar]
            if (mapped != null) {
                result.append(mapped)
            }
            i++
        }
        return result.toString()
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
}
