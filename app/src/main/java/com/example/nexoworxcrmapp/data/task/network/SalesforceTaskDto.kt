// NEW FILE
// app/src/main/java/com/example/nexoworxcrmapp/data/task/network/SalesforceTaskDto.kt

package com.example.nexoworxcrmapp.data.task.network

import com.google.gson.annotations.SerializedName

data class SalesforceTaskQueryResponse(
    @SerializedName("totalSize") val totalSize: Int = 0,
    @SerializedName("done") val done: Boolean = true,
    @SerializedName("records") val records: List<SalesforceTaskDto> = emptyList(),
)

data class SalesforceTaskDto(
    @SerializedName("attributes") val attributes: SalesforceTaskAttributes? = null,
    @SerializedName("Id") val id: String? = null,
    @SerializedName("Subject") val subject: String? = null,
    @SerializedName("Status") val status: String? = null,
    @SerializedName("Priority") val priority: String? = null,
    @SerializedName("ActivityDate") val activityDate: String? = null,
    @SerializedName("WhatId") val whatId: String? = null,
    @SerializedName("Description") val description: String? = null,
    @SerializedName("WhoId") val whoId: String? = null,

    )

data class SalesforceTaskAttributes(
    @SerializedName("type") val type: String? = null,
    @SerializedName("url") val url: String? = null,
)

// All tasks
const val TASKS_SOQL =
    "SELECT Id,Subject,Status,Priority,ActivityDate,WhatId,WhoId,Description FROM Task ORDER BY ActivityDate ASC"
fun tasksByParentSoql(parentId: String, isLead: Boolean): String {
    val field = if (isLead) "WhoId" else "WhatId"
    return "SELECT Id,Subject,Status,Priority,ActivityDate,WhatId,WhoId,Description FROM Task WHERE $field = '$parentId' ORDER BY ActivityDate ASC"
}


data class SalesforceTaskCreateRequest(
    @SerializedName("Subject") val subject: String,
    @SerializedName("Status") val status: String = "Not Started",
    @SerializedName("Priority") val priority: String = "Normal",
    @SerializedName("ActivityDate") val activityDate: String? = null,
    @SerializedName("WhatId") val whatId: String? = null,
    @SerializedName("Description") val description: String? = null,
    @SerializedName("WhoId") val whoId: String? = null,
)

data class SalesforceTaskCreateResponse(
    @SerializedName("id") val id: String,
    @SerializedName("success") val success: Boolean,
    @SerializedName("errors") val errors: List<SalesforceTaskError>? = null,
)

data class SalesforceTaskError(
    @SerializedName("message") val message: String? = null,
    @SerializedName("statusCode") val statusCode: String? = null,
)
