// Step 5 of 15
// NEW FILE: app/src/main/java/com/example/nexoworxcrmapp/data/contact/ContactRepository.kt

package com.example.nexoworxcrmapp.data.contact

import com.example.nexoworxcrmapp.data.Contact
import com.example.nexoworxcrmapp.data.contact.network.ContactApiService
import com.example.nexoworxcrmapp.data.contact.network.SalesforceContactCreateRequest
import com.example.nexoworxcrmapp.data.contact.network.toDomain
import com.example.nexoworxcrmapp.network.ApiResult
import com.example.nexoworxcrmapp.network.safeApiCall
import com.example.nexoworxcrmapp.network.safeApiCallEmpty

class ContactRepository(
    private val api: ContactApiService,
) {
    suspend fun readAllContacts(): ApiResult<List<Contact>> {
        return when (val result = safeApiCall { api.queryContacts() }) {
            is ApiResult.Success -> ApiResult.Success(result.data.records.map { it.toDomain() })
            is ApiResult.Error -> result
        }
    }

    suspend fun readOneContact(id: String): ApiResult<Contact> {
        return when (val result = safeApiCall { api.getContact(id) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> result
        }
    }

    suspend fun createContact(request: SalesforceContactCreateRequest): ApiResult<Contact> {
        return when (val result = safeApiCall { api.createContact(request) }) {
            is ApiResult.Success -> {
                if (!result.data.success) {
                    val msg = result.data.errors?.firstOrNull()?.message ?: "Create failed"
                    ApiResult.Error(message = msg)
                } else {
                    readOneContact(result.data.id)
                }
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun updateContact(id: String, request: SalesforceContactCreateRequest): ApiResult<Contact> {
        return when (val result = safeApiCallEmpty { api.updateContact(id, request) }) {
            is ApiResult.Success -> readOneContact(id)
            is ApiResult.Error -> result
        }
    }

    suspend fun deleteContact(id: String): ApiResult<Unit> {
        return safeApiCallEmpty { api.deleteContact(id) }
    }
}
