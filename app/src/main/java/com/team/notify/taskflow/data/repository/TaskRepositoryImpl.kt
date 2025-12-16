package com.team.notify.taskflow.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.team.notify.taskflow.data.dao.TaskDao
import com.team.notify.taskflow.data.entities.TaskEntity
import com.team.notify.taskflow.model.FirestoreTaskDTO
import com.team.notify.taskflow.data.repository.interfaces.OpQueueRepository
import com.team.notify.taskflow.data.repository.interfaces.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val firestore: FirebaseFirestore,
    private val opQueue: OpQueueRepository
) : TaskRepository {

    private var listener: ListenerRegistration? = null
    private val tasks = MutableStateFlow<List<TaskEntity>>(emptyList())

    override fun getTasks(spaceId: String): Flow<List<TaskEntity>> =
        tasks.map { list -> list.filter { it.spaceId == spaceId } }
    override fun listenToRemote(spaceId: String) {
        listener?.remove()
        listener = firestore.collection("spaces")
            .document(spaceId)
            .collection("tasks")
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null) return@addSnapshotListener
                val updates = snap.documents.mapNotNull { doc ->
                    doc.toObject(FirestoreTaskDTO::class.java)?.toEntity()
                }
                CoroutineScope(Dispatchers.IO).launch {
                    taskDao.upsertAll(updates)
                }
            }
    }

    override suspend fun upsert(task: TaskEntity) {
        taskDao.upsert(task)
        opQueue.enqueueUpsert(
            "TASK",
            task.id,
            mapOf(
                "id" to task.id,
                "spaceId" to task.spaceId,
                "title" to task.title,
                "description" to task.description,
                "deadline" to task.deadline,
                "isCompleted" to task.isCompleted,
                "updatedAt" to System.currentTimeMillis()
            )
        )
    }

    override fun startRealtimeListener(spaceId: String) {
        listener?.remove()
        listener = firestore.collection("spaces")
            .document(spaceId)
            .collection("tasks")
            .addSnapshotListener { snap, error ->
                if (snap == null || error != null) return@addSnapshotListener
                val remote = snap.documents.mapNotNull { doc ->
                    doc.toObject(FirestoreTaskDTO::class.java)?.toEntity()
                }
                CoroutineScope(Dispatchers.IO).launch {
                    taskDao.upsertAll(remote)
                }
            }
    }

    override fun startRealtimeSync(spaceId: String) {
        listenToRemote(spaceId)
    }

    override fun getTasksForSpace(spaceId: String): Flow<List<TaskEntity>> =
        taskDao.getTasks(spaceId)

    override fun getTaskById(id: String): Flow<TaskEntity?> =
        taskDao.getTaskById(id)

    override fun searchTasks(query: String): Flow<List<TaskEntity>> =
        taskDao.searchTasks(query)

    override suspend fun insert(task: TaskEntity) {
        upsert(task)
    }

    override suspend fun deleteById(id: String) {
        taskDao.deleteById(id)

        opQueue.enqueueUpsert(
            "TASK_DELETE",
            id,
            mapOf(
                "id" to id,
                "deletedAt" to System.currentTimeMillis()
            )
        )
    }

    override suspend fun updateStatus(taskId: String, status: String) {
        taskDao.updateStatus(taskId, status)

        opQueue.enqueueUpsert(
            "TASK",
            taskId,
            mapOf(
                "id" to taskId,
                "status" to status,
                "updatedAt" to System.currentTimeMillis()
            )
        )
    }

    override suspend fun pullRemoteChanges(spaceId: String) {
        val snap = firestore.collection("spaces")
            .document(spaceId)
            .collection("tasks")
            .get()
            .await()

        val remote = snap.documents.mapNotNull { doc ->
            doc.toObject(FirestoreTaskDTO::class.java)?.toEntity()
        }

        taskDao.upsertAll(remote)
    }

    override suspend fun pushPendingOperations() {
        opQueue.pushPendingOperations()
    }

    override suspend fun initialSync(spaceId: String) {
        pullRemoteChanges(spaceId)
        pushPendingOperations()
        startRealtimeSync(spaceId)
    }
}
