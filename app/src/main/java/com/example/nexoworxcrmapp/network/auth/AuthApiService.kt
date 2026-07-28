package com.example.nexoworxcrmapp.network.auth

import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Salesforce OAuth2 — matches Postman "Authentication" request.
 * POST {InstanceUrl}/services/oauth2/token?grant_type=client_credentials&client_id=...&client_secret=...
 */
interface AuthApiService {
    @POST("services/oauth2/token")
    suspend fun getToken(
        @Query("grant_type") grantType: String,
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String,
    ): Response<TokenResponse>
}
