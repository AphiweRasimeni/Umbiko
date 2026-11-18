package com.umbiko.app.util

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Multi-language support (English, isiZulu, Afrikaans, Sesotho). Uses the AndroidX
 * per-app language API, which works on every supported Android version and is also
 * visible in the system "App languages" screen on Android 13+.
 *
 * Reference: Android Developers (2025) Per-app language support.
 * https://developer.android.com/guide/topics/resources/app-languages [Accessed 5 November 2026].
 */
object LocaleManager {
    private const val TAG = "LocaleManager"

    fun apply(languageCode: String) {
        val current = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (current == languageCode) return
        Log.i(TAG, "Switching app language: '$current' -> '$languageCode'")
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))
    }
}
