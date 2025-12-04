package com.team.notify.taskflow.mappers

import com.team.notify.taskflow.data.entities.TaskEntity
import com.team.notify.taskflow.model.Task
import com.team.notify.taskflow.model.TaskStatus
import java.util.Date
import kotlin.toString

fun TaskEntity.toUiModel(): Task {
    val status = try {
        TaskStatus.valueOf(this.status.toString())
    } catch (e: Exception) {
        TaskStatus.TODO
    }
    val dueDate = this.deadline?.let { Date(it) } ?: Date(0)
    return Task(
        id = this.id,
        title = this.title,
        description = this.description ?: "",
        status = status,
        dueDate = dueDate
    )
}

fun Task.toEntity(id: String, spaceId: String, updatedAt: Long): TaskEntity {
    return TaskEntity(
        id = id,
        spaceId = spaceId,
        title = this.title,
        description = this.description,
        status = this.status.name,
        deadline = this.dueDate.time,
        isCompleted = this.status == TaskStatus.DONE,
        updatedAt = updatedAt
    )
}
