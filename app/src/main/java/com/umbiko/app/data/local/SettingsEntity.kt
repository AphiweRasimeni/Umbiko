package com.umbiko.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val languageCode: String = "en",
    val radiusKm: Int = 20,
    val pushOptIn: Boolean = true,
    val syncOnWifiOnly: Boolean = false,
    val biometricUnlock: Boolean = false,
)
