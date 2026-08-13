package com.example.nexoworxcrmapp.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nexoworxcrmapp.network.NetworkModule

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            NetworkModule.syncManager.sync()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}