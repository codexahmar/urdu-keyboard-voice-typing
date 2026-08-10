package com.codexahmar.urdu_keyboard.keyboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.*

/**
 * Manages voice-to-text input using Android SpeechRecognizer.
 * Supports continuous dictation and automatic punctuation.
 */
class VoiceInputManager(
    private val context: Context,
    private val keyboardService: UrduKeyboardService
) : RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var shouldRestart = false
    private var currentLanguageCode = "en-US"
    private val mainHandler = Handler(Looper.getMainLooper())

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
     * Start listening for voice input
     * @param languageCode Language code (e.g., "en-US", "ur-PK")
     */
    fun startListening(languageCode: String = "en-US") {
        currentLanguageCode = languageCode
        if (shouldRestart) {
            stopListening()
            return
        }

        shouldRestart = true
        startListeningInternal()
    }

    private fun startListeningInternal() {
        if (speechRecognizer == null) {
            initializeRecognizer()
        }
        if (speechRecognizer == null) {
            Toast.makeText(context, "Speech recognition not available", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguageCode)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            // Dictation mode request
            putExtra("android.speech.extra.DICTATION_MODE", true)
        }

        try {
            speechRecognizer?.startListening(intent)
            isListening = true
            Toast.makeText(context, "🎤 Listening (${getLangDisplayName(currentLanguageCode)})...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("VoiceInputManager", "Error starting listening", e)
            isListening = false
            shouldRestart = false
        }
    }

    /**
     * Stop listening
     */
    fun stopListening() {
        shouldRestart = false
        speechRecognizer?.stopListening()
        speechRecognizer?.cancel()
        isListening = false
        Toast.makeText(context, "🎤 Microphone Off", Toast.LENGTH_SHORT).show()
    }

    /**
     * Cleanup resources
     */
    fun cleanup() {
        shouldRestart = false
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private fun getLangDisplayName(code: String): String {
        return if (code.startsWith("ur")) "Urdu" else "English"
    }

    // RecognitionListener callbacks

    override fun onReadyForSpeech(params: Bundle?) {
        isListening = true
    }

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(rmsdB: Float) {}

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        isListening = false
    }

    override fun onError(error: Int) {
        isListening = false
        Log.d("VoiceInputManager", "onError: $error")

        // Fatal permissions or Client errors should stop listening loop
        val isFatal = error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ||
                      error == SpeechRecognizer.ERROR_CLIENT

        if (shouldRestart && !isFatal) {
            // Restart after a small delay to let speech recognition release resources
            mainHandler.postDelayed({
                if (shouldRestart) {
                    startListeningInternal()
                }
            }, 300)
        } else {
            shouldRestart = false
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                else -> "Unknown error"
            }
            if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                Toast.makeText(context, "Voice input: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResults(results: Bundle?) {
        isListening = false
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            var recognizedText = matches[0].trim()
            if (recognizedText.isNotEmpty()) {
                recognizedText = formatTextWithPunctuation(recognizedText, currentLanguageCode)
                keyboardService.insertVoiceText(recognizedText + " ")
            }
        }

        if (shouldRestart) {
            mainHandler.postDelayed({
                if (shouldRestart) {
                    startListeningInternal()
                }
            }, 300)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {}

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
            val capitalized = text.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            "$capitalized."
        }
    }
}
