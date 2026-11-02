package com.codexahmar.urdu_keyboard.keyboard

import java.util.Locale

/**
 * Intelligent English Suggestion & Next-Word Prediction Engine.
 * Implements Google Gboard suggestion architecture:
 *  - High-frequency unigram candidate retrieval with pre-indexed prefix buckets.
 *  - Bigram contextual next-word transitions.
 *  - Adaptive on-device personal vocabulary boosting.
 */
class EnglishSuggestionEngine {

    private val prefixBuckets: Map<Char, List<LexiconWord>> by lazy {
        EnglishLexicon.LEXICON_ENTRIES.groupBy { it.word.lowercase(Locale.ROOT).firstOrNull() ?: ' ' }
    }

    fun getNextWordPredictions(
        previousWord: String,
        userFrequencies: Map<String, Int> = emptyMap(),
        maxSuggestions: Int = 3
    ): List<String> {
        val clean = previousWord.lowercase(Locale.ROOT).trim()
        if (clean.isEmpty()) return emptyList()

        val candidates = LinkedHashSet<String>(maxSuggestions)

        // 1. Contextual Bigram predictions
        val bigramTails = EnglishLexicon.BIGRAM_TRANSITIONS[clean]
        if (!bigramTails.isNullOrEmpty()) {
            val sortedTails = if (userFrequencies.isNotEmpty()) {
                bigramTails.sortedByDescending { userFrequencies[it.lowercase(Locale.ROOT)] ?: 0 }
            } else {
                bigramTails
            }
            for (tail in sortedTails) {
                if (candidates.size >= maxSuggestions) break
                candidates.add(tail)
            }
        }

        // 2. Fill with frequent user-typed words
        if (candidates.size < maxSuggestions && userFrequencies.isNotEmpty()) {
            for ((word, _) in userFrequencies) {
                if (candidates.size >= maxSuggestions) break
                val lower = word.lowercase(Locale.ROOT)
                val firstChar = lower.firstOrNull() ?: ' '
                if (firstChar in 'a'..'z' && lower != clean) {
                    candidates.add(lower)
                }
            }
        }

        // 3. Fallback common English staples
        if (candidates.size < maxSuggestions) {
            val defaults = listOf("the", "you", "to", "and", "is")
            for (def in defaults) {
                if (candidates.size >= maxSuggestions) break
                if (def != clean) {
                    candidates.add(def)
                }
            }
        }

        return candidates.toList()
    }

    fun getSuggestions(
        prefix: String,
        previousWord: String? = null,
        userFrequencies: Map<String, Int> = emptyMap(),
        maxSuggestions: Int = 3
    ): List<String> {
        val cleanPrefix = prefix.lowercase(Locale.ROOT).trim()
        if (cleanPrefix.isEmpty()) return emptyList()

        val firstChar = cleanPrefix[0]
        val bucket = prefixBuckets[firstChar] ?: emptyList()

        val bigramContextSet = if (!previousWord.isNullOrBlank()) {
            EnglishLexicon.BIGRAM_TRANSITIONS[previousWord.lowercase(Locale.ROOT).trim()]?.toSet() ?: emptySet()
        } else {
            emptySet()
        }

        val matches = bucket.filter { it.word.lowercase(Locale.ROOT).startsWith(cleanPrefix) }

        fun scoreWord(word: String, baseFreq: Int): Int {
            var score = baseFreq
            val userCount = userFrequencies[word.lowercase(Locale.ROOT)] ?: 0
            score += userCount * 30
            if (bigramContextSet.contains(word)) {
                score += 50
            }
            if (word.equals(cleanPrefix, ignoreCase = true)) {
                score += 35
            }
            return score
        }

        val candidateMap = mutableMapOf<String, Int>()
        for (item in matches) {
            candidateMap[item.word] = scoreWord(item.word, item.frequency)
        }

        if (userFrequencies.isNotEmpty()) {
            for ((uWord, _) in userFrequencies) {
                val lower = uWord.lowercase(Locale.ROOT)
                if (lower.startsWith(cleanPrefix) && !candidateMap.containsKey(lower)) {
                    val firstC = lower.firstOrNull() ?: ' '
                    if (firstC in 'a'..'z') {
                        candidateMap[lower] = scoreWord(lower, 40)
                    }
                }
            }
        }

        return candidateMap.entries
            .sortedByDescending { it.value }
            .take(maxSuggestions)
            .map { it.key }
    }
}
