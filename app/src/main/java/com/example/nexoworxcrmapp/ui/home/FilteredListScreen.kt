package com.example.nexoworxcrmapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexoworxcrmapp.ui.theme.Charcoal
import com.example.nexoworxcrmapp.ui.theme.CrmBg
import com.example.nexoworxcrmapp.ui.theme.CrmSurface
import com.example.nexoworxcrmapp.ui.theme.TextMuted
import androidx.compose.foundation.layout.statusBarsPadding
/**
 * The screen behind Home's "Today at a glance" tiles. One generic list for
 * all four tile types instead of four near-identical screens — add a new
 * tile later by adding one HomeListType case + one branch in `rowsFor`.
 */
@Composable
fun FilteredListScreen(
    type: HomeListType,
    viewModel: HomeViewModel,
    onBack: () -> Unit,
    onOpenLead: (String) -> Unit,
    onOpenTasksTab: () -> Unit,
    onOpenCalendarTab: () -> Unit,
    onOpenDealsTab: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()

    data class ListRow(val title: String, val subtitle: String, val onClick: () -> Unit)

    val rows: List<ListRow> = when (type) {
        HomeListType.HotLeads -> state.hotLeads.map { lead ->
            ListRow(
                title = lead.fullName.ifBlank { lead.company },
                subtitle = listOf(lead.company, lead.status).filter { it.isNotBlank() }.joinToString(" · "),
                onClick = { onOpenLead(lead.id) },
            )
        }
        HomeListType.TodayMeetings -> state.todayMeetings.map { item ->
            ListRow(
                title = item.subject,
                subtitle = listOf(item.time, item.location).filter { it.isNotBlank() }.joinToString(" · "),
                onClick = onOpenCalendarTab,
            )
        }
        HomeListType.TodayFollowUps -> state.todayFollowUps.map { task ->
            ListRow(
                title = task.subject,
                subtitle = listOf(task.status, task.dueDate).filter { it.isNotBlank() }.joinToString(" · "),
                onClick = onOpenTasksTab,
            )
        }
        HomeListType.OpenDeals -> state.openDealsList.map { deal ->
            ListRow(
                title = deal.name,
                subtitle = listOf(deal.stageName, deal.amount?.let { "₹%,.0f".format(it) }).filterNotNull().filter { it.isNotBlank() }.joinToString(" · "),
                onClick = onOpenDealsTab,
            )
        }
    }

    Column(modifier = modifier.fillMaxSize().background(CrmBg)) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CrmSurface)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Charcoal)
            }
            Column {
                Text(type.title, fontSize = 15.sp, color = Charcoal)
                Text("${rows.size} ${if (rows.size == 1) "record" else "records"}", fontSize = 11.sp, color = TextMuted)
            }
        }

        if (rows.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Nothing here right now.", fontSize = 13.sp, color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(rows) { row ->
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = row.onClick)
                            .background(CrmSurface, RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        Column {
                            Text(row.title, fontSize = 13.sp, color = Charcoal)
                            if (row.subtitle.isNotBlank()) {
                                Text(row.subtitle, fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}