package com.example.nexoworxcrmapp.network.auth

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val authManager: SalesforceAuthManager,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { authManager.getAccessToken() }
        var request = chain.request().newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        var response = chain.proceed(request)

        if (response.code == 401) {
            response.close()
            authManager.invalidate()
            val newToken = runBlocking { authManager.refreshToken() }
            request = chain.request().newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
            response = chain.proceed(request)
        }
        return response
    }
}
