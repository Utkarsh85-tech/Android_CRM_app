package com.example.nexoworxcrmapp.ui.opportunity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexoworxcrmapp.data.OpportunityLineItem
import com.example.nexoworxcrmapp.data.PricebookEntry
import com.example.nexoworxcrmapp.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpportunityProductsScreen(
    onBack: () -> Unit,
    viewModel: OpportunityProductsViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val fmt = NumberFormat.getCurrencyInstance(Locale.US)

    // Save error dialog
    if (state.saveError != null) {
        AlertDialog(
            onDismissRequest = viewModel::clearSaveError,
            title = { Text("Error", fontWeight = FontWeight.Bold) },
            text = { Text(state.saveError.orEmpty()) },
            confirmButton = { TextButton(onClick = viewModel::clearSaveError) { Text("OK", color = Forest) } },
            containerColor = CrmSurface,
            shape = RoundedCornerShape(16.dp),
        )
    }

    // Add / Edit bottom sheet
    if (state.showAddSheet) {
        ProductBottomSheet(
            editingItem = state.editingItem,
            pricebookEntries = state.pricebookEntries,
            isSaving = state.isSaving,
            onDismiss = viewModel::closeSheet,
            onSave = { pbEntryId, qty, price ->
                val editing = state.editingItem
                if (editing != null) viewModel.updateProduct(editing.id, qty, price)
                else viewModel.addProduct(pbEntryId, qty, price)
            },
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(CrmBg),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Forest)
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CrmSurface)
            }
            Text(
                text = "Products",
                modifier = Modifier.weight(1f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = CrmSurface,
            )
            IconButton(onClick = viewModel::openAddSheet) {
                Icon(Icons.Default.Add, contentDescription = "Add Product", tint = CrmSurface)
            }
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Forest)
            }
            state.errorMessage != null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.errorMessage.orEmpty(), color = Color(0xFFC0392B))
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = viewModel::load, colors = ButtonDefaults.buttonColors(containerColor = Forest)) {
                        Text("Retry")
                    }
                }
            }
            state.lineItems.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No products added yet", color = TextMuted, fontSize = 15.sp)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = viewModel::openAddSheet, colors = ButtonDefaults.buttonColors(containerColor = Forest)) {
                        Text("+ Add Product")
                    }
                }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.lineItems, key = { it.id }) { item ->
                    ProductCard(
                        item = item,
                        fmt = fmt,
                        onEdit = { viewModel.openEditSheet(item) },
                        onDelete = { viewModel.deleteProduct(item.id) },
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun ProductCard(
    item: OpportunityLineItem,
    fmt: NumberFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Remove Product", fontWeight = FontWeight.Bold) },
            text = { Text("Remove \"${item.productName}\" from this opportunity?") },
            confirmButton = {
                Button(
                    onClick = { showDeleteDialog = false; onDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0392B)),
                    shape = RoundedCornerShape(10.dp),
                ) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel", color = Forest) }
            },
            containerColor = CrmSurface,
            shape = RoundedCornerShape(16.dp),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CrmSurface)
            .border(1.dp, BorderGreen, RoundedCornerShape(14.dp))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = item.productName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Charcoal,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Forest, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFC0392B), modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ProductStat("Qty", item.quantity.toInt().toString())
            ProductStat("Unit Price", fmt.format(item.unitPrice))
            ProductStat("Total", fmt.format(item.totalPrice))
        }
    }
}

@Composable
private fun ProductStat(label: String, value: String) {
    Column {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, letterSpacing = 0.5.sp)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Charcoal, modifier = Modifier.padding(top = 2.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductBottomSheet(
    editingItem: OpportunityLineItem?,
    pricebookEntries: List<PricebookEntry>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (pricebookEntryId: String, quantity: Double, unitPrice: Double) -> Unit,
) {
    val isEdit = editingItem != null
    var selectedEntry by remember { mutableStateOf<PricebookEntry?>(null) }
    var quantity by remember { mutableStateOf(editingItem?.quantity?.toInt()?.toString() ?: "") }
    var unitPrice by remember { mutableStateOf(editingItem?.unitPrice?.toString() ?: "") }
    var search by remember { mutableStateOf("") }
    var showPicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CrmSurface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = if (isEdit) "Edit Product" else "Add Product",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Charcoal,
            )

            // Product picker — only for Add
            if (!isEdit) {
                OutlinedTextField(
                    value = selectedEntry?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Product *") },
                    placeholder = { Text("Tap to select a product") },
                    modifier = Modifier.fillMaxWidth().clickable { showPicker = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Charcoal,
                        disabledBorderColor = BorderGreen,
                        disabledLabelColor = TextMuted,
                    ),
                )

                if (showPicker) {
                    val filtered = pricebookEntries.filter {
                        it.name.contains(search, ignoreCase = true)
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, BorderGreen, RoundedCornerShape(12.dp))
                            .background(CrmSurface),
                    ) {
                        OutlinedTextField(
                            value = search,
                            onValueChange = { search = it },
                            placeholder = { Text("Search products...") },
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            singleLine = true,
                        )
                        filtered.take(6).forEach { entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedEntry = entry
                                        unitPrice = entry.unitPrice.toString()
                                        showPicker = false
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(entry.name, fontSize = 14.sp, color = Charcoal, modifier = Modifier.weight(1f))
                                Text(
                                    NumberFormat.getCurrencyInstance(Locale.US).format(entry.unitPrice),
                                    fontSize = 13.sp,
                                    color = TextMuted,
                                )
                            }
                            HorizontalDivider(color = BorderGreen)
                        }
                    }
                }
            } else {
                // Show product name as read-only on edit
                Text(
                    text = editingItem?.productName.orEmpty(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Forest,
                )
            }

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Quantity *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )

            OutlinedTextField(
                value = unitPrice,
                onValueChange = { unitPrice = it },
                label = { Text("Sales Price *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
            )

            val canSave = quantity.isNotBlank() && unitPrice.isNotBlank() &&
                    (isEdit || selectedEntry != null)

            Button(
                onClick = {
                    val qty = quantity.toDoubleOrNull() ?: return@Button
                    val price = unitPrice.toDoubleOrNull() ?: return@Button
                    val pbEntryId = selectedEntry?.id ?: editingItem?.pricebookEntryId ?: return@Button
                    onSave(pbEntryId, qty, price)
                },
                enabled = canSave && !isSaving,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Forest),
                shape = RoundedCornerShape(12.dp),
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CrmSurface, strokeWidth = 2.dp)
                else Text(if (isEdit) "Save Changes" else "Add Product", fontWeight = FontWeight.Bold)
            }
        }
    }
}