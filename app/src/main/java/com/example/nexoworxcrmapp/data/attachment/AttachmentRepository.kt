package com.example.nexoworxcrmapp.data.attachment

import android.content.Context
import android.util.Base64
import com.example.nexoworxcrmapp.data.local.LocalFileStore
import com.example.nexoworxcrmapp.data.local.dao.AttachmentDao
import com.example.nexoworxcrmapp.data.local.dao.PendingOperationDao
import com.example.nexoworxcrmapp.data.local.entity.AttachmentEntity
import com.example.nexoworxcrmapp.data.local.entity.OperationType
import com.example.nexoworxcrmapp.data.local.entity.PendingOperationEntity
import com.example.nexoworxcrmapp.data.local.entity.SyncStatus
import com.example.nexoworxcrmapp.network.ApiResult
import com.example.nexoworxcrmapp.network.safeApiCall
import com.example.nexoworxcrmapp.network.safeApiCallEmpty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import kotlinx.coroutines.delay

data class AttachmentItem(
    val id: String,
    val contentDocumentId: String,
    val title: String,
    val fileExtension: String,
    val contentSize: Long,
    val lastModifiedDate: String,
    val downloadVersionId: String,
)

class AttachmentRepository(
    private val api: AttachmentApiService,
    private val attachmentDao: AttachmentDao,
    private val pendingOpDao: PendingOperationDao,
    private val appContext: Context,
) {
    fun observeAttachments(parentId: String): Flow<List<AttachmentItem>> =
        attachmentDao.observeForParent(parentId).map { list ->
            list.map {
                AttachmentItem(
                    id = it.id, contentDocumentId = it.id, title = it.title,
                    fileExtension = it.fileExtension, contentSize = it.fileSizeBytes,
                    lastModifiedDate = it.lastModifiedDate, downloadVersionId = "",
                )
            }
        }

    /** Pull is per-record and on-demand — not part of the global periodic sync. */
    suspend fun pullForParent(recordId: String) {
        val result = safeApiCall { api.queryAttachments(attachmentsSoql(recordId)) }
        if (result is ApiResult.Success) {
            val dirtyIds = attachmentDao.getDirtyIds().toSet()
            val entities = result.data.records.mapNotNull { dto ->
                val doc = dto.contentDocument ?: return@mapNotNull null
                val id = doc.id.orEmpty()
                if (id.isBlank() || id in dirtyIds) return@mapNotNull null
                AttachmentEntity(
                    id = id, parentRecordId = recordId, title = doc.title.orEmpty(),
                    fileExtension = doc.fileExtension.orEmpty(), localFilePath = "",
                    fileSizeBytes = doc.contentSize ?: 0L,
                    lastModifiedDate = doc.lastModifiedDate.orEmpty(),
                    syncStatus = SyncStatus.SYNCED, lastModifiedLocal = System.currentTimeMillis(),
                )
            }
            attachmentDao.upsertAll(entities)
        }
    }

    /** Writes the file to local storage immediately and queues the upload. */
    suspend fun addAttachment(parentId: String, fileName: String, fileBytes: ByteArray) {
        val localId = "local_${UUID.randomUUID()}"
        val path = LocalFileStore.save(appContext, fileName, fileBytes)
        val entity = AttachmentEntity(
            id = localId, parentRecordId = parentId, title = fileName.substringBeforeLast("."),
            fileExtension = fileName.substringAfterLast(".", ""), localFilePath = path,
            fileSizeBytes = fileBytes.size.toLong(), lastModifiedDate = "",
            syncStatus = SyncStatus.PENDING_CREATE, lastModifiedLocal = System.currentTimeMillis(),
        )
        attachmentDao.upsert(entity)
        pendingOpDao.insert(
            PendingOperationEntity(
                entityType = "ATTACHMENT", entityId = localId, operationType = OperationType.CREATE,
                payloadJson = "{}", createdAt = System.currentTimeMillis(),
            ),
        )
        com.example.nexoworxcrmapp.network.NetworkModule.triggerSyncIfOnline()
    }

    suspend fun deleteAttachment(id: String) {
        val existing = attachmentDao.getById(id) ?: return
        LocalFileStore.delete(existing.localFilePath)
        if (existing.syncStatus == SyncStatus.PENDING_CREATE) {
            attachmentDao.deleteById(id)
            pendingOpDao.deleteForEntity(id)
        } else {
            attachmentDao.setSyncStatus(id, SyncStatus.PENDING_DELETE)
            pendingOpDao.insert(
                PendingOperationEntity(
                    entityType = "ATTACHMENT", entityId = id, operationType = OperationType.DELETE,
                    payloadJson = "{}", createdAt = System.currentTimeMillis(),
                ),
            )
        }
        com.example.nexoworxcrmapp.network.NetworkModule.triggerSyncIfOnline()
    }

    // ---- Sync engine hooks ----

    suspend fun pushCreate(op: PendingOperationEntity): Boolean {
        val entity = attachmentDao.getById(op.entityId) ?: return true
        if (entity.parentRecordId.startsWith("local_")) return false // parent not synced yet

        val bytes = LocalFileStore.read(entity.localFilePath) ?: return true // file gone, drop the op
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        val request = SalesforceUploadRequest(
            title = entity.title, pathOnClient = "${entity.title}.${entity.fileExtension}",
            versionData = base64, firstPublishLocationId = entity.parentRecordId,
        )
        return when (val result = safeApiCall { api.uploadFile(request) }) {
            is ApiResult.Success -> {
                if (!result.data.success) return false
                val contentVersionId = result.data.id ?: return false

                // The upload itself succeeded — from here on we must never
                // leave this record stuck, since we can't safely retry the
                // upload again without risking a duplicate file.
                var contentDocumentId: String? = null
                repeat(3) { attempt ->
                    if (contentDocumentId != null) return@repeat
                    val docIdResult = safeApiCall { api.queryContentVersion(contentVersionSoql(contentVersionId)) }
                    contentDocumentId = (docIdResult as? ApiResult.Success)?.data?.records?.firstOrNull()?.contentDocumentId
                    if (contentDocumentId == null && attempt < 2) kotlinx.coroutines.delay(500)
                }

                val resolvedId = contentDocumentId ?: contentVersionId // fallback so it's never stuck
                LocalFileStore.delete(entity.localFilePath)
                attachmentDao.replaceLocalWithServer(
                    op.entityId,
                    entity.copy(id = resolvedId, localFilePath = "", syncStatus = SyncStatus.SYNCED),
                )
                true
            }
            is ApiResult.Error -> false
        }
    }

    suspend fun pushDelete(op: PendingOperationEntity): Boolean {
        return when (safeApiCallEmpty { api.deleteFile(op.entityId) }) {
            is ApiResult.Success -> { attachmentDao.deleteById(op.entityId); true }
            is ApiResult.Error -> false
        }
    }

    suspend fun reparentAttachments(oldParentId: String, newParentId: String) {
        attachmentDao.reparent(oldParentId, newParentId)
    }
}