package com.team.notify.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.team.notify.data.remote.FirestoreSyncer
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncer: FirestoreSyncer
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            syncer.sync()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
