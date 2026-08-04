package com.example.nexoworxcrmapp.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexoworxcrmapp.data.Account
import com.example.nexoworxcrmapp.data.Contact
import com.example.nexoworxcrmapp.data.CrmRepository
import com.example.nexoworxcrmapp.data.Lead
import com.example.nexoworxcrmapp.ui.components.leadMatchesSearch
import com.example.nexoworxcrmapp.ui.theme.Charcoal
import com.example.nexoworxcrmapp.ui.theme.CrmBg
import com.example.nexoworxcrmapp.ui.theme.CrmSurface
import com.example.nexoworxcrmapp.ui.theme.TextMuted
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.statusBarsPadding

/**
 * Local, client-side search across Leads / Accounts / Contacts already held
 * in CrmRepository. Not a server search — fine for the data volumes this
 * app deals with, and results are instant with no network calls once the
 * three lists are loaded.
 */
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenLead: (String) -> Unit,
    onOpenAccount: (String) -> Unit,
    onOpenContact: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        scope.launch { CrmRepository.refreshAccounts() }
        scope.launch { CrmRepository.refreshContacts() }
    }

    val leads by CrmRepository.leads.collectAsState()
    val accounts by CrmRepository.accounts.collectAsState()
    val contacts by CrmRepository.contacts.collectAsState()

    val matchedLeads = remember(query, leads) {
        if (query.isBlank()) emptyList() else leads.filter { leadMatchesSearch(it, query) }.take(6)
    }
    val matchedAccounts = remember(query, accounts) {
        if (query.isBlank()) emptyList() else accounts.filter { it.name.contains(query, ignoreCase = true) }.take(6)
    }
    val matchedContacts = remember(query, contacts) {
        if (query.isBlank()) {
            emptyList()
        } else {
            contacts.filter { "${it.firstName} ${it.lastName}".contains(query, ignoreCase = true) }.take(6)
        }
    }
    val hasResults = matchedLeads.isNotEmpty() || matchedAccounts.isNotEmpty() || matchedContacts.isNotEmpty()

    Column(modifier = modifier.fillMaxSize().background(CrmBg)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CrmSurface)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Charcoal)
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search leads, accounts, contacts") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CrmBg,
                    unfocusedContainerColor = CrmBg,
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
            )
        }

        when {
            query.isBlank() -> CenteredHint("Start typing to search across leads, accounts, and contacts.")
            !hasResults -> CenteredHint("No matches for \"$query\".")
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (matchedLeads.isNotEmpty()) {
                    item { SearchSectionLabel("Leads") }
                    items(matchedLeads, key = { "lead_${it.id}" }) { lead ->
                        SearchResultRow(
                            title = lead.fullName.ifBlank { lead.company },
                            subtitle = lead.company,
                            onClick = { onOpenLead(lead.id) },
                        )
                    }
                }
                if (matchedAccounts.isNotEmpty()) {
                    item { SearchSectionLabel("Accounts") }
                    items(matchedAccounts, key = { "account_${it.id}" }) { account ->
                        SearchResultRow(
                            title = account.name,
                            subtitle = account.industry,
                            onClick = { onOpenAccount(account.id) },
                        )
                    }
                }
                if (matchedContacts.isNotEmpty()) {
                    item { SearchSectionLabel("Contacts") }
                    items(matchedContacts, key = { "contact_${it.id}" }) { contact ->
                        SearchResultRow(
                            title = contact.fullName,
                            subtitle = contact.accountName,
                            onClick = { onOpenContact(contact.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSectionLabel(text: String) {
    Text(
        text.uppercase(),
        fontSize = 11.sp,
        color = TextMuted,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
    )
}

@Composable
private fun SearchResultRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(CrmSurface, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Column {
            Text(title, fontSize = 13.sp, color = Charcoal)
            if (subtitle.isNotBlank()) {
                Text(subtitle, fontSize = 11.sp, color = TextMuted)
            }
        }
    }
}

@Composable
private fun CenteredHint(text: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text, fontSize = 12.sp, color = TextMuted)
    }
}