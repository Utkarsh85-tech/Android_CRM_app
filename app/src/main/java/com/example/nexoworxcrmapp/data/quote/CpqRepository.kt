package com.example.nexoworxcrmapp.data.quote

import com.example.nexoworxcrmapp.network.ApiResult
import com.example.nexoworxcrmapp.network.NetworkModule
import com.example.nexoworxcrmapp.network.safeApiCall
import com.example.nexoworxcrmapp.network.safeApiCallEmpty

class CpqRepository(private val api: CpqApiService) {

    suspend fun getAccounts(): ApiResult<List<CpqAccount>> =
        safeApiCall { api.getAccounts() }.mapList()

    suspend fun getOpportunities(accountId: String): ApiResult<List<CpqOpportunity>> =
        safeApiCall { api.getOpportunities(accountId) }.mapList()

    suspend fun getQuotes(accountId: String): ApiResult<List<CpqQuote>> =
        safeApiCall { api.getQuotes(accountId) }.mapList()



    suspend fun createQuote(
        accountId: String,
        opportunityId: String,
        name: String,
        startDate: String = "",
        expirationDate: String = "",
        endDate: String = "",
        term: Int? = null,
        description: String = "",
    ): ApiResult<String> {
        val today = if (startDate.isNotBlank()) convertToIso(startDate) else getCurrentDate()
        val expiry = if (expirationDate.isNotBlank()) convertToIso(expirationDate) else getExpiryDate()
        val end = if (endDate.isNotBlank()) convertToIso(endDate) else null
        val body = StandardCreateQuoteBody(
            name = name,
            opportunityId = opportunityId,
            status = "Draft",
            expirationDate = expiry.ifBlank { null },
            description = description.ifBlank { null },
            startDate = today.ifBlank { null },
            endDate = end,
            term = term,
        )
        return when (val r = safeApiCall { api.createQuote(body) }) {
            is ApiResult.Success -> {
                val quoteId = r.data.id.orEmpty()
                if (r.data.success && quoteId.isNotBlank()) ApiResult.Success(quoteId)
                else ApiResult.Error(r.data.errors?.firstOrNull()?.message ?: "Create quote failed — no ID returned")
            }
            is ApiResult.Error -> r
        }
    }

    suspend fun createQuoteFull(
        accountId: String,
        opportunityId: String,
        name: String,
        startDate: String,
        expirationDate: String,
        endDate: String,
        description: String,
    ): ApiResult<String> {
        val body = StandardCreateQuoteBody(
            name = name,
            opportunityId = opportunityId,
            status = "Draft",
            expirationDate = expirationDate.ifBlank { null },
            description = description.ifBlank { null },
            startDate = startDate.ifBlank { null },
        )
        return when (val r = safeApiCall { api.createQuote(body) }) {
            is ApiResult.Success -> {
                val quoteId = r.data.id.orEmpty()
                if (r.data.success && quoteId.isNotBlank()) ApiResult.Success(quoteId)
                else ApiResult.Error(r.data.errors?.firstOrNull()?.message ?: "Create quote failed — no ID returned")
            }
            is ApiResult.Error -> r
        }
    }
    // ADD this method anywhere inside CpqRepository (e.g. right after createQuoteFull):
    suspend fun updateQuoteHeader(
        quoteId: String,
        name: String,
        startDate: String,
        expirationDate: String,
        endDate: String,
        term: String,
        description: String,
    ): ApiResult<Unit> {
        val fields = mutableMapOf<String, String>()
        if (name.isNotBlank()) fields["Name"] = name
        if (startDate.isNotBlank()) fields["Start_Date__c"] = convertToIso(startDate)
        if (expirationDate.isNotBlank()) fields["ExpirationDate"] = convertToIso(expirationDate)
        if (endDate.isNotBlank()) fields["End_Date__c"] = convertToIso(endDate)
        if (term.isNotBlank()) fields["Term__c"] = term
        fields["Description"] = description // allowed to be blank/cleared
        return safeApiCallEmpty { api.updateQuote(quoteId, fields) }
    }
    suspend fun getQuote(quoteId: String): ApiResult<CpqQuote> {
        val soql = "SELECT Id,Name,Status,QuoteNumber,GrandTotal,TotalPrice,Subtotal,Tax,Discount," +
                "OpportunityId,AccountId,ExpirationDate,Start_Date__c,End_Date__c,Term__c,Description,Opportunity.Name,Account.Name " +
                "FROM Quote WHERE Id='$quoteId'"
        return when (val r = safeApiCall { api.getQuoteDetail(soql) }) {
            is ApiResult.Success -> {
                val record = r.data.records.firstOrNull()
                if (record != null) ApiResult.Success(record.toCpqQuote())
                else ApiResult.Error("Quote not found")
            }
            is ApiResult.Error -> r
        }
    }

    suspend fun getQuotesByOpportunity(
        accountId: String,
        opportunityId: String,
    ): ApiResult<List<CpqQuote>> {
        return when (val r = safeApiCall { api.getQuotes(accountId) }) {
            is ApiResult.Success -> ApiResult.Success(r.data)
            is ApiResult.Error -> r
        }
    }
    suspend fun getCatalogs(): ApiResult<List<CpqCatalog>> =
        safeApiCall { api.getCatalogs() }.mapList()

    suspend fun getCategories(catalogId: String): ApiResult<List<CpqCategory>> =
        safeApiCall { api.getCategories(catalogId) }.mapList()

    suspend fun getProducts(catalogId: String, categoryId: String = ""): ApiResult<List<CpqProduct>> =
        safeApiCall { api.getProducts(catalogId, categoryId) }.mapList()

    suspend fun getSellingModels(productId: String): ApiResult<List<CpqSellingModel>> =
        safeApiCall { api.getSellingModels(productId) }.mapList()

    suspend fun getAttributes(productId: String): ApiResult<List<CpqAttribute>> =
        safeApiCall { api.getAttributes(productId) }.mapList()

    suspend fun getBundleComponents(productId: String): ApiResult<List<CpqBundleComponent>> =
        safeApiCall { api.getBundleComponents(productId) }.mapList()

    suspend fun getLines(quoteId: String): ApiResult<List<CpqQuoteLine>> =
        safeApiCall { api.getLines(quoteId) }.mapList()

    suspend fun saveLines(quoteId: String, lines: List<SaveLineItem>): ApiResult<Unit> {
        return when (val r = safeApiCall { api.saveLines(SaveLinesBody(quoteId, lines)) }) {
            is ApiResult.Success -> {
                val msg = r.data.message.orEmpty()

                if (
                    r.data.success ||
                    msg.equals("SUCCESS", ignoreCase = true) ||
                    msg.startsWith("SUCCESS", ignoreCase = true)
                ) {
                    ApiResult.Success(Unit)
                } else {
                    ApiResult.Error(msg.ifBlank { "Save lines failed" })
                }
            }

            is ApiResult.Error -> r
        }
    }

    suspend fun editLines(changes: List<EditLineChange>): ApiResult<Unit> =
        safeApiCallEmpty { api.editLines(EditLinesBody(changes)) }

    suspend fun deleteLines(ids: List<String>): ApiResult<Unit> =
        safeApiCallEmpty { api.deleteLines(ids.joinToString(",")) }

    suspend fun getPdfTemplates(): ApiResult<List<CpqPdfTemplate>> =
        safeApiCall { api.getPdfTemplates() }.mapList()

    suspend fun savePdf(templateId: String, quoteId: String): ApiResult<String> {
        val saveResult = safeApiCall { api.savePdf(SavePdfBody(templateId, quoteId)) }
        if (saveResult is ApiResult.Error) return saveResult
        val body = (saveResult as ApiResult.Success).data
        if (!body.isSuccess) return ApiResult.Error(body.message ?: "PDF generation failed")

        // Save succeeded but doesn't return the file — fetch the newest file on the quote instead.
        return when (val filesResult = safeApiCall { api.getFiles(quoteId) }) {
            is ApiResult.Success -> {
                val newest = filesResult.data.firstOrNull()
                if (newest != null && newest.downloadUrl.isNotBlank()) ApiResult.Success(newest.downloadUrl)
                else ApiResult.Error("PDF was generated, but no file could be found on the quote.")
            }
            is ApiResult.Error -> ApiResult.Error("PDF was generated, but couldn't load the file: ${filesResult.message}")
        }
    }

    suspend fun getApprovalStatus(quoteId: String): ApiResult<CpqApprovalStatus> =
        safeApiCall { api.getApprovalStatus(quoteId) }.map()

    suspend fun submitApproval(quoteId: String, comments: String): ApiResult<Unit> {
        val body = SubmitApprovalBody(
            requests = listOf(ApprovalRequestItem(contextId = quoteId, comments = comments))
        )
        return when (val r = safeApiCall { api.submitApproval(body) }) {
            is ApiResult.Success -> {
                val result = r.data.firstOrNull()
                if (result?.success == true) ApiResult.Success(Unit)
                else ApiResult.Error(result?.errors?.firstOrNull()?.message ?: "Submit approval failed")
            }
            is ApiResult.Error -> r
        }
    }
    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun getCurrentDate(): String {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                java.time.LocalDate.now().toString()
            } else {
                val cal = java.util.Calendar.getInstance()
                "${cal.get(java.util.Calendar.YEAR)}-${(cal.get(java.util.Calendar.MONTH)+1).toString().padStart(2,'0')}-${cal.get(java.util.Calendar.DAY_OF_MONTH).toString().padStart(2,'0')}"
            }
        } catch (e: Exception) { "2026-01-01" }
    }

    private fun getExpiryDate(): String {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                java.time.LocalDate.now().plusMonths(3).toString()
            } else {
                val cal = java.util.Calendar.getInstance()
                cal.add(java.util.Calendar.MONTH, 3)
                "${cal.get(java.util.Calendar.YEAR)}-${(cal.get(java.util.Calendar.MONTH)+1).toString().padStart(2,'0')}-${cal.get(java.util.Calendar.DAY_OF_MONTH).toString().padStart(2,'0')}"
            }
        } catch (e: Exception) { "2026-12-31" }
    }

    private fun <T> ApiResult<T>.map(): ApiResult<T> = this
    private fun <T> ApiResult<List<T>>.mapList(): ApiResult<List<T>> = when (this) {
        is ApiResult.Success -> ApiResult.Success(data)
        is ApiResult.Error -> ApiResult.Error(message, code)
    }
    suspend fun downloadPdfBytes(relativeUrl: String): ApiResult<ByteArray> {
        return try {
            val fullUrl = NetworkModule.instanceUrl.trimEnd('/') + relativeUrl
            val response = api.downloadFile(fullUrl)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!.bytes())
            } else {
                ApiResult.Error("Download failed: ${response.code()}")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Download error")
        }
    }
    // Converts dd-mm-yyyy to yyyy-mm-dd for Salesforce
    private fun convertToIso(date: String): String {
        return try {
            val parts = date.split("-")
            if (parts.size != 3) return date
            // Handle dd-mm-yyyy format (day is 1-2 digits, year is 4 digits)
            return if (parts[2].length == 4) {
                // dd-mm-yyyy → yyyy-mm-dd
                val day = parts[0].toInt().toString().padStart(2, '0')
                val month = parts[1].toInt().toString().padStart(2, '0')
                "${parts[2]}-$month-$day"
            } else {
                // Already yyyy-mm-dd or unknown — return as is
                date
            }
        } catch (e: Exception) { date }
    }

    suspend fun getFavourites(): ApiResult<List<CpqFavourite>> {
        return when (val r = safeApiCall { api.getFavourites() }) {
            is ApiResult.Success -> ApiResult.Success(r.data)
            is ApiResult.Error -> r
        }
    }

    suspend fun getFavouriteLines(favouriteId: String): ApiResult<List<CpqFavouriteLine>> {
        return when (val r = safeApiCall { api.getFavouriteLines(favouriteId) }) {
            is ApiResult.Success -> ApiResult.Success(r.data)
            is ApiResult.Error -> r
        }
    }

    suspend fun saveFavourite(
        label: String,
        description: String,
        lines: List<CpqQuoteLine>,
    ): ApiResult<String> {
        val body = SaveFavouriteBody(
            label = label,
            description = description,
            lines = lines.map { line ->
                SaveFavouriteLine(
                    productId = line.productId,
                    quantity = line.quantity,
                    unitPrice = line.unitPrice,
                )
            },
        )
        return when (val r = safeApiCall { api.saveFavourite(body) }) {
            is ApiResult.Success -> ApiResult.Success(r.data.id)
            is ApiResult.Error -> r
        }
    }

    suspend fun deleteFavourite(favouriteId: String): ApiResult<Unit> =
        safeApiCallEmpty { api.deleteFavourite(favouriteId) }
}