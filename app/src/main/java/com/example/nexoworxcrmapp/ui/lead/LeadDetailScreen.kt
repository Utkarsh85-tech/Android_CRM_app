package com.example.nexoworxcrmapp.ui.lead

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexoworxcrmapp.data.Lead
import com.example.nexoworxcrmapp.data.LeadEvent
import com.example.nexoworxcrmapp.data.LeadTask
import com.example.nexoworxcrmapp.ui.components.LeadAvatar
import com.example.nexoworxcrmapp.ui.components.StatusBadge
import com.example.nexoworxcrmapp.ui.theme.BorderGreen
import com.example.nexoworxcrmapp.ui.theme.CardGreen
import com.example.nexoworxcrmapp.ui.theme.Charcoal
import com.example.nexoworxcrmapp.ui.theme.CrmBg
import com.example.nexoworxcrmapp.ui.theme.CrmSurface
import com.example.nexoworxcrmapp.ui.theme.Forest
import com.example.nexoworxcrmapp.ui.theme.MidGreen
import com.example.nexoworxcrmapp.ui.theme.MutedGreen
import com.example.nexoworxcrmapp.ui.theme.TextMuted
import com.example.nexoworxcrmapp.ui.theme.TextSecondary
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.ui.platform.LocalContext

@Composable
fun LeadDetailScreen(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onEmailClick: (email: String, name: String) -> Unit = { _, _ -> },
    onAddTask: () -> Unit = {},
    modifier: Modifier = Modifier,
    refreshTrigger: Boolean = false,
    onRefreshConsumed: () -> Unit = {},
    viewModel: LeadDetailViewModel = viewModel(),
    onFilesClick: () -> Unit = {}

) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger) {
            viewModel.loadLead()
            onRefreshConsumed()
        }
    }

// Controls whether the "are you sure?" dialog is visible
    var showDeleteDialog by remember { mutableStateOf(false) }

    // When delete succeeds, go back to the lead list
    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
            viewModel.consumeDeleteSuccess()
            onBack()
        }
    }

    // When convert succeeds, go back to the lead list
    // (the lead no longer exists — it became an Account/Contact/Opportunity)
    LaunchedEffect(uiState.convertSuccess) {
        if (uiState.convertSuccess) {
            viewModel.consumeConvertSuccess()
            onBack()
        }
    }

    // Show the confirmation dialog when the user taps the delete icon
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Lead") },
            text = { Text("Are you sure you want to permanently delete this lead? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteLead()
                    },
                ) {
                    Text("Delete", color = Color(0xFFC0392B))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CrmBg),
    ) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Forest)
                }
            }
            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(uiState.errorMessage.orEmpty(), color = Color(0xFFC0392B), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { viewModel.loadLead() }, colors = ButtonDefaults.buttonColors(containerColor = Forest)) {
                        Text("Retry")
                    }
                }
            }
            uiState.lead != null -> {
                val lead = uiState.lead!!
                LeadDetailHeader(
                    lead = lead,
                    onBack = onBack,
                    onEdit = onEdit,
                    context = context,
                    onAddTask = onAddTask,
                    onEmailClick = onEmailClick,
                    onFilesClick = onFilesClick,
                    isDeleting = uiState.isDeleting,
                    onDeleteClick = { showDeleteDialog = true },
                    onConvertClick = { viewModel.convertLead() },
                    isConverting = uiState.isConverting,
                )

                LeadInfoTab(lead = lead)
//                LeadDetailTabs(
//                    selectedTab = uiState.selectedTab,
//                    taskCount = uiState.tasks.size,
//                    eventCount = uiState.events.size,
//                    onTabSelected = viewModel::selectTab,
//                )
//                when (uiState.selectedTab) {
//                    LeadDetailTab.Info -> LeadInfoTab(lead = lead)
//                    LeadDetailTab.Tasks -> LeadTasksTab(tasks = uiState.tasks)
//
//                }
            }
        }
    }
}

@Composable
private fun LeadDetailHeader(
    lead: Lead,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    context: android.content.Context,
    onAddTask: () -> Unit = {},
    onEmailClick: (email: String, name: String) -> Unit = { _, _ -> },
    isDeleting: Boolean = false,
    onDeleteClick: () -> Unit = {},
    onConvertClick: () -> Unit = {},
    isConverting: Boolean = false,
    onFilesClick: () -> Unit = {},


) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Forest)
            .padding(top = 8.dp, bottom = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CrmSurface)
            }
            Text(
                text = "Lead Detail",
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = CrmSurface,
            )
            // Convert Lead → Account/Contact/Opportunity
            if (isConverting) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 4.dp, top = 12.dp).size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                IconButton(onClick = onConvertClick) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = "Convert Lead", tint = CrmSurface)
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CrmSurface)
            }
            // Delete button
            if (isDeleting) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 12.dp, top = 12.dp).size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Lead", tint = CrmSurface)
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LeadAvatar(lead.fullName, size = 56, background = Color(0xFF9E9E9E))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lead.fullName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = CrmSurface,
                )
                val subtitle = buildString {
                    if (lead.title.isNotBlank()) append(lead.title)
                    if (lead.title.isNotBlank() && lead.company.isNotBlank()) append(" · ")
                    if (lead.company.isNotBlank()) append(lead.company)
                }
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = CrmSurface.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                StatusBadge(lead.status, modifier = Modifier.padding(top = 6.dp))
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            QuickActionButton("Call", Icons.Default.Phone, Modifier.weight(1f)) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${lead.phone}"))
                context.startActivity(intent)
            }
            QuickActionButton("Email", Icons.Default.Email, Modifier.weight(1f)) {
                if (lead.email.isNotBlank()) {
                    onEmailClick(lead.email, lead.fullName)
                }
            }

            QuickActionButton("+ Task", Icons.Default.CheckBoxOutlineBlank, Modifier.weight(1f)) {
                onAddTask()
            }
            QuickActionButton("Files", Icons.Default.AttachFile, Modifier.weight(1f)) {
                onFilesClick()
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, tint = CrmSurface, modifier = Modifier.size(18.dp))
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = CrmSurface, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun LeadDetailTabs(
    selectedTab: LeadDetailTab,
    taskCount: Int,
    eventCount: Int,
    onTabSelected: (LeadDetailTab) -> Unit,
) {
    val tabs = listOf(
        LeadDetailTab.Info to "Info",
        LeadDetailTab.Tasks to "Tasks ($taskCount)",
    )

    val selectedIndex = tabs.indexOfFirst { it.first == selectedTab }.coerceAtLeast(0)
    TabRow(
        selectedTabIndex = selectedIndex,
        containerColor = CrmSurface,
        contentColor = Forest,
        indicator = { tabPositions ->
            if (selectedIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    color = Forest,
                    height = 3.dp,
                )
            }
        },
        divider = {},
    ) {
        tabs.forEachIndexed { index, (tab, label) ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedIndex == index) Forest else TextMuted,
                    )
                },
            )
        }
    }
}

@Composable
private fun LeadInfoTab(lead: Lead) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            // Single consolidated card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CrmSurface)
                    .border(1.dp, BorderGreen, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "LEAD INFORMATION",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted,
                    letterSpacing = 0.5.sp,
                )
                InfoRow("Email", lead.email.ifBlank { "—" }, Icons.Default.Email)
                InfoRow("Phone", lead.phone.ifBlank { "—" }, Icons.Default.Phone)
                InfoRow("Company", lead.company.ifBlank { "—" }, Icons.Default.Business)
                InfoRow("Source", lead.source.ifBlank { "—" }, Icons.Default.Info)
                InfoRow("Industry", lead.industry.ifBlank { "—" }, Icons.Default.Info)
                InfoRow("Rating", lead.rating.ifBlank { "—" }, Icons.Default.Star)
            }
        }
        item {
            // Notes separate block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CrmSurface)
                    .border(1.dp, BorderGreen, RoundedCornerShape(14.dp))
                    .padding(14.dp),
            ) {
                Text("NOTES", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, letterSpacing = 0.5.sp)
                Text(
                    lead.description.ifBlank { "No notes added." },
                    fontSize = 14.sp,
                    color = Charcoal,
                    modifier = Modifier.padding(top = 6.dp),
                    lineHeight = 20.sp,
                )
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun InfoRow(label: String, value: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MutedGreen, modifier = Modifier.size(14.dp))
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(label, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 13.sp, color = Charcoal, fontWeight = FontWeight.SemiBold)
        }
    }
}

//@Composable
//private fun InfoFieldCard(label: String, value: String, icon: ImageVector) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clip(RoundedCornerShape(14.dp))
//            .background(CrmSurface)
//            .border(1.dp, BorderGreen, RoundedCornerShape(14.dp))
//            .padding(14.dp),
//        verticalAlignment = Alignment.CenterVertically,
//    ) {
//        Box(
//            modifier = Modifier
//                .size(36.dp)
//                .clip(RoundedCornerShape(10.dp))
//                .background(CardGreen),
//            contentAlignment = Alignment.Center,
//        ) {
//            Icon(icon, contentDescription = null, tint = Forest, modifier = Modifier.size(18.dp))
//        }
//        Column(modifier = Modifier.padding(start = 12.dp)) {
//            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, letterSpacing = 0.5.sp)
//            Text(
//                text = value,
//                fontSize = 14.sp,
//                fontWeight = FontWeight.Bold,
//                color = Charcoal,
//                modifier = Modifier.padding(top = 2.dp),
//            )
//        }
//    }
//}

//@Composable
//private fun NotesCard(notes: String) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clip(RoundedCornerShape(14.dp))
//            .background(CrmSurface)
//            .border(1.dp, BorderGreen, RoundedCornerShape(14.dp))
//            .padding(14.dp),
//    ) {
//        Text(text = "NOTES", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, letterSpacing = 0.5.sp)
//        Text(
//            text = notes,
//            fontSize = 14.sp,
//            color = Charcoal,
//            modifier = Modifier.padding(top = 6.dp),
//            lineHeight = 20.sp,
//        )
//    }
//}

@Composable
private fun LeadTasksTab(tasks: List<LeadTask>) {
    if (tasks.isEmpty()) {
        EmptyTabState(
            icon = Icons.Default.CheckBoxOutlineBlank,
            message = "No tasks yet",
            buttonLabel = "+ Add Task",
        )
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(tasks, key = { it.id }) { task ->
            TaskCard(task)
        }
        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun TaskCard(task: LeadTask) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CrmSurface)
            .border(1.dp, BorderGreen, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .border(2.dp, if (task.completed) MidGreen else BorderGreen, CircleShape)
                .background(if (task.completed) MidGreen else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            if (task.completed) {
                Text("✓", color = CrmSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = task.subject,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Charcoal,
                textDecoration = if (task.completed) TextDecoration.LineThrough else null,
            )
            Row(
                modifier = Modifier.padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PriorityBadge(task.priority)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                    Text(text = task.dueDate, fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(start = 3.dp))
                }
            }
        }
    }
}

@Composable
private fun PriorityBadge(priority: String) {
    val (bg, text) = when (priority.lowercase()) {
        "high" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        else -> Color(0xFFFFF8E1) to Color(0xFFF57F17)
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(text))
        Text(text = priority, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = text, modifier = Modifier.padding(start = 4.dp))
    }
}

@Composable
private fun LeadEventsTab(events: List<LeadEvent>) {
    if (events.isEmpty()) {
        EmptyTabState(
            icon = Icons.Default.CalendarMonth,
            message = "No events scheduled",
            buttonLabel = "+ Add Event",
        )
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(events, key = { it.id }) { event ->
            EventCard(event)
        }
        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun EventCard(event: LeadEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CrmSurface)
            .border(1.dp, BorderGreen, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(CardGreen),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Forest, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(text = event.subject, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Charcoal)
            Text(text = event.timeRange, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                Text(text = event.location, fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(start = 3.dp))
            }
        }
    }
}

@Composable
private fun EmptyTabState(icon: ImageVector, message: String, buttonLabel: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = MutedGreen.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
        Text(text = message, fontSize = 14.sp, color = TextMuted, modifier = Modifier.padding(top = 12.dp))
        Button(
            onClick = { },
            modifier = Modifier.padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Forest),
            shape = RoundedCornerShape(24.dp),
        ) {
            Text(buttonLabel, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}