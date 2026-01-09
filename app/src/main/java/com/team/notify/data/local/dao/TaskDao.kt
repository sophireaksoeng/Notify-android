package com.team.notify.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team.notify.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE pageId = :pageId AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun observeByPageId(pageId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId AND isDeleted = 0 LIMIT 1")
    fun observeById(taskId: String): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    suspend fun getById(taskId: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteById(taskId: String)

    @Query(
        "UPDATE tasks SET isDeleted = 1, deletedAt = :now, updatedAt = :now, isSynced = 0 WHERE id = :taskId",
    )
    suspend fun softDeleteById(taskId: String, now: Long)

    @Query(
        "UPDATE tasks SET isDeleted = 1, deletedAt = :now, updatedAt = :now, isSynced = 0 WHERE pageId = :pageId",
    )
    suspend fun softDeleteByPageId(pageId: String, now: Long)

    @Query(
        "UPDATE tasks SET isDeleted = 1, deletedAt = :now, updatedAt = :now, isSynced = 0 WHERE pageId IN (:pageIds)",
    )
    suspend fun softDeleteByPageIds(pageIds: List<String>, now: Long)

    @Query("SELECT * FROM tasks WHERE isSynced = 0")
    suspend fun getUnsynced(): List<TaskEntity>

    @Query("UPDATE tasks SET isSynced = 1 WHERE id = :taskId")
    suspend fun markSynced(taskId: String)

    @Query("SELECT * FROM tasks WHERE isDeleted = 0 AND dueDate IS NOT NULL AND dueDate > :now AND status != :doneStatus")
    suspend fun getUpcomingDueTasks(now: Long, doneStatus: String = "DONE"): List<TaskEntity>
}
