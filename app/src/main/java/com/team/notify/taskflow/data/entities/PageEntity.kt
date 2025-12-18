package com.team.notify.taskflow.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pages")
data class PageEntity(
    @PrimaryKey val id: String,
    val spaceId: String,
    val title: String,
    val content: String?,
    val version: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val isLoading: Boolean = false,
    val hasConflict: Boolean = false
)
