// NEW FILE
// app/src/main/java/com/example/nexoworxcrmapp/ui/email/EmailViewModel.kt

package com.example.nexoworxcrmapp.ui.email

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexoworxcrmapp.data.CrmRepository
import com.example.nexoworxcrmapp.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Sender address — update this when client configures their org email
const val SENDER_EMAIL = "utkarsh.shivhare@nexoworx.com"

data class EmailUiState(
    val to: String = "",
    val subject: String = "",
    val body: String = "",
    val isSending: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
)

class EmailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EmailUiState())
    val uiState: StateFlow<EmailUiState> = _uiState.asStateFlow()

    // Called from LeadDetailScreen to prefill fields
    fun prefill(toEmail: String, leadName: String) {
        _uiState.update {
            it.copy(
                to = toEmail,
                subject = "Follow up — $leadName",
                body = "Hi $leadName,\n\nThank you for your interest. I wanted to follow up regarding our previous conversation.\n\nLooking forward to hearing from you.\n\nBest regards",
            )
        }
    }

    fun onToChange(v: String) = _uiState.update { it.copy(to = v) }
    fun onSubjectChange(v: String) = _uiState.update { it.copy(subject = v) }
    fun onBodyChange(v: String) = _uiState.update { it.copy(body = v) }
    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    fun send(leadId: String) {
        val s = _uiState.value
        if (s.to.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Recipient email is required") }
            return
        }
        if (s.subject.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Subject is required") }
            return
        }
        if (s.body.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email body cannot be empty") }
            return
        }
        if (!com.example.nexoworxcrmapp.network.NetworkModule.connectivityObserver.isCurrentlyOnline()) {
            _uiState.update { it.copy(errorMessage = "You're offline. Email requires an internet connection to send.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, errorMessage = null) }
            when (val result = CrmRepository.sendEmailToLead(
                toAddress = s.to,
                subject = s.subject,
                body = s.body,
                senderAddress = SENDER_EMAIL,
                leadId = leadId,
            )) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isSending = false, isSuccess = true)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSending = false, errorMessage = result.message)
                }
            }
        }
    }
}
