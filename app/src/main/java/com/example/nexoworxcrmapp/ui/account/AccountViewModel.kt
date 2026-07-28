package com.example.nexoworxcrmapp.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexoworxcrmapp.data.Account
import com.example.nexoworxcrmapp.data.CrmRepository
import com.example.nexoworxcrmapp.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountUiState(
    val isLoading: Boolean = false,
    val accounts: List<Account> = emptyList(),
    val errorMessage: String? = null,
)

class AccountViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        // Observe the shared accounts cache — when delete/create happen anywhere,
        // this list updates automatically without an extra network call.
        viewModelScope.launch {
            CrmRepository.accounts.collect { accounts ->
                _uiState.update { it.copy(accounts = accounts) }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = CrmRepository.refreshAccounts()) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = null)
                        // accounts are already updated via the StateFlow collector above
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }
}