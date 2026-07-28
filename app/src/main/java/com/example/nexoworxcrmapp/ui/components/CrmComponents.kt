package com.example.nexoworxcrmapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexoworxcrmapp.ui.theme.AccentGreen
import com.example.nexoworxcrmapp.ui.theme.Forest
import com.example.nexoworxcrmapp.ui.theme.StatusNewBg
import com.example.nexoworxcrmapp.ui.theme.StatusNewText
import com.example.nexoworxcrmapp.ui.theme.StatusQualifiedBg
import com.example.nexoworxcrmapp.ui.theme.StatusQualifiedText
import com.example.nexoworxcrmapp.ui.theme.StatusUnqualifiedBg
import com.example.nexoworxcrmapp.ui.theme.StatusUnqualifiedText
import com.example.nexoworxcrmapp.ui.theme.StatusWorkingBg
import com.example.nexoworxcrmapp.ui.theme.StatusWorkingText

data class BadgeColors(val background: Color, val text: Color, val dot: Color)

fun statusBadgeColors(status: String): BadgeColors = when (displayLeadStatus(status)) {
    "New" -> BadgeColors(StatusNewBg, StatusNewText, StatusNewText)
    "Working" -> BadgeColors(StatusWorkingBg, StatusWorkingText, StatusWorkingText)
    "Qualified" -> BadgeColors(StatusQualifiedBg, StatusQualifiedText, StatusQualifiedText)
    "Unqualified" -> BadgeColors(StatusUnqualifiedBg, StatusUnqualifiedText, StatusUnqualifiedText)
    else -> BadgeColors(Color(0xFFF3F4F6), Color(0xFF555555), Color(0xFF999999))
}

fun displayLeadStatus(status: String): String = when (status.trim()) {
    "Open - Not Contacted" -> "New"
    "Working - Contacted", "Open - Contacted" -> "Working"
    "Closed - Not Converted" -> "Unqualified"
    "Closed - Converted" -> "Converted"
    else -> status
}

fun leadMatchesStatusFilter(leadStatus: String, filterStatus: String): Boolean =
    filterStatus == "All" || displayLeadStatus(leadStatus) == filterStatus

fun leadMatchesSearch(lead: com.example.nexoworxcrmapp.data.Lead, query: String): Boolean {
    if (query.isBlank()) return true
    val q = query.trim()
    val textHaystack = "${lead.firstName} ${lead.lastName} ${lead.company} ${lead.phone} ${lead.email}"
    if (textHaystack.contains(q, ignoreCase = true)) return true
    val queryDigits = q.filter { it.isDigit() }
    if (queryDigits.length >= 3) {
        val phoneDigits = lead.phone.filter { it.isDigit() }
        if (phoneDigits.contains(queryDigits)) return true
    }
    return false
}

@Composable
fun StatusBadge(label: String, modifier: Modifier = Modifier) {
    val colors = statusBadgeColors(label)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(colors.background)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(colors.dot),
        )
        Text(
            text = displayLeadStatus(label),
            color = colors.text,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

@Composable
fun LeadAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Int = 42,
    background: Color = Forest,
) {
    val initials = name.split(" ")
        .filter { it.isNotBlank() }
        .map { it.first().uppercaseChar() }
        .take(2)
        .joinToString("")
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontSize = (size * 0.36).sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
