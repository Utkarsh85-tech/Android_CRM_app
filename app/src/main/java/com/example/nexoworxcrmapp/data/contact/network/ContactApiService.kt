// Step 4 of 15
// NEW FILE: app/src/main/java/com/example/nexoworxcrmapp/data/contact/network/ContactApiService.kt

package com.example.nexoworxcrmapp.data.contact.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ContactApiService {

    @GET("services/data/v66.0/query/")
    suspend fun queryContacts(
        @Query("q") soql: String = CONTACTS_SOQL,
    ): Response<SalesforceContactQueryResponse>

    @GET("services/data/v66.0/sobjects/Contact/{contactId}")
    suspend fun getContact(
        @Path("contactId") contactId: String,
    ): Response<SalesforceContactDto>

    @POST("services/data/v66.0/sobjects/Contact/")
    suspend fun createContact(
        @Body body: SalesforceContactCreateRequest,
    ): Response<SalesforceContactCreateResponse>

    @PATCH("services/data/v66.0/sobjects/Contact/{contactId}")
    suspend fun updateContact(
        @Path("contactId") contactId: String,
        @Body body: SalesforceContactCreateRequest,
    ): Response<Unit>

    @DELETE("services/data/v66.0/sobjects/Contact/{contactId}")
    suspend fun deleteContact(
        @Path("contactId") contactId: String,
    ): Response<Unit>
}
