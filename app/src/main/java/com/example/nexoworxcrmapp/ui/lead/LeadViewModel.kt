package com.example.nexoworxcrmapp.ui.lead

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexoworxcrmapp.data.CrmRepository
import com.example.nexoworxcrmapp.data.Lead
import com.example.nexoworxcrmapp.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LeadUiState(
    val isLoading: Boolean = false,
    val leads: List<Lead> = emptyList(),
    val errorMessage: String? = null,
)

class LeadViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LeadUiState())
    val uiState: StateFlow<LeadUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            CrmRepository.leads.collect { leadList ->
                _uiState.update { it.copy(leads = leadList) }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = CrmRepository.refreshLeads()) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }
}
