package com.team.notify.taskflow.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.team.notify.taskflow.data.dao.OpQueueDao
import com.google.gson.Gson
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import android.util.Log

@HiltWorker
class SyncPushWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val opDao: OpQueueDao,
    private val gson: Gson,
    private val firestore: FirebaseFirestore
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val ops = opDao.getAllOperations().first()

            for (op in ops) {
                try {
                    val payload = gson.fromJson(op.payloadJson, Map::class.java)
                    push(op.entityType, op.entityId, op.opType, payload)
                    opDao.deleteById(op.id)
                } catch (e: Exception) {
                    Log.e("SyncPushWorker", "Failed op: ${e.message}")
                    return Result.retry()
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("SyncPushWorker", "doWork failed: ${e.message}")
            Result.retry()
        }
    }

    private suspend fun push(type: String, id: String, opType: String, payload: Map<*, *>) {
        val spaceId = payload["spaceId"] as? String ?: return
        val ref = firestore.collection("spaces")
            .document(spaceId)
            .collection("tasks")
            .document(id)

        when (opType) {
            "UPSERT" -> ref.set(payload).await()
            "DELETE" -> ref.delete().await()
        }
    }
}
