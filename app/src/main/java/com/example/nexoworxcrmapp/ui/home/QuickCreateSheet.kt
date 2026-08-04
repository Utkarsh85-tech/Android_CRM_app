package com.example.nexoworxcrmapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexoworxcrmapp.ui.theme.BorderGreen
import com.example.nexoworxcrmapp.ui.theme.Charcoal
import com.example.nexoworxcrmapp.ui.theme.CrmSurface
import com.example.nexoworxcrmapp.ui.theme.Forest
import com.example.nexoworxcrmapp.ui.theme.TextMuted
import androidx.compose.material.icons.filled.TrendingUp

/**
 * Content for the "Quick create" bottom sheet. To add a new creatable type:
 * add a case to QuickCreateTarget, one line to `options` below, and handle
 * it once in NexoworxApp's quick-create wiring.
 */
@Composable
fun QuickCreateSheetContent(
    onSelect: (QuickCreateTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(
        Triple("New lead", Icons.Default.People, QuickCreateTarget.Lead),
        Triple("New account", Icons.Default.Domain, QuickCreateTarget.Account),
        Triple("New contact", Icons.Default.Contacts, QuickCreateTarget.Contact),
        Triple("New task", Icons.Default.CheckBox, QuickCreateTarget.Task),
        Triple("New meeting", Icons.Default.CalendarMonth, QuickCreateTarget.Event),
        Triple("New opportunity", Icons.Default.TrendingUp, QuickCreateTarget.Opportunity),
    )
    Column(modifier = modifier.padding(bottom = 24.dp)) {
        Text(
            "Quick create",
            fontSize = 13.sp,
            color = TextMuted,
            modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 8.dp),
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .background(CrmSurface, RoundedCornerShape(12.dp)),
        ) {
            options.forEachIndexed { index, (label, icon, target) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(target) }
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(icon, contentDescription = label, tint = Forest, modifier = Modifier.padding(end = 14.dp))
                    Text(label, fontSize = 14.sp, color = Charcoal)
                }
                if (index != options.lastIndex) {
                    HorizontalDivider(color = BorderGreen)
                }
            }
        }
    }
}