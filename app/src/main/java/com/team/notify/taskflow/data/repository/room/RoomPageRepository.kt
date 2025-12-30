package com.team.notify.taskflow.data.repository.room

import com.team.notify.taskflow.auth.CurrentUserProvider
import com.team.notify.taskflow.data.dao.OpQueueDao
import com.team.notify.taskflow.data.dao.PageDao
import com.team.notify.taskflow.data.entities.OperationEntity
import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.taskflow.data.repository.interfaces.PageRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class RoomPageRepository @Inject constructor(
    private val pageDao: PageDao,
    private val opDao: OpQueueDao,
    private val currentUserProvider: CurrentUserProvider
) : PageRepository {

    override fun getPagesForSpace(id: String): Flow<List<PageEntity>> =
        pageDao.pagesForSpace(id)

    override fun getPageById(id: String): Flow<PageEntity?> =
        pageDao.observeById(id)

    override fun searchPages(query: String): Flow<List<PageEntity>> =
        pageDao.searchPages(query)

    override suspend fun insert(page: PageEntity) {
        pageDao.upsert(page)

        opDao.insert(
            OperationEntity(
                id = UUID.randomUUID().toString(),
                entityId = page.id,
                entityType = "PAGE",
                spaceId = page.spaceId,
                userId = currentUserProvider.getCurrentUserId() ?: "",
                operation = "UPSERT",
                payloadJson = "",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    override suspend fun deleteById(id: String) {
        val spaceId = pageDao.getByIdOnce(id)?.spaceId ?: ""

        pageDao.deleteById(id)

        opDao.insert(
            OperationEntity(
                id = UUID.randomUUID().toString(),
                entityId = id,
                entityType = "PAGE",
                spaceId = spaceId,
                userId = currentUserProvider.getCurrentUserId() ?: "",
                operation = "DELETE",
                payloadJson = "",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    override suspend fun pullRemoteChanges(spaceId: String) {}
    override suspend fun pushPendingOperations() {}
    override suspend fun initialSync(spaceId: String) {}
}