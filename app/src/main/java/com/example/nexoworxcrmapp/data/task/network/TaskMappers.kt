// NEW FILE
// app/src/main/java/com/example/nexoworxcrmapp/data/task/network/TaskMappers.kt

package com.example.nexoworxcrmapp.data.task.network

import com.example.nexoworxcrmapp.data.Task
import com.example.nexoworxcrmapp.data.local.entity.SyncStatus
import com.example.nexoworxcrmapp.data.local.entity.TaskEntity

fun SalesforceTaskDto.toDomain(): Task {
    return Task(
        id = id.orEmpty(),
        subject = subject.orEmpty(),
        status = status.orEmpty(),
        priority = priority.orEmpty(),
        dueDate = activityDate.orEmpty(),
        whatId = whatId.orEmpty(),
        description = description.orEmpty(),
        whoId = whoId.orEmpty(),
    )
}
fun TaskEntity.toDomain(): Task = Task(
    id = id, subject = subject, status = status, priority = priority,
    dueDate = dueDate, whatId = whatId, description = description, whoId = whoId,
)

fun Task.toEntity(syncStatus: String = SyncStatus.SYNCED): TaskEntity = TaskEntity(
    id = id, subject = subject, status = status, priority = priority,
    dueDate = dueDate, whatId = whatId, whoId = whoId, description = description,
    syncStatus = syncStatus, lastModifiedLocal = System.currentTimeMillis(),
)