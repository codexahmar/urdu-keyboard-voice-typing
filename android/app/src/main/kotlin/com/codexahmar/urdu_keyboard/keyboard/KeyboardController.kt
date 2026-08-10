package com.codexahmar.urdu_keyboard.keyboard

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.codexahmar.urdu_keyboard.R
import com.codexahmar.urdu_keyboard.utils.PreferenceHelper
import kotlin.math.max
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * KeyboardController — manages keyboard view creation, input logic, and UI state.
 *
 * Architectural notes (v2):
 *  - urduLetters array corrected: 38 unique characters, zero duplicates.
 *  - Key-press animation added (subtle 0.87 → 1.0 scale, 60 ms) for tactile feel.
 *  - Shift key now swaps vector drawables (ic_shift / ic_shift_active) instead of
 *    mutating button text, keeping icon-only buttons consistent.
 *  - applyBottomInsetPadding() consolidated to a single reliable
 *    ViewCompat.setOnApplyWindowInsetsListener flow with a manual trigger post-layout.
 *  - All functionality (text commit, language switch, voice, symbols) is unchanged.
 */
class KeyboardController(
    private val context: Context,
    private val preferenceHelper: PreferenceHelper
) {
    private lateinit var keyboardView: View
    private val languageManager = LanguageManager(context, preferenceHelper)
    private lateinit var voiceInputManager: VoiceInputManager
    private lateinit var backspaceHandler: BackspaceHandler
    private val mainHandler = Handler(Looper.getMainLooper())
    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var englishView: View? = null
    private var urduView: View? = null
    private var symbolsView: View? = null

    private val currentInputConnection: InputConnection?
        get() = (context as? UrduKeyboardService)?.currentInputConnection

    private val englishButtonsMap = mutableMapOf<String, Button>()
    private val urduButtonsMap = mutableMapOf<String, Button>()

    private var isUrduKeyboardLayout = false
    private var shiftState = ShiftState.OFF

    private val composingText = StringBuilder()
    private val phoneticMapper = PhoneticMapper()
    private var suggestionLeftBtn: Button? = null
    private var suggestionCenterBtn: Button? = null
    private var suggestionRightBtn: Button? = null
    private var lastShiftTapUptimeMs = 0L
    private var isAutoCapNext = true
    private var isSymbolMode = false
    private var lastKnownBottomInsetPx = 0

    private var isEmojiMode = false
    private var emojiView: View? = null
    private var currentEmojiCategory = EmojiCategory.SMILEYS
    private val recentEmojis = mutableListOf<String>()

    private enum class EmojiCategory {
        RECENTS, SMILEYS, PEOPLE, ANIMALS, FOOD, TRAVEL, ACTIVITIES, OBJECTS, SYMBOLS, FLAGS
    }

    private val smileysEmojis = listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰",
        "😘", "😗", "😙", "😚", "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🥸", "🥳", "😏",
        "😒", "😞", "😔", "😟", "😕", "🙁", "☹️", "😣", "😖", "😫", "😩", "🥺", "😢", "😭", "😤", "😠",
        "😡", "🤬", "🤯", "😳", "🥵", "🥶", "😱", "😨", "😰", "😥", "😓", "🤗", "🤔", "🫣", "🤭", "🫢",
        "🫡", "🤫", "🫠", "🤥", "😶", "😐", "😑", "😬", "🫨", "😯", "😦", "😧", "😮", "😲", "🥱", "😴",
        "🤤", "😪", "😵", "😵‍💫", "🤐", "🥴", "🤢", "🤮", "🤧", "😷", "🤒", "🤕", "🤑", "🤠", "😈", "👿"
    )

    private val peopleEmojis = listOf(
        "👋", "🤚", "🖐️", "✋", "🖖", "👌", "🤌", "🤏", "✌️", "🤞", "🫰", "🤟", "🤘", "🤙", "👈", "👉",
        "👆", "🖕", "👇", "☝️", "👍", "👎", "✊", "👊", "🤛", "🤜", "👏", "🙌", "👐", "🤲", "🤝", "🙏",
        "✍️", "💅", "🤳", "💪", "🦾", "🦿", "🦵", "🦶", "👂", "🦻", "👃", "🧠", "🫀", "🫁", "🦷", "🦴",
        "👀", "👁️", "👅", "👄", "💋", "🩸", "👶", "👧", "🧒", "👦", "👩", "🧑", "👨"
    )

    private val animalsEmojis = listOf(
        "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯", "🦁", "🐮", "🐷", "🐽", "🐸", "🐵",
        "🙈", "🙉", "🙊", "🐒", "🐔", "🐧", "🐦", "🐤", "🐣", "🦆", "🦅", "🦉", "🦇", "🐺", "🐗", "🐴",
        "🦄", "🐝", "🪱", "🐛", "🦋", "🐌", "🐞", "🐜", "🪰", "🪲", "🪳", "🦟", "🦗", "🕷️", "🕸️", "🦂",
        "🐢", "🐍", "🦎", "🦖", "🦕", "🐙", "🦑", "🦞", "🦀", "🐡", "🐠", "🐟", "🐬", "🐳", "🐋", "🦈"
    )

    private val foodEmojis = listOf(
        "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐", "🍒", "🍑", "🥭", "🍍", "🥥", "🥝",
        "🍅", "🍆", "🌶️", "🫑", "🌽", "🥕", "🍠", "🥐", "🍞", "🥖", "🥨", "🥯", "🥞", "🧇", "🧀", "🍖",
        "🍗", "🥩", "🥓", "🍔", "🍟", "🍕", "🌭", "🥪", "🌮", "burrito", "🍳", "🍲", "popcorn", "🍚", "🍛", "🍣",
        "🍪", "🎂", "🍰", "🧁", "🍫", "🍬", "🍭", "🍩", "🍧", "🍨", "🍦", "🧉", "☕", "🍵", "🥤", "🧃"
    )

    private val travelEmojis = listOf(
        "🚗", "🚕", "🚙", "🚌", "🚎", "🏎️", "🚓", "🚑", "🚒", "🚐", "🛻", "🚚", "🚛", "🚜", "🛵", "🚲",
        "🛴", "🚏", "🛣️", "🛤️", "🚨", "✈️", "🛫", "🛬", "🛸", "🚁", "🚀", "🛰️", "⛵", "🛥️", "🛳️", "⛴️",
        "🚢", "⚓", "🛟", "🌋", "🗻", "🏔️", "🏕️", "🏖️", "🏜️", "🏝️", "🏞️", "🏟️", "🏛️", "🏗️", "🧱", "🏠"
    )

    private val activitiesEmojis = listOf(
        "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🥏", "🎱", "🪀", "🏓", "🏸", "🏒", "🏑", "🥍",
        "🏏", "🪃", "🥅", "⛳", "🪁", "🏹", "🎣", "🤿", "🥊", "🥋", "🎽", "🛹", "🛼", "🛷", "⛸️", "🥌",
        "🎿", "⛷️", "🏂", "🏋️", "🤼", "🤸", "⛹️", "🤺", "🤾", "🏌️", "🏇", "🧘", "🏄", "🏊", "🤽", "🚣"
    )

    private val objectsEmojis = listOf(
        "⌚", "📱", "📲", "💻", "⌨️", "🖱️", "🖲️", "🖥️", "🖨️", "📷", "📸", "📹", "🎥", "📽️", "🎞️", "📞",
        "☎️", "📟", "📠", "📺", "📻", "🎙️", "🎚️", "🎛️", "🧭", "⏱️", "⏲️", "⏰", "⏳", "💡", "🔦", "🕯️",
        "🏮", "🪔", "📔", "📕", "📖", "📗", "📘", "📙", "📚", "📓", "📒", "📝", "✉️", "📧", "📨", "📩"
    )

    private val symbolsEmojis = listOf(
        "💘", "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔", "❤️‍🔥", "❤️‍🩹", "❣️", "💕", "💞",
        "💓", "💗", "💖", "🪞", "📿", "🧿", "💈", "🛎️", "🚪", "🛏️", "🛋️", "🛕", "🕌", "⛪", "🕍", "🕋",
        "⚠️", "🚫", "🔞", "☢️", "☣️", "⬆️", "↗️", "➡️", "↘️", "⬇️", "↙️", "⬅️", "↖️", "↕️", "↔️", "↩️"
    )

    private val flagsEmojis = listOf(
        "🏁", "🚩", "🎌", "🏴", "🏳️", "🏳️‍🌈", "🏳️‍⚧️", "🏴‍☠️", "🇵🇰", "🇦🇫", "🇸🇦", "🇦🇪", "🇹🇷", "🇮🇳", "🇺🇸", "🇬🇧",
        "🇨🇦", "🇦🇺", "🇩🇪", "🇫🇷", "🇯🇵", "🇨🇳", "🇧🇷", "🇷🇺", "🇮🇹", "🇪🇸", "🇿🇦", "🇰🇷", "🇮🇩", "🇲🇾", "🇧🇩", "🇮🇷"
    )
    private var spaceLongPressRunnable: Runnable? = null
    private var lastFeedbackUptimeMs = 0L
    // Debounce runnable for suggestion updates — prevents flooding coroutines on fast typing
    private var suggestionDebounceRunnable: Runnable? = null

    private enum class ShiftState {
        OFF,
        SHIFT_ONCE,
        CAPS_LOCK
    }

    companion object {
        private const val TAG = "KeyboardController"
        // Extra bottom clearance so content clears the nav bar shadow.
        private const val EXTRA_BOTTOM_GAP_DP = 8
        // Press-animation scale factor — only scale down, no scale-back chain for speed
        private const val PRESS_SCALE = 0.90f
        private const val ANIM_DURATION_MS = 20L   // 20 ms single-phase flash
        private const val SPACE_SWIPE_SWITCH_THRESHOLD_DP = 24
        private const val FEEDBACK_THROTTLE_MS = 20L  // tighter throttle for fast typists
        // How long to wait after the last keypress before querying suggestions
        private const val SUGGESTION_DEBOUNCE_MS = 120L
    }

    /**
     * Urdu letters — 38 unique characters mapped to urdu_key_1 … urdu_key_38.
     *
     * Row 1  (keys 1–10):  ض ص ث ق ف غ ع ہ خ ح
     * Row 2  (keys 11–20): ج چ ظ ط ز ر ذ د ڈ ب
     * Row 3  (keys 21–30): پ ت ٹ ش س ک گ ل م ن
     * Row 4  (keys 31–38): و ے ی ئ ؤ ا آ ژ
     */
    private val urduLetters = arrayOf(
        // Row 1
        "ض", "ص", "ث", "ق", "ف", "غ", "ع", "ہ", "خ", "ح",
        // Row 2
        "ج", "چ", "ظ", "ط", "ز", "ر", "ذ", "د", "ڈ", "ب",
        // Row 3
        "پ", "ت", "ٹ", "ش", "س", "ک", "گ", "ل", "م", "ن",
        // Row 4
        "و", "ے", "ی", "ئ", "ؤ", "ا", "آ", "ژ"
    )

    init {
        val currentLang = languageManager.getCurrentLanguage()
        isUrduKeyboardLayout = (currentLang == "ur")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    fun createKeyboardView(inflater: LayoutInflater): View {
        val cachedView = when {
            isEmojiMode -> emojiView
            isSymbolMode -> symbolsView
            isUrduKeyboardLayout -> urduView
            else -> englishView
        }

        if (cachedView != null) {
            keyboardView = cachedView
            if (isEmojiMode) {
                setupEmojiKeyboardButtons()
            } else {
                setupSuggestionButtons()
            }
            updateKeyboardLayout()
            refreshEnglishKeyLabels()
            ViewCompat.requestApplyInsets(keyboardView)
            return keyboardView
        }

        val layoutId = when {
            isEmojiMode -> R.layout.keyboard_emoji
            isSymbolMode -> R.layout.keyboard_symbols
            isUrduKeyboardLayout -> R.layout.keyboard_urdu
            else -> R.layout.keyboard_view
        }

        val newView = inflater.inflate(layoutId, null)
        keyboardView = newView
        if (isEmojiMode) {
            setupEmojiKeyboardButtons()
        } else {
            setupSuggestionButtons()
        }

        when {
            isEmojiMode -> emojiView = newView
            isSymbolMode -> symbolsView = newView
            isUrduKeyboardLayout -> urduView = newView
            else -> englishView = newView
        }

        if (!::backspaceHandler.isInitialized) {
            backspaceHandler = BackspaceHandler(
                context = context,
                getInputConnection = { currentInputConnection },
                onDelete = { handleBackspaceAction() }
            )
        }

        if (context is UrduKeyboardService && !::voiceInputManager.isInitialized) {
            voiceInputManager = VoiceInputManager(context, context)
        }

        if (isEmojiMode) {
            setupEmojiKeyboardButtons()
        } else {
            setupKeyboardButtons()
        }
        updateKeyboardLayout()
        refreshEnglishKeyLabels()
        applyBottomInsetPadding()

        return keyboardView
    }

    fun onStartInput(ic: InputConnection?, info: EditorInfo?) {
        isAutoCapNext = true
        shiftState = ShiftState.OFF
        Log.d(TAG, "onStartInput: IC=${if (ic != null) "valid" else "null"}")
        clearComposingState()
        refreshEnglishKeyLabels()
    }

    fun onFinishInput() {
        isAutoCapNext = true
        shiftState = ShiftState.OFF
        clearComposingState()
        if (::voiceInputManager.isInitialized) {
            voiceInputManager.stopListening()
        }
    }

    fun handleKeyPress(key: String, ic: InputConnection) {
        when {
            isUrduKeyboardLayout -> onUrduKeyPressed(key)
            else -> onEnglishKeyPressed(key)
        }
    }

    fun switchLanguage() = onLanguageSwitch()
    fun startVoiceInput() = onVoiceInput()

    fun cleanup() {
        spaceLongPressRunnable?.let(mainHandler::removeCallbacks)
        spaceLongPressRunnable = null
        ioScope.cancel()
        if (::backspaceHandler.isInitialized) {
            backspaceHandler.cleanup()
        }
        if (::voiceInputManager.isInitialized) {
            voiceInputManager.cleanup()
        }
        englishView = null
        urduView = null
        symbolsView = null
        englishButtonsMap.clear()
        urduButtonsMap.clear()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inset handling — single consolidated path, no redundant triple-calls
    // ─────────────────────────────────────────────────────────────────────────

    private fun applyBottomInsetPadding() {
        val extraGap = dpToPx(EXTRA_BOTTOM_GAP_DP)
        val minBottom = dpToPx(2)
        val baseLeft = keyboardView.paddingLeft
        val baseTop = keyboardView.paddingTop
        val baseRight = keyboardView.paddingRight

        fun applyInset(bottomInset: Int) {
            lastKnownBottomInsetPx = bottomInset
            keyboardView.setPadding(
                baseLeft, baseTop, baseRight,
                max(minBottom, bottomInset) + extraGap
            )
        }

        // Apply a best-effort initial padding so there's no first-frame jump.
        applyInset(lastKnownBottomInsetPx)

        // Register the reliable callback — fires whenever insets change.
        ViewCompat.setOnApplyWindowInsetsListener(keyboardView) { _, insets ->
            val navBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val systemBarsBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            applyInset(max(navBottom, systemBarsBottom))
            insets
        }

        // Trigger manually after the view is laid out (handles cases where the
        // callback fires before the view is attached).
        keyboardView.post { ViewCompat.requestApplyInsets(keyboardView) }
    }

    private fun dpToPx(dp: Int): Int =
        (dp * context.resources.displayMetrics.density).toInt()

    // ─────────────────────────────────────────────────────────────────────────
    // Button wiring
    // ─────────────────────────────────────────────────────────────────────────

    private fun setupKeyboardButtons() {
        when {
            isSymbolMode -> setupSymbolButtons()
            isUrduKeyboardLayout -> setupUrduKeyboardButtons()
            else -> {
                setupEnglishKeyboardButtons()
                setupPunctuationButtons()
            }
        }
        setupCommonButtons()
    }

    private fun setupEnglishKeyboardButtons() {
        val letterKeys = arrayOf(
            "q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
            "a", "s", "d", "f", "g", "h", "j", "k", "l",
            "z", "x", "c", "v", "b", "n", "m"
        )
        englishButtonsMap.clear()
        letterKeys.forEach { key ->
            val id = context.resources.getIdentifier("key_$key", "id", context.packageName)
            val btn = keyboardView.findViewById<Button>(id)
            if (btn != null) {
                englishButtonsMap[key] = btn
                setFastTapListener(btn) { onEnglishKeyPressed(key) }
            }
        }
    }

    private fun setupUrduKeyboardButtons() {
        urduButtonsMap.clear()
        urduLetters.forEachIndexed { index, letter ->
            val id = context.resources.getIdentifier(
                "urdu_key_${index + 1}", "id", context.packageName
            )
            val btn = keyboardView.findViewById<Button>(id)
            if (btn != null) {
                urduButtonsMap[letter] = btn
                setFastTapListener(btn) { onUrduKeyPressed(letter) }
            }
        }
        // Wire ،  and  ۔ and any other punctuation present in the Urdu layout
        setupPunctuationButtons()
    }

    private fun setupPunctuationButtons() {
        val prefix = when {
            isUrduKeyboardLayout -> "urdu_key_"
            else -> "key_"
        }
        val comma = if (isUrduKeyboardLayout) "،" else ","
        val dot = if (isUrduKeyboardLayout) "۔" else "."
        val question = if (isUrduKeyboardLayout) "؟" else "?"

        listOf(
            Triple("${prefix}comma", comma, false),
            Triple("${prefix}dot", dot, true),
            Triple("${prefix}question", question, true)
        ).forEach { (idName, symbol, isEndSentence) ->
            val id = context.resources.getIdentifier(idName, "id", context.packageName)
            setFastTapListener(keyboardView.findViewById(id)) {
                onPunctuationPressed(symbol)
                if (isEndSentence) isAutoCapNext = true
            }
        }

        // Extra symbols — silently skip if the key doesn't exist in this layout.
        mapOf(
            "${prefix}apostrophe" to "'",
            "${prefix}slash" to "/",
            "${prefix}at" to "@",
            "${prefix}minus" to "-",
            "${prefix}plus" to "+"
        ).forEach { (idName, symbol) ->
            val id = context.resources.getIdentifier(idName, "id", context.packageName)
            val btn = keyboardView.findViewById<Button?>(id) ?: return@forEach
            setFastTapListener(btn) { onPunctuationPressed(symbol) }
        }
    }

    private fun setupSymbolButtons() {
        // Number row: controller iterates sym_key_0 .. sym_key_9
        for (i in 0..9) {
            val id = context.resources.getIdentifier("sym_key_$i", "id", context.packageName)
            val symbol = i.toString()
            setFastTapListener(keyboardView.findViewById(id)) { commitText(symbol) }
        }

        mapOf(
            "sym_key_at" to "@", "sym_key_hash" to "#", "sym_key_dollar" to "$",
            "sym_key_percent" to "%", "sym_key_amp" to "&", "sym_key_star" to "*",
            "sym_key_minus" to "-", "sym_key_plus" to "+",
            "sym_key_lparen" to "(", "sym_key_rparen" to ")",
            "sym_key_slash" to "/", "sym_key_quote" to "\"",
            "sym_key_apostrophe" to "'", "sym_key_colon" to ":",
            "sym_key_semicolon" to ";", "sym_key_exclamation" to "!",
            "sym_key_question" to "?", "sym_key_comma" to ",",
            "sym_key_dot" to ".", "sym_key_equal" to "=",
            "sym_key_lbracket" to "[", "sym_key_rbracket" to "]",
            "sym_key_lbrace" to "{", "sym_key_rbrace" to "}",
            "sym_key_backslash" to "\\", "sym_key_less" to "<",
            "sym_key_greater" to ">", "sym_key_caret" to "^",
            "sym_key_tilde" to "~"
        ).forEach { (idName, symbol) ->
            val id = context.resources.getIdentifier(idName, "id", context.packageName)
            val btn = keyboardView.findViewById<Button?>(id) ?: return@forEach
            setFastTapListener(btn) { commitText(symbol) }
        }
    }

    private fun setupCommonButtons() {
        val prefix = when {
            isSymbolMode -> "sym_key_"
            isUrduKeyboardLayout -> "urdu_key_"
            else -> "key_"
        }
        val backPrefix = when {
            isSymbolMode -> "sym_"
            isUrduKeyboardLayout -> "urdu_"
            else -> "key_"
        }

        // Space
        val spaceId = context.resources.getIdentifier("${prefix}space", "id", context.packageName)
        setupSpaceButton(keyboardView.findViewById(spaceId))

        // Backspace — long-press handled by BackspaceHandler
        val bsId = context.resources.getIdentifier("${backPrefix}backspace", "id", context.packageName)
        keyboardView.findViewById<Button>(bsId)?.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    applyTapFeedback(view)
                    backspaceHandler.onBackspaceDown()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    backspaceHandler.onBackspaceUp()
                    view.performClick()
                    true
                }
                else -> false
            }
        }

        // Enter
        val enterId = context.resources.getIdentifier("${prefix}enter", "id", context.packageName)
        setFastTapListener(keyboardView.findViewById(enterId)) { onEnterPressed() }

        // Voice / Mic
        val voiceId = context.resources.getIdentifier("${prefix}voice", "id", context.packageName)
        keyboardView.findViewById<Button>(voiceId)?.setOnClickListener { onVoiceInput() }

        // Language switch
        val langId = context.resources.getIdentifier("${prefix}language", "id", context.packageName)
        keyboardView.findViewById<Button>(langId)?.setOnClickListener { onLanguageSwitch() }

        // Emoji toggle
        val emojiId = context.resources.getIdentifier("${prefix}emoji", "id", context.packageName)
        keyboardView.findViewById<Button>(emojiId)?.setOnClickListener { onEmojiModeToggle() }

        // 123 ↔ ABC toggle
        val toggleIdName = if (isSymbolMode) "sym_key_abc" else "${prefix}symbols"
        val toggleId = context.resources.getIdentifier(toggleIdName, "id", context.packageName)
        keyboardView.findViewById<Button>(toggleId)?.setOnClickListener { onSymbolsModeToggle() }

        // Shift (English only)
        if (!isSymbolMode && !isUrduKeyboardLayout) {
            val shiftId = context.resources.getIdentifier("key_shift", "id", context.packageName)
            keyboardView.findViewById<Button>(shiftId)?.setOnClickListener { onShiftPressed() }
        }
    }

    private fun setupSpaceButton(spaceButton: Button?) {
        spaceButton ?: return

        val longPressTimeoutMs = ViewConfiguration.getLongPressTimeout().toLong()
        val swipeThresholdPx = dpToPx(SPACE_SWIPE_SWITCH_THRESHOLD_DP).toFloat()

        var isLongPressHandled = false
        var isSwipeSwitchHandled = false
        var downX = 0f
        var downY = 0f

        spaceButton.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    applyTapFeedback(view)
                    isLongPressHandled = false
                    isSwipeSwitchHandled = false
                    downX = event.x
                    downY = event.y

                    spaceLongPressRunnable?.let(mainHandler::removeCallbacks)
                    spaceLongPressRunnable = Runnable {
                        isLongPressHandled = true
                        onSpaceLongPressed(view)
                    }
                    mainHandler.postDelayed(spaceLongPressRunnable!!, longPressTimeoutMs)
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!isLongPressHandled && !isSwipeSwitchHandled) {
                        val dx = event.x - downX
                        val dy = event.y - downY
                        if (kotlin.math.abs(dx) >= swipeThresholdPx && kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                            spaceLongPressRunnable?.let(mainHandler::removeCallbacks)
                            isSwipeSwitchHandled = true
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onLanguageSwitch()
                        }
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    spaceLongPressRunnable?.let(mainHandler::removeCallbacks)
                    if (!isLongPressHandled && !isSwipeSwitchHandled) {
                        onSpacePressed()
                    }
                    view.performClick()
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    spaceLongPressRunnable?.let(mainHandler::removeCallbacks)
                    true
                }

                else -> false
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Touch listener with press animation
    // ─────────────────────────────────────────────────────────────────────────

    private fun setFastTapListener(button: Button?, onTap: () -> Unit) {
        button?.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    applyTapFeedback(view)
                    onTap()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.performClick()
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Single-phase scale-down flash on key press (Gboard-style).
     * Removed the scale-back chain — the view naturally resets via the next press.
     * This halves animation overhead from ~80 ms to ~20 ms per key.
     */
    private fun animateKeyPress(view: View) {
        view.animate().cancel()
        view.scaleX = PRESS_SCALE
        view.scaleY = PRESS_SCALE
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(ANIM_DURATION_MS)
            .start()
    }

    private fun applyTapFeedback(view: View) {
        if (!shouldEmitVisualFeedback()) return
        animateKeyPress(view)
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    private fun shouldEmitVisualFeedback(): Boolean {
        val now = SystemClock.uptimeMillis()
        val allow = (now - lastFeedbackUptimeMs) >= FEEDBACK_THROTTLE_MS
        if (allow) lastFeedbackUptimeMs = now
        return allow
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Key action handlers (all input logic unchanged)
    // ─────────────────────────────────────────────────────────────────────────

    private fun onEnglishKeyPressed(key: String) {
        val output = if (shouldUseUppercase()) key.uppercase() else key.lowercase()
        composingText.append(output)
        currentInputConnection?.setComposingText(composingText.toString(), 1)
        updateSuggestions()

        isAutoCapNext = false
        if (shiftState == ShiftState.SHIFT_ONCE) {
            shiftState = ShiftState.OFF
            refreshEnglishKeyLabels()
        }
    }

    private fun onUrduKeyPressed(urduLetter: String) {
        composingText.append(urduLetter)
        currentInputConnection?.setComposingText(composingText.toString(), 1)
        updateSuggestions()
        isAutoCapNext = false
    }

    private fun onPunctuationPressed(symbol: String) {
        if (composingText.isNotEmpty()) {
            currentInputConnection?.commitText(composingText.toString(), 1)
            clearComposingState()
        }
        commitText(symbol)
        isAutoCapNext = symbol == "." || symbol == "?" || symbol == "؟" || symbol == "!" || symbol == "۔"
    }

    private fun onSpacePressed() {
        if (composingText.isNotEmpty()) {
            currentInputConnection?.commitText(composingText.toString(), 1)
            currentInputConnection?.commitText(" ", 1)
            clearComposingState()
        } else {
            commitText(" ")
        }
    }

    private fun onEnterPressed() {
        if (composingText.isNotEmpty()) {
            currentInputConnection?.commitText(composingText.toString(), 1)
            clearComposingState()
        }
        commitText("\n")
        isAutoCapNext = true
    }

    private fun onShiftPressed() {
        if (isUrduKeyboardLayout) return

        if (shiftState == ShiftState.CAPS_LOCK) {
            shiftState = ShiftState.OFF
            refreshEnglishKeyLabels()
            return
        }

        val now = SystemClock.uptimeMillis()
        val doubleTapTimeoutMs = ViewConfiguration.getDoubleTapTimeout().toLong()
        shiftState = if (
            shiftState == ShiftState.SHIFT_ONCE &&
            (now - lastShiftTapUptimeMs) <= doubleTapTimeoutMs
        ) {
            ShiftState.CAPS_LOCK
        } else {
            ShiftState.SHIFT_ONCE
        }
        lastShiftTapUptimeMs = now
        refreshEnglishKeyLabels()
    }

    private fun shouldUseUppercase() = shiftState != ShiftState.OFF || isAutoCapNext

    /**
     * Refresh English key labels AND shift icon drawable when shift state changes.
     * Dark-mode aware: uses the appropriate icon variant for contrast on both themes.
     */
    private fun refreshEnglishKeyLabels() {
        if (isUrduKeyboardLayout || isSymbolMode) return

        val uppercase = shouldUseUppercase()
        englishButtonsMap.forEach { (key, button) ->
            button.text = if (uppercase) key.uppercase() else key.lowercase()
        }

        // Detect night mode so we pick the right icon variant for contrast.
        val nightModeBits = context.resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK
        val isNight = nightModeBits == android.content.res.Configuration.UI_MODE_NIGHT_YES

        val shiftIcon = when (shiftState) {
            ShiftState.OFF -> if (isNight) R.drawable.ic_shift_dark else R.drawable.ic_shift
            ShiftState.SHIFT_ONCE -> if (isNight) R.drawable.ic_shift_active_dark else R.drawable.ic_shift_active
            ShiftState.CAPS_LOCK -> if (isNight) R.drawable.ic_shift_caps_dark else R.drawable.ic_shift_caps
        }

        val shiftButton = keyboardView.findViewById<Button>(R.id.key_shift)
        shiftButton?.setCompoundDrawablesWithIntrinsicBounds(0, shiftIcon, 0, 0)
        shiftButton?.isActivated = (shiftState == ShiftState.CAPS_LOCK)
        shiftButton?.contentDescription = when (shiftState) {
            ShiftState.OFF -> "Shift off"
            ShiftState.SHIFT_ONCE -> "Shift on for next letter"
            ShiftState.CAPS_LOCK -> "Caps lock on"
        }
    }

    private fun onLanguageSwitch() {
        languageManager.switchToNextLanguage()
        val newLang = languageManager.getCurrentLanguage()
        val wasSymbol = isSymbolMode
        val wasEmoji = isEmojiMode
        val wasUrdu = isUrduKeyboardLayout
        isUrduKeyboardLayout = (newLang == "ur")
        if (isSymbolMode) isSymbolMode = false
        if (isEmojiMode) isEmojiMode = false

        val needsRebuild = wasSymbol || wasEmoji || wasUrdu != isUrduKeyboardLayout
        if (needsRebuild) {
            (context as? UrduKeyboardService)?.recreateKeyboard()
        } else {
            updateKeyboardLayout()
        }
        isAutoCapNext = true
        shiftState = ShiftState.OFF

        val langName = when (newLang) {
            "ur" -> "اردو"
            else -> "English"
        }
        Toast.makeText(context, "Keyboard: $langName", Toast.LENGTH_SHORT).show()
    }

    private fun onVoiceInput() {
        if (::voiceInputManager.isInitialized) {
            val language = when (languageManager.getCurrentLanguage()) {
                "ur" -> "ur-PK"
                else -> "en-US"
            }
            voiceInputManager.startListening(language)
        }
    }

    private fun onSymbolsModeToggle() {
        isSymbolMode = !isSymbolMode
        isEmojiMode = false
        (context as? UrduKeyboardService)?.recreateKeyboard()
    }

    private fun updateKeyboardLayout() {
        val currentLang = languageManager.getCurrentLanguage()
        val langText = "Space"
        val langShortText = when (currentLang) {
            "ur" -> "اردو"
            else -> "EN"
        }

        val spaceButtonId = when {
            isSymbolMode -> R.id.sym_key_space
            isUrduKeyboardLayout -> R.id.urdu_key_space
            else -> R.id.key_space
        }
        keyboardView.findViewById<Button>(spaceButtonId)?.text = langText

        val languageButtonId = when {
            isSymbolMode -> R.id.sym_key_language
            isUrduKeyboardLayout -> R.id.urdu_key_language
            else -> R.id.key_language
        }
        keyboardView.findViewById<Button>(languageButtonId)?.text = langShortText
    }

    private fun onSpaceLongPressed(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        (context as? UrduKeyboardService)?.showSystemKeyboardPicker()
    }

    private fun commitText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Composing and Suggestions Logic
    // ─────────────────────────────────────────────────────────────────────────

    private fun setupSuggestionButtons() {
        suggestionLeftBtn = keyboardView.findViewById(R.id.suggestion_left)
        suggestionCenterBtn = keyboardView.findViewById(R.id.suggestion_center)
        suggestionRightBtn = keyboardView.findViewById(R.id.suggestion_right)

        clearSuggestionsUI()

        setFastTapListener(suggestionLeftBtn) { selectSuggestion(suggestionLeftBtn?.text?.toString() ?: "") }
        setFastTapListener(suggestionCenterBtn) { selectSuggestion(suggestionCenterBtn?.text?.toString() ?: "") }
        setFastTapListener(suggestionRightBtn) { selectSuggestion(suggestionRightBtn?.text?.toString() ?: "") }
    }

    private fun selectSuggestion(suggestion: String) {
        if (suggestion.isEmpty()) return
        // Cancel any pending debounce before committing
        suggestionDebounceRunnable?.let(mainHandler::removeCallbacks)
        currentInputConnection?.commitText(suggestion, 1)
        currentInputConnection?.commitText(" ", 1)
        // Track frequency so this word bubbles up in future suggestions
        ioScope.launch { preferenceHelper.incrementWordFrequency(suggestion) }
        clearComposingState()
    }

    private fun clearComposingState() {
        composingText.setLength(0)
        currentInputConnection?.finishComposingText()
        clearSuggestionsUI()
    }

    private fun clearSuggestionsUI() {
        suggestionLeftBtn?.text = ""
        suggestionCenterBtn?.text = ""
        suggestionRightBtn?.text = ""
    }

    fun handleBackspaceAction(): Boolean {
        if (composingText.isNotEmpty()) {
            composingText.deleteCharAt(composingText.length - 1)
            if (composingText.isEmpty()) {
                clearComposingState()
            } else {
                currentInputConnection?.setComposingText(composingText.toString(), 1)
                updateSuggestions()
            }
            return true
        }
        return false
    }

    private fun updateSuggestions() {
        val input = composingText.toString()
        if (input.isEmpty()) {
            clearSuggestionsUI()
            suggestionDebounceRunnable?.let(mainHandler::removeCallbacks)
            return
        }

        // Debounce: cancel any pending lookup and schedule a fresh one
        // after SUGGESTION_DEBOUNCE_MS of inactivity so fast typists don't
        // flood the IO thread with coroutines on every single keypress.
        suggestionDebounceRunnable?.let(mainHandler::removeCallbacks)
        suggestionDebounceRunnable = Runnable {
            val snapshot = composingText.toString()
            if (snapshot.isEmpty()) return@Runnable
            ioScope.launch {
                val suggestions = getSuggestionsForInput(snapshot)
                withContext(Dispatchers.Main) {
                    if (composingText.toString() == snapshot) {
                        displaySuggestions(suggestions, snapshot)
                    }
                }
            }
        }
        mainHandler.postDelayed(suggestionDebounceRunnable!!, SUGGESTION_DEBOUNCE_MS)
    }

    private fun getSuggestionsForInput(input: String): List<String> {
        // Build a frequency map of all tracked Urdu words for ranked completions
        val freqMap = preferenceHelper.getTopFrequentWords(50)
            .associate { (word, count) -> word to count }
        return phoneticMapper.getUrduSuggestions(input, freqMap)
    }

    private fun displaySuggestions(suggestions: List<String>, rawInput: String) {
        clearSuggestionsUI()
        if (suggestions.isEmpty()) return

        suggestionLeftBtn?.text = rawInput

        if (suggestions.isNotEmpty()) {
            suggestionCenterBtn?.text = suggestions[0]
        }

        if (suggestions.size > 1) {
            suggestionRightBtn?.text = suggestions[1]
        }
    }

    private fun onEmojiModeToggle() {
        isEmojiMode = !isEmojiMode
        isSymbolMode = false
        (context as? UrduKeyboardService)?.recreateKeyboard()
    }

    private fun setupEmojiKeyboardButtons() {
        val categories = mapOf(
            R.id.emoji_tab_recents to EmojiCategory.RECENTS,
            R.id.emoji_tab_smileys to EmojiCategory.SMILEYS,
            R.id.emoji_tab_people to EmojiCategory.PEOPLE,
            R.id.emoji_tab_animals to EmojiCategory.ANIMALS,
            R.id.emoji_tab_food to EmojiCategory.FOOD,
            R.id.emoji_tab_travel to EmojiCategory.TRAVEL,
            R.id.emoji_tab_activities to EmojiCategory.ACTIVITIES,
            R.id.emoji_tab_objects to EmojiCategory.OBJECTS,
            R.id.emoji_tab_symbols to EmojiCategory.SYMBOLS,
            R.id.emoji_tab_flags to EmojiCategory.FLAGS
        )

        categories.forEach { (id, category) ->
            keyboardView.findViewById<Button>(id)?.setOnClickListener {
                currentEmojiCategory = category
                highlightActiveTab()
                updateEmojiGrid()
            }
        }

        keyboardView.findViewById<Button>(R.id.emoji_key_abc)?.setOnClickListener {
            isEmojiMode = false
            (context as? UrduKeyboardService)?.recreateKeyboard()
        }

        keyboardView.findViewById<Button>(R.id.emoji_key_space)?.setOnClickListener {
            commitText(" ")
        }

        keyboardView.findViewById<Button>(R.id.emoji_key_backspace)?.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    applyTapFeedback(view)
                    backspaceHandler.onBackspaceDown()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    backspaceHandler.onBackspaceUp()
                    view.performClick()
                    true
                }
                else -> false
            }
        }

        loadRecentEmojis()
        highlightActiveTab()
        updateEmojiGrid()
    }

    private fun highlightActiveTab() {
        val categories = listOf(
            R.id.emoji_tab_recents to EmojiCategory.RECENTS,
            R.id.emoji_tab_smileys to EmojiCategory.SMILEYS,
            R.id.emoji_tab_people to EmojiCategory.PEOPLE,
            R.id.emoji_tab_animals to EmojiCategory.ANIMALS,
            R.id.emoji_tab_food to EmojiCategory.FOOD,
            R.id.emoji_tab_travel to EmojiCategory.TRAVEL,
            R.id.emoji_tab_activities to EmojiCategory.ACTIVITIES,
            R.id.emoji_tab_objects to EmojiCategory.OBJECTS,
            R.id.emoji_tab_symbols to EmojiCategory.SYMBOLS,
            R.id.emoji_tab_flags to EmojiCategory.FLAGS
        )

        categories.forEach { (id, category) ->
            val btn = keyboardView.findViewById<Button>(id) ?: return@forEach
            if (category == currentEmojiCategory) {
                btn.setBackgroundColor(context.getColor(android.R.color.darker_gray))
            } else {
                btn.setBackgroundColor(context.getColor(android.R.color.transparent))
            }
        }
    }

    private fun updateEmojiGrid() {
        val grid = keyboardView.findViewById<android.widget.GridView>(R.id.emoji_grid) ?: return
        val list = when (currentEmojiCategory) {
            EmojiCategory.RECENTS -> recentEmojis
            EmojiCategory.SMILEYS -> smileysEmojis
            EmojiCategory.PEOPLE -> peopleEmojis
            EmojiCategory.ANIMALS -> animalsEmojis
            EmojiCategory.FOOD -> foodEmojis
            EmojiCategory.TRAVEL -> travelEmojis
            EmojiCategory.ACTIVITIES -> activitiesEmojis
            EmojiCategory.OBJECTS -> objectsEmojis
            EmojiCategory.SYMBOLS -> symbolsEmojis
            EmojiCategory.FLAGS -> flagsEmojis
        }

        grid.adapter = object : android.widget.BaseAdapter() {
            override fun getCount(): Int = list.size
            override fun getItem(position: Int): Any = list[position]
            override fun getItemId(position: Int): Long = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup?): View {
                val button = (convertView as? Button) ?: Button(context).apply {
                    textSize = 24f
                    gravity = android.view.Gravity.CENTER
                    setBackgroundColor(context.getColor(android.R.color.transparent))
                    setPadding(0, 0, 0, 0)
                    setOnClickListener {
                        val emoji = list[position]
                        commitText(emoji)

                        recentEmojis.remove(emoji)
                        recentEmojis.add(0, emoji)
                        if (recentEmojis.size > 24) {
                            recentEmojis.removeAt(recentEmojis.size - 1)
                        }
                        saveRecentEmojis()
                    }
                }
                button.text = list[position]
                return button
            }
        }
    }

    private fun loadRecentEmojis() {
        recentEmojis.clear()
        val helper = PreferenceHelper(context)
        val saved = helper.getRecentEmojis()
        if (saved != null) {
            recentEmojis.addAll(saved)
        }
        if (recentEmojis.isEmpty()) {
            recentEmojis.addAll(listOf("❤️", "😂", "😊", "👍", "🔥", "🙏", "✨", "🎉"))
        }
    }

    private fun saveRecentEmojis() {
        val helper = PreferenceHelper(context)
        helper.saveRecentEmojis(recentEmojis)
    }
}
