package com.team.notify.taskflow.repository.room

import com.team.notify.taskflow.data.dao.OpQueueDao
import com.team.notify.taskflow.data.dao.TaskDao
import com.team.notify.taskflow.data.entities.OperationEntity
import com.team.notify.taskflow.data.entities.TaskEntity
import com.team.notify.taskflow.repository.interfaces.TaskRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

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

    override suspend fun insert(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.insert(task)

        val op = OperationEntity(
            id = UUID.randomUUID().toString(),
            entityId = task.id,
            entityType = "TASK",
            opType = "UPSERT",
            payloadJson = gson.toJson(task),
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
            opType = "DELETE",
            payloadJson = "{}",
            timestamp = System.currentTimeMillis()
        )
        opQueueDao.insert(op)
    }
}
