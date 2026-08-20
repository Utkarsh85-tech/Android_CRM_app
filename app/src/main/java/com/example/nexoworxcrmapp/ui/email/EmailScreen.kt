// NEW FILE
// app/src/main/java/com/example/nexoworxcrmapp/ui/email/EmailScreen.kt

package com.example.nexoworxcrmapp.ui.email

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.nexoworxcrmapp.ui.theme.TextSecondary

@Composable
fun EmailScreen(
    leadId: String,
    toEmail: String,
    leadName: String,
    onBack: () -> Unit,
    onSent: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EmailViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // Prefill fields when screen opens
    LaunchedEffect(toEmail) {
        viewModel.prefill(toEmail, leadName)
    }


    // Success dialog
    if (uiState.isSuccess) {
        AlertDialog(
            onDismissRequest = { onSent() },
            title = { Text("Email Sent!", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Your email to $toEmail has been sent and logged on the Lead record in Salesforce.",
                    fontSize = 14.sp,
                )
            },
            confirmButton = {
                Button(
                    onClick = { onSent() },
                    colors = ButtonDefaults.buttonColors(containerColor = Forest),
                    shape = RoundedCornerShape(10.dp),
                ) { Text("Done", fontWeight = FontWeight.Bold) }
            },
            containerColor = CrmSurface,
            shape = RoundedCornerShape(16.dp),
        )
    }

    // Error dialog
    val err = uiState.errorMessage
    if (err != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Send Failed", fontWeight = FontWeight.Bold) },
            text = { Text(err, fontSize = 14.sp) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
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
                text = "New Email",
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = CrmSurface,
            )
            if (uiState.isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = CrmSurface,
                    strokeWidth = 2.dp,
                )
            } else {
                IconButton(onClick = { viewModel.send(leadId) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = CrmSurface,
                    )
                }
            }
        }

        // Sender info bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CrmSurface)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("From:", fontSize = 12.sp, color = TextMuted, modifier = Modifier.padding(end = 8.dp))
            Text(SENDER_EMAIL, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // To field
            EmailField(
                label = "TO",
                value = uiState.to,
                placeholder = "recipient@email.com",
                onValueChange = viewModel::onToChange,
                singleLine = true,
            )

            // Subject field
            EmailField(
                label = "SUBJECT",
                value = uiState.subject,
                placeholder = "Email subject",
                onValueChange = viewModel::onSubjectChange,
                singleLine = true,
            )

            // Body field
            EmailField(
                label = "MESSAGE",
                value = uiState.body,
                placeholder = "Write your message here…",
                onValueChange = viewModel::onBodyChange,
                singleLine = false,
                minLines = 12,
            )

            // Salesforce note
            Text(
                text = "📌 This email will be sent via Salesforce and logged on the Lead record.",
                fontSize = 11.sp,
                color = MutedGreen,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE8F5EE))
                    .padding(10.dp),
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EmailField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean,
    minLines: Int = 1,
) {
    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CrmSurface)
                .border(1.dp, BorderGreen, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            textStyle = TextStyle(fontSize = 14.sp, color = Charcoal),
            cursorBrush = SolidColor(Forest),
            singleLine = singleLine,
            minLines = minLines,
            decorationBox = { inner ->
                if (value.isEmpty()) Text(placeholder, fontSize = 14.sp, color = TextMuted)
                inner()
            },
        )
    }
}
