package com.team.notify.taskflow.data.history

import com.team.notify.taskflow.auth.CurrentUserProvider
import com.team.notify.taskflow.data.dao.HistoryDao
import com.team.notify.taskflow.data.entities.HistoryEntity
import java.util.UUID

class HistoryWriter(
    private val historyDao: HistoryDao,
    private val currentUserProvider: CurrentUserProvider
) {
    suspend fun log(
        spaceId: String,
        entityType: String,
        entityId: String,
        field: String,
        oldValue: String?,
        newValue: String?,
        source: String = "LOCAL"
    ) {
        if (oldValue == newValue) return
        historyDao.insert(
            HistoryEntity(
                id = UUID.randomUUID().toString(),
                spaceId = spaceId,
                entityType = entityType,
                entityId = entityId,
                field = field,
                oldValue = oldValue,
                newValue = newValue,
                userId = currentUserProvider.getCurrentUserId(),
                timestamp = System.currentTimeMillis(),
                source = source
            )
        )
    }
}
