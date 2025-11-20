package com.team.notify.taskflow.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team.notify.taskflow.data.entities.PageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PageDao {
    @Query("SELECT * FROM pages WHERE spaceId = :spaceId")
    fun getPagesForSpace(spaceId: String): Flow<List<PageEntity>>

    @Query("SELECT * FROM pages WHERE id = :id LIMIT 1")
    fun getPageById(id: String): Flow<PageEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(page: PageEntity)

    @Query("DELETE FROM pages WHERE id = :id")
    suspend fun deleteById(id: String)
}
