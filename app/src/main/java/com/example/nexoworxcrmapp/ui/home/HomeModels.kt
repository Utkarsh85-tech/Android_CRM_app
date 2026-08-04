package com.example.nexoworxcrmapp.ui.home
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// What the Quick Create sheet can create.
// To support a new record type: add a case here, one line in
// QuickCreateSheet's `options` list, and one `openCreateOnLaunch`
// branch where NexoworxApp wires up the target's NavHost.
enum class QuickCreateTarget {
    Lead, Account, Contact, Task, Event, Opportunity
}

enum class ActionPriority { High, Medium, Low }

// The four "Today at a glance" tiles each open a filtered list of just
// their own records — not the full Leads/Tasks/Deals tab.
enum class HomeListType(val title: String) {
    HotLeads("Hot leads"),
    TodayMeetings("Today's meetings"),
    TodayFollowUps("Today's follow-ups"),
    OpenDeals("Open deals"),
}

// Where tapping a stat tile / action card / search result should take the user.
// NexoworxApp.handleHomeRoute() is the single place that turns these into
// real navigation, so Home itself never needs to know about tabs or NavHosts.
sealed class HomeActionRoute {
    data class OpenLead(val leadId: String) : HomeActionRoute()
    data class OpenAccount(val accountId: String) : HomeActionRoute()
    data class OpenContact(val contactId: String) : HomeActionRoute()
    data class OpenFilteredList(val type: HomeListType) : HomeActionRoute()
    object OpenLeadsList : HomeActionRoute()
    object OpenAccountsList : HomeActionRoute()
    object OpenContactsList : HomeActionRoute()
    object OpenTasks : HomeActionRoute()
    object OpenDeals : HomeActionRoute()
    object OpenCalendar : HomeActionRoute()
}

data class HomeStat(
    val label: String,
    val value: Int,
    val icon: ImageVector,
    val accent: Color,
    val route: HomeActionRoute,
)

data class HomeActionItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val priority: ActionPriority,
    val icon: ImageVector,
    val route: HomeActionRoute,
)
data class HomeUiState(
    val isLoading: Boolean = true,
    val greetingName: String = "",
    val initials: String = "",
    val stats: List<HomeStat> = emptyList(),
    val actionItems: List<HomeActionItem> = emptyList(),
    val unreadCount: Int = 0,
    val hotLeads: List<com.example.nexoworxcrmapp.data.Lead> = emptyList(),
    val todayMeetings: List<com.example.nexoworxcrmapp.data.CalendarDayItem> = emptyList(),
    val todayFollowUps: List<com.example.nexoworxcrmapp.data.Task> = emptyList(),
    val openDealsList: List<com.example.nexoworxcrmapp.data.Opportunity> = emptyList(),
    val errorMessage: String? = null,
)