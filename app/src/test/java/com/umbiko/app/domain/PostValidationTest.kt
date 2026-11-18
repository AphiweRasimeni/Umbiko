package com.umbiko.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Unit tests for the Create Post validation rules (FR-07). */
class PostValidationTest {

    private fun notice(title: String = "Water outage Friday", body: String = "Taps off 9am to 1pm.") =
        NewPostDraft(type = "NOTICE", title = title, body = body)

    @Test fun validNotice_returnsNull() = assertNull(PostValidation.validate(notice()))

    @Test fun shortTitle_isRejected() =
        assertEquals("Title must be 5–80 characters", PostValidation.validate(notice(title = "Hi")))

    @Test fun longTitle_isRejected() =
        assertEquals("Title must be 5–80 characters", PostValidation.validate(notice(title = "x".repeat(81))))

    @Test fun blankBody_isRejected() =
        assertEquals("Details must be 1–1000 characters", PostValidation.validate(notice(body = "   ")))

    @Test fun eventWithoutStart_isRejected() {
        val draft = NewPostDraft(type = "EVENT", title = "Community clean-up", body = "Bring gloves.")
        assertEquals("Choose a start date and time for the event", PostValidation.validate(draft))
    }

    @Test fun eventWithStart_isAccepted() {
        val draft = NewPostDraft(
            type = "EVENT", title = "Community clean-up", body = "Bring gloves.",
            eventStartsAtIso = "2026-12-01T08:00:00Z",
        )
        assertNull(PostValidation.validate(draft))
    }
}
