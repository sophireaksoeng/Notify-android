package com.team.notify.taskflow.reminders

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    fun scheduleReminder(context: Context, taskId: String, dueMillis: Long) {
        val delay = dueMillis - System.currentTimeMillis()
        if (delay <= 0) return

        val work = OneTimeWorkRequestBuilder<ReminderWorker>()
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

    suspend fun rescheduleAll(context: Context, taskDao: com.team.notify.taskflow.data.dao.TaskDao) {
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
