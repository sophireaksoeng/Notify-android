package com.team.notify.data.repository

import com.team.notify.data.local.dao.PageDao
import com.team.notify.data.local.dao.SpaceDao
import com.team.notify.data.local.dao.TaskDao
import com.team.notify.data.local.entity.PageEntity
import com.team.notify.data.local.entity.SpaceEntity
import com.team.notify.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NotifyRepositoryImpl @Inject constructor(
    private val spaceDao: SpaceDao,
    private val pageDao: PageDao,
    private val taskDao: TaskDao,
) : NotifyRepository {

    override fun observeSpaces(): Flow<List<SpaceEntity>> = spaceDao.observeAll()

    override fun observeSpace(spaceId: String): Flow<SpaceEntity?> = spaceDao.observeById(spaceId)

    override suspend fun upsertSpace(space: SpaceEntity) {
        val now = System.currentTimeMillis()
        spaceDao.upsert(space.copy(updatedAt = now, isSynced = false, isDeleted = space.isDeleted, deletedAt = space.deletedAt))
    }

    override suspend fun deleteSpace(spaceId: String) {
        val now = System.currentTimeMillis()
        val pageIds = pageDao.getIdsBySpaceId(spaceId)

        // Soft-delete children first so they also get synced as tombstones.
        if (pageIds.isNotEmpty()) {
            taskDao.softDeleteByPageIds(pageIds, now)
        }
        pageDao.softDeleteBySpaceId(spaceId, now)
        spaceDao.softDeleteById(spaceId, now)
    }

    override suspend fun upsertSpaceFromRemote(space: SpaceEntity) {
        spaceDao.upsert(space.copy(isSynced = true, isDeleted = space.isDeleted, deletedAt = space.deletedAt))
    }

    override suspend fun markSpaceSynced(spaceId: String) {
        spaceDao.markSynced(spaceId)
    }

    override fun observePages(spaceId: String): Flow<List<PageEntity>> = pageDao.observeBySpaceId(spaceId)

    override fun observePage(pageId: String): Flow<PageEntity?> = pageDao.observeById(pageId)

    override suspend fun upsertPage(page: PageEntity) {
        val now = System.currentTimeMillis()
        pageDao.upsert(page.copy(updatedAt = now, isSynced = false, isDeleted = page.isDeleted, deletedAt = page.deletedAt))
    }

    override suspend fun deletePage(pageId: String) {
        val now = System.currentTimeMillis()
        taskDao.softDeleteByPageId(pageId, now)
        pageDao.softDeleteById(pageId, now)
    }

    override suspend fun upsertPageFromRemote(page: PageEntity) {
        pageDao.upsert(page.copy(isSynced = true, isDeleted = page.isDeleted, deletedAt = page.deletedAt))
    }

    override suspend fun markPageSynced(pageId: String) {
        pageDao.markSynced(pageId)
    }

    override fun observeTasks(pageId: String): Flow<List<TaskEntity>> = taskDao.observeByPageId(pageId)

    override fun observeTask(taskId: String): Flow<TaskEntity?> = taskDao.observeById(taskId)

    override suspend fun upsertTask(task: TaskEntity) {
        val now = System.currentTimeMillis()
        taskDao.upsert(task.copy(updatedAt = now, isSynced = false, isDeleted = task.isDeleted, deletedAt = task.deletedAt))
    }

    override suspend fun deleteTask(taskId: String) {
        val now = System.currentTimeMillis()
        taskDao.softDeleteById(taskId, now)
    }

    override suspend fun upsertTaskFromRemote(task: TaskEntity) {
        taskDao.upsert(task.copy(isSynced = true, isDeleted = task.isDeleted, deletedAt = task.deletedAt))
    }

    override suspend fun markTaskSynced(taskId: String) {
        taskDao.markSynced(taskId)
    }

    override suspend fun getUnsyncedSpaces(): List<SpaceEntity> = spaceDao.getUnsynced()

    override suspend fun getUnsyncedPages(): List<PageEntity> = pageDao.getUnsynced()

    override suspend fun getUnsyncedTasks(): List<TaskEntity> = taskDao.getUnsynced()
}
