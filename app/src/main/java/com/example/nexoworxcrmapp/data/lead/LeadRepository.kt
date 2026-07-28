package com.example.nexoworxcrmapp.data.lead

import com.example.nexoworxcrmapp.data.Lead
import com.example.nexoworxcrmapp.data.lead.network.LeadApiService
import com.example.nexoworxcrmapp.data.lead.network.SalesforceLeadCreateRequest
import com.example.nexoworxcrmapp.data.lead.network.SalesforceLeadPatchRequest
import com.example.nexoworxcrmapp.data.lead.network.toCreateRequest
import com.example.nexoworxcrmapp.data.lead.network.toDomain
import com.example.nexoworxcrmapp.data.lead.network.toPatchRequest
import com.example.nexoworxcrmapp.network.ApiResult
import com.example.nexoworxcrmapp.network.safeApiCall
import com.example.nexoworxcrmapp.network.safeApiCallEmpty
import com.example.nexoworxcrmapp.data.lead.network.ApexConvertRequest
class LeadRepository(
    private val api: LeadApiService,
) {
    suspend fun readAllLeads(): ApiResult<List<Lead>> {
        return when (val result = safeApiCall { api.queryLeads() }) {
            is ApiResult.Success -> ApiResult.Success(result.data.records.map { it.toDomain() })
            is ApiResult.Error -> result
        }
    }

    suspend fun readOneLead(id: String): ApiResult<Lead> {
        return when (val result = safeApiCall { api.getLead(id) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> result
        }
    }

    suspend fun createLead(request: SalesforceLeadCreateRequest): ApiResult<Lead> {
        return when (val result = safeApiCall { api.createLead(request) }) {
            is ApiResult.Success -> {
                if (!result.data.success) {
                    val errorMsg = result.data.errors?.firstOrNull()?.message ?: "Create lead failed"
                    ApiResult.Error(message = errorMsg)
                } else {
                    readOneLead(result.data.id)
                }
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun createLead(lead: Lead): ApiResult<Lead> = createLead(lead.toCreateRequest())

    suspend fun updateLead(id: String, request: SalesforceLeadPatchRequest): ApiResult<Lead> {
        return when (val result = safeApiCallEmpty { api.updateLead(id, request) }) {
            is ApiResult.Success -> readOneLead(id)
            is ApiResult.Error -> result
        }
    }

    suspend fun updateLead(id: String, lead: Lead): ApiResult<Lead> =
        updateLead(id, lead.toPatchRequest())

    /** Permanently delete a Lead from Salesforce */
    suspend fun deleteLead(id: String): ApiResult<Unit> {
        return safeApiCallEmpty { api.deleteLead(id) }
    }
    /** Converts a Lead into Account + Contact in Salesforce */
    suspend fun convertLead(id: String): ApiResult<Unit> {
        val body = ApexConvertRequest(leadId = id)
        return when (val result = safeApiCall { api.convertLead(body) }) {
            is ApiResult.Success -> {
                if (result.data.success) {
                    ApiResult.Success(Unit)
                } else {
                    ApiResult.Error(message = result.data.error ?: "Convert failed")
                }
            }
            is ApiResult.Error -> result
        }
    }
}
