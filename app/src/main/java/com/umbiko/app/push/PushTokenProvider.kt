package com.umbiko.app.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Fetches this device's FCM registration token. Returns null (instead of crashing)
 * when Firebase is not configured, i.e. there is no google-services.json.
 */
object PushTokenProvider {
    private const val TAG = "PushTokenProvider"

    suspend fun currentToken(): String? = try {
        suspendCancellableCoroutine { cont ->
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                cont.resume(if (task.isSuccessful) task.result else null)
            }
        }
    } catch (e: Exception) {
        Log.w(TAG, "FCM unavailable (is google-services.json present?): ${e.message}")
        null
    }
}
