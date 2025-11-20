package com.team.notify.taskflow.repository.inmemory

import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.taskflow.data.entities.SpaceEntity
import com.team.notify.taskflow.data.entities.TaskEntity
import com.team.notify.taskflow.repository.interfaces.PageRepository
import com.team.notify.taskflow.repository.interfaces.SpaceRepository
import com.team.notify.taskflow.repository.interfaces.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
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

    override fun getPagesForSpace(spaceId: String): Flow<List<PageEntity>> =
        flow.map { list -> list.filter { it.spaceId == spaceId } }

    override fun getPageById(id: String): Flow<PageEntity?> =
        flow.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun insert(page: PageEntity) {
        store[page.id] = page
        flow.value = store.values.sortedBy { it.title }
    }

    override suspend fun deleteById(id: String) {
        store.remove(id)
        flow.value = store.values.sortedBy { it.title }
    }
}

class InMemoryTaskRepository(
    private val spaceId: String = DEFAULT_SPACE_ID
) : TaskRepository {
    private val store = ConcurrentHashMap<String, TaskEntity>()
    private val flow = MutableStateFlow<List<TaskEntity>>(emptyList())

    init {
        val now = System.currentTimeMillis()
        fun seed(title: String, status: String, hoursAhead: Int): TaskEntity {
            return TaskEntity(
                id = UUID.randomUUID().toString(),
                spaceId = spaceId,
                title = title,
                description = "$title description",
                status = status,
                assigneeId = null,
                labelsCsv = null,
                dueAt = now + hoursAhead * 60 * 60 * 1000L,
                createdAt = now,
                updatedAt = now
            )
        }
        listOf(
            seed("Fix login bug", "TODO", 6),
            seed("UI polish", "DOING", 24),
            seed("Write reminder worker", "DONE", 48)
        ).forEach { store[it.id] = it }
        flow.value = store.values.toList()
    }

    override fun getTasksForSpace(spaceId: String): Flow<List<TaskEntity>> =
        flow.map { list -> list.filter { it.spaceId == spaceId }.sortedBy { it.dueAt ?: Long.MAX_VALUE } }

    override fun getTaskById(id: String): Flow<TaskEntity?> =
        flow.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun insert(task: TaskEntity) {
        store[task.id] = task
        flow.value = store.values.toList()
    }

    override suspend fun deleteById(id: String) {
        store.remove(id)
        flow.value = store.values.toList()
    }
}
