// ─────────────────────────────────────────────────────────────────────────────
// NEW FILE
// app/src/main/java/com/example/nexoworxcrmapp/data/opportunity/OpportunityRepository.kt
// ─────────────────────────────────────────────────────────────────────────────

package com.example.nexoworxcrmapp.data.opportunity

import com.example.nexoworxcrmapp.data.Opportunity
import com.example.nexoworxcrmapp.data.opportunity.network.OpportunityApiService
import com.example.nexoworxcrmapp.data.opportunity.network.SalesforceOpportunityCreateRequest
import com.example.nexoworxcrmapp.data.opportunity.network.toDomain
import com.example.nexoworxcrmapp.network.ApiResult
import com.example.nexoworxcrmapp.network.safeApiCall
import com.example.nexoworxcrmapp.network.safeApiCallEmpty


class OpportunityRepository(
    private val api: OpportunityApiService,
) {
    suspend fun readAllOpportunities(): ApiResult<List<Opportunity>> {
        return when (val result = safeApiCall { api.queryOpportunities() }) {
            is ApiResult.Success -> ApiResult.Success(result.data.records.map { it.toDomain() })
            is ApiResult.Error -> result
        }
    }

    suspend fun readOneOpportunity(id: String): ApiResult<Opportunity> {
        return when (val result = safeApiCall { api.getOpportunity(id) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> result
        }
    }

    suspend fun createOpportunity(request: SalesforceOpportunityCreateRequest): ApiResult<Opportunity> {
        return when (val result = safeApiCall { api.createOpportunity(request) }) {
            is ApiResult.Success -> {
                if (!result.data.success) {
                    val msg = result.data.errors?.firstOrNull()?.message ?: "Create failed"
                    ApiResult.Error(message = msg)
                } else {
                    readOneOpportunity(result.data.id)
                }
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun updateOpportunity(id: String, request: SalesforceOpportunityCreateRequest): ApiResult<Opportunity> {
        return when (val result = safeApiCallEmpty { api.updateOpportunity(id, request) }) {
            is ApiResult.Success -> readOneOpportunity(id)
            is ApiResult.Error -> result
        }
    }

    suspend fun deleteOpportunity(id: String): ApiResult<Unit> {
        return safeApiCallEmpty { api.deleteOpportunity(id) }
    }


}
