package com.codexahmar.urdu_keyboard.keyboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import java.util.*

/**
 * Manages voice-to-text input using Android SpeechRecognizer
 */
class VoiceInputManager(
    private val context: Context,
    private val keyboardService: UrduKeyboardService
) : RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    init {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(this)
        }
    }

    /**
     * Start listening for voice input
     * @param languageCode Language code (e.g., "en-US", "ur-PK", "ar-SA")
     */
    fun startListening(languageCode: String = "en-US") {
        if (speechRecognizer == null) {
            Toast.makeText(
                context,
                "Speech recognition not available",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (isListening) {
            stopListening()
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        try {
            speechRecognizer?.startListening(intent)
            isListening = true
            Toast.makeText(context, "🎤 Listening...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Error starting voice input: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            isListening = false
        }
    }

    /**
     * Stop listening
     */
    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
    }

    /**
     * Cleanup resources
     */
    fun cleanup() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    // RecognitionListener callbacks

    override fun onReadyForSpeech(params: Bundle?) {
        // Ready to listen
    }

    override fun onBeginningOfSpeech() {
        // User started speaking
    }

    override fun onRmsChanged(rmsdB: Float) {
        // Audio level changed (can be used for visual feedback)
    }

    override fun onBufferReceived(buffer: ByteArray?) {
        // Audio buffer received
    }

    override fun onEndOfSpeech() {
        isListening = false
    }

    override fun onError(error: Int) {
        isListening = false
        
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

        // Only show error if it's not a simple "no match" or timeout
        if (error != SpeechRecognizer.ERROR_NO_MATCH && 
            error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
            Toast.makeText(context, "Voice input error: $errorMessage", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResults(results: Bundle?) {
        isListening = false
        
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val recognizedText = matches[0]
            // Insert the recognized text into the input field
            keyboardService.insertVoiceText(recognizedText)
            Toast.makeText(context, "✓ Text inserted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        // Partial results available (optional: show real-time recognition)
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        // Reserved for future use
    }
}
