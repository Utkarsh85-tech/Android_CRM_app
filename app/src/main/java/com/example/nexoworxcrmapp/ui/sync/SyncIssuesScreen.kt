package com.example.nexoworxcrmapp.ui.sync

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexoworxcrmapp.data.sync.SyncIssue

@Composable
fun SyncIssuesScreen(onBack: () -> Unit, viewModel: SyncIssuesViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Text("Sync issues", style = MaterialTheme.typography.titleLarge)
        }
        if (state.issues.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nothing stuck — everything's synced")
            }
        } else {
            LazyColumn(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.issues, key = { it.opId }) { issue ->
                    SyncIssueCard(issue, onRetry = { viewModel.retry(issue.opId) }, onDiscard = { viewModel.discard(issue.opId) })
                }
            }
        }
    }
}

@Composable
private fun SyncIssueCard(issue: SyncIssue, onRetry: () -> Unit, onDiscard: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            val statusLabel = if (issue.retryCount >= com.example.nexoworxcrmapp.data.local.entity.MAX_SYNC_RETRIES) "Failed" else "Pending"
            Text("${issue.entityType} — ${issue.operationType} · $statusLabel", style = MaterialTheme.typography.titleSmall)
            Text(issue.lastError ?: "Waiting to sync", style = MaterialTheme.typography.bodySmall)
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDiscard) { Text("Discard") }
                Button(onClick = onRetry) { Text("Retry") }
            }
        }
    }
}