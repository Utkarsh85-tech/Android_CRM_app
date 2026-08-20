package com.example.nexoworxcrmapp.data.sync

import com.example.nexoworxcrmapp.data.local.dao.PendingOperationDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class SyncIssue(
    val opId: Long,
    val entityType: String,
    val entityId: String,
    val operationType: String,
    val lastError: String?,
    val retryCount: Int,
)

/**
 * Read-only view over the outbox's health. Nothing here pushes or pulls data —
 * that stays SyncManager's job. This class only answers "what's the current
 * sync state of the world", for badges and the Sync Issues screen.
 */
class SyncStatusRepository(private val pendingOpDao: PendingOperationDao) {

    fun observePendingIds(): Flow<Set<String>> =
        pendingOpDao.observePendingEntityIds().map { it.toSet() }

    fun observeFailedIds(): Flow<Set<String>> =
        pendingOpDao.observeFailedEntityIds().map { it.toSet() }

    fun observeIssues(): Flow<List<SyncIssue>> =
        pendingOpDao.observeFailedOperations().map { ops ->
            ops.map { SyncIssue(it.opId, it.entityType, it.entityId, it.operationType, it.lastError, it.retryCount) }
        }

    fun observeAllIssues(): Flow<List<SyncIssue>> =
        pendingOpDao.getAllOrderedFlow().map { ops ->
            ops.map { SyncIssue(it.opId, it.entityType, it.entityId, it.operationType, it.lastError, it.retryCount) }
        }
    /** Gives a permanently-failed operation another shot on the next sync pass. */
    suspend fun retry(opId: Long) = pendingOpDao.resetRetry(opId)

    /** Gives up on a permanently-failed operation and removes it from the outbox. */
    suspend fun discard(opId: Long) = pendingOpDao.delete(opId)
}