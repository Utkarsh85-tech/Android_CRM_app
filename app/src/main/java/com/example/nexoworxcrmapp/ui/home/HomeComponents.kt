package com.example.nexoworxcrmapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexoworxcrmapp.ui.theme.BorderGreen
import com.example.nexoworxcrmapp.ui.theme.Charcoal
import com.example.nexoworxcrmapp.ui.theme.CrmSurface
import com.example.nexoworxcrmapp.ui.theme.Danger
import com.example.nexoworxcrmapp.ui.theme.DangerBg
import com.example.nexoworxcrmapp.ui.theme.Forest
import com.example.nexoworxcrmapp.ui.theme.TextMuted
import com.example.nexoworxcrmapp.ui.theme.TextSecondary
import com.example.nexoworxcrmapp.ui.theme.Warning
import com.example.nexoworxcrmapp.ui.theme.WarningBg

/**
 * Small reusable pieces the Home screen is built from. Kept generic (no
 * business logic, no ViewModel access) so they're safe to reuse on other
 * screens later — e.g. Notifications reuses [ActionCard] as-is.
 */

@Composable
fun HomeSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailingCount: Int? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Charcoal,
            letterSpacing = 0.6.sp,
        )
        if (trailingCount != null) {
            Text(
                text = trailingCount.toString(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Danger,
                modifier = Modifier
                    .background(DangerBg, RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
    }
}

@Composable
fun StatTile(
    label: String,
    value: Int,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(CrmSurface, RoundedCornerShape(10.dp))
            .border(0.5.dp, BorderGreen, RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, tint = accent, modifier = Modifier.size(16.dp))
        Text(value.toString(), fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Charcoal)
        Text(label, fontSize = 10.sp, color = TextSecondary)
    }
}

@Composable
fun JumpToTile(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(CrmSurface, RoundedCornerShape(12.dp))
            .border(0.5.dp, BorderGreen, RoundedCornerShape(12.dp))
            .padding(vertical = 14.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, contentDescription = label, tint = Forest, modifier = Modifier.size(22.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Charcoal)
    }
}

/** Priority-colored left border + trailing chevron. Used by both the Home
 *  "Needs action now" list and the standalone Notifications screen. */
@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    priority: ActionPriority,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val (borderColor, iconTint) = when (priority) {
        ActionPriority.High -> Danger to Danger
        ActionPriority.Medium -> Warning to Warning
        ActionPriority.Low -> Forest to Forest
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(CrmSurface, RoundedCornerShape(12.dp))
            .border(0.5.dp, BorderGreen, RoundedCornerShape(12.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .size(width = 3.dp, height = 44.dp)
                .background(borderColor, RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)),
        ) {}
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Charcoal, maxLines = 1)
                if (subtitle.isNotBlank()) {
                    Text(subtitle, fontSize = 11.sp, color = TextMuted, maxLines = 1)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
        }
    }
}