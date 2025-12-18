package com.team.notify.taskflow.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team.notify.taskflow.data.entities.ConflictEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConflictDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conflict: ConflictEntity)

    @Query("SELECT * FROM conflicts")
    fun getConflicts(): Flow<List<ConflictEntity>>
}
