package com.team.notify.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team.notify.data.local.entity.PageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PageDao {
    @Query("SELECT * FROM pages WHERE spaceId = :spaceId AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun observeBySpaceId(spaceId: String): Flow<List<PageEntity>>

    @Query("SELECT * FROM pages WHERE id = :pageId AND isDeleted = 0 LIMIT 1")
    fun observeById(pageId: String): Flow<PageEntity?>

    @Query("SELECT * FROM pages WHERE id = :pageId LIMIT 1")
    suspend fun getById(pageId: String): PageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(page: PageEntity)

    @Query("DELETE FROM pages WHERE id = :pageId")
    suspend fun deleteById(pageId: String)

    @Query(
        "UPDATE pages SET isDeleted = 1, deletedAt = :now, updatedAt = :now, isSynced = 0 WHERE id = :pageId",
    )
    suspend fun softDeleteById(pageId: String, now: Long)

    @Query(
        "UPDATE pages SET isDeleted = 1, deletedAt = :now, updatedAt = :now, isSynced = 0 WHERE spaceId = :spaceId",
    )
    suspend fun softDeleteBySpaceId(spaceId: String, now: Long)

    @Query("SELECT id FROM pages WHERE spaceId = :spaceId")
    suspend fun getIdsBySpaceId(spaceId: String): List<String>

    @Query("SELECT * FROM pages WHERE isSynced = 0")
    suspend fun getUnsynced(): List<PageEntity>

    @Query("UPDATE pages SET isSynced = 1 WHERE id = :pageId")
    suspend fun markSynced(pageId: String)
}
