package com.team.notify.taskflow.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
