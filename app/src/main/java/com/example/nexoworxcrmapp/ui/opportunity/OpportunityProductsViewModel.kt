package com.example.nexoworxcrmapp.ui.opportunity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexoworxcrmapp.data.OpportunityLineItem
import com.example.nexoworxcrmapp.data.PricebookEntry
import com.example.nexoworxcrmapp.network.ApiResult
import com.example.nexoworxcrmapp.network.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductsUiState(
    val isLoading: Boolean = false,
    val lineItems: List<OpportunityLineItem> = emptyList(),
    val pricebookEntries: List<PricebookEntry> = emptyList(),
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val showAddSheet: Boolean = false,
    val editingItem: OpportunityLineItem? = null,
)

class OpportunityProductsViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val opportunityId: String = checkNotNull(savedStateHandle["opportunityId"])
    private val repo = NetworkModule.productRepository

    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState = _uiState.asStateFlow()

    private var pricebookId: String = ""

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            // Fetch line items
            when (val r = repo.fetchLineItems(opportunityId)) {
                is ApiResult.Success -> {
                    val items = r.data.map { dto ->
                        OpportunityLineItem(
                            id = dto.id.orEmpty(),
                            pricebookEntryId = dto.pricebookEntryId.orEmpty(),
                            productName = dto.product?.name ?: "Unknown Product",
                            quantity = dto.quantity ?: 1.0,
                            unitPrice = dto.unitPrice ?: 0.0,
                            totalPrice = dto.totalPrice ?: 0.0,
                        )
                    }
                    _uiState.update { it.copy(lineItems = items, isLoading = false) }
                }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = r.message) }
            }
        }
    }

    fun openAddSheet() {
        viewModelScope.launch {
            // Resolve pricebook and fetch entries
            if (pricebookId.isBlank()) {
                when (val r = repo.resolvePricebookId(opportunityId)) {
                    is ApiResult.Success -> pricebookId = r.data
                    is ApiResult.Error -> { _uiState.update { it.copy(saveError = r.message) }; return@launch }
                }
            }
            if (_uiState.value.pricebookEntries.isEmpty()) {
                when (val r = repo.fetchPricebookEntries(pricebookId)) {
                    is ApiResult.Success -> {
                        val entries = r.data.map { PricebookEntry(it.id.orEmpty(), it.name.orEmpty(), it.unitPrice ?: 0.0) }
                        _uiState.update { it.copy(pricebookEntries = entries) }
                    }
                    is ApiResult.Error -> { _uiState.update { it.copy(saveError = r.message) }; return@launch }
                }
            }
            _uiState.update { it.copy(showAddSheet = true, editingItem = null) }
        }
    }

    fun openEditSheet(item: OpportunityLineItem) {
        _uiState.update { it.copy(showAddSheet = true, editingItem = item) }
    }

    fun closeSheet() {
        _uiState.update { it.copy(showAddSheet = false, editingItem = null) }
    }

    fun addProduct(pricebookEntryId: String, quantity: Double, unitPrice: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            when (val r = repo.addLineItem(opportunityId, pricebookEntryId, quantity, unitPrice)) {
                is ApiResult.Success -> { closeSheet(); load() }
                is ApiResult.Error -> _uiState.update { it.copy(isSaving = false, saveError = r.message) }
            }
        }
    }

    fun updateProduct(lineItemId: String, quantity: Double, unitPrice: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            when (val r = repo.updateLineItem(lineItemId, quantity, unitPrice)) {
                is ApiResult.Success -> { closeSheet(); load() }
                is ApiResult.Error -> _uiState.update { it.copy(isSaving = false, saveError = r.message) }
            }
        }
    }

    fun deleteProduct(lineItemId: String) {
        viewModelScope.launch {
            when (val r = repo.deleteLineItem(lineItemId)) {
                is ApiResult.Success -> load()
                is ApiResult.Error -> _uiState.update { it.copy(saveError = r.message) }
            }
        }
    }

    fun clearSaveError() = _uiState.update { it.copy(saveError = null) }
}