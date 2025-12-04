package com.team.notify.taskflow.model

import com.team.notify.taskflow.data.entities.TaskEntity
import com.team.notify.taskflow.model.TaskStatus

data class FirestoreTaskDTO(
    val id: String = "",
    val spaceId: String = "",
    val title: String = "",
    val description: String = "",
    val deadline: Long? = null,
    val isCompleted: Boolean = false,
    val updatedAt: Long = 0
) {
    fun toEntity() = TaskEntity(
        id = id,
        spaceId = spaceId,
        title = title,
        description = description,
        deadline = deadline,
        isCompleted = isCompleted,
        status = if (isCompleted) TaskStatus.DONE.name else TaskStatus.DOING.name,
        updatedAt = updatedAt
    )
}
