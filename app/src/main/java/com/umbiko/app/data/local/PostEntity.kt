package com.umbiko.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val postId: String,
    val authorId: String,
    val authorName: String,
    val areaId: String,
    val categoryId: String = "",
    val type: String = "NOTICE",
    val title: String,
    val body: String,
    val imageUrl: String? = null,
    val status: String = "PUBLISHED",
    val createdAt: String,
    val updatedAt: String,
    val eventStartsAt: String? = null,
    val eventEndsAt: String? = null,
    val eventVenueName: String? = null,
    val eventLatitude: Double? = null,
    val eventLongitude: Double? = null,
    val rsvpGoing: Int = 0,
    val rsvpInterested: Int = 0,
    val rsvpNotGoing: Int = 0,
    val myRsvpResponse: String? = null,
    val commentCount: Int = 0,
    val syncState: String = "SYNCED",
)
