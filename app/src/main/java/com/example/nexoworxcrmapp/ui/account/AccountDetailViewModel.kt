package com.example.nexoworxcrmapp.ui.account

import androidx.lifecycle.SavedStateHandle
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

data class AccountDetailUiState(
    val isLoading: Boolean = true,
    val account: Account? = null,
    val errorMessage: String? = null,
    // Delete state
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val deleteErrorMessage: String? = null,
)

class AccountDetailViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val accountId: String = checkNotNull(savedStateHandle["accountId"])

    private val _uiState = MutableStateFlow(AccountDetailUiState())
    val uiState: StateFlow<AccountDetailUiState> = _uiState.asStateFlow()

    init {
        loadAccount()
    }

    fun loadAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = CrmRepository.readAccountDetail(accountId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, account = result.data) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    /** Permanently deletes this account from Salesforce */
    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, deleteErrorMessage = null) }
            when (val result = CrmRepository.deleteAccount(accountId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isDeleting = false, deleteSuccess = true) }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(isDeleting = false, deleteErrorMessage = result.message)
                    }
                }
            }
        }
    }

    fun consumeDeleteSuccess() = _uiState.update { it.copy(deleteSuccess = false) }
    fun clearDeleteError() = _uiState.update { it.copy(deleteErrorMessage = null) }
}
