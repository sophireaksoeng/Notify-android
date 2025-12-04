package com.team.notify.taskflow.sync

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object SyncScheduler {

    fun schedule(context: Context, spaceId: String) {
        val push = PeriodicWorkRequestBuilder<SyncPushWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .addTag("sync-push")
            .build()

        val pull = PeriodicWorkRequestBuilder<SyncPullWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
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
}
