package com.example.nexoworxcrmapp.ui.speaker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexoworxcrmapp.calendar.DeviceCalendarRepository
import com.example.nexoworxcrmapp.data.CrmRepository
import com.example.nexoworxcrmapp.network.ApiResult
import com.example.nexoworxcrmapp.speech.SpeechEngine
import com.example.nexoworxcrmapp.speech.SpeechEngineFactory
import com.example.nexoworxcrmapp.speech.SpeechEngineState
import com.example.nexoworxcrmapp.speech.LeadDraft
import com.example.nexoworxcrmapp.speech.VoiceCommandParser
import com.example.nexoworxcrmapp.speech.VoiceParseResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class VoiceUiPhase { Idle, Listening, Processing, Result, Error }

data class SpeakerUiState(
    val phase: VoiceUiPhase = VoiceUiPhase.Idle,
    val transcript: String = "",
    val parseResult: VoiceParseResult? = null,
    val statusMessage: String = "Tap mic to speak",
    val isListening: Boolean = false,
    val savedMessage: String? = null,
    val engineLabel: String = "",
    val micHint: String = "Tap mic, speak clearly, then wait",
)

class SpeakerViewModel(application: Application) : AndroidViewModel(application) {
    private val speechEngine: SpeechEngine = SpeechEngineFactory.create(application)
    private val parser = VoiceCommandParser()
    private val deviceCalendar = DeviceCalendarRepository(application)

    private val _uiState = MutableStateFlow(
        SpeakerUiState(
            engineLabel = speechEngine.engineLabel,
            micHint = "Tap mic, speak clearly, then wait",
        ),
    )
    val uiState: StateFlow<SpeakerUiState> = _uiState.asStateFlow()

    private var isRecording = false

    init {
        speechEngine.prepare()
        viewModelScope.launch {
            speechEngine.state.collect { engineState ->
                when (engineState) {
                    is SpeechEngineState.Uninitialized -> Unit
                    is SpeechEngineState.Loading -> {
                        _uiState.update {
                            it.copy(
                                phase = VoiceUiPhase.Idle,
                                statusMessage = engineState.message,
                                isListening = false,
                            )
                        }
                    }
                    is SpeechEngineState.Ready -> {
                        _uiState.update {
                            it.copy(
                                phase = VoiceUiPhase.Idle,
                                statusMessage = "Tap mic to speak",
                                engineLabel = speechEngine.engineLabel,
                                isListening = false,
                            )
                        }
                    }
                    is SpeechEngineState.Listening -> {
                        _uiState.update {
                            it.copy(
                                phase = VoiceUiPhase.Listening,
                                transcript = engineState.partialText,
                                statusMessage = "Listening… speak now",
                                isListening = true,
                            )
                        }
                    }
                    is SpeechEngineState.Final -> processTranscript(engineState.transcript)
                    is SpeechEngineState.Error -> {
                        _uiState.update {
                            it.copy(
                                phase = VoiceUiPhase.Error,
                                statusMessage = engineState.message,
                                isListening = false,
                            )
                        }
                        isRecording = false
                    }
                }
            }
        }
    }

    fun hasRecordPermission(): Boolean = speechEngine.hasRecordPermission()

    fun onPermissionDenied() {
        _uiState.update {
            it.copy(
                phase = VoiceUiPhase.Error,
                statusMessage = "Microphone permission is required. Enable it in Settings → Apps → NexoworxCRMApp.",
                isListening = false,
            )
        }
        isRecording = false
    }

    fun onMicClicked() {
        if (_uiState.value.phase == VoiceUiPhase.Processing) return
        if (!speechEngine.hasRecordPermission()) {
            onPermissionDenied()
            return
        }
        if (_uiState.value.statusMessage.contains("loading", ignoreCase = true)) {
            return
        }
        if (isRecording || _uiState.value.isListening) {
            isRecording = false
            _uiState.update {
                it.copy(phase = VoiceUiPhase.Processing, statusMessage = "Processing…", isListening = false)
            }
            speechEngine.stopListening()
            return
        }
        isRecording = true
        _uiState.update {
            it.copy(
                phase = VoiceUiPhase.Listening,
                transcript = "",
                parseResult = null,
                savedMessage = null,
                statusMessage = "Listening… speak now",
                isListening = true,
            )
        }
        speechEngine.startListening()
    }

    fun onSamplePhrase(phrase: String) {
        isRecording = false
        speechEngine.stopListening()
        _uiState.update {
            it.copy(
                phase = VoiceUiPhase.Processing,
                transcript = phrase,
                statusMessage = "Processing…",
                savedMessage = null,
                isListening = false,
            )
        }
        processTranscript(phrase)
    }

    private fun processTranscript(text: String) {
        val parsed = parser.parse(text)
        _uiState.update {
            it.copy(
                phase = VoiceUiPhase.Result,
                transcript = text,
                parseResult = parsed,
                statusMessage = when (parsed) {
                    is VoiceParseResult.Unknown -> parsed.reason
                    else -> "Review & confirm"
                },
                isListening = false,
            )
        }
        isRecording = false
    }

    fun getCreateLeadDraft(): LeadDraft? {
        val result = _uiState.value.parseResult
        return (result as? VoiceParseResult.CreateLead)?.draft
    }

    fun confirmCreate(onCalendarPermissionDenied: () -> Unit = {}) {
        val result = _uiState.value.parseResult ?: return
        viewModelScope.launch {
            when (result) {
                is VoiceParseResult.CreateLead -> Unit
                is VoiceParseResult.CreateEvent -> {
                    val missing = result.draft.missingRequired()
                    if (missing.isNotEmpty()) {
                        _uiState.update {
                            it.copy(
                                phase = VoiceUiPhase.Error,
                                statusMessage = "Missing: ${missing.joinToString()}",
                            )
                        }
                        return@launch
                    }
                    val deviceId = deviceCalendar.insertEvent(result.draft).getOrNull()
                    if (deviceId == null) {
                        onCalendarPermissionDenied()
                    }
                    CrmRepository.addEventFromVoice(result.draft, deviceId)
                    resetAfterSave(
                        if (deviceId != null) "Event saved to CRM & device calendar"
                        else "Event saved to CRM (grant calendar permission for device sync)",
                    )
                }
                is VoiceParseResult.Unknown -> Unit
            }
        }
    }

    private fun resetAfterSave(message: String) {
        _uiState.update {
            SpeakerUiState(
                phase = VoiceUiPhase.Idle,
                statusMessage = message,
                savedMessage = message,
                engineLabel = speechEngine.engineLabel,
            )
        }
    }

    fun tryAgain() {
        isRecording = false
        if (_uiState.value.isListening) {
            speechEngine.stopListening()
        }
        _uiState.update {
            SpeakerUiState(
                engineLabel = speechEngine.engineLabel,
                statusMessage = "Tap mic to speak",
            )
        }
    }

    override fun onCleared() {
        speechEngine.release()
        super.onCleared()
    }
}
