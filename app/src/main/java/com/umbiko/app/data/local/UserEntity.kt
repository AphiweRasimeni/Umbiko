// UserEntity.kt
package com.umbiko.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val fullName: String,
    val email: String,      // stored lowercase
    val mobile: String,
    val areaId: String,
    val salt: String,
    val passwordHash: String,
)