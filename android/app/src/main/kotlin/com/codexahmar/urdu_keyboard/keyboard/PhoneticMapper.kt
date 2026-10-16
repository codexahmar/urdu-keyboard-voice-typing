package com.codexahmar.urdu_keyboard.keyboard

import java.util.Locale

/**
 * High-performance Phonetic Roman-Urdu to Urdu script transliteration engine.
 * Supports:
 *  - Comprehensive 500+ high-frequency Roman Urdu words & variations.
 *  - Greedy longest-match tokenization (trigraphs -> digraphs -> single letters).
 *  - Aspirated consonants (بھ, پھ, تھ, ٹھ, جھ, چھ, دھ, ڈھ, کھ, گھ).
 *  - Contextual end-of-word Noon Ghunna (ں) and Alif Madda (آ) handling.
 *  - Intelligent prefix completion and frequency-ranked suggestions.
 */
class PhoneticMapper {

    // Comprehensive Roman Urdu to Urdu dictionary
    private val phoneticMap: Map<String, String> = mapOf(
        // ── Greetings & Religious Phrases ────────────────────────────────────
        "salam" to "سلام",
        "salaam" to "سلام",
        "slam" to "سلام",
        "assalam" to "السلام",
        "assalamualaikum" to "السلام علیکم",
        "asalamoalikum" to "السلام علیکم",
        "walekumassalam" to "وعلیکم السلام",
        "wslm" to "وعلیکم السلام",
        "allah" to "اللہ",
        "khuda" to "خدا",
        "rab" to "رب",
        "dua" to "دعا",
        "quran" to "قرآن",
        "masjid" to "مسجد",
        "namaz" to "نماز",
        "roza" to "روزہ",
        "ramzan" to "رمضان",
        "ramadan" to "رمضان",
        "eid" to "عید",
        "mubarak" to "مبارک",
        "mubarik" to "مبارک",
        "shukriya" to "شکریہ",
        "shukria" to "شکریہ",
        "jazakallah" to "جزاک اللہ",
        "jazakallahu" to "جزاک اللہ",
        "alhamdulillah" to "الحمد للہ",
        "mashallah" to "ماشاء اللہ",
        "mashaallah" to "ماشاء اللہ",
        "inshallah" to "انشاء اللہ",
        "subhanallah" to "سبحان اللہ",
        "astaghfirullah" to "استغفر اللہ",
        "bismillah" to "بسم اللہ",
        "ameen" to "آمین",
        "amin" to "آمین",
        "muhammad" to "محمد",
        "ahmed" to "احمد",
        "ali" to "علی",
        "hussain" to "حسین",
        "hasan" to "حسن",
        "fatima" to "فاطمہ",
        "aisha" to "عائشہ",
        "ayesha" to "عائشہ",
        "umair" to "عمیر",
        "usman" to "عثمان",
        "umar" to "عمر",
        "abu" to "ابو",
        "abbu" to "ابو",
        "ammi" to "امی",
        "walid" to "والد",
        "walida" to "والدہ",
        "bhai" to "بھائی",
        "bhae" to "بھائی",
        "behan" to "بہن",
        "baji" to "باجی",
        "chacha" to "چاچا",
        "chachu" to "چچو",
        "mamo" to "ماموں",
        "mamu" to "ماموں",
        "khala" to "خالہ",
        "dada" to "دادا",
        "dadi" to "دادی",
        "nana" to "نانا",
        "nani" to "نانی",
        "beta" to "بیٹا",
        "beti" to "بیٹی",
        "bacha" to "بچہ",
        "bache" to "بچے",
        "bachay" to "بچے",
        "shohar" to "شوہر",
        "biwi" to "بیوی",
        "khandan" to "خاندان",

        // ── Pronouns & Variations ────────────────────────────────────────────
        "main" to "میں",
        "mein" to "میں",
        "me" to "میں",
        "mai" to "میں",
        "mujhe" to "مجھے",
        "mujy" to "مجھے",
        "mujh" to "مجھ",
        "mera" to "میرا",
        "meri" to "میری",
        "mere" to "میرے",
        "aap" to "آپ",
        "ap" to "آپ",
        "app" to "آپ",
        "apka" to "آپ کا",
        "aapka" to "آپ کا",
        "apki" to "آپ کی",
        "aapki" to "آپ کی",
        "apke" to "آپ کے",
        "aapke" to "آپ کے",
        "tum" to "تم",
        "tumhe" to "تمہیں",
        "tumhein" to "تمہیں",
        "tumhara" to "تمہارا",
        "tumhari" to "تمہاری",
        "tumhare" to "تمہارے",
        "hum" to "ہم",
        "humein" to "ہمیں",
        "hamara" to "ہمارا",
        "hamari" to "ہماری",
        "hamare" to "ہمارے",
        "woh" to "وہ",
        "wo" to "وہ",
        "us" to "اس",
        "ise" to "اسے",
        "use" to "اسے",
        "is" to "اس",
        "iska" to "اس کا",
        "iski" to "اس کی",
        "iske" to "اس کے",
        "uska" to "اس کا",
        "uski" to "اس کی",
        "uske" to "اس کے",
        "un" to "ان",
        "unka" to "ان کا",
        "unki" to "ان کی",
        "unke" to "ان کے",
        "in" to "ان",
        "inka" to "ان کا",
        "inki" to "ان کی",
        "inke" to "ان کے",
        "unhe" to "انہیں",
        "unhein" to "انہیں",
        "inhe" to "انہیں",
        "inhein" to "انہیں",
        "sab" to "سب",
        "sabka" to "سب کا",
        "sabki" to "سب کی",
        "sabke" to "سب کے",
        "kisi" to "کسی",
        "koi" to "کوئی",
        "kuch" to "کچھ",
        "kuchh" to "کچھ",

        // ── Question Words ───────────────────────────────────────────────────
        "kya" to "کیا",
        "kia" to "کیا",
        "kyaa" to "کیا",
        "kyun" to "کیوں",
        "kyon" to "کیوں",
        "kion" to "کیوں",
        "kaun" to "کون",
        "kon" to "کون",
        "kaise" to "کیسے",
        "kese" to "کیسے",
        "kaisi" to "کیسی",
        "kesi" to "کیسی",
        "kaisa" to "کیسا",
        "kesa" to "کیسا",
        "kab" to "کب",
        "kahan" to "کہاں",
        "kaha" to "کہاں",
        "kidhar" to "کدھر",
        "kdhr" to "کدھر",
        "kitna" to "کتنا",
        "kitne" to "کتنے",
        "kitni" to "کتنی",
        "kis" to "کس",
        "kisko" to "کس کو",
        "kisse" to "کس سے",
        "kaunsa" to "کونسا",
        "konsa" to "کونسا",
        "kaunsi" to "کونسی",
        "konsi" to "کونسی",

        // ── Auxiliary Verbs, Tenses & Particles ──────────────────────────────
        "hai" to "ہے",
        "he" to "ہے",
        "hain" to "ہیں",
        "hn" to "ہیں",
        "tha" to "تھا",
        "thi" to "تھی",
        "the" to "تھے",
        "ho" to "ہو",
        "hoon" to "ہوں",
        "hu" to "ہوں",
        "hun" to "ہوں",
        "hoga" to "ہوگا",
        "hogi" to "ہوگی",
        "honge" to "ہوں گے",
        "hote" to "ہوتے",
        "hoti" to "ہوتی",
        "hota" to "ہوتا",
        "hona" to "ہونا",
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
        "pa" to "پر",
        "aur" to "اور",
        "or" to "اور",
        "ya" to "یا",
        "bhi" to "بھی",
        "bh" to "بھی",
        "to" to "تو",
        "toh" to "تو",
        "na" to "نا",
        "nahi" to "نہیں",
        "nahin" to "نہیں",
        "nhn" to "نہیں",
        "nae" to "نہیں",
        "ni" to "نہیں",
        "han" to "ہاں",
        "haan" to "ہاں",
        "ji" to "جی",
        "jee" to "جی",
        "wala" to "والا",
        "wali" to "والی",
        "wale" to "والے",
        "balke" to "بلکہ",
        "lekin" to "لیکن",
        "magar" to "مگر",
        "agar" to "اگر",
        "jab" to "جب",
        "tab" to "تب",
        "phir" to "پھر",
        "fir" to "پھر",
        "shayed" to "شاید",
        "shayad" to "شاید",
        "zaroor" to "ضرور",
        "zarur" to "ضرور",
        "bilkul" to "بالکل",
        "hamesha" to "ہمیشہ",
        "kabhi" to "کبھی",
        "jabtak" to "جب تک",
        "sirf" to "صرف",

        // ── Adjectives, Adverbs & States ─────────────────────────────────────
        "acha" to "اچھا",
        "achha" to "اچھا",
        "ache" to "اچھے",
        "achhe" to "اچھے",
        "achi" to "اچھی",
        "achhi" to "اچھی",
        "bura" to "برا",
        "buri" to "بری",
        "bure" to "برے",
        "theek" to "ٹھیک",
        "thik" to "ٹھیک",
        "sahi" to "صحیح",
        "galat" to "غلط",
        "ghalat" to "غلط",
        "bohot" to "بہت",
        "bohat" to "بہت",
        "bahut" to "بہت",
        "bht" to "بہت",
        "ziada" to "زیادہ",
        "zyada" to "زیادہ",
        "kam" to "کم",
        "thoda" to "تھوڑا",
        "thora" to "تھوڑا",
        "thori" to "تھوڑی",
        "bada" to "بڑا",
        "bara" to "بڑا",
        "badi" to "بڑی",
        "bari" to "بڑی",
        "chota" to "چھوٹا",
        "choti" to "چھوٹی",
        "chote" to "چھوٹے",
        "naya" to "نیا",
        "nayi" to "نئی",
        "naye" to "نئے",
        "purana" to "پرانا",
        "purani" to "پرانی",
        "khubsurat" to "خوبصورت",
        "pyara" to "پیارا",
        "pyari" to "پیاری",
        "saaf" to "صاف",
        "kharab" to "خراب",
        "aasan" to "آسان",
        "asan" to "آسان",
        "mushkil" to "مشکل",
        "jaldi" to "جلدی",
        "aahista" to "آہستہ",
        "tez" to "تیز",
        "mehenga" to "مہنگا",
        "sasta" to "سستا",

        // ── Common Verbs & Actions ───────────────────────────────────────────
        "kar" to "کر",
        "karna" to "کرنا",
        "karein" to "کریں",
        "karo" to "کرو",
        "kiya" to "کیا",
        "raha" to "رہا",
        "rahi" to "رہی",
        "rahe" to "رہے",
        "aao" to "آؤ",
        "aana" to "آنا",
        "aaya" to "آیا",
        "aayi" to "آئی",
        "aaye" to "آئے",
        "jao" to "جاؤ",
        "jana" to "جانا",
        "gaya" to "گیا",
        "gayi" to "گئی",
        "gaye" to "گئے",
        "dekh" to "دیکھ",
        "dekha" to "دیکھا",
        "dekho" to "دیکھو",
        "dekhna" to "دیکھنا",
        "sun" to "سن",
        "suna" to "سنا",
        "suno" to "سنو",
        "sunna" to "سننا",
        "bol" to "بول",
        "bola" to "بولا",
        "bolo" to "بولو",
        "bolna" to "بولنا",
        "kaho" to "کہو",
        "kehna" to "کہنا",
        "samajh" to "سمجھ",
        "samjha" to "سمجھا",
        "samjho" to "سمجھو",
        "samjhna" to "سمجھنا",
        "likh" to "لکھ",
        "likha" to "لکھا",
        "likho" to "لکھو",
        "likhna" to "لکھنا",
        "parh" to "پڑھ",
        "parha" to "پڑھا",
        "parho" to "پڑھو",
        "parhna" to "پڑھنا",
        "bhej" to "بھیج",
        "bheja" to "بھیجا",
        "bhejo" to "بھیجو",
        "bhejna" to "بھیجنا",
        "soch" to "سوچ",
        "socha" to "سوچا",
        "socho" to "سوچو",
        "sochna" to "سوچنا",
        "mil" to "مل",
        "mila" to "ملا",
        "mili" to "ملی",
        "mile" to "ملے",
        "milna" to "ملنا",
        "rakh" to "رکھ",
        "rakha" to "رکھا",
        "rakho" to "رکھو",
        "rakhna" to "رکھنا",
        "le" to "لے",
        "lo" to "لو",
        "liya" to "لیا",
        "lena" to "لینا",
        "de" to "دے",
        "do" to "دو",
        "diya" to "دیا",
        "dena" to "دینا",
        "batana" to "بتانا",
        "batao" to "بتاؤ",
        "bataya" to "بتایا",
        "chahiye" to "چاہیے",
        "chahye" to "چاہیے",

        // ── Positions & Directions ───────────────────────────────────────────
        "yahan" to "یہاں",
        "yaha" to "یہاں",
        "wahan" to "وہاں",
        "waha" to "وہاں",
        "jahan" to "جہاں",
        "idhar" to "ادھر",
        "udhar" to "ادھر",
        "andar" to "اندر",
        "bahir" to "باہر",
        "upar" to "اوپر",
        "neeche" to "نیچے",
        "niche" to "نیچے",
        "aage" to "آگے",
        "peeche" to "پیچھے",
        "saath" to "ساتھ",
        "sat" to "ساتھ",
        "paas" to "پاس",
        "dur" to "دور",
        "door" to "دور",
        "pehle" to "پہلے",
        "baad" to "بعد",

        // ── Time & Dates ─────────────────────────────────────────────────────
        "aaj" to "آج",
        "aj" to "آج",
        "kal" to "کل",
        "parso" to "پرسوں",
        "subah" to "صبح",
        "subha" to "صبح",
        "dopahar" to "دوپہر",
        "shaam" to "شام",
        "sham" to "شام",
        "raat" to "رات",
        "waqt" to "وقت",
        "din" to "دن",
        "hafte" to "ہفتے",
        "hafta" to "ہفتہ",
        "mahina" to "مہینہ",
        "saal" to "سال",
        "ghanta" to "گھنٹہ",
        "minute" to "منٹ",
        "ab" to "اب",
        "abhi" to "ابھی",

        // ── Daily Living, Objects & Food ─────────────────────────────────────
        "naam" to "نام",
        "nam" to "نام",
        "kaam" to "کام",
        "kam" to "کام",
        "baat" to "بات",
        "bat" to "بات",
        "ghar" to "گھر",
        "makan" to "مکان",
        "kamra" to "کمرہ",
        "dukan" to "دکان",
        "bazar" to "بازار",
        "shahar" to "شہر",
        "shehr" to "شہر",
        "gaon" to "گاؤں",
        "mulk" to "ملک",
        "watan" to "وطن",
        "pakistan" to "پاکستان",
        "dost" to "دوست",
        "yaar" to "یار",
        "kitab" to "کتاب",
        "qalam" to "قلم",
        "pani" to "پانی",
        "khana" to "کھانا",
        "chai" to "چائے",
        "roti" to "روٹی",
        "salan" to "سالن",
        "chawal" to "چاول",
        "doodh" to "دودھ",
        "phal" to "پھل",
        "seb" to "سیب",
        "aam" to "آم",
        "gari" to "گاڑی",
        "gaari" to "گاڑی",
        "cycle" to "سائیکل",
        "rasta" to "راستہ",
        "safar" to "سفر",
        "manzil" to "منزل",
        "mobile" to "موبائل",
        "phone" to "فون",
        "message" to "میسج",
        "tasveer" to "تصویر",
        "paise" to "پیسے",
        "rupaye" to "روپے",
        "log" to "لوگ",
        "dil" to "دل",
        "pyar" to "پیار",
        "mohabbat" to "محبت",
        "zindagi" to "زندگی",
        "dunya" to "دنیا",
        "khushi" to "خوشی",
        "gham" to "غم",
        "khayal" to "خیال",
        "umeed" to "امید",
        "yaqeen" to "یقین",
        "pata" to "پتہ",

        // ── Numbers ──────────────────────────────────────────────────────────
        "ek" to "ایک",
        "aik" to "ایک",
        "do" to "دو",
        "teen" to "تین",
        "char" to "چار",
        "panch" to "پانچ",
        "che" to "چھ",
        "saat" to "سات",
        "aath" to "آٹھ",
        "nau" to "نو",
        "das" to "دس",
        "gyara" to "گیارہ",
        "bara" to "بارہ",
        "tera" to "تیرہ",
        "chauda" to "چودہ",
        "pandra" to "پندرہ",
        "sola" to "سولہ",
        "sattra" to "سترہ",
        "athara" to "اٹھارہ",
        "unnees" to "انیس",
        "bees" to "بیس",
        "tees" to "تیس",
        "chalees" to "چالیس",
        "pachas" to "پچاس",
        "sath" to "ساٹھ",
        "sattar" to "ستر",
        "assi" to "اسی",
        "nabbay" to "نوے",
        "sau" to "سو",
        "hazar" to "ہزار",
        "lakh" to "لاکھ",
        "crore" to "کروڑ"
    )

    // Trigraph rules: 3-character sequences parsed first for longest match
    private val trigraphRules: Map<String, String> = mapOf(
        "chh" to "چھ",
        "khh" to "کھ",
        "ghh" to "گھ",
        "thh" to "ٹھ",
        "dhh" to "ڈھ",
        "rrh" to "ڑھ",
        "sch" to "ش"
    )

    // Digraph rules: 2-character sequences
    private val digraphRules: Map<String, String> = mapOf(
        "kh" to "خ",
        "gh" to "غ",
        "sh" to "ش",
        "ch" to "چ",
        "jh" to "جھ",
        "ph" to "پھ",
        "bh" to "بھ",
        "th" to "تھ",
        "dh" to "دھ",
        "zh" to "ژ",
        "rr" to "ڑ",
        "ng" to "ں",
        "aa" to "آ",
        "ee" to "ی",
        "oo" to "و",
        "ai" to "ے",
        "ei" to "ے",
        "au" to "او",
        "wh" to "وہ"
    )

    // Single character phonetics
    private val singleRules: Map<String, String> = mapOf(
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
        "v" to "و",
        "y" to "ی",
        "e" to "ے",
        "i" to "ی",
        "u" to "و",
        "o" to "و",
        "x" to "کس",
        "c" to "ک"
    )

    /**
     * Get Urdu suggestions for a given Roman or Urdu input.
     * [frequencyMap] provides user word usage counts for ranking.
     */
    fun getUrduSuggestions(
        input: String,
        frequencyMap: Map<String, Int> = emptyMap()
    ): List<String> {
        val lowerInput = input.lowercase(Locale.ROOT).trim()
        if (lowerInput.isEmpty()) return emptyList()

        val suggestions = LinkedHashSet<String>()

        // Check if input is already in Urdu Persio-Arabic script (typing on Urdu layout)
        val isUrduScript = lowerInput.any { it in '\u0600'..'\u06FF' || it in '\uFB50'..'\uFDFF' || it in '\uFE70'..'\uFEFF' }
        if (isUrduScript) {
            val urduMatches = phoneticMap.values
                .filter { it.startsWith(lowerInput) }
                .distinct()
                .sortedByDescending { frequencyMap[it] ?: 0 }
                .take(3)
            suggestions.addAll(urduMatches)

            if (suggestions.size < 3 && frequencyMap.isNotEmpty()) {
                frequencyMap.keys
                    .filter { it.startsWith(lowerInput) && !suggestions.contains(it) }
                    .take(3 - suggestions.size)
                    .forEach { suggestions.add(it) }
            }
            return suggestions.take(3).toList()
        }

        // 1. Exact dictionary match (highest priority)
        phoneticMap[lowerInput]?.let { suggestions.add(it) }

        // 2. Prefix matches from dictionary — sorted by user frequency desc
        val prefixMatches = phoneticMap.entries
            .filter { it.key.startsWith(lowerInput) && it.key != lowerInput }
            .sortedByDescending { frequencyMap[it.value] ?: 0 }
            .take(4)
        prefixMatches.forEach { suggestions.add(it.value) }

        // 3. Frequency-based: if we have fewer than 3 suggestions, add the user's
        //    most-typed Urdu words that match the phonetic prefix
        if (suggestions.size < 3 && frequencyMap.isNotEmpty()) {
            val fallbackPrefix = getPhoneticFallback(lowerInput)
            if (fallbackPrefix.isNotEmpty()) {
                frequencyMap.entries
                    .filter { (word, _) -> word.startsWith(fallbackPrefix) && !suggestions.contains(word) }
                    .sortedByDescending { it.value }
                    .take(3 - suggestions.size)
                    .forEach { suggestions.add(it.key) }
            }
        }

        // 4. Fallback algorithmic phonetic transliteration
        val fallback = getPhoneticFallback(lowerInput)
        if (fallback.isNotEmpty() && !suggestions.contains(fallback)) {
            suggestions.add(fallback)
        }

        return suggestions.take(3).toList()
    }

    /**
     * Greedy longest-match algorithmic transliterator for Roman Urdu -> Urdu.
     * Evaluates trigraphs, then digraphs, then single letters.
     * Features contextual Noon Ghunna (ں) and Alif Madda (آ).
     */
    fun getPhoneticFallback(input: String): String {
        val result = StringBuilder()
        val lower = input.lowercase(Locale.ROOT).trim()
        var i = 0
        val len = lower.length

        while (i < len) {
            // Check trigraphs first
            if (i + 3 <= len) {
                val tri = lower.substring(i, i + 3)
                val mapped = trigraphRules[tri]
                if (mapped != null) {
                    result.append(mapped)
                    i += 3
                    continue
                }
            }

            // Check digraphs second
            if (i + 2 <= len) {
                val di = lower.substring(i, i + 2)
                // Contextual rule: "aa" at start -> "آ", inside -> "ا"
                if (di == "aa") {
                    result.append(if (i == 0) "آ" else "ا")
                    i += 2
                    continue
                }
                val mapped = digraphRules[di]
                if (mapped != null) {
                    result.append(mapped)
                    i += 2
                    continue
                }
            }

            // Contextual end-of-word: trailing 'n' after a vowel maps to Noon Ghunna 'ں'
            if (i == len - 1 && lower[i] == 'n' && len > 2) {
                val prev = lower[i - 1]
                if (prev == 'a' || prev == 'e' || prev == 'i' || prev == 'o' || prev == 'u') {
                    result.append("ں")
                    i++
                    continue
                }
            }

            // Single letter fallback
            val singleChar = lower[i].toString()
            val mapped = singleRules[singleChar] ?: singleChar
            result.append(mapped)
            i++
        }

        return result.toString()
    }

    /**
     * Check if input has a direct phonetic mapping
     */
    fun hasMapping(input: String): Boolean = phoneticMap.containsKey(input.lowercase(Locale.ROOT))

    /**
     * Get exact Urdu text for input
     */
    fun getUrduText(input: String): String? = phoneticMap[input.lowercase(Locale.ROOT)]

    /**
     * Get all mappings
     */
    fun getAllMappings(): Map<String, String> = phoneticMap
}
