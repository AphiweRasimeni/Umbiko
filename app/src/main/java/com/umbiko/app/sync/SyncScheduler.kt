package com.umbiko.app.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Schedules [SyncWorker]. Called whenever the repository queues something and when
 * connectivity returns. Part 3: honours the "Sync on Wi-Fi only" setting by requiring
 * an UNMETERED network instead of any connected one.
 */
object SyncScheduler {

    private const val UNIQUE_WORK_NAME = "umbiko_outbox_sync"

    fun networkType(wifiOnly: Boolean): NetworkType =
        if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED

    fun requestSync(context: Context, wifiOnly: Boolean = false) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(networkType(wifiOnly))
            .build()

        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        // KEEP: a sync already queued/running will drain the whole outbox anyway.
        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }
}
