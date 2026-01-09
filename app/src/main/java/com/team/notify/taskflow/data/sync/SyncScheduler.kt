package com.team.notify.taskflow.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncScheduler @Inject constructor() {

    fun scheduleNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<SyncPullWorker>()
            .addTag(TAG_IMMEDIATE)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                UNIQUE_IMMEDIATE,
                ExistingWorkPolicy.REPLACE,
                request
            )
    }

    companion object {
        private const val UNIQUE_IMMEDIATE = "sync_now"
        private const val UNIQUE_PERIODIC = "sync_periodic"
        private const val TAG_IMMEDIATE = "SYNC_NOW"
        private const val TAG_PERIODIC = "SYNC_PERIODIC"
    }

    private val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()

    fun schedulePeriodic(context: Context) {
        val push = PeriodicWorkRequestBuilder<SyncPushWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        val pull = PeriodicWorkRequestBuilder<SyncPullWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "sync-push",
            ExistingPeriodicWorkPolicy.KEEP,
            push
        )
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "sync-pull",
            ExistingPeriodicWorkPolicy.KEEP,
            pull
        )
    }

    fun runNow(context: Context) {
        val pushNow = OneTimeWorkRequestBuilder<SyncPushWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        val pullNow = OneTimeWorkRequestBuilder<SyncPullWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "sync-push-now",
            ExistingWorkPolicy.REPLACE,
            pushNow
        )
        WorkManager.getInstance(context).enqueueUniqueWork(
            "sync-pull-now",
            ExistingWorkPolicy.REPLACE,
            pullNow
        )
    }
}
