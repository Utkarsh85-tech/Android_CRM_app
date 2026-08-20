package com.example.nexoworxcrmapp.ui.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexoworxcrmapp.data.sync.SyncIssue
import com.example.nexoworxcrmapp.network.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SyncIssuesUiState(val issues: List<SyncIssue> = emptyList())

class SyncIssuesViewModel : ViewModel() {
    private val repo = NetworkModule.syncStatusRepository
    private val _uiState = MutableStateFlow(SyncIssuesUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observeAllIssues().collect { issues -> _uiState.update { it.copy(issues = issues) } }
        }
    }

    fun retry(opId: Long) = viewModelScope.launch { repo.retry(opId) }
    fun discard(opId: Long) = viewModelScope.launch { repo.discard(opId) }
}