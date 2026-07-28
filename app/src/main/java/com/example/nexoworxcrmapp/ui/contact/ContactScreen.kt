// Step 11 of 15
// NEW FILE: app/src/main/java/com/example/nexoworxcrmapp/ui/contact/ContactScreen.kt

package com.example.nexoworxcrmapp.ui.contact

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexoworxcrmapp.data.Contact
import com.example.nexoworxcrmapp.ui.theme.BorderGreen
import com.example.nexoworxcrmapp.ui.theme.Charcoal
import com.example.nexoworxcrmapp.ui.theme.CrmBg
import com.example.nexoworxcrmapp.ui.theme.CrmSurface
import com.example.nexoworxcrmapp.ui.theme.Forest
import com.example.nexoworxcrmapp.ui.theme.MutedGreen
import com.example.nexoworxcrmapp.ui.theme.TextMuted
import com.example.nexoworxcrmapp.ui.theme.TextSecondary

private val contactGradients = listOf(
    Brush.linearGradient(listOf(Color(0xFF1A6B4A), Color(0xFF2E9B6E))),
    Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF3B82F6))),
    Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFF8B5CF6))),
    Brush.linearGradient(listOf(Color(0xFF0F766E), Color(0xFF14B8A6))),
    Brush.linearGradient(listOf(Color(0xFFB45309), Color(0xFFF59E0B))),
)

private fun contactMatchesSearch(contact: Contact, query: String): Boolean {
    if (query.isBlank()) return true
    val q = query.trim().lowercase()
    return contact.fullName.lowercase().contains(q) ||
        contact.phone.contains(q) ||
        contact.email.lowercase().contains(q) ||
        contact.accountName.lowercase().contains(q)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
    modifier: Modifier = Modifier,
    viewModel: ContactViewModel = viewModel(),
    onContactClick: (Contact) -> Unit = {},
    onCreateClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    var search by remember { mutableStateOf("") }

    val contacts = uiState.contacts.filter { contactMatchesSearch(it, search) }

    Scaffold(
        modifier = modifier,
        containerColor = CrmBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                containerColor = Forest,
                contentColor = CrmSurface,
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Contact")
            }
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CrmBg),
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "NEXOWORX CRM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MutedGreen,
                        letterSpacing = 0.5.sp,
                    )
                    Text(
                        text = "Contacts",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Charcoal,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Search bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CrmSurface)
                            .border(1.dp, BorderGreen, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MutedGreen,
                            modifier = Modifier.size(16.dp),
                        )
                        BasicTextField(
                            value = search,
                            onValueChange = { search = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            textStyle = TextStyle(fontSize = 13.sp, color = Charcoal),
                            cursorBrush = SolidColor(Forest),
                            decorationBox = { inner ->
                                if (search.isEmpty()) {
                                    Text(
                                        "Search name, phone, or company…",
                                        fontSize = 13.sp,
                                        color = TextMuted,
                                    )
                                }
                                inner()
                            },
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Error
                    val err = uiState.errorMessage
                    if (err != null) {
                        Text(
                            text = err,
                            fontSize = 11.sp,
                            color = Color(0xFFC0392B),
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .clickable { viewModel.refresh() },
                        )
                    }

                    // Stat card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CrmSurface)
                            .border(1.dp, BorderGreen, RoundedCornerShape(12.dp))
                            .padding(vertical = 8.dp, horizontal = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.contacts.size.toString(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Forest,
                            )
                            Text(
                                text = "Total",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MutedGreen,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (contacts.isEmpty() && !uiState.isLoading) {
                        item {
                            Text(
                                text = if (search.isNotBlank()) "No contacts match your search"
                                else "No contacts found",
                                fontSize = 13.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(vertical = 24.dp),
                            )
                        }
                    }
                    itemsIndexed(contacts) { index, contact ->
                        ContactCard(
                            contact = contact,
                            gradient = contactGradients[index % contactGradients.size],
                            onClick = { onContactClick(contact) },
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ContactCard(
    contact: Contact,
    gradient: Brush,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CrmSurface)
            .border(1.dp, BorderGreen, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Gradient avatar
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(brush = gradient),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = contact.initials,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.fullName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Charcoal,
                maxLines = 1,
            )

            // Title · Company
            val subtitle = buildString {
                if (contact.title.isNotBlank()) append(contact.title)
                if (contact.title.isNotBlank() && contact.accountName.isNotBlank()) append(" · ")
                if (contact.accountName.isNotBlank()) append(contact.accountName)
            }
            if (subtitle.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 3.dp),
                ) {
                    Icon(
                        Icons.Default.Business,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(10.dp),
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(start = 3.dp),
                        maxLines = 1,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 4.dp),
            ) {
                if (contact.phone.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(10.dp),
                        )
                        Text(
                            text = contact.phone,
                            fontSize = 11.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(start = 3.dp),
                        )
                    }
                }
                if (contact.email.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(10.dp),
                        )
                        Text(
                            text = contact.email,
                            fontSize = 11.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(start = 3.dp),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}
