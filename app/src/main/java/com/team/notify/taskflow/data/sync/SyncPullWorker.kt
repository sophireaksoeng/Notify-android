package com.team.notify.taskflow.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.team.notify.taskflow.data.dao.PageDao
import com.team.notify.taskflow.data.dao.SpaceDao
import com.team.notify.taskflow.data.dao.SyncStateDao
import com.team.notify.taskflow.data.dao.TaskDao
import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.taskflow.data.entities.SyncStateEntity
import com.team.notify.taskflow.data.entities.TaskEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SyncPullWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val firestore: FirebaseFirestore,
    private val spaceDao: SpaceDao,
    private val taskDao: TaskDao,
    private val pageDao: PageDao,
    private val syncStateDao: SyncStateDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            spaceDao.getAllSpacesOnce().forEach { space ->
                pullSpace(space.id)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun pullSpace(spaceId: String) {
        val lastPulled = syncStateDao.get(spaceId)?.lastPulledAt ?: 0L
        val spaceRef = firestore.collection("spaces").document(spaceId)


        val taskSnap = spaceRef.collection("tasks")
            .whereGreaterThan("updatedAt", lastPulled)
            .get()
            .awaitCompat()

        taskSnap.toObjects(TaskEntity::class.java).forEach { remote ->
            val local = taskDao.getTaskByIdOnce(remote.id)
            if (local == null || remote.updatedAt > local.updatedAt) {
                taskDao.upsert(remote)
            }
        }

        val pageSnap = spaceRef.collection("pages")
            .whereGreaterThan("updatedAt", lastPulled)
            .get()
            .awaitCompat()

        pageSnap.toObjects(PageEntity::class.java).forEach { remote ->
            val local = pageDao.getByIdOnce(remote.id)
            if (local == null || remote.updatedAt > local.updatedAt) {
                pageDao.upsert(remote)
            }
        }

        syncStateDao.upsert(
            SyncStateEntity(spaceId, System.currentTimeMillis())
        )
    }
}
