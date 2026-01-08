package com.team.notify.taskflow.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team.notify.taskflow.data.entities.OperationEntity

@Dao
interface OpQueueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(op: OperationEntity)

    @Query("SELECT * FROM op_queue ORDER BY timestamp ASC LIMIT :limit")
    suspend fun loadBatch(limit: Int): List<OperationEntity>

    @Query("DELETE FROM op_queue WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE op_queue SET retryCount = retryCount + 1, lastError = :error WHERE id = :id")
    suspend fun markFailed(id: String, error: String)

    @Query("DELETE FROM op_queue WHERE retryCount >= :maxRetries")
    suspend fun dropTooManyFailures(maxRetries: Int)
}
