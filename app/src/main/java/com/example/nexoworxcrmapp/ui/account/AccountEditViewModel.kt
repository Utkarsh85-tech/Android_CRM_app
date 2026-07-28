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

data class AccountEditFormState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isCreateMode: Boolean = false,
    // Form fields — match the Account data class
    val name: String = "",
    val phone: String = "",
    val industry: String = "",
    val type: String = "",
    val billingCity: String = "",
    val billingCountry: String = "",
    val website: String = "",
    val description: String = "",
    // Validation
    val nameError: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false,
)

class AccountEditViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // accountId is null when we are creating, present when editing
    private val accountId: String? = savedStateHandle.get<String>("accountId")
    private val isCreateMode = accountId.isNullOrBlank()
    private var originalAccount: Account? = null

    private val _formState = MutableStateFlow(AccountEditFormState(isCreateMode = isCreateMode))
    val formState: StateFlow<AccountEditFormState> = _formState.asStateFlow()

    init {
        if (isCreateMode) {
            // Nothing to load — form starts empty
            _formState.update { it.copy(isLoading = false) }
        } else {
            loadAccount()
        }
    }

    private fun loadAccount() {
        val id = accountId ?: return
        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = CrmRepository.readAccountDetail(id)) {
                is ApiResult.Success -> {
                    originalAccount = result.data
                    val account = result.data
                    _formState.update {
                        it.copy(
                            isLoading = false,
                            name = account.name,
                            phone = account.phone,
                            industry = account.industry,
                            type = account.type,
                            billingCity = account.billingCity,
                            billingCountry = account.billingCountry,
                            website = account.website,
                            description = account.description,
                        )
                    }
                }
                is ApiResult.Error -> {
                    _formState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    // ── Field update functions ────────────────────────────────────────────────
    fun updateName(value: String) = _formState.update { it.copy(name = value, nameError = false) }
    fun updatePhone(value: String) = _formState.update { it.copy(phone = value) }
    fun updateIndustry(value: String) = _formState.update { it.copy(industry = value) }
    fun updateType(value: String) = _formState.update { it.copy(type = value) }
    fun updateBillingCity(value: String) = _formState.update { it.copy(billingCity = value) }
    fun updateBillingCountry(value: String) = _formState.update { it.copy(billingCountry = value) }
    fun updateWebsite(value: String) = _formState.update { it.copy(website = value) }
    fun updateDescription(value: String) = _formState.update { it.copy(description = value) }

    // ── Save ──────────────────────────────────────────────────────────────────
    fun save() {
        val state = _formState.value

        // Validate — Name is the only required field for an Account
        if (state.name.isBlank()) {
            _formState.update { it.copy(nameError = true, errorMessage = "Account name is required") }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true, errorMessage = null) }

            val accountData = Account(
                id = originalAccount?.id.orEmpty(),
                name = state.name.trim(),
                phone = state.phone.trim(),
                industry = state.industry.trim(),
                type = state.type.trim(),
                billingCity = state.billingCity.trim(),
                billingCountry = state.billingCountry.trim(),
                website = state.website.trim(),
                description = state.description.trim(),
            )

            val result = if (isCreateMode) {
                CrmRepository.createAccount(accountData)
            } else {
                CrmRepository.updateAccount(accountId!!, accountData)
            }

            when (result) {
                is ApiResult.Success -> {
                    originalAccount = result.data
                    _formState.update { it.copy(isSaving = false, saveSuccess = true) }
                }
                is ApiResult.Error -> {
                    _formState.update {
                        it.copy(isSaving = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun consumeSaveSuccess() {
        _formState.update { it.copy(saveSuccess = false) }
    }
}
