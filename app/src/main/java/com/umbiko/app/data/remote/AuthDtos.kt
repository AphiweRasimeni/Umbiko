package com.umbiko.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    @SerialName("full_name") val fullName: String,
    val email: String,
    val mobile: String,
    @SerialName("area_id") val areaId: String,
    val password: String,
)

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class TokenResponseDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("token_type") val tokenType: String = "Bearer",
    val user: UserDto,
)

@Serializable
data class UserDto(
    @SerialName("user_id") val userId: String,
    @SerialName("full_name") val fullName: String,
    val email: String,
    @SerialName("area_id") val areaId: String,
)

@Serializable
data class RefreshRequestDto(
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
data class RefreshResponseDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
data class SettingsResponseDto(
    @SerialName("language_code") val languageCode: String = "en",
    @SerialName("radius_km") val radiusKm: Int = 20,
    @SerialName("push_opt_in") val pushOptIn: Boolean = true,
    @SerialName("sync_on_wifi_only") val syncOnWifiOnly: Boolean = false,
    @SerialName("biometric_unlock") val biometricUnlock: Boolean = false,
)

@Serializable
data class SettingsUpdateDto(
    @SerialName("language_code") val languageCode: String? = null,
    @SerialName("radius_km") val radiusKm: Int? = null,
    @SerialName("push_opt_in") val pushOptIn: Boolean? = null,
    @SerialName("sync_on_wifi_only") val syncOnWifiOnly: Boolean? = null,
    @SerialName("biometric_unlock") val biometricUnlock: Boolean? = null,
)

@Serializable
data class ApiErrorDto(
    val detail: String? = null,
    val message: String? = null,
)
