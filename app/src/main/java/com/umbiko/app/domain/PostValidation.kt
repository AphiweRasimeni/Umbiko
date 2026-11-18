package com.umbiko.app.domain

object PostValidation {
    fun validate(draft: NewPostDraft): String? {
        val title = draft.title.trim()
        if (title.length !in 5..80) return "Title must be 5–80 characters"
        val body = draft.body.trim()
        if (body.isEmpty() || body.length > 1000) return "Details must be 1–1000 characters"
        if (draft.type == "EVENT" && (draft.eventStartsAtIso == null || draft.eventStartsAtIso.isBlank())) {
            return "Choose a start date and time for the event"
        }
        return null
    }
}
