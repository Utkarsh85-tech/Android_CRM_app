package com.example.nexoworxcrmapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val subject: String,
    val status: String,
    val priority: String,
    val dueDate: String,
    val whatId: String,
    val whoId: String,
    val description: String,
    val syncStatus: String,
    val lastModifiedLocal: Long,
)