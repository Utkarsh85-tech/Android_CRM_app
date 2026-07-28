// ─────────────────────────────────────────────────────────────────────────────
// NEW FILE
// app/src/main/java/com/example/nexoworxcrmapp/ui/opportunity/OpportunityEditScreen.kt
// ─────────────────────────────────────────────────────────────────────────────

package com.example.nexoworxcrmapp.ui.opportunity

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexoworxcrmapp.ui.theme.BorderGreen
import com.example.nexoworxcrmapp.ui.theme.Charcoal
import com.example.nexoworxcrmapp.ui.theme.CrmBg
import com.example.nexoworxcrmapp.ui.theme.CrmSurface
import com.example.nexoworxcrmapp.ui.theme.Forest
import com.example.nexoworxcrmapp.ui.theme.MutedGreen
import com.example.nexoworxcrmapp.ui.theme.TextMuted
import com.example.nexoworxcrmapp.ui.common.DatePickerField
@Composable
fun OpportunityEditScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OpportunityEditViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) onSaved()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CrmBg),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Forest)
                .padding(top = 8.dp, bottom = 12.dp, start = 4.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CrmSurface)
            }
            Text(
                text = if (uiState.name.isBlank()) "New Opportunity" else "Edit Opportunity",
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = CrmSurface,
            )
            if (uiState.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CrmSurface, strokeWidth = 2.dp)
            } else {
                IconButton(onClick = { viewModel.save() }) {
                    Icon(Icons.Default.Check, contentDescription = "Save", tint = CrmSurface)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                OppEditField(label = "DEAL NAME *", value = uiState.name, placeholder = "e.g. Tata Motors – CPQ License", onValueChange = viewModel::onNameChange)
            }

            // Stage dropdown
            item {
                var expanded by remember { mutableStateOf(false) }
                Column {
                    Text(text = "STAGE *", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, letterSpacing = 0.5.sp, modifier = Modifier.padding(bottom = 4.dp))
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CrmSurface)
                                .border(1.dp, BorderGreen, RoundedCornerShape(12.dp))
                                .clickable { expanded = true }
                                .padding(horizontal = 14.dp, vertical = 13.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = uiState.stageName.ifBlank { "Select stage…" },
                                fontSize = 13.sp,
                                color = if (uiState.stageName.isBlank()) TextMuted else Charcoal,
                            )
                            Text("▾", fontSize = 12.sp, color = MutedGreen)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            OPPORTUNITY_STAGES.forEach { stage ->
                                DropdownMenuItem(
                                    text = { Text(stage, fontSize = 13.sp) },
                                    onClick = {
                                        viewModel.onStageChange(stage)
                                        expanded = false
                                    },
                                )
                            }
                        }
                    }
                }
            }

            item {
                DatePickerField(
                    label = "CLOSE DATE *",
                    value = uiState.closeDate,
                    onValueChange = viewModel::onCloseDateChange,
                    outputFormat = "yyyy-MM-dd",
                    placeholder = "Select close date",
                )            }

            item {
                OppEditField(label = "AMOUNT", value = uiState.amount, placeholder = "e.g. 50000", onValueChange = viewModel::onAmountChange)
            }

            item {
                OppEditField(label = "DESCRIPTION", value = uiState.description, placeholder = "Notes about this deal…", onValueChange = viewModel::onDescriptionChange, minLines = 4)
            }

            // Error message
            val err = uiState.errorMessage
            if (err != null) {
                item {
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

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun OppEditField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    minLines: Int = 1,
) {
    Column {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, letterSpacing = 0.5.sp, modifier = Modifier.padding(bottom = 4.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CrmSurface)
                .border(1.dp, BorderGreen, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 13.dp),
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
