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
import kotlinx.coroutines.flow.combine
import com.example.nexoworxcrmapp.network.NetworkModule

data class LeadUiState(
    val isLoading: Boolean = false,
    val leads: List<Lead> = emptyList(),
    val pendingIds: Set<String> = emptySet(),
    val failedIds: Set<String> = emptySet(),
    val errorMessage: String? = null,
)

class LeadViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LeadUiState())
    val uiState: StateFlow<LeadUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                CrmRepository.leads,
                NetworkModule.syncStatusRepository.observePendingIds(),
                NetworkModule.syncStatusRepository.observeFailedIds(),
            ) { leads, pending, failed -> Triple(leads, pending, failed) }
                .collect { (leads, pending, failed) ->
                    _uiState.update { it.copy(leads = leads, pendingIds = pending, failedIds = failed) }
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