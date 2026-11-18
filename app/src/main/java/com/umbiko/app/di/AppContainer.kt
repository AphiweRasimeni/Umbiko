package com.umbiko.app.di

import android.content.Context
import com.umbiko.app.data.ConnectivityObserver
import com.umbiko.app.data.TokenManager
import com.umbiko.app.data.local.AppDatabase
import com.umbiko.app.data.remote.ApiService
import com.umbiko.app.data.remote.NetworkModule
import com.umbiko.app.data.repository.UmbikoRepository
import com.umbiko.app.push.NotificationHelper
import com.umbiko.app.sync.SyncScheduler

/**
 * Hand-written dependency container (a DI framework such as Hilt is overkill for an
 * app this size). One instance lives in [com.umbiko.app.UmbikoApplication].
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val tokenManager: TokenManager = TokenManager(appContext)
    val database: AppDatabase = AppDatabase.getInstance(appContext)
    val apiService: ApiService = NetworkModule.createApiService(tokenManager)
    val connectivityObserver: ConnectivityObserver = ConnectivityObserver(appContext)
    val repository: UmbikoRepository = UmbikoRepository(
        api = apiService,
        db = database,
        tokenManager = tokenManager,
        onOutboxChanged = { SyncScheduler.requestSync(appContext, tokenManager.syncOnWifiOnly) },
        onNotification = { title, body -> NotificationHelper.show(appContext, title, body) },
    )
}
