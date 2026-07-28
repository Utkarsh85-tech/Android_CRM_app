package com.example.nexoworxcrmapp.data.opportunity

import com.example.nexoworxcrmapp.data.opportunity.network.AddLineItemRequest
import com.example.nexoworxcrmapp.data.opportunity.network.OpportunityApiService
import com.example.nexoworxcrmapp.data.opportunity.network.SalesforcePricebookEntryDto
import com.example.nexoworxcrmapp.data.opportunity.network.SalesforceLineItemDto
import com.example.nexoworxcrmapp.data.opportunity.network.SetPricebookRequest
import com.example.nexoworxcrmapp.data.opportunity.network.UpdateLineItemRequest
import com.example.nexoworxcrmapp.data.opportunity.network.lineItemsSoql
import com.example.nexoworxcrmapp.data.opportunity.network.opportunityPricebookSoql
import com.example.nexoworxcrmapp.data.opportunity.network.pricebookEntriesSoql
import com.example.nexoworxcrmapp.network.ApiResult
import com.example.nexoworxcrmapp.network.safeApiCall
import com.example.nexoworxcrmapp.network.safeApiCallEmpty

class ProductRepository(private val api: OpportunityApiService) {

    suspend fun fetchLineItems(opportunityId: String): ApiResult<List<SalesforceLineItemDto>> {
        return when (val r = safeApiCall { api.queryLineItems(lineItemsSoql(opportunityId)) }) {
            is ApiResult.Success -> ApiResult.Success(r.data.records)
            is ApiResult.Error -> r
        }
    }

    /** Returns pricebookId — sets Standard Pricebook if none assigned */
    suspend fun resolvePricebookId(opportunityId: String): ApiResult<String> {
        val oppResult = safeApiCall { api.queryOpportunityPricebook(opportunityPricebookSoql(opportunityId)) }
        if (oppResult is ApiResult.Error) return oppResult
        val existing = (oppResult as ApiResult.Success).data.records.firstOrNull()?.pricebookId
        if (!existing.isNullOrBlank()) return ApiResult.Success(existing)

        // No pricebook set — fetch and assign Standard
        val pbResult = safeApiCall { api.queryStandardPricebook() }
        if (pbResult is ApiResult.Error) return pbResult
        val pbId = (pbResult as ApiResult.Success).data.records.firstOrNull()?.id
            ?: return ApiResult.Error(message = "No standard pricebook found")
        val setResult = safeApiCallEmpty { api.setPricebook(opportunityId, SetPricebookRequest(pbId)) }
        if (setResult is ApiResult.Error) return setResult
        return ApiResult.Success(pbId)
    }

    suspend fun fetchPricebookEntries(pricebookId: String): ApiResult<List<SalesforcePricebookEntryDto>> {
        return when (val r = safeApiCall { api.queryPricebookEntries(pricebookEntriesSoql(pricebookId)) }) {
            is ApiResult.Success -> ApiResult.Success(r.data.records)
            is ApiResult.Error -> r
        }
    }

    suspend fun addLineItem(
        opportunityId: String,
        pricebookEntryId: String,
        quantity: Double,
        unitPrice: Double,
    ): ApiResult<Unit> {
        val body = AddLineItemRequest(opportunityId, pricebookEntryId, quantity, unitPrice)
        return when (val r = safeApiCall { api.addLineItem(body) }) {
            is ApiResult.Success -> if (r.data.success) ApiResult.Success(Unit)
            else ApiResult.Error(r.data.errors?.firstOrNull()?.message ?: "Add failed")
            is ApiResult.Error -> r
        }
    }

    suspend fun updateLineItem(id: String, quantity: Double, unitPrice: Double): ApiResult<Unit> {
        return safeApiCallEmpty { api.updateLineItem(id, UpdateLineItemRequest(quantity, unitPrice)) }
    }

    suspend fun deleteLineItem(id: String): ApiResult<Unit> {
        return safeApiCallEmpty { api.deleteLineItem(id) }
    }
}