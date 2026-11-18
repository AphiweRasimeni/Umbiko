package com.umbiko.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Body for POST /auth/google: the Google ID token the backend verifies (SSO). */
@Serializable
data class GoogleLoginRequestDto(
    @SerialName("id_token") val idToken: String,
)

/** Body for POST /users/me/device-token: the FCM registration token for push. */
@Serializable
data class DeviceTokenDto(
    val token: String,
)
