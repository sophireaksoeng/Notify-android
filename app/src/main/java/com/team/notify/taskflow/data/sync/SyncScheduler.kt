package com.team.notify.taskflow.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncScheduler @Inject constructor() {

    fun schedule(context: Context, spaceId: String) {
        val push = PeriodicWorkRequestBuilder<SyncPushWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag("sync-push")
            .build()

        val pull = PeriodicWorkRequestBuilder<SyncPullWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag("sync-pull")
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

    fun scheduleNow(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val push: OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<SyncPushWorker>().build()
        val pull: OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<SyncPullWorker>().build()

        workManager.enqueueUniqueWork(
            "sync-push-now",
            ExistingWorkPolicy.REPLACE,
            push
        )
        workManager.enqueueUniqueWork(
            "sync-pull-now",
            ExistingWorkPolicy.REPLACE,
            pull
        )
    }
}