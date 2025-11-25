package com.team.notify.taskflow.presentation.tasks

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val taskId = inputData.getString("taskId")
        Log.d("ReminderWorker", "Reminder triggered for taskId=$taskId")

        if (Build.VERSION.SDK_INT >= 33) {
            val granted = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                Log.w("ReminderWorker", "POST_NOTIFICATIONS not granted; skipping notification")
                return Result.success()
            }
        }

        try {
            val builder = NotificationCompat.Builder(applicationContext, "reminders_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Task reminder")
                .setContentText("Reminder for task: $taskId")
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            val manager = NotificationManagerCompat.from(applicationContext)
            if (manager.areNotificationsEnabled()) {
                manager.notify(taskId?.hashCode() ?: 0, builder.build())
            } else {
                Log.w("ReminderWorker", "Notifications disabled; skipping")
            }
        } catch (e: Exception) {
            Log.w("ReminderWorker", "Failed to show notification: ${e.message}")
        }

        return Result.success()
    }
}
