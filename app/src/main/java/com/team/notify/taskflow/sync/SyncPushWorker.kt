package com.team.notify.taskflow.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.google.firebase.firestore.FirebaseFirestore
import com.team.notify.taskflow.data.dao.OpQueueDao
import com.team.notify.taskflow.data.dao.TaskDao
import com.team.notify.taskflow.data.entities.OperationEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncPushWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val firestore: FirebaseFirestore,
    private val opDao: OpQueueDao,
    private val taskDao: TaskDao,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val operations = opDao.getAllOperations().firstOrNull() ?: emptyList()

        for (op in operations) {
            try {
                pushOperation(op)
                opDao.deleteById(op.id)
            } catch (e: Exception) {
                return Result.retry()
            }
        }
        return Result.success()
    }

    private suspend fun pushOperation(op: OperationEntity) {
        when (op.entityType) {
            "TASK" -> {
                val task = taskDao.getTaskByIdOnce(op.entityId) ?: return
                firestore.collection("tasks").document(task.id).set(task).await()
            }
        }
    }
}
