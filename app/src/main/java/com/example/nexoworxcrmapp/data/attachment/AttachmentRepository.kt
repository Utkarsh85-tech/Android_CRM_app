package com.example.nexoworxcrmapp.data.attachment

import android.util.Base64
import com.example.nexoworxcrmapp.network.ApiResult
import com.example.nexoworxcrmapp.network.safeApiCall
import com.example.nexoworxcrmapp.network.safeApiCallEmpty

data class AttachmentItem(
    val id: String,
    val contentDocumentId: String,
    val title: String,
    val fileExtension: String,
    val contentSize: Long,
    val lastModifiedDate: String,
    val downloadVersionId: String,
)

class AttachmentRepository(private val api: AttachmentApiService) {

    suspend fun fetchAttachments(recordId: String): ApiResult<List<AttachmentItem>> {
        return when (val r = safeApiCall { api.queryAttachments(attachmentsSoql(recordId)) }) {
            is ApiResult.Success -> {
                val items = r.data.records.mapNotNull { dto ->
                    val doc = dto.contentDocument ?: return@mapNotNull null
                    AttachmentItem(
                        id = dto.id.orEmpty(),
                        contentDocumentId = doc.id.orEmpty(),
                        title = doc.title.orEmpty(),
                        fileExtension = doc.fileExtension.orEmpty(),
                        contentSize = doc.contentSize ?: 0L,
                        lastModifiedDate = doc.lastModifiedDate.orEmpty(),
                        downloadVersionId = doc.latestVersionId.orEmpty(),
                    )
                }
                ApiResult.Success(items)
            }
            is ApiResult.Error -> r
        }
    }

    suspend fun uploadFile(
        recordId: String,
        fileName: String,
        fileBytes: ByteArray,
    ): ApiResult<Unit> {
        val base64 = Base64.encodeToString(fileBytes, Base64.NO_WRAP)
        val body = SalesforceUploadRequest(
            title = fileName.substringBeforeLast("."),
            pathOnClient = fileName,
            versionData = base64,
            firstPublishLocationId = recordId,
        )
        return when (val r = safeApiCall { api.uploadFile(body) }) {
            is ApiResult.Success -> {
                if (r.data.success) ApiResult.Success(Unit)
                else ApiResult.Error(r.data.errors?.firstOrNull()?.message ?: "Upload failed")
            }
            is ApiResult.Error -> r
        }
    }

    suspend fun deleteFile(contentDocumentId: String): ApiResult<Unit> {
        return safeApiCallEmpty { api.deleteFile(contentDocumentId) }
    }
}