package com.team.notify.fake

import com.team.notify.data.local.entity.PageEntity
import com.team.notify.data.local.entity.SpaceEntity
import com.team.notify.data.local.entity.TaskEntity
import com.team.notify.data.repository.NotifyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeNotifyRepository : NotifyRepository {
    private val spacesState = MutableStateFlow<List<SpaceEntity>>(emptyList())

    override fun observeSpaces(): Flow<List<SpaceEntity>> = spacesState.asStateFlow()

    override fun observeSpace(spaceId: String): Flow<SpaceEntity?> = MutableStateFlow(
        spacesState.value.firstOrNull { it.id == spaceId },
    )

    override suspend fun upsertSpace(space: SpaceEntity) {
        spacesState.value = spacesState.value.filterNot { it.id == space.id } + space
    }

    override suspend fun deleteSpace(spaceId: String) {
        spacesState.value = spacesState.value.filterNot { it.id == spaceId }
    }

    override suspend fun upsertSpaceFromRemote(space: SpaceEntity) {
        upsertSpace(space.copy(isSynced = true))
    }

    override suspend fun markSpaceSynced(spaceId: String) {
        val current = spacesState.value.firstOrNull { it.id == spaceId } ?: return
        upsertSpace(current.copy(isSynced = true))
    }

    override fun observePages(spaceId: String): Flow<List<PageEntity>> = MutableStateFlow(emptyList())

    override fun observePage(pageId: String): Flow<PageEntity?> = MutableStateFlow(null)

    override suspend fun upsertPage(page: PageEntity) = Unit

    override suspend fun deletePage(pageId: String) = Unit

    override suspend fun upsertPageFromRemote(page: PageEntity) = Unit

    override suspend fun markPageSynced(pageId: String) = Unit

    override fun observeTasks(pageId: String): Flow<List<TaskEntity>> = MutableStateFlow(emptyList())

    override fun observeTask(taskId: String): Flow<TaskEntity?> = MutableStateFlow(null)

    override suspend fun upsertTask(task: TaskEntity) = Unit

    override suspend fun deleteTask(taskId: String) = Unit

    override suspend fun upsertTaskFromRemote(task: TaskEntity) = Unit

    override suspend fun markTaskSynced(taskId: String) = Unit

    override suspend fun getUnsyncedSpaces(): List<SpaceEntity> = spacesState.value.filter { !it.isSynced }

    override suspend fun getUnsyncedPages(): List<PageEntity> = emptyList()

    override suspend fun getUnsyncedTasks(): List<TaskEntity> = emptyList()
}
