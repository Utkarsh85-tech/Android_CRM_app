// NEW FILE
// app/src/main/java/com/example/nexoworxcrmapp/data/event/EventRepository.kt

package com.example.nexoworxcrmapp.data.event

import com.example.nexoworxcrmapp.data.Event
import com.example.nexoworxcrmapp.data.event.network.EVENTS_SOQL
import com.example.nexoworxcrmapp.data.event.network.EventApiService
import com.example.nexoworxcrmapp.data.event.network.SalesforceEventCreateRequest
import com.example.nexoworxcrmapp.data.event.network.eventsByParentSoql
import com.example.nexoworxcrmapp.data.event.network.toDomain
import com.example.nexoworxcrmapp.network.ApiResult
import com.example.nexoworxcrmapp.network.safeApiCall
import com.example.nexoworxcrmapp.network.safeApiCallEmpty

class EventRepository(private val api: EventApiService) {

    // Get all events
    suspend fun readAllEvents(): ApiResult<List<Event>> {
        return when (val result = safeApiCall { api.queryEvents(EVENTS_SOQL) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.records.map { it.toDomain() })
            is ApiResult.Error -> result
        }
    }

    // Get events for a specific Lead/Account/Opportunity
    suspend fun readEventsForParent(parentId: String, isLead: Boolean = false): ApiResult<List<Event>> {
        return when (val result = safeApiCall { api.queryEvents(eventsByParentSoql(parentId, isLead)) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.records.map { it.toDomain() })
            is ApiResult.Error -> result
        }
    }

    suspend fun readOneEvent(id: String): ApiResult<Event> {
        return when (val result = safeApiCall { api.getEvent(id) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> result
        }
    }

    suspend fun createEvent(request: SalesforceEventCreateRequest): ApiResult<Event> {
        return when (val result = safeApiCall { api.createEvent(request) }) {
            is ApiResult.Success -> {
                if (!result.data.success) {
                    ApiResult.Error(message = result.data.errors?.firstOrNull()?.message ?: "Create failed")
                } else {
                    readOneEvent(result.data.id)
                }
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun updateEvent(id: String, request: SalesforceEventCreateRequest): ApiResult<Event> {
        return when (val result = safeApiCallEmpty { api.updateEvent(id, request) }) {
            is ApiResult.Success -> readOneEvent(id)
            is ApiResult.Error -> result
        }
    }

    suspend fun deleteEvent(id: String): ApiResult<Unit> {
        return safeApiCallEmpty { api.deleteEvent(id) }
    }
}