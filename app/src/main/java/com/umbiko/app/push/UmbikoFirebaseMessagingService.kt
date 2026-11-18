package com.umbiko.app.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.umbiko.app.R
import com.umbiko.app.UmbikoApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Real-time notifications (PoE requirement). The backend sends DATA messages
 * (type, title, body, post_id); this service stores them in the Room notification
 * log and shows a system notification.
 *
 * Reference: Firebase (2025) Receive messages in an Android app.
 * https://firebase.google.com/docs/cloud-messaging/android/receive [Accessed 5 November 2026].
 */
class UmbikoFirebaseMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val repository get() = (application as UmbikoApplication).container.repository

    override fun onNewToken(token: String) {
        Log.i(TAG, "New FCM token received")
        scope.launch { repository.registerPushToken(token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val title = data["title"] ?: message.notification?.title ?: getString(R.string.app_name)
        val body = data["body"] ?: message.notification?.body ?: ""
        val type = data["type"] ?: "NEW_POST"
        Log.i(TAG, "Push received: type=$type")
        scope.launch {
            repository.recordPushNotification(type, title, body, data["post_id"])
            repository.refreshPosts()
        }
    }

    private companion object {
        const val TAG = "UmbikoFcm"
    }
}
