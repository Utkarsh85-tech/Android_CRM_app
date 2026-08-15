package com.example.nexoworxcrmapp.data.sync

import com.example.nexoworxcrmapp.data.lead.LeadRepository
import com.example.nexoworxcrmapp.data.local.dao.PendingOperationDao
import com.example.nexoworxcrmapp.data.local.entity.OperationType
import com.example.nexoworxcrmapp.data.task.TaskRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
class SyncManager(
    private val leadRepository: LeadRepository,
    private val taskRepository: TaskRepository,
    private val attachmentRepository: com.example.nexoworxcrmapp.data.attachment.AttachmentRepository,
    private val pendingOpDao: PendingOperationDao,
) {
    private val maxRetries = 5
    private val syncMutex = Mutex()


    suspend fun sync() {
        syncMutex.withLock {
            pushPending()
            leadRepository.pullFromServer()
            taskRepository.pullFromServer()
        }
    }

    private suspend fun pushPending() {
        val allOps = pendingOpDao.getAllOrdered()
        val leadOps = allOps.filter { it.entityType == "LEAD" }
        val taskOps = allOps.filter { it.entityType == "TASK" }
        val attachmentOps = allOps.filter { it.entityType == "ATTACHMENT" }

        for (op in leadOps) {
            if (op.retryCount >= maxRetries) continue
            val succeeded = when (op.operationType) {
                OperationType.CREATE -> {
                    val newId = leadRepository.pushCreateReturningId(op)
                    if (newId != null) {
                        taskRepository.reparentTasks(op.entityId, newId)
                        attachmentRepository.reparentAttachments(op.entityId, newId)
                    }
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

        for (op in attachmentOps) {
            if (op.retryCount >= maxRetries) continue
            val succeeded = when (op.operationType) {
                OperationType.CREATE -> attachmentRepository.pushCreate(op)
                OperationType.DELETE -> attachmentRepository.pushDelete(op)
                else -> false
            }
            if (succeeded) pendingOpDao.delete(op.opId) else pendingOpDao.markFailed(op.opId, "sync failed")
        }
    }
}