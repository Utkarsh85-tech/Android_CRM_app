package com.example.nexoworxcrmapp.data.local.dao

import androidx.room.*
import com.example.nexoworxcrmapp.data.local.entity.AttachmentEntity
import com.example.nexoworxcrmapp.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {

    @Query("SELECT * FROM attachments WHERE syncStatus != :pendingDelete AND parentRecordId = :parentId ORDER BY lastModifiedLocal DESC")
    fun observeForParent(parentId: String, pendingDelete: String = SyncStatus.PENDING_DELETE): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): AttachmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(attachment: AttachmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(attachments: List<AttachmentEntity>)

    @Query("DELETE FROM attachments WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE attachments SET syncStatus = :status WHERE id = :id")
    suspend fun setSyncStatus(id: String, status: String)

    @Transaction
    suspend fun replaceLocalWithServer(localId: String, server: AttachmentEntity) {
        deleteById(localId)
        upsert(server)
    }

    @Query("SELECT id FROM attachments WHERE syncStatus != :synced")
    suspend fun getDirtyIds(synced: String = SyncStatus.SYNCED): List<String>

    // FK cascade: file attached to a Lead/Task created offline follows its parent's real id
    @Query("UPDATE attachments SET parentRecordId = :newId WHERE parentRecordId = :oldId")
    suspend fun reparent(oldId: String, newId: String)
}