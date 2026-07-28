package com.example.nexoworxcrmapp.ui.opportunity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexoworxcrmapp.data.Opportunity
import com.example.nexoworxcrmapp.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun OpportunityDetailScreen(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit,
    onAddTask: () -> Unit = {},
    onFilesClick: () -> Unit = {},
    onQuoteClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: OpportunityDetailViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.deleteState) {
        if (uiState.deleteState is DeleteState.Deleted) onDeleted()
    }

    if (uiState.deleteState is DeleteState.Confirming) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Delete Opportunity", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("\"${uiState.opportunity?.name}\" will be permanently deleted.", fontSize = 14.sp, color = Charcoal) },
            confirmButton = {
                Button(onClick = { viewModel.confirmDelete() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0392B)), shape = RoundedCornerShape(10.dp)) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { viewModel.cancelDelete() }) { Text("Cancel", color = Forest) } },
            containerColor = CrmSurface, shape = RoundedCornerShape(16.dp),
        )
    }

    if (uiState.deleteState is DeleteState.Error) {
        val msg = (uiState.deleteState as DeleteState.Error).message
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteError() },
            title = { Text("Delete Failed", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text(msg, fontSize = 14.sp, color = Charcoal) },
            confirmButton = { TextButton(onClick = { viewModel.dismissDeleteError() }) { Text("OK", color = Forest) } },
            containerColor = CrmSurface, shape = RoundedCornerShape(16.dp),
        )
    }

    Column(modifier = modifier.fillMaxSize().background(CrmBg)) {
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
                Button(onClick = { viewModel.load() }, colors = ButtonDefaults.buttonColors(containerColor = Forest)) { Text("Retry") }
            }
            uiState.opportunity != null -> {
                val opp = uiState.opportunity!!
                val isDeleting = uiState.deleteState is DeleteState.Deleting
                val fmt = NumberFormat.getCurrencyInstance(Locale.US)

                // App bar
                Row(
                    modifier = Modifier.fillMaxWidth().background(Forest).padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CrmSurface)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Opportunity", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CrmSurface)
                        if (opp.accountName.isNotBlank()) Text(opp.accountName, fontSize = 11.sp, color = CrmSurface.copy(alpha = 0.7f))
                    }
                    if (isDeleting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CrmSurface, strokeWidth = 2.dp)
                    else IconButton(onClick = { viewModel.requestDelete() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CrmSurface.copy(alpha = 0.85f))
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CrmSurface)
                    }
                }

                LazyColumn(modifier = Modifier.weight(1f).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Hero card — matches HTML .opp-hero
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Forest).padding(16.dp),
                        ) {
                            Text(opp.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = CrmSurface)
                            if (opp.accountName.isNotBlank()) Text(opp.accountName, fontSize = 12.sp, color = CrmSurface.copy(alpha = 0.7f), modifier = Modifier.padding(top = 3.dp))
                            if (opp.amount != null) {
                                Text(fmt.format(opp.amount), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CrmSurface, modifier = Modifier.padding(top = 10.dp))
                                Text("Expected amount · Close ${opp.closeDate}", fontSize = 10.sp, color = CrmSurface.copy(alpha = 0.7f))
                            }
                        }
                    }

                    // Info card — matches HTML .card.mint
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                .background(Mint).border(1.dp, BorderGreen, RoundedCornerShape(12.dp)).padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            InfoKv("Stage", opp.stageName)
                            if (opp.probability != null) InfoKv("Probability", "${opp.probability.toInt()}%")
                            if (opp.type.isNotBlank()) InfoKv("Type", opp.type)
                            if (opp.leadSource.isNotBlank()) InfoKv("Lead Source", opp.leadSource)
                            if (opp.deliveryInstallationStatus.isNotBlank()) InfoKv("Delivery Status", opp.deliveryInstallationStatus)
                        }
                    }

                    // Next step / description
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("NEXT STEP", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, letterSpacing = 0.5.sp, modifier = Modifier.padding(bottom = 6.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                    .background(CrmSurface).border(1.dp, BorderGreen, RoundedCornerShape(12.dp)).padding(12.dp),
                            ) {
                                Text(
                                    opp.description.ifBlank { "No active quote on this opportunity yet. Create a quote to start pricing." },
                                    fontSize = 12.sp, color = TextMuted,
                                )
                            }
                        }
                    }

                    // Extra actions
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onAddTask,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Forest),
                            ) {
                                Icon(Icons.Default.CheckBoxOutlineBlank, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Task", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = onFilesClick,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Forest),
                            ) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Files", fontSize = 12.sp)
                            }
                        }
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }

                // Bottom "Create quote" button — matches HTML actionbar
                Box(
                    modifier = Modifier.fillMaxWidth().background(CrmSurface)
                        .border(1.dp, BorderGreen, RoundedCornerShape(0.dp)).padding(14.dp),
                ) {
                    Button(
                        onClick = onQuoteClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Forest),
                        shape = RoundedCornerShape(10.dp),
                    ) { Text("Create quote", fontWeight = FontWeight.Bold, fontSize = 15.sp) }
                }
            }
        }
    }
}

@Composable
private fun InfoKv(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = TextMuted)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Charcoal)
    }
}