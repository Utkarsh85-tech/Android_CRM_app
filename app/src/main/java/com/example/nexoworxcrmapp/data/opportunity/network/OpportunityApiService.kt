// ─────────────────────────────────────────────────────────────────────────────
// NEW FILE
// app/src/main/java/com/example/nexoworxcrmapp/data/opportunity/network/OpportunityApiService.kt
// ─────────────────────────────────────────────────────────────────────────────

package com.example.nexoworxcrmapp.data.opportunity.network


import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.DELETE
import com.example.nexoworxcrmapp.data.opportunity.network.SalesforceLineItemQueryResponse
import com.example.nexoworxcrmapp.data.opportunity.network.SalesforcePricebookQueryResponse
import com.example.nexoworxcrmapp.data.opportunity.network.SalesforcePricebookResponse
import com.example.nexoworxcrmapp.data.opportunity.network.SalesforceStandardPricebookResponse
import com.example.nexoworxcrmapp.data.opportunity.network.SetPricebookRequest
import com.example.nexoworxcrmapp.data.opportunity.network.AddLineItemRequest
import com.example.nexoworxcrmapp.data.opportunity.network.UpdateLineItemRequest
import com.example.nexoworxcrmapp.data.opportunity.network.SalesforceCreateResponse
import com.example.nexoworxcrmapp.data.opportunity.network.STANDARD_PRICEBOOK_SOQL


interface OpportunityApiService {

    @GET("services/data/v66.0/query/")
    suspend fun queryOpportunities(
        @Query("q") soql: String = OPPORTUNITIES_SOQL,
    ): Response<SalesforceOpportunityQueryResponse>

    @GET("services/data/v66.0/sobjects/Opportunity/{opportunityId}")
    suspend fun getOpportunity(
        @Path("opportunityId") opportunityId: String,
    ): Response<SalesforceOpportunityDto>

    @POST("services/data/v66.0/sobjects/Opportunity/")
    suspend fun createOpportunity(
        @Body body: SalesforceOpportunityCreateRequest,
    ): Response<SalesforceOpportunityCreateResponse>

    @PATCH("services/data/v66.0/sobjects/Opportunity/{opportunityId}")
    suspend fun updateOpportunity(
        @Path("opportunityId") opportunityId: String,
        @Body body: SalesforceOpportunityCreateRequest,
    ): Response<Unit>

    /** Delete Opportunity — returns 204 No Content on success */
    @DELETE("services/data/v66.0/sobjects/Opportunity/{opportunityId}")
    suspend fun deleteOpportunity(
        @Path("opportunityId") opportunityId: String,
    ): Response<Unit>

// ── Products ──────────────────────────────────────────────────────────────────

    @GET("services/data/v66.0/query/")
    suspend fun queryLineItems(
        @Query("q") soql: String,
    ): Response<SalesforceLineItemQueryResponse>

    @GET("services/data/v66.0/query/")
    suspend fun queryPricebookEntries(
        @Query("q") soql: String,
    ): Response<SalesforcePricebookQueryResponse>

    @GET("services/data/v66.0/query/")
    suspend fun queryOpportunityPricebook(
        @Query("q") soql: String,
    ): Response<SalesforcePricebookResponse>

    @GET("services/data/v66.0/query/")
    suspend fun queryStandardPricebook(
        @Query("q") soql: String = STANDARD_PRICEBOOK_SOQL,
    ): Response<SalesforceStandardPricebookResponse>

    @PATCH("services/data/v66.0/sobjects/Opportunity/{opportunityId}")
    suspend fun setPricebook(
        @Path("opportunityId") opportunityId: String,
        @Body body: SetPricebookRequest,
    ): Response<Unit>

    @POST("services/data/v66.0/sobjects/OpportunityLineItem/")
    suspend fun addLineItem(
        @Body body: AddLineItemRequest,
    ): Response<SalesforceCreateResponse>

    @PATCH("services/data/v66.0/sobjects/OpportunityLineItem/{lineItemId}")
    suspend fun updateLineItem(
        @Path("lineItemId") lineItemId: String,
        @Body body: UpdateLineItemRequest,
    ): Response<Unit>

    @DELETE("services/data/v66.0/sobjects/OpportunityLineItem/{lineItemId}")
    suspend fun deleteLineItem(
        @Path("lineItemId") lineItemId: String,
    ): Response<Unit>
}
