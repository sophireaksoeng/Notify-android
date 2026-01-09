package com.team.notify.taskflow.data.repository.interfaces

import kotlinx.coroutines.flow.Flow
import com.team.notify.taskflow.data.entities.UserProfileEntity

interface UserProfileRepository {
    suspend fun getUserProfile(userId: String): UserProfileEntity?
    suspend fun getUserProfileByEmail(email: String): UserProfileEntity?
    suspend fun createUserProfile(userProfile: UserProfileEntity): Long
    suspend fun updateUserProfile(userProfile: UserProfileEntity)
    suspend fun deleteUserProfile(userId: String)
    suspend fun updateLastLogin(userId: String, timestamp: Long)
    suspend fun setEmailVerified(userId: String, verified: Boolean)
    fun getActiveUserProfiles(): Flow<List<UserProfileEntity>>
    suspend fun getAllUserProfiles(): List<UserProfileEntity>
}
