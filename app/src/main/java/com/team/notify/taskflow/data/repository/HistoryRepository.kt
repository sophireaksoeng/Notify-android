package com.team.notify.taskflow.data.repository

import com.team.notify.taskflow.data.dao.OpQueueDao
import com.team.notify.taskflow.data.dao.PageDao
import com.team.notify.taskflow.data.dao.PageHistoryDao
import com.team.notify.taskflow.data.entities.OperationEntity
import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.taskflow.data.entities.PageHistoryEntity
import java.util.UUID
import javax.inject.Inject

class HistoryRepository @Inject constructor(
    private val historyDao: PageHistoryDao,
    private val pageDao: PageDao,
    private val opDao: OpQueueDao
) {

    fun historyForPage(pageId: String) =
        historyDao.getHistory(pageId)

    suspend fun restore(pageId: String, version: PageHistoryEntity) {
        val restored = PageEntity(
            id = pageId,
            spaceId = "",
            title = "",
            content = version.content,
            version = version.version + 1,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        pageDao.upsert(restored)

        opDao.insert(
            OperationEntity(
                id = UUID.randomUUID().toString(),
                entityType = "PAGE",
                entityId = pageId,
                spaceId = restored.spaceId,
                userId = "currentUser",
                operation = "UPSERT",
                payloadJson = "",
                timestamp = System.currentTimeMillis()
            )
        )
    }
}
