package com.example.nexoworxcrmapp.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexoworxcrmapp.data.account.SalesforceContractDto
import com.example.nexoworxcrmapp.network.ApiResult
import com.example.nexoworxcrmapp.network.NetworkModule
import com.example.nexoworxcrmapp.network.safeApiCall
import com.example.nexoworxcrmapp.ui.theme.*

// ── Contract List Screen ──────────────────────────────────────────────────────

@Composable
fun ContractListScreen(
    accountId: String,
    onBack: () -> Unit,
    onContractClick: (contractId: String) -> Unit,
) {
    val api = remember { NetworkModule.contractApi }
    var contracts by remember { mutableStateOf<List<SalesforceContractDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(accountId) {
        isLoading = true
        val soql = "SELECT Id,ContractNumber,Status,StartDate,EndDate FROM Contract WHERE AccountId='$accountId' ORDER BY CreatedDate DESC"
        when (val r = safeApiCall { api.getContractsByAccount(soql) }) {
            is ApiResult.Success -> { contracts = r.data.records; isLoading = false }
            is ApiResult.Error -> { error = r.message; isLoading = false }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(CrmBg)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().background(Forest).padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CrmSurface)
            }
            Text("Contracts", modifier = Modifier.weight(1f), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CrmSurface)
        }

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Forest)
            }
            error != null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFC0392B), modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(error.orEmpty(), color = Color(0xFFC0392B), fontSize = 14.sp)
                }
            }
            contracts.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = MutedGreen, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No contracts found", color = TextMuted, fontSize = 15.sp)
                }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(contracts, key = { it.id }) { contract ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CrmSurface)
                            .border(1.dp, BorderGreen, RoundedCornerShape(12.dp))
                            .clickable { onContractClick(contract.id) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Contract #${contract.contractNumber}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Charcoal,
                            )
                            Text(
                                "${contract.startDate} → ${contract.endDate}",
                                fontSize = 12.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(top = 3.dp),
                            )
                        }
                        ContractStatusBadge(contract.status)
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

// ── Contract Detail Screen ────────────────────────────────────────────────────

@Composable
fun ContractDetailScreen(
    contractId: String,
    onBack: () -> Unit,
) {
    val api = remember { NetworkModule.contractApi }
    var contract by remember { mutableStateOf<SalesforceContractDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(contractId) {
        isLoading = true
        when (val r = safeApiCall { api.getContractDetail(contractId) }) {
            is ApiResult.Success -> { contract = r.data; isLoading = false }
            is ApiResult.Error -> { error = r.message; isLoading = false }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(CrmBg)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().background(Forest).padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CrmSurface)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Contract #${contract?.contractNumber ?: ""}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = CrmSurface,
                )
                if (contract != null) {
                    ContractStatusBadge(contract!!.status)
                }
            }
        }

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Forest)
            }
            error != null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(error.orEmpty(), color = Color(0xFFC0392B))
            }
            contract != null -> {
                val c = contract!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Status hero card
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Forest)
                                .padding(16.dp),
                        ) {
                            Text("Contract #${c.contractNumber}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CrmSurface)
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ContractStatusBadge(c.status)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "${c.startDate} → ${c.endDate}",
                                fontSize = 13.sp,
                                color = CrmSurface.copy(alpha = 0.8f),
                            )
                        }
                    }

                    // Contract details card
                    item {
                        ContractDetailCard(title = "CONTRACT DETAILS") {
                            ContractDetailRow(Icons.Default.Tag, "Contract Number", c.contractNumber)
                            ContractDetailRow(Icons.Default.Info, "Status", c.status)
                            ContractDetailRow(Icons.Default.CalendarMonth, "Start Date", c.startDate.ifBlank { "—" })
                            ContractDetailRow(Icons.Default.CalendarMonth, "End Date", c.endDate.ifBlank { "—" })
                            ContractDetailRow(Icons.Default.Schedule, "Term (months)", if (c.contractTerm > 0) "${c.contractTerm} months" else "—")
                            ContractDetailRow(Icons.Default.NotificationsActive, "Owner Expiration Notice", if (c.ownerExpirationNotice.isNotBlank()) "${c.ownerExpirationNotice} days" else "—")
                        }
                    }

                    // Signing details card
                    item {
                        ContractDetailCard(title = "SIGNING DETAILS") {
                            ContractDetailRow(Icons.Default.Person, "Customer Signed Date", c.customerSignedDate.ifBlank { "—" })
                            ContractDetailRow(Icons.Default.Business, "Company Signed Date", c.companySignedDate.ifBlank { "—" })
                        }
                    }

                    // Special terms card (only if present)
                    if (!c.specialTerms.isNullOrBlank()) {
                        item {
                            ContractDetailCard(title = "SPECIAL TERMS") {
                                Text(
                                    c.specialTerms,
                                    fontSize = 14.sp,
                                    color = Charcoal,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                    }

                    // Description card (only if present)
                    if (!c.description.isNullOrBlank()) {
                        item {
                            ContractDetailCard(title = "DESCRIPTION") {
                                Text(
                                    c.description,
                                    fontSize = 14.sp,
                                    color = Charcoal,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                    }

                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

// ── Reusable components ───────────────────────────────────────────────────────

@Composable
private fun ContractDetailCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CrmSurface)
            .border(1.dp, BorderGreen, RoundedCornerShape(12.dp))
            .padding(14.dp),
    ) {
        Text(
            title,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        content()
    }
}

@Composable
private fun ContractDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MutedGreen, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 13.sp, color = Charcoal, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 2.dp))
        }
    }
    HorizontalDivider(color = BorderGreen.copy(alpha = 0.5f), thickness = 0.5.dp)
}

@Composable
fun ContractStatusBadge(status: String) {
    val (bg, fg) = when (status.lowercase()) {
        "draft" -> Color(0xFFE7ECE8) to Color(0xFF5A6A5E)
        "activated" -> Color(0xFFDCF5E7) to Color(0xFF0E6B3E)
        "expired" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        else -> CardGreen to Forest
    }
    Box(
        modifier = Modifier.clip(RoundedCornerShape(99.dp)).background(bg).padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}