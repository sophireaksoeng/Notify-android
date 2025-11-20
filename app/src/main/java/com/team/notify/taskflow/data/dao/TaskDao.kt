package com.team.notify.taskflow.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team.notify.taskflow.data.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE spaceId = :spaceId ORDER BY dueAt IS NULL, dueAt ASC")
    fun getTasksForSpace(spaceId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    fun getTaskById(id: String): Flow<TaskEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String)
}
