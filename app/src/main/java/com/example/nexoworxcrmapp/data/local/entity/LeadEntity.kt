package com.example.nexoworxcrmapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * "id" is either a Salesforce Id (once synced) or a "local_<uuid>" placeholder
 * for a record created offline that hasn't been pushed yet.
 */
@Entity(tableName = "leads")
data class LeadEntity(
    @PrimaryKey val id: String,
    val firstName: String,
    val lastName: String,
    val company: String,
    val status: String,
    val phone: String,
    val email: String,
    val source: String,
    val rating: String,
    val industry: String,
    val title: String,
    val description: String,
    val syncStatus: String,       // SYNCED | PENDING_CREATE | PENDING_UPDATE | PENDING_DELETE | FAILED
    val lastModifiedLocal: Long,
)

object SyncStatus {
    const val SYNCED = "SYNCED"
    const val PENDING_CREATE = "PENDING_CREATE"
    const val PENDING_UPDATE = "PENDING_UPDATE"
    const val PENDING_DELETE = "PENDING_DELETE"
    const val FAILED = "FAILED"
}