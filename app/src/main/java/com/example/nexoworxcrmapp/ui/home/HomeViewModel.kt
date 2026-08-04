package com.example.nexoworxcrmapp.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.PersonAdd
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexoworxcrmapp.data.CrmRepository
import com.example.nexoworxcrmapp.data.Task
import com.example.nexoworxcrmapp.ui.theme.AccentGreen
import com.example.nexoworxcrmapp.ui.theme.Danger
import com.example.nexoworxcrmapp.ui.theme.Forest
import com.example.nexoworxcrmapp.ui.theme.Warning
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Aggregates data that already lives in CrmRepository into the shape the
 * Home screen (and the Notifications screen, which reuses [actionItems])
 * need. Home never talks to repositories directly — if a new "today at a
 * glance" tile or "needs action" rule is needed later, add it here only.
 */
class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // IDs the user has already seen via "Mark all as read". Kept separate
    // from the action items themselves — the underlying task/lead/meeting
    // is still real and still due, we just stop badging it.
    private val readIds = mutableSetOf<String>()

    init {
        refresh()
    }

    fun markAllAsRead() {
        readIds.addAll(_uiState.value.actionItems.map { it.id })
        _uiState.value = _uiState.value.copy(unreadCount = 0)
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // These populate CrmRepository's shared StateFlows; every screen
            // that reads them (Leads tab, Accounts tab, etc.) benefits too.
            CrmRepository.refreshLeads()
            CrmRepository.refreshTasks()
            CrmRepository.refreshOpportunities()
            CrmRepository.refreshEvents()

            val leads = CrmRepository.leads.value
            val tasks = CrmRepository.tasks.value
            val opportunities = CrmRepository.opportunities.value
            val calendarItems = CrmRepository.calendarItems.value
            val today = LocalDate.now()

            val meetingsTodayList = calendarItems.filter { item ->
                item.type == "event" && item.date == today.dayOfMonth &&
                        item.month == today.monthValue && item.year == today.year
            }
            val followUpsTodayList = tasks.filter { task ->
                !task.isCompleted && parseIsoDate(task.dueDate) == today
            }
            val hotLeadsList = leads.filter { it.rating.equals("Hot", ignoreCase = true) }
            val openDealsList = opportunities.filter { !it.isClosed }

            val stats = listOf(
                HomeStat("Meetings", meetingsTodayList.size, Icons.Default.CalendarMonth, Forest, HomeActionRoute.OpenFilteredList(HomeListType.TodayMeetings)),
                HomeStat("Follow-ups", followUpsTodayList.size, Icons.Default.Phone, AccentGreen, HomeActionRoute.OpenFilteredList(HomeListType.TodayFollowUps)),
                HomeStat("Hot leads", hotLeadsList.size, Icons.Default.LocalFireDepartment, Danger, HomeActionRoute.OpenFilteredList(HomeListType.HotLeads)),
                HomeStat("Open deals", openDealsList.size, Icons.Default.TrendingUp, Forest, HomeActionRoute.OpenFilteredList(HomeListType.OpenDeals)),
            )

            val actionItems = buildActionItems(tasks, leads, calendarItems, today)
            val unreadCount = actionItems.count { it.id !in readIds }

            _uiState.value = HomeUiState(
                isLoading = false,
                stats = stats,
                actionItems = actionItems,
                unreadCount = unreadCount,
                hotLeads = hotLeadsList,
                todayMeetings = meetingsTodayList,
                todayFollowUps = followUpsTodayList,
                openDealsList = openDealsList,
            )
        }
    }

    private fun buildActionItems(
        tasks: List<Task>,
        leads: List<com.example.nexoworxcrmapp.data.Lead>,
        calendarItems: List<com.example.nexoworxcrmapp.data.CalendarDayItem>,
        today: LocalDate,
    ): List<HomeActionItem> {
        val items = mutableListOf<HomeActionItem>()

        // Tasks due today or tomorrow — highest priority, most actionable.
        val tomorrow = today.plusDays(1)
        tasks.filter { task ->
            val dueDate = parseIsoDate(task.dueDate)
            !task.isCompleted && (dueDate == today || dueDate == tomorrow)
        }.forEach { task ->
            val dueDate = parseIsoDate(task.dueDate)
            val label = if (dueDate == today) "Due today" else "Due tomorrow"
            items += HomeActionItem(
                id = "task_${task.id}",
                title = task.subject,
                subtitle = "$label · ${task.dueDate}",
                priority = if (dueDate == today) ActionPriority.High else ActionPriority.Medium,
                icon = Icons.Default.CheckCircle,
                route = HomeActionRoute.OpenTasks,
            )
        }

        // Newly assigned hot leads.
        leads.filter { lead ->
            lead.rating.equals("Hot", ignoreCase = true) &&
                    lead.status.contains("New", ignoreCase = true)
        }.forEach { lead ->
            items += HomeActionItem(
                id = "lead_${lead.id}",
                title = "New hot lead: ${lead.company.ifBlank { lead.fullName }}",
                subtitle = listOf(lead.industry, lead.source).filter { it.isNotBlank() }.joinToString(" · "),
                priority = ActionPriority.Medium,
                icon = Icons.Default.PersonAdd,
                route = HomeActionRoute.OpenLead(lead.id),
            )
        }

        // Meetings starting soon today.
        calendarItems.filter { item ->
            item.type == "event" && item.date == today.dayOfMonth
        }.forEach { item ->
            items += HomeActionItem(
                id = "event_${item.id}",
                title = item.subject,
                subtitle = listOf(item.time, item.location).filter { it.isNotBlank() }.joinToString(" · "),
                priority = ActionPriority.Medium,
                icon = Icons.Default.Star,
                route = HomeActionRoute.OpenCalendar,
            )
        }

        return items
    }

    private fun parseIsoDate(value: String): LocalDate? =
        try {
            if (value.isBlank()) null else LocalDate.parse(value.take(10))
        } catch (e: Exception) {
            null
        }
}