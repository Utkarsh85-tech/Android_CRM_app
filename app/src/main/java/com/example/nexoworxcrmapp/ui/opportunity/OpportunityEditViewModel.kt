// ─────────────────────────────────────────────────────────────────────────────
// NEW FILE
// app/src/main/java/com/example/nexoworxcrmapp/ui/opportunity/OpportunityEditViewModel.kt
// ─────────────────────────────────────────────────────────────────────────────

package com.example.nexoworxcrmapp.ui.opportunity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexoworxcrmapp.data.CrmRepository
import com.example.nexoworxcrmapp.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OpportunityEditUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val name: String = "",
    val stageName: String = "Prospecting",
    val closeDate: String = "",
    val amount: String = "",
    val accountId: String = "",
    val description: String = "",
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false,
    val type: String = "",
    val leadSource: String = "",
    val deliveryInstallationStatus: String = "",
)

val OPPORTUNITY_STAGES = listOf(
    "Prospecting",
    "Qualification",
    "Needs Analysis",
    "Value Proposition",
    "Id. Decision Makers",
    "Perception Analysis",
    "Proposal/Price Quote",
    "Negotiation/Review",
    "Closed Won",
    "Closed Lost",
)

class OpportunityEditViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    // opportunityId is null when creating a new opportunity
    private val opportunityId: String? = savedStateHandle.get<String>("opportunityId")
        ?.takeIf { it != "new" }

    private val _uiState = MutableStateFlow(OpportunityEditUiState())
    val uiState: StateFlow<OpportunityEditUiState> = _uiState.asStateFlow()

    init {
        if (opportunityId != null) loadExisting()
    }

    private fun loadExisting() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = CrmRepository.readOpportunityDetail(opportunityId!!)) {
                is ApiResult.Success -> {
                    val o = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = o.name,
                            stageName = o.stageName,
                            closeDate = o.closeDate,
                            amount = o.amount?.toBigDecimal()?.toPlainString() ?: "",
                            accountId = o.accountId,
                            description = o.description,
                            type = o.type,
                            leadSource = o.leadSource,
                            deliveryInstallationStatus = o.deliveryInstallationStatus,
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun onNameChange(v: String) = _uiState.update { it.copy(name = v) }
    fun onStageChange(v: String) = _uiState.update { it.copy(stageName = v) }
    fun onCloseDateChange(v: String) = _uiState.update { it.copy(closeDate = v) }
    fun onAmountChange(v: String) = _uiState.update { it.copy(amount = v) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(description = v) }

    fun onTypeChange(v: String) = _uiState.update { it.copy(type = v) }
    fun onLeadSourceChange(v: String) = _uiState.update { it.copy(leadSource = v) }
    fun onDeliveryStatusChange(v: String) = _uiState.update { it.copy(deliveryInstallationStatus = v) }
    fun save() {
        val s = _uiState.value
        if (s.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Deal name is required") }
            return
        }
        if (s.closeDate.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Close date is required (YYYY-MM-DD)") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val result = if (opportunityId == null) {
                CrmRepository.createOpportunity(
                    name = s.name,
                    stageName = s.stageName,
                    closeDate = s.closeDate,
                    amount = s.amount.toDoubleOrNull(),
                    accountId = s.accountId.takeIf { it.isNotBlank() },
                    description = s.description.takeIf { it.isNotBlank() },
                    type = s.type.takeIf { it.isNotBlank() },
                    leadSource = s.leadSource.takeIf { it.isNotBlank() },
                    deliveryInstallationStatus = s.deliveryInstallationStatus.takeIf { it.isNotBlank() },
                )

            } else {
                CrmRepository.updateOpportunity(
                    id = opportunityId,
                    name = s.name,
                    stageName = s.stageName,
                    closeDate = s.closeDate,
                    amount = s.amount.toDoubleOrNull(),
                    accountId = s.accountId.takeIf { it.isNotBlank() },
                    description = s.description.takeIf { it.isNotBlank() },
                    type = s.type.takeIf { it.isNotBlank() },
                    leadSource = s.leadSource.takeIf { it.isNotBlank() },
                    deliveryInstallationStatus = s.deliveryInstallationStatus.takeIf { it.isNotBlank() },
                    )
            }
            when (result) {
                is ApiResult.Success -> _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                is ApiResult.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
            }
        }
    }
}
