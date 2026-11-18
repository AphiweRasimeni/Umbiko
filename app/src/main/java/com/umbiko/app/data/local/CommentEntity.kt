package com.umbiko.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val commentId: String,
    val postId: String,
    val userId: String,
    val userName: String,
    val body: String,
    val createdAt: String,
    val syncState: String = "SYNCED",
)
