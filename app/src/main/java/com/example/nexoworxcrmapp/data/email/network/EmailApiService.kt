// NEW FILE
// app/src/main/java/com/example/nexoworxcrmapp/data/email/network/EmailApiService.kt

package com.example.nexoworxcrmapp.data.email.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface EmailApiService {

    @POST("services/data/v66.0/actions/standard/emailSimple")
    suspend fun sendEmail(
        @Body body: EmailRequest,
    ): Response<List<EmailResult>>
}

data class EmailRequest(
    @SerializedName("inputs") val inputs: List<EmailInput>,
)

data class EmailInput(
    @SerializedName("emailBody") val emailBody: String,
    @SerializedName("emailAddresses") val emailAddresses: String,
    @SerializedName("emailSubject") val emailSubject: String,
    @SerializedName("senderAddress") val senderAddress: String,
    @SerializedName("targetObjectId") val targetObjectId: String? = null, // Lead ID — logs email on Lead record
)

data class EmailResult(
    @SerializedName("actionName") val actionName: String? = null,
    @SerializedName("isSuccess") val isSuccess: Boolean = false,
    @SerializedName("errors") val errors: List<EmailError>? = null,
)

data class EmailError(
    @SerializedName("message") val message: String? = null,
    @SerializedName("statusCode") val statusCode: String? = null,
)
