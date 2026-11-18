package com.umbiko.app

import android.app.Application
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.umbiko.app.di.AppContainer
import com.umbiko.app.push.NotificationHelper
import com.umbiko.app.sync.SyncScheduler
import com.umbiko.app.sync.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.TimeUnit

class UmbikoApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.createChannel(this)

        val wifiOnly = container.tokenManager.syncOnWifiOnly

        // Drain anything left over from a previous session.
        SyncScheduler.requestSync(this, wifiOnly)

        // Periodic safety net (15 min is the platform minimum). UPDATE replaces an
        // existing job so a changed Wi-Fi-only setting takes effect on next launch.
        val periodicRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(SyncScheduler.networkType(wifiOnly))
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "umbiko_periodic_sync",
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRequest,
        )

        // Responsive path: the moment the device is next online, sync immediately.
        container.connectivityObserver.isOnline
            .distinctUntilChanged()
            .onEach { online ->
                Log.i("UmbikoApplication", "Connectivity changed: online=$online")
                if (online) {
                    SyncScheduler.requestSync(this, container.tokenManager.syncOnWifiOnly)
                }
            }
            .launchIn(applicationScope)
    }
}
