package com.team.notify.taskflow.mappers

import com.team.notify.taskflow.data.entities.TaskEntity
import com.team.notify.taskflow.model.Task
import com.team.notify.taskflow.model.TaskStatus
import java.util.Date

fun TaskEntity.toUiModel(): Task {
    val status = try {
        TaskStatus.valueOf(this.status)
    } catch (e: Exception) {
        TaskStatus.TODO
    }
    val dueDate = this.dueAt?.let { Date(it) } ?: Date(0)
    return Task(
        id = this.id,
        title = this.title,
        description = this.description ?: "",
        status = status,
        dueDate = dueDate
    )
}

fun Task.toEntity(id: String, spaceId: String, createdAt: Long, updatedAt: Long): TaskEntity {
    return TaskEntity(
        id = id,
        spaceId = spaceId,
        title = this.title,
        description = this.description,
        status = this.status.name,
        assigneeId = null,
        labelsCsv = null,
        dueAt = this.dueDate.time,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
