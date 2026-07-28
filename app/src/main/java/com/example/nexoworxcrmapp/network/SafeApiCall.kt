package com.example.nexoworxcrmapp.network

import retrofit2.Response

suspend fun <T> safeApiCall(call: suspend () -> Response<T>): ApiResult<T> {
    return try {
        val response = call()
        val body = response.body()
        if (response.isSuccessful && body != null) {
            ApiResult.Success(body)
        } else {
            ApiResult.Error(
                message = response.errorBody()?.string().orEmpty().ifBlank { "Request failed" },
                code = response.code(),
            )
        }
    } catch (e: Exception) {
        ApiResult.Error(message = e.message ?: "Network error")
    }
}

/** For endpoints that return 204 No Content (e.g. Salesforce PATCH). */
suspend fun safeApiCallEmpty(call: suspend () -> Response<Unit>): ApiResult<Unit> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            ApiResult.Success(Unit)
        } else {
            ApiResult.Error(
                message = response.errorBody()?.string().orEmpty().ifBlank { "Request failed" },
                code = response.code(),
            )
        }
    } catch (e: Exception) {
        ApiResult.Error(message = e.message ?: "Network error")
    }
}
