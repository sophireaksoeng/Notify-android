package com.team.notify.taskflow.presentation.tasks

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay
import android.util.Log

class ReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        Log.d("ReminderWorker", "🔔 Reminder triggered!")
        delay(1000)
        return Result.success()
    }
}