package com.example.nexoworxcrmapp.data.account

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class SalesforceContractListResponse(
    @SerializedName("totalSize") val totalSize: Int = 0,
    @SerializedName("records") val records: List<SalesforceContractDto> = emptyList(),
)

data class SalesforceContractDto(
    @SerializedName("Id") val id: String = "",
    @SerializedName("ContractNumber") val contractNumber: String = "",
    @SerializedName("Status") val status: String = "",
    @SerializedName("StartDate") val startDate: String = "",
    @SerializedName("EndDate") val endDate: String = "",
    @SerializedName("AccountId") val accountId: String = "",
    @SerializedName("OwnerId") val ownerId: String = "",
    @SerializedName("ContractTerm") val contractTerm: Int = 0,
    @SerializedName("OwnerExpirationNotice") val ownerExpirationNotice: String = "",
    @SerializedName("CustomerSignedDate") val customerSignedDate: String = "",
    @SerializedName("CompanySignedDate") val companySignedDate: String = "",
    @SerializedName("SpecialTerms") val specialTerms: String? = null,
    @SerializedName("Description") val description: String? = null,
)

interface ContractApiService {

    @GET("services/data/v66.0/query/")
    suspend fun getContractsByAccount(
        @Query("q") soql: String,
    ): Response<SalesforceContractListResponse>

    @GET("services/data/v66.0/sobjects/Contract/{contractId}")
    suspend fun getContractDetail(
        @Path("contractId") contractId: String,
    ): Response<SalesforceContractDto>
}