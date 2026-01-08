package com.team.notify.taskflow.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.team.notify.taskflow.data.entities.UserProfileEntity

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    suspend fun getUserProfile(userId: String): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE email = :email")
    suspend fun getUserProfileByEmail(email: String): UserProfileEntity?

    @Query("SELECT * FROM user_profiles ORDER BY updatedAt DESC")
    suspend fun getAllUserProfiles(): List<UserProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfileEntity): Long

    @Update
    suspend fun updateUserProfile(userProfile: UserProfileEntity)

    @Query("DELETE FROM user_profiles WHERE userId = :userId")
    suspend fun deleteUserProfile(userId: String)

    @Query("SELECT * FROM user_profiles WHERE status != 'DELETED' ORDER BY displayName ASC")
    fun getActiveUserProfiles(): Flow<List<UserProfileEntity>>

    @Query("UPDATE user_profiles SET lastLoginAt = :timestamp WHERE userId = :userId")
    suspend fun updateLastLogin(userId: String, timestamp: Long)

    @Query("UPDATE user_profiles SET isEmailVerified = :verified WHERE userId = :userId")
    suspend fun setEmailVerified(userId: String, verified: Boolean)
}
