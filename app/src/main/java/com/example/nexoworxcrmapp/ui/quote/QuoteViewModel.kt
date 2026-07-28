package com.example.nexoworxcrmapp.ui.quote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexoworxcrmapp.data.quote.*
import com.example.nexoworxcrmapp.network.ApiResult
import com.example.nexoworxcrmapp.network.NetworkModule
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuoteUiState(
    // Quote workspace
    val isLoading: Boolean = false,
    val quote: CpqQuote? = null,
    val lines: List<CpqQuoteLine> = emptyList(),
    val errorMessage: String? = null,
    val pdfBytes: ByteArray? = null,
    val isDownloadingPdf: Boolean = false,
    val isEditingHeader: Boolean = false,
    val editQuoteName: String = "",
    val editStartDate: String = "",
    val editExpirationDate: String = "",
    val editTerm: String = "",
    val editEndDate: String = "",
    val editDescription: String = "",
    val isSavingHeader: Boolean = false,

    // Catalog
    val catalogs: List<CpqCatalog> = emptyList(),
    val categories: List<CpqCategory> = emptyList(),
    val products: List<CpqProduct> = emptyList(),
    val selectedCatalogId: String = "",
    val selectedCategoryId: String = "",
    val isCatalogLoading: Boolean = false,

    // Product config sheet
    val configuringProduct: CpqProduct? = null,
    val attributes: List<CpqAttribute> = emptyList(),
    val bundleComponents: List<CpqBundleComponent> = emptyList(),
    val selectedSellingModelId: String = "",
    val selectedAttributes: Map<String, String> = emptyMap(),
    val configQuantity: Double = 1.0,
    val configUnitPrice: Double = 0.0,
    val isConfigLoading: Boolean = false,
    val selectedComponents: List<CpqBundleComponent> = emptyList(),
    val componentQuantities: Map<String, Double> = emptyMap(),

    // Edit line sheet
    val editingLine: CpqQuoteLine? = null,
    val editQty: Double = 1.0,
    val editPrice: Double = 0.0,

    // Saving / actions
    val isSaving: Boolean = false,
    val saveError: String? = null,

    // Approval
    val approvalStatus: CpqApprovalStatus? = null,
    val isSubmittingApproval: Boolean = false,
    val approvalComments: String = "",

    // PDF
    val pdfTemplates: List<CpqPdfTemplate> = emptyList(),
    val pdfDownloadUrl: String? = null,
    val isGeneratingPdf: Boolean = false,
// Favourites
    val showSaveFavouriteDialog: Boolean = false,
    val favouriteLabel: String = "",
    val favouriteDescription: String = "",
    val isSavingFavourite: Boolean = false,
    val showFavouritesSheet: Boolean = false,
    val favourites: List<CpqFavourite> = emptyList(),
    val isFavouritesLoading: Boolean = false,
    val isLoadingFavouriteLines: Boolean = false,
    // Navigation screens
    val screen: QuoteScreen = QuoteScreen.Workspace,
)

enum class QuoteScreen { Workspace, Catalog, Approval, Pdf }

class QuoteViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    val quoteId: String = checkNotNull(savedStateHandle["quoteId"])
    private val repo = NetworkModule.cpqRepository

    private val _uiState = MutableStateFlow(QuoteUiState())
    val uiState = _uiState.asStateFlow()

    init { loadQuote() }

    // ── Quote workspace ───────────────────────────────────────────────────────

    fun loadQuote() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            // Load quote + lines in parallel
            val quoteResult = repo.getQuote(quoteId)
            val linesResult = repo.getLines(quoteId)
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    quote = (quoteResult as? ApiResult.Success)?.data ?: state.quote,
                    lines = (linesResult as? ApiResult.Success)?.data ?: state.lines,
                    errorMessage = (quoteResult as? ApiResult.Error)?.message,
                )
            }
        }
    }

    // ── Catalog ───────────────────────────────────────────────────────────────

    fun openCatalog() {
        _uiState.update { it.copy(screen = QuoteScreen.Catalog) }
        if (_uiState.value.catalogs.isEmpty()) loadCatalogs()
    }

    private fun loadCatalogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCatalogLoading = true) }
            when (val r = repo.getCatalogs()) {
                is ApiResult.Success -> {
                    val catalogId = r.data.firstOrNull()?.id.orEmpty()
                    _uiState.update { it.copy(catalogs = r.data, selectedCatalogId = catalogId) }
                    if (catalogId.isNotBlank()) loadCategories(catalogId)
                }
                is ApiResult.Error -> _uiState.update { it.copy(isCatalogLoading = false, saveError = r.message) }
            }
        }
    }
    private fun List<CpqProduct>.standaloneOrBundleOnly(): List<CpqProduct> =
        filter { it.type.equals("standalone", ignoreCase = true) || it.type.equals("bundle", ignoreCase = true) }
    fun selectCategory(categoryId: String) {
        _uiState.update { it.copy(selectedCategoryId = categoryId, isCatalogLoading = true, products = emptyList()) }
        viewModelScope.launch {
            val catalogId = _uiState.value.selectedCatalogId
            if (categoryId.isBlank()) {
                // Backend requires a categoryId per call — "All" fans out across every
                // known category and merges the results client-side instead.
                val allCategoryIds = _uiState.value.categories.map { it.id }
                if (allCategoryIds.isEmpty()) {
                    _uiState.update { it.copy(isCatalogLoading = false, products = emptyList()) }
                    return@launch
                }
                val merged = mutableListOf<CpqProduct>()
                var firstError: String? = null
                for (catId in allCategoryIds) {
                    when (val r = repo.getProducts(catalogId, catId)) {
                        is ApiResult.Success -> merged.addAll(r.data)
                        is ApiResult.Error -> if (firstError == null) firstError = r.message
                    }
                }
                val deduped = merged.distinctBy { it.id }.standaloneOrBundleOnly()
                _uiState.update {
                    it.copy(
                        products = deduped,
                        isCatalogLoading = false,
                        saveError = if (deduped.isEmpty()) firstError else null,
                    )
                }
            } else {
                when (val r = repo.getProducts(catalogId, categoryId)) {
                    is ApiResult.Success -> _uiState.update { it.copy(products = r.data.standaloneOrBundleOnly(), isCatalogLoading = false) }
                    is ApiResult.Error -> _uiState.update { it.copy(isCatalogLoading = false, saveError = r.message) }
                }
            }
        }
    }

    private fun loadCategories(catalogId: String) {
        viewModelScope.launch {
            when (val r = repo.getCategories(catalogId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(categories = r.data, selectedCategoryId = "") }
                    selectCategory("") // triggers the merged "All" fetch above
                }
                is ApiResult.Error -> _uiState.update { it.copy(isCatalogLoading = false, saveError = r.message) }
            }
        }
    }

    // ── Product config ────────────────────────────────────────────────────────

    fun configureProduct(product: CpqProduct) {
        _uiState.update { it.copy(
            configuringProduct = product,
            configQuantity = 1.0,
            configUnitPrice = product.price,
            selectedAttributes = emptyMap(),
            isConfigLoading = true,
        )}
        viewModelScope.launch {
            val attrs = repo.getAttributes(product.id)
            val bundles = if (product.type.equals("bundle", ignoreCase = true)) repo.getBundleComponents(product.id) else ApiResult.Success(emptyList())
            val fetchedBundles = (bundles as? ApiResult.Success)?.data ?: emptyList()
            val autoSelected = fetchedBundles
                .filter { it.required }
                .associate { it.id to it.quantity }
            _uiState.update { state ->
                state.copy(
                    attributes = (attrs as? ApiResult.Success)?.data ?: emptyList(),
                    bundleComponents = fetchedBundles,
                    componentQuantities = autoSelected,
                    isConfigLoading = false,
                )
            }
        }
    }

    fun setConfigQuantity(qty: Double) = _uiState.update { it.copy(configQuantity = qty) }
    fun setConfigPrice(price: Double) = _uiState.update { it.copy(configUnitPrice = price) }
    fun selectAttribute(name: String, value: String) = _uiState.update {
        it.copy(selectedAttributes = it.selectedAttributes + (name to value))
    }
    fun setComponentQuantity(componentId: String, qty: Double) = _uiState.update {
        it.copy(componentQuantities = if (qty < 0) it.componentQuantities - componentId
        else it.componentQuantities + (componentId to qty))
    }
    fun dismissConfig() = _uiState.update { it.copy(configuringProduct = null) }

    fun addProductToQuote() {
        val state = _uiState.value
        val product = state.configuringProduct ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }


            if (state.configUnitPrice <= 0.0) {
                _uiState.update { it.copy(isSaving = false, saveError = "Unit price is missing for this product") }
                return@launch
            }

            val components = state.bundleComponents
                .filter { comp ->
                    comp.required || state.componentQuantities.containsKey(comp.id)
                }
                .map { comp ->
                    CpqBundleComponentLine(
                        bundleComponentId = comp.id,
                        componentProductId = comp.productId,
                        componentPricebookEntryId = comp.pricebookEntryId,
                        quantity = state.componentQuantities[comp.id] ?: comp.quantity,
                        unitPrice = comp.price,
                        isIncludedInPrice = false,
                    )
                }
            val line = SaveLineItem(
                productId = product.id,
                quantity = state.configQuantity,
                unitPrice = state.configUnitPrice,
                sellingModelId = "",
                attributes = state.selectedAttributes,
                components = components,
            )
            when (val r = repo.saveLines(quoteId, listOf(line))) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, configuringProduct = null) }
                    loadQuote()
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSaving = false, saveError = r.message) }
            }
        }
    }
    fun quickAddStandaloneLine(product: CpqProduct, quantity: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            if (quantity <= 0) {
                _uiState.update { it.copy(isSaving = false) }
                return@launch
            }
            val line = SaveLineItem(
                productId = product.id,
                quantity = quantity.toDouble(),
                unitPrice = product.price,
                sellingModelId = "",
                attributes = emptyMap(),
                components = emptyList(),
            )
            when (val r = repo.saveLines(quoteId, listOf(line))) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    loadQuote()
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSaving = false, saveError = r.message) }
            }
        }
    }
    // ── Edit line ─────────────────────────────────────────────────────────────

    fun openEditLine(line: CpqQuoteLine) {
        _uiState.update { it.copy(editingLine = line, editQty = line.quantity, editPrice = line.unitPrice) }    }

    fun setEditQty(qty: Double) = _uiState.update { it.copy(editQty = qty) }
    fun setEditPrice(price: Double) = _uiState.update { it.copy(editPrice = price) }
    fun dismissEditLine() = _uiState.update { it.copy(editingLine = null) }

    fun saveEditLine() {
        val state = _uiState.value
        val line = state.editingLine ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            when (val r = repo.editLines(listOf(EditLineChange(line.id, state.editQty, state.editPrice)))) {
                is ApiResult.Success -> { _uiState.update { it.copy(isSaving = false, editingLine = null) }; loadQuote() }
                is ApiResult.Error -> _uiState.update { it.copy(isSaving = false, saveError = r.message) }
            }
        }
    }

    fun deleteLine(lineId: String) {
        viewModelScope.launch {
            when (repo.deleteLines(listOf(lineId))) {
                is ApiResult.Success -> loadQuote()
                is ApiResult.Error -> {}
            }
        }
    }

    // ── Approval ──────────────────────────────────────────────────────────────

    fun openApproval() {
        _uiState.update { it.copy(screen = QuoteScreen.Approval) }
        viewModelScope.launch {
            when (val r = repo.getApprovalStatus(quoteId)) {
                is ApiResult.Success -> _uiState.update { it.copy(approvalStatus = r.data) }
                is ApiResult.Error -> {}
            }
        }
    }

    fun setApprovalComments(c: String) = _uiState.update { it.copy(approvalComments = c) }

    fun submitApproval() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingApproval = true, saveError = null) }
            when (val r = repo.submitApproval(quoteId, _uiState.value.approvalComments)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmittingApproval = false) }
                    // Poll approval status
                    delay(1500)
                    openApproval()
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSubmittingApproval = false, saveError = r.message) }
            }
        }
    }

    // ── PDF ───────────────────────────────────────────────────────────────────

    fun openPdf() {
        _uiState.update { it.copy(screen = QuoteScreen.Pdf) }
        if (_uiState.value.pdfTemplates.isEmpty()) {
            viewModelScope.launch {
                when (val r = repo.getPdfTemplates()) {
                    is ApiResult.Success -> _uiState.update { it.copy(pdfTemplates = r.data) }
                    is ApiResult.Error -> {}
                }
            }
        }
    }

    fun generatePdf(templateId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingPdf = true, saveError = null) }
            when (val r = repo.savePdf(templateId, quoteId)) {
                is ApiResult.Success -> _uiState.update { it.copy(isGeneratingPdf = false, pdfDownloadUrl = r.data) }
                is ApiResult.Error -> _uiState.update { it.copy(isGeneratingPdf = false, saveError = r.message) }
            }
        }
    }

//    fun downloadAndOpenPdf(context: android.content.Context, onReady: (android.net.Uri) -> Unit) {
//        val relativeUrl = _uiState.value.pdfDownloadUrl ?: return
//        viewModelScope.launch {
//            when (val r = repo.downloadPdfBytes(relativeUrl)) {
//                is ApiResult.Success -> {
//                    val file = java.io.File(context.cacheDir, "quote_${quoteId}.pdf")
//                    file.writeBytes(r.data)
//                    val uri = androidx.core.content.FileProvider.getUriForFile(
//                        context, "${context.packageName}.fileprovider", file
//                    )
//                    onReady(uri)
//                }
//                is ApiResult.Error -> _uiState.update { it.copy(saveError = r.message) }
//            }
//        }
//    }

    // ── Navigation ────────────────────────────────────────────────────────────

    // ── Favourites ────────────────────────────────────────────────────────────────

    fun openSaveFavouriteDialog() = _uiState.update {
        it.copy(showSaveFavouriteDialog = true, favouriteLabel = "", favouriteDescription = "")
    }

    fun dismissSaveFavouriteDialog() = _uiState.update {
        it.copy(showSaveFavouriteDialog = false, favouriteLabel = "", favouriteDescription = "")
    }

    fun setFavouriteLabel(v: String) = _uiState.update { it.copy(favouriteLabel = v) }
    fun setFavouriteDescription(v: String) = _uiState.update { it.copy(favouriteDescription = v) }

    fun saveFavourite() {
        val state = _uiState.value
        if (state.favouriteLabel.isBlank()) return
        if (state.lines.isEmpty()) {
            _uiState.update { it.copy(saveError = "No products to save. Add products first.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingFavourite = true, saveError = null) }
            when (val r = repo.saveFavourite(state.favouriteLabel, state.favouriteDescription, state.lines)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isSavingFavourite = false, showSaveFavouriteDialog = false,
                        favouriteLabel = "", favouriteDescription = "")
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSavingFavourite = false, saveError = r.message)
                }
            }
        }
    }

    fun openFavouritesSheet() {
        _uiState.update { it.copy(showFavouritesSheet = true, isFavouritesLoading = true) }
        viewModelScope.launch {
            when (val r = repo.getFavourites()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(favourites = r.data, isFavouritesLoading = false)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isFavouritesLoading = false, saveError = r.message)
                }
            }
        }
    }

    fun closeFavouritesSheet() = _uiState.update { it.copy(showFavouritesSheet = false) }

    fun loadFavouriteToQuote(favouriteId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingFavouriteLines = true, saveError = null) }
            when (val r = repo.getFavouriteLines(favouriteId)) {
                is ApiResult.Success -> {
                    val lines = r.data.map { line ->
                        SaveLineItem(
                            productId = line.productId,
                            quantity = line.quantity,
                            unitPrice = line.unitPrice,
                        )
                    }
                    when (val saveResult = repo.saveLines(quoteId, lines)) {
                        is ApiResult.Success -> {
                            _uiState.update { it.copy(isLoadingFavouriteLines = false, showFavouritesSheet = false) }
                            loadQuote()
                        }
                        is ApiResult.Error -> _uiState.update {
                            it.copy(isLoadingFavouriteLines = false, saveError = saveResult.message)
                        }
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoadingFavouriteLines = false, saveError = r.message)
                }
            }
        }
    }

    fun deleteFavourite(favouriteId: String) {
        viewModelScope.launch {
            when (val r = repo.deleteFavourite(favouriteId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(
                        favourites = _uiState.value.favourites.filter { it.id != favouriteId }
                    )}
                }
                is ApiResult.Error -> _uiState.update { it.copy(saveError = r.message) }
            }
        }
    }

    fun goToWorkspace() = _uiState.update { it.copy(screen = QuoteScreen.Workspace, configuringProduct = null, editingLine = null) }
    fun clearSaveError() = _uiState.update { it.copy(saveError = null) }


    private fun getTodayDate(): String {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            java.time.LocalDate.now().toString()
        } else {
            val cal = java.util.Calendar.getInstance()
            val year = cal.get(java.util.Calendar.YEAR)
            val month = (cal.get(java.util.Calendar.MONTH) + 1).toString().padStart(2, '0')
            val day = cal.get(java.util.Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
            "$year-$month-$day"
        }
    }

    fun downloadPdfForViewing() {
        val relativeUrl = _uiState.value.pdfDownloadUrl ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDownloadingPdf = true, saveError = null) }
            when (val r = repo.downloadPdfBytes(relativeUrl)) {
                is ApiResult.Success -> _uiState.update { it.copy(isDownloadingPdf = false, pdfBytes = r.data) }
                is ApiResult.Error -> _uiState.update { it.copy(isDownloadingPdf = false, saveError = r.message) }
            }
        }
    }

    fun closePdfViewer() = _uiState.update { it.copy(pdfBytes = null) }

    fun openEditQuoteHeader() {
        val q = _uiState.value.quote ?: return
        _uiState.update {
            it.copy(
                isEditingHeader = true,
                editQuoteName = q.name,
                editStartDate = q.startDate,
                editExpirationDate = q.expirationDate,
                editTerm = q.term?.toString() ?: "",
                editEndDate = q.endDate,
                editDescription = q.description,
            )
        }
    }

    fun dismissEditQuoteHeader() = _uiState.update { it.copy(isEditingHeader = false) }

    fun setEditQuoteName(v: String) = _uiState.update { it.copy(editQuoteName = v) }
    fun setEditStartDate(v: String) = _uiState.update {
        it.copy(editStartDate = v, editEndDate = computeEndDateFromTerm(v, it.editTerm))
    }
    fun setEditExpirationDate(v: String) = _uiState.update { it.copy(editExpirationDate = v) }
    fun setEditTerm(v: String) {
        val digits = v.filter { it.isDigit() }
        _uiState.update { it.copy(editTerm = digits, editEndDate = computeEndDateFromTerm(it.editStartDate, digits)) }
    }
    fun setEditDescription(v: String) = _uiState.update { it.copy(editDescription = v) }

    fun saveQuoteHeader() {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingHeader = true, saveError = null) }
            when (val r = repo.updateQuoteHeader(
                quoteId = quoteId,
                name = s.editQuoteName,
                startDate = s.editStartDate,
                expirationDate = s.editExpirationDate,
                endDate = s.editEndDate,
                term = s.editTerm,
                description = s.editDescription,
            )) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSavingHeader = false, isEditingHeader = false) }
                    loadQuote()
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSavingHeader = false, saveError = r.message) }
            }
        }
    }

    private fun computeEndDateFromTerm(startDate: String, term: String): String {
        val months = term.toIntOrNull() ?: return ""
        if (startDate.isBlank() || months <= 0) return ""
        return try {
            val normalized = normalizeToIso(startDate) ?: return ""
            val parts = normalized.split("-")
            val cal = java.util.Calendar.getInstance()
            cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            cal.add(java.util.Calendar.MONTH, months)
            "${cal.get(java.util.Calendar.YEAR)}-" +
                    "${(cal.get(java.util.Calendar.MONTH)+1).toString().padStart(2,'0')}-" +
                    "${cal.get(java.util.Calendar.DAY_OF_MONTH).toString().padStart(2,'0')}"
        } catch (e: Exception) { "" }
    }

    private fun normalizeToIso(date: String): String? {
        val parts = date.split("-")
        if (parts.size != 3) return null
        return when {
            parts[0].length == 4 -> date
            parts[2].length == 4 -> "${parts[2]}-${parts[1].padStart(2,'0')}-${parts[0].padStart(2,'0')}"
            else -> null
        }
    }
    fun downloadPdfToDevice(context: android.content.Context) {
        val relativeUrl = _uiState.value.pdfDownloadUrl ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDownloadingPdf = true, saveError = null) }
            when (val r = repo.downloadPdfBytes(relativeUrl)) {
                is ApiResult.Success -> {
                    try {
                        val fileName = "Quote_${quoteId}_${System.currentTimeMillis()}.pdf"
                        val resolver = context.contentResolver

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                            val values = android.content.ContentValues().apply {
                                put(android.provider.MediaStore.Downloads.DISPLAY_NAME, fileName)
                                put(android.provider.MediaStore.Downloads.MIME_TYPE, "application/pdf")
                                put(android.provider.MediaStore.Downloads.IS_PENDING, 1)
                            }
                            val uri = resolver.insert(
                                android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
                            ) ?: throw Exception("Could not create file in Downloads")

                            resolver.openOutputStream(uri)?.use { it.write(r.data) }

                            values.clear()
                            values.put(android.provider.MediaStore.Downloads.IS_PENDING, 0)
                            resolver.update(uri, values, null, null)
                        } else {
                            // Android 9 and below — needs WRITE_EXTERNAL_STORAGE permission
                            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                                android.os.Environment.DIRECTORY_DOWNLOADS
                            )
                            val file = java.io.File(downloadsDir, fileName)
                            file.writeBytes(r.data)
                        }

                        _uiState.update { it.copy(isDownloadingPdf = false) }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(isDownloadingPdf = false, saveError = "Download failed: ${e.message}") }
                    }
                }
                is ApiResult.Error -> _uiState.update { it.copy(isDownloadingPdf = false, saveError = r.message) }
            }
        }
    }
}