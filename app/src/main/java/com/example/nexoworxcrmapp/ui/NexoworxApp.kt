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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Home
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
import com.example.nexoworxcrmapp.ui.home.FilteredListScreen
import com.example.nexoworxcrmapp.ui.home.HomeActionRoute
import com.example.nexoworxcrmapp.ui.home.HomeListType
import com.example.nexoworxcrmapp.ui.home.HomeScreen
import com.example.nexoworxcrmapp.ui.home.HomeViewModel
import com.example.nexoworxcrmapp.ui.home.QuickCreateSheetContent
import com.example.nexoworxcrmapp.ui.home.QuickCreateTarget
import com.example.nexoworxcrmapp.ui.lead.LeadNavHost
import com.example.nexoworxcrmapp.ui.notifications.NotificationsScreen
import com.example.nexoworxcrmapp.ui.opportunity.OpportunityNavHost
import com.example.nexoworxcrmapp.ui.search.SearchScreen
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
import androidx.compose.runtime.collectAsState
import com.example.nexoworxcrmapp.network.NetworkModule
import com.example.nexoworxcrmapp.ui.auth.LoginScreen
import com.example.nexoworxcrmapp.ui.sync.SyncIssuesScreen

// Speaker tab removed — now a floating draggable mic button
enum class HomeTab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Default.Home),
    Lead("Leads", Icons.Default.People),
    Deals("Deals", Icons.Default.TrendingUp),
    Calendar("Calendar", Icons.Default.CalendarMonth),
    More("More", Icons.Default.MoreHoriz),
}
enum class MoreItem(val label: String, val icon: ImageVector) {
    Accounts("Accounts", Icons.Default.Domain),
    Contacts("Contacts", Icons.Default.Contacts),
    Tasks("Tasks", Icons.Default.CheckBox),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexoworxApp() {
    NexoworxTheme {
        var isLoggedIn by rememberSaveable { mutableStateOf(false) }
        var selectedTab by rememberSaveable { mutableStateOf(HomeTab.Home) }
        var pendingVoiceDraft by remember { mutableStateOf<LeadDraft?>(null) }
        var pendingVoiceAccountDraft by remember {
            mutableStateOf<com.example.nexoworxcrmapp.speech.AccountDraft?>(null)
        }
        var pendingVoiceOpportunityDraft by remember {
            mutableStateOf<com.example.nexoworxcrmapp.speech.OpportunityDraft?>(null)
        }
        var pendingVoiceTaskDraft by remember {
            mutableStateOf<com.example.nexoworxcrmapp.speech.TaskDraft?>(null)
        }
        var pendingVoiceEventDraft by remember {
            mutableStateOf<com.example.nexoworxcrmapp.speech.EventDraft?>(null)
        }
        val speakerViewModel: SpeakerViewModel = viewModel()
        val homeViewModel: HomeViewModel = viewModel()

        // Speaker sheet state
        var showSpeakerSheet by remember { mutableStateOf(false) }

        var showMoreSheet by remember { mutableStateOf(false) }
        var selectedMore by remember { mutableStateOf<MoreItem?>(null) }
        val speakerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        // --- Home dashboard wiring -------------------------------------------------
        // Home never touches other tabs' NavHosts directly. It just describes what
        // it wants ("open lead X", "create a task") via these pending* triggers;
        // each NavHost consumes its own trigger once and resets it to null.
        var pendingQuickCreate by remember { mutableStateOf<QuickCreateTarget?>(null) }
        var pendingOpenLeadId by remember { mutableStateOf<String?>(null) }
        var pendingOpenAccountId by remember { mutableStateOf<String?>(null) }
        var pendingOpenContactId by remember { mutableStateOf<String?>(null) }

        var showSearch by remember { mutableStateOf(false) }
        var showNotifications by remember { mutableStateOf(false) }
        var showQuickCreateSheet by remember { mutableStateOf(false) }
        var homeListType by remember { mutableStateOf<HomeListType?>(null) }
        val quickCreateSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        // Single place that turns a HomeActionRoute into real navigation.
        // Add a new destination? Add one case here — Home/Notifications/Search
        // don't need to change.
        fun handleHomeRoute(route: HomeActionRoute) {
            when (route) {
                is HomeActionRoute.OpenLead -> {
                    pendingOpenLeadId = route.leadId
                    selectedTab = HomeTab.Lead
                }
                is HomeActionRoute.OpenAccount -> {
                    pendingOpenAccountId = route.accountId
                    selectedMore = MoreItem.Accounts
                    selectedTab = HomeTab.More
                }
                is HomeActionRoute.OpenContact -> {
                    pendingOpenContactId = route.contactId
                    selectedMore = MoreItem.Contacts
                    selectedTab = HomeTab.More
                }
                is HomeActionRoute.OpenFilteredList -> homeListType = route.type
                HomeActionRoute.OpenLeadsList -> selectedTab = HomeTab.Lead
                HomeActionRoute.OpenAccountsList -> {
                    selectedMore = MoreItem.Accounts
                    selectedTab = HomeTab.More
                }
                HomeActionRoute.OpenContactsList -> {
                    selectedMore = MoreItem.Contacts
                    selectedTab = HomeTab.More
                }
                HomeActionRoute.OpenTasks -> {
                    selectedMore = MoreItem.Tasks
                    selectedTab = HomeTab.More
                }
                HomeActionRoute.OpenDeals -> selectedTab = HomeTab.Deals
                HomeActionRoute.OpenCalendar -> selectedTab = HomeTab.Calendar
            }
        }

        // Draggable mic button position
        // Starts at bottom-right (offset is from top-left, so we start negative to push it right/down)
        var micOffsetX by remember { mutableFloatStateOf(0f) }
        var micOffsetY by remember { mutableFloatStateOf(0f) }

        when {
            // Gate everything behind login. UI-only for now (validates input,
            // doesn't call Salesforce) — real per-user OAuth swaps in here next.
            !isLoggedIn -> LoginScreen(
                onLoginSuccess = { isLoggedIn = true },
            )

            // Full-screen overlays. These intentionally replace the whole app
            // (including the bottom nav) while open, same as the mockup's sheets.
            showSearch -> SearchScreen(
                onBack = { showSearch = false },
                onOpenLead = { id ->
                    showSearch = false
                    handleHomeRoute(HomeActionRoute.OpenLead(id))
                },
                onOpenAccount = { id ->
                    showSearch = false
                    handleHomeRoute(HomeActionRoute.OpenAccount(id))
                },
                onOpenContact = { id ->
                    showSearch = false
                    handleHomeRoute(HomeActionRoute.OpenContact(id))
                },
            )

            showNotifications -> NotificationsScreen(
                viewModel = homeViewModel,
                onBack = { showNotifications = false },
                onRoute = { route ->
                    showNotifications = false
                    handleHomeRoute(route)
                },
            )

            homeListType != null -> FilteredListScreen(
                type = homeListType!!,
                viewModel = homeViewModel,
                onBack = { homeListType = null },
                onOpenLead = { id ->
                    homeListType = null
                    handleHomeRoute(HomeActionRoute.OpenLead(id))
                },
                onOpenTasksTab = {
                    homeListType = null
                    selectedMore = MoreItem.Tasks
                    selectedTab = HomeTab.More
                },
                onOpenCalendarTab = {
                    homeListType = null
                    selectedTab = HomeTab.Calendar
                },
                onOpenDealsTab = {
                    homeListType = null
                    selectedTab = HomeTab.Deals
                },
            )

            else -> {
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
                            onOpenCreate = { result ->
                                showSpeakerSheet = false
                                when (result) {
                                    is com.example.nexoworxcrmapp.speech.VoiceParseResult.CreateLead -> {
                                        pendingVoiceDraft = result.draft
                                        selectedTab = HomeTab.Lead
                                    }
                                    is com.example.nexoworxcrmapp.speech.VoiceParseResult.CreateAccount -> {
                                        pendingVoiceAccountDraft = result.draft
                                        selectedMore = MoreItem.Accounts
                                        selectedTab = HomeTab.More
                                    }
                                    is com.example.nexoworxcrmapp.speech.VoiceParseResult.CreateOpportunity -> {
                                        pendingVoiceOpportunityDraft = result.draft
                                        selectedTab = HomeTab.Deals
                                    }
                                    is com.example.nexoworxcrmapp.speech.VoiceParseResult.CreateTask -> {
                                        pendingVoiceTaskDraft = result.draft
                                        selectedMore = MoreItem.Tasks
                                        selectedTab = HomeTab.More
                                    }
                                    is com.example.nexoworxcrmapp.speech.VoiceParseResult.CreateEvent -> {
                                        pendingVoiceEventDraft = result.draft
                                        selectedTab = HomeTab.Calendar
                                    }
                                    is com.example.nexoworxcrmapp.speech.VoiceParseResult.Unknown -> Unit
                                }
                            },
                        )
                    }
                }

                // Quick create bottom sheet (from Home's "+" button)
                if (showQuickCreateSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showQuickCreateSheet = false },
                        sheetState = quickCreateSheetState,
                        containerColor = CrmSurface,
                    ) {
                        QuickCreateSheetContent(
                            onSelect = { target ->
                                pendingQuickCreate = target
                                showQuickCreateSheet = false
                                selectedTab = when (target) {
                                    QuickCreateTarget.Lead -> HomeTab.Lead
                                    QuickCreateTarget.Account -> { selectedMore = MoreItem.Accounts; HomeTab.More }
                                    QuickCreateTarget.Contact -> { selectedMore = MoreItem.Contacts; HomeTab.More }
                                    QuickCreateTarget.Task -> { selectedMore = MoreItem.Tasks; HomeTab.More }
                                    QuickCreateTarget.Event -> HomeTab.Calendar
                                    QuickCreateTarget.Opportunity -> HomeTab.Deals
                                }
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
                            selectedTab == HomeTab.Home -> HomeScreen(
                                modifier = Modifier.padding(innerPadding),
                                viewModel = homeViewModel,
                                onRoute = { route -> handleHomeRoute(route) },
                                onSearchClick = { showSearch = true },
                                onNotificationsClick = { showNotifications = true },
                                onQuickCreateClick = { showQuickCreateSheet = true },
                            )
                            selectedTab == HomeTab.Lead -> LeadNavHost(
                                modifier = Modifier.padding(innerPadding),
                                pendingVoiceDraft = pendingVoiceDraft,
                                onVoiceDraftConsumed = { pendingVoiceDraft = null },
                                openCreateOnLaunch = pendingQuickCreate == QuickCreateTarget.Lead,
                                onCreateHandled = { pendingQuickCreate = null },
                                initialLeadId = pendingOpenLeadId,
                                onInitialLeadHandled = { pendingOpenLeadId = null },
                            )
                            selectedTab == HomeTab.Deals -> OpportunityNavHost(
                                modifier = Modifier.padding(innerPadding),
                                openCreateOnLaunch = pendingQuickCreate == QuickCreateTarget.Opportunity,
                                onCreateHandled = { pendingQuickCreate = null },
                                pendingVoiceDraft = pendingVoiceOpportunityDraft,
                                onVoiceDraftConsumed = { pendingVoiceOpportunityDraft = null },
                            )
                            selectedTab == HomeTab.Calendar -> CalendarScreen(
                                modifier = Modifier.padding(innerPadding),
                                openCreateOnLaunch = pendingQuickCreate == QuickCreateTarget.Event,
                                onCreateHandled = { pendingQuickCreate = null },
                                pendingVoiceDraft = pendingVoiceEventDraft,
                                onVoiceDraftConsumed = { pendingVoiceEventDraft = null },
                            )
                            selectedTab == HomeTab.More && selectedMore == MoreItem.Accounts -> AccountNavHost(
                                modifier = Modifier.padding(innerPadding),
                                openCreateOnLaunch = pendingQuickCreate == QuickCreateTarget.Account,
                                onCreateHandled = { pendingQuickCreate = null },
                                initialAccountId = pendingOpenAccountId,
                                onInitialAccountHandled = { pendingOpenAccountId = null },
                                pendingVoiceDraft = pendingVoiceAccountDraft,
                                onVoiceDraftConsumed = { pendingVoiceAccountDraft = null },
                            )
                            selectedTab == HomeTab.More && selectedMore == MoreItem.Contacts -> ContactNavHost(
                                modifier = Modifier.padding(innerPadding),
                                openCreateOnLaunch = pendingQuickCreate == QuickCreateTarget.Contact,
                                onCreateHandled = { pendingQuickCreate = null },
                                initialContactId = pendingOpenContactId,
                                onInitialContactHandled = { pendingOpenContactId = null },
                            )
                            selectedTab == HomeTab.More && selectedMore == MoreItem.Tasks -> TaskNavHost(
                                modifier = Modifier.padding(innerPadding),
                                openCreateOnLaunch = pendingQuickCreate == QuickCreateTarget.Task,
                                onCreateHandled = { pendingQuickCreate = null },
                                pendingVoiceDraft = pendingVoiceTaskDraft,
                                onVoiceDraftConsumed = { pendingVoiceTaskDraft = null },
                            )
                            else -> HomeScreen(
                                modifier = Modifier.padding(innerPadding),
                                viewModel = homeViewModel,
                                onRoute = { route -> handleHomeRoute(route) },
                                onSearchClick = { showSearch = true },
                                onNotificationsClick = { showNotifications = true },
                                onQuickCreateClick = { showQuickCreateSheet = true },
                            )
                        }
                        // Draggable floating mic button
                        // Default corner is bottom-start (left); still draggable anywhere.
                        FloatingActionButton(
                            onClick = { showSpeakerSheet = true },
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(
                                    start = 16.dp,
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

                        // Fixed quick-create button — bottom-right, Home tab only.
                        if (selectedTab == HomeTab.Home) {
                            FloatingActionButton(
                                onClick = { showQuickCreateSheet = true },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(
                                        end = 16.dp,
                                        bottom = innerPadding.calculateBottomPadding() + 16.dp,
                                    )
                                    .size(56.dp),
                                shape = CircleShape,
                                containerColor = Forest,
                                contentColor = CrmSurface,
                                elevation = FloatingActionButtonDefaults.elevation(
                                    defaultElevation = 6.dp,
                                    pressedElevation = 10.dp,
                                ),
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Quick create",
                                    modifier = Modifier.size(26.dp),
                                )
                            }
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
    }
}