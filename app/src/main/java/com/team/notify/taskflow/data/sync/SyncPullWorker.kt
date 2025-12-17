package com.team.notify.taskflow.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.team.notify.taskflow.data.dao.PageDao
import com.team.notify.taskflow.data.dao.TaskDao
import com.team.notify.taskflow.data.dao.SpaceMemberDao
import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.taskflow.data.entities.TaskEntity
import com.team.notify.taskflow.data.entities.SpaceMemberEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

@HiltWorker
class SyncPullWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val firestore: FirebaseFirestore,
    private val taskDao: TaskDao,
    private val pageDao: PageDao,
    private val spaceMemberDao: SpaceMemberDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            pullAllMembers()

            pullPages()
            val snapshot = firestore.collection("tasks").get().await()
            val remoteTasks: List<TaskEntity> = snapshot.toObjects(TaskEntity::class.java)

            for (remote in remoteTasks) {
                val local = taskDao.getTaskByIdOnce(remote.id)

                val chosen: TaskEntity = if (local == null) {
                    remote
                } else {
                    SyncConflictResolver.resolveTaskConflict(local, remote)
                }

                taskDao.upsert(chosen)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun pullAllMembers() {
        val spacesSnapshot = firestore.collection("spaces").get().await()
        for (spaceDoc in spacesSnapshot.documents) {
            val spaceId = spaceDoc.id
            pullMembers(spaceId)
        }
    }

    private suspend fun pullMembers(spaceId: String) {
        val snapshot = firestore
            .collection("spaces")
            .document(spaceId)
            .collection("members")
            .get()
            .await()

        for (doc in snapshot.documents) {
            spaceMemberDao.upsert(
                SpaceMemberEntity(
                    spaceId = spaceId,
                    userId = doc.id,
                    role = doc.getString("role") ?: "VIEWER"
                )
            )
        }
    }

    private suspend fun pullPages() {
        val snapshot = firestore.collection("pages").get().await()
        val remotePages: List<PageEntity> = snapshot.toObjects(PageEntity::class.java)

        for (remote in remotePages) {
            val local = pageDao.getPageByIdOnce(remote.id)

            val chosen: PageEntity = if (local == null) {
                remote
            } else {
                if (remote.updatedAt > local.updatedAt) remote else local
            }

            pageDao.upsert(chosen)
        }
    }
}
