package com.team.notify.taskflow.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.team.notify.taskflow.data.dao.OpQueueDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

@HiltWorker
class SyncPushWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val firestore: FirebaseFirestore,
    private val opDao: OpQueueDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val ops = opDao.loadBatch(25)

        try {
            for (op in ops) {
                val docRef = firestore
                    .collection("spaces")
                    .document(op.spaceId)
                    .collection(op.entityType)
                    .document(op.entityId)

                when (op.operation) {
                    "UPSERT" -> {
                        val data = JSONObject(op.payloadJson)
                        docRef.set(data.toMap()).awaitCompat()
                    }
                    "DELETE" -> {
                        docRef.delete().awaitCompat()
                    }
                }

                opDao.deleteById(op.id)
            }

            opDao.dropTooManyFailures(5)
            Result.success()
        } catch (e: Exception) {
            ops.forEach {
                opDao.markFailed(it.id, e.message ?: "Unknown error")
            }
            Result.retry()
        }
    }
}

private fun JSONObject.toMap(): Map<String, Any?> =
    keys().asSequence().associateWith { get(it) }
