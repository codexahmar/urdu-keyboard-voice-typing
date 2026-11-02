package com.codexahmar.urdu_keyboard.keyboard

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.InputType
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewConfiguration
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Button
import androidx.emoji2.emojipicker.EmojiPickerView
import com.codexahmar.urdu_keyboard.R
import com.codexahmar.urdu_keyboard.utils.PreferenceHelper

/**
 * KeyboardController — Production-grade orchestrator for Urdu & English IME.
 *
 * Architecture:
 *  - [KeyboardLayerManager]: Zero-detachment layer visibility swapping (0ms latency).
 *  - [KeyTouchHandler]: Instant touch feedback, multi-touch rollover, space scrubbing.
 *  - [KeyboardSuggestionManager]: Async debounced suggestion engine (Urdu/English/Roman).
 *  - [EmojiSearchController]: Real-time in-place emoji search bar and results.
 *  - [LanguageManager]: Urdu/English language switching & persistence.
 *  - [VoiceInputManager]: Google speech recognition integration.
 *  - [BackspaceHandler]: Accelerated continuous backspacing.
 */
class KeyboardController(
    private val context: Context,
    private val preferenceHelper: PreferenceHelper
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val languageManager = LanguageManager(context, preferenceHelper)

    private var currentLayoutInflater: LayoutInflater? = null
    private var keyPopupOverlay: KeyPopupOverlay? = null

    private lateinit var voiceInputManager: VoiceInputManager
    private lateinit var backspaceHandler: BackspaceHandler

    private val currentInputConnection: InputConnection?
        get() = (context as? UrduKeyboardService)?.currentInputConnection

    private var isUrduKeyboardLayout = false
    private var shiftState = ShiftState.OFF
    private var lastShiftTapUptimeMs = 0L

    private var isSymbolMode = false
    private var isEmojiMode = false
    private var isHapticFeedbackEnabled = true

    private val composingText = StringBuilder()

    private enum class ShiftState {
        OFF,
        SHIFT_ONCE,
        CAPS_LOCK
    }

    private val touchHandler: KeyTouchHandler = KeyTouchHandler(
        context = context,
        mainHandler = mainHandler,
        isHapticFeedbackEnabled = { isHapticFeedbackEnabled },
        isNightMode = { layerManager.isNightMode },
        getPopupOverlay = { keyPopupOverlay },
        getInputConnection = { currentInputConnection }
    )

    private val layerManager: KeyboardLayerManager = KeyboardLayerManager(
        context = context,
        onKeyCommit = { commitText(it) }
    )

    private val suggestionManager: KeyboardSuggestionManager = KeyboardSuggestionManager(
        preferenceHelper = preferenceHelper,
        mainHandler = mainHandler,
        touchHandler = touchHandler
    )

    private val emojiSearchController: EmojiSearchController = EmojiSearchController(
        context = context,
        mainHandler = mainHandler,
        applyTapFeedback = { touchHandler.applyTapFeedback(it) }
    )

    init {
        val currentLang = languageManager.getCurrentLanguage()
        isUrduKeyboardLayout = (currentLang == "ur")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lifecycle & Input View Initialization
    // ─────────────────────────────────────────────────────────────────────────

    fun createKeyboardView(inflater: LayoutInflater): View {
        currentLayoutInflater = inflater

        if (!::backspaceHandler.isInitialized) {
            backspaceHandler = BackspaceHandler(
                context = context,
                getInputConnection = { currentInputConnection },
                onDelete = { count -> handleBackspaceAction(count) }
            )
        }

        if (context is UrduKeyboardService && !::voiceInputManager.isInitialized) {
            voiceInputManager = VoiceInputManager(context, context).apply {
                onListeningStateChanged = { listening ->
                    mainHandler.post { updateVoiceButtonState(listening) }
                }
            }
        }

        val container = layerManager.getOrCreateRootContainer()
        if (keyPopupOverlay == null) {
            val overlay = KeyPopupOverlay(context)
            keyPopupOverlay = overlay
            container.addView(overlay)
        }

        updateActiveLayer()
        return container
    }

    fun onStartInput(ic: InputConnection?, info: EditorInfo?) {
        isHapticFeedbackEnabled = preferenceHelper.isHapticFeedbackEnabled()
        val shouldCap = shouldAutoCap(info)
        shiftState = if (shouldCap) ShiftState.SHIFT_ONCE else ShiftState.OFF
        clearComposingState()
        updateShiftButtonIcon()
    }

    private fun shouldAutoCap(info: EditorInfo?): Boolean {
        if (info == null) return true
        val inputType = info.inputType
        if (inputType == 0) return true
        val clazz = inputType and InputType.TYPE_MASK_CLASS
        if (clazz != InputType.TYPE_CLASS_TEXT) return false
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        return when (variation) {
            InputType.TYPE_TEXT_VARIATION_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            InputType.TYPE_TEXT_VARIATION_URI,
            InputType.TYPE_TEXT_VARIATION_FILTER -> false
            else -> true
        }
    }

    fun onFinishInput() {
        touchHandler.commitPendingKey()
        keyPopupOverlay?.dismiss()
        shiftState = ShiftState.OFF
        clearComposingState()
        if (::voiceInputManager.isInitialized) {
            voiceInputManager.stopListening()
        }
    }

    fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        if (composingText.isNotEmpty()) {
            val isOurComposing = candidatesStart >= 0 &&
                candidatesEnd >= 0 &&
                newSelStart == candidatesEnd &&
                newSelEnd == candidatesEnd &&
                (candidatesEnd - candidatesStart) == composingText.length

            if (!isOurComposing) {
                clearComposingState()
            }
        }
    }

    fun cleanup() {
        exitEmojiSearchMode()
        touchHandler.cleanup()
        suggestionManager.cleanup()
        layerManager.cleanup()
        keyPopupOverlay = null
        if (::backspaceHandler.isInitialized) {
            backspaceHandler.cleanup()
        }
        if (::voiceInputManager.isInitialized) {
            voiceInputManager.cleanup()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Layer Transitions (Zero-Detachment In-Memory Swaps)
    // ─────────────────────────────────────────────────────────────────────────

    fun updateActiveLayer() {
        touchHandler.commitPendingKey()
        keyPopupOverlay?.dismiss()

        val inflater = currentLayoutInflater ?: LayoutInflater.from(context)

        val targetView = when {
            isEmojiMode -> layerManager.getOrCreateEmojiView(inflater) { setupEmojiViewButtons(it) }
            isSymbolMode -> layerManager.getOrCreateSymbolsView(inflater) { setupSymbolsViewButtons(it) }
            isUrduKeyboardLayout -> layerManager.getOrCreateUrduView(inflater) { setupUrduViewButtons(it) }
            else -> layerManager.getOrCreateEnglishView(inflater) { setupEnglishViewButtons(it) }
        }

        layerManager.activateLayer(targetView)

        if (targetView === layerManager.symbolsView) {
            layerManager.refreshSymbolsKeys(targetView, isUrduKeyboardLayout, touchHandler)
        }

        if (!isEmojiMode) {
            suggestionManager.updateActiveButtons(targetView)
        }

        layerManager.updateKeyboardLayoutLabels(isUrduKeyboardLayout)
        updateShiftButtonIcon()
        emojiSearchController.syncHeaderUI(targetView)
        if (emojiSearchController.isSearchMode) {
            updateEmojiSearchUI()
        }
        ensurePopupOverlayOnTop()
    }

    private fun ensurePopupOverlayOnTop() {
        val container = layerManager.rootContainer ?: return
        val overlay = keyPopupOverlay ?: return
        if (overlay.parent === container) {
            container.bringChildToFront(overlay)
        } else {
            container.addView(overlay)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // View Buttons Wiring
    // ─────────────────────────────────────────────────────────────────────────

    private fun setupEnglishViewButtons(view: View) {
        for (i in KeyboardConstants.ENGLISH_KEY_ID_MAP.indices) {
            val key = KeyboardConstants.ENGLISH_KEY_CHAR_MAP[i]
            val btn = view.findViewById<Button>(KeyboardConstants.ENGLISH_KEY_ID_MAP[i]) ?: continue
            val variants = KeyboardConstants.ENGLISH_LONG_PRESS_VARIANTS[key]
            touchHandler.bindSmoothKeyTouch(btn, key, variants) { onEnglishKeyPressed(it) }
        }

        touchHandler.setFastTapListener(view.findViewById(R.id.key_comma)) { onPunctuationPressed(",") }
        touchHandler.setFastTapListener(view.findViewById(R.id.key_dot)) { onPunctuationPressed(".") }

        touchHandler.setupSpaceButton(
            spaceButton = view.findViewById(R.id.key_space),
            onSpaceTap = { onSpacePressed() },
            onSpaceLongPress = { onSpaceLongPressed(it) }
        )
        touchHandler.setupBackspaceButton(view.findViewById(R.id.key_backspace), backspaceHandler)
        touchHandler.setFastTapListener(view.findViewById(R.id.key_enter)) { onEnterPressed() }
        touchHandler.setFastTapListener(view.findViewById(R.id.key_voice)) { onVoiceInput() }
        touchHandler.setFastTapListener(view.findViewById(R.id.key_language)) { onLanguageSwitch() }
        touchHandler.setFastTapListener(view.findViewById(R.id.key_symbols)) { onSymbolsModeToggle() }
        touchHandler.setFastTapListener(view.findViewById(R.id.key_emoji)) { onEmojiModeToggle() }
        touchHandler.setFastTapListener(view.findViewById(R.id.key_shift)) { onShiftPressed() }

        suggestionManager.bindButtons(view) { selectSuggestion(it) }
        emojiSearchController.setupHeader(
            view = view,
            onBack = { exitEmojiSearchMode(returnToEmojiPicker = true) },
            onClear = { updateEmojiSearchUI() }
        )
    }

    private fun setupUrduViewButtons(view: View) {
        for (index in 0 until 39) {
            val btn = view.findViewById<Button>(KeyboardConstants.URDU_KEY_IDS[index]) ?: continue
            val baseChar = KeyboardConstants.URDU_BASE_LETTERS[index]
            val variants = KeyboardConstants.URDU_LONG_PRESS_VARIANTS[baseChar]
            touchHandler.bindSmoothKeyTouch(btn, baseChar, variants) { onUrduKeyPressed(it) }
        }

        touchHandler.setFastTapListener(view.findViewById(R.id.urdu_key_comma)) { onPunctuationPressed("،") }
        touchHandler.setFastTapListener(view.findViewById(R.id.urdu_key_dot)) { onPunctuationPressed("۔") }

        touchHandler.setupSpaceButton(
            spaceButton = view.findViewById(R.id.urdu_key_space),
            onSpaceTap = { onSpacePressed() },
            onSpaceLongPress = { onSpaceLongPressed(it) }
        )
        touchHandler.setupBackspaceButton(view.findViewById(R.id.urdu_key_backspace), backspaceHandler)
        touchHandler.setFastTapListener(view.findViewById(R.id.urdu_key_enter)) { onEnterPressed() }
        touchHandler.setFastTapListener(view.findViewById(R.id.key_voice)) { onVoiceInput() }
        touchHandler.setFastTapListener(view.findViewById(R.id.urdu_key_language)) { onLanguageSwitch() }
        touchHandler.setFastTapListener(view.findViewById(R.id.urdu_key_symbols)) { onSymbolsModeToggle() }
        touchHandler.setFastTapListener(view.findViewById(R.id.urdu_key_emoji)) { onEmojiModeToggle() }

        suggestionManager.bindButtons(view) { selectSuggestion(it) }
        emojiSearchController.setupHeader(
            view = view,
            onBack = { exitEmojiSearchMode(returnToEmojiPicker = true) },
            onClear = { updateEmojiSearchUI() }
        )
    }

    private fun setupSymbolsViewButtons(view: View) {
        layerManager.refreshSymbolsKeys(view, isUrduKeyboardLayout, touchHandler)

        for ((id, sym) in KeyboardConstants.COMMON_SYMBOLS) {
            val btn = view.findViewById<Button?>(id) ?: continue
            btn.text = sym
            btn.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 19f)
            touchHandler.setFastTapListener(btn) { commitText(sym) }
        }

        touchHandler.setupSpaceButton(
            spaceButton = view.findViewById(R.id.sym_key_space),
            onSpaceTap = { onSpacePressed() },
            onSpaceLongPress = { onSpaceLongPressed(it) }
        )
        touchHandler.setupBackspaceButton(view.findViewById(R.id.sym_backspace), backspaceHandler)
        touchHandler.setFastTapListener(view.findViewById(R.id.sym_key_enter)) { onEnterPressed() }
        touchHandler.setFastTapListener(view.findViewById(R.id.sym_key_voice)) { onVoiceInput() }
        touchHandler.setFastTapListener(view.findViewById(R.id.sym_key_language)) { onLanguageSwitch() }
        touchHandler.setFastTapListener(view.findViewById(R.id.sym_key_abc)) { onSymbolsModeToggle() }
        touchHandler.setFastTapListener(view.findViewById(R.id.sym_key_emoji)) { onEmojiModeToggle() }

        suggestionManager.bindButtons(view) { selectSuggestion(it) }
    }

    private fun setupEmojiViewButtons(view: View) {
        val emojiPickerView = view.findViewById<EmojiPickerView>(R.id.emoji_picker_view)
        emojiPickerView?.setOnEmojiPickedListener { emojiViewItem ->
            touchHandler.applyTapFeedback(emojiPickerView)
            commitText(emojiViewItem.emoji)
        }
        layerManager.styleEmojiPickerTitles(emojiPickerView)

        val abcBtn = view.findViewById<Button>(R.id.emoji_key_abc)
        abcBtn?.text = if (isUrduKeyboardLayout) "اردو" else "ABC"
        touchHandler.setFastTapListener(abcBtn) {
            clearComposingState()
            exitEmojiSearchMode()
            isEmojiMode = false
            updateActiveLayer()
        }

        touchHandler.setupBackspaceButton(view.findViewById(R.id.emoji_key_backspace), backspaceHandler)
        touchHandler.setFastTapListener(view.findViewById(R.id.emoji_search_pill)) {
            enterEmojiSearchMode()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Emoji Search Intercepts & Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private fun enterEmojiSearchMode() {
        emojiSearchController.enter()
        isEmojiMode = false
        updateActiveLayer()
        emojiSearchController.syncHeaderUI(layerManager.currentActiveView)
        updateEmojiSearchUI()
    }

    private fun exitEmojiSearchMode(returnToEmojiPicker: Boolean = false) {
        emojiSearchController.exit()
        emojiSearchController.syncHeaderUI(layerManager.currentActiveView)
        if (returnToEmojiPicker) {
            isEmojiMode = true
            updateActiveLayer()
        }
    }

    private fun updateEmojiSearchUI() {
        emojiSearchController.updateUI(
            activeView = layerManager.currentActiveView,
            isUrduLayout = isUrduKeyboardLayout,
            onEmojiSelected = { commitText(it) }
        )
    }

    fun isEmojiModeActive(): Boolean = isEmojiMode || emojiSearchController.isSearchMode

    fun handleEmojiSearchKeyEvent(keyCode: Int, event: KeyEvent): Boolean {
        return emojiSearchController.handleKeyEvent(
            keyCode = keyCode,
            event = event,
            onUpdateUI = { updateEmojiSearchUI() },
            onExitToPicker = { exitEmojiSearchMode(returnToEmojiPicker = true) },
            onExitSearch = { exitEmojiSearchMode(returnToEmojiPicker = false) }
        )
    }

    fun handleEmojiSearchKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return emojiSearchController.handleKeyUp(keyCode, event)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Typing Action Logic
    // ─────────────────────────────────────────────────────────────────────────

    private fun onEnglishKeyPressed(key: String) {
        val uppercase = shiftState != ShiftState.OFF
        val output = if (uppercase) key.uppercase() else key.lowercase()

        if (emojiSearchController.isSearchMode) {
            emojiSearchController.searchQuery.append(output)
            updateEmojiSearchUI()
            return
        }

        composingText.append(output)
        val ic = currentInputConnection
        ic?.beginBatchEdit()
        ic?.setComposingText(composingText.toString(), 1)
        ic?.endBatchEdit()

        triggerSuggestionsUpdate()

        if (shiftState == ShiftState.SHIFT_ONCE) {
            shiftState = ShiftState.OFF
            updateShiftButtonIcon()
        }
    }

    private fun onUrduKeyPressed(urduLetter: String) {
        if (emojiSearchController.isSearchMode) {
            emojiSearchController.searchQuery.append(urduLetter)
            updateEmojiSearchUI()
            return
        }

        composingText.append(urduLetter)
        val ic = currentInputConnection
        ic?.beginBatchEdit()
        ic?.setComposingText(composingText.toString(), 1)
        ic?.endBatchEdit()

        triggerSuggestionsUpdate()
    }

    private fun onPunctuationPressed(symbol: String) {
        if (emojiSearchController.isSearchMode) {
            emojiSearchController.searchQuery.append(symbol)
            updateEmojiSearchUI()
            return
        }

        val ic = currentInputConnection
        ic?.beginBatchEdit()
        if (composingText.isNotEmpty()) {
            ic?.commitText(composingText.toString(), 1)
            clearComposingState()
        }
        ic?.commitText(symbol, 1)
        ic?.endBatchEdit()

        if (symbol == "." || symbol == "?" || symbol == "!" || symbol == "۔" || symbol == "؟") {
            shiftState = ShiftState.SHIFT_ONCE
            updateShiftButtonIcon()
        }
    }

    private fun onSpacePressed() {
        if (emojiSearchController.isSearchMode) {
            emojiSearchController.searchQuery.append(" ")
            updateEmojiSearchUI()
            return
        }

        val ic = currentInputConnection
        ic?.beginBatchEdit()
        var committedWord: String? = null
        if (composingText.isNotEmpty()) {
            val committed = composingText.toString()
            ic?.commitText("${committed} ", 1)
            committedWord = committed
            clearComposingState(keepSuggestions = true)
        } else {
            ic?.commitText(" ", 1)
            committedWord = getPreviousWordFromEditor()
        }
        ic?.endBatchEdit()

        if (!committedWord.isNullOrBlank()) {
            suggestionManager.learnCommittedWord(committedWord)
            val isUrdu = isUrduWord(committedWord)
            suggestionManager.requestNextWordPredictions(committedWord, isUrdu)
        } else {
            suggestionManager.clearSuggestionsUI()
        }
    }

    private fun onEnterPressed() {
        if (emojiSearchController.isSearchMode) {
            exitEmojiSearchMode(returnToEmojiPicker = false)
            return
        }

        val ic = currentInputConnection
        ic?.beginBatchEdit()
        if (composingText.isNotEmpty()) {
            ic?.commitText(composingText.toString(), 1)
            clearComposingState()
        }
        ic?.commitText("\n", 1)
        ic?.endBatchEdit()

        shiftState = ShiftState.SHIFT_ONCE
        updateShiftButtonIcon()
    }

    private fun onShiftPressed() {
        if (isUrduKeyboardLayout) return

        if (shiftState == ShiftState.CAPS_LOCK) {
            shiftState = ShiftState.OFF
            updateShiftButtonIcon()
            return
        }

        val now = SystemClock.uptimeMillis()
        val doubleTapTimeoutMs = ViewConfiguration.getDoubleTapTimeout().toLong()
        shiftState = if (
            shiftState == ShiftState.SHIFT_ONCE &&
            (now - lastShiftTapUptimeMs) <= doubleTapTimeoutMs
        ) {
            ShiftState.CAPS_LOCK
        } else if (shiftState == ShiftState.OFF) {
            ShiftState.SHIFT_ONCE
        } else {
            ShiftState.OFF
        }
        lastShiftTapUptimeMs = now
        updateShiftButtonIcon()
    }

    private fun updateShiftButtonIcon() {
        if (isUrduKeyboardLayout || isSymbolMode) return

        val isNight = layerManager.isNightMode
        val shiftIcon = when (shiftState) {
            ShiftState.OFF -> if (isNight) R.drawable.ic_shift_dark else R.drawable.ic_shift
            ShiftState.SHIFT_ONCE -> if (isNight) R.drawable.ic_shift_active_dark else R.drawable.ic_shift_active
            ShiftState.CAPS_LOCK -> if (isNight) R.drawable.ic_shift_caps_dark else R.drawable.ic_shift_caps
        }

        val shiftButton = layerManager.englishView?.findViewById<Button>(R.id.key_shift)
        shiftButton?.setCompoundDrawablesWithIntrinsicBounds(0, shiftIcon, 0, 0)
        shiftButton?.isActivated = (shiftState == ShiftState.CAPS_LOCK)
        shiftButton?.contentDescription = when (shiftState) {
            ShiftState.OFF -> "Shift off"
            ShiftState.SHIFT_ONCE -> "Shift on for next letter"
            ShiftState.CAPS_LOCK -> "Caps lock on"
        }
    }

    private fun onLanguageSwitch() {
        clearComposingState()
        languageManager.switchToNextLanguage()
        val newLang = languageManager.getCurrentLanguage()
        isUrduKeyboardLayout = (newLang == "ur")
        isSymbolMode = false
        isEmojiMode = false

        updateActiveLayer()
        shiftState = if (!isUrduKeyboardLayout) ShiftState.SHIFT_ONCE else ShiftState.OFF
        updateShiftButtonIcon()
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
        clearComposingState()
        exitEmojiSearchMode()
        isSymbolMode = !isSymbolMode
        isEmojiMode = false
        updateActiveLayer()
    }

    private fun onEmojiModeToggle() {
        clearComposingState()
        exitEmojiSearchMode()
        isEmojiMode = !isEmojiMode
        isSymbolMode = false
        updateActiveLayer()
    }

    private fun onSpaceLongPressed(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        (context as? UrduKeyboardService)?.showSystemKeyboardPicker()
    }

    private fun commitText(text: String) {
        val ic = currentInputConnection
        ic?.beginBatchEdit()
        if (composingText.isNotEmpty()) {
            ic?.commitText(composingText.toString(), 1)
            clearComposingState()
        }
        ic?.commitText(text, 1)
        ic?.endBatchEdit()
    }

    fun commitVoiceText(text: String) {
        commitText(text)
    }

    private fun selectSuggestion(suggestion: String) {
        if (suggestion.isEmpty()) return
        suggestionManager.cancelDebounce()

        val ic = currentInputConnection
        ic?.beginBatchEdit()
        ic?.commitText("$suggestion ", 1)
        clearComposingState(keepSuggestions = true)
        ic?.endBatchEdit()

        suggestionManager.learnCommittedWord(suggestion)
        suggestionManager.requestNextWordPredictions(suggestion, isUrduWord(suggestion))
    }

    private fun clearComposingState(keepSuggestions: Boolean = false) {
        composingText.setLength(0)
        currentInputConnection?.finishComposingText()
        if (!keepSuggestions) {
            suggestionManager.clearSuggestionsUI()
        }
    }

    fun handleBackspaceAction(count: Int = 1): Boolean {
        if (emojiSearchController.handleBackspace(count, { updateEmojiSearchUI() }, { exitEmojiSearchMode(true) })) {
            return true
        }

        if (composingText.isNotEmpty()) {
            val ic = currentInputConnection
            val textBefore = ic?.getTextBeforeCursor(composingText.length, 0)?.toString()
            if (textBefore != null && textBefore != composingText.toString()) {
                clearComposingState()
                return false
            }

            val toDelete = minOf(count, composingText.length)
            composingText.delete(composingText.length - toDelete, composingText.length)
            ic?.beginBatchEdit()
            if (composingText.isEmpty()) {
                clearComposingState(keepSuggestions = true)
                suggestionManager.onComposingEmpty(getPreviousWordFromEditor())
            } else {
                ic?.setComposingText(composingText.toString(), 1)
                triggerSuggestionsUpdate()
            }
            ic?.endBatchEdit()
            return true
        }
        return false
    }

    fun performBackspace() {
        if (!handleBackspaceAction(1)) {
            val ic = currentInputConnection ?: return
            val selectedText = ic.getSelectedText(0)
            if (!selectedText.isNullOrEmpty()) {
                ic.commitText("", 1)
                return
            }
            val deleted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ic.deleteSurroundingTextInCodePoints(1, 0)
            } else {
                ic.deleteSurroundingText(1, 0)
            }
            if (!deleted) {
                (context as? android.inputmethodservice.InputMethodService)
                    ?.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
            }
        }
    }

    private fun getPreviousWordFromEditor(): String? {
        val ic = currentInputConnection ?: return null
        val text = ic.getTextBeforeCursor(40, 0)?.toString() ?: return null
        if (text.isBlank()) return null
        val tokens = text.trimEnd().split(Regex("[\\s\\p{Punct}،۔؟]+"))
        return tokens.lastOrNull()?.trim()
    }

    private fun triggerSuggestionsUpdate() {
        suggestionManager.onComposingChanged(
            currentComposing = composingText.toString(),
            prevWord = getPreviousWordFromEditor(),
            isUrduLayout = isUrduKeyboardLayout
        )
    }

    private fun isUrduWord(word: String): Boolean {
        val firstChar = word.trim().firstOrNull() ?: ' '
        return firstChar in '\u0600'..'\u06FF' ||
               firstChar in '\uFB50'..'\uFDFF' ||
               firstChar in '\uFE70'..'\uFEFF'
    }

    private fun updateVoiceButtonState(isListening: Boolean) {
        val activeView = layerManager.currentActiveView ?: return
        for (id in listOf(R.id.key_voice, R.id.sym_key_voice)) {
            val btn = activeView.findViewById<Button>(id) ?: continue
            btn.isSelected = isListening
            btn.alpha = if (isListening) 0.6f else 1.0f
        }
    }
}
