// NEW FILE
// app/src/main/java/com/example/nexoworxcrmapp/data/task/TaskRepository.kt

package com.example.nexoworxcrmapp.data.task

import com.example.nexoworxcrmapp.data.Task
import com.example.nexoworxcrmapp.data.task.network.SalesforceTaskCreateRequest
import com.example.nexoworxcrmapp.data.task.network.TASKS_SOQL
import com.example.nexoworxcrmapp.data.task.network.TaskApiService
import com.example.nexoworxcrmapp.data.task.network.tasksByParentSoql
import com.example.nexoworxcrmapp.data.task.network.toDomain
import com.example.nexoworxcrmapp.network.ApiResult
import com.example.nexoworxcrmapp.network.safeApiCall
import com.example.nexoworxcrmapp.network.safeApiCallEmpty

class TaskRepository(private val api: TaskApiService) {

    // Get all tasks
    suspend fun readAllTasks(): ApiResult<List<Task>> {
        return when (val result = safeApiCall { api.queryTasks(TASKS_SOQL) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.records.map { it.toDomain() })
            is ApiResult.Error -> result
        }
    }

    // Get tasks for a specific Lead/Account/Opportunity
    suspend fun readTasksForParent(parentId: String, isLead: Boolean = false): ApiResult<List<Task>> {
        return when (val result = safeApiCall { api.queryTasks(tasksByParentSoql(parentId, isLead)) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.records.map { it.toDomain() })
            is ApiResult.Error -> result
        }
    }

    suspend fun readOneTask(id: String): ApiResult<Task> {
        return when (val result = safeApiCall { api.getTask(id) }) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> result
        }
    }

    suspend fun createTask(request: SalesforceTaskCreateRequest): ApiResult<Task> {
        return when (val result = safeApiCall { api.createTask(request) }) {
            is ApiResult.Success -> {
                if (!result.data.success) {
                    ApiResult.Error(message = result.data.errors?.firstOrNull()?.message ?: "Create failed")
                } else {
                    readOneTask(result.data.id)
                }
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun updateTask(id: String, request: SalesforceTaskCreateRequest): ApiResult<Task> {
        return when (val result = safeApiCallEmpty { api.updateTask(id, request) }) {
            is ApiResult.Success -> readOneTask(id)
            is ApiResult.Error -> result
        }
    }

    suspend fun deleteTask(id: String): ApiResult<Unit> {
        return safeApiCallEmpty { api.deleteTask(id) }
    }
}
