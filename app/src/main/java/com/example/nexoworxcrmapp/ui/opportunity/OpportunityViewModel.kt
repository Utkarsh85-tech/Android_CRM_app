// ─────────────────────────────────────────────────────────────────────────────
// NEW FILE
// app/src/main/java/com/example/nexoworxcrmapp/ui/opportunity/OpportunityViewModel.kt
// ─────────────────────────────────────────────────────────────────────────────

package com.example.nexoworxcrmapp.ui.opportunity

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

data class OpportunityUiState(
    val isLoading: Boolean = false,
    val opportunities: List<Opportunity> = emptyList(),
    val errorMessage: String? = null,
)

class OpportunityViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OpportunityUiState())
    val uiState: StateFlow<OpportunityUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = CrmRepository.refreshOpportunities()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, opportunities = result.data, errorMessage = null)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        opportunities = CrmRepository.opportunities.value,
                        errorMessage = result.message,
                    )
                }
            }
        }
    }
}
