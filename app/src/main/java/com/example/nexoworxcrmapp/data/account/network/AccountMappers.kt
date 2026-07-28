package com.example.nexoworxcrmapp.data.account.network

import com.example.nexoworxcrmapp.data.Account

// ── Salesforce DTO → our clean Account model ─────────────────────────────────

fun SalesforceAccountDto.toDomain(): Account {
    return Account(
        id = id.orEmpty(),
        name = name.orEmpty(),
        phone = phone.orEmpty(),
        industry = industry.orEmpty(),
        type = type.orEmpty(),
        billingCity = billingCity.orEmpty(),
        billingCountry = billingCountry.orEmpty(),
        website = website.orEmpty(),
        description = description.orEmpty(),
    )
}

// ── Our Account model → Salesforce CREATE request body ───────────────────────

fun Account.toCreateRequest(): SalesforceAccountCreateRequest {
    return SalesforceAccountCreateRequest(
        name = name.trim().ifBlank { "Unknown" },
        phone = phone.trim().ifBlank { null },
        industry = industry.trim().ifBlank { null },
        type = type.trim().ifBlank { null },
        billingCity = billingCity.trim().ifBlank { null },
        billingCountry = billingCountry.trim().ifBlank { null },
        website = website.trim().ifBlank { null },
        description = description.trim().ifBlank { null },
    )
}

// ── Our Account model → Salesforce PATCH (update) request body ───────────────

fun Account.toPatchRequest(): SalesforceAccountPatchRequest {
    return SalesforceAccountPatchRequest(
        name = name.trim().ifBlank { "Unknown" },
        phone = phone.trim().ifBlank { null },
        industry = industry.trim().ifBlank { null },
        type = type.trim().ifBlank { null },
        billingCity = billingCity.trim().ifBlank { null },
        billingCountry = billingCountry.trim().ifBlank { null },
        website = website.trim().ifBlank { null },
        description = description.trim().ifBlank { null },
    )
}
