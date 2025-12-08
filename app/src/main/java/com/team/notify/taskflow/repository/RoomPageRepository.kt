package com.team.notify.taskflow.repository

import com.team.notify.taskflow.data.dao.OpQueueDao
import com.team.notify.taskflow.data.dao.PageDao
import com.team.notify.taskflow.data.entities.OperationEntity
import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.taskflow.repository.interfaces.PageRepository
import java.util.UUID
import javax.inject.Inject

class RoomPageRepository @Inject constructor(
    private val pageDao: PageDao,
    private val opDao: OpQueueDao
) : PageRepository {

    override fun getPagesForSpace(id: String) = pageDao.getPagesForSpace(id)

    override fun getPageById(id: String) = pageDao.getPageById(id)

    override fun searchPages(query: String) = pageDao.searchPages("%$query%")

    override suspend fun insert(page: PageEntity) {
        pageDao.upsert(page)

        opDao.insert(
            OperationEntity(
                id = UUID.randomUUID().toString(),
                entityType = "PAGE",
                entityId = page.id,
                opType = "UPSERT",
                timestamp = System.currentTimeMillis(),
                payloadJson = ""
            )
        )
    }

    override suspend fun deleteById(id: String) {
        pageDao.deleteById(id)

        opDao.insert(
            OperationEntity(
                id = UUID.randomUUID().toString(),
                entityType = "PAGE",
                entityId = id,
                opType = "DELETE",
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
