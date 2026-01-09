package com.team.notify.taskflow.data.repository.room

import com.team.notify.taskflow.auth.CurrentUserProvider
import com.team.notify.taskflow.data.dao.SpaceDao
import com.team.notify.taskflow.data.dao.OpQueueDao
import com.team.notify.data.local.entity.SpaceEntity
import com.team.notify.taskflow.data.entities.OperationEntity
import com.team.notify.taskflow.data.repository.interfaces.SpaceRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomSpaceRepository @Inject constructor(
    private val spaceDao: SpaceDao,
    private val opDao: OpQueueDao,
    private val currentUserProvider: CurrentUserProvider
) : SpaceRepository {
    
    override fun getSpaces(): Flow<List<SpaceEntity>> = spaceDao.getAllSpaces()
    
    override fun getSpaceById(id: String): Flow<SpaceEntity?> = spaceDao.getSpaceById(id)
    
    override suspend fun insert(space: SpaceEntity) {
        spaceDao.insert(space)
        
        // Add operation to sync queue
        opDao.insert(
            OperationEntity(
                id = UUID.randomUUID().toString(),
                entityId = space.id,
                entityType = "SPACE",
                spaceId = space.id,
                userId = currentUserProvider.getCurrentUserId() ?: "",
                operation = "UPSERT",
                payloadJson = "",
                timestamp = System.currentTimeMillis()
            )
        )
    }
    
    override suspend fun deleteById(id: String) {
        spaceDao.deleteById(id)
        
        // Add operation to sync queue
        opDao.insert(
            OperationEntity(
                id = UUID.randomUUID().toString(),
                entityId = id,
                entityType = "SPACE",
                spaceId = id,
                userId = currentUserProvider.getCurrentUserId() ?: "",
                operation = "DELETE",
                payloadJson = "",
                timestamp = System.currentTimeMillis()
            )
        )
    }
}
