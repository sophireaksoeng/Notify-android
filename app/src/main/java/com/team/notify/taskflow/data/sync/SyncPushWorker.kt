package com.team.notify.taskflow.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.team.notify.taskflow.data.dao.OpQueueDao
import com.team.notify.taskflow.data.dao.PageDao
import com.team.notify.taskflow.data.dao.TaskDao
import com.team.notify.taskflow.data.entities.OperationEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await

@HiltWorker
class SyncPushWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val firestore: FirebaseFirestore,
    private val opDao: OpQueueDao,
    private val taskDao: TaskDao,
    private val pageDao: PageDao,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val operations: List<OperationEntity> =
            opDao.getAllOperations().firstOrNull() ?: emptyList()

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
            "TASK" -> pushTask(op)
            "PAGE" -> pushPage(op)
            else -> {
            }
        }
    }

    private suspend fun pushTask(op: OperationEntity) {
        val task = taskDao.getTaskByIdOnce(op.entityId) ?: return
        firestore
            .collection("tasks")
            .document(task.id)
            .set(task)
            .await()
    }

    private suspend fun pushPage(op: OperationEntity) {
        val page = pageDao.getPageByIdOnce(op.entityId) ?: return
        firestore
            .collection("pages")
            .document(page.id)
            .set(page)
            .await()
    }
}