
package com.example.nexoworxcrmapp.data.contact.network

import com.google.gson.annotations.SerializedName

data class SalesforceContactQueryResponse(
    @SerializedName("totalSize") val totalSize: Int = 0,
    @SerializedName("done") val done: Boolean = true,
    @SerializedName("records") val records: List<SalesforceContactDto> = emptyList(),
)

data class SalesforceContactDto(
    @SerializedName("attributes") val attributes: SalesforceContactAttributes? = null,
    @SerializedName("Id") val id: String? = null,
    @SerializedName("FirstName") val firstName: String? = null,
    @SerializedName("LastName") val lastName: String? = null,
    @SerializedName("Title") val title: String? = null,
    @SerializedName("Phone") val phone: String? = null,
    @SerializedName("Email") val email: String? = null,
    @SerializedName("AccountId") val accountId: String? = null,
    @SerializedName("Account") val account: SalesforceContactAccountRef? = null,
    @SerializedName("Department") val department: String? = null,
    @SerializedName("Description") val description: String? = null,
)

data class SalesforceContactAttributes(
    @SerializedName("type") val type: String? = null,
    @SerializedName("url") val url: String? = null,
)

data class SalesforceContactAccountRef(
    @SerializedName("Name") val name: String? = null,
)

const val CONTACTS_SOQL =
    "SELECT Id,FirstName,LastName,Title,Phone,Email,AccountId,Account.Name,Department,Description FROM Contact ORDER BY LastName ASC"

data class SalesforceContactCreateRequest(
    @SerializedName("FirstName") val firstName: String? = null,
    @SerializedName("LastName") val lastName: String,
    @SerializedName("Title") val title: String? = null,
    @SerializedName("Phone") val phone: String? = null,
    @SerializedName("Email") val email: String? = null,
    @SerializedName("AccountId") val accountId: String? = null,
    @SerializedName("Department") val department: String? = null,
    @SerializedName("Description") val description: String? = null,
)

data class SalesforceContactCreateResponse(
    @SerializedName("id") val id: String,
    @SerializedName("success") val success: Boolean,
    @SerializedName("errors") val errors: List<SalesforceContactError>? = null,
)

data class SalesforceContactError(
    @SerializedName("message") val message: String? = null,
    @SerializedName("statusCode") val statusCode: String? = null,
)