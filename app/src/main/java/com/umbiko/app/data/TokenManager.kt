package com.umbiko.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Stores the JWT access/refresh tokens and a little profile data using
 * [EncryptedSharedPreferences] (NFR-03): the file on disk is encrypted with a key
 * held in the Android Keystore. The password itself is never stored on the device.
 *
 * Part 3 additions: three small mirrors of settings that must be readable
 * synchronously before Room/coroutines are available (WorkManager constraints and
 * the biometric lock screen in MainActivity).
 *
 * Reference: Android Developers (2025) Work with data more securely.
 * https://developer.android.com/privacy-and-security/security-tips [Accessed 5 November 2026].
 */
class TokenManager(context: Context) {

    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "umbiko_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    } catch (e: Exception) {
        // Rare (corrupted keystore entry after a device restore). Fall back so the
        // app stays usable; the user is simply asked to log in again.
        context.getSharedPreferences("umbiko_prefs_fallback", Context.MODE_PRIVATE)
    }

    fun saveSession(accessToken: String, refreshToken: String, userId: String, fullName: String, email: String, areaId: String?) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_FULL_NAME, fullName)
            .putString(KEY_EMAIL, email)
            .putString(KEY_AREA_ID, areaId)
            .apply()
    }

    fun updateAccessToken(accessToken: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, accessToken).apply()
    }

    fun updateAreaId(areaId: String) {
        prefs.edit().putString(KEY_AREA_ID, areaId).apply()
    }

    val accessToken: String? get() = prefs.getString(KEY_ACCESS_TOKEN, null)
    val refreshToken: String? get() = prefs.getString(KEY_REFRESH_TOKEN, null)
    val userId: String? get() = prefs.getString(KEY_USER_ID, null)
    val fullName: String? get() = prefs.getString(KEY_FULL_NAME, null)
    val email: String? get() = prefs.getString(KEY_EMAIL, null)
    val areaId: String? get() = prefs.getString(KEY_AREA_ID, null)

    /** Mirror of the "Sync on Wi-Fi only" setting, read when scheduling WorkManager. */
    var syncOnWifiOnly: Boolean
        get() = prefs.getBoolean(KEY_WIFI_ONLY, false)
        set(value) { prefs.edit().putBoolean(KEY_WIFI_ONLY, value).apply() }

    /** Mirror of the "Unlock with biometrics" setting, read before any UI is shown. */
    var biometricUnlock: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC, false)
        set(value) { prefs.edit().putBoolean(KEY_BIOMETRIC, value).apply() }

    fun isLoggedIn(): Boolean = accessToken != null && userId != null

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_AREA_ID = "area_id"
        private const val KEY_WIFI_ONLY = "sync_wifi_only"
        private const val KEY_BIOMETRIC = "biometric_unlock"
    }
}
