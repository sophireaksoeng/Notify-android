package com.team.notify.taskflow.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.team.notify.taskflow.data.dao.UserProfileDao
import com.team.notify.taskflow.data.entities.UserProfileEntity
import com.team.notify.taskflow.data.repository.interfaces.UserProfileRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val userProfileDao: UserProfileDao
) : UserProfileRepository {

    override suspend fun getUserProfile(userId: String): UserProfileEntity? {
        return userProfileDao.getUserProfile(userId)
    }

    override suspend fun getUserProfileByEmail(email: String): UserProfileEntity? {
        return userProfileDao.getUserProfileByEmail(email)
    }

    override suspend fun createUserProfile(userProfile: UserProfileEntity): Long {
        return userProfileDao.insertUserProfile(userProfile)
    }

    override suspend fun updateUserProfile(userProfile: UserProfileEntity) {
        userProfileDao.updateUserProfile(userProfile)
    }

    override suspend fun deleteUserProfile(userId: String) {
        userProfileDao.deleteUserProfile(userId)
    }

    override suspend fun updateLastLogin(userId: String, timestamp: Long) {
        userProfileDao.updateLastLogin(userId, timestamp)
    }

    override suspend fun setEmailVerified(userId: String, verified: Boolean) {
        userProfileDao.setEmailVerified(userId, verified)
    }

    override fun getActiveUserProfiles(): Flow<List<UserProfileEntity>> {
        return userProfileDao.getActiveUserProfiles()
    }

    override suspend fun getAllUserProfiles(): List<UserProfileEntity> {
        return userProfileDao.getAllUserProfiles()
    }
}
