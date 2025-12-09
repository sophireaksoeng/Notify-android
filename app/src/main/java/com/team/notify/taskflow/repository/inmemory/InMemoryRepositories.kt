package com.team.notify.taskflow.repository.inmemory

import com.team.notify.taskflow.data.dao.OpQueueDao
import com.team.notify.taskflow.data.entities.OperationEntity
import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.taskflow.data.entities.SpaceEntity
import com.team.notify.taskflow.data.entities.TaskEntity
import com.team.notify.taskflow.data.entities.PageHistoryEntity
import com.team.notify.taskflow.repository.interfaces.OpQueueRepository
import com.team.notify.taskflow.repository.interfaces.PageRepository
import com.team.notify.taskflow.repository.interfaces.SpaceRepository
import com.team.notify.taskflow.repository.interfaces.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.collections.remove
import kotlin.text.get
import kotlin.text.insert
import kotlin.text.set
import kotlin.toString

const val DEFAULT_SPACE_ID = "DEFAULT_SPACE_ID"

class InMemorySpaceRepository : SpaceRepository {
    private val store = ConcurrentHashMap<String, SpaceEntity>()
    private val flow = MutableStateFlow<List<SpaceEntity>>(emptyList())

    init {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val space = SpaceEntity(
            id = id,
            name = "Team Space",
            description = "Default space created in-memory",
            createdAt = now,
            updatedAt = now
        )
        store[id] = space
        flow.value = listOf(space)
    }

    override fun getSpaces(): Flow<List<SpaceEntity>> = flow

    override fun getSpaceById(id: String): Flow<SpaceEntity?> =
        flow.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun insert(space: SpaceEntity) {
        store[space.id] = space
        flow.value = store.values.sortedBy { it.name }
    }

    override suspend fun deleteById(id: String) {
        store.remove(id)
        flow.value = store.values.sortedBy { it.name }
    }
}

class InMemoryPageRepository : PageRepository {
    private val store = ConcurrentHashMap<String, PageEntity>()
    private val flow = MutableStateFlow<List<PageEntity>>(emptyList())
    private val historyStore = ConcurrentHashMap<String, MutableList<PageHistoryEntity>>()
    private val historyFlow = MutableStateFlow<List<PageHistoryEntity>>(emptyList())

    override fun getPagesForSpace(spaceId: String): Flow<List<PageEntity>> =
        flow.map { list -> list.filter { it.spaceId == spaceId } }

    override fun getPageById(id: String): Flow<PageEntity?> =
        flow.map { list -> list.firstOrNull { it.id == id } }

    override fun searchPages(query: String): Flow<List<PageEntity>> =
        flow.map { list ->
            val trimmed = query.trim()
            if (trimmed.isEmpty()) {
                list.sortedBy { it.title }
            } else {
                list.filter {
                    it.title.contains(trimmed, ignoreCase = true) ||
                            (it.content?.contains(trimmed, ignoreCase = true) == true)
                }.sortedBy { it.title }
            }
        }

    override suspend fun insertHistory(history: PageHistoryEntity) {
        val listForPage = historyStore.getOrPut(history.pageId) { mutableListOf() }
        listForPage.add(history)

        historyFlow.value = historyStore.values.flatten()
    }

    override fun getHistoryForPage(pageId: String): Flow<List<PageHistoryEntity>> =
        historyFlow.map { all -> all.filter { it.pageId == pageId } }

    override suspend fun insert(page: PageEntity) {
        store[page.id] = page
        flow.value = store.values.sortedBy { it.title }
    }

    override suspend fun deleteById(id: String) {
        store.remove(id)
        flow.value = store.values.sortedBy { it.title }
    }

    override suspend fun deleteHistoryForPage(pageId: String) {
        historyStore.remove(pageId)
        historyFlow.value = historyStore.values.flatten()
    }

    override suspend fun pullRemoteChanges(spaceId: String) { }

    override suspend fun pushPendingOperations() { }

    override suspend fun initialSync(spaceId: String) { }
}

class InMemoryTaskRepository(
    private val spaceId: String = DEFAULT_SPACE_ID
) : TaskRepository {
    private val store = ConcurrentHashMap<String, TaskEntity>()
    private val flow = MutableStateFlow<List<TaskEntity>>(emptyList())

    init {
        val now = System.currentTimeMillis()

        fun seed(title: String, status: String, hoursAhead: Int, isCompleted: Boolean): TaskEntity {
            return TaskEntity(
                id = UUID.randomUUID().toString(),
                spaceId = spaceId,
                title = title,
                description = "$title description",
                deadline = now + hoursAhead * 60 * 60 * 1000L,
                isCompleted = isCompleted,
                status = status,
                updatedAt = now
            )
        }

        listOf(
            seed("Fix login bug", "TODO", 6, isCompleted = false),
            seed("UI polish", "DOING", 24, isCompleted = false),
            seed("Write reminder worker", "DONE", 48, isCompleted = true)
        ).forEach { store[it.id] = it }
        flow.value = store.values.toList()
    }

    override fun getTasksForSpace(spaceId: String): Flow<List<TaskEntity>> =
        flow.map { list ->
            list.filter { it.spaceId == spaceId }
                .sortedBy { it.deadline ?: Long.MAX_VALUE }
        }

    override fun getTasks(spaceId: String): Flow<List<TaskEntity>> =
        getTasksForSpace(spaceId)

    override fun getTaskById(id: String): Flow<TaskEntity?> =
        flow.map { list -> list.firstOrNull { it.id == id } }

    override fun searchTasks(query: String): Flow<List<TaskEntity>> =
        flow.map { list ->
            val trimmed = query.trim()
            list.filter { it.spaceId == spaceId }
                .filter {
                    if (trimmed.isEmpty()) true
                    else it.title.contains(trimmed, ignoreCase = true) ||
                            (it.description?.contains(trimmed, ignoreCase = true) == true)
                }
                .sortedBy { it.deadline ?: Long.MAX_VALUE }
        }

    override suspend fun insert(task: TaskEntity) {
        store[task.id] = task
        flow.value = store.values.toList()
    }

    override suspend fun upsert(task: TaskEntity) {
        insert(task)
    }

    override suspend fun deleteById(id: String) {
        store.remove(id)
        flow.value = store.values.toList()
    }

    override suspend fun updateStatus(taskId: String, status: String) {
        val existing = store[taskId] ?: return
        val updated = existing.copy(
            status = status,
            isCompleted = status.equals("DONE", ignoreCase = true),
            updatedAt = System.currentTimeMillis()
        )
        store[taskId] = updated
        flow.value = store.values.toList()
    }

    override fun listenToRemote(spaceId: String) {
    }

    override fun startRealtimeListener(spaceId: String) {
    }

    override fun startRealtimeSync(spaceId: String) {
    }

    override suspend fun pullRemoteChanges(spaceId: String) {
    }

    override suspend fun pushPendingOperations() {
    }

    override suspend fun initialSync(spaceId: String) {
    }
}

class OpQueueRepositoryImpl @Inject constructor(
    private val dao: OpQueueDao
) : OpQueueRepository {

    override fun getPendingOperations(): Flow<List<OperationEntity>> {
        return dao.getAllOperations()
    }

    override suspend fun addOperation(op: OperationEntity) {
        dao.insert(op)
    }

    override suspend fun removeOperation(id: String) {
        dao.deleteById(id)
    }

    override suspend fun clear() {
        dao.clearAll()
    }

    override suspend fun enqueueUpsert(
        type: String,
        id: String,
        payload: Map<String, Any?>
    ) {
        val op = OperationEntity(
            id = UUID.randomUUID().toString(),
            entityId = id,
            entityType = type,
            opType = "UPSERT",
            payloadJson = payload.toString(),
            timestamp = System.currentTimeMillis()
        )
        addOperation(op)
    }

    override suspend fun pushPendingOperations() {
        dao.getAllOperations()
    }
}