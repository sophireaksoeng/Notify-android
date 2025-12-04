package com.team.notify.taskflow.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team.notify.taskflow.data.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE spaceId = :spaceId ORDER BY (deadline IS NULL), deadline ASC")
    fun getTasksForSpace(spaceId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE spaceId = :spaceId ORDER BY updatedAt DESC")
    fun getTasks(spaceId: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(tasks: List<TaskEntity>)

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    fun getTaskById(id: String): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskByIdOnce(id: String): TaskEntity?

    @Query("""
        SELECT * FROM tasks
        WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'
        ORDER BY (deadline IS NULL), deadline ASC
    """)
    fun searchTasks(query: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksDebug(): List<TaskEntity>

    @Query("UPDATE tasks SET status = :status, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun updateStatus(taskId: String, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun countAllTasks(): Int
}
