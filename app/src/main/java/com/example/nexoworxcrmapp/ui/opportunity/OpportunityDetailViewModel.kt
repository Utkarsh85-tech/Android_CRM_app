// ─────────────────────────────────────────────────────────────────────────────
// REPLACE OpportunityDetailViewModel.kt entirely with this file
// app/src/main/java/com/example/nexoworxcrmapp/ui/opportunity/OpportunityDetailViewModel.kt
// ─────────────────────────────────────────────────────────────────────────────

package com.example.nexoworxcrmapp.ui.opportunity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexoworxcrmapp.data.CrmRepository
import com.example.nexoworxcrmapp.data.Opportunity
import com.example.nexoworxcrmapp.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class DeleteState {
    object Idle : DeleteState()
    object Confirming : DeleteState()   // show confirmation dialog
    object Deleting : DeleteState()     // API call in progress
    object Deleted : DeleteState()      // success — navigate away
    data class Error(val message: String) : DeleteState()
}

data class OpportunityDetailUiState(
    val isLoading: Boolean = true,
    val opportunity: Opportunity? = null,
    val errorMessage: String? = null,
    val deleteState: DeleteState = DeleteState.Idle,
)

class OpportunityDetailViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val opportunityId: String = checkNotNull(savedStateHandle["opportunityId"])

    private val _uiState = MutableStateFlow(OpportunityDetailUiState())
    val uiState: StateFlow<OpportunityDetailUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = CrmRepository.readOpportunityDetail(opportunityId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, opportunity = result.data, errorMessage = null)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    /** User tapped Delete — show confirmation dialog */
    fun requestDelete() {
        _uiState.update { it.copy(deleteState = DeleteState.Confirming) }
    }

    /** User cancelled the dialog */
    fun cancelDelete() {
        _uiState.update { it.copy(deleteState = DeleteState.Idle) }
    }

    /** User confirmed — call Salesforce API */
    fun confirmDelete() {
        viewModelScope.launch {
            _uiState.update { it.copy(deleteState = DeleteState.Deleting) }
            when (val result = CrmRepository.deleteOpportunity(opportunityId)) {
                is ApiResult.Success -> _uiState.update { it.copy(deleteState = DeleteState.Deleted) }
                is ApiResult.Error -> _uiState.update {
                    it.copy(deleteState = DeleteState.Error(result.message ?: "Delete failed"))
                }
            }
        }
    }

    fun dismissDeleteError() {
        _uiState.update { it.copy(deleteState = DeleteState.Idle) }
    }
}
