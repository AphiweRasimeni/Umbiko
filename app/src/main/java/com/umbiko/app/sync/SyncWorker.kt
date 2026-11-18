package com.umbiko.app.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.umbiko.app.UmbikoApplication
import com.umbiko.app.domain.ApiResult

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val application = applicationContext as? UmbikoApplication ?: return Result.failure()
        val repository = application.container.repository

        return when (repository.drainOutbox()) {
            is ApiResult.Success -> Result.success()
            is ApiResult.NetworkError -> Result.retry()
            is ApiResult.Error -> Result.failure()
        }
    }
}
