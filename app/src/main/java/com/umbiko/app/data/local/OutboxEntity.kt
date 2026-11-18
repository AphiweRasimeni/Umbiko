package com.umbiko.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outbox")
data class OutboxEntity(
    @PrimaryKey val queueId: String,
    val entityName: String,
    val operation: String,
    val payloadJson: String,
    val createdAt: Long,
)
