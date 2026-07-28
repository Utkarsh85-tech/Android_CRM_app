package com.example.nexoworxcrmapp.ui.speaker

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexoworxcrmapp.data.SampleData
import com.example.nexoworxcrmapp.speech.VoiceParseResult
import com.example.nexoworxcrmapp.ui.lead.LeadScreen
import com.example.nexoworxcrmapp.ui.theme.AccentGreen
import com.example.nexoworxcrmapp.ui.theme.Forest
import com.example.nexoworxcrmapp.ui.theme.MidGreen

private val ScrimColor = Color(0xD90A1410)
private val SheetDark = Color(0xFF1E2A22)
private val SheetDeep = Color(0xFF0D1A12)
private val SubtitleMuted = Color(0x80FFFFFF)
private val LabelMuted = Color(0x59FFFFFF)
private val CardBg = Color(0x0FFFFFFF)
private val CardBorder = Color(0x12FFFFFF)

@Composable
fun SpeakerScreen(
    modifier: Modifier = Modifier,
    viewModel: SpeakerViewModel = viewModel(),
    onClose: () -> Unit = {},
    onOpenCreateLead: (com.example.nexoworxcrmapp.speech.LeadDraft) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val phase = uiState.phase

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.onMicClicked()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    val onMicWithPermission: () -> Unit = {
        if (viewModel.hasRecordPermission()) {
            viewModel.onMicClicked()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LeadScreen(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.35f),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScrimColor)
                .clickable(enabled = phase == VoiceUiPhase.Idle) { onClose() },
        )
        VoiceAssistantSheet(
            uiState = uiState,
            onClose = onClose,
            onMicClick = onMicWithPermission,
            onTryAgain = viewModel::tryAgain,
            onConfirmEvent = { viewModel.confirmCreate() },
            onOpenCreateLead = {
                viewModel.getCreateLeadDraft()?.let(onOpenCreateLead)
            },
            onSampleClick = viewModel::onSamplePhrase,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun VoiceAssistantSheet(
    uiState: SpeakerUiState,
    onClose: () -> Unit,
    onMicClick: () -> Unit,
    onTryAgain: () -> Unit,
    onConfirmEvent: () -> Unit,
    onOpenCreateLead: () -> Unit,
    onSampleClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val phase = uiState.phase
    val micPhase = when (phase) {
        VoiceUiPhase.Listening -> MicPhase.Listening
        VoiceUiPhase.Processing -> MicPhase.Processing
        VoiceUiPhase.Result -> MicPhase.Result
        else -> MicPhase.Idle
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Brush.linearGradient(colors = listOf(SheetDark, SheetDeep)))
            .clickable(enabled = false) { }
            .verticalScroll(rememberScrollState())
            .padding(bottom = 28.dp),
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0x33FFFFFF))
                .align(Alignment.CenterHorizontally),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Voice Assistant",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                )
                Text(
                    text = uiState.statusMessage,
                    fontSize = 12.sp,
                    color = SubtitleMuted,
                    modifier = Modifier.padding(top = 4.dp),
                )
                if (uiState.savedMessage != null) {
                    Text(
                        text = uiState.savedMessage,
                        fontSize = 11.sp,
                        color = AccentGreen,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0x1AFFFFFF)),
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xB3FFFFFF), modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        MicButtonWithRings(
            phase = micPhase,
            onClick = onMicClick,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Text(
            text = uiState.micHint,
            fontSize = 10.sp,
            color = LabelMuted,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp),
            textAlign = TextAlign.Center,
        )
        if (uiState.engineLabel.isNotBlank()) {
            Text(
                text = uiState.engineLabel,
                fontSize = 9.sp,
                color = LabelMuted.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 2.dp, bottom = 4.dp),
            )
        }
        Spacer(modifier = Modifier.height(20.dp))

        if (uiState.transcript.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x0FFFFFFF))
                    .padding(14.dp),
            ) {
                Text("YOU SAID", fontSize = 11.sp, color = LabelMuted, letterSpacing = 0.5.sp)
                Text(
                    text = "\"${uiState.transcript}\"",
                    fontSize = 13.sp,
                    color = Color(0xE6FFFFFF),
                    fontStyle = FontStyle.Italic,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        val parseResult = uiState.parseResult
        if (phase == VoiceUiPhase.Result && parseResult != null && parseResult !is VoiceParseResult.Unknown) {
            val fields = when (parseResult) {
                is VoiceParseResult.CreateLead -> parseResult.draft.displayFields()
                is VoiceParseResult.CreateEvent -> parseResult.draft.displayFields()
                is VoiceParseResult.Unknown -> emptyList()
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AccentGreen.copy(alpha = 0.12f))
                    .border(1.dp, AccentGreen.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
            ) {
                Text(
                    text = "DETECTED: ${parseResult.intentLabel.uppercase()}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGreen,
                    letterSpacing = 0.5.sp,
                )
                fields.forEach { (key, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(key, fontSize = 12.sp, color = SubtitleMuted)
                        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Try Again",
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x14FFFFFF))
                        .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(14.dp))
                        .clickable(onClick = onTryAgain)
                        .padding(vertical = 13.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xB3FFFFFF),
                    textAlign = TextAlign.Center,
                )
                if (parseResult is VoiceParseResult.CreateLead) {
                    Row(
                        modifier = Modifier
                            .weight(2f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.linearGradient(listOf(MidGreen, Forest)))
                            .clickable(onClick = onOpenCreateLead)
                            .padding(vertical = 13.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "Open Form to Confirm",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                } else {
                    Text(
                        text = "Save",
                        modifier = Modifier
                            .weight(2f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.linearGradient(listOf(MidGreen, Forest)))
                            .clickable(onClick = onConfirmEvent)
                            .padding(vertical = 13.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (phase == VoiceUiPhase.Idle || phase == VoiceUiPhase.Error) {
            Text(
                text = "TRY SAYING",
                fontSize = 11.sp,
                color = LabelMuted,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            )
            SampleData.voiceSamples.forEach { sample ->
                VoiceSampleCard(
                    sample = sample,
                    onClick = { onSampleClick(sample) },
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun VoiceSampleCard(
    sample: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "\"$sample\"",
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        fontSize = 12.sp,
        color = Color(0x99FFFFFF),
        fontStyle = FontStyle.Italic,
        lineHeight = 18.sp,
    )
}

private enum class MicPhase { Idle, Listening, Processing, Result }

@Composable
private fun MicButtonWithRings(
    phase: MicPhase,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ring_scale",
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ring_alpha",
    )
    val showRings = phase == MicPhase.Idle || phase == MicPhase.Listening
    val micSize = if (phase == MicPhase.Idle) 70.dp else 64.dp

    Box(modifier = modifier.size(120.dp), contentAlignment = Alignment.Center) {
        if (showRings) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .scale(ringScale)
                    .clip(CircleShape)
                    .border(2.dp, Forest.copy(alpha = ringAlpha * 0.5f), CircleShape),
            )
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .scale(ringScale * 0.98f)
                    .clip(CircleShape)
                    .border(2.dp, MidGreen.copy(alpha = ringAlpha), CircleShape),
            )
        }
        Box(
            modifier = Modifier
                .size(micSize)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(MidGreen, Forest)))
                .clickable(
                    enabled = phase != MicPhase.Processing,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            when (phase) {
                MicPhase.Processing -> ProcessingDots()
                else -> Icon(
                    imageVector = if (phase == MicPhase.Result) Icons.Default.Check else Icons.Default.Mic,
                    contentDescription = "Microphone",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

@Composable
private fun ProcessingDots() {
    val transition = rememberInfiniteTransition(label = "dots")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) { index ->
            val alpha by transition.animateFloat(
                initialValue = 0.35f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, delayMillis = index * 150),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dot_$index",
            )
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .alpha(alpha)
                    .clip(CircleShape)
                    .background(AccentGreen),
            )
        }
    }
}
