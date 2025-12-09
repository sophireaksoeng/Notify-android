package com.team.notify.taskflow.repository.interfaces

import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.taskflow.data.entities.PageHistoryEntity
import kotlinx.coroutines.flow.Flow

interface PageRepository {

    fun getPagesForSpace(id: String): Flow<List<PageEntity>>
    fun getPageById(id: String): Flow<PageEntity?>
    fun searchPages(query: String): Flow<List<PageEntity>>
    fun getHistoryForPage(pageId: String): Flow<List<PageHistoryEntity>>
    suspend fun insert(page: PageEntity)
    suspend fun insertHistory(history: PageHistoryEntity)
    suspend fun deleteHistoryForPage(pageId: String)
    suspend fun deleteById(id: String)
    suspend fun pullRemoteChanges(spaceId: String)
    suspend fun pushPendingOperations()
    suspend fun initialSync(spaceId: String)
}
