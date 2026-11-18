package com.umbiko.app.push

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.umbiko.app.MainActivity
import com.umbiko.app.R

/**
 * Shows real system notifications (sync results, safety alerts delivered by FCM).
 * The in-app Notifications screen reads the Room log; this makes the same events
 * visible outside the app.
 *
 * Reference: Android Developers (2025) Create a notification.
 * https://developer.android.com/develop/ui/views/notifications/build-notification [Accessed 5 November 2026].
 */
object NotificationHelper {
    const val CHANNEL_ID = "umbiko_updates"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    // areNotificationsEnabled() also covers the Android 13 POST_NOTIFICATIONS permission.
    @SuppressLint("MissingPermission")
    fun show(context: Context, title: String, body: String) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pending = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
