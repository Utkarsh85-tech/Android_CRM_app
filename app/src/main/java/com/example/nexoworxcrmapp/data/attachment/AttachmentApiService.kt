package com.example.nexoworxcrmapp.data.attachment

import retrofit2.Response
import retrofit2.http.*

interface AttachmentApiService {

    @GET("services/data/v66.0/query/")
    suspend fun queryAttachments(
        @Query("q") soql: String,
    ): Response<SalesforceAttachmentQueryResponse>

    @POST("services/data/v66.0/sobjects/ContentVersion/")
    suspend fun uploadFile(
        @Body body: SalesforceUploadRequest,
    ): Response<SalesforceUploadResponse>

    @DELETE("services/data/v66.0/sobjects/ContentDocument/{documentId}")
    suspend fun deleteFile(
        @Path("documentId") documentId: String,
    ): Response<Unit>

    @GET("services/data/v66.0/query/")
    suspend fun queryContentVersion(
        @Query("q") soql: String,
    ): Response<SalesforceContentVersionQueryResponse>
}