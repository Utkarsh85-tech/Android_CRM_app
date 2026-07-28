package com.example.nexoworxcrmapp.network.auth

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("instance_url") val instanceUrl: String? = null,
    @SerializedName("token_type") val tokenType: String? = null,
)
