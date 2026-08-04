package com.example.nexoworxcrmapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexoworxcrmapp.ui.theme.CrmBg
import com.example.nexoworxcrmapp.ui.theme.Forest
import com.example.nexoworxcrmapp.ui.theme.TextMuted
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.widthIn

/**
 * The Home dashboard. Pure presentation — all data comes from [viewModel],
 * all navigation decisions are delegated to [onRoute] so this file never
 * needs to know about tabs, NavHosts, or other screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onRoute: (HomeActionRoute) -> Unit,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onQuickCreateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()

    // HomeScreen leaves composition when the user switches tabs and is
    // recomposed fresh when they come back to Home — so this refires on
    // every "return to Home", not just the first time the app opens.
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Column(modifier = modifier.fillMaxSize()) {
        HomeHeader(
            actionCount = state.actionItems.size,
            unreadCount = state.unreadCount,
            onSearchClick = onSearchClick,
            onNotificationsClick = onNotificationsClick,
        )
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CrmBg)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                if (state.isLoading && state.stats.isEmpty()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Spacer(Modifier.height(24.dp))
                        CircularProgressIndicator(color = Forest)
                    }
                    return@Column
                }

            HomeSectionHeader(
                "Needs action now",
                trailingCount = state.actionItems.size.takeIf { it > 0 },
            )
            Spacer(Modifier.height(10.dp))
            if (state.actionItems.isEmpty()) {
                Text("You're all caught up.", fontSize = 12.sp, color = TextMuted)
            } else {
                state.actionItems.take(2).forEach { item ->
                    ActionCard(
                        title = item.title,
                        subtitle = item.subtitle,
                        priority = item.priority,
                        icon = item.icon,
                        onClick = { onRoute(item.route) },
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                if (state.actionItems.size > 2) {
                    Text(
                        "See ${state.actionItems.size - 2} more in Notifications",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Forest,
                        modifier = Modifier
                            .clickable(onClick = onNotificationsClick)
                            .padding(vertical = 6.dp),
                    )
                }
            }

            Spacer(Modifier.height(22.dp))
            HomeSectionHeader("Today at a glance")
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.stats.forEach { stat ->
                    StatTile(
                        label = stat.label,
                        value = stat.value,
                        icon = stat.icon,
                        accent = stat.accent,
                        onClick = { onRoute(stat.route) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(22.dp))
            HomeSectionHeader("Jump to")
            Spacer(Modifier.height(10.dp))
            val jumpTiles = listOf(
                Triple("Leads", Icons.Default.People, HomeActionRoute.OpenLeadsList),
                Triple("Accounts", Icons.Default.Domain, HomeActionRoute.OpenAccountsList),
                Triple("Contacts", Icons.Default.Contacts, HomeActionRoute.OpenContactsList),
                Triple("Tasks", Icons.Default.CheckBox, HomeActionRoute.OpenTasks),
                Triple("Deals", Icons.Default.TrendingUp, HomeActionRoute.OpenDeals),
                Triple("Calendar", Icons.Default.CalendarMonth, HomeActionRoute.OpenCalendar),
            )
            jumpTiles.chunked(3).forEach { rowTiles ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowTiles.forEach { (label, icon, route) ->
                        JumpToTile(
                            label = label,
                            icon = icon,
                            onClick = { onRoute(route) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    // Pad the last row so tiles stay the same width as full rows.
                    repeat(3 - rowTiles.size) { Spacer(Modifier.weight(1f)) }
                }
            }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun HomeHeader(
    actionCount: Int,
    unreadCount: Int,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
) {
    val today = remember { LocalDate.now() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Forest)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = today.format(DateTimeFormatter.ofPattern("EEEE, d MMM", Locale.ENGLISH)),
                    fontSize = 11.sp,
                    color = Color(0xFFA8D8BE),
                )
                Text(
                    text = greetingForHour(),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                )
                if (actionCount > 0) {
                    Text(
                        text = "$actionCount ${if (actionCount == 1) "thing needs" else "things need"} attention today",
                        fontSize = 11.sp,
                        color = Color(0xFFD7EEE0),
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                HeaderIconButton(Icons.Default.Search, "Search", onSearchClick)
                HeaderIconButton(Icons.Default.Notifications, "Notifications", onNotificationsClick, badgeCount = unreadCount)
            }
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    badgeCount: Int = 0,
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.size(38.dp),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(38.dp),
        ) {
            Icon(icon, contentDescription = contentDescription, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        if (badgeCount > 0) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .height(16.dp)
                    .widthIn(min = 16.dp)
                    .background(Color(0xFFFF6B6B), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                    fontSize = 9.sp,
                    lineHeight = 9.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 3.dp),
                )
            }
        }
    }
}

private fun greetingForHour(): String {
    val liveHour = java.time.LocalTime.now().hour
    return when {
        liveHour < 12 -> "Good morning"
        liveHour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}