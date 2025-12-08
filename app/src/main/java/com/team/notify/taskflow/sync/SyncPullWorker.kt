package com.team.notify.taskflow.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.team.notify.taskflow.data.dao.PageDao
import com.team.notify.taskflow.data.dao.TaskDao
import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.taskflow.data.entities.TaskEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await

@HiltWorker
class SyncPullWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val firestore: FirebaseFirestore,
    private val taskDao: TaskDao,
    private val pageDao: PageDao,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            pullPages()

            val snapshot = firestore.collection("tasks").get().await()
            val remoteTasks = snapshot.toObjects(TaskEntity::class.java)

            for (remote in remoteTasks) {
                val local = taskDao.getTaskByIdOnce(remote.id)

                val chosen = if (local == null) {
                    remote
                } else if (remote.updatedAt > local.updatedAt) {
                    remote
                } else {
                    local
                }

                taskDao.upsert(chosen)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun pullPages() {
        val snapshot = firestore.collection("pages").get().await()
        val remotePages = snapshot.toObjects(PageEntity::class.java)

        for (remote in remotePages) {
            val local = pageDao.getPageById(remote.id).firstOrNull()

            val chosen = if (local == null) {
                remote
            } else if (remote.updatedAt > local.updatedAt) {
                remote
            } else {
                local
            }

            pageDao.upsert(chosen)
        }
    }
}
