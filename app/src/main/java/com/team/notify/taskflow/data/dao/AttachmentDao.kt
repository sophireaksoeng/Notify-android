package com.team.notify.taskflow.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team.notify.taskflow.data.entities.AttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {

    @Query("SELECT * FROM attachments WHERE spaceId = :spaceId AND entityType = :entityType AND entityId = :entityId ORDER BY updatedAt DESC")
    fun attachmentsFor(spaceId: String, entityType: String, entityId: String): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments WHERE id = :id LIMIT 1")
    suspend fun getByIdOnce(id: String): AttachmentEntity?

    @Query("SELECT * FROM attachments WHERE status IN ('PENDING','FAILED') ORDER BY updatedAt ASC LIMIT :limit")
    suspend fun loadUploadQueue(limit: Int): List<AttachmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(a: AttachmentEntity)

    @Query("DELETE FROM attachments WHERE id = :id")
    suspend fun deleteById(id: String)
}
