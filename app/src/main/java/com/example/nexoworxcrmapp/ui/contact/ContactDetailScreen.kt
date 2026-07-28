// Step 12 of 15
// NEW FILE: app/src/main/java/com/example/nexoworxcrmapp/ui/contact/ContactDetailScreen.kt

package com.example.nexoworxcrmapp.ui.contact

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.nexoworxcrmapp.data.Contact
import com.example.nexoworxcrmapp.ui.theme.BorderGreen
import com.example.nexoworxcrmapp.ui.theme.CardGreen
import com.example.nexoworxcrmapp.ui.theme.Charcoal
import com.example.nexoworxcrmapp.ui.theme.CrmBg
import com.example.nexoworxcrmapp.ui.theme.CrmSurface
import com.example.nexoworxcrmapp.ui.theme.Forest
import com.example.nexoworxcrmapp.ui.theme.MidGreen
import com.example.nexoworxcrmapp.ui.theme.TextMuted
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext

@Composable
fun ContactDetailScreen(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ContactDetailViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.deleteState) {
        if (uiState.deleteState is ContactDeleteState.Deleted) onDeleted()
    }

    // Confirm delete dialog
    if (uiState.deleteState is ContactDeleteState.Confirming) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Delete Contact", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "\"${uiState.contact?.fullName}\" will be permanently deleted. This cannot be undone.",
                    fontSize = 14.sp,
                )
            },
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

    // Delete error dialog
    if (uiState.deleteState is ContactDeleteState.Error) {
        val msg = (uiState.deleteState as ContactDeleteState.Error).message
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteError() },
            title = { Text("Delete Failed", fontWeight = FontWeight.Bold) },
            text = { Text(msg, fontSize = 14.sp) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissDeleteError() }) {
                    Text("OK", color = Forest)
                }
            },
            containerColor = CrmSurface,
            shape = RoundedCornerShape(16.dp),
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CrmBg),
    ) {
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Forest)
            }

            uiState.errorMessage != null -> Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(uiState.errorMessage.orEmpty(), color = Color(0xFFC0392B), fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.load() },
                    colors = ButtonDefaults.buttonColors(containerColor = Forest),
                ) { Text("Retry") }
            }

            uiState.contact != null -> {
                val contact = uiState.contact!!
                val isDeleting = uiState.deleteState is ContactDeleteState.Deleting

                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(Forest, MidGreen))
                        )
                        .padding(top = 8.dp, bottom = 20.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CrmSurface)
                        }
                        Text(
                            text = "Contact",
                            modifier = Modifier.weight(1f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CrmSurface,
                        )
                        if (isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp).padding(end = 12.dp),
                                color = CrmSurface,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            IconButton(onClick = { viewModel.requestDelete() }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CrmSurface.copy(alpha = 0.85f))
                            }
                        }
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CrmSurface)
                        }
                    }

                    // Avatar + name
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                        ) {
                            Text(
                                contact.initials,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(contact.fullName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        if (contact.title.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(contact.title, color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
                        }
                    }
                }

                // Body
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (contact.phone.isNotBlank()) {
                        item { ContactInfoCard("PHONE", contact.phone, Icons.Default.Phone,
                            onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))) }
                        ) }                    }
                    if (contact.email.isNotBlank()) {
                        item { ContactInfoCard("EMAIL", contact.email, Icons.Default.Email,
                            onClick = { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${contact.email}"))) }
                        ) }                    }
                    if (contact.accountName.isNotBlank()) {
                        item { ContactInfoCard("ACCOUNT", contact.accountName, Icons.Default.Business) }
                    }
                    if (contact.department.isNotBlank()) {
                        item { ContactInfoCard("DEPARTMENT", contact.department, Icons.Default.Person) }
                    }
                    if (contact.description.isNotBlank()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(CrmSurface)
                                    .border(1.dp, BorderGreen, RoundedCornerShape(14.dp))
                                    .padding(14.dp),
                            ) {
                                Text("NOTES", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, letterSpacing = 0.5.sp)
                                Text(contact.description, fontSize = 14.sp, color = Charcoal, modifier = Modifier.padding(top = 6.dp), lineHeight = 20.sp)
                            }
                        }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ContactInfoCard(label: String, value: String, icon: ImageVector, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))

            .background(CrmSurface)
            .border(1.dp, BorderGreen, RoundedCornerShape(14.dp))
            .padding(14.dp)
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        verticalAlignment = Alignment.CenterVertically,

    )
    {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(CardGreen),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Forest, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, letterSpacing = 0.5.sp)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Charcoal, modifier = Modifier.padding(top = 2.dp))
        }
    }
}
