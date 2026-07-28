// Step 8 of 15
// NEW FILE: app/src/main/java/com/example/nexoworxcrmapp/ui/contact/ContactViewModel.kt

package com.example.nexoworxcrmapp.ui.contact

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

data class ContactUiState(
    val isLoading: Boolean = false,
    val contacts: List<Contact> = emptyList(),
    val errorMessage: String? = null,
)

class ContactViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ContactUiState())
    val uiState: StateFlow<ContactUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = CrmRepository.refreshContacts()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, contacts = result.data, errorMessage = null)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        contacts = CrmRepository.contacts.value,
                        errorMessage = result.message,
                    )
                }
            }
        }
    }
}
