package com.team.notify.taskflow.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.TypeConverter
import com.team.notify.taskflow.model.UserStatus
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "user_profiles",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["email"]),
        Index(value = ["updatedAt"])
    ]
)
data class UserProfileEntity(
    @PrimaryKey val userId: String,
    val email: String,
    val displayName: String,
    val username: String,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val status: UserStatus = UserStatus.ACTIVE,
    val isEmailVerified: Boolean = false,
    val lastLoginAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val preferences: UserPreferences? = null
)

data class UserPreferences(
    @SerializedName("theme") val theme: String = "system", // system, light, dark
    @SerializedName("language") val language: String = "en", // en, es, fr, de, etc.
    @SerializedName("notificationsEnabled") val notificationsEnabled: Boolean = true,
    @SerializedName("emailNotifications") val emailNotifications: Boolean = true,
    @SerializedName("pushNotifications") val pushNotifications: Boolean = true,
    @SerializedName("autoSync") val autoSync: Boolean = true,
    @SerializedName("dateFormat") val dateFormat: String = "MM/dd/yyyy", // MM/dd/yyyy, dd/MM/yyyy, yyyy-MM-dd
    @SerializedName("timeFormat") val timeFormat: String = "h:mm a", // h:mm a, HH:mm, 24-hour
    @SerializedName("weekStartDay") val weekStartDay: Int = 0, // 0=Sunday, 1=Monday, etc.
    @SerializedName("defaultPageType") val defaultPageType: String = "TASKS" // TASKS, NOTES, DOCUMENT
)
