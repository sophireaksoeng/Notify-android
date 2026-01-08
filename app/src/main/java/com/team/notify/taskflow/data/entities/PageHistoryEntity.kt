package com.team.notify.taskflow.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "page_history")
data class PageHistoryEntity(
    @PrimaryKey val id: String,
    val pageId: String,
    val version: Int,
    val content: String,
    val timestamp: Long
)
