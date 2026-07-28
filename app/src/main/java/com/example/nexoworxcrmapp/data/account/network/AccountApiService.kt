package com.example.nexoworxcrmapp.data.account.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AccountApiService {

    @GET("services/data/v66.0/query/")
    suspend fun queryAccounts(
        @Query("q") soql: String = ACCOUNTS_SOQL,
    ): Response<SalesforceAccountQueryResponse>

    @GET("services/data/v66.0/sobjects/Account/{accountId}")
    suspend fun getAccount(
        @Path("accountId") accountId: String,
    ): Response<SalesforceAccountDto>

    @POST("services/data/v66.0/sobjects/Account/")
    suspend fun createAccount(
        @Body body: SalesforceAccountCreateRequest,
    ): Response<SalesforceAccountCreateResponse>

    @PATCH("services/data/v66.0/sobjects/Account/{accountId}")
    suspend fun updateAccount(
        @Path("accountId") accountId: String,
        @Body body: SalesforceAccountPatchRequest,
    ): Response<Unit>

    /** Delete an Account permanently — Salesforce returns 204 No Content */
    @DELETE("services/data/v66.0/sobjects/Account/{accountId}")
    suspend fun deleteAccount(
        @Path("accountId") accountId: String,
    ): Response<Unit>
}

data class SalesforceAccountCreateResponse(
    @com.google.gson.annotations.SerializedName("id") val id: String,
    @com.google.gson.annotations.SerializedName("success") val success: Boolean,
    @com.google.gson.annotations.SerializedName("errors") val errors: List<String>? = null,
)
