package com.team.notify.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TaskPriority(val value: String) {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high");

    companion object {
        fun fromString(value: String?): TaskPriority = values().find { it.value == value } ?: MEDIUM
    }
}

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = PageEntity::class,
            parentColumns = ["id"],
            childColumns = ["pageId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["pageId"]),
        Index(value = ["status"]),
        Index(value = ["dueDate"]),
        Index(value = ["priority"], name = "index_tasks_priority"),
        Index(value = ["updatedAt"]),
        Index(value = ["isSynced"]),
    ],
)
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val pageId: String,
    val title: String,
    val status: String,
    val dueDate: Long?,
    val assigneeId: String?,
    val priority: String = TaskPriority.MEDIUM.value,
    val updatedAt: Long,
    val isSynced: Boolean,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
)
