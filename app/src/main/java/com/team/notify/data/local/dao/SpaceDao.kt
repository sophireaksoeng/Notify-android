package com.team.notify.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.team.notify.data.local.entity.SpaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpaceDao {
    @Query("SELECT * FROM spaces WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<SpaceEntity>>

    @Query("SELECT * FROM spaces WHERE id = :spaceId AND isDeleted = 0 LIMIT 1")
    fun observeById(spaceId: String): Flow<SpaceEntity?>

    @Query("SELECT * FROM spaces WHERE id = :spaceId LIMIT 1")
    suspend fun getById(spaceId: String): SpaceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(space: SpaceEntity)

    @Update
    suspend fun update(space: SpaceEntity)

    @Query("DELETE FROM spaces WHERE id = :spaceId")
    suspend fun deleteById(spaceId: String)

    @Query(
        "UPDATE spaces SET isDeleted = 1, deletedAt = :now, updatedAt = :now, isSynced = 0 WHERE id = :spaceId",
    )
    suspend fun softDeleteById(spaceId: String, now: Long)

    @Query("SELECT * FROM spaces WHERE isSynced = 0")
    suspend fun getUnsynced(): List<SpaceEntity>

    @Query("UPDATE spaces SET isSynced = 1 WHERE id = :spaceId")
    suspend fun markSynced(spaceId: String)
}
