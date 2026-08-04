package com.example.nexoworxcrmapp.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexoworxcrmapp.ui.home.ActionCard
import com.example.nexoworxcrmapp.ui.home.HomeActionRoute
import com.example.nexoworxcrmapp.ui.home.HomeViewModel
import com.example.nexoworxcrmapp.ui.theme.Charcoal
import com.example.nexoworxcrmapp.ui.theme.CrmBg
import com.example.nexoworxcrmapp.ui.theme.CrmSurface
import com.example.nexoworxcrmapp.ui.theme.Forest
import com.example.nexoworxcrmapp.ui.theme.TextMuted
import androidx.compose.foundation.layout.statusBarsPadding

/**
 * Full list version of Home's "Needs action now". Reuses HomeViewModel and
 * ActionCard instead of keeping a second copy of the same logic — when the
 * rules in HomeViewModel.buildActionItems change, both screens update.
 */
@Composable
fun NotificationsScreen(
    viewModel: HomeViewModel,
    onBack: () -> Unit,
    onRoute: (HomeActionRoute) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = modifier.fillMaxSize().background(CrmBg)) {
        Row(
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Notifications",
                    fontSize = 15.sp,
                    color = Charcoal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    if (state.unreadCount > 0) "${state.unreadCount} unread" else "You're all caught up",
                    fontSize = 11.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (state.unreadCount > 0) {
                androidx.compose.material3.TextButton(
                    onClick = { viewModel.markAllAsRead() },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.wrapContentWidth(),
                ) {
                    Text(
                        "Mark all as read",
                        fontSize = 11.sp,
                        color = Forest,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        if (state.actionItems.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("You're all caught up.", fontSize = 13.sp, color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.actionItems, key = { it.id }) { item ->
                    ActionCard(
                        title = item.title,
                        subtitle = item.subtitle,
                        priority = item.priority,
                        icon = item.icon,
                        onClick = { onRoute(item.route) },
                    )
                }
            }
        }
    }
}