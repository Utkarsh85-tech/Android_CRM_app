package com.example.nexoworxcrmapp.speech

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Uses Android [SpeechRecognizer] (Google on-device/cloud depending on device).
 * Works reliably on emulators with a Google Play system image; best fallback for dev.
 */
class PlatformSpeechEngine(context: Context) : SpeechEngine {
    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())

    private var speechRecognizer: SpeechRecognizer? = null

    private val _state = MutableStateFlow<SpeechEngineState>(
        if (SpeechRecognizer.isRecognitionAvailable(appContext)) {
            SpeechEngineState.Ready
        } else {
            SpeechEngineState.Error("Speech recognition not available on this device")
        },
    )
    override val state: StateFlow<SpeechEngineState> = _state.asStateFlow()
    override val engineLabel: String = "Device speech recognition"
    override val autoStopListening: Boolean = true

    override fun hasRecordPermission(): Boolean =
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    override fun prepare() {
        if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
            _state.value = SpeechEngineState.Error(
                "Install a Google Play emulator image for speech, or use a physical device.",
            )
        } else {
            _state.value = SpeechEngineState.Ready
        }
    }

    override fun startListening() {
        if (!hasRecordPermission()) {
            _state.value = SpeechEngineState.Error("Microphone permission required")
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
            _state.value = SpeechEngineState.Error("Speech recognition not available")
            return
        }
        mainHandler.post {
            _state.value = SpeechEngineState.Listening("")
            speechRecognizer?.destroy()
            val recognizer = SpeechRecognizer.createSpeechRecognizer(appContext).also {
                speechRecognizer = it
                it.setRecognitionListener(createListener())
            }
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, appContext.packageName)
            }
            recognizer.startListening(intent)
        }
    }

    override fun stopListening() {
        mainHandler.post { speechRecognizer?.stopListening() }
    }

    override fun release() {
        mainHandler.post {
            speechRecognizer?.destroy()
            speechRecognizer = null
        }
    }

    private fun createListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _state.value = SpeechEngineState.Listening("")
        }

        override fun onBeginningOfSpeech() = Unit
        override fun onRmsChanged(rmsdB: Float) = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEndOfSpeech() {
            _state.value = SpeechEngineState.Listening("Processing…")
        }

        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO ->
                    "Microphone error. Emulator: Extended Controls → Microphone → Virtual mic."
                SpeechRecognizer.ERROR_CLIENT -> "Speech client error. Try again."
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                SpeechRecognizer.ERROR_NETWORK -> "Network required for device speech on this emulator"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech network timeout"
                SpeechRecognizer.ERROR_NO_MATCH ->
                    "No speech heard. Speak clearly or tap a sample phrase below."
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy. Wait and try again."
                SpeechRecognizer.ERROR_SERVER -> "Speech server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                    "No speech detected. Tap mic and speak within a few seconds."
                else -> "Speech error ($error)"
            }
            _state.value = SpeechEngineState.Error(message)
        }

        override fun onResults(results: Bundle?) {
            val text = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                ?.trim()
                .orEmpty()
            _state.value = if (text.isBlank()) {
                SpeechEngineState.Error("No speech detected. Try again.")
            } else {
                SpeechEngineState.Final(text)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partial = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                ?.trim()
                .orEmpty()
            if (partial.isNotBlank()) {
                _state.value = SpeechEngineState.Listening(partial)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }
}
