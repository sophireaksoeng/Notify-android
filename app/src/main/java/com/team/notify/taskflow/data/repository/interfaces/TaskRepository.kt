package com.team.notify.taskflow.data.repository.interfaces

import com.team.notify.taskflow.data.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasksForSpace(spaceId: String): Flow<List<TaskEntity>>
    fun getTaskById(id: String): Flow<TaskEntity?>
    fun searchTasks(query: String): Flow<List<TaskEntity>>
    suspend fun insert(task: TaskEntity)
    suspend fun deleteById(id: String)
    suspend fun updateStatus(taskId: String, status: String)
    suspend fun pullRemoteChanges(spaceId: String)
    suspend fun pushPendingOperations()
    suspend fun initialSync(spaceId: String)
    fun getTasks(spaceId: String): Flow<List<TaskEntity>>
    fun listenToRemote(spaceId: String)
    suspend fun upsert(task: TaskEntity)
    fun startRealtimeListener(spaceId: String)
    fun startRealtimeSync(spaceId: String)
    fun tasks(spaceId: String, query: String): Flow<List<TaskEntity>>
    fun task(id: String): Flow<TaskEntity?>
    suspend fun delete(id: String)
}
