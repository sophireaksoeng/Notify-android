package com.team.notify.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.team.notify.data.local.dao.TaskDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RescheduleDueRemindersWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val taskDao: TaskDao,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val now = System.currentTimeMillis()
        val tasks = taskDao.getUpcomingDueTasks(now)

        for (task in tasks) {
            val dueAt = task.dueDate ?: continue
            DueReminderScheduler.schedule(
                context = applicationContext,
                taskId = task.id,
                taskTitle = task.title,
                dueAtMillis = dueAt,
            )
        }

        return Result.success()
    }
}
