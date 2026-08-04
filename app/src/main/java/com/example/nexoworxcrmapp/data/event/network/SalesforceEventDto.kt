// NEW FILE
// app/src/main/java/com/example/nexoworxcrmapp/data/event/network/SalesforceEventDto.kt

package com.example.nexoworxcrmapp.data.event.network

import com.google.gson.annotations.SerializedName

data class SalesforceEventQueryResponse(
    @SerializedName("totalSize") val totalSize: Int = 0,
    @SerializedName("done") val done: Boolean = true,
    @SerializedName("records") val records: List<SalesforceEventDto> = emptyList(),
)

data class SalesforceEventDto(
    @SerializedName("attributes") val attributes: SalesforceEventAttributes? = null,
    @SerializedName("Id") val id: String? = null,
    @SerializedName("Subject") val subject: String? = null,
    @SerializedName("StartDateTime") val startDateTime: String? = null,
    @SerializedName("EndDateTime") val endDateTime: String? = null,
    @SerializedName("Location") val location: String? = null,
    @SerializedName("WhatId") val whatId: String? = null,
    @SerializedName("Description") val description: String? = null,
    @SerializedName("WhoId") val whoId: String? = null,
)

data class SalesforceEventAttributes(
    @SerializedName("type") val type: String? = null,
    @SerializedName("url") val url: String? = null,
)

// All events, soonest first
const val EVENTS_SOQL =
    "SELECT Id,Subject,StartDateTime,EndDateTime,Location,WhatId,WhoId,Description FROM Event ORDER BY StartDateTime ASC"

fun eventsByParentSoql(parentId: String, isLead: Boolean): String {
    val field = if (isLead) "WhoId" else "WhatId"
    return "SELECT Id,Subject,StartDateTime,EndDateTime,Location,WhatId,WhoId,Description FROM Event WHERE $field = '$parentId' ORDER BY StartDateTime ASC"
}

data class SalesforceEventCreateRequest(
    @SerializedName("Subject") val subject: String,
    @SerializedName("StartDateTime") val startDateTime: String,
    @SerializedName("EndDateTime") val endDateTime: String,
    @SerializedName("Location") val location: String? = null,
    @SerializedName("WhatId") val whatId: String? = null,
    @SerializedName("Description") val description: String? = null,
    @SerializedName("WhoId") val whoId: String? = null,
)

data class SalesforceEventCreateResponse(
    @SerializedName("id") val id: String,
    @SerializedName("success") val success: Boolean,
    @SerializedName("errors") val errors: List<SalesforceEventError>? = null,
)

data class SalesforceEventError(
    @SerializedName("message") val message: String? = null,
    @SerializedName("statusCode") val statusCode: String? = null,
)