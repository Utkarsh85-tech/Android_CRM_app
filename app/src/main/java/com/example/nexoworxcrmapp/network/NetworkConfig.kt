package com.example.nexoworxcrmapp.network

import com.example.nexoworxcrmapp.BuildConfig

object NetworkConfig {
    val instanceUrl: String = BuildConfig.SF_INSTANCE_URL
    val clientId: String = BuildConfig.SF_CLIENT_ID
    val clientSecret: String = BuildConfig.SF_CLIENT_SECRET
    const val GRANT_TYPE = "client_credentials"
    const val API_VERSION = "v66.0"
}
