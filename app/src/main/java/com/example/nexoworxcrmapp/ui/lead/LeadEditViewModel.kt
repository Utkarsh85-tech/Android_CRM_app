package com.example.nexoworxcrmapp.ui.lead

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexoworxcrmapp.data.CrmRepository
import com.example.nexoworxcrmapp.data.Lead
import com.example.nexoworxcrmapp.network.ApiResult
import com.example.nexoworxcrmapp.speech.LeadDraft
import com.example.nexoworxcrmapp.ui.components.displayLeadStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LeadEditFormState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isCreateMode: Boolean = false,
    val fromVoice: Boolean = false,
    val firstName: String = "",
    val lastName: String = "",
    val company: String = "",
    val email: String = "",
    val phone: String = "",
    val status: String = "New",
    val source: String = "",
    val rating: String = "",
    val description: String = "",
    val lastNameError: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false,
)

class LeadEditViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val leadId: String? = savedStateHandle.get<String>("leadId")
    private val isCreateMode = leadId.isNullOrBlank()
    private var originalLead: Lead? = null

    private val _formState = MutableStateFlow(LeadEditFormState(isCreateMode = isCreateMode))
    val formState: StateFlow<LeadEditFormState> = _formState.asStateFlow()

    init {
        if (isCreateMode) {
            _formState.update { it.copy(isLoading = false, status = "New") }
        } else {
            loadLead()
        }
    }

    fun applyVoiceDraft(draft: LeadDraft) {
        _formState.update {
            it.copy(
                fromVoice = true,
                firstName = draft.firstName,
                lastName = draft.lastName,
                company = draft.company,
                phone = draft.phone,
                email = draft.email,
                status = draft.status.ifBlank { "New" },
                source = draft.source,
                rating = draft.rating,
                description = draft.description,
                lastNameError = false,
                errorMessage = null,
            )
        }
    }

    fun loadLead() {
        val id = leadId ?: return
        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true, errorMessage = null) }
            val lead = CrmRepository.readLeadDetail(id)
            if (lead != null) {
                originalLead = lead
                _formState.update {
                    it.copy(
                        isLoading = false,
                        firstName = lead.firstName,
                        lastName = lead.lastName,
                        company = lead.company,
                        email = lead.email,
                        phone = lead.phone,
                        status = displayLeadStatus(lead.status),
                        source = lead.source,
                        rating = lead.rating,
                        description = lead.description,
                    )
                }
            } else {
                _formState.update {
                    it.copy(isLoading = false, errorMessage = "Lead not found")
                }
            }
        }
    }

    fun updateFirstName(value: String) = _formState.update { it.copy(firstName = value) }
    fun updateLastName(value: String) = _formState.update { it.copy(lastName = value, lastNameError = false) }
    fun updateCompany(value: String) = _formState.update { it.copy(company = value) }
    fun updateEmail(value: String) = _formState.update { it.copy(email = value) }
    fun updatePhone(value: String) = _formState.update { it.copy(phone = value) }
    fun updateStatus(value: String) = _formState.update { it.copy(status = value) }
    fun updateSource(value: String) = _formState.update { it.copy(source = value) }
    fun updateRating(value: String) = _formState.update { it.copy(rating = value) }
    fun updateDescription(value: String) = _formState.update { it.copy(description = value) }

    fun save() {
        val state = _formState.value
        val lastNameMissing = state.lastName.isBlank()
        val companyMissing = state.company.isBlank()
        if (lastNameMissing || companyMissing) {
            _formState.update {
                it.copy(
                    lastNameError = lastNameMissing,
                    errorMessage = when {
                        lastNameMissing && companyMissing -> "Last name and company are required"
                        lastNameMissing -> "Last name is required"
                        else -> "Company is required"
                    },
                )
            }
            return
        }
        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true, errorMessage = null, lastNameError = false) }
            val leadData = Lead(
                id = originalLead?.id.orEmpty(),
                firstName = state.firstName.trim(),
                lastName = state.lastName.trim(),
                company = state.company.trim(),
                email = state.email.trim(),
                phone = state.phone.trim(),
                status = state.status.trim(),
                source = state.source.trim(),
                rating = state.rating.trim(),
                description = state.description.trim(),
            )
            if (isCreateMode) {
                val created = CrmRepository.createLead(leadData)
                originalLead = created
            } else {
                CrmRepository.updateLead(leadId!!, leadData)
                originalLead = leadData.copy(id = leadId)
            }
            _formState.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }

    fun consumeSaveSuccess() {
        _formState.update { it.copy(saveSuccess = false) }
    }
}
