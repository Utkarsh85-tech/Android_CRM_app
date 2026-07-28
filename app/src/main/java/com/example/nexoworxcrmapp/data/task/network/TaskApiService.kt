// NEW FILE
// app/src/main/java/com/example/nexoworxcrmapp/data/task/network/TaskApiService.kt

package com.example.nexoworxcrmapp.data.task.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TaskApiService {

    @GET("services/data/v66.0/query/")
    suspend fun queryTasks(
        @Query("q") soql: String,
    ): Response<SalesforceTaskQueryResponse>

    @GET("services/data/v66.0/sobjects/Task/{taskId}")
    suspend fun getTask(
        @Path("taskId") taskId: String,
    ): Response<SalesforceTaskDto>

    @POST("services/data/v66.0/sobjects/Task/")
    suspend fun createTask(
        @Body body: SalesforceTaskCreateRequest,
    ): Response<SalesforceTaskCreateResponse>

    @PATCH("services/data/v66.0/sobjects/Task/{taskId}")
    suspend fun updateTask(
        @Path("taskId") taskId: String,
        @Body body: SalesforceTaskCreateRequest,
    ): Response<Unit>

    @DELETE("services/data/v66.0/sobjects/Task/{taskId}")
    suspend fun deleteTask(
        @Path("taskId") taskId: String,
    ): Response<Unit>
}
