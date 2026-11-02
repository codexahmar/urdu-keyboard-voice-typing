package com.codexahmar.urdu_keyboard.keyboard

import com.codexahmar.urdu_keyboard.R

/**
 * Constants, character maps, and compile-time layout identifiers for Urdu & English keyboards.
 * Centralizing layout specifications eliminates magic numbers and provides clean zero-reflection lookups.
 */
object KeyboardConstants {

    const val EXTRA_BOTTOM_GAP_DP = 8
    const val SUGGESTION_DEBOUNCE_MS = 25L
    const val LONG_PRESS_TIMEOUT_MS = 280L

    // ── Urdu Character Sets — Exact Google Gboard Layout (39 unique keys) ──────
    val URDU_BASE_LETTERS = arrayOf(
        // Row 1 (10 keys)
        "ض", "ص", "غ", "ڑ", "ٹ", "ث", "ح", "ئ", "ظ", "ط",
        // Row 2 (10 keys)
        "ق", "و", "ع", "ر", "ت", "ے", "ء", "ی", "ہ", "پ",
        // Row 3 (10 keys)
        "ا", "س", "ڈ", "د", "ف", "گ", "ھ", "ج", "ک", "ل",
        // Row 4 (9 keys)
        "ذ", "ز", "ش", "خ", "چ", "ب", "ں", "ن", "م"
    )

    val URDU_LONG_PRESS_VARIANTS = mapOf(
        "ا" to arrayOf("آ", "أ"),
        "و" to arrayOf("ؤ"),
        "ہ" to arrayOf("ۂ", "ۃ"),
        "ے" to arrayOf("ۓ"),
        "ء" to arrayOf("ٔ"),
        "ز" to arrayOf("ژ"),
        "م" to arrayOf("؟"),
        // Row 1 Perso-Arabic digits on long-press (matching Gboard):
        "ض" to arrayOf("۱"), "ص" to arrayOf("۲"), "غ" to arrayOf("۳"),
        "ڑ" to arrayOf("۴"), "ٹ" to arrayOf("۵"), "ث" to arrayOf("۶"),
        "ح" to arrayOf("۷"), "ئ" to arrayOf("۸"), "ظ" to arrayOf("۹"),
        "ط" to arrayOf("۰")
    )

    val ENGLISH_LONG_PRESS_VARIANTS = mapOf(
        "q" to arrayOf("1"), "w" to arrayOf("2"), "e" to arrayOf("3"), "r" to arrayOf("4"),
        "t" to arrayOf("5"), "y" to arrayOf("6"), "u" to arrayOf("7"), "i" to arrayOf("8"),
        "o" to arrayOf("9"), "p" to arrayOf("0")
    )

    // ── Compile-Time IDs for Zero-Reflection Mapping ───────────────────────────
    val ENGLISH_KEY_ID_MAP = intArrayOf(
        R.id.key_q, R.id.key_w, R.id.key_e, R.id.key_r, R.id.key_t,
        R.id.key_y, R.id.key_u, R.id.key_i, R.id.key_o, R.id.key_p,
        R.id.key_a, R.id.key_s, R.id.key_d, R.id.key_f, R.id.key_g,
        R.id.key_h, R.id.key_j, R.id.key_k, R.id.key_l,
        R.id.key_z, R.id.key_x, R.id.key_c, R.id.key_v, R.id.key_b,
        R.id.key_n, R.id.key_m
    )

    val ENGLISH_KEY_CHAR_MAP = arrayOf(
        "q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
        "a", "s", "d", "f", "g", "h", "j", "k", "l",
        "z", "x", "c", "v", "b", "n", "m"
    )

    val URDU_KEY_IDS = intArrayOf(
        R.id.urdu_key_1, R.id.urdu_key_2, R.id.urdu_key_3, R.id.urdu_key_4, R.id.urdu_key_5,
        R.id.urdu_key_6, R.id.urdu_key_7, R.id.urdu_key_8, R.id.urdu_key_9, R.id.urdu_key_10,
        R.id.urdu_key_11, R.id.urdu_key_12, R.id.urdu_key_13, R.id.urdu_key_14, R.id.urdu_key_15,
        R.id.urdu_key_16, R.id.urdu_key_17, R.id.urdu_key_18, R.id.urdu_key_19, R.id.urdu_key_20,
        R.id.urdu_key_21, R.id.urdu_key_22, R.id.urdu_key_23, R.id.urdu_key_24, R.id.urdu_key_25,
        R.id.urdu_key_26, R.id.urdu_key_27, R.id.urdu_key_28, R.id.urdu_key_29, R.id.urdu_key_30,
        R.id.urdu_key_31, R.id.urdu_key_32, R.id.urdu_key_33, R.id.urdu_key_34, R.id.urdu_key_35,
        R.id.urdu_key_36, R.id.urdu_key_37, R.id.urdu_key_38, R.id.urdu_key_39
    )

    val SYM_DIGIT_IDS = intArrayOf(
        R.id.sym_key_1, R.id.sym_key_2, R.id.sym_key_3, R.id.sym_key_4, R.id.sym_key_5,
        R.id.sym_key_6, R.id.sym_key_7, R.id.sym_key_8, R.id.sym_key_9, R.id.sym_key_0
    )

    val URDU_DIGITS = listOf("۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹", "۰")
    val ENGLISH_DIGITS = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")

    val URDU_PUNCTUATION = arrayOf(
        Pair(R.id.sym_key_question, "؟"), Pair(R.id.sym_key_comma, "،"),
        Pair(R.id.sym_key_dot, "۔"), Pair(R.id.sym_key_semicolon, "؛"),
        Pair(R.id.sym_key_percent, "٪")
    )

    val ENGLISH_PUNCTUATION = arrayOf(
        Pair(R.id.sym_key_question, "?"), Pair(R.id.sym_key_comma, ","),
        Pair(R.id.sym_key_dot, "."), Pair(R.id.sym_key_semicolon, ";"),
        Pair(R.id.sym_key_percent, "%")
    )

    val COMMON_SYMBOLS = arrayOf(
        Pair(R.id.sym_key_at, "@"), Pair(R.id.sym_key_hash, "#"), Pair(R.id.sym_key_dollar, "$"),
        Pair(R.id.sym_key_amp, "&"), Pair(R.id.sym_key_star, "*"), Pair(R.id.sym_key_minus, "-"),
        Pair(R.id.sym_key_plus, "+"), Pair(R.id.sym_key_lparen, "("), Pair(R.id.sym_key_rparen, ")"),
        Pair(R.id.sym_key_slash, "/"), Pair(R.id.sym_key_quote, "\""), Pair(R.id.sym_key_apostrophe, "'"),
        Pair(R.id.sym_key_colon, ":"), Pair(R.id.sym_key_exclamation, "!")
    )
}

