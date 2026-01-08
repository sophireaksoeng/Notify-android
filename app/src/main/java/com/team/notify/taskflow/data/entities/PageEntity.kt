package com.team.notify.taskflow.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.team.notify.taskflow.model.PageType

@Entity(tableName = "pages")
data class PageEntity(
    @PrimaryKey val id: String,
    val spaceId: String,
    val title: String,
    val content: String? = "",
    val pageType: PageType = PageType.TASKS, // New field for page type
    val version: Long = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val hasConflict: Boolean? = false,
    val ownerId: String = "", // Added for Firebase security rules
    val isSynced: Boolean = false, // Added for sync tracking
    val isDeleted: Boolean = false, // Added for soft delete
    val deletedAt: Long? = null // Added for soft delete
)
