package com.example.nexoworxcrmapp.network.auth

import com.example.nexoworxcrmapp.network.ApiResult
import com.example.nexoworxcrmapp.network.NetworkConfig
import com.example.nexoworxcrmapp.network.safeApiCall

class SalesforceAuthManager(
    private val authApi: AuthApiService,
) {
    @Volatile
    private var cachedToken: String? = null

    suspend fun getAccessToken(): String {
        cachedToken?.let { return it }
        return refreshToken().also { cachedToken = it }
    }

    suspend fun refreshToken(): String {
        when (val result = safeApiCall {
            authApi.getToken(
                grantType = NetworkConfig.GRANT_TYPE,
                clientId = NetworkConfig.clientId,
                clientSecret = NetworkConfig.clientSecret,
            )
        }) {
            is ApiResult.Success -> {
                cachedToken = result.data.accessToken
                return result.data.accessToken
            }
            is ApiResult.Error -> throw IllegalStateException(result.message)
        }
    }

    fun invalidate() {
        cachedToken = null
    }
}
