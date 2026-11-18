package com.umbiko.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class AreaResponseDto(
    @SerialName("area_id") val areaId: String,
    val name: String,
    val code: String = "",
)

@Serializable
data class CategoryResponseDto(
    @SerialName("category_id") val categoryId: String,
    @SerialName("name_key") val nameKey: String,
    @SerialName("icon_name") val iconName: String = "",
)

@Serializable
data class EventDetailInDto(
    @SerialName("starts_at") val startsAt: String? = null,
    @SerialName("ends_at") val endsAt: String? = null,
    @SerialName("venue_name") val venueName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

@Serializable
data class EventDetailOutDto(
    @SerialName("starts_at") val startsAt: String? = null,
    @SerialName("ends_at") val endsAt: String? = null,
    @SerialName("venue_name") val venueName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

@Serializable
data class PostCreateDto(
    val type: String,
    val title: String,
    val body: String,
    @SerialName("category_id") val categoryId: String? = null,
    val event: EventDetailInDto? = null,
    @SerialName("client_post_id") val clientPostId: String? = null,
)

@Serializable
data class UserSummaryDto(
    @SerialName("user_id") val userId: String,
    @SerialName("full_name") val fullName: String,
)

@Serializable
data class RsvpCountsDto(
    val going: Int = 0,
    val interested: Int = 0,
    @SerialName("not_going") val notGoing: Int = 0,
    @SerialName("my_response") val myResponse: String? = null,
)

@Serializable
data class PostResponseDto(
    @SerialName("post_id") val postId: String,
    val author: UserSummaryDto,
    @SerialName("area_id") val areaId: String,
    @SerialName("category_id") val categoryId: String = "",
    val type: String,
    val title: String,
    val body: String,
    @SerialName("image_url") val imageUrl: String? = null,
    val status: String = "PUBLISHED",
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    val event: EventDetailOutDto? = null,
    @SerialName("rsvp_counts") val rsvpCounts: RsvpCountsDto = RsvpCountsDto(),
    @SerialName("comment_count") val commentCount: Int = 0,
)

@Serializable
data class PostListResponseDto(
    val items: List<PostResponseDto> = emptyList(),
    val total: Int = 0,
)

@Serializable
data class PostUpdateDto(
    val title: String? = null,
    val body: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
)

@Serializable
data class RsvpRequestDto(
    @SerialName("post_id") val postId: String,
    val response: String,
)

@Serializable
data class CommentResponseDto(
    @SerialName("comment_id") val commentId: String,
    @SerialName("post_id") val postId: String,
    val user: UserSummaryDto,
    val body: String,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class CommentCreateDto(
    @SerialName("post_id") val postId: String,
    val body: String,
)

@Serializable
data class OutboxOperationDto(
    @SerialName("queue_id") val queueId: String,
    @SerialName("entity_name") val entityName: String,
    val operation: String,
    val payload: JsonElement,
)

@Serializable
data class SyncPushRequestDto(
    val operations: List<OutboxOperationDto>,
)

@Serializable
data class SyncOperationResultDto(
    @SerialName("queue_id") val queueId: String,
    val status: String,
    @SerialName("server_id") val serverId: String? = null,
    val message: String? = null,
)

@Serializable
data class SyncPushResponseDto(
    val results: List<SyncOperationResultDto> = emptyList(),
)

@Serializable
data class SyncPullResponseDto(
    val posts: List<PostResponseDto> = emptyList(),
    val comments: List<CommentResponseDto> = emptyList(),
)
