package com.team.notify.taskflow.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.team.notify.taskflow.model.TaskStatus

@Entity(
    tableName = "tasks",
    indices = [Index(value = ["spaceId", "pageId", "updatedAt"])]
)
data class TaskEntity(
    @PrimaryKey val id: String,
    val spaceId: String,
    val pageId: String? = null,
    val title: String,
    val description: String? = null,
    val status: TaskStatus = TaskStatus.TODO,
    val deadline: Long? = null,
    val isCompleted: Boolean = (status == TaskStatus.DONE),
    val assigneeId: String? = null,
    val labels: List<String> = emptyList(),
    val ownerId: String = "", // Added for Firebase security rules
    val isSynced: Boolean = false, // Added for sync tracking
    val isDeleted: Boolean = false, // Added for soft delete
    val deletedAt: Long? = null, // Added for soft delete
    val priority: String = "medium", // Added for priority
    val updatedAt: Long = System.currentTimeMillis()
)
