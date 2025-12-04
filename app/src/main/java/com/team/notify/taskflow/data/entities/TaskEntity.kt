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
    val deadline: Long?,
    val isCompleted: Boolean,
    val updatedAt: Long = 0L
)
