package com.team.notify.taskflow.data.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.team.notify.taskflow.data.dao.ConflictDao
import com.team.notify.taskflow.data.dao.PageDao
import com.team.notify.taskflow.data.dao.TaskDao
import com.team.notify.taskflow.data.entities.ConflictEntity
import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.taskflow.data.entities.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

class RealtimeSyncManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val pageDao: PageDao,
    private val taskDao: TaskDao,
    private val conflictDao: ConflictDao
) {

    private val listeners = mutableListOf<ListenerRegistration>()

    fun start(spaceId: String) {
        listenPages(spaceId)
        listenTasks(spaceId)
    }

    fun stop() {
        listeners.forEach { it.remove() }
        listeners.clear()
    }

    private fun listenPages(spaceId: String) {
        val reg = firestore.collection("pages")
            .whereEqualTo("spaceId", spaceId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                snapshot.documentChanges.forEach { change ->
                    val page = change.document.toObject(PageEntity::class.java)
                    handleRemotePage(page)
                }
            }
        listeners.add(reg)
    }

    private fun listenTasks(spaceId: String) {
        val reg = firestore.collection("tasks")
            .whereEqualTo("spaceId", spaceId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                snapshot.documentChanges.forEach { change ->
                    val task = change.document.toObject(TaskEntity::class.java)
                    handleRemoteTask(task)
                }
            }
        listeners.add(reg)
    }

    private fun handleRemotePage(remote: PageEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            pageDao.upsert(remote)
        }
    }

    private fun handleRemoteTask(remote: TaskEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            taskDao.upsert(remote)
        }
    }
    private suspend fun detectConflict(
        localVersion: Int,
        remoteVersion: Int,
        entityId: String,
        type: String
    ) {
        if (remoteVersion == localVersion) return

        conflictDao.insert(
            ConflictEntity(
                id = UUID.randomUUID().toString(),
                entityType = type,
                entityId = entityId,
                localVersion = localVersion,
                remoteVersion = remoteVersion,
                timestamp = System.currentTimeMillis()
            )
        )
    }

}
