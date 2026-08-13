package com.example.nexoworxcrmapp.data.sync

import com.example.nexoworxcrmapp.data.lead.LeadRepository
import com.example.nexoworxcrmapp.data.local.dao.PendingOperationDao
import com.example.nexoworxcrmapp.data.local.entity.OperationType
import com.example.nexoworxcrmapp.data.task.TaskRepository

class SyncManager(
    private val leadRepository: LeadRepository,
    private val taskRepository: TaskRepository,
    private val pendingOpDao: PendingOperationDao,
) {
    private val maxRetries = 5

    suspend fun sync() {
        pushPending()
        leadRepository.pullFromServer()
        taskRepository.pullFromServer()
    }

    private suspend fun pushPending() {
        val allOps = pendingOpDao.getAllOrdered()
        // LEAD ops must fully process before TASK ops, so any FK cascade
        // (local Lead id -> real Salesforce id) lands before a dependent
        // Task tries to push in the same pass.
        val leadOps = allOps.filter { it.entityType == "LEAD" }
        val taskOps = allOps.filter { it.entityType == "TASK" }

        for (op in leadOps) {
            if (op.retryCount >= maxRetries) continue
            val succeeded = when (op.operationType) {
                OperationType.CREATE -> {
                    val newId = leadRepository.pushCreateReturningId(op)
                    if (newId != null) taskRepository.reparentTasks(op.entityId, newId)
                    newId != null
                }
                OperationType.UPDATE -> leadRepository.pushUpdate(op)
                OperationType.DELETE -> leadRepository.pushDelete(op)
                else -> false
            }
            if (succeeded) pendingOpDao.delete(op.opId) else pendingOpDao.markFailed(op.opId, "sync failed")
        }

        for (op in taskOps) {
            if (op.retryCount >= maxRetries) continue
            val succeeded = when (op.operationType) {
                OperationType.CREATE -> taskRepository.pushCreate(op)
                OperationType.UPDATE -> taskRepository.pushUpdate(op)
                OperationType.DELETE -> taskRepository.pushDelete(op)
                else -> false
            }
            if (succeeded) pendingOpDao.delete(op.opId) else pendingOpDao.markFailed(op.opId, "sync failed")
        }
    }
}