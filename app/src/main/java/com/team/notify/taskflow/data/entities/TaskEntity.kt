package com.team.notify.taskflow.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val spaceId: String,
    val title: String,
    val description: String?,
    val status: String,
    val assigneeId: String?,
    val labelsCsv: String?,
    val dueAt: Long?,
    val createdAt: Long,
    val updatedAt: Long
)
