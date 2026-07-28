package com.example.nexoworxcrmapp.ui.account

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexoworxcrmapp.data.Account
import com.example.nexoworxcrmapp.ui.theme.CrmBg
import com.example.nexoworxcrmapp.ui.theme.CrmSurface
import com.example.nexoworxcrmapp.ui.theme.Forest
import com.example.nexoworxcrmapp.ui.theme.MidGreen
import com.example.nexoworxcrmapp.ui.theme.TextMuted
import com.example.nexoworxcrmapp.ui.theme.TextSecondary
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import com.example.nexoworxcrmapp.ui.theme.BorderGreen
import com.example.nexoworxcrmapp.ui.theme.CardGreen

@Composable
fun AccountDetailScreen(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onAddTask: () -> Unit = {},
    onFilesClick: () -> Unit = {},
    onContractClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    refreshTrigger: Boolean = false,
    onRefreshConsumed: () -> Unit = {},
    viewModel: AccountDetailViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Reload after returning from edit screen
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger) {
            viewModel.loadAccount()
            onRefreshConsumed()
        }
    }

    // After successful delete, go back to the list
    LaunchedEffect(state.deleteSuccess) {
        if (state.deleteSuccess) {
            viewModel.consumeDeleteSuccess()
            onBack()
        }
    }

    // Show delete error as a snackbar or inline — for now a simple dialog
    LaunchedEffect(state.deleteErrorMessage) {
        if (state.deleteErrorMessage != null) {
            // Error is set — dialog below will show it
        }
    }

    if (state.deleteErrorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearDeleteError() },
            title = { Text("Delete Failed") },
            text = { Text(state.deleteErrorMessage ?: "Unknown error") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearDeleteError() }) {
                    Text("OK")
                }
            },
        )
    }

    // Confirmation dialog before deleting
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to permanently delete this account? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteAccount()
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
        // ── Header ──────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Forest, MidGreen)))
                .padding(top = 16.dp, bottom = 24.dp),
        ) {
            // Back button — top left
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart).padding(start = 4.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            // Edit + Delete buttons — top right
            Row(modifier = Modifier.align(Alignment.TopEnd)) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                }
                if (state.isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 12.dp, top = 12.dp).size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                    }
                }
            }

            // Avatar + name centred
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                ) {
                    Text(state.account?.initials ?: "?", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                Text(state.account?.name ?: "", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                if (state.account?.type?.isNotBlank() == true) {
                    Spacer(Modifier.height(4.dp))
                    Text(state.account!!.type, color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
                }
            }
        }

        // ── Loading / Error / Content ────────────────────────────────────────
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Forest)
            }
            state.errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.errorMessage ?: "Error", color = TextMuted, fontSize = 14.sp)
            }
            state.account != null -> AccountInfoContent(
                account = state.account!!,
                context = context,
                onAddTask = onAddTask,
                onFilesClick = onFilesClick,
                onContractClick = onContractClick,
            )
        }
    }
}

@Composable
private fun AccountInfoContent(account: Account, context: android.content.Context, onAddTask: () -> Unit, onFilesClick: () -> Unit, onContractClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            QuickActionButton("+ Task", Icons.Default.CheckBoxOutlineBlank, Modifier.weight(1f)) {
                onAddTask()
            }
            QuickActionButton("Files", Icons.Default.AttachFile, Modifier.weight(1f)) {
                onFilesClick()
            }
            QuickActionButton("Contracts", Icons.Default.Article, Modifier.weight(1f)) {                onContractClick()
            }
        }
        InfoCard {
            if (account.phone.isNotBlank()) InfoRow(
                Icons.Default.Phone, "Phone", account.phone,
                onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${account.phone}"))) }
            )
            if (account.industry.isNotBlank()) InfoRow(Icons.Default.Business, "Industry", account.industry)
            val location = listOf(account.billingCity, account.billingCountry)
                .filter { it.isNotBlank() }.joinToString(", ")
            if (location.isNotBlank()) InfoRow(Icons.Default.LocationOn, "Location", location)
            if (account.website.isNotBlank()) InfoRow(
                Icons.Default.Language, "Website", account.website,
                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(account.website))) }
            )        }
        if (account.description.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            InfoCard {
                Text("Description", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 4.dp))
                Text(account.description, fontSize = 14.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun InfoCard(content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(CrmSurface).padding(16.dp)) {
        content()
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String, onClick: (() -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier) ) {
        Icon(icon, contentDescription = label, tint = Forest, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = TextMuted)
            Text(value, fontSize = 14.sp, color = TextSecondary)
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
            .background(CardGreen)
            .border(1.dp, BorderGreen, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, tint = Forest, modifier = Modifier.size(18.dp))
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Forest, modifier = Modifier.padding(top = 4.dp))
    }
}
