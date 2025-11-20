package com.team.notify.taskflow.repository.interfaces

import com.team.notify.taskflow.data.entities.PageEntity
import kotlinx.coroutines.flow.Flow

interface PageRepository {
    fun getPagesForSpace(spaceId: String): Flow<List<PageEntity>>
    fun getPageById(id: String): Flow<PageEntity?>
    suspend fun insert(page: PageEntity)
    suspend fun deleteById(id: String)
}
