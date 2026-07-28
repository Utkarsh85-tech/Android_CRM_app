// NEW FILE
// app/src/main/java/com/example/nexoworxcrmapp/data/email/EmailRepository.kt

package com.example.nexoworxcrmapp.data.email

import com.example.nexoworxcrmapp.data.email.network.EmailApiService
import com.example.nexoworxcrmapp.data.email.network.EmailInput
import com.example.nexoworxcrmapp.data.email.network.EmailRequest
import com.example.nexoworxcrmapp.network.ApiResult
import com.example.nexoworxcrmapp.network.safeApiCall

class EmailRepository(private val api: EmailApiService) {

    suspend fun sendEmail(
        toAddress: String,
        subject: String,
        body: String,
        senderAddress: String,
        leadId: String? = null,     // logs email on Lead record in Salesforce
    ): ApiResult<Unit> {
        val request = EmailRequest(
            inputs = listOf(
                EmailInput(
                    emailBody = body,
                    emailAddresses = toAddress,
                    emailSubject = subject,
                    senderAddress = senderAddress,
                    targetObjectId = leadId,
                )
            )
        )
        return when (val result = safeApiCall { api.sendEmail(request) }) {
            is ApiResult.Success -> {
                val emailResult = result.data.firstOrNull()
                if (emailResult?.isSuccess == true) {
                    ApiResult.Success(Unit)
                } else {
                    val msg = emailResult?.errors?.firstOrNull()?.message ?: "Email send failed"
                    ApiResult.Error(message = msg)
                }
            }
            is ApiResult.Error -> result
        }
    }
}
