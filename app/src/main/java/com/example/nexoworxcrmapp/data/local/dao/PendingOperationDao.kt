package com.example.nexoworxcrmapp.data.local.dao

import androidx.room.*
import com.example.nexoworxcrmapp.data.local.entity.PendingOperationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingOperationDao {

    @Query("SELECT * FROM pending_operations ORDER BY createdAt ASC")
    suspend fun getAllOrdered(): List<PendingOperationEntity>

    @Query("SELECT COUNT(*) FROM pending_operations")
    fun observeCount(): Flow<Int>

    @Insert
    suspend fun insert(op: PendingOperationEntity): Long

    @Query("DELETE FROM pending_operations WHERE opId = :opId")
    suspend fun delete(opId: Long)

    @Query("DELETE FROM pending_operations WHERE entityId = :entityId")
    suspend fun deleteForEntity(entityId: String)

    @Query("UPDATE pending_operations SET retryCount = retryCount + 1, lastError = :error WHERE opId = :opId")
    suspend fun markFailed(opId: Long, error: String)
}