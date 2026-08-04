package com.example.nexoworxcrmapp.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexoworxcrmapp.data.CrmRepository
import com.example.nexoworxcrmapp.network.ApiResult
import com.example.nexoworxcrmapp.ui.theme.BorderGreen
import com.example.nexoworxcrmapp.ui.theme.Charcoal
import com.example.nexoworxcrmapp.ui.theme.CrmBg
import com.example.nexoworxcrmapp.ui.theme.CrmSurface
import com.example.nexoworxcrmapp.ui.theme.Danger
import com.example.nexoworxcrmapp.ui.theme.Forest
import com.example.nexoworxcrmapp.ui.theme.TextMuted
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.layout.size
import androidx.annotation.RequiresApi

/**
 * A real "New meeting" form — calls CrmRepository.createEvent(), which is a
 * genuine Salesforce Event create, not a local-only draft.
 */
@RequiresApi(android.os.Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCreateScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    voicePrefill: com.example.nexoworxcrmapp.speech.EventDraft? = null,
) {
    val scope = rememberCoroutineScope()

    var subject by remember { mutableStateOf(voicePrefill?.subject.orEmpty()) }
    var location by remember { mutableStateOf(voicePrefill?.location.orEmpty()) }
    var date by remember {
        mutableStateOf(
            voicePrefill?.startEpochMillis?.takeIf { it > 0 }
                ?.let { java.time.Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
                ?: LocalDate.now(),
        )
    }
    var startTime by remember {
        mutableStateOf(
            voicePrefill?.startEpochMillis?.takeIf { it > 0 }
                ?.let { java.time.Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalTime() }
                ?: LocalTime.of(10, 0),
        )
    }
    var endTime by remember {
        mutableStateOf(
            voicePrefill?.endEpochMillis?.takeIf { it > 0 }
                ?.let { java.time.Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalTime() }
                ?: LocalTime.of(10, 30),
        )
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, d MMM yyyy") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a") }

    fun save() {
        if (subject.isBlank()) {
            errorMessage = "Subject is required"
            return
        }
        val startDateTime = java.time.LocalDateTime.of(date, startTime)
        val endDateTime = java.time.LocalDateTime.of(date, endTime)
        if (!endDateTime.isAfter(startDateTime)) {
            errorMessage = "End time must be after start time"
            return
        }
        val zone = ZoneId.systemDefault()
        errorMessage = null
        isSaving = true
        scope.launch {
            val result = CrmRepository.createEvent(
                subject = subject.trim(),
                startEpochMillis = startDateTime.atZone(zone).toInstant().toEpochMilli(),
                endEpochMillis = endDateTime.atZone(zone).toInstant().toEpochMilli(),
                location = location.trim(),
            )
            isSaving = false
            when (result) {
                is ApiResult.Success -> onSaved()
                is ApiResult.Error -> errorMessage = result.message ?: "Couldn't save the meeting"
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().background(CrmBg)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CrmSurface)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Charcoal)
            }
            Text("New meeting", fontSize = 15.sp, color = Charcoal)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))

            FieldRow(label = "Date", value = date.format(dateFormatter), onClick = { showDatePicker = true })
            FieldRow(label = "Start time", value = startTime.format(timeFormatter), onClick = { showStartTimePicker = true })
            FieldRow(label = "End time", value = endTime.format(timeFormatter), onClick = { showEndTimePicker = true })

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )

            if (errorMessage != null) {
                Text(
                    errorMessage!!,
                    color = Danger,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }

            Button(
                onClick = { save() },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = Forest),
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = CrmSurface)
                } else {
                    Text("Save meeting")
                }
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        date = java.time.Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            androidx.compose.material3.DatePicker(state = state)
        }
    }

    if (showStartTimePicker) {
        TimePickerDialogSimple(
            initial = startTime,
            onDismiss = { showStartTimePicker = false },
            onConfirm = { startTime = it; showStartTimePicker = false },
        )
    }

    if (showEndTimePicker) {
        TimePickerDialogSimple(
            initial = endTime,
            onDismiss = { showEndTimePicker = false },
            onConfirm = { endTime = it; showEndTimePicker = false },
        )
    }
}

@Composable
private fun FieldRow(label: String, value: String, onClick: () -> Unit) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Text(label, fontSize = 11.sp, color = TextMuted)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .background(CrmSurface, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 14.dp),
        ) {
            Text(value, fontSize = 14.sp, color = Charcoal)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialogSimple(
    initial: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = false,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = { TimePicker(state = state) },
    )
}