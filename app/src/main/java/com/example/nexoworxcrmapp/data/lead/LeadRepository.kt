package com.example.nexoworxcrmapp.data.lead

import com.example.nexoworxcrmapp.data.Lead
import com.example.nexoworxcrmapp.data.lead.network.*
import com.example.nexoworxcrmapp.data.local.dao.LeadDao
import com.example.nexoworxcrmapp.data.local.dao.PendingOperationDao
import com.example.nexoworxcrmapp.data.local.entity.OperationType
import com.example.nexoworxcrmapp.data.local.entity.PendingOperationEntity
import com.example.nexoworxcrmapp.data.local.entity.SyncStatus
import com.example.nexoworxcrmapp.network.ApiResult
import com.example.nexoworxcrmapp.network.safeApiCall
import com.example.nexoworxcrmapp.network.safeApiCallEmpty
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class LeadRepository(
    private val api: LeadApiService,
    private val leadDao: LeadDao,
    private val pendingOpDao: PendingOperationDao,
    private val gson: Gson = Gson(),
) {
    fun observeLeads(): Flow<List<Lead>> = leadDao.observeAll().map { it.map { e -> e.toDomain() } }

    // ---- Local-first writes ----

    /** Instant local write. Returns immediately; sync happens in the background. */
    suspend fun createLead(lead: Lead): Lead {
        val localId = "local_${UUID.randomUUID()}"
        val entity = lead.copy(id = localId).toEntity(SyncStatus.PENDING_CREATE)
        leadDao.upsert(entity)
        pendingOpDao.insert(
            PendingOperationEntity(
                entityType = "LEAD",
                entityId = localId,
                operationType = OperationType.CREATE,
                payloadJson = gson.toJson(lead.copy(id = localId).toCreateRequest()),
                createdAt = System.currentTimeMillis(),
            ),
        )
        return entity.toDomain()
    }

    suspend fun updateLead(id: String, lead: Lead) {
        val existing = leadDao.getById(id)
        val newStatus = if (existing?.syncStatus == SyncStatus.PENDING_CREATE) {
            SyncStatus.PENDING_CREATE // still hasn't hit the server — stays a CREATE
        } else {
            SyncStatus.PENDING_UPDATE
        }
        leadDao.upsert(lead.copy(id = id).toEntity(newStatus))
        if (newStatus == SyncStatus.PENDING_UPDATE) {
            pendingOpDao.insert(
                PendingOperationEntity(
                    entityType = "LEAD",
                    entityId = id,
                    operationType = OperationType.UPDATE,
                    payloadJson = gson.toJson(lead.toPatchRequest()),
                    createdAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    suspend fun deleteLead(id: String) {
        val existing = leadDao.getById(id) ?: return
        if (existing.syncStatus == SyncStatus.PENDING_CREATE) {
            // never reached the server — just remove it and its queued create
            leadDao.deleteById(id)
            pendingOpDao.deleteForEntity(id)
        } else {
            leadDao.setSyncStatus(id, SyncStatus.PENDING_DELETE)
            pendingOpDao.insert(
                PendingOperationEntity(
                    entityType = "LEAD",
                    entityId = id,
                    operationType = OperationType.DELETE,
                    payloadJson = "{}",
                    createdAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    suspend fun convertLead(id: String): ApiResult<Unit> {
        // online-only for the pilot — see Step 10 notes
        val body = ApexConvertRequest(leadId = id)
        return when (val result = safeApiCall { api.convertLead(body) }) {
            is ApiResult.Success ->
                if (result.data.success) { leadDao.deleteById(id); ApiResult.Success(Unit) }
                else ApiResult.Error(message = result.data.error ?: "Convert failed")
            is ApiResult.Error -> result
        }
    }

    // ---- Sync engine hooks (called by SyncManager, not the UI) ----

    suspend fun pullFromServer() {
        val result = safeApiCall { api.queryLeads() }
        if (result is ApiResult.Success) {
            val dirtyIds = leadDao.getDirtyIds().toSet()
            val serverEntities = result.data.records
                .map { it.toDomain().toEntity(SyncStatus.SYNCED) }
                .filter { it.id !in dirtyIds } // never overwrite un-synced local edits
            leadDao.upsertAll(serverEntities)
        }
    }

    suspend fun pushCreate(op: PendingOperationEntity): Boolean {
        val request = gson.fromJson(op.payloadJson, SalesforceLeadCreateRequest::class.java)
        return when (val result = safeApiCall { api.createLead(request) }) {
            is ApiResult.Success -> {
                if (!result.data.success) return false
                val fresh = safeApiCall { api.getLead(result.data.id) }
                if (fresh is ApiResult.Success) {
                    leadDao.replaceLocalWithServer(op.entityId, fresh.data.toDomain().toEntity(SyncStatus.SYNCED))
                }
                true
            }
            is ApiResult.Error -> false
        }
    }

    suspend fun pushUpdate(op: PendingOperationEntity): Boolean {
        val request = gson.fromJson(op.payloadJson, SalesforceLeadPatchRequest::class.java)
        return when (safeApiCallEmpty { api.updateLead(op.entityId, request) }) {
            is ApiResult.Success -> { leadDao.setSyncStatus(op.entityId, SyncStatus.SYNCED); true }
            is ApiResult.Error -> false
        }
    }

    suspend fun pushDelete(op: PendingOperationEntity): Boolean {
        return when (safeApiCallEmpty { api.deleteLead(op.entityId) }) {
            is ApiResult.Success -> { leadDao.deleteById(op.entityId); true }
            is ApiResult.Error -> false
        }
    }
}