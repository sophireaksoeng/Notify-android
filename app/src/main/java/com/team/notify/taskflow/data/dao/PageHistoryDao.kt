package com.team.notify.taskflow.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team.notify.taskflow.data.entities.PageHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PageHistoryDao {

    @Query("SELECT * FROM page_history WHERE pageId = :pageId ORDER BY timestamp DESC")
    fun getHistoryForPage(pageId: String): Flow<List<PageHistoryEntity>>

    @Query("SELECT * FROM page_history WHERE pageId = :pageId ORDER BY version DESC")
    fun getHistory(pageId: String): Flow<List<PageHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: PageHistoryEntity)

    @Query("DELETE FROM page_history WHERE pageId = :pageId")
    suspend fun deleteHistoryForPage(pageId: String)
}
