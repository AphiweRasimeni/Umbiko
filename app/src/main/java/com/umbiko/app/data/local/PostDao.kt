package com.umbiko.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("""
        SELECT * FROM posts 
        WHERE (:areaId IS NULL OR areaId = :areaId OR areaId = '' OR areaId = 'area-1') 
        AND (:type IS NULL OR type = :type) 
        ORDER BY createdAt DESC
    """)
    fun observePosts(areaId: String?, type: String?): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE postId = :postId")
    fun observePost(postId: String): Flow<PostEntity?>

    @Query("SELECT * FROM posts WHERE postId = :postId")
    suspend fun getPost(postId: String): PostEntity?

    @Query("SELECT * FROM posts WHERE type = 'EVENT' AND myRsvpResponse IS NOT NULL ORDER BY eventStartsAt ASC")
    fun observeMyEvents(): Flow<List<PostEntity>>

    @Upsert
    suspend fun upsert(post: PostEntity)

    @Upsert
    suspend fun upsertAll(posts: List<PostEntity>)

    @Update
    suspend fun update(post: PostEntity)

    @Query("DELETE FROM posts WHERE postId = :postId")
    suspend fun delete(postId: String)

    @Query("UPDATE posts SET rsvpGoing = :going, rsvpInterested = :interested, rsvpNotGoing = :notGoing, myRsvpResponse = :myResponse WHERE postId = :postId")
    suspend fun updateRsvpCounts(postId: String, going: Int, interested: Int, notGoing: Int, myResponse: String)

    @Query("UPDATE posts SET commentCount = commentCount + 1 WHERE postId = :postId")
    suspend fun incrementCommentCount(postId: String)
}
