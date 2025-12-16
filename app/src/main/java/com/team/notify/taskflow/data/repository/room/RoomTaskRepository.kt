package com.team.notify.taskflow.data.repository.room

import com.team.notify.taskflow.data.dao.OpQueueDao
import com.team.notify.taskflow.data.dao.TaskDao
import com.team.notify.taskflow.data.entities.OperationEntity
import com.team.notify.taskflow.data.entities.TaskEntity
import com.team.notify.taskflow.data.repository.interfaces.TaskRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomTaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val opQueueDao: OpQueueDao,
    private val gson: Gson
) : TaskRepository {

    override fun getTasksForSpace(spaceId: String): Flow<List<TaskEntity>> =
        taskDao.getTasksForSpace(spaceId)

    override fun getTaskById(id: String): Flow<TaskEntity?> =
        taskDao.getTaskById(id)

    override fun searchTasks(query: String): Flow<List<TaskEntity>> =
        taskDao.searchTasks(query)

    override suspend fun insert(task: TaskEntity) {
        val toSave = task.copy(updatedAt = System.currentTimeMillis())
        taskDao.insert(toSave)

        val op = OperationEntity(
            id = UUID.randomUUID().toString(),
            entityId = toSave.id,
            entityType = "TASK",
            operation = "UPSERT",
            payloadJson = gson.toJson(toSave),
            timestamp = System.currentTimeMillis()
        )
        opQueueDao.insert(op)
    }

    override suspend fun deleteById(id: String) = withContext(Dispatchers.IO) {
        taskDao.deleteById(id)
        val op = OperationEntity(
            id = UUID.randomUUID().toString(),
            entityId = id,
            entityType = "TASK",
            operation = "DELETE",
            payloadJson = "{}",
            timestamp = System.currentTimeMillis()
        )
        opQueueDao.insert(op)
    }

    override suspend fun updateStatus(taskId: String, status: String) = withContext(Dispatchers.IO) {
        val existing = taskDao.getTaskByIdOnce(taskId)
        if (existing != null) {
            val updated = existing.copy(
                status = status,
                isCompleted = status == "DONE",
                updatedAt = System.currentTimeMillis()
            )
            taskDao.insert(updated)

            val op = OperationEntity(
                id = UUID.randomUUID().toString(),
                entityId = updated.id,
                entityType = "TASK",
                operation = "UPSERT",
                payloadJson = gson.toJson(updated),
                timestamp = System.currentTimeMillis()
            )
            opQueueDao.insert(op)
        }
    }

    override suspend fun pullRemoteChanges(spaceId: String) {
    }

    override suspend fun pushPendingOperations() {
    }

    override suspend fun initialSync(spaceId: String) {
    }

    override fun getTasks(spaceId: String): Flow<List<TaskEntity>> =
        taskDao.getTasksForSpace(spaceId)

    override suspend fun upsert(task: TaskEntity) {
        insert(task)
    }

    override fun listenToRemote(spaceId: String) {
    }

    override fun startRealtimeListener(spaceId: String) {
    }

    override fun startRealtimeSync(spaceId: String) {
    }
}
