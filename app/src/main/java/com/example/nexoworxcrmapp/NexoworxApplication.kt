package com.example.nexoworxcrmapp

import android.app.Application
import androidx.work.*
import com.example.nexoworxcrmapp.data.sync.SyncWorker
import com.example.nexoworxcrmapp.network.NetworkModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class NexoworxApplication : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        NetworkModule.appContext = applicationContext

        // Periodic safety-net sync every 15 min while online
        val periodic = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork("periodic_sync", ExistingPeriodicWorkPolicy.KEEP, periodic)

        // Immediate sync the moment connectivity returns
        appScope.launch {
            NetworkModule.connectivityObserver.isOnline.collect { online ->
                if (online) {
                    val oneOff = OneTimeWorkRequestBuilder<SyncWorker>()
                        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                        .build()
                    WorkManager.getInstance(this@NexoworxApplication)
                        .enqueueUniqueWork("connectivity_sync", ExistingWorkPolicy.KEEP, oneOff)
                }
            }
        }
    }
}