package com.team.notify.taskflow.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team.notify.taskflow.data.entities.PageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PageDao {

    @Query("SELECT * FROM pages WHERE spaceId = :spaceId ORDER BY updatedAt DESC")
    fun pagesForSpace(spaceId: String): Flow<List<PageEntity>>

    @Query("SELECT * FROM pages WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<PageEntity?>

    @Query("SELECT * FROM pages WHERE id = :id LIMIT 1")
    suspend fun getByIdOnce(id: String): PageEntity?

    @Query("""
        SELECT * FROM pages
        WHERE title LIKE '%' || :query || '%'
           OR content LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC
    """)
    fun searchPages(query: String): Flow<List<PageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(page: PageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(pages: List<PageEntity>)

    @Query("DELETE FROM pages WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM pages")
    suspend fun getAllPagesDebug(): List<PageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(page: PageEntity)

    @Query("SELECT COUNT(*) FROM pages WHERE spaceId = :spaceId")
    suspend fun countForSpace(spaceId: String): Int
}
