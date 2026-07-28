// Step 9 of 15
// NEW FILE: app/src/main/java/com/example/nexoworxcrmapp/ui/contact/ContactDetailViewModel.kt

package com.example.nexoworxcrmapp.ui.contact

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexoworxcrmapp.data.Contact
import com.example.nexoworxcrmapp.data.CrmRepository
import com.example.nexoworxcrmapp.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class ContactDeleteState {
    object Idle : ContactDeleteState()
    object Confirming : ContactDeleteState()
    object Deleting : ContactDeleteState()
    object Deleted : ContactDeleteState()
    data class Error(val message: String) : ContactDeleteState()
}

data class ContactDetailUiState(
    val isLoading: Boolean = true,
    val contact: Contact? = null,
    val errorMessage: String? = null,
    val deleteState: ContactDeleteState = ContactDeleteState.Idle,
)

class ContactDetailViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val contactId: String = checkNotNull(savedStateHandle["contactId"])

    private val _uiState = MutableStateFlow(ContactDetailUiState())
    val uiState: StateFlow<ContactDetailUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = CrmRepository.readContactDetail(contactId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, contact = result.data, errorMessage = null)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun requestDelete() = _uiState.update { it.copy(deleteState = ContactDeleteState.Confirming) }
    fun cancelDelete() = _uiState.update { it.copy(deleteState = ContactDeleteState.Idle) }
    fun dismissDeleteError() = _uiState.update { it.copy(deleteState = ContactDeleteState.Idle) }

    fun confirmDelete() {
        viewModelScope.launch {
            _uiState.update { it.copy(deleteState = ContactDeleteState.Deleting) }
            when (val result = CrmRepository.deleteContact(contactId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(deleteState = ContactDeleteState.Deleted)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(deleteState = ContactDeleteState.Error(result.message ?: "Delete failed"))
                }
            }
        }
    }
}
