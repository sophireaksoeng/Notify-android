package com.team.notify.taskflow.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team.notify.taskflow.data.entities.OperationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OpQueueDao {
    @Query("SELECT * FROM op_queue ORDER BY timestamp ASC")
    fun getAllOperations(): Flow<List<OperationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(op: OperationEntity)

    @Query("DELETE FROM op_queue WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM op_queue")
    suspend fun clearAll()
}
