package com.team.notify.taskflow.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.notify.taskflow.data.entities.UserProfileEntity
import com.team.notify.taskflow.data.repository.interfaces.UserProfileRepository
import com.team.notify.taskflow.model.UserStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val firebaseAuth: com.google.firebase.auth.FirebaseAuth,
    private val userProfileSyncService: com.team.notify.taskflow.data.sync.UserProfileSyncService
) : ViewModel() {

    private val _currentUserProfile = MutableStateFlow<UserProfileEntity?>(null)
    val currentUserProfile: StateFlow<UserProfileEntity?> = _currentUserProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadCurrentUserProfile()
    }

    private fun loadCurrentUserProfile() {
        viewModelScope.launch {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                loadUserProfile(currentUser.uid)
            } else {
                _currentUserProfile.value = null
            }
        }
    }

    fun refreshProfileState() {
        loadCurrentUserProfile()
    }

    private fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // First try to sync from cloud
                val cloudResult = userProfileSyncService.syncProfileFromCloud(userId)
                
                // Then get from local database
                val profile = userProfileRepository.getUserProfile(userId)
                _currentUserProfile.value = profile
                _isLoading.value = false
                
                // If cloud sync failed, show error but don't fail completely
                if (cloudResult.isFailure) {
                    _errorMessage.value = "Cloud sync failed: ${cloudResult.exceptionOrNull()?.message}. Using local data."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load profile: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun syncProfileFromCloud() {
        viewModelScope.launch {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                _isLoading.value = true
                try {
                    val result = userProfileSyncService.syncProfileFromCloud(currentUser.uid)
                    if (result.isSuccess) {
                        loadUserProfile(currentUser.uid)
                    } else {
                        _errorMessage.value = "Failed to sync from cloud: ${result.exceptionOrNull()?.message}"
                    }
                    _isLoading.value = false
                } catch (e: Exception) {
                    _errorMessage.value = "Sync failed: ${e.message}"
                    _isLoading.value = false
                }
            }
        }
    }

    fun createProfile(
        email: String,
        displayName: String,
        username: String,
        bio: String? = null,
        avatarUrl: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val currentUser = firebaseAuth.currentUser
                if (currentUser == null) {
                    _errorMessage.value = "No user logged in"
                    _isLoading.value = false
                    return@launch
                }

                // Check if profile already exists
                val existingProfile = userProfileRepository.getUserProfileByEmail(email)
                if (existingProfile != null) {
                    _errorMessage.value = "Profile with this email already exists"
                    _isLoading.value = false
                    return@launch
                }

                val newProfile = UserProfileEntity(
                    userId = currentUser.uid,
                    email = email,
                    displayName = displayName,
                    username = username,
                    bio = bio,
                    avatarUrl = avatarUrl,
                    status = UserStatus.ACTIVE,
                    isEmailVerified = currentUser.isEmailVerified,
                    lastLoginAt = System.currentTimeMillis(),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                userProfileRepository.createUserProfile(newProfile)
                
                // Sync to cloud
                userProfileSyncService.syncProfileToCloud(newProfile)
                
                loadUserProfile(currentUser.uid)
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create profile: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(
        displayName: String? = null,
        username: String? = null,
        bio: String? = null,
        avatarUrl: String? = null,
        preferences: com.team.notify.taskflow.data.entities.UserPreferences? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val currentUser = firebaseAuth.currentUser
                if (currentUser == null) {
                    _errorMessage.value = "No user logged in"
                    _isLoading.value = false
                    return@launch
                }

                val currentProfile = _currentUserProfile.value
                if (currentProfile == null) {
                    _errorMessage.value = "No profile found"
                    _isLoading.value = false
                    return@launch
                }

                val updatedProfile = currentProfile.copy(
                    displayName = displayName ?: currentProfile.displayName,
                    username = username ?: currentProfile.username,
                    bio = bio ?: currentProfile.bio,
                    avatarUrl = avatarUrl ?: currentProfile.avatarUrl,
                    preferences = preferences ?: currentProfile.preferences,
                    updatedAt = System.currentTimeMillis()
                )

                userProfileRepository.updateUserProfile(updatedProfile)
                
                // Sync to cloud
                userProfileSyncService.syncProfileToCloud(updatedProfile)
                
                loadUserProfile(currentUser.uid)
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update profile: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            firebaseAuth.signOut()
            _currentUserProfile.value = null
            _isLoading.value = false
            _errorMessage.value = null
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
