package com.example.nexoworxcrmapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attachments")
data class AttachmentEntity(
    @PrimaryKey val id: String,          // "local_<uuid>" until synced, then real ContentDocumentId
    val parentRecordId: String,           // Lead/Account/etc. Id this file belongs to
    val title: String,
    val fileExtension: String,
    val localFilePath: String,            // path on device disk; blank once cleaned up post-sync
    val fileSizeBytes: Long,
    val lastModifiedDate: String,
    val syncStatus: String,
    val lastModifiedLocal: Long,
)