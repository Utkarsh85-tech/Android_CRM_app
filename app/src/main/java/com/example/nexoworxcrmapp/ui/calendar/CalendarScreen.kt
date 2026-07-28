package com.example.nexoworxcrmapp.ui.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexoworxcrmapp.data.CalendarDayItem
import com.example.nexoworxcrmapp.data.CrmRepository
import com.example.nexoworxcrmapp.ui.components.StatusBadge
import com.example.nexoworxcrmapp.ui.theme.AccentGreen
import com.example.nexoworxcrmapp.ui.theme.BorderGreen
import com.example.nexoworxcrmapp.ui.theme.Charcoal
import com.example.nexoworxcrmapp.ui.theme.CrmBg
import com.example.nexoworxcrmapp.ui.theme.CrmSurface
import com.example.nexoworxcrmapp.ui.theme.Forest
import com.example.nexoworxcrmapp.ui.theme.MidGreen
import com.example.nexoworxcrmapp.ui.theme.MutedGreen
import com.example.nexoworxcrmapp.ui.theme.TextMuted
import androidx.compose.ui.platform.LocalLocale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(modifier: Modifier = Modifier) {
    val today = remember { LocalDate.now() }
    var calendarMonth by remember { mutableStateOf(LocalDate.of(today.year, today.month, 1)) }
    var selectedDay by remember { mutableIntStateOf(today.dayOfMonth) }
    val daysInMonth = calendarMonth.lengthOfMonth()
    val startDayOffset = calendarMonth.dayOfWeek.value % 7
    val monthLabel = "${calendarMonth.month.getDisplayName(TextStyle.FULL, LocalLocale.current.platformLocale)} ${calendarMonth.year}"
    val allEvents by CrmRepository.calendarItems.collectAsState()
    val dayEvents = allEvents.filter {
        it.date == selectedDay && it.month == calendarMonth.monthValue && it.year == calendarMonth.year
    }
    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CrmBg),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Calendar", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Charcoal)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(
                    onClick = {
                        calendarMonth = calendarMonth.minusMonths(1)
                        selectedDay = 1
                    },
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month", tint = Forest)
                }
                Text(
                    text = monthLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MutedGreen,
                )
                IconButton(
                    onClick = {
                        calendarMonth = calendarMonth.plusMonths(1)
                        selectedDay = 1
                    },
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next month", tint = Forest)
                }
            }
        }
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CrmSurface)
                .border(1.dp, BorderGreen, RoundedCornerShape(16.dp))
                .padding(14.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                dayLabels.forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedGreen,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            val cells = startDayOffset + daysInMonth
            val rows = (cells + 6) / 7
            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val index = row * 7 + col
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            val day = index - startDayOffset + 1
                            if (day in 1..daysInMonth) {
                                val isSelected = day == selectedDay
                                val isToday = day == today.dayOfMonth &&
                                    calendarMonth.month == today.month &&
                                    calendarMonth.year == today.year
                                val types = allEvents
                                    .filter {
                                        it.date == day &&
                                            it.month == calendarMonth.monthValue &&
                                            it.year == calendarMonth.year
                                    }
                                    .map { it.type }
                                    .distinct()
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> Forest
                                                isToday -> AccentGreen.copy(alpha = 0.25f)
                                                else -> Color.Transparent
                                            },
                                        )
                                        .clickable { selectedDay = day },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = day.toString(),
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isSelected -> CrmSurface
                                                isToday -> Forest
                                                else -> Charcoal
                                            },
                                        )
                                        if (types.isNotEmpty() && !isSelected) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                if (types.contains("event")) {
                                                    Box(
                                                        Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(Forest),
                                                    )
                                                }
                                                if (types.contains("task")) {
                                                    Box(
                                                        Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(AccentGreen),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LegendDot(Forest, "Event")
                LegendDot(AccentGreen, "Task")
            }
        }
        val suffix = if (dayEvents.size == 1) "" else "s"
        Text(
            text = "$selectedDay $monthLabel · ${dayEvents.size} item$suffix",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MutedGreen,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 10.dp),
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (dayEvents.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = BorderGreen,
                            modifier = Modifier.size(32.dp),
                        )
                        Text(
                            "No events or tasks",
                            fontSize = 13.sp,
                            color = MutedGreen,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            } else {
                items(dayEvents) { event -> CalendarEventCard(event) }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(text = label, fontSize = 10.sp, color = MutedGreen)
    }
}

@Composable
private fun CalendarEventCard(event: CalendarDayItem) {
    val isEvent = event.type == "event"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CrmSurface)
            .border(1.dp, BorderGreen, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isEvent) Forest.copy(alpha = 0.12f) else AccentGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isEvent) Icons.Default.Event else Icons.Default.TaskAlt,
                contentDescription = null,
                tint = if (isEvent) Forest else MidGreen,
                modifier = Modifier.size(16.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(event.subject, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Charcoal)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                Icon(Icons.Default.AccessTime, contentDescription = null, tint = TextMuted, modifier = Modifier.size(10.dp))
                Text(
                    text = "${event.time} · ${if (isEvent) "Event" else "Task Due"}",
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(start = 3.dp),
                )
            }
        }
        StatusBadge(if (isEvent) "Upcoming" else "Open")
    }
}
