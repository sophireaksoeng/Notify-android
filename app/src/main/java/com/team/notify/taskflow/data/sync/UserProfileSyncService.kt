package com.team.notify.taskflow.data.sync

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.team.notify.taskflow.data.entities.UserProfileEntity
import com.team.notify.taskflow.data.repository.interfaces.UserProfileRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileSyncService @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userProfileRepository: UserProfileRepository
) {

    suspend fun syncProfileToCloud(userProfile: UserProfileEntity): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Result.failure(Exception("No authenticated user"))
            } else {
                // Upload to Firestore
                val userProfileData = hashMapOf(
                    "userId" to userProfile.userId,
                    "email" to userProfile.email,
                    "displayName" to userProfile.displayName,
                    "username" to userProfile.username,
                    "bio" to userProfile.bio,
                    "avatarUrl" to userProfile.avatarUrl,
                    "status" to userProfile.status.name,
                    "isEmailVerified" to userProfile.isEmailVerified,
                    "lastLoginAt" to userProfile.lastLoginAt,
                    "createdAt" to userProfile.createdAt,
                    "updatedAt" to userProfile.updatedAt,
                    "preferences" to userProfile.preferences
                )

                firestore.collection("users")
                    .document(userProfile.userId)
                    .set(userProfileData)
                    .await()

                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncProfileFromCloud(userId: String): Result<UserProfileEntity> {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                val data = document.data
                val userProfile = UserProfileEntity(
                    userId = data?.get("userId") as? String ?: userId,
                    email = data?.get("email") as? String ?: "",
                    displayName = data?.get("displayName") as? String ?: "",
                    username = data?.get("username") as? String ?: "",
                    bio = data?.get("bio") as? String,
                    avatarUrl = data?.get("avatarUrl") as? String,
                    status = com.team.notify.taskflow.model.UserStatus.valueOf(
                        data?.get("status") as? String ?: "ACTIVE"
                    ),
                    isEmailVerified = data?.get("isEmailVerified") as? Boolean ?: false,
                    lastLoginAt = data?.get("lastLoginAt") as? Long ?: System.currentTimeMillis(),
                    createdAt = data?.get("createdAt") as? Long ?: System.currentTimeMillis(),
                    updatedAt = data?.get("updatedAt") as? Long ?: System.currentTimeMillis(),
                    preferences = data?.get("preferences") as? com.team.notify.taskflow.data.entities.UserPreferences
                )

                // Update local database
                userProfileRepository.updateUserProfile(userProfile)
                
                Result.success(userProfile)
            } else {
                Result.failure(Exception("Profile not found in cloud"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProfileFromCloud(userId: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncAllProfilesToCloud(): Result<Int> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Result.failure(Exception("No authenticated user"))
            } else {
                val localProfiles = userProfileRepository.getAllUserProfiles()
                var syncedCount = 0

                for (profile in localProfiles) {
                    val result = syncProfileToCloud(profile)
                    if (result.isSuccess) {
                        syncedCount++
                    }
                }

                Result.success(syncedCount)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
