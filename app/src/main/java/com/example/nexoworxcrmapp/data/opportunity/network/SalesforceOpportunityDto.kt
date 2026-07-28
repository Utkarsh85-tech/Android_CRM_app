// ─────────────────────────────────────────────────────────────────────────────
// NEW FILE
// app/src/main/java/com/example/nexoworxcrmapp/data/opportunity/network/SalesforceOpportunityDto.kt
// ─────────────────────────────────────────────────────────────────────────────

package com.example.nexoworxcrmapp.data.opportunity.network

import com.google.gson.annotations.SerializedName

/** SOQL query wrapper */
data class SalesforceOpportunityQueryResponse(
    @SerializedName("totalSize") val totalSize: Int = 0,
    @SerializedName("done") val done: Boolean = true,
    @SerializedName("records") val records: List<SalesforceOpportunityDto> = emptyList(),
)

/** Opportunity record from Salesforce REST API */
data class SalesforceOpportunityDto(
    @SerializedName("attributes") val attributes: SalesforceOpportunityAttributes? = null,
    @SerializedName("Id") val id: String? = null,
    @SerializedName("Name") val name: String? = null,
    @SerializedName("StageName") val stageName: String? = null,
    @SerializedName("CloseDate") val closeDate: String? = null,
    @SerializedName("Amount") val amount: Double? = null,
    @SerializedName("AccountId") val accountId: String? = null,
    @SerializedName("Account") val account: SalesforceOpportunityAccountRef? = null,
    @SerializedName("Description") val description: String? = null,
    @SerializedName("Probability") val probability: Double? = null,
    @SerializedName("Type") val type: String? = null,
    @SerializedName("LeadSource") val leadSource: String? = null,
    @SerializedName("ExpectedRevenue") val expectedRevenue: Double? = null,
    @SerializedName("DeliveryInstallationStatus__c") val deliveryInstallationStatus: String? = null,
)

data class SalesforceOpportunityAttributes(
    @SerializedName("type") val type: String? = null,
    @SerializedName("url") val url: String? = null,
)

/** Nested Account reference from relationship query */
data class SalesforceOpportunityAccountRef(
    @SerializedName("Name") val name: String? = null,
)

const val OPPORTUNITIES_SOQL =
    "SELECT Id,Name,StageName,CloseDate,Amount,AccountId,Account.Name,Description,Probability,Type,LeadSource,ExpectedRevenue,DeliveryInstallationStatus__c FROM Opportunity ORDER BY CloseDate ASC"

/** POST body for creating an Opportunity */
data class SalesforceOpportunityCreateRequest(
    @SerializedName("Name") val name: String,
    @SerializedName("StageName") val stageName: String,
    @SerializedName("CloseDate") val closeDate: String,
    @SerializedName("Amount") val amount: Double? = null,
    @SerializedName("AccountId") val accountId: String? = null,
    @SerializedName("Description") val description: String? = null,
    @SerializedName("Type") val type: String? = null,
    @SerializedName("LeadSource") val leadSource: String? = null,
    @SerializedName("DeliveryInstallationStatus__c") val deliveryInstallationStatus: String? = null,
)

/** POST/PATCH response */
data class SalesforceOpportunityCreateResponse(
    @SerializedName("id") val id: String,
    @SerializedName("success") val success: Boolean,
    @SerializedName("errors") val errors: List<SalesforceOpportunityError>? = null,
)

data class SalesforceOpportunityError(
    @SerializedName("message") val message: String? = null,
    @SerializedName("statusCode") val statusCode: String? = null,
)
