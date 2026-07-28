package com.example.nexoworxcrmapp.data.quote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*
import okhttp3.ResponseBody
import retrofit2.http.Streaming
import retrofit2.http.Url


// ── DTOs ─────────────────────────────────────────────────────────────────────

data class CpqAccount(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
)

data class CpqOpportunity(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("accountId") val accountId: String = "",
    @SerializedName("accountName") val accountName: String = "",
    @SerializedName("amount") val amount: Double? = null,
    @SerializedName("stageName") val stageName: String = "",
    @SerializedName("closeDate") val closeDate: String = "",
    @SerializedName("pricebookName") val pricebookName: String = "",
    @SerializedName("quoteCount") val quoteCount: Int = 0,
    @SerializedName("ownerName") val ownerName: String = "",
)

data class CpqQuote(
    @SerializedName(value = "id", alternate = ["Id"]) val id: String = "",
    @SerializedName(value = "name", alternate = ["Name"]) val name: String = "",
    @SerializedName(value = "status", alternate = ["Status"]) val status: String = "",
    @SerializedName(value = "quoteNumber", alternate = ["QuoteNumber"]) val quoteNumber: String = "",
    @SerializedName(value = "grandTotal", alternate = ["GrandTotal", "totalAmount"]) val grandTotal: Double = 0.0,    @SerializedName(value = "totalPrice", alternate = ["TotalPrice"]) val totalPrice: Double = 0.0,
    @SerializedName(value = "subtotal", alternate = ["Subtotal"]) val subtotal: Double = 0.0,
    @SerializedName(value = "tax", alternate = ["Tax"]) val tax: Double = 0.0,
    @SerializedName(value = "discount", alternate = ["Discount"]) val discount: Double = 0.0,
    @SerializedName(value = "opportunityId", alternate = ["OpportunityId"]) val opportunityId: String = "",
    @SerializedName(value = "accountId", alternate = ["AccountId"]) val accountId: String = "",
    @SerializedName(value = "expirationDate", alternate = ["ExpirationDate"]) val expirationDate: String = "",
    @SerializedName(value = "startDate", alternate = ["Start_Date__c"]) val startDate: String = "",
    @SerializedName(value = "endDate", alternate = ["End_Date__c"]) val endDate: String = "",
    @SerializedName(value = "term", alternate = ["Term__c"]) val term: Int? = null,
    @SerializedName(value = "description", alternate = ["Description"]) val description: String = "",
    @SerializedName("opportunityName") val opportunityName: String = "",
    @SerializedName("accountName") val accountName: String = "",
)
data class QuoteQueryResponse(
    @SerializedName("totalSize") val totalSize: Int = 0,
    @SerializedName("done") val done: Boolean = true,
    @SerializedName("records") val records: List<QuoteQueryRecord> = emptyList(),
)

data class QuoteQueryRecord(
    @SerializedName("Id") val id: String = "",
    @SerializedName("Name") val name: String = "",
    @SerializedName("Status") val status: String = "",
    @SerializedName("QuoteNumber") val quoteNumber: String = "",
    @SerializedName("GrandTotal") val grandTotal: Double? = null,
    @SerializedName("TotalPrice") val totalPrice: Double? = null,
    @SerializedName("Subtotal") val subtotal: Double? = null,
    @SerializedName("Tax") val tax: Double? = null,
    @SerializedName("Discount") val discount: Double? = null,
    @SerializedName("OpportunityId") val opportunityId: String = "",
    @SerializedName("AccountId") val accountId: String = "",
    @SerializedName("ExpirationDate") val expirationDate: String? = null,
    @SerializedName("Description") val description: String? = null,
    @SerializedName("Opportunity") val opportunity: QuoteRelatedName? = null,
    @SerializedName("Account") val account: QuoteRelatedName? = null,
    @SerializedName("Start_Date__c") val startDate: String? = null,
    @SerializedName("End_Date__c") val endDate: String? = null,
    @SerializedName("Term__c") val term: Double? = null,
) {
    fun toCpqQuote() = CpqQuote(
        id = id, name = name, status = status, quoteNumber = quoteNumber,
        grandTotal = grandTotal ?: 0.0, totalPrice = totalPrice ?: 0.0, subtotal = subtotal ?: 0.0,
        tax = tax ?: 0.0, discount = discount ?: 0.0, opportunityId = opportunityId, accountId = accountId,
        expirationDate = expirationDate.orEmpty(), startDate = startDate.orEmpty(),
        endDate = endDate.orEmpty(), term = term?.toInt(),
        description = description.orEmpty(),
        opportunityName = opportunity?.name.orEmpty(), accountName = account?.name.orEmpty(),
    )
}


data class QuoteRelatedName(
    @SerializedName("Name") val name: String? = null,
)
data class CpqCatalog(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
)

data class CpqCategory(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
)

data class CpqProduct(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName(value = "code", alternate = ["productCode"]) val productCode: String = "",
    @SerializedName("unitPrice") val price: Double = 0.0,
    @SerializedName("productType") val type: String = "Standalone",
    @SerializedName("family") val categoryName: String = "",
    @SerializedName("billingFrequency") val billingFrequency: String = "",
    @SerializedName("pricebookEntryId") val pricebookEntryId: String = "",
    @SerializedName("hsnCode") val hsnCode: String = "",
    @SerializedName("marginFloor") val marginFloor: Double = 0.0,
    @SerializedName("catalogName") val catalogName: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("configurationType") val configurationType: String = "",
    @SerializedName("isFeatured") val isFeatured: Boolean = false,
    @SerializedName("sortOrder") val sortOrder: Int = 0,
)

data class CpqSellingModel(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("pricingModel") val pricingModel: String = "",
)
data class ApprovalSubmitError(
    @SerializedName("message") val message: String? = null,
    @SerializedName("errorCode") val errorCode: String? = null,
)

data class CpqApprovalSubmitResult(
    @SerializedName("actorIds") val actorIds: List<String> = emptyList(),
    @SerializedName("entityId") val entityId: String = "",
    @SerializedName("errors") val errors: List<ApprovalSubmitError>? = null,
    @SerializedName("instanceId") val instanceId: String = "",
    @SerializedName("instanceStatus") val instanceStatus: String = "",
    @SerializedName("newWorkitemIds") val newWorkitemIds: List<String> = emptyList(),
    @SerializedName("success") val success: Boolean = false,
)
data class CpqAttribute(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("value") val value: String = "",
    @SerializedName("options") val options: List<CpqAttributeOption> = emptyList(),
)

data class CpqAttributeOption(
    @SerializedName("label") val label: String = "",
    @SerializedName("value") val value: String = "",
    @SerializedName("priceDelta") val priceDelta: Double = 0.0,
)

// REPLACE the CpqBundleComponent data class with:
data class CpqBundleComponent(
    @SerializedName("id") val id: String = "",
    @SerializedName("productId") val productId: String = "",
    @SerializedName("productName") val name: String = "",
    @SerializedName("productCode") val productCode: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("componentType") val relationshipType: String = "Component",
    @SerializedName("isRequired") val required: Boolean = false,
    @SerializedName("defaultQty") val quantity: Double = 1.0,
    @SerializedName("minQty") val minQty: Double = 0.0,
    @SerializedName("maxQty") val maxQty: Double = 1.0,
    @SerializedName("price") val price: Double = 0.0,
    @SerializedName("pricebookEntryId") val pricebookEntryId: String = "",
    )

data class CpqQuoteLine(
    @SerializedName("id") val id: String = "",
    @SerializedName("productId") val productId: String = "",
    @SerializedName("productName") val productName: String = "",
    @SerializedName("productCode") val productCode: String = "",
    @SerializedName("productType") val type: String = "Standalone",
    @SerializedName("quantity") val quantity: Double = 1.0,
    @SerializedName("salesPrice") val unitPrice: Double = 0.0,
    @SerializedName("listPrice") val listPrice: Double = 0.0,
    @SerializedName("netUnitPrice") val netUnitPrice: Double = 0.0,
    @SerializedName("netTotalPrice") val netTotalPrice: Double = 0.0,
    @SerializedName("totalPrice") val totalPrice: Double = 0.0,
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("discount") val discount: Double = 0.0,
    @SerializedName("uom") val uom: String = "",
    @SerializedName("isConfigured") val isConfigured: Boolean = false,
    @SerializedName("componentsTotal") val componentsTotal: Double = 0.0,
    @SerializedName("description") val description: String = "",
    @SerializedName("components") private val rawComponents: List<CpqLineComponentRaw>? = null,
) {
    val isComponent: Boolean get() = false // top-level lines from GET /cpq/lines are never components themselves
    val components: List<CpqLineComponent> get() = rawComponents?.map { it.toClean() } ?: emptyList()
}

data class CpqLineComponentRaw(
    @SerializedName("id") val id: String? = null,
    @SerializedName("productId") val productId: String? = null,
    @SerializedName("productName") val name: String? = null,
    @SerializedName("productCode") val productCode: String? = null,
    @SerializedName("quantity") val quantity: Double? = null,
    @SerializedName(value = "unitPrice", alternate = ["salesPrice"]) val unitPrice: Double? = null,
    @SerializedName("totalPrice") val totalPrice: Double? = null,
    @SerializedName("isIncludedInPrice") val isIncludedInPrice: Boolean? = null,
) {
    fun toClean() = CpqLineComponent(
        id = id.orEmpty(), productId = productId.orEmpty(), name = name.orEmpty(),
        productCode = productCode.orEmpty(), quantity = quantity ?: 1.0,
        unitPrice = unitPrice ?: 0.0, totalPrice = totalPrice ?: 0.0,
        isIncludedInPrice = isIncludedInPrice ?: false,
    )
}

data class CpqLineComponent(
    val id: String = "",
    val productId: String = "",
    val name: String = "",
    val productCode: String = "",
    val quantity: Double = 1.0,
    val unitPrice: Double = 0.0,
    val totalPrice: Double = 0.0,
    val isIncludedInPrice: Boolean = false,
)
data class CpqApprovalStep(
    @SerializedName("id") val id: String = "",
    @SerializedName("approverName") val approverName: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("comments") val comments: String = "",
)

data class CpqApprovalStatus(
    @SerializedName("status") val status: String = "",
    @SerializedName("steps") val steps: List<CpqApprovalStep> = emptyList(),
)

data class CpqPdfTemplate(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
)

// ── Request bodies ────────────────────────────────────────────────────────────

data class CreateQuoteBody(
    @SerializedName("accountId") val accountId: String,
    @SerializedName("opportunityId") val opportunityId: String,
    @SerializedName("input") val input: CreateQuoteInput,
)

data class CreateQuoteInput(
    @SerializedName("name") val name: String,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("expirationDate") val expirationDate: String,
    @SerializedName("endDate") val endDate: String,
    @SerializedName("description") val description: String = "",
)
data class StandardCreateQuoteBody(
    @SerializedName("Name") val name: String,
    @SerializedName("OpportunityId") val opportunityId: String,
    @SerializedName("Status") val status: String = "Draft",
    @SerializedName("ExpirationDate") val expirationDate: String? = null,
    @SerializedName("Description") val description: String? = null,
    @SerializedName("Start_Date__c") val startDate: String? = null,
    @SerializedName("End_Date__c") val endDate: String? = null,
    @SerializedName("Term__c") val term: Int? = null,
)

data class StandardCreateResponse(
    @SerializedName("id") val id: String? = null,
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("errors") val errors: List<StandardSalesforceError>? = null,
)

data class StandardSalesforceError(
    @SerializedName("message") val message: String? = null,
    @SerializedName("errorCode") val errorCode: String? = null,
)

data class SaveLinesBody(
    @SerializedName("quoteId") val quoteId: String,
    @SerializedName("lines") val lines: List<SaveLineItem>,
)

data class CpqBundleComponentLine(
    @SerializedName("bundleComponentId") val bundleComponentId: String,
    @SerializedName("componentProductId") val componentProductId: String,
    @SerializedName("componentPricebookEntryId") val componentPricebookEntryId: String = "",
    @SerializedName("quantity") val quantity: Double = 1.0,
    @SerializedName("unitPrice") val unitPrice: Double = 0.0,
    @SerializedName("isIncludedInPrice") val isIncludedInPrice: Boolean = false,
)
data class SaveLineItem(
    @SerializedName("productId") val productId: String,
    @SerializedName("quantity") val quantity: Double,
    @SerializedName("unitPrice") val unitPrice: Double,
    @SerializedName("term") val term: Int = 12,
    @SerializedName("sellingModelId") val sellingModelId: String = "",
    @SerializedName("startDate") val startDate: String = "",
    @SerializedName("attributes") val attributes: Map<String, String> = emptyMap(),
    @SerializedName("components") val components: List<CpqBundleComponentLine> = emptyList(),
)

data class EditLinesBody(
    @SerializedName("changes") val changes: List<EditLineChange>,
)

data class EditLineChange(
    @SerializedName("id") val id: String,
    @SerializedName("quantity") val quantity: Double,
    @SerializedName("unitPrice") val unitPrice: Double,
)

data class ApprovalRequestItem(
    @SerializedName("actionType") val actionType: String = "Submit",
    @SerializedName("contextId") val contextId: String,
    @SerializedName("comments") val comments: String = "",
)

data class SubmitApprovalBody(
    @SerializedName("requests") val requests: List<ApprovalRequestItem>,
)

data class SavePdfBody(
    @SerializedName("templateId") val templateId: String,
    @SerializedName("quoteId") val quoteId: String,
)

data class CpqCreateResponse(
    @SerializedName("id") val id: String? = null,
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
)

data class CpqPdfSaveResponse(
    @SerializedName(value = "result", alternate = ["status"]) val result: String? = null,
    @SerializedName(value = "message", alternate = ["error", "errorMessage"]) val message: String? = null,
) {
    val isSuccess: Boolean get() = result.equals("SUCCESS", ignoreCase = true)
}

data class CpqFileInfo(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("extension") val extension: String = "",
    @SerializedName("downloadUrl") val downloadUrl: String = "",
    @SerializedName("createdDate") val createdDate: String = "",
)
data class SaveLinesResponse(
    @SerializedName("success") val success: Boolean = false,

    @SerializedName(
        value = "message",
        alternate = ["status", "result", "response", "data"]
    )
    val message: String? = null,

    @SerializedName("id") val id: String? = null,
)

// ── Favourites ────────────────────────────────────────────────────────────────

data class CpqFavourite(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("description") val description: String? = null,
    @SerializedName("totalPrice") val totalPrice: Double = 0.0,
    @SerializedName("lineCount") val lineCount: Int = 0,
    @SerializedName("createdDate") val createdDate: String = "",
)
data class CpqFavouriteLine(
    @SerializedName("id") val id: String = "",
    @SerializedName("productId") val productId: String = "",
    @SerializedName("productName") val productName: String? = null,
    @SerializedName("productCode") val productCode: String? = null,
    @SerializedName("quantity") val quantity: Double = 1.0,
    @SerializedName("unitPrice") val unitPrice: Double = 0.0,
    @SerializedName("discount") val discount: Double = 0.0,
    @SerializedName("sellingModel") val sellingModel: String? = null,
)

data class SaveFavouriteBody(
    @SerializedName("label") val label: String,
    @SerializedName("description") val description: String,
    @SerializedName("lines") val lines: List<SaveFavouriteLine>,
)

data class SaveFavouriteLine(
    @SerializedName("productId") val productId: String,
    @SerializedName("quantity") val quantity: Double,
    @SerializedName("unitPrice") val unitPrice: Double,
    @SerializedName("sellingModelId") val sellingModelId: String = "",
)

data class CpqFavouriteCreateResponse(
    @SerializedName("id") val id: String = "",
)

// ── API Service ───────────────────────────────────────────────────────────────

interface CpqApiService {

    // Accounts & Opportunities
    @GET("services/apexrest/cpq/accounts")
    suspend fun getAccounts(): Response<List<CpqAccount>>

    @GET("services/apexrest/cpq/opportunities")
    suspend fun getOpportunities(
        @Query("accountId") accountId: String,
    ): Response<List<CpqOpportunity>>

    // Quotes
    @GET("services/apexrest/cpq/quotes")
    suspend fun getQuotes(
        @Query("accountId") accountId: String,
    ): Response<List<CpqQuote>>



    @GET("services/apexrest/cpq/quotes")
    suspend fun getQuotesByOpportunity(
        @Query("opportunityId") opportunityId: String,
    ): Response<List<CpqQuote>>

    @POST("services/data/v66.0/sobjects/Quote/")
    suspend fun createQuote(
        @Body body: StandardCreateQuoteBody,
    ): Response<StandardCreateResponse>

    @GET("services/data/v66.0/query/")
    suspend fun getQuoteDetail(
        @Query("q") soql: String,
    ): Response<QuoteQueryResponse>

    @PATCH("services/data/v66.0/sobjects/Quote/{quoteId}")
    suspend fun updateQuote(
        @Path("quoteId") quoteId: String,
        @Body body: Map<String, String>,
    ): Response<Unit>

    // Catalog
    @GET("services/apexrest/cpq/catalogs")
    suspend fun getCatalogs(): Response<List<CpqCatalog>>

    @GET("services/apexrest/cpq/categories")
    suspend fun getCategories(
        @Query("catalogId") catalogId: String,
    ): Response<List<CpqCategory>>

    @GET("services/apexrest/cpq/products")
    suspend fun getProducts(
        @Query("catalogId") catalogId: String,
        @Query("categoryId") categoryId: String = "",
    ): Response<List<CpqProduct>>

    @GET("services/apexrest/cpq/selling-models")
    suspend fun getSellingModels(
        @Query("productId") productId: String,
    ): Response<List<CpqSellingModel>>

    @GET("services/apexrest/cpq/attributes")
    suspend fun getAttributes(
        @Query("productId") productId: String,
    ): Response<List<CpqAttribute>>

    @GET("services/apexrest/cpq/bundle-components")
    suspend fun getBundleComponents(
        @Query("productId") productId: String,
    ): Response<List<CpqBundleComponent>>

    // Lines
    @GET("services/apexrest/cpq/lines")
    suspend fun getLines(
        @Query("quoteId") quoteId: String,
    ): Response<List<CpqQuoteLine>>

    @POST("services/apexrest/cpq/lines/save")
    suspend fun saveLines(
        @Body body: SaveLinesBody,
    ): Response<SaveLinesResponse>

    @POST("services/apexrest/cpq/lines/edit")
    suspend fun editLines(
        @Body body: EditLinesBody,
    ): Response<Unit>

    @DELETE("services/apexrest/cpq/lines")
    suspend fun deleteLines(
        @Query("ids") ids: String,
    ): Response<Unit>

    // PDF
    @GET("services/apexrest/cpq/pdf/templates")
    suspend fun getPdfTemplates(): Response<List<CpqPdfTemplate>>

    @POST("services/apexrest/cpq/pdf/save")
    suspend fun savePdf(
        @Body body: SavePdfBody,
    ): Response<CpqPdfSaveResponse>

    @GET("services/apexrest/cpq/files")
    suspend fun getFiles(
        @Query("quoteId") quoteId: String,
    ): Response<List<CpqFileInfo>>

    // Approval
    @GET("services/apexrest/cpq/approval")
    suspend fun getApprovalStatus(
        @Query("quoteId") quoteId: String,
    ): Response<CpqApprovalStatus>

    @POST("services/data/v66.0/process/approvals")
    suspend fun submitApproval(
        @Body body: SubmitApprovalBody,
    ): Response<List<CpqApprovalSubmitResult>>

    @GET
    @Streaming
    suspend fun downloadFile(@Url url: String): Response<ResponseBody>

    // ── Favourites ────────────────────────────────────────────────────────────────

    @GET("services/apexrest/cpq/favourites")
    suspend fun getFavourites(): Response<List<CpqFavourite>>

    @GET("services/apexrest/cpq/favourites/{favouriteId}/lines")
    suspend fun getFavouriteLines(
        @Path("favouriteId") favouriteId: String,
    ): Response<List<CpqFavouriteLine>>

    @POST("services/apexrest/cpq/favourites")
    suspend fun saveFavourite(
        @Body body: SaveFavouriteBody,
    ): Response<CpqFavouriteCreateResponse>

    @DELETE("services/apexrest/cpq/favourites/{favouriteId}")
    suspend fun deleteFavourite(
        @Path("favouriteId") favouriteId: String,
    ): Response<Unit>
}