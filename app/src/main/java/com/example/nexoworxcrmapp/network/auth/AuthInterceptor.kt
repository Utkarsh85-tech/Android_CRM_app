package com.example.nexoworxcrmapp.network.auth

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AuthInterceptor(
    private val authManager: SalesforceAuthManager,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = try {
            runBlocking { authManager.getAccessToken() }
        } catch (e: Exception) {
            throw IOException("Unable to authenticate: ${e.message}", e)
        }

        var request = chain.request().newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        var response = chain.proceed(request)

        if (response.code == 401) {
            response.close()
            authManager.invalidate()
            val newToken = try {
                runBlocking { authManager.refreshToken() }
            } catch (e: Exception) {
                throw IOException("Unable to refresh token: ${e.message}", e)
            }
            request = chain.request().newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
            response = chain.proceed(request)
        }
        return response
    }
}