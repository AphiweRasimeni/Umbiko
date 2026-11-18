package com.umbiko.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_log")
data class NotificationLogEntity(
    @PrimaryKey val notifId: String,
    val type: String,
    val title: String,
    val body: String,
    val relatedPostId: String? = null,
    val createdAt: Long,
    val read: Boolean = false,
)
