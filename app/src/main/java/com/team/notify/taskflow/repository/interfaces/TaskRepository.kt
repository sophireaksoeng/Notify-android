package com.team.notify.taskflow.repository.interfaces

import com.team.notify.taskflow.data.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasksForSpace(spaceId: String): Flow<List<TaskEntity>>
    fun getTaskById(id: String): Flow<TaskEntity?>
    fun searchTasks(query: String): Flow<List<TaskEntity>>
    suspend fun insert(task: TaskEntity)
    suspend fun deleteById(id: String)
}
