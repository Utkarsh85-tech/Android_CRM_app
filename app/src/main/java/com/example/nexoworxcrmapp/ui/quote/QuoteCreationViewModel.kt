package com.example.nexoworxcrmapp.ui.quote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexoworxcrmapp.network.ApiResult
import com.example.nexoworxcrmapp.network.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class QuoteCreationState {
    object Idle : QuoteCreationState()
    object Creating : QuoteCreationState()
    data class Success(val quoteId: String) : QuoteCreationState()
    data class Error(val message: String) : QuoteCreationState()
}

data class QuoteFormState(
    val quoteName: String = "",
    val accountName: String = "",
    val opportunityName: String = "",
    val startDate: String = "",
    val expirationDate: String = "",
    val term: String = "12",
    val endDate: String = "",
    val description: String = "",
)


class QuoteCreationViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val opportunityId: String = checkNotNull(savedStateHandle["opportunityId"])
    private val accountId: String = savedStateHandle["accountId"] ?: ""
    private val opportunityName: String = savedStateHandle["opportunityName"] ?: ""
    private val accountName: String = savedStateHandle["accountName"] ?: ""

    private val repo = NetworkModule.cpqRepository

    private val _state = MutableStateFlow<QuoteCreationState>(QuoteCreationState.Idle)
    val state = _state.asStateFlow()

    private val _formState = MutableStateFlow(
        QuoteFormState(
            quoteName = "",
            accountName = accountName,
            opportunityName = opportunityName,
            startDate = getToday(),
            expirationDate = computeEndDate(getToday(), "12"),
            endDate = computeEndDate(getToday(), "12"),
        )
    )
    val formState = _formState.asStateFlow()

    fun setQuoteName(v: String) = _formState.update { it.copy(quoteName = v) }
    fun setStartDate(v: String) = _formState.update {
        it.copy(
            startDate = v,
            expirationDate = computeEndDate(v, "12").ifBlank { it.expirationDate },
            endDate = computeEndDate(v, it.term),
        )
    }
    fun setTerm(v: String) {
        val digits = v.filter { it.isDigit() }
        _formState.update { it.copy(term = digits, endDate = computeEndDate(it.startDate, digits)) }
    }
    fun setExpirationDate(v: String) = _formState.update { it.copy(expirationDate = v) }
    fun setEndDate(v: String) = _formState.update { it.copy(endDate = v) }
    fun setDescription(v: String) = _formState.update { it.copy(description = v) }

    fun submitForm() {
        val form = _formState.value
        viewModelScope.launch {
            _state.update { QuoteCreationState.Creating }
            when (val r = repo.createQuote(
                accountId = accountId,
                opportunityId = opportunityId,
                name = form.quoteName,
                startDate = form.startDate,
                expirationDate = form.expirationDate,
                endDate = form.endDate,
                term = form.term.toIntOrNull(),
                description = form.description,
            )) {
                is ApiResult.Success -> _state.update { QuoteCreationState.Success(r.data) }
                is ApiResult.Error -> _state.update { QuoteCreationState.Error(r.message ?: "Failed to create quote") }
            }
        }
    }

    private fun getToday(): String {
        val cal = java.util.Calendar.getInstance()
        return "${cal.get(java.util.Calendar.YEAR)}-" +
                "${(cal.get(java.util.Calendar.MONTH)+1).toString().padStart(2,'0')}-" +
                "${cal.get(java.util.Calendar.DAY_OF_MONTH).toString().padStart(2,'0')}"
    }


    private fun computeEndDate(startDate: String, term: String): String {
        val months = term.toIntOrNull() ?: return ""
        if (startDate.isBlank() || months <= 0) return ""
        return try {
            val normalized = normalizeToIso(startDate) ?: return ""
            val parts = normalized.split("-")
            val cal = java.util.Calendar.getInstance()
            cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            cal.add(java.util.Calendar.MONTH, months)
            "${cal.get(java.util.Calendar.YEAR)}-" +
                    "${(cal.get(java.util.Calendar.MONTH)+1).toString().padStart(2,'0')}-" +
                    "${cal.get(java.util.Calendar.DAY_OF_MONTH).toString().padStart(2,'0')}"
        } catch (e: Exception) { "" }
    }

    private fun normalizeToIso(date: String): String? {
        val parts = date.split("-")
        if (parts.size != 3) return null
        return when {
            parts[0].length == 4 -> date
            parts[2].length == 4 -> "${parts[2]}-${parts[1].padStart(2,'0')}-${parts[0].padStart(2,'0')}"
            else -> null
        }
    }
}