package com.example.nexoworxcrmapp.data.account.network

import com.google.gson.annotations.SerializedName

// ── Query response wrapper ────────────────────────────────────────────────────

data class SalesforceAccountQueryResponse(
    @SerializedName("totalSize") val totalSize: Int = 0,
    @SerializedName("done") val done: Boolean = true,
    @SerializedName("records") val records: List<SalesforceAccountDto> = emptyList(),
)

// ── Account record from Salesforce ───────────────────────────────────────────

data class SalesforceAccountDto(
    @SerializedName("attributes") val attributes: SalesforceAccountAttributes? = null,
    @SerializedName("Id") val id: String? = null,
    @SerializedName("Name") val name: String? = null,
    @SerializedName("Phone") val phone: String? = null,
    @SerializedName("Industry") val industry: String? = null,
    @SerializedName("Type") val type: String? = null,
    @SerializedName("BillingCity") val billingCity: String? = null,
    @SerializedName("BillingCountry") val billingCountry: String? = null,
    @SerializedName("Website") val website: String? = null,
    @SerializedName("Description") val description: String? = null,
)

data class SalesforceAccountAttributes(
    @SerializedName("type") val type: String? = null,
    @SerializedName("url") val url: String? = null,
)

// ── Request body sent when CREATING a new Account ────────────────────────────

data class SalesforceAccountCreateRequest(
    @SerializedName("Name") val name: String,
    @SerializedName("Phone") val phone: String?,
    @SerializedName("Industry") val industry: String?,
    @SerializedName("Type") val type: String?,
    @SerializedName("BillingCity") val billingCity: String?,
    @SerializedName("BillingCountry") val billingCountry: String?,
    @SerializedName("Website") val website: String?,
    @SerializedName("Description") val description: String?,
)

// ── Request body sent when UPDATING (PATCH) an existing Account ───────────────
// Fields are nullable so we only send what changed

data class SalesforceAccountPatchRequest(
    @SerializedName("Name") val name: String,
    @SerializedName("Phone") val phone: String?,
    @SerializedName("Industry") val industry: String?,
    @SerializedName("Type") val type: String?,
    @SerializedName("BillingCity") val billingCity: String?,
    @SerializedName("BillingCountry") val billingCountry: String?,
    @SerializedName("Website") val website: String?,
    @SerializedName("Description") val description: String?,
)

// ── SOQL query ───────────────────────────────────────────────────────────────

const val ACCOUNTS_SOQL =
    "SELECT Id,Name,Phone,Industry,Type,BillingCity,BillingCountry,Website,Description FROM Account"
