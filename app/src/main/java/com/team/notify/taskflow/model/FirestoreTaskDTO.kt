package com.team.notify.taskflow.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.team.notify.taskflow.data.entities.TaskEntity

@IgnoreExtraProperties
data class FirestoreTaskDTO(
    val id: String = "",
    val spaceId: String = "",
    val pageId: String? = null,
    val title: String = "",
    val description: String? = null,
    val status: String = TaskStatus.TODO.name,
    val deadline: Long? = null,
    val isCompleted: Boolean = false,
    val assigneeId: String? = null,
    val labels: List<String> = emptyList(),
    val updatedAt: Long = 0L
) {
    fun toEntity(): TaskEntity {
        val parsedStatus = try {
            TaskStatus.valueOf(status)
        } catch (_: Exception) {
            TaskStatus.TODO
        }

        return TaskEntity(
            id = id,
            spaceId = spaceId,
            pageId = pageId,
            title = title,
            description = description,
            status = parsedStatus,
            deadline = deadline,
            isCompleted = isCompleted,
            assigneeId = assigneeId,
            labels = labels,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromEntity(entity: TaskEntity): FirestoreTaskDTO {
            return FirestoreTaskDTO(
                id = entity.id,
                spaceId = entity.spaceId,
                pageId = entity.pageId,
                title = entity.title,
                description = entity.description,
                status = entity.status.name,
                deadline = entity.deadline,
                isCompleted = entity.isCompleted,
                assigneeId = entity.assigneeId,
                labels = entity.labels,
                updatedAt = entity.updatedAt
            )
        }
    }
}