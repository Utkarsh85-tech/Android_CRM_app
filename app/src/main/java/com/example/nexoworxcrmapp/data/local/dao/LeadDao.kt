package com.example.nexoworxcrmapp.data.local.dao

import androidx.room.*
import com.example.nexoworxcrmapp.data.local.entity.LeadEntity
import com.example.nexoworxcrmapp.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface LeadDao {

    // Hide rows queued for deletion so the UI doesn't flash them
    @Query("SELECT * FROM leads WHERE syncStatus != :pendingDelete ORDER BY firstName COLLATE NOCASE ASC, lastName COLLATE NOCASE ASC")
    fun observeAll(pendingDelete: String = SyncStatus.PENDING_DELETE): Flow<List<LeadEntity>>

    @Query("SELECT * FROM leads WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): LeadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(lead: LeadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(leads: List<LeadEntity>)

    @Query("DELETE FROM leads WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE leads SET syncStatus = :status WHERE id = :id")
    suspend fun setSyncStatus(id: String, status: String)

    /** Swap a locally-created placeholder row for its real Salesforce record. */
    @Transaction
    suspend fun replaceLocalWithServer(localId: String, server: LeadEntity) {
        deleteById(localId)
        upsert(server)
    }

    /** Pull merge: never stomp a row that has un-synced local edits. */
    @Query("SELECT id FROM leads WHERE syncStatus != :synced")
    suspend fun getDirtyIds(synced: String = SyncStatus.SYNCED): List<String>
}