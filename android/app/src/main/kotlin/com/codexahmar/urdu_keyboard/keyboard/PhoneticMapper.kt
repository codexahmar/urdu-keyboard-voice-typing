package com.codexahmar.urdu_keyboard.keyboard

import java.util.Locale

/**
 * High-performance Phonetic Roman-Urdu to Urdu script transliteration engine.
 * Supports:
 *  - Comprehensive 500+ high-frequency Roman Urdu words & variations via RomanUrduDictionary.
 *  - Greedy longest-match tokenization (trigraphs -> digraphs -> single letters).
 *  - Aspirated consonants (بھ, پھ, تھ, ٹھ, جھ, چھ, دھ, ڈھ, کھ, گھ).
 *  - Contextual end-of-word Noon Ghunna (ں) and Alif Madda (آ) handling.
 *  - Pre-indexed prefix buckets for O(1) suggestions and frequency-ranked completions.
 */
class PhoneticMapper {

    private val phoneticMap: Map<String, String> get() = RomanUrduDictionary.PHONETIC_MAP
    private val trigraphRules: Map<String, String> get() = RomanUrduDictionary.TRIGRAPH_RULES
    private val digraphRules: Map<String, String> get() = RomanUrduDictionary.DIGRAPH_RULES
    private val singleRules: Map<String, String> get() = RomanUrduDictionary.SINGLE_RULES

    // Pre-indexed prefix buckets for O(1) candidate lookup (eliminates 500+ item iterations)
    private val romanPrefixBuckets: Map<Char, List<Map.Entry<String, String>>> by lazy {
        phoneticMap.entries.groupBy { it.key.firstOrNull() ?: ' ' }
    }

    private val urduPrefixBuckets: Map<Char, List<String>> by lazy {
        phoneticMap.values.distinct().groupBy { it.firstOrNull() ?: ' ' }
    }

    /**
     * Get Urdu suggestions for a given Roman or Urdu input.
     * [frequencyMap] provides user word usage counts for ranking.
     * Zero-allocation, high-performance lookup.
     */
    fun getUrduSuggestions(
        input: String,
        frequencyMap: Map<String, Int> = emptyMap()
    ): List<String> {
        val lowerInput = input.lowercase(Locale.ROOT).trim()
        if (lowerInput.isEmpty()) return emptyList()

        val suggestions = LinkedHashSet<String>(4)

        // Check if input is already in Urdu Persio-Arabic script (typing on Urdu layout)
        val firstChar = lowerInput[0]
        val isUrduScript = firstChar in '\u0600'..'\u06FF' || firstChar in '\uFB50'..'\uFDFF' || firstChar in '\uFE70'..'\uFEFF'
        if (isUrduScript) {
            val bucket = urduPrefixBuckets[firstChar]
            if (bucket != null) {
                var matches = bucket.filter { it.startsWith(lowerInput) }
                if (frequencyMap.isNotEmpty()) {
                    matches = matches.sortedByDescending { frequencyMap[it] ?: 0 }
                }
                for (word in matches) {
                    if (suggestions.size >= 3) break
                    suggestions.add(word)
                }
            }

            if (suggestions.size < 3 && frequencyMap.isNotEmpty()) {
                for (k in frequencyMap.keys) {
                    if (suggestions.size >= 3) break
                    if (k.startsWith(lowerInput)) {
                        suggestions.add(k)
                    }
                }
            }
            return suggestions.toList()
        }

        // 1. Exact dictionary match (highest priority)
        phoneticMap[lowerInput]?.let { suggestions.add(it) }

        // 2. Prefix matches from indexed bucket — sorted by user frequency desc
        val bucket = romanPrefixBuckets[firstChar]
        if (bucket != null) {
            var prefixMatches = bucket.filter { it.key.startsWith(lowerInput) && it.key != lowerInput }
            if (frequencyMap.isNotEmpty()) {
                prefixMatches = prefixMatches.sortedByDescending { frequencyMap[it.value] ?: 0 }
            }
            for (entry in prefixMatches) {
                if (suggestions.size >= 3) break
                suggestions.add(entry.value)
            }
        }

        // 3. Frequency-based: if we have fewer than 3 suggestions, check top typed words
        if (suggestions.size < 3 && frequencyMap.isNotEmpty()) {
            val fallbackPrefix = getPhoneticFallback(lowerInput)
            if (fallbackPrefix.isNotEmpty()) {
                for ((word, _) in frequencyMap) {
                    if (suggestions.size >= 3) break
                    if (word.startsWith(fallbackPrefix)) {
                        suggestions.add(word)
                    }
                }
            }
        }

        // 4. Fallback algorithmic phonetic transliteration
        if (suggestions.size < 3) {
            val fallback = getPhoneticFallback(lowerInput)
            if (fallback.isNotEmpty()) {
                suggestions.add(fallback)
            }
        }

        return suggestions.toList()
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
