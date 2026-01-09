package com.team.notify.taskflow.mappers

import com.team.notify.taskflow.data.entities.TaskEntity
import com.team.notify.taskflow.model.Task
import com.team.notify.taskflow.model.TaskStatus
import java.util.Date

fun TaskEntity.toUiModel(): Task {
    val dueDate = this.deadline?.let { Date(it) } ?: Date(0)
    return Task(
        id = this.id,
        title = this.title,
        description = this.description ?: "",
        status = this.status,
        dueDate = dueDate
    )
}

fun Task.toEntity(id: String, spaceId: String, updatedAt: Long): TaskEntity {
    return TaskEntity(
        id = id,
        spaceId = spaceId,
        title = this.title,
        description = this.description,
        status = this.status,
        deadline = this.dueDate.time,
        isCompleted = this.status == TaskStatus.DONE,
        updatedAt = updatedAt
    )
}
