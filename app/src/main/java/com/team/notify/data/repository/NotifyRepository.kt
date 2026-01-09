package com.team.notify.data.repository

import com.team.notify.data.local.entity.PageEntity
import com.team.notify.data.local.entity.SpaceEntity
import com.team.notify.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

interface NotifyRepository {
    fun observeSpaces(): Flow<List<SpaceEntity>>
    fun observeSpace(spaceId: String): Flow<SpaceEntity?>
    suspend fun upsertSpace(space: SpaceEntity)
    suspend fun deleteSpace(spaceId: String)

    suspend fun upsertSpaceFromRemote(space: SpaceEntity)
    suspend fun markSpaceSynced(spaceId: String)

    fun observePages(spaceId: String): Flow<List<PageEntity>>
    fun observePage(pageId: String): Flow<PageEntity?>
    suspend fun upsertPage(page: PageEntity)
    suspend fun deletePage(pageId: String)

    suspend fun upsertPageFromRemote(page: PageEntity)
    suspend fun markPageSynced(pageId: String)

    fun observeTasks(pageId: String): Flow<List<TaskEntity>>
    fun observeTask(taskId: String): Flow<TaskEntity?>
    suspend fun upsertTask(task: TaskEntity)
    suspend fun deleteTask(taskId: String)

    suspend fun upsertTaskFromRemote(task: TaskEntity)
    suspend fun markTaskSynced(taskId: String)

    suspend fun getUnsyncedSpaces(): List<SpaceEntity>
    suspend fun getUnsyncedPages(): List<PageEntity>
    suspend fun getUnsyncedTasks(): List<TaskEntity>
}
