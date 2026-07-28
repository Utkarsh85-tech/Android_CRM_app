// ─────────────────────────────────────────────────────────────────────────────
// NEW FILE
// app/src/main/java/com/example/nexoworxcrmapp/data/opportunity/network/OpportunityMappers.kt
// ─────────────────────────────────────────────────────────────────────────────

package com.example.nexoworxcrmapp.data.opportunity.network

import com.example.nexoworxcrmapp.data.Opportunity

fun SalesforceOpportunityDto.toDomain(): Opportunity {
    return Opportunity(
        id = id.orEmpty(),
        name = name.orEmpty(),
        stageName = stageName.orEmpty(),
        closeDate = closeDate.orEmpty(),
        amount = amount,
        accountId = accountId.orEmpty(),
        accountName = account?.name.orEmpty(),
        description = description.orEmpty(),
        probability = probability,
        type = type.orEmpty(),
        leadSource = leadSource.orEmpty(),
        expectedRevenue = expectedRevenue,
        deliveryInstallationStatus = deliveryInstallationStatus.orEmpty(),
    )
}
