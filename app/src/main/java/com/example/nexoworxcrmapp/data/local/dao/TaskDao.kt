package com.example.nexoworxcrmapp.data.local.dao

import androidx.room.*
import com.example.nexoworxcrmapp.data.local.entity.SyncStatus
import com.example.nexoworxcrmapp.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE syncStatus != :pendingDelete ORDER BY dueDate ASC")
    fun observeAll(pendingDelete: String = SyncStatus.PENDING_DELETE): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE syncStatus != :pendingDelete AND (whoId = :parentId OR whatId = :parentId) ORDER BY dueDate ASC")
    fun observeForParent(parentId: String, pendingDelete: String = SyncStatus.PENDING_DELETE): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(tasks: List<TaskEntity>)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE tasks SET syncStatus = :status WHERE id = :id")
    suspend fun setSyncStatus(id: String, status: String)

    @Transaction
    suspend fun replaceLocalWithServer(localId: String, server: TaskEntity) {
        deleteById(localId)
        upsert(server)
    }

    @Query("SELECT id FROM tasks WHERE syncStatus != :synced")
    suspend fun getDirtyIds(synced: String = SyncStatus.SYNCED): List<String>

    // FK cascade: when a Lead's local placeholder ID gets swapped for its
    // real Salesforce ID, any Task referencing that Lead needs to follow.
    @Query("UPDATE tasks SET whoId = :newId WHERE whoId = :oldId")
    suspend fun reparentWhoId(oldId: String, newId: String)

    @Query("UPDATE tasks SET whatId = :newId WHERE whatId = :oldId")
    suspend fun reparentWhatId(oldId: String, newId: String)
}