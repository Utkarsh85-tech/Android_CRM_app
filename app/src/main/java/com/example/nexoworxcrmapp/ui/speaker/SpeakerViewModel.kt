package com.example.nexoworxcrmapp.ui.speaker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexoworxcrmapp.speech.AccountDraft
import com.example.nexoworxcrmapp.speech.EventDraft
import com.example.nexoworxcrmapp.speech.LeadDraft
import com.example.nexoworxcrmapp.speech.OpportunityDraft
import com.example.nexoworxcrmapp.speech.SpeechEngine
import com.example.nexoworxcrmapp.speech.SpeechEngineFactory
import com.example.nexoworxcrmapp.speech.SpeechEngineState
import com.example.nexoworxcrmapp.speech.TaskDraft
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

/**
 * Every intent (Lead/Account/Opportunity/Task/Event) follows the same
 * pattern: parse the transcript into a draft, hand the draft to the real
 * create screen for that record type to pre-fill, and let that screen's own
 * "Save" button do the actual create. This ViewModel never talks to
 * CrmRepository directly — that keeps exactly one save path per record type,
 * instead of a second voice-only path that can drift out of sync with it.
 */
class SpeakerViewModel(application: Application) : AndroidViewModel(application) {
    private val speechEngine: SpeechEngine = SpeechEngineFactory.create(application)
    private val parser = VoiceCommandParser()

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

    fun getCreateLeadDraft(): LeadDraft? = (_uiState.value.parseResult as? VoiceParseResult.CreateLead)?.draft
    fun getCreateAccountDraft(): AccountDraft? = (_uiState.value.parseResult as? VoiceParseResult.CreateAccount)?.draft
    fun getCreateOpportunityDraft(): OpportunityDraft? = (_uiState.value.parseResult as? VoiceParseResult.CreateOpportunity)?.draft
    fun getCreateTaskDraft(): TaskDraft? = (_uiState.value.parseResult as? VoiceParseResult.CreateTask)?.draft
    fun getCreateEventDraft(): EventDraft? = (_uiState.value.parseResult as? VoiceParseResult.CreateEvent)?.draft

    // Called by the parent once it has handed the draft off to the right
    // create screen — just resets the sheet back to idle.
    fun onDraftHandedOff() {
        _uiState.update {
            SpeakerUiState(
                engineLabel = speechEngine.engineLabel,
                statusMessage = "Tap mic to speak",
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
