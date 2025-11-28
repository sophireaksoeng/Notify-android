package com.team.notify.taskflow.sync

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object SyncScheduler {
    fun schedule(context: Context) {
        val req = PeriodicWorkRequestBuilder<SyncPushWorker>(15, TimeUnit.MINUTES)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30,
                TimeUnit.SECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "sync_push",
            ExistingPeriodicWorkPolicy.KEEP,
            req
        )
    }
}
