package com.umbiko.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboxDao {
    @Query("SELECT COUNT(*) FROM outbox")
    fun observePendingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: OutboxEntity)

    @Query("SELECT * FROM outbox ORDER BY createdAt ASC")
    suspend fun getAll(): List<OutboxEntity>

    @Query("DELETE FROM outbox WHERE queueId = :queueId")
    suspend fun delete(queueId: String)
}
