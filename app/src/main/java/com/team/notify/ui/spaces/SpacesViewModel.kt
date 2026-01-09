package com.team.notify.ui.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.data.local.entity.SpaceEntity
import com.team.notify.taskflow.data.entities.TaskEntity
import com.team.notify.taskflow.model.TaskStatus
import com.team.notify.taskflow.data.repository.interfaces.SpaceRepository
import com.team.notify.taskflow.data.repository.interfaces.PageRepository
import com.team.notify.data.sync.FirebaseSyncService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import javax.inject.Inject

@HiltViewModel
class SpacesViewModel @Inject constructor(
    private val spaceRepo: SpaceRepository,
    private val pageRepo: PageRepository,
    private val firebaseSyncService: FirebaseSyncService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpacesUiState())
    val uiState: StateFlow<SpacesUiState> = _uiState.asStateFlow()

    init {
        loadSpaces()
    }

    fun loadSpaces() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Load spaces from repository - get first result then collect for updates
                val spacesFlow = spaceRepo.getSpaces()
                
                // Get current spaces immediately
                val currentSpaces = spacesFlow.first()
                _uiState.value = _uiState.value.copy(
                    spaces = currentSpaces,
                    pages = emptyList(), // Pages will be loaded per space
                    tasks = emptyList(), // Tasks would come from task repository
                    isLoading = false
                )
                
                // Then continue collecting for real-time updates
                spacesFlow.collect { spaces ->
                    _uiState.value = _uiState.value.copy(
                        spaces = spaces,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun createSpace(name: String, description: String) {
        viewModelScope.launch {
            try {
                // Check if user is authenticated
                val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Please sign in to create spaces"
                    )
                    return@launch
                }
                
                Log.d("SpacesViewModel", "Creating space for user: ${currentUser.uid}")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null, success = null)
                
                val newSpaceId = "space-${System.currentTimeMillis()}"
                val newSpace = SpaceEntity(
                    id = newSpaceId,
                    name = name,
                    ownerId = currentUser.uid,
                    updatedAt = System.currentTimeMillis(),
                    isSynced = false
                )
                
                Log.d("SpacesViewModel", "Attempting to create space: ${newSpace.name}")
                
                // Try to create in Firebase first, but fallback to local only
                val firebaseResult = try {
                    withTimeout(10000) { // 10 second timeout
                        firebaseSyncService.createSpaceInFirebase(newSpace)
                    }
                } catch (e: TimeoutCancellationException) {
                    Log.w("SpacesViewModel", "Firebase timeout, creating locally only")
                    // Firebase timed out, create locally only with ownerId
                    spaceRepo.insert(newSpace)
                    Result.success(newSpace.id)
                }
                
                if (firebaseResult.isSuccess) {
                    val spaceId = firebaseResult.getOrThrow()
                    Log.d("SpacesViewModel", "Space created successfully with ID: $spaceId")
                    
                    // Create initial page for the new space
                    val initialPage = PageEntity(
                        id = "page-${System.currentTimeMillis()}",
                        spaceId = spaceId,
                        title = "Welcome to $name",
                        content = "This is your new workspace. Start by creating pages and organizing your tasks.",
                        version = 1,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        hasConflict = false,
                        ownerId = currentUser.uid // Add ownerId for Firebase security rules
                    )
                    
                    // Try to create page in Firebase, but fallback to local
                    try {
                        withTimeout(5000) { // 5 second timeout for page
                            firebaseSyncService.createPageInFirebase(initialPage)
                        }
                    } catch (e: TimeoutCancellationException) {
                        // Firebase timed out, create locally only
                        pageRepo.insert(initialPage)
                    }
                    
                    // Reload spaces to get updated list
                    loadSpaces()
                    
                    val message = if (spaceId == newSpace.id) {
                        "Space created locally (Firebase unavailable)"
                    } else {
                        "Space created successfully!"
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        success = message
                    )
                } else {
                    Log.e("SpacesViewModel", "Failed to create space: ${firebaseResult.exceptionOrNull()?.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to create space: ${firebaseResult.exceptionOrNull()?.message}"
                    )
                }
                
            } catch (e: Exception) {
                Log.e("SpacesViewModel", "Exception creating space: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to create space: ${e.message}"
                )
            }
        }
    }

    fun syncWithFirebase() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val result = firebaseSyncService.syncAll()
                
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            success = "Successfully synced with Firebase!"
                        )
                        loadSpaces() // Reload to show synced data
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Sync failed: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Sync failed: ${e.message}"
                )
            }
        }
    }

    fun refreshSpaces() {
        loadSpaces()
    }
}

data class SpacesUiState(
    val spaces: List<SpaceEntity> = emptyList(),
    val pages: List<PageEntity> = emptyList(),
    val tasks: List<TaskEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val success: String? = null
)
