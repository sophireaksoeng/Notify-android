package com.team.notify.taskflow.data.repository.interfaces

import com.team.notify.taskflow.data.entities.OperationEntity
import kotlinx.coroutines.flow.Flow

interface OpQueueRepository {
    fun getPendingOperations(): Flow<List<OperationEntity>>
    suspend fun addOperation(op: OperationEntity)
    suspend fun removeOperation(id: String)
    suspend fun clear()
    suspend fun enqueueUpsert(
        type: String,
        id: String,
        payload: Map<String, Any?>
    )
    suspend fun pushPendingOperations()
}
