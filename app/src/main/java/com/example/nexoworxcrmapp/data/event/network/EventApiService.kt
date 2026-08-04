// NEW FILE
// app/src/main/java/com/example/nexoworxcrmapp/data/event/network/EventApiService.kt

package com.example.nexoworxcrmapp.data.event.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface EventApiService {

    @GET("services/data/v66.0/query/")
    suspend fun queryEvents(
        @Query("q") soql: String,
    ): Response<SalesforceEventQueryResponse>

    @GET("services/data/v66.0/sobjects/Event/{eventId}")
    suspend fun getEvent(
        @Path("eventId") eventId: String,
    ): Response<SalesforceEventDto>

    @POST("services/data/v66.0/sobjects/Event/")
    suspend fun createEvent(
        @Body body: SalesforceEventCreateRequest,
    ): Response<SalesforceEventCreateResponse>

    @PATCH("services/data/v66.0/sobjects/Event/{eventId}")
    suspend fun updateEvent(
        @Path("eventId") eventId: String,
        @Body body: SalesforceEventCreateRequest,
    ): Response<Unit>

    @DELETE("services/data/v66.0/sobjects/Event/{eventId}")
    suspend fun deleteEvent(
        @Path("eventId") eventId: String,
    ): Response<Unit>
}