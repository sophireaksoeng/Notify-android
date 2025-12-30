package com.team.notify.taskflow.data.repository.room

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.team.notify.taskflow.data.dao.TaskDao
import com.team.notify.taskflow.data.entities.TaskEntity
import com.team.notify.taskflow.data.reminders.ReminderScheduler
import com.team.notify.taskflow.data.repository.interfaces.TaskRepository
import com.team.notify.taskflow.data.sync.SyncPullWorker
import com.team.notify.taskflow.data.sync.SyncPushWorker
import com.team.notify.taskflow.model.TaskStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class RoomTaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    @ApplicationContext private val context: Context
) : TaskRepository {

    override fun getTasksForSpace(spaceId: String): Flow<List<TaskEntity>> =
        taskDao.getTasksForSpace(spaceId)

    override fun getTasks(spaceId: String): Flow<List<TaskEntity>> =
        taskDao.getTasksForSpace(spaceId)

    override fun tasks(spaceId: String, query: String): Flow<List<TaskEntity>> =
        if (query.isBlank()) taskDao.getTasksForSpace(spaceId) else taskDao.searchTasks(spaceId, query)

    override fun searchTasks(query: String): Flow<List<TaskEntity>> {
        return flowOf(emptyList())
    }

    override fun getTaskById(id: String): Flow<TaskEntity?> =
        taskDao.getTaskById(id)

    override fun task(id: String): Flow<TaskEntity?> =
        taskDao.getTaskById(id)

    override suspend fun insert(task: TaskEntity) {
        upsert(task)
    }

    override suspend fun upsert(task: TaskEntity) {
        val now = System.currentTimeMillis()
        val normalized = task.copy(
            isCompleted = task.isCompleted || task.status == TaskStatus.DONE,
            updatedAt = now
        )
        taskDao.upsert(normalized)
        scheduleReminder(normalized)
    }

    override suspend fun delete(id: String) {
        deleteById(id)
    }

    override suspend fun deleteById(id: String) {
        ReminderScheduler.cancelReminder(context, id)
        taskDao.deleteById(id)
    }

    override suspend fun updateStatus(taskId: String, status: String) {
        val current = taskDao.getTaskByIdOnce(taskId) ?: return
        val parsedStatus = try { TaskStatus.valueOf(status) } catch (_: Exception) { TaskStatus.TODO }

        val updated = current.copy(
            status = parsedStatus,
            isCompleted = parsedStatus == TaskStatus.DONE,
            updatedAt = System.currentTimeMillis()
        )
        taskDao.upsert(updated)
        scheduleReminder(updated)
    }

    override suspend fun pullRemoteChanges(spaceId: String) {
        WorkManager.getInstance(context)
            .enqueue(OneTimeWorkRequestBuilder<SyncPullWorker>().build())
    }

    override suspend fun pushPendingOperations() {
        WorkManager.getInstance(context)
            .enqueue(OneTimeWorkRequestBuilder<SyncPushWorker>().build())
    }

    override suspend fun initialSync(spaceId: String) {
        pullRemoteChanges(spaceId)
        pushPendingOperations()
    }

    override fun listenToRemote(spaceId: String) {
    }

    override fun startRealtimeListener(spaceId: String) = listenToRemote(spaceId)
    override fun startRealtimeSync(spaceId: String) = listenToRemote(spaceId)

    private fun scheduleReminder(task: TaskEntity) {
        val due = task.deadline
        if (due != null && !task.isCompleted && due > System.currentTimeMillis()) {
            ReminderScheduler.scheduleReminder(context, task.id, due)
        } else {
            ReminderScheduler.cancelReminder(context, task.id)
        }
    }
}
