package com.team.notify.work

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object DueReminderScheduler {
    fun schedule(context: Context, taskId: String, taskTitle: String, dueAtMillis: Long) {
        val now = System.currentTimeMillis()
        val delay = (dueAtMillis - now).coerceAtLeast(0L)

        val data = Data.Builder()
            .putString(DueReminderWorker.KEY_TASK_ID, taskId)
            .putString(DueReminderWorker.KEY_TASK_TITLE, taskTitle)
            .build()

        val request = OneTimeWorkRequestBuilder<DueReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(uniqueName(taskId), ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(context: Context, taskId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueName(taskId))
    }

    private fun uniqueName(taskId: String) = "due_reminder_$taskId"
}
