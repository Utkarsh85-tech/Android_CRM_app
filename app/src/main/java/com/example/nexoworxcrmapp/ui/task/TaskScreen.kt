// REPLACE TaskScreen.kt entirely
// app/src/main/java/com/example/nexoworxcrmapp/ui/task/TaskScreen.kt

package com.example.nexoworxcrmapp.ui.task

import android.os.Build
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexoworxcrmapp.data.Task
import com.example.nexoworxcrmapp.data.TASK_PRIORITIES
import com.example.nexoworxcrmapp.data.TASK_STATUSES
import com.example.nexoworxcrmapp.ui.theme.BorderGreen
import com.example.nexoworxcrmapp.ui.theme.Charcoal
import com.example.nexoworxcrmapp.ui.theme.CrmBg
import com.example.nexoworxcrmapp.ui.theme.CrmSurface
import com.example.nexoworxcrmapp.ui.theme.Forest
import com.example.nexoworxcrmapp.ui.theme.MidGreen
import com.example.nexoworxcrmapp.ui.theme.MutedGreen
import com.example.nexoworxcrmapp.ui.theme.TextMuted
import com.example.nexoworxcrmapp.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    modifier: Modifier = Modifier,
    showHeader: Boolean = true,
    showCategoryFilter: Boolean = false, // true only in Tasks tab
    viewModel: TaskViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedStatusFilter by remember { mutableStateOf("Open") }

    // Delete confirmation dialog
    if (uiState.deletingTaskId != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Delete Task", fontWeight = FontWeight.Bold) },
            text = { Text("This task will be permanently deleted.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0392B)),
                    shape = RoundedCornerShape(10.dp),
                ) { Text("Delete", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) {
                    Text("Cancel", color = Forest)
                }
            },
            containerColor = CrmSurface,
            shape = RoundedCornerShape(16.dp),
        )
    }

    // Create / Edit bottom sheet
    if (uiState.showSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeSheet() },
            sheetState = sheetState,
            containerColor = CrmSurface,
        ) {
            TaskEditSheet(uiState = uiState, viewModel = viewModel)
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = CrmBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openCreateSheet() },
                containerColor = Forest,
                contentColor = CrmSurface,
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Task")
            }
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            Column(modifier = Modifier.fillMaxSize().background(CrmBg)) {

                if (showHeader) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Spacer(Modifier.height(8.dp))
                        Text("NEXOWORX CRM", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MutedGreen, letterSpacing = 0.5.sp)
                        Text("Tasks", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Charcoal)
                        Spacer(Modifier.height(12.dp))
                    }
                }

                // Category filter — only shown in Tasks tab
                if (showCategoryFilter) {
                    CategoryFilterRow(
                        selected = uiState.selectedCategory,
                        onCategoryChange = { viewModel.selectCategory(it) },
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // Status filter tabs
                StatusFilterRow(
                    selected = selectedStatusFilter,
                    onFilterChange = { selectedStatusFilter = it },
                )

                Spacer(Modifier.height(8.dp))

                // Error
                val err = uiState.errorMessage
                if (err != null) {
                    Text(
                        text = err,
                        fontSize = 11.sp,
                        color = Color(0xFFC0392B),
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { viewModel.refresh() },
                    )
                }

                // Apply both category and status filters
                val categoryFiltered = viewModel.filteredTasks()
                val today = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    java.time.LocalDate.now().toString() else ""
                val displayTasks = when (selectedStatusFilter) {
                    "Open" -> categoryFiltered.filter { !it.isCompleted }
                    "Completed" -> categoryFiltered.filter { it.isCompleted }
                    "Due Today" -> categoryFiltered.filter { it.dueDate == today && !it.isCompleted }
                    "Overdue" -> categoryFiltered.filter { it.dueDate.isNotBlank() && it.dueDate < today && !it.isCompleted }
                    else -> categoryFiltered
                }

                if (displayTasks.isEmpty() && !uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MutedGreen.copy(alpha = 0.3f), modifier = Modifier.size(56.dp))
                            Text("No tasks found", fontSize = 14.sp, color = TextMuted, modifier = Modifier.padding(top = 8.dp))
                            TextButton(onClick = { viewModel.openCreateSheet() }) {
                                Text("+ Add Task", color = Forest, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(displayTasks, key = { it.id }) { task ->
                            val parentName = if (showCategoryFilter) viewModel.parentNameForTask(task) else ""
                            TaskCard(
                                task = task,
                                parentName = parentName,
                                onEdit = { viewModel.openEditSheet(task) },
                                onDelete = { viewModel.requestDelete(task.id) },
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

// ── Category filter (All / Leads / Accounts / Opportunities) ─────────────────

@Composable
private fun CategoryFilterRow(
    selected: TaskCategory,
    onCategoryChange: (TaskCategory) -> Unit,
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TaskCategory.entries.forEach { category ->
            val isSelected = selected == category
            val label = when (category) {
                TaskCategory.All -> "All"
                TaskCategory.Leads -> "Leads"
                TaskCategory.Accounts -> "Accounts"
                TaskCategory.Opportunities -> "Deals"
            }
            Text(
                text = label,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) Forest else CrmSurface)
                    .border(1.5.dp, if (isSelected) Forest else BorderGreen, RoundedCornerShape(20.dp))
                    .clickable { onCategoryChange(category) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) CrmSurface else TextSecondary,
            )
        }
    }
}

// ── Status filter (Open / Due Today / Overdue / Completed) ───────────────────

@Composable
private fun StatusFilterRow(
    selected: String,
    onFilterChange: (String) -> Unit,
) {
    val filters = listOf("Open", "Due Today", "Overdue", "Completed")
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        filters.forEach { filter ->
            val isSelected = selected == filter
            Text(
                text = filter,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) MidGreen else CrmSurface)
                    .border(1.5.dp, if (isSelected) MidGreen else BorderGreen, RoundedCornerShape(20.dp))
                    .clickable { onFilterChange(filter) }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) CrmSurface else TextSecondary,
            )
        }
    }
}

// ── Task card ─────────────────────────────────────────────────────────────────

@Composable
private fun TaskCard(
    task: Task,
    parentName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CrmSurface)
            .border(1.dp, BorderGreen, RoundedCornerShape(14.dp))
            .clickable { expanded = !expanded }
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Completion circle
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .border(2.dp, if (task.isCompleted) MidGreen else BorderGreen, CircleShape)
                    .background(if (task.isCompleted) MidGreen else Color.Transparent),
                contentAlignment = Alignment.Center,
            ) {
                if (task.isCompleted) {
                    Text("✓", color = CrmSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(
                    text = task.subject,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 2,
                )
                // Parent name (Lead/Account/Opportunity)
                if (parentName.isNotBlank()) {
                    Text(
                        text = parentName,
                        fontSize = 11.sp,
                        color = MutedGreen,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PriorityBadge(task.priority)
                    if (task.dueDate.isNotBlank()) {
                        Text(task.dueDate, fontSize = 11.sp, color = TextMuted)
                    }
                    Text(task.status, fontSize = 11.sp, color = TextSecondary)
                }
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MutedGreen, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFC0392B).copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
            }
        }

        // Expandable description
        if (expanded && task.description.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = task.description,
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CrmBg)
                    .padding(10.dp),
                lineHeight = 18.sp,
            )
        }
    }
}

// ── Priority badge ────────────────────────────────────────────────────────────

@Composable
private fun PriorityBadge(priority: String) {
    val (bg, fg) = when (priority.lowercase()) {
        "high" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        "low" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        else -> Color(0xFFFFF8E1) to Color(0xFFF57F17)
    }
    Text(
        text = priority,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = fg,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 7.dp, vertical = 2.dp),
    )
}

// ── Edit / Create bottom sheet ────────────────────────────────────────────────

@Composable
private fun TaskEditSheet(uiState: TaskUiState, viewModel: TaskViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (uiState.editingTask == null) "New Task" else "Edit Task",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Charcoal,
            )
            Row {
                if (uiState.sheetSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Forest, strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = { viewModel.saveTask() }) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = Forest)
                    }
                }
                IconButton(onClick = { viewModel.closeSheet() }) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }
        }

        SheetField("SUBJECT *", uiState.sheetSubject, "e.g. Send proposal", viewModel::onSubjectChange)
        SheetDropdown("PRIORITY", uiState.sheetPriority, TASK_PRIORITIES, viewModel::onPriorityChange)
        SheetDropdown("STATUS", uiState.sheetStatus, TASK_STATUSES, viewModel::onStatusChange)
        SheetField("DUE DATE (YYYY-MM-DD)", uiState.sheetDueDate, "e.g. 2026-12-31", viewModel::onDueDateChange)
        SheetField("DESCRIPTION", uiState.sheetDescription, "Notes about this task…", viewModel::onDescriptionChange, minLines = 3)

        val err = uiState.sheetError
        if (err != null) {
            Text(
                text = err,
                fontSize = 12.sp,
                color = Color(0xFFC0392B),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFFEBEE))
                    .padding(12.dp),
            )
        }
    }
}

@Composable
private fun SheetField(label: String, value: String, placeholder: String, onValueChange: (String) -> Unit, minLines: Int = 1) {
    Column {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, letterSpacing = 0.5.sp, modifier = Modifier.padding(bottom = 4.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CrmBg)
                .border(1.dp, BorderGreen, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            textStyle = TextStyle(fontSize = 13.sp, color = Charcoal),
            cursorBrush = SolidColor(Forest),
            minLines = minLines,
            decorationBox = { inner ->
                if (value.isEmpty()) Text(placeholder, fontSize = 13.sp, color = TextMuted)
                inner()
            },
        )
    }
}

@Composable
private fun SheetDropdown(label: String, selected: String, options: List<String>, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, letterSpacing = 0.5.sp, modifier = Modifier.padding(bottom = 4.dp))
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CrmBg)
                    .border(1.dp, BorderGreen, RoundedCornerShape(12.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(selected, fontSize = 13.sp, color = Charcoal)
                Text("▾", fontSize = 12.sp, color = MutedGreen)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, fontSize = 13.sp) },
                        onClick = { onSelected(option); expanded = false },
                    )
                }
            }
        }
    }
}
