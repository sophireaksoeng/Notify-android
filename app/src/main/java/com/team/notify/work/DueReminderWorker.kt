package com.team.notify.work

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.team.notify.R
import com.team.notify.data.local.dao.TaskDao
import com.team.notify.data.model.TaskStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DueReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val taskDao: TaskDao,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getString(KEY_TASK_ID) ?: return Result.failure()
        val title = inputData.getString(KEY_TASK_TITLE) ?: "Task due"

        val task = taskDao.getById(taskId) ?: return Result.success()
        if (task.isDeleted) return Result.success()
        if (task.status == TaskStatus.DONE.name) return Result.success()
        if (task.dueDate == null) return Result.success()

        NotificationHelper.ensureChannel(applicationContext)

        val notification = NotificationCompat.Builder(applicationContext, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Due now")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(taskId.hashCode(), notification)

        return Result.success()
    }

    companion object {
        const val KEY_TASK_ID = "taskId"
        const val KEY_TASK_TITLE = "taskTitle"
    }
}
