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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
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
import com.example.nexoworxcrmapp.speech.LeadDraft
import com.example.nexoworxcrmapp.ui.theme.BorderGreen
import com.example.nexoworxcrmapp.ui.theme.CardGreen
import com.example.nexoworxcrmapp.ui.theme.Charcoal
import com.example.nexoworxcrmapp.ui.theme.CrmBg
import com.example.nexoworxcrmapp.ui.theme.CrmSurface
import com.example.nexoworxcrmapp.ui.theme.Forest
import com.example.nexoworxcrmapp.ui.theme.MutedGreen
import com.example.nexoworxcrmapp.ui.theme.TextMuted

private val ErrorBorder = Color(0xFFE57373)

@Composable
fun LeadEditScreen(
    onClose: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    isCreateMode: Boolean = false,
    voicePrefill: LeadDraft? = null,
    viewModel: LeadEditViewModel = viewModel(),
) {
    val form by viewModel.formState.collectAsState()

    LaunchedEffect(voicePrefill) {
        if (voicePrefill != null) {
            viewModel.applyVoiceDraft(voicePrefill)
        }
    }

    LaunchedEffect(form.saveSuccess) {
        if (form.saveSuccess) {
            viewModel.consumeSaveSuccess()
            onSaved()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CrmBg),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CrmSurface)
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Charcoal)
            }
            Text(
                text = if (isCreateMode || form.isCreateMode) "Create Lead" else "Edit Lead",
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Charcoal,
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (form.isSaving) MutedGreen else Forest)
                    .clickable(enabled = !form.isSaving && !form.isLoading) { viewModel.save() }
                    .padding(horizontal = 18.dp, vertical = 8.dp),
            ) {
                if (form.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(18.dp),
                        color = CrmSurface,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Save", color = CrmSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        if (form.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Forest)
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (form.fromVoice) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardGreen)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, tint = Forest, modifier = Modifier.height(18.dp))
                    Text(
                        text = "Fields pre-filled from voice — review and confirm",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Forest,
                    )
                }
            }

            form.errorMessage?.let { error ->
                Text(text = error, color = Color(0xFFC0392B), fontSize = 12.sp)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FormField(
                    label = "FIRST NAME",
                    value = form.firstName,
                    onValueChange = viewModel::updateFirstName,
                    modifier = Modifier.weight(1f),
                )
                FormField(
                    label = "LAST NAME *",
                    value = form.lastName,
                    onValueChange = viewModel::updateLastName,
                    modifier = Modifier.weight(1f),
                    isError = form.lastNameError,
                    placeholder = "Last name",
                )
            }

            FormField(
                label = "COMPANY *",
                value = form.company,
                onValueChange = viewModel::updateCompany,
            )

            FormField(
                label = "EMAIL",
                value = form.email,
                onValueChange = viewModel::updateEmail,
                placeholder = "email@company.com",
            )

            FormField(
                label = "PHONE",
                value = form.phone,
                onValueChange = viewModel::updatePhone,
            )

            FormDropdown(
                label = "STATUS *",
                value = form.status,
                options = LeadFieldOptions.statuses,
                onValueChange = viewModel::updateStatus,
            )

            FormDropdown(
                label = "LEAD SOURCE",
                value = form.source,
                options = LeadFieldOptions.sources,
                onValueChange = viewModel::updateSource,
                allowEmpty = true,
            )

            FormDropdown(
                label = "RATING",
                value = form.rating,
                options = LeadFieldOptions.ratings,
                onValueChange = viewModel::updateRating,
                allowEmpty = true,
            )

            MultilineFormField(
                label = "DESCRIPTION",
                value = form.description,
                onValueChange = viewModel::updateDescription,
                placeholder = "Notes or description...",
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    placeholder: String = "",
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isError) Color(0xFFC0392B) else TextMuted,
            letterSpacing = 0.5.sp,
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CrmSurface)
                .border(
                    1.dp,
                    if (isError) ErrorBorder else BorderGreen,
                    RoundedCornerShape(12.dp),
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            textStyle = TextStyle(fontSize = 14.sp, color = Charcoal),
            cursorBrush = SolidColor(Forest),
            singleLine = true,
            decorationBox = { inner ->
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(placeholder, fontSize = 14.sp, color = TextMuted)
                }
                inner()
            },
        )
    }
}

@Composable
private fun MultilineFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
) {
    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted,
            letterSpacing = 0.5.sp,
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CrmSurface)
                .border(1.dp, BorderGreen, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            textStyle = TextStyle(fontSize = 14.sp, color = Charcoal, lineHeight = 20.sp),
            cursorBrush = SolidColor(Forest),
            decorationBox = { inner ->
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(placeholder, fontSize = 14.sp, color = TextMuted)
                }
                inner()
            },
        )
    }
}

@Composable
private fun FormDropdown(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    allowEmpty: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted,
            letterSpacing = 0.5.sp,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CrmSurface)
                    .border(1.dp, BorderGreen, RoundedCornerShape(12.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Text(
                    text = value.ifBlank { "Select..." },
                    fontSize = 14.sp,
                    color = if (value.isBlank()) TextMuted else Charcoal,
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                if (allowEmpty) {
                    DropdownMenuItem(
                        text = { Text("Select...") },
                        onClick = {
                            onValueChange("")
                            expanded = false
                        },
                    )
                }
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}
