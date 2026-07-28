package com.example.nexoworxcrmapp.ui.quote

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexoworxcrmapp.data.quote.*
import com.example.nexoworxcrmapp.ui.theme.*
import java.text.NumberFormat
import java.util.Locale
import com.example.nexoworxcrmapp.ui.quote.QuoteCreationViewModel
import com.example.nexoworxcrmapp.ui.quote.QuoteCreationState
import com.example.nexoworxcrmapp.network.NetworkModule
import com.example.nexoworxcrmapp.network.ApiResult
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.nexoworxcrmapp.ui.common.DatePickerField
import com.example.nexoworxcrmapp.ui.common.DatePickerField
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.nexoworxcrmapp.data.quote.CpqFavourite
@Composable
fun QuoteListScreen(
    opportunityId: String,
    accountId: String = "",
    onBack: () -> Unit,
    onCreateQuote: () -> Unit,
    onOpenQuote: (quoteId: String) -> Unit,
) {
    val repo = remember { NetworkModule.cpqRepository }
    var quotes by remember { mutableStateOf<List<CpqQuote>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val fmt = NumberFormat.getCurrencyInstance(Locale.US)

    LaunchedEffect(opportunityId) {
        isLoading = true
        // Fetch quotes for this opportunity
        // We use accountId="" here since we fetch by opportunityId via workspace
        when (val r = repo.getQuotesByOpportunity(accountId, opportunityId)) {            is ApiResult.Success -> { quotes = r.data; isLoading = false }
            is ApiResult.Error -> { error = r.message; isLoading = false }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(CrmBg)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Forest).padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CrmSurface)
            }
            Text("Quotes", modifier = Modifier.weight(1f), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CrmSurface)
        }

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Forest)
            }
            error != null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(error.orEmpty(), color = Color(0xFFC0392B))
            }
            else -> LazyColumn(
                modifier = Modifier.weight(1f).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (quotes.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No quotes yet for this opportunity.", color = TextMuted, fontSize = 14.sp)
                        }
                    }
                } else {
                    items(quotes, key = { it.id }) { quote ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                .background(CrmSurface).border(1.dp, BorderGreen, RoundedCornerShape(12.dp))
                                .clickable { onOpenQuote(quote.id) }.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(quote.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Charcoal)
                                Text(fmt.format(quote.grandTotal), fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(top = 2.dp))
                            }
                            QuoteStatusBadge(quote.status)
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().background(CrmSurface).padding(14.dp)) {
            Button(
                onClick = onCreateQuote,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Forest),
                shape = RoundedCornerShape(10.dp),
            ) { Text("+ Create New Quote", fontWeight = FontWeight.Bold) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteCreationScreen(
    onQuoteCreated: (quoteId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: QuoteCreationViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val formState by viewModel.formState.collectAsState()

    LaunchedEffect(state) {
        if (state is QuoteCreationState.Success) {
            onQuoteCreated((state as QuoteCreationState.Success).quoteId)
        }
    }

    if (state is QuoteCreationState.Creating) {
        Box(modifier = Modifier.fillMaxSize().background(CrmBg), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Forest)
                Spacer(Modifier.height(16.dp))
                Text("Creating quote...", fontSize = 14.sp, color = TextMuted)
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().background(CrmBg)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().background(Forest).padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CrmSurface)
            }
            Text("Create New Quote", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CrmSurface)
        }

        if (state is QuoteCreationState.Error) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFC0392B), modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Failed to create quote", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Charcoal)
                    Text((state as QuoteCreationState.Error).message, fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(top = 4.dp))
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.submitForm() }, colors = ButtonDefaults.buttonColors(containerColor = Forest), shape = RoundedCornerShape(10.dp)) { Text("Try Again") }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onBack) { Text("Cancel", color = TextMuted) }
                }
            }
            return@Column
        }

        LazyColumn(modifier = Modifier.weight(1f).padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Quote Name
            item {
                QuoteFormField(
                    label = "Quote Name *",
                    value = formState.quoteName,
                    placeholder = "e.g. Fleet Deal Q3 2026",
                    onValueChange = { viewModel.setQuoteName(it) },
                )
            }

            // Account (pre-filled, read-only)
            item {
                QuoteFormReadOnly(label = "Account", value = formState.accountName)
            }

            // Opportunity (pre-filled, read-only)
            item {
                QuoteFormReadOnly(label = "Opportunity", value = formState.opportunityName)
            }

            // Expiration Date
            item {
                DatePickerField(
                    label = "Expiration Date",
                    value = formState.expirationDate,
                    onValueChange = { viewModel.setExpirationDate(it) },
                    outputFormat = "dd-MM-yyyy",
                )
            }

            // Start Date (pre-filled with today)
            item {
                DatePickerField(
                    label = "Start Date",
                    value = formState.startDate,
                    onValueChange = { viewModel.setStartDate(it) },
                    outputFormat = "dd-MM-yyyy",
                )
            }

            // End Date
            item {
                QuoteFormField(
                    label = "Term (months)",
                    value = formState.term,
                    placeholder = "e.g. 12",
                    onValueChange = { viewModel.setTerm(it) },
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                )
            }

// End Date (auto-computed, read-only)
            item {
                QuoteFormReadOnly(
                    label = "End Date",
                    value = formState.endDate.ifBlank { "— set Term to auto-fill —" },
                )
            }

            // Description
            item {
                Column {
                    Text("Description", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, modifier = Modifier.padding(bottom = 6.dp))
                    OutlinedTextField(
                        value = formState.description,
                        onValueChange = { viewModel.setDescription(it) },
                        placeholder = { Text("Additional notes or instructions...", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(10.dp),
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }

        // Bottom action bar
        Row(
            modifier = Modifier.fillMaxWidth().background(CrmSurface)
                .border(1.dp, BorderGreen, RoundedCornerShape(0.dp)).padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Forest),
            ) { Text("Cancel") }
            Button(
                onClick = { viewModel.submitForm() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Forest),
                shape = RoundedCornerShape(10.dp),
                enabled = formState.quoteName.isNotBlank(),
            ) { Text("Create Quote", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun QuoteFormField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    Column {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextMuted) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            keyboardOptions = keyboardOptions,
        )
    }
}

@Composable
private fun QuoteFormReadOnly(label: String, value: String) {
    Column {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, modifier = Modifier.padding(bottom = 6.dp))
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                .background(Mint).border(1.dp, BorderGreen, RoundedCornerShape(10.dp)).padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Forest))
            Spacer(Modifier.width(8.dp))
            Text(value, fontSize = 14.sp, color = Charcoal)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteWorkspaceScreen(
    onBack: () -> Unit,
    viewModel: QuoteViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val fmt = NumberFormat.getCurrencyInstance(Locale.US)

    // PDF viewer overlay — takes over the screen whenever bytes are downloaded
    if (state.pdfBytes != null) {
        PdfViewerScreen(pdfBytes = state.pdfBytes!!, onBack = { viewModel.closePdfViewer() })
        return
    }


    // Save Favourite dialog
    if (state.showSaveFavouriteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissSaveFavouriteDialog,
            containerColor = CrmSurface,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                    Text("Add to Favourites", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "${state.lines.size} saved item(s) will be saved as a reusable template with full configuration.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Mint)
                            .padding(10.dp),
                    )
                    OutlinedTextField(
                        value = state.favouriteLabel,
                        onValueChange = { viewModel.setFavouriteLabel(it) },
                        label = { Text("Template Name *") },
                        placeholder = { Text("e.g. Fleet 5-Truck Bundle Config") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                    )
                    OutlinedTextField(
                        value = state.favouriteDescription,
                        onValueChange = { viewModel.setFavouriteDescription(it) },
                        label = { Text("Description") },
                        placeholder = { Text("Optional notes about this config...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(10.dp),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::saveFavourite,
                    enabled = state.favouriteLabel.isNotBlank() && !state.isSavingFavourite,
                    colors = ButtonDefaults.buttonColors(containerColor = Forest),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    if (state.isSavingFavourite) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CrmSurface, strokeWidth = 2.dp)
                    } else {
                        Text("Save to Fav", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissSaveFavouriteDialog) {
                    Text("Cancel", color = TextMuted)
                }
            },
        )
    }

// Favourites sheet
    if (state.showFavouritesSheet) {
        FavouritesSheet(
            favourites = state.favourites,
            isLoading = state.isFavouritesLoading,
            isLoadingLines = state.isLoadingFavouriteLines,
            onDismiss = viewModel::closeFavouritesSheet,
            onLoad = { viewModel.loadFavouriteToQuote(it) },
            onDelete = { viewModel.deleteFavourite(it) },
        )
    }
    // Error dialog
    // Error dialog
    if (state.saveError != null) {
        AlertDialog(
            onDismissRequest = viewModel::clearSaveError,
            title = { Text("Error", fontWeight = FontWeight.Bold) },
            text = { Text(state.saveError.orEmpty()) },
            confirmButton = { TextButton(onClick = viewModel::clearSaveError) { Text("OK", color = Forest) } },
            containerColor = CrmSurface, shape = RoundedCornerShape(16.dp),
        )
    }

    // Product config bottom sheet
    if (state.configuringProduct != null) {
        ProductConfigSheet(
            product = state.configuringProduct!!,

            attributes = state.attributes,
            bundleComponents = state.bundleComponents,
            isLoading = state.isConfigLoading,
            isSaving = state.isSaving,
            quantity = state.configQuantity,
            unitPrice = state.configUnitPrice,

            selectedAttributes = state.selectedAttributes,
            onQuantityChange = { viewModel.setConfigQuantity(it) },
            onPriceChange = { viewModel.setConfigPrice(it) },

            onAttributeSelect = { name, value -> viewModel.selectAttribute(name, value) },
            componentQuantities = state.componentQuantities,
            onComponentQtyChange = { id, qty -> viewModel.setComponentQuantity(id, qty) },
            onDismiss = viewModel::dismissConfig,
            onAddToQuote = viewModel::addProductToQuote,
        )
    }

    // Edit line bottom sheet
    if (state.editingLine != null) {
        EditLineSheet(
            line = state.editingLine!!,
            qty = state.editQty,
            price = state.editPrice,
            isSaving = state.isSaving,
            onQtyChange = { viewModel.setEditQty(it) },
            onPriceChange = { viewModel.setEditPrice(it) },
            onDismiss = viewModel::dismissEditLine,
            onSave = viewModel::saveEditLine,
        )
    }

    if (state.isEditingHeader) {
        EditQuoteHeaderSheet(
            name = state.editQuoteName,
            startDate = state.editStartDate,
            expirationDate = state.editExpirationDate,
            term = state.editTerm,
            endDate = state.editEndDate,
            description = state.editDescription,
            isSaving = state.isSavingHeader,
            onNameChange = { viewModel.setEditQuoteName(it) },
            onStartDateChange = { viewModel.setEditStartDate(it) },
            onExpirationDateChange = { viewModel.setEditExpirationDate(it) },
            onTermChange = { viewModel.setEditTerm(it) },
            onDescriptionChange = { viewModel.setEditDescription(it) },
            onDismiss = viewModel::dismissEditQuoteHeader,
            onSave = viewModel::saveQuoteHeader,
        )
    }

    when (state.screen) {
        QuoteScreen.Catalog -> CatalogScreen(state = state, viewModel = viewModel)
        QuoteScreen.Approval -> ApprovalScreen(state = state, viewModel = viewModel)
        QuoteScreen.Pdf -> PdfScreen(state = state, context = context, viewModel = viewModel)
        QuoteScreen.Workspace -> {
            Column(modifier = Modifier.fillMaxSize().background(CrmBg)) {
                // App bar
                Row(
                    modifier = Modifier.fillMaxWidth().background(Forest).padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CrmSurface)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            state.quote?.name ?: "Quote",
                            fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CrmSurface,
                        )
                        if (state.quote != null) {
                            Text(
                                state.quote!!.opportunityName.ifBlank { state.quote!!.opportunityId },
                                fontSize = 11.sp, color = CrmSurface.copy(alpha = 0.7f),
                            )
                        }
                    }
                    // Favourites button
                    IconButton(onClick = viewModel::openFavouritesSheet) {
                        Icon(Icons.Default.StarBorder, contentDescription = "Favourites", tint = CrmSurface)
                    }
// Save to favourite button
                    IconButton(onClick = viewModel::openSaveFavouriteDialog) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = "Save Favourite", tint = CrmSurface)
                    }
                    QuoteStatusBadge(state.quote?.status ?: "Draft")
                    if (state.quote != null) {
                        IconButton(onClick = { viewModel.openEditQuoteHeader() }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Quote", tint = CrmSurface, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.width(4.dp))
                }

                when {
                    state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Forest)
                    }
                    state.errorMessage != null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.errorMessage.orEmpty(), color = Color(0xFFC0392B))
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = viewModel::loadQuote, colors = ButtonDefaults.buttonColors(containerColor = Forest)) { Text("Retry") }
                        }
                    }
                    else -> {
                        val quote = state.quote
                        LazyColumn(
                            modifier = Modifier.weight(1f).padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            // Total card
                            if (quote != null) {
                                item {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Forest).padding(14.dp),
                                    ) {
                                        Text("Quote total", fontSize = 11.sp, color = CrmSurface.copy(alpha = 0.7f))
                                        Text(fmt.format(quote.grandTotal), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = CrmSurface, modifier = Modifier.padding(top = 2.dp))
                                        Text(
                                            "Subtotal ${fmt.format(quote.subtotal)}",                                            fontSize = 11.sp, color = CrmSurface.copy(alpha = 0.7f), modifier = Modifier.padding(top = 4.dp),
                                        )
                                        Divider(color = CrmSurface.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 10.dp))
                                        QuoteHeaderDetailRow("Quote Number", quote.quoteNumber.ifBlank { "—" })
                                        QuoteHeaderDetailRow("Start Date", quote.startDate.ifBlank { "—" })
                                        QuoteHeaderDetailRow("Term (months)", quote.term?.toString() ?: "—")
                                        QuoteHeaderDetailRow("End Date", quote.endDate.ifBlank { "—" })
                                        QuoteHeaderDetailRow("Expiration Date", quote.expirationDate.ifBlank { "—" })
                                        QuoteHeaderDetailRow("Opportunity", quote.opportunityName.ifBlank { "—" })
                                        QuoteHeaderDetailRow("Account", quote.accountName.ifBlank { "—" })
                                    }
                                }

                                // Stage rail
                                item { StageRail(currentStatus = quote.status) }
                            }

                            // Lines header
                            item {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "QUOTE LINES · ${state.lines.size}",
                                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextMuted,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }

                            if (state.lines.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                            .background(CrmSurface).border(1.dp, BorderGreen, RoundedCornerShape(12.dp)).padding(20.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text("No products yet. Tap \"Add products\" to open the catalog.", color = TextMuted, fontSize = 13.sp)
                                    }
                                }
                            } else {
                                state.lines.forEach { parent ->
                                    item(key = parent.id) {
                                        var expanded by remember { mutableStateOf(false) }
                                        val hasComponents = parent.components.isNotEmpty()
                                        Column {
                                            QuoteLineCard(
                                                line = parent,
                                                displayType = parent.type,
                                                isComponent = false,
                                                fmt = fmt,
                                                onEdit = { viewModel.openEditLine(parent) },
                                                onDelete = { viewModel.deleteLine(parent.id) },
                                                isExpandable = hasComponents,
                                                isExpanded = expanded,
                                                onToggleExpand = { expanded = !expanded },
                                            )
                                            if (expanded && hasComponents) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                                                        .background(Mint)
                                                        .border(1.dp, BorderGreen, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                                ) {
                                                    parent.components.forEach { comp ->
                                                        QuoteLineCardComponent(comp = comp, fmt = fmt)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            item { Spacer(Modifier.height(100.dp)) }
                        }

                        // Action bar
                        val quote2 = state.quote
                        if (quote2 != null) {
                            QuoteActionBar(
                                status = quote2.status,
                                isSaving = state.isSaving,
                                onAddProducts = { viewModel.openCatalog() },
                                onApproval = { viewModel.openApproval() },
                                onPdf = { viewModel.openPdf() },
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Catalog Screen ────────────────────────────────────────────────────────────

@Composable
private fun CatalogScreen(state: QuoteUiState, viewModel: QuoteViewModel) {
    Column(modifier = Modifier.fillMaxSize().background(CrmBg)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Forest).padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = viewModel::goToWorkspace) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CrmSurface)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Add products", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CrmSurface)
                Text("Select from catalog", fontSize = 11.sp, color = CrmSurface.copy(alpha = 0.7f))
            }
        }

        when {
            state.isCatalogLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Forest)
            }
            else -> {
                // Category chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        CategoryChip(
                            label = "All",
                            selected = state.selectedCategoryId.isEmpty(),
                            onClick = { viewModel.selectCategory("") },
                        )
                    }
                    items(state.categories) { cat ->
                        CategoryChip(
                            label = cat.name,
                            selected = state.selectedCategoryId == cat.id,
                            onClick = { viewModel.selectCategory(cat.id) },
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    items(state.products, key = { it.id }) { product ->
                        ProductCatalogCard(
                            product = product,
                            onConfigure = { viewModel.configureProduct(product) },
                            onQuickAdd = { qty -> viewModel.quickAddStandaloneLine(product, qty) },
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }

                // Back to quote button
                Box(
                    modifier = Modifier.fillMaxWidth().background(CrmSurface)
                        .border(1.dp, BorderGreen, RoundedCornerShape(0.dp)).padding(14.dp),
                ) {
                    Button(
                        onClick = viewModel::goToWorkspace,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Forest),
                        shape = RoundedCornerShape(10.dp),
                    ) { Text("Back to quote", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

// ── Approval Screen ───────────────────────────────────────────────────────────

@Composable
private fun ApprovalScreen(state: QuoteUiState, viewModel: QuoteViewModel) {
    var comments by remember { mutableStateOf(state.approvalComments) }

    Column(modifier = Modifier.fillMaxSize().background(CrmBg)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Forest).padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = viewModel::goToWorkspace) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CrmSurface)
            }
            Text("Submit for Approval", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CrmSurface)
        }

        LazyColumn(modifier = Modifier.weight(1f).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Approval status
            if (state.approvalStatus != null) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(CrmSurface).border(1.dp, BorderGreen, RoundedCornerShape(12.dp)).padding(14.dp),
                    ) {
                        Text("APPROVAL STATUS", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, letterSpacing = 0.5.sp)
                        Text(state.approvalStatus!!.status, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Forest, modifier = Modifier.padding(top = 4.dp))
                        if (state.approvalStatus!!.steps.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            state.approvalStatus!!.steps.forEach { step ->
                                ApprovalStepRow(step)
                            }
                        }
                    }
                }
            }

            // Comments field
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Comments", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, modifier = Modifier.padding(bottom = 6.dp))
                    OutlinedTextField(
                        value = comments,
                        onValueChange = { comments = it; viewModel.setApprovalComments(it) },
                        placeholder = { Text("Add comments for the approver...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().background(CrmSurface).padding(14.dp)) {
            Button(
                onClick = viewModel::submitApproval,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Forest),
                shape = RoundedCornerShape(10.dp),
                enabled = !state.isSubmittingApproval,
            ) {
                if (state.isSubmittingApproval) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CrmSurface, strokeWidth = 2.dp)
                else Text("Submit for Approval", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── PDF Screen ────────────────────────────────────────────────────────────────

@Composable
private fun PdfScreen(state: QuoteUiState, context: Context, viewModel: QuoteViewModel) {
    Column(modifier = Modifier.fillMaxSize().background(CrmBg)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Forest).padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = viewModel::goToWorkspace) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CrmSurface)
            }
            Text("Generate PDF", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CrmSurface)
        }

        LazyColumn(modifier = Modifier.weight(1f).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (state.pdfDownloadUrl != null) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(CardGreen).border(1.dp, BorderGreen, RoundedCornerShape(12.dp)).padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Forest, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("PDF Generated!", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Forest)
                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.downloadPdfToDevice(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = Forest),
                            shape = RoundedCornerShape(10.dp),
                        ) { Text(if (state.isDownloadingPdf) "Downloading..." else "Download PDF") }

                        Spacer(Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { viewModel.downloadPdfForViewing() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Forest),
                            shape = RoundedCornerShape(10.dp),
                        ) { Text(if (state.isDownloadingPdf) "Loading..." else "View PDF") }
                    }
                }
            }

            items(state.pdfTemplates) { template ->
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(CrmSurface).border(1.dp, BorderGreen, RoundedCornerShape(12.dp))
                        .clickable { viewModel.generatePdf(template.id) }.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = Forest, modifier = Modifier.size(24.dp))
                    Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                        Text(template.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Charcoal)
                        Text("Tap to generate", fontSize = 11.sp, color = TextMuted)
                    }
                    if (state.isGeneratingPdf) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Forest, strokeWidth = 2.dp)
                    else Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                }
            }
        }
    }
}

// ── Components ────────────────────────────────────────────────────────────────

@Composable
private fun StageRail(currentStatus: String) {
    val stages = listOf("Draft", "Finalized", "In review", "Approved", "Ordered")
    val currentIndex = stages.indexOfFirst { it.equals(currentStatus, ignoreCase = true) }.coerceAtLeast(0)

    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(CrmSurface).border(1.dp, BorderGreen, RoundedCornerShape(12.dp)).padding(14.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            stages.forEachIndexed { index, stage ->
                val isDone = index < currentIndex
                val isNow = index == currentIndex
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier.size(22.dp).clip(CircleShape).background(
                            when { isDone -> AccentGreen; isNow -> Forest; else -> BorderGreen }
                        ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isDone || isNow) Icon(Icons.Default.Check, contentDescription = null, tint = CrmSurface, modifier = Modifier.size(12.dp))
                        else Text("${index + 1}", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        stage, fontSize = 9.sp,
                        color = if (isNow) Forest else TextMuted,
                        fontWeight = if (isNow) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
                if (index < stages.size - 1) {
                    Box(modifier = Modifier.weight(0.3f).height(2.dp).background(if (isDone) AccentGreen else BorderGreen))
                }
            }
        }
    }
}

@Composable
private fun QuoteLineCard(
    line: CpqQuoteLine,
    displayType: String = line.type,
    isComponent: Boolean = false,
    fmt: NumberFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isExpandable: Boolean = false,
    isExpanded: Boolean = false,
    onToggleExpand: () -> Unit = {},
) {
    var showDelete by remember { mutableStateOf(false) }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Remove Product", fontWeight = FontWeight.Bold) },
            text = { Text("Remove \"${line.productName}\"?") },
            confirmButton = {
                Button(onClick = { showDelete = false; onDelete() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0392B)), shape = RoundedCornerShape(10.dp)) { Text("Remove") }
            },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancel", color = Forest) } },
            containerColor = CrmSurface, shape = RoundedCornerShape(16.dp),
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(CrmSurface).border(1.dp, BorderGreen, RoundedCornerShape(12.dp)).padding(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    line.productName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isComponent) TextMuted else Charcoal,
                    modifier = if (isComponent) Modifier.padding(start = 12.dp) else Modifier,
                )
                if (displayType.isNotBlank()) {
                    Box(
                        modifier = Modifier.padding(top = 3.dp).clip(RoundedCornerShape(99.dp))
                            .background(when(displayType.lowercase()) {
                                "bundle" -> CardGreen
                                "component" -> Color(0xFFE3F2FD)
                                "accessory" -> Color(0xFFFCE4EC)
                                "option" -> Color(0xFFFFF3E0)
                                else -> Color(0xFFEEF1EF)
                            }).padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            displayType,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = when(displayType.lowercase()) {
                                "bundle" -> Forest
                                "component" -> Color(0xFF1565C0)
                                "accessory" -> Color(0xFFC62828)
                                "option" -> Color(0xFFE65100)
                                else -> TextMuted
                            },
                        )
                    }
                }
                Text("${line.quantity.toInt()} × ${fmt.format(line.unitPrice)}", fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(top = 3.dp))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(fmt.format(line.totalPrice), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Charcoal)
                if (line.netTotalPrice > 0 && line.netTotalPrice != line.totalPrice) {
                    Text("Net: ${fmt.format(line.netTotalPrice)}", fontSize = 10.sp, color = MidGreen)
                }
            }
            if (!isComponent) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Forest, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = { showDelete = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFC0392B), modifier = Modifier.size(16.dp))
                }
            }
        }
        if (line.discount > 0) {
            Text("Discount: ${line.discount.toInt()}%  −${fmt.format(line.listPrice - line.netUnitPrice)}", fontSize = 11.sp, color = MidGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        }
        if (line.discount > 0) {
            Text("Discount: ${fmt.format(line.discount)}", fontSize = 11.sp, color = MidGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        }
        if (isExpandable) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (isExpanded) "▲ Hide components" else "▼ Show components (${line.components.size})",
                    fontSize = 11.sp,
                    color = Forest,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun QuoteLineCardComponent(comp: CpqLineComponent, fmt: NumberFormat) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, top = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(BorderGreen))
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(comp.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
            Text("${comp.quantity.toInt()} × ${fmt.format(comp.unitPrice)}", fontSize = 10.sp, color = TextMuted)
        }
        Text(
            if (comp.isIncludedInPrice) "Included" else fmt.format(comp.totalPrice),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (comp.isIncludedInPrice) MutedGreen else Charcoal,
        )
    }
}
@Composable
private fun ProductCatalogCard(product: CpqProduct, onConfigure: () -> Unit, onQuickAdd: (Int) -> Unit) {
    val fmt = NumberFormat.getCurrencyInstance(Locale.US)
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(CrmSurface).border(1.dp, BorderGreen, RoundedCornerShape(12.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(product.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Charcoal)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 3.dp)) {
                ProductTypeBadge(product.type)
            }
            Text(
                fmt.format(product.price) + if (product.billingFrequency.isNotBlank()) " · ${product.billingFrequency}" else "",
                fontSize = 12.sp, color = TextMuted, modifier = Modifier.padding(top = 2.dp),
            )
            if (product.productCode.isNotBlank()) {
                Text(product.productCode, fontSize = 10.sp, color = TextMuted, modifier = Modifier.padding(top = 2.dp))
            }
        }
        if (product.type.equals("bundle", ignoreCase = true)) {            OutlinedButton(
                onClick = onConfigure,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Forest),
                modifier = Modifier.height(34.dp),
            ) { Text("Configure", fontSize = 12.sp) }
        } else {
            // Stepper for standalone — adds/updates the line directly, never opens the config sheet
            var qty by remember { mutableStateOf(0) }
            Row(
                modifier = Modifier.clip(RoundedCornerShape(9.dp)).border(1.dp, BorderGreen, RoundedCornerShape(9.dp)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { if (qty > 0) { qty--; onQuickAdd(qty) } },
                    modifier = Modifier.size(34.dp).background(Mint),
                ) { Text("−", fontSize = 16.sp, color = Forest, fontWeight = FontWeight.Bold) }
                Text("$qty", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(
                    onClick = { qty++; onQuickAdd(qty) },
                    modifier = Modifier.size(34.dp).background(Mint),
                ) { Text("+", fontSize = 16.sp, color = Forest, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(99.dp))
            .background(if (selected) Forest else CrmSurface)
            .border(1.dp, if (selected) Forest else BorderGreen, RoundedCornerShape(99.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(label, fontSize = 12.sp, color = if (selected) CrmSurface else Charcoal, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun ProductTypeBadge(type: String) {
    val (bg, fg) = if (type.equals("bundle", ignoreCase = true)) CardGreen to Forest else Color(0xFFEEF1EF) to TextMuted
    Box(modifier = Modifier.clip(RoundedCornerShape(99.dp)).background(bg).padding(horizontal = 7.dp, vertical = 2.dp)) {
        Text(type, fontSize = 10.sp, color = fg, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun QuoteStatusBadge(status: String) {
    val (bg, fg) = when (status.lowercase()) {
        "draft" -> Color(0xFFE7ECE8) to Color(0xFF5A6A5E)
        "finalized" -> CardGreen to Forest
        "in review", "review" -> Color(0xFFFBF3DC) to Color(0xFF8A6410)
        "approved" -> Color(0xFFDCF5E7) to Color(0xFF0E6B3E)
        "ordered" -> Forest to CrmSurface
        else -> CardGreen to Forest
    }
    Box(modifier = Modifier.clip(RoundedCornerShape(99.dp)).background(bg).padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}
@Composable
private fun QuoteHeaderDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 11.sp, color = CrmSurface.copy(alpha = 0.65f))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CrmSurface)
    }
}

@Composable
private fun QuoteActionBar(
    status: String,
    isSaving: Boolean,
    onAddProducts: () -> Unit,
    onApproval: () -> Unit,
    onPdf: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().background(CrmSurface)
            .border(1.dp, BorderGreen, RoundedCornerShape(0.dp)).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when (status.lowercase()) {
            "draft" -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onAddProducts,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Forest),
                    ) { Text("+ Add products") }
                    Button(
                        onClick = onApproval,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Forest),
                        shape = RoundedCornerShape(10.dp),
                    ) { Text("Submit for approval") }
                }
            }
            "approved" -> {
                Button(
                    onClick = onPdf,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Forest),
                    shape = RoundedCornerShape(10.dp),
                ) { Text("Generate PDF") }
            }
            else -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onAddProducts, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) { Text("Add products") }
                    Button(onClick = onPdf, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Forest), shape = RoundedCornerShape(10.dp)) { Text("Generate PDF") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductConfigSheet(
    product: CpqProduct,

    attributes: List<CpqAttribute>,
    bundleComponents: List<CpqBundleComponent>,
    isLoading: Boolean,
    isSaving: Boolean,
    quantity: Double,
    unitPrice: Double,

    selectedAttributes: Map<String, String>,
    componentQuantities: Map<String, Double> = emptyMap(),
    onComponentQtyChange: (String, Double) -> Unit = { _, _ -> },
    onQuantityChange: (Double) -> Unit,
    onPriceChange: (Double) -> Unit,

    onAttributeSelect: (String, String) -> Unit,
    onDismiss: () -> Unit,
    onAddToQuote: () -> Unit,
) {
    val fmt = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    fun isSelected(c: CpqBundleComponent) = c.required || componentQuantities.containsKey(c.id)
    fun qtyOf(c: CpqBundleComponent) = componentQuantities[c.id] ?: c.quantity

    val selectedComponents = remember(bundleComponents, componentQuantities) {
        bundleComponents.filter { isSelected(it) }
    }
    val componentsTotal = selectedComponents.sumOf { it.price * qtyOf(it) }
    val bundleBase = product.price
    val grandTotal = (bundleBase + componentsTotal) * quantity

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = CrmSurface) {
            Column(Modifier.fillMaxSize()) {

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().background(CrmSurface).padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column {
                        Text("Configure — ${product.name}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Charcoal)
                        Text(
                            "${product.productCode} · Base ${fmt.format(product.price)}",
                            fontSize = 12.sp, color = TextMuted,
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp).clip(CircleShape).border(1.dp, BorderGreen, CircleShape)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Forest, modifier = Modifier.size(18.dp))
                    }
                }
                Divider(color = BorderGreen)

                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Forest) }
                } else {
                    val tabs = listOf("Component", "Option", "Accessory", "Summary")
                    val grouped = remember(bundleComponents) {
                        bundleComponents.groupBy { c ->
                            when {
                                c.relationshipType.contains("Option", true) -> "Option"
                                c.relationshipType.contains("Accessory", true) -> "Accessory"
                                else -> "Component"
                            }
                        }
                    }
                    var selectedTab by remember { mutableStateOf(0) }

                    ScrollableTabRow(selectedTabIndex = selectedTab, containerColor = CrmSurface, contentColor = Forest, edgePadding = 12.dp) {
                        tabs.forEachIndexed { i, label ->
                            val count = if (label == "Summary") selectedComponents.size else (grouped[label]?.size ?: 0)
                            Tab(
                                selected = selectedTab == i,
                                onClick = { selectedTab = i },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        if (count > 0) {
                                            Spacer(Modifier.width(5.dp))
                                            Box(
                                                modifier = Modifier.size(18.dp).clip(CircleShape)
                                                    .background(if (selectedTab == i) Forest else BorderGreen),
                                                contentAlignment = Alignment.Center,
                                            ) { Text("$count", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CrmSurface) }
                                        }
                                    }
                                },
                            )
                        }
                    }
                    Divider(color = BorderGreen)

                    Column(
                        modifier = Modifier.weight(1f).fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (selectedTab < 3) {
                            // Product-level attributes (selling model options) shown once, on the Component tab only
                            if (selectedTab == 0 && attributes.isNotEmpty()) {
                                attributes.forEach { attr ->
                                    Text(attr.name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
                                    attr.options.forEach { option ->
                                        val isAttrSelected = selectedAttributes[attr.name] == option.value
                                        Row(
                                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                                .border(1.5.dp, if (isAttrSelected) Forest else BorderGreen, RoundedCornerShape(10.dp))
                                                .background(if (isAttrSelected) Mint else CrmSurface)
                                                .clickable { onAttributeSelect(attr.name, option.value) }
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Text(option.label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Charcoal)
                                            if (option.priceDelta != 0.0) Text("+${fmt.format(option.priceDelta)}", fontSize = 12.sp, color = TextMuted)
                                        }
                                    }
                                }
                                Divider(color = BorderGreen, modifier = Modifier.padding(vertical = 4.dp))
                            }

                            val rows = grouped[tabs[selectedTab]].orEmpty()
                            if (rows.isEmpty()) {
                                Text("No ${tabs[selectedTab].lowercase()}s for this bundle.", fontSize = 13.sp, color = TextMuted)
                            }
                            rows.forEach { comp -> BundleComponentRow(comp, isSelected(comp), qtyOf(comp), fmt, onComponentQtyChange) }
                        } else {
                            // Summary tab
                            Text("Selected Items", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Charcoal)
                            if (selectedComponents.isEmpty()) {
                                Text("Nothing selected yet.", fontSize = 13.sp, color = TextMuted)
                            }
                            selectedComponents.forEach { comp ->
                                val q = qtyOf(comp)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(comp.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Charcoal)
                                        Text("${comp.relationshipType} · x${q.toInt()}", fontSize = 11.sp, color = TextMuted)
                                    }
                                    Text(fmt.format(comp.price * q), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Charcoal)
                                }
                            }

                            Divider(color = BorderGreen, modifier = Modifier.padding(vertical = 6.dp))

                            SummaryRow("Bundle Base", fmt.format(bundleBase))
                            SummaryRow("Components", fmt.format(componentsTotal))
                            SummaryRow("Per-unit Total", fmt.format(bundleBase + componentsTotal))
                            Divider(color = BorderGreen, modifier = Modifier.padding(vertical = 6.dp))

                            Text("Bundle Quantity", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Row(
                                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).border(1.dp, BorderGreen, RoundedCornerShape(8.dp)),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    IconButton(onClick = { if (quantity > 1) onQuantityChange(quantity - 1) }, modifier = Modifier.size(36.dp).background(Mint)) { Text("−", fontSize = 16.sp, color = Forest, fontWeight = FontWeight.Bold) }
                                    Text("${quantity.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp))
                                    IconButton(onClick = { onQuantityChange(quantity + 1) }, modifier = Modifier.size(36.dp).background(Mint)) { Text("+", fontSize = 16.sp, color = Forest, fontWeight = FontWeight.Bold) }
                                }
                            }

                            Divider(color = BorderGreen, modifier = Modifier.padding(vertical = 6.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Charcoal)
                                Text(fmt.format(grandTotal), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Forest)
                            }
                        }
                    }

                    Divider(color = BorderGreen)
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Button(
                            onClick = onAddToQuote,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Forest),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isSaving,
                        ) {
                            if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CrmSurface, strokeWidth = 2.dp)
                            else Text("Add Bundle to Quote (${fmt.format(grandTotal)})", fontWeight = FontWeight.Bold)
                        }
                        Text("Required components cannot be removed.", fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun BundleComponentRow(
    comp: CpqBundleComponent,
    isSelected: Boolean,
    compQty: Double,
    fmt: NumberFormat,
    onComponentQtyChange: (String, Double) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .border(1.dp, if (isSelected) Forest else BorderGreen, RoundedCornerShape(10.dp))
            .background(if (isSelected) Mint else CrmSurface)
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
                Checkbox(
                    checked = isSelected,
                    enabled = !comp.required,
                    onCheckedChange = { checked -> onComponentQtyChange(comp.id, if (checked) comp.quantity else -1.0) },
                    colors = CheckboxDefaults.colors(checkedColor = Forest),
                )
                Column(Modifier.padding(top = 10.dp)) {
                    Text(comp.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Charcoal)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(BorderGreen).padding(horizontal = 6.dp, vertical = 1.dp),
                        ) { Text(comp.relationshipType.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Forest) }
                        if (comp.required) {
                            Spacer(Modifier.width(6.dp))
                            Text("Required", fontSize = 10.sp, color = Forest, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Text(comp.productCode, fontSize = 10.sp, color = TextMuted, modifier = Modifier.padding(top = 2.dp))
                    if (comp.description.isNotBlank()) {
                        Text(comp.description, fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }
            Text(fmt.format(comp.price), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Charcoal)
        }

        if (isSelected) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).border(1.dp, BorderGreen, RoundedCornerShape(8.dp)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { if (compQty > comp.minQty) onComponentQtyChange(comp.id, compQty - 1) },
                        modifier = Modifier.size(30.dp).background(Mint),
                    ) { Text("−", fontSize = 14.sp, color = Forest, fontWeight = FontWeight.Bold) }
                    Text("${compQty.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp))
                    IconButton(
                        onClick = { if (compQty < comp.maxQty) onComponentQtyChange(comp.id, compQty + 1) },
                        modifier = Modifier.size(30.dp).background(Mint),
                    ) { Text("+", fontSize = 14.sp, color = Forest, fontWeight = FontWeight.Bold) }
                }
                Text("Total: ${fmt.format(comp.price * compQty)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Forest)
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = TextMuted)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Charcoal)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditLineSheet(
    line: CpqQuoteLine,
    qty: Double,
    price: Double,
    isSaving: Boolean,
    onQtyChange: (Double) -> Unit,
    onPriceChange: (Double) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = CrmSurface, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Edit: ${line.productName}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Charcoal)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Quantity", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, modifier = Modifier.padding(bottom = 4.dp))
                    OutlinedTextField(
                        value = qty.toInt().toString(),
                        onValueChange = { it.toDoubleOrNull()?.let { v -> onQtyChange(v) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Unit Price", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, modifier = Modifier.padding(bottom = 4.dp))
                    OutlinedTextField(
                        value = price.toString(),
                        onValueChange = { it.toDoubleOrNull()?.let { v -> onPriceChange(v) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                }
            }
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Forest),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSaving,
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CrmSurface, strokeWidth = 2.dp)
                else Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditQuoteHeaderSheet(
    name: String,
    startDate: String,
    expirationDate: String,
    term: String,
    endDate: String,
    description: String,
    isSaving: Boolean,
    onNameChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onExpirationDateChange: (String) -> Unit,
    onTermChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = CrmSurface, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Edit Quote", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Charcoal)

            Column {
                Text("Quote Name", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, modifier = Modifier.padding(bottom = 4.dp))
                OutlinedTextField(value = name, onValueChange = onNameChange, singleLine = true, modifier = Modifier.fillMaxWidth())
            }

            DatePickerField(
                label = "Start Date",
                value = startDate,
                onValueChange = onStartDateChange,
                outputFormat = "dd-MM-yyyy",
            )

            DatePickerField(
                label = "Expiration Date",
                value = expirationDate,
                onValueChange = onExpirationDateChange,
                outputFormat = "dd-MM-yyyy",
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Term (months)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, modifier = Modifier.padding(bottom = 4.dp))
                    OutlinedTextField(
                        value = term, onValueChange = onTermChange, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("End Date (auto)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, modifier = Modifier.padding(bottom = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(Mint).border(1.dp, BorderGreen, RoundedCornerShape(10.dp)).padding(14.dp),
                    ) {
                        Text(endDate.ifBlank { "—" }, fontSize = 13.sp, color = Charcoal)
                    }
                }
            }

            Column {
                Text("Description", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, modifier = Modifier.padding(bottom = 4.dp))
                OutlinedTextField(value = description, onValueChange = onDescriptionChange, modifier = Modifier.fillMaxWidth().height(90.dp))
            }

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Forest),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSaving,
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CrmSurface, strokeWidth = 2.dp)
                else Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}
@Composable
private fun ApprovalStepRow(step: CpqApprovalStep) {
    val (icBg, icFg) = when (step.status.lowercase()) {
        "approved" -> Color(0xFFDCF5E7) to Color(0xFF0E6B3E)
        "pending", "waiting" -> Color(0xFFFBF3DC) to Color(0xFF8A6410)
        else -> Color(0xFFEEF1EF) to TextMuted
    }
    Row(modifier = Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(icBg), contentAlignment = Alignment.Center) {
            Text(step.approverName.take(1), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = icFg)
        }
        Column {
            Text(step.approverName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Charcoal)
            Text(step.status, fontSize = 11.sp, color = TextMuted)
            if (step.comments.isNotBlank()) Text(step.comments, fontSize = 11.sp, color = TextMuted)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavouritesSheet(
    favourites: List<CpqFavourite>,
    isLoading: Boolean,
    isLoadingLines: Boolean,
    onDismiss: () -> Unit,
    onLoad: (favouriteId: String) -> Unit,
    onDelete: (favouriteId: String) -> Unit,
) {
    val fmt = NumberFormat.getCurrencyInstance(Locale.US)
    var deleteConfirmId by remember { mutableStateOf<String?>(null) }

    if (deleteConfirmId != null) {
        AlertDialog(
            onDismissRequest = { deleteConfirmId = null },
            title = { Text("Delete Favourite", fontWeight = FontWeight.Bold) },
            text = { Text("Delete this favourite template? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = { onDelete(deleteConfirmId!!); deleteConfirmId = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0392B)),
                    shape = RoundedCornerShape(10.dp),
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmId = null }) { Text("Cancel", color = Forest) }
            },
            containerColor = CrmSurface,
            shape = RoundedCornerShape(16.dp),
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CrmSurface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 32.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Saved Favourites", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Charcoal, modifier = Modifier.weight(1f))
                Text("${favourites.size} templates", fontSize = 12.sp, color = TextMuted)
            }

            when {
                isLoading -> Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Forest)
                }
                isLoadingLines -> Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Forest)
                        Spacer(Modifier.height(10.dp))
                        Text("Loading to quote...", fontSize = 13.sp, color = TextMuted)
                    }
                }
                favourites.isEmpty() -> Box(
                    Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.StarBorder, contentDescription = null, tint = MutedGreen, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No saved favourites yet", fontSize = 14.sp, color = TextMuted)
                    }
                }
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(favourites, key = { it.id }) { fav ->
                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CrmSurface)
                                .border(1.dp, BorderGreen, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                        ) {
                            // Name + date row
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(CardGreen),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(fav.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Charcoal)
                                    if (!fav.description.isNullOrBlank()) {
                                        Text(fav.description, fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(top = 1.dp))
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(color = BorderGreen, thickness = 0.5.dp)
                            Spacer(Modifier.height(8.dp))

                            // Stats row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                FavStatChip(Icons.Default.Inventory2, "${fav.lineCount} products")
                                FavStatChip(Icons.Default.CurrencyRupee, fmt.format(fav.totalPrice))
                                FavStatChip(Icons.Default.CalendarMonth, fav.createdDate.take(10))
                            }

                            Spacer(Modifier.height(10.dp))

                            // Action buttons
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onLoad(fav.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Forest),
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Load to Staging", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { deleteConfirmId = fav.id },
                                    modifier = Modifier.weight(0.6f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC0392B)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC0392B)),
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Delete", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun FavStatChip(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, tint = MutedGreen, modifier = Modifier.size(12.dp))
        Text(label, fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)
    }
}