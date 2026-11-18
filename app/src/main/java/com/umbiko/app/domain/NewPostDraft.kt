package com.umbiko.app.domain

import kotlinx.serialization.Serializable

@Serializable
data class NewPostDraft(
    val categoryId: String? = null,
    val type: String = "NOTICE",
    val title: String = "",
    val body: String = "",
    val eventStartsAtIso: String? = null,
    val eventEndsAtIso: String? = null,
    val eventVenueName: String? = null,
)
