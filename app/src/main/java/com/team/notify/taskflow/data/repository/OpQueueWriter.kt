package com.team.notify.taskflow.data.repository

import com.team.notify.taskflow.auth.CurrentUserProvider
import com.team.notify.taskflow.data.dao.OpQueueDao
import com.team.notify.taskflow.data.entities.OperationEntity
import org.json.JSONObject
import java.util.UUID

class OpQueueWriter(
    private val opQueueDao: OpQueueDao,
    private val currentUserProvider: CurrentUserProvider
) {
    suspend fun enqueueUpsert(entityType: String, entityId: String, spaceId: String, payloadJson: String) {
        val uid = currentUserProvider.getCurrentUserId() ?: "unknown"
        opQueueDao.insert(
            OperationEntity(
                id = UUID.randomUUID().toString(),
                entityType = entityType,
                entityId = entityId,
                spaceId = spaceId,
                userId = uid,
                operation = "UPSERT",
                payloadJson = payloadJson,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun enqueueDelete(entityType: String, entityId: String, spaceId: String) {
        val uid = currentUserProvider.getCurrentUserId() ?: "unknown"
        opQueueDao.insert(
            OperationEntity(
                id = UUID.randomUUID().toString(),
                entityType = entityType,
                entityId = entityId,
                spaceId = spaceId,
                userId = uid,
                operation = "DELETE",
                payloadJson = JSONObject().toString(),
                timestamp = System.currentTimeMillis()
            )
        )
    }
}