// Step 10 of 15
// NEW FILE: app/src/main/java/com/example/nexoworxcrmapp/ui/contact/ContactEditViewModel.kt

package com.example.nexoworxcrmapp.ui.contact

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

data class ContactEditUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val firstName: String = "",
    val lastName: String = "",
    val title: String = "",
    val phone: String = "",
    val email: String = "",
    val department: String = "",
    val description: String = "",
    val selectedAccountId: String = "",
    val selectedAccountName: String = "",
    val availableAccounts: List<Account> = emptyList(),
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false,
)

class ContactEditViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val contactId: String? = savedStateHandle.get<String>("contactId")
        ?.takeIf { it != "new" }

    private val _uiState = MutableStateFlow(ContactEditUiState())
    val uiState: StateFlow<ContactEditUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
        if (contactId != null) loadExisting()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            val cached = CrmRepository.accounts.value
            if (cached.isNotEmpty()) {
                _uiState.update { it.copy(availableAccounts = cached) }
            } else {
                CrmRepository.refreshAccounts()
                _uiState.update { it.copy(availableAccounts = CrmRepository.accounts.value) }
            }
        }
    }

    private fun loadExisting() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = CrmRepository.readContactDetail(contactId!!)) {
                is ApiResult.Success -> {
                    val c = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            firstName = c.firstName,
                            lastName = c.lastName,
                            title = c.title,
                            phone = c.phone,
                            email = c.email,
                            department = c.department,
                            description = c.description,
                            selectedAccountId = c.accountId,
                            selectedAccountName = c.accountName,
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun onFirstNameChange(v: String) = _uiState.update { it.copy(firstName = v) }
    fun onLastNameChange(v: String) = _uiState.update { it.copy(lastName = v) }
    fun onTitleChange(v: String) = _uiState.update { it.copy(title = v) }
    fun onPhoneChange(v: String) = _uiState.update { it.copy(phone = v) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v) }
    fun onDepartmentChange(v: String) = _uiState.update { it.copy(department = v) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(description = v) }
    fun onAccountSelected(account: Account) = _uiState.update {
        it.copy(selectedAccountId = account.id, selectedAccountName = account.name)
    }

    fun save() {
        val s = _uiState.value
        if (s.lastName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Last name is required") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val result = if (contactId == null) {
                CrmRepository.createContact(
                    firstName = s.firstName.takeIf { it.isNotBlank() },
                    lastName = s.lastName,
                    title = s.title.takeIf { it.isNotBlank() },
                    phone = s.phone.takeIf { it.isNotBlank() },
                    email = s.email.takeIf { it.isNotBlank() },
                    accountId = s.selectedAccountId.takeIf { it.isNotBlank() },
                    department = s.department.takeIf { it.isNotBlank() },
                    description = s.description.takeIf { it.isNotBlank() },
                )
            } else {
                CrmRepository.updateContact(
                    id = contactId,
                    firstName = s.firstName.takeIf { it.isNotBlank() },
                    lastName = s.lastName,
                    title = s.title.takeIf { it.isNotBlank() },
                    phone = s.phone.takeIf { it.isNotBlank() },
                    email = s.email.takeIf { it.isNotBlank() },
                    accountId = s.selectedAccountId.takeIf { it.isNotBlank() },
                    department = s.department.takeIf { it.isNotBlank() },
                    description = s.description.takeIf { it.isNotBlank() },
                )
            }
            when (result) {
                is ApiResult.Success -> _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                is ApiResult.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
            }
        }
    }
}
