package com.example.nexoworxcrmapp.data.opportunity.network

import com.google.gson.annotations.SerializedName

// ── Query responses ───────────────────────────────────────────────────────────

data class SalesforceLineItemQueryResponse(
    @SerializedName("totalSize") val totalSize: Int = 0,
    @SerializedName("records") val records: List<SalesforceLineItemDto> = emptyList(),
)

data class SalesforceLineItemDto(
    @SerializedName("Id") val id: String? = null,
    @SerializedName("PricebookEntryId") val pricebookEntryId: String? = null,
    @SerializedName("Product2") val product: SalesforceProductRef? = null,
    @SerializedName("Quantity") val quantity: Double? = null,
    @SerializedName("UnitPrice") val unitPrice: Double? = null,
    @SerializedName("TotalPrice") val totalPrice: Double? = null,
    @SerializedName("ServiceDate") val serviceDate: String? = null,
    @SerializedName("Description") val description: String? = null,
)

data class SalesforceProductRef(
    @SerializedName("Name") val name: String? = null,
)

data class SalesforcePricebookQueryResponse(
    @SerializedName("totalSize") val totalSize: Int = 0,
    @SerializedName("records") val records: List<SalesforcePricebookEntryDto> = emptyList(),
)

data class SalesforcePricebookEntryDto(
    @SerializedName("Id") val id: String? = null,
    @SerializedName("Name") val name: String? = null,
    @SerializedName("UnitPrice") val unitPrice: Double? = null,
    @SerializedName("Product2Id") val productId: String? = null,
    @SerializedName("IsActive") val isActive: Boolean = true,
)

// ── SOQL queries ──────────────────────────────────────────────────────────────

fun lineItemsSoql(opportunityId: String) =
    "SELECT Id,PricebookEntryId,Product2.Name,Quantity,UnitPrice,TotalPrice,ServiceDate,Description FROM OpportunityLineItem WHERE OpportunityId='$opportunityId'"

fun pricebookEntriesSoql(pricebookId: String) =
    "SELECT Id,Name,UnitPrice,Product2Id,IsActive FROM PricebookEntry WHERE Pricebook2Id='$pricebookId' AND IsActive=true ORDER BY Name ASC"

const val STANDARD_PRICEBOOK_SOQL =
    "SELECT Id FROM Pricebook2 WHERE IsStandard=true LIMIT 1"

fun opportunityPricebookSoql(opportunityId: String) =
    "SELECT Pricebook2Id FROM Opportunity WHERE Id='$opportunityId' LIMIT 1"

// ── Request bodies ────────────────────────────────────────────────────────────

data class AddLineItemRequest(
    @SerializedName("OpportunityId") val opportunityId: String,
    @SerializedName("PricebookEntryId") val pricebookEntryId: String,
    @SerializedName("Quantity") val quantity: Double,
    @SerializedName("UnitPrice") val unitPrice: Double,
)

data class UpdateLineItemRequest(
    @SerializedName("Quantity") val quantity: Double,
    @SerializedName("UnitPrice") val unitPrice: Double,
)

data class SalesforceCreateResponse(
    @SerializedName("id") val id: String,
    @SerializedName("success") val success: Boolean,
    @SerializedName("errors") val errors: List<SalesforceProductError>? = null,
)

data class SalesforceProductError(
    @SerializedName("message") val message: String? = null,
)

data class SalesforcePricebookResponse(
    @SerializedName("totalSize") val totalSize: Int = 0,
    @SerializedName("records") val records: List<SalesforcePricebookIdDto> = emptyList(),
)

data class SalesforcePricebookIdDto(
    @SerializedName("Pricebook2Id") val pricebookId: String? = null,
)

data class SetPricebookRequest(
    @SerializedName("Pricebook2Id") val pricebookId: String,
)
data class SalesforceStandardPricebookResponse(
    @SerializedName("totalSize") val totalSize: Int = 0,
    @SerializedName("records") val records: List<SalesforceStandardPricebookDto> = emptyList(),
)

data class SalesforceStandardPricebookDto(
    @SerializedName("Id") val id: String? = null,
)