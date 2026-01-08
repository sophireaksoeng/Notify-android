package com.team.notify.fake

import com.team.notify.data.local.dao.PageDao
import com.team.notify.data.local.dao.SpaceDao
import com.team.notify.data.local.dao.TaskDao
import com.team.notify.data.local.entity.PageEntity
import com.team.notify.data.local.entity.SpaceEntity
import com.team.notify.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSpaceDao : SpaceDao {
    private val items = LinkedHashMap<String, SpaceEntity>()
    private val state = MutableStateFlow<List<SpaceEntity>>(emptyList())

    override fun observeAll(): Flow<List<SpaceEntity>> = state

    override fun observeById(spaceId: String): Flow<SpaceEntity?> = state.map { list ->
        list.firstOrNull { it.id == spaceId }
    }

    override suspend fun getById(spaceId: String): SpaceEntity? = items[spaceId]

    override suspend fun upsert(space: SpaceEntity) {
        items[space.id] = space
        state.value = items.values.toList()
    }

    override suspend fun update(space: SpaceEntity) {
        upsert(space)
    }

    override suspend fun deleteById(spaceId: String) {
        items.remove(spaceId)
        state.value = items.values.toList()
    }

    override suspend fun getUnsynced(): List<SpaceEntity> = items.values.filter { !it.isSynced }

    override suspend fun markSynced(spaceId: String) {
        val current = items[spaceId] ?: return
        items[spaceId] = current.copy(isSynced = true)
        state.value = items.values.toList()
    }
}

class FakePageDao : PageDao {
    private val items = LinkedHashMap<String, PageEntity>()

    override fun observeBySpaceId(spaceId: String): Flow<List<PageEntity>> {
        return MutableStateFlow(items.values.filter { it.spaceId == spaceId })
    }

    override fun observeById(pageId: String): Flow<PageEntity?> {
        return MutableStateFlow(items[pageId])
    }

    override suspend fun getById(pageId: String): PageEntity? = items[pageId]

    override suspend fun upsert(page: PageEntity) {
        items[page.id] = page
    }

    override suspend fun deleteById(pageId: String) {
        items.remove(pageId)
    }

    override suspend fun getUnsynced(): List<PageEntity> = items.values.filter { !it.isSynced }

    override suspend fun markSynced(pageId: String) {
        val current = items[pageId] ?: return
        items[pageId] = current.copy(isSynced = true)
    }
}

class FakeTaskDao : TaskDao {
    private val items = LinkedHashMap<String, TaskEntity>()

    override fun observeByPageId(pageId: String): Flow<List<TaskEntity>> {
        return MutableStateFlow(items.values.filter { it.pageId == pageId })
    }

    override fun observeById(taskId: String): Flow<TaskEntity?> {
        return MutableStateFlow(items[taskId])
    }

    override suspend fun getById(taskId: String): TaskEntity? = items[taskId]

    override suspend fun upsert(task: TaskEntity) {
        items[task.id] = task
    }

    override suspend fun deleteById(taskId: String) {
        items.remove(taskId)
    }

    override suspend fun getUnsynced(): List<TaskEntity> = items.values.filter { !it.isSynced }

    override suspend fun markSynced(taskId: String) {
        val current = items[taskId] ?: return
        items[taskId] = current.copy(isSynced = true)
    }

    override suspend fun getUpcomingDueTasks(now: Long, doneStatus: String): List<TaskEntity> {
        return items.values.filter { it.dueDate != null && it.dueDate > now && it.status != doneStatus }
    }
}
