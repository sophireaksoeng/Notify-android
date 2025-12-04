package com.team.notify.taskflow.repository

import com.team.notify.taskflow.data.dao.OpQueueDao
import com.team.notify.taskflow.data.entities.OperationEntity
import com.team.notify.taskflow.repository.interfaces.OpQueueRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpQueueRepositoryImpl @Inject constructor(
    private val dao: OpQueueDao
) : OpQueueRepository {

    override fun getPendingOperations(): Flow<List<OperationEntity>> =
        dao.getAllOperations()

    override suspend fun addOperation(op: OperationEntity) {
        dao.insert(op)
    }

    override suspend fun removeOperation(id: String) {
        dao.deleteById(id)
    }

    override suspend fun clear() {
        dao.clearAll()
    }

    override suspend fun enqueueUpsert(
        type: String,
        id: String,
        payload: Map<String, Any?>
    ) {
        val op = OperationEntity(
            id = UUID.randomUUID().toString(),
            entityId = id,
            entityType = type,
            opType = "UPSERT",
            payloadJson = payload.toString(),
            timestamp = System.currentTimeMillis()
        )
        addOperation(op)
    }

    override suspend fun pushPendingOperations() {
        dao.getAllOperations()
    }
}
