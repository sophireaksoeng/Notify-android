package com.team.notify.taskflow.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team.notify.taskflow.data.entities.PageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(page: PageEntity)

    @Query("SELECT * FROM pages WHERE id = :id")
    fun getPageById(id: String): Flow<PageEntity?>

    @Query("SELECT * FROM pages WHERE spaceId = :spaceId")
    fun getPagesForSpace(spaceId: String): Flow<List<PageEntity>>

    @Query("SELECT * FROM pages WHERE title LIKE :query OR content LIKE :query")
    fun searchPages(query: String): Flow<List<PageEntity>>

    @Query("DELETE FROM pages WHERE id = :id")
    suspend fun deleteById(id: String)
}