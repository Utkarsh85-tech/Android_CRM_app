package com.example.nexoworxcrmapp.data.lead.network

import com.google.gson.annotations.SerializedName

/** SOQL query wrapper — GET /services/data/v66.0/query/ */
data class SalesforceQueryResponse(
    @SerializedName("totalSize") val totalSize: Int = 0,
    @SerializedName("done") val done: Boolean = true,
    @SerializedName("records") val records: List<SalesforceLeadDto> = emptyList(),
)

data class SalesforceAttributes(
    @SerializedName("type") val type: String? = null,
    @SerializedName("url") val url: String? = null,
)

/** Lead record from Salesforce REST API */
data class SalesforceLeadDto(
    @SerializedName("attributes") val attributes: SalesforceAttributes? = null,
    @SerializedName("Id") val id: String? = null,
    @SerializedName("Salutation") val salutation: String? = null,
    @SerializedName("FirstName") val firstName: String? = null,
    @SerializedName("LastName") val lastName: String? = null,
    @SerializedName("Company") val company: String? = null,
    @SerializedName("Status") val status: String? = null,
    @SerializedName("Phone") val phone: String? = null,
    @SerializedName("MobilePhone") val mobilePhone: String? = null,
    @SerializedName("Email") val email: String? = null,
    @SerializedName("LeadSource") val leadSource: String? = null,
    @SerializedName("Rating") val rating: String? = null,
    @SerializedName("Industry") val industry: String? = null,
    @SerializedName("Title") val title: String? = null,
    @SerializedName("Website") val website: String? = null,
    @SerializedName("Description") val description: String? = null,
)

/** POST /services/data/v66.0/sobjects/Lead/ response */
data class SalesforceCreateResponse(
    @SerializedName("id") val id: String,
    @SerializedName("success") val success: Boolean,
    @SerializedName("errors") val errors: List<SalesforceError>? = null,
)

data class SalesforceError(
    @SerializedName("message") val message: String? = null,
    @SerializedName("statusCode") val statusCode: String? = null,
)

/** Create Lead body — matches Postman "Create Lead" */
data class SalesforceLeadCreateRequest(
    @SerializedName("Salutation") val salutation: String? = null,
    @SerializedName("FirstName") val firstName: String,
    @SerializedName("LastName") val lastName: String,
    @SerializedName("Company") val company: String,
    @SerializedName("MobilePhone") val mobilePhone: String? = null,
    @SerializedName("Phone") val phone: String? = null,
    @SerializedName("Title") val title: String? = null,
    @SerializedName("Email") val email: String? = null,
    @SerializedName("LeadSource") val leadSource: String? = null,
    @SerializedName("Website") val website: String? = null,
    @SerializedName("Industry") val industry: String? = null,
    @SerializedName("Status") val status: String? = null,
    @SerializedName("Rating") val rating: String? = null,
    @SerializedName("Description") val description: String? = null,
)

/** PATCH body — only send fields being updated */
data class SalesforceLeadPatchRequest(
    @SerializedName("FirstName") val firstName: String? = null,
    @SerializedName("LastName") val lastName: String? = null,
    @SerializedName("Company") val company: String? = null,
    @SerializedName("MobilePhone") val mobilePhone: String? = null,
    @SerializedName("Phone") val phone: String? = null,
    @SerializedName("Title") val title: String? = null,
    @SerializedName("Email") val email: String? = null,
    @SerializedName("LeadSource") val leadSource: String? = null,
    @SerializedName("Industry") val industry: String? = null,
    @SerializedName("Status") val status: String? = null,
    @SerializedName("Rating") val rating: String? = null,
    @SerializedName("Description") val description: String? = null,
)

const val LEADS_SOQL =
    "SELECT Id,FirstName,LastName,Company,Status,Phone,MobilePhone,Email,LeadSource,Rating,Industry,Title,Description FROM Lead"
/** Request body for POST /services/data/v66.0/actions/standard/convertLead */
//data class ConvertLeadRequest(
//    @SerializedName("inputs") val inputs: List<ConvertLeadInput>,
////)
//
//data class ConvertLeadInput(
//    @SerializedName("leadId") val leadId: String,
//    @SerializedName("convertedStatus") val convertedStatus: String = "Closed - Converted",
//    @SerializedName("doCreateOpportunity") val doCreateOpportunity: Boolean = false,
//)
///** Response item from POST /actions/standard/convertLead */
//data class ConvertLeadResult(
//    @SerializedName("accountId") val accountId: String? = null,
//    @SerializedName("contactId") val contactId: String? = null,
//    @SerializedName("leadId") val leadId: String? = null,
//    @SerializedName("success") val success: Boolean = false,
//    @SerializedName("errors") val errors: List<SalesforceError>? = null,
//)

data class ApexConvertRequest(
    @SerializedName("leadId") val leadId: String,
)

data class ApexConvertResult(
    @SerializedName("success") val success: Boolean,
    @SerializedName("accountId") val accountId: String? = null,
    @SerializedName("contactId") val contactId: String? = null,
    @SerializedName("error") val error: String? = null,
)