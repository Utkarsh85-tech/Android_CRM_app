package com.example.nexoworxcrmapp.data.attachment

import com.google.gson.annotations.SerializedName

// ── Query responses ───────────────────────────────────────────────────────────

data class SalesforceAttachmentQueryResponse(
    @SerializedName("totalSize") val totalSize: Int = 0,
    @SerializedName("records") val records: List<SalesforceAttachmentDto> = emptyList(),
)

data class SalesforceAttachmentDto(
    @SerializedName("Id") val id: String? = null,
    @SerializedName("ContentDocumentId") val contentDocumentId: String? = null,
    @SerializedName("ContentDocument") val contentDocument: SalesforceContentDocumentDto? = null,
)

data class SalesforceContentDocumentDto(
    @SerializedName("Id") val id: String? = null,
    @SerializedName("Title") val title: String? = null,
    @SerializedName("FileExtension") val fileExtension: String? = null,
    @SerializedName("ContentSize") val contentSize: Long? = null,
    @SerializedName("LastModifiedDate") val lastModifiedDate: String? = null,
    @SerializedName("LatestPublishedVersionId") val latestVersionId: String? = null,
)

data class SalesforceUploadRequest(
    @SerializedName("Title") val title: String,
    @SerializedName("PathOnClient") val pathOnClient: String,
    @SerializedName("VersionData") val versionData: String, // Base64
    @SerializedName("FirstPublishLocationId") val firstPublishLocationId: String,
)

data class SalesforceUploadResponse(
    @SerializedName("id") val id: String? = null,
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("errors") val errors: List<SalesforceAttachmentError>? = null,
)

data class SalesforceAttachmentError(
    @SerializedName("message") val message: String? = null,
)

// ── SOQL ──────────────────────────────────────────────────────────────────────

fun attachmentsSoql(recordId: String) =
    "SELECT Id,ContentDocumentId,ContentDocument.Id,ContentDocument.Title," +
            "ContentDocument.FileExtension,ContentDocument.ContentSize," +
            "ContentDocument.LastModifiedDate,ContentDocument.LatestPublishedVersionId " +
            "FROM ContentDocumentLink WHERE LinkedEntityId='$recordId' ORDER BY ContentDocument.LastModifiedDate DESC"

data class SalesforceContentVersionQueryResponse(
    @SerializedName("records") val records: List<SalesforceContentVersionRecord> = emptyList(),
)

data class SalesforceContentVersionRecord(
    @SerializedName("ContentDocumentId") val contentDocumentId: String? = null,
)

fun contentVersionSoql(contentVersionId: String) =
    "SELECT ContentDocumentId FROM ContentVersion WHERE Id='$contentVersionId'"