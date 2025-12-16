package com.team.notify.taskflow.data.reminders

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.team.notify.R
import com.team.notify.taskflow.data.dao.TaskDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class TaskReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskDao: TaskDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getString("taskId") ?: return Result.failure()
        val task = taskDao.getTaskByIdOnce(taskId) ?: return Result.success()

        val notification = NotificationCompat.Builder(applicationContext, "reminders_channel")
            .setContentTitle("Task Reminder")
            .setContentText(task.title)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        if (NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
            try {
                NotificationManagerCompat.from(applicationContext)
                    .notify(taskId.hashCode(), notification)
            } catch (e: SecurityException) {
                return Result.failure()
            }
        }

        return Result.success()
    }
}
