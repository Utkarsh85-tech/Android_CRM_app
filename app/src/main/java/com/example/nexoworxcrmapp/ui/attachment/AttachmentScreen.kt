package com.example.nexoworxcrmapp.ui.attachment

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexoworxcrmapp.data.attachment.AttachmentItem
import com.example.nexoworxcrmapp.network.NetworkModule
import com.example.nexoworxcrmapp.ui.theme.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
@Composable
fun AttachmentScreen(
    onBack: () -> Unit,
    viewModel: AttachmentViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    // File picker launcher
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val (name, bytes) = readFileFromUri(context, uri) ?: return@rememberLauncherForActivityResult
        viewModel.onFilePicked(name, bytes)
    }

    // File size warning dialog
    if (state.fileSizeWarning) {
        val mb = String.format("%.1f", (state.pendingFileBytes?.size ?: 0) / (1024.0 * 1024.0))
        AlertDialog(
            onDismissRequest = viewModel::dismissSizeWarning,
            title = { Text("Large File Warning", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This file is ${mb}MB which exceeds the recommended 5MB limit. " +
                            "Large files may take longer to upload or fail. Continue anyway?",
                )
            },
            confirmButton = {
                Button(
                    onClick = viewModel::confirmLargeUpload,
                    colors = ButtonDefaults.buttonColors(containerColor = Forest),
                    shape = RoundedCornerShape(10.dp),
                ) { Text("Upload Anyway") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissSizeWarning) {
                    Text("Cancel", color = Forest)
                }
            },
            containerColor = CrmSurface,
            shape = RoundedCornerShape(16.dp),
        )
    }



    // Error dialog
    if (state.errorMessage != null) {
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text("Error", fontWeight = FontWeight.Bold) },
            text = { Text(state.errorMessage.orEmpty()) },
            confirmButton = {
                TextButton(onClick = viewModel::clearError) { Text("OK", color = Forest) }
            },
            containerColor = CrmSurface,
            shape = RoundedCornerShape(16.dp),
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(CrmBg)) {

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Forest)
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CrmSurface)
            }
            Text(
                text = "Notes & Attachments",
                modifier = Modifier.weight(1f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = CrmSurface,
            )
            IconButton(onClick = { filePicker.launch("*/*") }) {
                Icon(Icons.Default.Upload, contentDescription = "Upload File", tint = CrmSurface)
            }
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Forest)
            }
            state.attachments.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = MutedGreen,
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("No files attached yet", color = TextMuted, fontSize = 15.sp)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { filePicker.launch("*/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Forest),
                        shape = RoundedCornerShape(10.dp),
                    ) { Text("Upload File") }
                }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.attachments, key = { it.id }) { item ->
                    AttachmentCard(
                        item = item,
                        onOpen = {
                            scope.launch {
                                try {
                                    val token = NetworkModule.authManager.getAccessToken()
                                    downloadFile(context, item, token)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Can't download while offline",
                                        android.widget.Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            }
                        },
                        onDelete = { viewModel.deleteFile(item.contentDocumentId) },
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun AttachmentCard(
    item: AttachmentItem,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete File", fontWeight = FontWeight.Bold) },
            text = { Text("Delete \"${item.title}\"? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = { showDeleteDialog = false; onDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0392B)),
                    shape = RoundedCornerShape(10.dp),
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel", color = Forest) }
            },
            containerColor = CrmSurface,
            shape = RoundedCornerShape(16.dp),
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CrmSurface)
            .border(1.dp, BorderGreen, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = fileIcon(item.fileExtension),
            contentDescription = null,
            tint = Forest,
            modifier = Modifier.size(36.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${item.title}.${item.fileExtension}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Charcoal,
            )
            Text(
                text = formatSize(item.contentSize) + " • " + formatDate(item.lastModifiedDate),
                fontSize = 11.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        if (item.isPending) {
            Text(
                text = "Pending",
                fontSize = 10.sp,
                color = Color(0xFFB8860B),
                modifier = Modifier
                    .background(Color(0xFFFFF3CD), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
            Spacer(Modifier.width(6.dp))
        }

        IconButton(onClick = onOpen, modifier = Modifier.size(36.dp)) {
        Icon(Icons.Default.Download, contentDescription = "Download", tint = Forest, modifier = Modifier.size(18.dp))
    }
        IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFC0392B), modifier = Modifier.size(18.dp))
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun fileIcon(ext: String): ImageVector = when (ext.lowercase()) {
    "pdf" -> Icons.Default.PictureAsPdf
    "jpg", "jpeg", "png", "gif", "webp" -> Icons.Default.Image
    "doc", "docx" -> Icons.Default.Article
    "xls", "xlsx" -> Icons.Default.TableChart
    "ppt", "pptx" -> Icons.Default.Slideshow
    "zip", "rar" -> Icons.Default.FolderZip
    "mp4", "mov", "avi" -> Icons.Default.VideoFile
    "mp3", "wav" -> Icons.Default.AudioFile
    else -> Icons.Default.InsertDriveFile
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 -> String.format("%.1fMB", bytes / (1024.0 * 1024.0))
        bytes >= 1024 -> String.format("%.0fKB", bytes / 1024.0)
        else -> "${bytes}B"
    }
}

private fun formatDate(isoDate: String): String {
    return try {
        // isoDate format: "2026-07-06T19:53:00.000+0000"
        isoDate.substring(0, 10) // gives "2026-07-06"
            .split("-")
            .let { (y, m, d) ->
                val month = listOf("Jan","Feb","Mar","Apr","May","Jun",
                    "Jul","Aug","Sep","Oct","Nov","Dec")[m.toInt() - 1]
                "$month $d, $y"
            }
    } catch (e: Exception) { isoDate.take(10) }
}

private fun downloadFile(context: Context, item: AttachmentItem, token: String) {
    val instanceUrl = com.example.nexoworxcrmapp.network.NetworkModule.instanceUrl
    val url = "${instanceUrl}services/data/v66.0/sobjects/ContentVersion/${item.downloadVersionId}/VersionData"

    val request = android.app.DownloadManager.Request(Uri.parse(url)).apply {
        addRequestHeader("Authorization", "Bearer $token")
        setTitle(item.title)
        setDescription("Downloading...")
        setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        setDestinationInExternalPublicDir(
            android.os.Environment.DIRECTORY_DOWNLOADS,
            "${item.title}.${item.fileExtension}",
        )
        setMimeType("*/*")
    }
    val dm = context.getSystemService(android.content.Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
    dm.enqueue(request)

    android.widget.Toast.makeText(context, "Downloading ${item.title}...", android.widget.Toast.LENGTH_SHORT).show()
}
private fun readFileFromUri(context: Context, uri: Uri): Pair<String, ByteArray>? {
    return try {
        val name = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(idx)
        } ?: "file"
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        Pair(name, bytes)
    } catch (e: Exception) { null }
}