package com.umbiko.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notification_log ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<NotificationLogEntity>>

    @Query("SELECT COUNT(*) FROM notification_log WHERE read = 0")
    fun observeUnreadCount(): Flow<Int>

    @Query("UPDATE notification_log SET read = 1 WHERE read = 0")
    suspend fun markAllRead()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationLogEntity)
}
