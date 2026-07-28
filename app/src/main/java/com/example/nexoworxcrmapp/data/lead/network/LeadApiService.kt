package com.example.nexoworxcrmapp.data.lead.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface LeadApiService {

    @GET("services/data/v66.0/query/")
    suspend fun queryLeads(
        @Query("q") soql: String = LEADS_SOQL,
    ): Response<SalesforceQueryResponse>

    @GET("services/data/v66.0/sobjects/Lead/{leadId}")
    suspend fun getLead(
        @Path("leadId") leadId: String,
    ): Response<SalesforceLeadDto>

    @POST("services/data/v66.0/sobjects/Lead/")
    suspend fun createLead(
        @Body body: SalesforceLeadCreateRequest,
    ): Response<SalesforceCreateResponse>

    @PATCH("services/data/v66.0/sobjects/Lead/{leadId}")
    suspend fun updateLead(
        @Path("leadId") leadId: String,
        @Body body: SalesforceLeadPatchRequest,
    ): Response<Unit>

    /** Delete a Lead permanently — Salesforce returns 204 No Content */
    @DELETE("services/data/v66.0/sobjects/Lead/{leadId}")
    suspend fun deleteLead(
        @Path("leadId") leadId: String,
    ): Response<Unit>

    /** Convert a Lead → Account + Contact via Salesforce invocable action */
    @POST("services/apexrest/convertlead/")
    suspend fun convertLead(
        @Body body: ApexConvertRequest,
    ): Response<ApexConvertResult>

    
}
