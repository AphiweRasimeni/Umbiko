package com.umbiko.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {

    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt ASC")
    fun observeForPost(postId: String): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(comment: CommentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(comments: List<CommentEntity>)

    /** Once the server has accepted a queued comment, the server's copy (with its own id)
     * replaces the local PENDING one, so the local one is removed to avoid duplicates. */
    @Query("DELETE FROM comments WHERE postId = :postId AND syncState = 'PENDING'")
    suspend fun deletePendingForPost(postId: String)
}
