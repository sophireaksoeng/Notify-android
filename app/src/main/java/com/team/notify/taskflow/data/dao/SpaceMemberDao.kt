package com.team.notify.taskflow.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.team.notify.taskflow.data.entities.SpaceMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpaceMemberDao {

    @Query("SELECT * FROM space_members WHERE spaceId = :spaceId")
    fun getMembers(spaceId: String): Flow<List<SpaceMemberEntity>>

    @Query("SELECT * FROM space_members WHERE spaceId = :spaceId AND userId = :userId")
    suspend fun getMember(spaceId: String, userId: String): SpaceMemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(member: SpaceMemberEntity)
}
