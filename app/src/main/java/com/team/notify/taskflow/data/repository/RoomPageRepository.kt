package com.team.notify.taskflow.data.repository

import com.team.notify.taskflow.data.dao.OpQueueDao
import com.team.notify.taskflow.data.dao.PageDao
import com.team.notify.taskflow.data.dao.PageHistoryDao
import com.team.notify.taskflow.data.entities.OperationEntity
import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.taskflow.data.entities.PageHistoryEntity
import com.team.notify.taskflow.data.repository.interfaces.PageRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class RoomPageRepository @Inject constructor(
    private val pageDao: PageDao,
    private val pageHistoryDao: PageHistoryDao,
    private val opDao: OpQueueDao
) : PageRepository {

    override fun getPagesForSpace(id: String): Flow<List<PageEntity>> =
        pageDao.getPagesForSpace(id)

    override fun getPageById(id: String): Flow<PageEntity?> =
        pageDao.getPageById(id)

    override fun searchPages(query: String): Flow<List<PageEntity>> =
        pageDao.searchPages("%$query%")

    override fun getHistoryForPage(pageId: String): Flow<List<PageHistoryEntity>> =
        pageHistoryDao.getHistoryForPage(pageId)

    override suspend fun insert(page: PageEntity) {
        pageDao.upsert(page)

        opDao.insert(
            OperationEntity(
                id = UUID.randomUUID().toString(),
                entityType = "PAGE",
                entityId = page.id,
                operation = "UPSERT",
                timestamp = System.currentTimeMillis(),
                payloadJson = ""
            )
        )
    }

    override suspend fun insertHistory(history: PageHistoryEntity) {
        pageHistoryDao.insert(history)
    }

    override suspend fun deleteHistoryForPage(pageId: String) {
        pageHistoryDao.deleteHistoryForPage(pageId)
    }

    override suspend fun deleteById(id: String) {
        pageDao.deleteById(id)

        opDao.insert(
            OperationEntity(
                id = UUID.randomUUID().toString(),
                entityType = "PAGE",
                entityId = id,
                operation = "DELETE",
                timestamp = System.currentTimeMillis(),
                payloadJson = ""
            )
        )
    }

    override suspend fun pullRemoteChanges(spaceId: String) {
    }

    override suspend fun pushPendingOperations() {
    }

    override suspend fun initialSync(spaceId: String) {
    }
}
