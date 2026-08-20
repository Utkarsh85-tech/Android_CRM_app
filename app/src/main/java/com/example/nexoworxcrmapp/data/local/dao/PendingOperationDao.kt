package com.example.nexoworxcrmapp.data.local.dao

import androidx.room.*
import com.example.nexoworxcrmapp.data.local.entity.PendingOperationEntity
import kotlinx.coroutines.flow.Flow
import com.example.nexoworxcrmapp.data.local.entity.MAX_SYNC_RETRIES
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

    @Query("SELECT DISTINCT entityId FROM pending_operations WHERE retryCount < :maxRetries")
    fun observePendingEntityIds(maxRetries: Int = MAX_SYNC_RETRIES): Flow<List<String>>

    @Query("SELECT DISTINCT entityId FROM pending_operations WHERE retryCount >= :maxRetries")
    fun observeFailedEntityIds(maxRetries: Int = MAX_SYNC_RETRIES): Flow<List<String>>

    @Query("SELECT * FROM pending_operations WHERE retryCount >= :maxRetries ORDER BY createdAt DESC")
    fun observeFailedOperations(maxRetries: Int = MAX_SYNC_RETRIES): Flow<List<PendingOperationEntity>>

    @Query("UPDATE pending_operations SET retryCount = 0, lastError = NULL WHERE opId = :opId")
    suspend fun resetRetry(opId: Long)
    @Query("SELECT * FROM pending_operations ORDER BY createdAt DESC")
    fun getAllOrderedFlow(): Flow<List<PendingOperationEntity>>
}