package com.example.nexoworxcrmapp.ui.lead

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexoworxcrmapp.data.CrmRepository
import com.example.nexoworxcrmapp.data.Lead
import com.example.nexoworxcrmapp.data.LeadEvent
import com.example.nexoworxcrmapp.data.LeadTask
import com.example.nexoworxcrmapp.data.SampleData
import com.example.nexoworxcrmapp.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LeadDetailTab { Info, Tasks }

data class LeadDetailUiState(
    val isLoading: Boolean = true,
    val lead: Lead? = null,
    val tasks: List<LeadTask> = emptyList(),
    val events: List<LeadEvent> = emptyList(),
    val selectedTab: LeadDetailTab = LeadDetailTab.Info,
    val errorMessage: String? = null,
    // Delete state
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val deleteErrorMessage: String? = null,
    // Convert state
    val isConverting: Boolean = false,
    val convertSuccess: Boolean = false,
    val convertErrorMessage: String? = null,
)

class LeadDetailViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val leadId: String = checkNotNull(savedStateHandle["leadId"])

    private val _uiState = MutableStateFlow(LeadDetailUiState())
    val uiState: StateFlow<LeadDetailUiState> = _uiState.asStateFlow()

    init {
        loadLead()
    }

    fun loadLead() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val lead = CrmRepository.readLeadDetail(leadId)
            if (lead != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        lead = lead,
                        tasks = SampleData.mockTasksForLead(lead),
                        events = SampleData.mockEventsForLead(lead),
                        errorMessage = null,
                    )
                }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Lead not found")
                }
            }
        }
    }

    fun selectTab(tab: LeadDetailTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun deleteLead() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, deleteErrorMessage = null) }
            CrmRepository.deleteLead(leadId)
            _uiState.update { it.copy(isDeleting = false, deleteSuccess = true) }
        }
    }

    fun consumeDeleteSuccess() = _uiState.update { it.copy(deleteSuccess = false) }
    fun clearDeleteError() = _uiState.update { it.copy(deleteErrorMessage = null) }

    /**
     * Converts this Lead into an Account (and optionally Contact + Opportunity)
     * using the Salesforce convert endpoint.
     * After success the lead no longer exists, so we navigate back to the list.
     */
    fun convertLead() {
        viewModelScope.launch {
            _uiState.update { it.copy(isConverting = true, convertErrorMessage = null) }
            when (val result = CrmRepository.convertLead(leadId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isConverting = false, convertSuccess = true) }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(isConverting = false, convertErrorMessage = result.message)
                    }
                }
            }
        }
    }

    fun consumeConvertSuccess() = _uiState.update { it.copy(convertSuccess = false) }
    fun clearConvertError() = _uiState.update { it.copy(convertErrorMessage = null) }
}