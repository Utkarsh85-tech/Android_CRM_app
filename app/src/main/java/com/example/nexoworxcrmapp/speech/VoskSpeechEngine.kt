package com.example.nexoworxcrmapp.speech

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

sealed class SpeechEngineState {
    data object Uninitialized : SpeechEngineState()
    data class Loading(val message: String) : SpeechEngineState()
    data object Ready : SpeechEngineState()
    data class Listening(val partialText: String) : SpeechEngineState()
    data class Final(val transcript: String) : SpeechEngineState()
    data class Error(val message: String) : SpeechEngineState()
}

/**
 * Offline speech-to-text using Vosk (Apache 2.0).
 */
class VoskSpeechEngine(context: Context) : SpeechEngine {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var model: Model? = null
    private var recognizer: Recognizer? = null
    private var recordJob: Job? = null
    private var audioRecord: AudioRecord? = null
    private val shouldStop = AtomicBoolean(false)
    private var modelReady = false

    private val _state = MutableStateFlow<SpeechEngineState>(SpeechEngineState.Uninitialized)
    override val state: StateFlow<SpeechEngineState> = _state.asStateFlow()
    override val engineLabel: String = "Vosk offline speech"
    override val autoStopListening: Boolean = true

    override fun hasRecordPermission(): Boolean =
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    override fun prepare() {
        if (_state.value is SpeechEngineState.Loading) return
        scope.launch {
            _state.value = SpeechEngineState.Loading("Preparing Vosk model…")
            val result = VoskModelManager.ensureModel(appContext) { msg ->
                _state.value = SpeechEngineState.Loading(msg)
            }
            result.fold(
                onSuccess = { dir -> initRecognizer(dir) },
                onFailure = { e ->
                    modelReady = false
                    _state.value = SpeechEngineState.Error(
                        e.message ?: "Failed to load Vosk model. Check internet for first download.",
                    )
                },
            )
        }
    }

    private suspend fun initRecognizer(modelDir: File) = withContext(Dispatchers.IO) {
        runCatching {
            recognizer?.close()
            model?.close()
            model = Model(modelDir.absolutePath)
            recognizer = Recognizer(model, SAMPLE_RATE.toFloat())
            modelReady = true
            _state.value = SpeechEngineState.Ready
        }.onFailure { e ->
            modelReady = false
            _state.value = SpeechEngineState.Error("Vosk init failed: ${e.message}")
        }
    }

    override fun startListening() {
        if (!hasRecordPermission()) {
            _state.value = SpeechEngineState.Error("Microphone permission required")
            return
        }
        if (!modelReady || recognizer == null) {
            _state.value = SpeechEngineState.Loading("Speech model still loading… wait, then try again")
            prepare()
            return
        }
        shouldStop.set(false)
        val rec = recognizer ?: return
        runCatching { rec.reset() }
        audioRecord?.run {
            try {
                if (recordingState == AudioRecord.RECORDSTATE_RECORDING) stop()
            } catch (_: Exception) {
            }
            release()
        }
        audioRecord = null
        recordJob?.cancel()
        recordJob = null

        _state.value = SpeechEngineState.Listening("")
        recordJob = scope.launch {
            runCatching { listenLoop(rec) }
                .onFailure { e ->
                    _state.value = SpeechEngineState.Error("Recording error: ${e.message}")
                }
        }
        scope.launch {
            delay(MAX_LISTEN_MS)
            if (!shouldStop.get()) {
                stopListening()
            }
        }
    }

    override fun stopListening() {
        if (recordJob == null && audioRecord == null) {
            shouldStop.set(false)
            return
        }
        if (!shouldStop.compareAndSet(false, true)) return
        scope.launch {
            recordJob?.join()
            audioRecord?.run {
                try {
                    if (recordingState == AudioRecord.RECORDSTATE_RECORDING) stop()
                } catch (_: Exception) {
                }
                release()
            }
            audioRecord = null
            recordJob = null
            val rec = recognizer
            if (rec == null) {
                _state.value = SpeechEngineState.Error("Recognizer not ready")
                return@launch
            }
            val text = parseVoskText(rec.finalResult)
            _state.value = if (text.isBlank()) {
                SpeechEngineState.Error(
                    if (DeviceUtils.isEmulator()) {
                        "No speech detected. Emulator mic often fails — use a physical device or tap a sample phrase."
                    } else {
                        "No speech detected. Speak closer to the mic and try again."
                    },
                )
            } else {
                SpeechEngineState.Final(text)
            }
        }
    }

    private fun stopListeningInternal() {
        recordJob?.cancel()
        recordJob = null
        audioRecord?.run {
            try {
                if (recordingState == AudioRecord.RECORDSTATE_RECORDING) stop()
            } catch (_: Exception) {
            }
            release()
        }
        audioRecord = null
    }

    private suspend fun listenLoop(recognizer: Recognizer) = withContext(Dispatchers.IO) {
        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            error("Invalid audio buffer size")
        }
        val audioSource = MediaRecorder.AudioSource.MIC
        val record = AudioRecord(
            audioSource,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize * 2,
        )
        if (record.state != AudioRecord.STATE_INITIALIZED) {
            record.release()
            error(
                "Microphone unavailable. On emulator: ⋮ → Extended Controls → Microphone → enable Virtual microphone.",
            )
        }
        audioRecord = record
        val buffer = ByteArray(bufferSize)
        record.startRecording()
        if (record.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
            record.release()
            error("Could not start recording. Check microphone permission and emulator mic settings.")
        }
        while (!shouldStop.get() && recordJob?.isActive == true) {
            val read = record.read(buffer, 0, buffer.size)
            if (read > 0) {
                recognizer.acceptWaveForm(buffer, read)
                val partial = parseVoskText(recognizer.partialResult)
                if (partial.isNotBlank()) {
                    _state.value = SpeechEngineState.Listening(partial)
                }
            }
        }
        try {
            record.stop()
        } catch (_: Exception) {
        }
        record.release()
        audioRecord = null
    }

    private fun parseVoskText(json: String): String {
        return runCatching {
            JSONObject(json).optString("text", "").trim()
        }.getOrDefault("")
    }

    override fun release() {
        shouldStop.set(true)
        stopListeningInternal()
        scope.cancel()
        recognizer?.close()
        recognizer = null
        model?.close()
        model = null
        modelReady = false
    }

    companion object {
        private const val SAMPLE_RATE = 16_000
        private const val MAX_LISTEN_MS = 12_000L
    }
}
