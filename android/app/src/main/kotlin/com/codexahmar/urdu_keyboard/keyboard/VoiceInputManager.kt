package com.codexahmar.urdu_keyboard.keyboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import java.util.Locale

/**
 * Manages voice-to-text input using Android SpeechRecognizer.
 *
 * Implements standard speech recognition behavior:
 *  - Does NOT restart itself in loops.
 *  - Supports single-tap toggle (tap to start, tap again to stop).
 *  - Does not cut off mid-speech while the person is speaking.
 *  - Automatically stops after silence/inactivity.
 */
class VoiceInputManager(
    private val context: Context,
    private val keyboardService: UrduKeyboardService
) : RecognitionListener {

    var onListeningStateChanged: ((Boolean) -> Unit)? = null

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var isSpeaking = false
    private var currentLanguageCode = "en-US"
    private val mainHandler = Handler(Looper.getMainLooper())

    // Initial timeout if user presses mic but never speaks
    private val initialSilenceRunnable = Runnable {
        if (isListening && !isSpeaking) {
            Log.d(TAG, "Initial silence timeout reached without speech. Stopping.")
            stopListening(showToast = true, toastMessage = "No speech detected")
        }
    }

    companion object {
        private const val TAG = "VoiceInputManager"
        private const val INITIAL_SILENCE_TIMEOUT_MS = 6000L
        private const val COMPLETE_SILENCE_LENGTH_MS = 2500L
        private const val POSSIBLY_COMPLETE_SILENCE_MS = 2000L
        private const val MIN_SPEECH_LENGTH_MS = 2500L
    }

    init {
        initializeRecognizer()
    }

    private fun initializeRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(this)
        }
    }

    /**
     * Toggles or starts voice input.
     * If already listening, stops immediately.
     */
    fun startListening(languageCode: String = "en-US") {
        if (isListening) {
            stopListening(showToast = true, toastMessage = "Microphone off")
            return
        }

        currentLanguageCode = languageCode
        startListeningInternal()
    }

    private fun startListeningInternal() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Toast.makeText(context, "Speech recognition not available on this device", Toast.LENGTH_SHORT).show()
            return
        }

        if (speechRecognizer == null) {
            initializeRecognizer()
        }

        val recognizer = speechRecognizer
        if (recognizer == null) {
            Toast.makeText(context, "Speech recognition initialization failed", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguageCode)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, currentLanguageCode)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, currentLanguageCode)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)

            // Extended silence windows so the speaker is not cut off mid-sentence
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, COMPLETE_SILENCE_LENGTH_MS)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, POSSIBLY_COMPLETE_SILENCE_MS)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, MIN_SPEECH_LENGTH_MS)

            putExtra("android.speech.extra.DICTATION_MODE", true)
        }

        try {
            isSpeaking = false
            isListening = true
            onListeningStateChanged?.invoke(true)
            recognizer.startListening(intent)

            // Schedule fallback timeout in case user never starts speaking
            mainHandler.removeCallbacks(initialSilenceRunnable)
            mainHandler.postDelayed(initialSilenceRunnable, INITIAL_SILENCE_TIMEOUT_MS)

            val langLabel = getLangDisplayName(currentLanguageCode)
            Toast.makeText(context, "🎤 Listening ($langLabel)... Speak now", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting voice recognition", e)
            cleanupListeningState()
        }
    }

    /**
     * Stops listening session cleanly without restarting.
     */
    fun stopListening(showToast: Boolean = false, toastMessage: String? = null) {
        mainHandler.removeCallbacks(initialSilenceRunnable)
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recognizer", e)
        }
        cleanupListeningState()

        if (showToast && toastMessage != null) {
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Cleans up all resources on keyboard destroy.
     */
    fun cleanup() {
        mainHandler.removeCallbacks(initialSilenceRunnable)
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
        cleanupListeningState()
    }

    private fun cleanupListeningState() {
        isListening = false
        isSpeaking = false
        onListeningStateChanged?.invoke(false)
    }

    private fun getLangDisplayName(code: String): String {
        return if (code.startsWith("ur")) "Urdu" else "English"
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RecognitionListener Callbacks
    // ─────────────────────────────────────────────────────────────────────────

    override fun onReadyForSpeech(params: Bundle?) {
        Log.d(TAG, "onReadyForSpeech")
        isListening = true
        onListeningStateChanged?.invoke(true)
    }

    override fun onBeginningOfSpeech() {
        Log.d(TAG, "onBeginningOfSpeech — User started talking")
        isSpeaking = true
        // Cancel initial silence timeout since user is actively speaking
        mainHandler.removeCallbacks(initialSilenceRunnable)
    }

    override fun onRmsChanged(rmsdB: Float) {
        if (rmsdB > 2.0f && !isSpeaking) {
            isSpeaking = true
            mainHandler.removeCallbacks(initialSilenceRunnable)
        }
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        Log.d(TAG, "onEndOfSpeech — User paused or finished utterance")
        isSpeaking = false
    }

    override fun onError(error: Int) {
        mainHandler.removeCallbacks(initialSilenceRunnable)
        cleanupListeningState()
        Log.d(TAG, "onError: code=$error")

        when (error) {
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
            SpeechRecognizer.ERROR_NO_MATCH -> {
                Log.d(TAG, "No speech matched or timeout reached. Microphone turned off.")
            }
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                Toast.makeText(context, "Microphone permission required for voice typing", Toast.LENGTH_LONG).show()
            }
            SpeechRecognizer.ERROR_NETWORK,
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                Toast.makeText(context, "Voice typing requires active internet connection", Toast.LENGTH_SHORT).show()
            }
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                Toast.makeText(context, "Speech service busy, please try again", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Log.w(TAG, "Recognition error: $error")
            }
        }
    }

    override fun onResults(results: Bundle?) {
        mainHandler.removeCallbacks(initialSilenceRunnable)
        cleanupListeningState()

        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            var recognizedText = matches[0].trim()
            if (recognizedText.isNotEmpty()) {
                recognizedText = formatTextWithPunctuation(recognizedText, currentLanguageCode)
                keyboardService.insertVoiceText(recognizedText + " ")
            }
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        // While partial results are arriving, speech is actively ongoing
        isSpeaking = true
        mainHandler.removeCallbacks(initialSilenceRunnable)
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    private fun formatTextWithPunctuation(text: String, lang: String): String {
        if (text.isEmpty()) return text

        val lastChar = text.last()
        if (lastChar == '.' || lastChar == '۔' || lastChar == '?' || lastChar == '؟' || lastChar == '!') {
            return text
        }

        return if (lang.startsWith("ur")) {
            val endWords = listOf("ہے", "ہیں", "تھا", "تھے", "تھی", "تھیں", "گا", "گی", "گے", "ہوں", "ہو")
            val words = text.split("\\s+".toRegex())
            if (words.isNotEmpty() && endWords.contains(words.last())) {
                "$text۔"
            } else {
                text
            }
        } else {
            val capitalized = text.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
            "$capitalized."
        }
    }
}
