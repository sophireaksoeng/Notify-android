package com.team.notify.taskflow.data.repository.interfaces

import com.team.notify.data.local.entity.SpaceEntity
import kotlinx.coroutines.flow.Flow

interface SpaceRepository {
    fun getSpaces(): Flow<List<SpaceEntity>>
    fun getSpaceById(id: String): Flow<SpaceEntity?>
    suspend fun insert(space: SpaceEntity)
    suspend fun deleteById(id: String)
}
