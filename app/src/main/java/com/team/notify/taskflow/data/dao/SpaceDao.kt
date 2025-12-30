package com.team.notify.taskflow.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team.notify.taskflow.data.entities.SpaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpaceDao {
    @Query("SELECT * FROM spaces")
    fun getAllSpaces(): Flow<List<SpaceEntity>>

    @Query("SELECT * FROM spaces WHERE id = :id LIMIT 1")
    fun getSpaceById(id: String): Flow<SpaceEntity?>

    @Query("SELECT * FROM spaces")
    suspend fun getAllSpacesOnce(): List<SpaceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(space: SpaceEntity)

    @Query("DELETE FROM spaces WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM spaces")
    suspend fun count(): Int
}
