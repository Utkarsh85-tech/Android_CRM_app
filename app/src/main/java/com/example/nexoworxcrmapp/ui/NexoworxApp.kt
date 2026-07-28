// REPLACE NexoworxApp.kt entirely

package com.example.nexoworxcrmapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexoworxcrmapp.speech.LeadDraft
import com.example.nexoworxcrmapp.ui.account.AccountNavHost
import com.example.nexoworxcrmapp.ui.calendar.CalendarScreen
import com.example.nexoworxcrmapp.ui.contact.ContactNavHost
import com.example.nexoworxcrmapp.ui.lead.LeadNavHost
import com.example.nexoworxcrmapp.ui.opportunity.OpportunityNavHost
import com.example.nexoworxcrmapp.ui.speaker.SpeakerScreen
import com.example.nexoworxcrmapp.ui.speaker.SpeakerViewModel
import com.example.nexoworxcrmapp.ui.task.TaskNavHost
import com.example.nexoworxcrmapp.ui.theme.CrmSurface
import com.example.nexoworxcrmapp.ui.theme.Forest
import com.example.nexoworxcrmapp.ui.theme.MutedGreen
import com.example.nexoworxcrmapp.ui.theme.NexoworxTheme
import kotlin.math.roundToInt
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.ui.unit.sp

// Speaker tab removed — now a floating draggable mic button
enum class HomeTab(val label: String, val icon: ImageVector) {
    Lead("Leads", Icons.Default.People),
    Accounts("Accounts", Icons.Default.Domain),
    Contacts("Contacts", Icons.Default.Contacts),
    Tasks("Tasks", Icons.Default.CheckBox),
    More("More", Icons.Default.MoreHoriz),
}
enum class MoreItem(val label: String, val icon: ImageVector) {
    Opportunities("Deals", Icons.Default.TrendingUp),
    Calendar("Calendar", Icons.Default.CalendarMonth),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexoworxApp() {
    NexoworxTheme {
        var selectedTab by rememberSaveable { mutableStateOf(HomeTab.Lead) }
        var pendingVoiceDraft by remember { mutableStateOf<LeadDraft?>(null) }
        val speakerViewModel: SpeakerViewModel = viewModel()

        // Speaker sheet state
        var showSpeakerSheet by remember { mutableStateOf(false) }

        var showMoreSheet by remember { mutableStateOf(false) }
        var selectedMore by remember { mutableStateOf<MoreItem?>(null) }
        val speakerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        // Draggable mic button position
        // Starts at bottom-right (offset is from top-left, so we start negative to push it right/down)
        var micOffsetX by remember { mutableFloatStateOf(0f) }
        var micOffsetY by remember { mutableFloatStateOf(0f) }

        // Speaker bottom sheet
        if (showSpeakerSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSpeakerSheet = false },
                sheetState = speakerSheetState,
                containerColor = CrmSurface,
            ) {
                SpeakerScreen(
                    modifier = Modifier,
                    viewModel = speakerViewModel,
                    onClose = { showSpeakerSheet = false },
                    onOpenCreateLead = { draft ->
                        pendingVoiceDraft = draft
                        selectedTab = HomeTab.Lead
                        showSpeakerSheet = false
                    },
                )
            }
        }

        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = CrmSurface,
                    tonalElevation = 0.dp,
                ) {
                    HomeTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = {
                                if (tab == HomeTab.More) showMoreSheet = true
                                else { selectedTab = tab; selectedMore = null }
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label,
                                )
                            },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Forest,
                                selectedTextColor = Forest,
                                indicatorColor = Forest.copy(alpha = 0.12f),
                                unselectedIconColor = MutedGreen,
                                unselectedTextColor = MutedGreen,
                            ),
                        )
                    }
                }
            },
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {

                // Main content
                when {
                    selectedTab == HomeTab.Lead -> LeadNavHost(
                        modifier = Modifier.padding(innerPadding),
                        pendingVoiceDraft = pendingVoiceDraft,
                        onVoiceDraftConsumed = { pendingVoiceDraft = null },
                    )
                    selectedTab == HomeTab.Accounts -> AccountNavHost(
                        modifier = Modifier.padding(innerPadding),
                    )
                    selectedTab == HomeTab.Contacts -> ContactNavHost(
                        modifier = Modifier.padding(innerPadding),
                    )
                    selectedTab == HomeTab.Tasks -> TaskNavHost(
                        modifier = Modifier.padding(innerPadding),
                    )
                    selectedTab == HomeTab.More && selectedMore == MoreItem.Opportunities -> OpportunityNavHost(
                        modifier = Modifier.padding(innerPadding),
                    )
                    selectedTab == HomeTab.More && selectedMore == MoreItem.Calendar -> CalendarScreen(
                        Modifier.padding(innerPadding),
                    )
                    else -> LeadNavHost(
                        modifier = Modifier.padding(innerPadding),
                        pendingVoiceDraft = pendingVoiceDraft,
                        onVoiceDraftConsumed = { pendingVoiceDraft = null },
                    )
                }
                // Draggable floating mic button
                // Aligned to bottom-end by default, draggable anywhere
                FloatingActionButton(
                    onClick = { showSpeakerSheet = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(
                            end = 16.dp,
                            bottom = innerPadding.calculateBottomPadding() + 16.dp,
                        )
                        .offset {
                            IntOffset(
                                micOffsetX.roundToInt(),
                                micOffsetY.roundToInt(),
                            )
                        }
                        .size(56.dp)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                micOffsetX += dragAmount.x
                                micOffsetY += dragAmount.y
                            }
                        },
                    shape = CircleShape,
                    containerColor = Forest,
                    contentColor = CrmSurface,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 10.dp,
                    ),
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Voice Assistant",
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
        }
        if (showMoreSheet) {
            ModalBottomSheet(
                onDismissRequest = { showMoreSheet = false },
                containerColor = CrmSurface,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(bottom = 32.dp),
                ) {
                    Text(
                        "More",
                        fontSize = 13.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        color = com.example.nexoworxcrmapp.ui.theme.TextMuted,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
                    )
                    MoreItem.entries.forEach { item ->
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedMore = item
                                    selectedTab = HomeTab.More
                                    showMoreSheet = false
                                }
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp),
                        ) {
                            Icon(item.icon, contentDescription = item.label, tint = Forest, modifier = Modifier.size(22.dp))
                            Text(item.label, fontSize = 15.sp, color = com.example.nexoworxcrmapp.ui.theme.Charcoal)
                        }
                        androidx.compose.material3.HorizontalDivider(color = com.example.nexoworxcrmapp.ui.theme.BorderGreen)
                    }
                }
            }
        }
    }

}
