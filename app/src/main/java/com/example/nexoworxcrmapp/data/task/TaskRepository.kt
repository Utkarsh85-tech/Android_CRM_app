package com.example.nexoworxcrmapp.data.task

import com.example.nexoworxcrmapp.data.Task
import com.example.nexoworxcrmapp.data.local.dao.PendingOperationDao
import com.example.nexoworxcrmapp.data.local.dao.TaskDao
import com.example.nexoworxcrmapp.data.local.entity.OperationType
import com.example.nexoworxcrmapp.data.local.entity.PendingOperationEntity
import com.example.nexoworxcrmapp.data.local.entity.SyncStatus
import com.example.nexoworxcrmapp.data.task.network.*
import com.example.nexoworxcrmapp.network.ApiResult
import com.example.nexoworxcrmapp.network.safeApiCall
import com.example.nexoworxcrmapp.network.safeApiCallEmpty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class TaskRepository(
    private val api: TaskApiService,
    private val taskDao: TaskDao,
    private val pendingOpDao: PendingOperationDao,
) {
    fun observeTasks(): Flow<List<Task>> = taskDao.observeAll().map { it.map { e -> e.toDomain() } }

    fun observeTasksForParent(parentId: String): Flow<List<Task>> =
        taskDao.observeForParent(parentId).map { it.map { e -> e.toDomain() } }

    suspend fun createTask(
        subject: String, status: String, priority: String, dueDate: String?,
        whoId: String?, whatId: String?, description: String?,
    ): Task {
        val localId = "local_${UUID.randomUUID()}"
        val entity = com.example.nexoworxcrmapp.data.local.entity.TaskEntity(
            id = localId, subject = subject, status = status, priority = priority,
            dueDate = dueDate.orEmpty(), whatId = whatId.orEmpty(), whoId = whoId.orEmpty(),
            description = description.orEmpty(), syncStatus = SyncStatus.PENDING_CREATE,
            lastModifiedLocal = System.currentTimeMillis(),
        )
        taskDao.upsert(entity)
        pendingOpDao.insert(
            PendingOperationEntity(
                entityType = "TASK", entityId = localId, operationType = OperationType.CREATE,
                payloadJson = "{}", createdAt = System.currentTimeMillis(),
            ),
        )
        com.example.nexoworxcrmapp.network.NetworkModule.triggerSyncIfOnline()
        return entity.toDomain()
    }

    suspend fun updateTask(
        id: String, subject: String, status: String, priority: String,
        dueDate: String?, whoId: String?, whatId: String?, description: String?,
    ) {
        val existing = taskDao.getById(id)
        val newStatus = if (existing?.syncStatus == SyncStatus.PENDING_CREATE) SyncStatus.PENDING_CREATE else SyncStatus.PENDING_UPDATE
        val entity = com.example.nexoworxcrmapp.data.local.entity.TaskEntity(
            id = id, subject = subject, status = status, priority = priority,
            dueDate = dueDate.orEmpty(), whatId = whatId.orEmpty(), whoId = whoId.orEmpty(),
            description = description.orEmpty(), syncStatus = newStatus,
            lastModifiedLocal = System.currentTimeMillis(),
        )
        taskDao.upsert(entity)
        if (newStatus == SyncStatus.PENDING_UPDATE) {
            pendingOpDao.insert(
                PendingOperationEntity(
                    entityType = "TASK", entityId = id, operationType = OperationType.UPDATE,
                    payloadJson = "{}", createdAt = System.currentTimeMillis(),
                ),
            )
        }
        com.example.nexoworxcrmapp.network.NetworkModule.triggerSyncIfOnline()
    }

    suspend fun deleteTask(id: String) {
        val existing = taskDao.getById(id) ?: return
        if (existing.syncStatus == SyncStatus.PENDING_CREATE) {
            taskDao.deleteById(id)
            pendingOpDao.deleteForEntity(id)
        } else {
            taskDao.setSyncStatus(id, SyncStatus.PENDING_DELETE)
            pendingOpDao.insert(
                PendingOperationEntity(
                    entityType = "TASK", entityId = id, operationType = OperationType.DELETE,
                    payloadJson = "{}", createdAt = System.currentTimeMillis(),
                ),
            )
        }
        com.example.nexoworxcrmapp.network.NetworkModule.triggerSyncIfOnline()
    }

    // ---- Sync engine hooks

    suspend fun pullFromServer() {
        val result = safeApiCall { api.queryTasks(TASKS_SOQL) }
        if (result is ApiResult.Success) {
            val dirtyIds = taskDao.getDirtyIds().toSet()
            val serverEntities = result.data.records
                .map { it.toDomain().toEntity(SyncStatus.SYNCED) }
                .filter { it.id !in dirtyIds }
            taskDao.upsertAll(serverEntities)
        }
    }

    /** Returns true if pushed, false if it should stay queued (failure OR parent not synced yet). */
    suspend fun pushCreate(op: PendingOperationEntity): Boolean {
        val entity = taskDao.getById(op.entityId) ?: return true // gone locally, drop the op
        if (entity.whoId.startsWith("local_") || entity.whatId.startsWith("local_")) {
            return false // parent Lead/Account hasn't synced yet — retry next pass
        }
        val request = SalesforceTaskCreateRequest(
            subject = entity.subject, status = entity.status, priority = entity.priority,
            activityDate = entity.dueDate.ifBlank { null },
            whatId = entity.whatId.ifBlank { null },
            whoId = entity.whoId.ifBlank { null },
            description = entity.description.ifBlank { null },
        )
        return when (val result = safeApiCall { api.createTask(request) }) {
            is ApiResult.Success -> {
                if (!result.data.success) return false
                val fresh = safeApiCall { api.getTask(result.data.id) }
                if (fresh is ApiResult.Success) {
                    taskDao.replaceLocalWithServer(op.entityId, fresh.data.toDomain().toEntity(SyncStatus.SYNCED))
                }
                true
            }
            is ApiResult.Error -> false
        }
    }

    suspend fun pushUpdate(op: PendingOperationEntity): Boolean {
        val entity = taskDao.getById(op.entityId) ?: return true
        if (entity.whoId.startsWith("local_") || entity.whatId.startsWith("local_")) return false
        val request = SalesforceTaskCreateRequest(
            subject = entity.subject, status = entity.status, priority = entity.priority,
            activityDate = entity.dueDate.ifBlank { null },
            whatId = entity.whatId.ifBlank { null },
            whoId = entity.whoId.ifBlank { null },
            description = entity.description.ifBlank { null },
        )
        return when (safeApiCallEmpty { api.updateTask(op.entityId, request) }) {
            is ApiResult.Success -> { taskDao.setSyncStatus(op.entityId, SyncStatus.SYNCED); true }
            is ApiResult.Error -> false
        }
    }

    suspend fun pushDelete(op: PendingOperationEntity): Boolean {
        return when (safeApiCallEmpty { api.deleteTask(op.entityId) }) {
            is ApiResult.Success -> { taskDao.deleteById(op.entityId); true }
            is ApiResult.Error -> false
        }
    }

    /** Called by SyncManager right after a Lead's local ID gets swapped for its real one. */
    suspend fun reparentTasks(oldLeadLocalId: String, newLeadServerId: String) {
        taskDao.reparentWhoId(oldLeadLocalId, newLeadServerId)
        taskDao.reparentWhatId(oldLeadLocalId, newLeadServerId)
    }
}