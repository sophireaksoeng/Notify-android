package com.team.notify.taskflow.data.reminders

import android.content.Context
import androidx.work.*
import com.team.notify.taskflow.data.dao.TaskDao
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    fun scheduleReminder(context: Context, taskId: String, dueMillis: Long) {
        val delay = dueMillis - System.currentTimeMillis()
        if (delay <= 0) return

        val work = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("taskId" to taskId))
            .addTag("reminder-$taskId")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "reminder-$taskId",
            ExistingWorkPolicy.REPLACE,
            work
        )
    }

    fun cancelReminder(context: Context, taskId: String) {
        WorkManager.getInstance(context).cancelUniqueWork("reminder-$taskId")
    }

    suspend fun rescheduleAll(context: Context, taskDao: TaskDao) {
        val tasks = taskDao.getAllTasksDebug()
        for (task in tasks) {
            task.deadline?.let {
                if (!task.isCompleted) {
                    scheduleReminder(context, task.id, it)
                }
            }
        }
    }
}
