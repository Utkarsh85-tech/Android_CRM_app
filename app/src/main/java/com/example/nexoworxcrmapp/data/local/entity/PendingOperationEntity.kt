package com.example.nexoworxcrmapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
const val MAX_SYNC_RETRIES = 5
@Entity(tableName = "pending_operations")
data class PendingOperationEntity(
    @PrimaryKey(autoGenerate = true) val opId: Long = 0,
    val entityType: String,      // "LEAD" (extend later: "TASK", "EVENT", ...)
    val entityId: String,        // matches LeadEntity.id
    val operationType: String,   // CREATE | UPDATE | DELETE
    val payloadJson: String,
    val createdAt: Long,
    val retryCount: Int = 0,
    val lastError: String? = null,
)

object OperationType {
    const val CREATE = "CREATE"
    const val UPDATE = "UPDATE"
    const val DELETE = "DELETE"
}