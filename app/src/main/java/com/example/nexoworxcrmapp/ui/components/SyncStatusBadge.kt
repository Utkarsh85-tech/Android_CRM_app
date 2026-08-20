package com.example.nexoworxcrmapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class SyncState { SYNCED, PENDING, FAILED }

fun syncStateFor(id: String, pendingIds: Set<String>, failedIds: Set<String>): SyncState = when {
    id in failedIds -> SyncState.FAILED
    id in pendingIds -> SyncState.PENDING
    else -> SyncState.SYNCED
}

@Composable
fun SyncStatusBadge(state: SyncState, modifier: Modifier = Modifier) {
    if (state == SyncState.SYNCED) return
    val (label, bg, fg) = when (state) {
        SyncState.PENDING -> Triple("Pending", Color(0xFFFFF3CD), Color(0xFFB8860B))
        SyncState.FAILED -> Triple("Failed", Color(0xFFFADBD8), Color(0xFFC0392B))
        SyncState.SYNCED -> return
    }
    Text(
        text = label,
        fontSize = 10.sp,
        color = fg,
        modifier = modifier
            .background(bg, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}