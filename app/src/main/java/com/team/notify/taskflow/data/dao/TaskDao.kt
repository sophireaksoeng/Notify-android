package com.team.notify.taskflow.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team.notify.taskflow.data.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE spaceId = :spaceId ORDER BY updatedAt DESC")
    fun getTasksForSpace(spaceId: String): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks 
        WHERE spaceId = :spaceId 
        AND (
            title LIKE '%' || :q || '%' 
            OR description LIKE '%' || :q || '%'
        )
        ORDER BY updatedAt DESC
    """)
    fun searchTasks(spaceId: String, q: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    fun getTaskById(id: String): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskByIdOnce(id: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(tasks: List<TaskEntity>)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksDebug(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Query("SELECT COUNT(*) FROM tasks WHERE spaceId = :spaceId")
    suspend fun countForSpace(spaceId: String): Int
}
