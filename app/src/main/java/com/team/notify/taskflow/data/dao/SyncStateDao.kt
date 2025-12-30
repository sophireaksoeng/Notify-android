package com.team.notify.taskflow.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team.notify.taskflow.data.entities.SyncStateEntity

@Dao
interface SyncStateDao {

    @Query("SELECT * FROM sync_state WHERE spaceId = :spaceId LIMIT 1")
    suspend fun get(spaceId: String): SyncStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: SyncStateEntity)
}
