package com.example.nexoworxcrmapp.ui.lead

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexoworxcrmapp.data.Lead
import com.example.nexoworxcrmapp.ui.components.LeadAvatar
import com.example.nexoworxcrmapp.ui.components.StatusBadge
import com.example.nexoworxcrmapp.ui.components.displayLeadStatus
import com.example.nexoworxcrmapp.ui.components.leadMatchesSearch
import com.example.nexoworxcrmapp.ui.components.leadMatchesStatusFilter
import com.example.nexoworxcrmapp.ui.theme.AvatarBlue
import com.example.nexoworxcrmapp.ui.theme.AvatarOrange
import com.example.nexoworxcrmapp.ui.theme.AvatarPurple
import com.example.nexoworxcrmapp.ui.theme.AvatarTeal
import com.example.nexoworxcrmapp.ui.theme.BorderGreen
import com.example.nexoworxcrmapp.ui.theme.Charcoal
import com.example.nexoworxcrmapp.ui.theme.CrmBg
import com.example.nexoworxcrmapp.ui.theme.CrmSurface
import com.example.nexoworxcrmapp.ui.theme.Forest
import com.example.nexoworxcrmapp.ui.theme.MutedGreen
import com.example.nexoworxcrmapp.ui.theme.TextMuted
import com.example.nexoworxcrmapp.ui.theme.TextSecondary

private val avatarColors = listOf(Forest, AvatarBlue, AvatarPurple, AvatarTeal, AvatarOrange)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadScreen(
    modifier: Modifier = Modifier,
    viewModel: LeadViewModel = viewModel(),
    onLeadClick: (Lead) -> Unit = {},
    onAddLeadClick: () -> Unit = {},
    refreshTrigger: Boolean = false,
) {
    val uiState by viewModel.uiState.collectAsState()
    var search by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("All") }
    val filters = listOf("All", "New", "Working", "Qualified")
    val allLeads = uiState.leads
    val leads = allLeads.filter { lead ->
        leadMatchesStatusFilter(lead.status, filterStatus) && leadMatchesSearch(lead, search)
    }

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger) {
            viewModel.refresh()
        }
    }

    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = { viewModel.refresh() },
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CrmBg),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "NEXOWORX CRM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MutedGreen,
                    letterSpacing = 0.5.sp,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Leads",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Charcoal,
                    )
                    IconButton(
                        onClick = onAddLeadClick,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Forest)
                            .shadow(4.dp, CircleShape),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add lead", tint = CrmSurface)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CrmSurface)
                        .border(1.dp, BorderGreen, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MutedGreen, modifier = Modifier.size(16.dp))
                    BasicTextField(
                        value = search,
                        onValueChange = { search = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        textStyle = TextStyle(fontSize = 13.sp, color = Charcoal),
                        cursorBrush = SolidColor(Forest),
                        decorationBox = { inner ->
                            if (search.isEmpty()) {
                                Text("Search name, company, or mobile…", fontSize = 13.sp, color = TextMuted)
                            }
                            inner()
                        },
                    )
                    Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = MutedGreen, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    filters.forEach { filter ->
                        val selected = filterStatus == filter
                        Text(
                            text = filter,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (selected) Forest else CrmSurface)
                                .border(1.5.dp, if (selected) Forest else BorderGreen, RoundedCornerShape(20.dp))
                                .clickable { filterStatus = filter }
                                .padding(horizontal = 12.dp, vertical = 5.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) CrmSurface else TextSecondary,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                val errorMessage = uiState.errorMessage
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        fontSize = 11.sp,
                        color = androidx.compose.ui.graphics.Color(0xFFC0392B),
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .clickable { viewModel.refresh() },
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("Total", allLeads.size.toString(), Modifier.weight(1f))
                    StatCard(
                        "New",
                        allLeads.count { displayLeadStatus(it.status) == "New" }.toString(),
                        Modifier.weight(1f),
                    )
                    StatCard(
                        "Working",
                        allLeads.count { displayLeadStatus(it.status) == "Working" }.toString(),
                        Modifier.weight(1f),
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (leads.isEmpty() && !uiState.isLoading) {
                    item {
                        Text(
                            text = if (search.isNotBlank() || filterStatus != "All") {
                                "No leads match your search"
                            } else {
                                "No leads found"
                            },
                            fontSize = 13.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 24.dp),
                        )
                    }
                }
                itemsIndexed(leads) { index, lead ->
                    LeadCard(
                        lead = lead,
                        avatarColor = avatarColors[index % avatarColors.size],
                        onClick = { onLeadClick(lead) },
                    )
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CrmSurface)
            .border(1.dp, BorderGreen, RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Forest)
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MutedGreen)
    }
}

@Composable
private fun LeadCard(lead: Lead, avatarColor: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CrmSurface)
            .border(1.dp, BorderGreen, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LeadAvatar("${lead.firstName} ${lead.lastName}", background = avatarColor, size = 42)
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = "${lead.firstName} ${lead.lastName}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal,
                )
                StatusBadge(lead.status)
            }
            Text(
                text = lead.company,
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp, bottom = 4.dp),
                maxLines = 1,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = TextMuted, modifier = Modifier.size(10.dp))
                Text(
                    text = lead.phone,
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(start = 3.dp),
                )
            }
        }
    }
}
