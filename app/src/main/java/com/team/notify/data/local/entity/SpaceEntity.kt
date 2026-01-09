package com.team.notify.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spaces")
data class SpaceEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val ownerId: String,
    val updatedAt: Long,
    val isSynced: Boolean,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
)
