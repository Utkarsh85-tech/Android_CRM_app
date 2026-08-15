package com.example.nexoworxcrmapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.nexoworxcrmapp.data.local.dao.LeadDao
import com.example.nexoworxcrmapp.data.local.dao.PendingOperationDao
import com.example.nexoworxcrmapp.data.local.entity.LeadEntity
import com.example.nexoworxcrmapp.data.local.entity.PendingOperationEntity
import com.example.nexoworxcrmapp.data.local.entity.TaskEntity
import com.example.nexoworxcrmapp.data.local.dao.TaskDao
import com.example.nexoworxcrmapp.data.local.dao.AttachmentDao
import com.example.nexoworxcrmapp.data.local.entity.AttachmentEntity
@Database(
    entities = [LeadEntity::class, PendingOperationEntity::class, TaskEntity::class, AttachmentEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun leadDao(): LeadDao
    abstract fun taskDao(): TaskDao
    abstract fun pendingOperationDao(): PendingOperationDao

    abstract fun attachmentDao(): AttachmentDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: run {
                    System.loadLibrary("sqlcipher")
                    val passphrase = DbPassphraseProvider.getOrCreatePassphrase(context)
                    val factory = net.zetetic.database.sqlcipher.SupportOpenHelperFactory(passphrase)
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "nexoworx_v2.db",
                    )
                        .openHelperFactory(factory)
                        .fallbackToDestructiveMigration()
                        .build().also { INSTANCE = it }
                }
            }
    }
}