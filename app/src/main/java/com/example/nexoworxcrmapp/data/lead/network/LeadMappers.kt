package com.example.nexoworxcrmapp.data.lead.network

import com.example.nexoworxcrmapp.data.Lead

fun SalesforceLeadDto.toDomain(): Lead {
    return Lead(
        id = id.orEmpty(),
        firstName = firstName.orEmpty(),
        lastName = lastName.orEmpty(),
        company = company.orEmpty(),
        status = status.orEmpty().ifBlank { DEFAULT_LEAD_STATUS },
        phone = phone.orEmpty().ifBlank { mobilePhone.orEmpty() },
        email = email.orEmpty(),
        source = leadSource.orEmpty(),
        rating = rating.orEmpty(),
        industry = industry.orEmpty(),
        title = title.orEmpty(),
        description = description.orEmpty(),
    )
}

fun Lead.toCreateRequest(): SalesforceLeadCreateRequest {
    return SalesforceLeadCreateRequest(
        firstName = firstName,
        lastName = lastName.ifBlank { "Unknown" },
        company = company.ifBlank { "Unknown" },
        mobilePhone = phone.takeIf { it.isNotBlank() },
        phone = phone.takeIf { it.isNotBlank() },
        title = title.takeIf { it.isNotBlank() },
        email = email.takeIf { it.isNotBlank() },
        leadSource = source.ifBlank { "Web" },
        industry = industry.takeIf { it.isNotBlank() },
        status = mapStatusForSalesforce(status),
        rating = rating.takeIf { it.isNotBlank() },
        description = description.takeIf { it.isNotBlank() },
    )
}

fun Lead.toPatchRequest(): SalesforceLeadPatchRequest {
    return SalesforceLeadPatchRequest(
        firstName = firstName.takeIf { it.isNotBlank() },
        lastName = lastName.takeIf { it.isNotBlank() },
        company = company.takeIf { it.isNotBlank() },
        mobilePhone = phone.takeIf { it.isNotBlank() },
        phone = phone.takeIf { it.isNotBlank() },
        title = title.takeIf { it.isNotBlank() },
        email = email.takeIf { it.isNotBlank() },
        leadSource = source.takeIf { it.isNotBlank() },
        industry = industry.takeIf { it.isNotBlank() },
        status = status.takeIf { it.isNotBlank() }?.let { mapStatusForSalesforce(it) },
        rating = rating.takeIf { it.isNotBlank() },
        description = description.takeIf { it.isNotBlank() },
    )
}

fun mapStatusForSalesforce(status: String): String {
    return when (status.trim()) {
        "", "New" -> DEFAULT_LEAD_STATUS
        "Working" -> "Working - Contacted"
        "Unqualified" -> "Closed - Not Converted"
        "Converted" -> "Closed - Converted"
        else -> status
    }
}

private const val DEFAULT_LEAD_STATUS = "Open - Not Contacted"
