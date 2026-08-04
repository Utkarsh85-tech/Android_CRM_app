package com.example.nexoworxcrmapp.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import com.example.nexoworxcrmapp.ui.theme.TextMuted

private val ErrorBorder = Color(0xFFE57373)

// ── Field option lists ────────────────────────────────────────────────────────

private object AccountFieldOptions {
    val industries = listOf(
        "Technology", "Finance", "Healthcare", "Manufacturing",
        "Retail", "Education", "Media", "Other",
    )
    val types = listOf("Customer", "Prospect", "Partner", "Competitor", "Other")
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun AccountEditScreen(
    onClose: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountEditViewModel = viewModel(),
    voicePrefill: com.example.nexoworxcrmapp.speech.AccountDraft? = null,
) {
    val form by viewModel.formState.collectAsState()

    // One-time seed from a voice-parsed draft — only applies in create mode,
    // and only once, so it never clobbers what the user types afterward.
    LaunchedEffect(voicePrefill) {
        if (voicePrefill != null && form.isCreateMode) {
            if (voicePrefill.name.isNotBlank()) viewModel.updateName(voicePrefill.name)
            if (voicePrefill.phone.isNotBlank()) viewModel.updatePhone(voicePrefill.phone)
            if (voicePrefill.industry.isNotBlank()) viewModel.updateIndustry(voicePrefill.industry)
            if (voicePrefill.website.isNotBlank()) viewModel.updateWebsite(voicePrefill.website)
            if (voicePrefill.billingCity.isNotBlank()) viewModel.updateBillingCity(voicePrefill.billingCity)
            if (voicePrefill.description.isNotBlank()) viewModel.updateDescription(voicePrefill.description)
        }
    }

    // When save succeeds, navigate back
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

        // ── Top bar ──────────────────────────────────────────────────────────
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
                text = if (form.isCreateMode) "Create Account" else "Edit Account",
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Charcoal,
            )

            // Save button — shows spinner while saving
            if (form.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 16.dp),
                    color = Forest,
                    strokeWidth = 2.dp,
                )
            } else {
                IconButton(onClick = viewModel::save) {
                    Icon(Icons.Default.Check, contentDescription = "Save", tint = Forest)
                }
            }
        }

        // ── Loading state (edit mode only — loading existing data) ───────────
        if (form.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Forest)
            }
            return@Column
        }

        // ── Error banner ─────────────────────────────────────────────────────
        if (form.errorMessage != null) {
            Text(
                text = form.errorMessage!!,
                color = Color(0xFFC0392B),
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFDEDED))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }

        // ── Form fields ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {

            FormField(
                label = "ACCOUNT NAME *",
                value = form.name,
                onValueChange = viewModel::updateName,
                isError = form.nameError,
                placeholder = "Company name",
            )

            Spacer(Modifier.height(16.dp))

            FormField(
                label = "PHONE",
                value = form.phone,
                onValueChange = viewModel::updatePhone,
                placeholder = "+91 00000 00000",
            )

            Spacer(Modifier.height(16.dp))

            FormDropdown(
                label = "INDUSTRY",
                value = form.industry,
                options = AccountFieldOptions.industries,
                onValueChange = viewModel::updateIndustry,
                allowEmpty = true,
            )

            Spacer(Modifier.height(16.dp))

            FormDropdown(
                label = "TYPE",
                value = form.type,
                options = AccountFieldOptions.types,
                onValueChange = viewModel::updateType,
                allowEmpty = true,
            )

            Spacer(Modifier.height(16.dp))

            FormField(
                label = "BILLING CITY",
                value = form.billingCity,
                onValueChange = viewModel::updateBillingCity,
            )

            Spacer(Modifier.height(16.dp))

            FormField(
                label = "BILLING COUNTRY",
                value = form.billingCountry,
                onValueChange = viewModel::updateBillingCountry,
            )

            Spacer(Modifier.height(16.dp))

            FormField(
                label = "WEBSITE",
                value = form.website,
                onValueChange = viewModel::updateWebsite,
                placeholder = "https://example.com",
            )

            Spacer(Modifier.height(16.dp))

            MultilineFormField(
                label = "DESCRIPTION",
                value = form.description,
                onValueChange = viewModel::updateDescription,
                placeholder = "Notes or description...",
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Reusable form components ──────────────────────────────────────────────────

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
