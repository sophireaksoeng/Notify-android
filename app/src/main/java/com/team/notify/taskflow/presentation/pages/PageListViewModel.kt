package com.team.notify.taskflow.presentation.pages

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.taskflow.data.repository.interfaces.PageRepository
import com.team.notify.data.sync.FirebaseSyncService
import com.team.notify.taskflow.model.PageType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PageListViewModel @Inject constructor(
    private val repo: PageRepository,
    private val firebaseSyncService: FirebaseSyncService,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val spaceIdFlow = MutableStateFlow("notify-db")

    val pages: StateFlow<List<PageEntity>> =
        spaceIdFlow
            .flatMapLatest { sid -> repo.getPagesForSpace(sid) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<PageListUiState> =
        pages
            .map { list ->
                if (list.isEmpty()) PageListUiState.Empty
                else PageListUiState.Data(list.sortedBy { it.title })
            }
            .catch { e ->
                Log.e("PageListVM", "Error", e)
                emit(PageListUiState.Error(e.message ?: "Unknown error"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PageListUiState.Loading)

    fun onEnter(spaceId: String) {
        spaceIdFlow.value = spaceId
    }

    fun createPage(title: String = "Untitled", pageType: PageType = PageType.TASKS, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            Log.d("PageListVM", "Creating page with title: $title")
            try {
                // First try creating locally for immediate UI update
                val localId = UUID.randomUUID().toString()
                val now = System.currentTimeMillis()
                val page = PageEntity(
                    id = localId,
                    spaceId = spaceIdFlow.value,
                    title = title,
                    content = "",
                    pageType = pageType, // Add pageType field
                    version = 1,
                    createdAt = now,
                    updatedAt = now,
                    hasConflict = false,
                    ownerId = auth.currentUser?.uid ?: "" // Add ownerId for Firebase security rules
                )
                
                Log.d("PageListVM", "Creating page locally first with ID: $localId")
                repo.insert(page)
                
                // Notify UI immediately
                onCreated(localId)
                
                // Then try to sync to Firebase in background
                try {
                    Log.d("PageListVM", "Attempting Firebase sync")
                    val result = firebaseSyncService.createPageInFirebase(page.copy(id = ""))
                    
                    result.fold(
                        onSuccess = { firebaseId ->
                            Log.d("PageListVM", "Firebase sync successful with ID: $firebaseId")
                            // Update local page with Firebase ID
                            val updatedPage = page.copy(id = firebaseId)
                            repo.update(updatedPage)
                            // Notify UI of ID change
                            onCreated(firebaseId)
                        },
                        onFailure = { exception ->
                            Log.e("PageListVM", "Firebase sync failed, keeping local page", exception)
                        }
                    )
                } catch (e: Exception) {
                    Log.e("PageListVM", "Firebase sync exception", e)
                }
            } catch (e: Exception) {
                Log.e("PageListVM", "createPage failed completely", e)
            }
        }
    }

    fun renamePage(pageId: String, newTitle: String) {
        viewModelScope.launch {
            try {
                val pages = repo.getPagesForSpace(spaceIdFlow.value).first()
                val page = pages.find { it.id == pageId }
                if (page != null) {
                    val updatedPage = page.copy(
                        title = newTitle,
                        updatedAt = System.currentTimeMillis()
                    )
                    repo.update(updatedPage)
                    
                    // Also sync to Firebase
                    firebaseSyncService.updatePageInFirebase(updatedPage)
                }
            } catch (e: Exception) {
                Log.e("PageListVM", "renamePage failed", e)
            }
        }
    }

    fun deletePage(pageId: String) {
        viewModelScope.launch {
            try {
                // Delete locally first
                repo.deleteById(pageId)
                
                // Also delete from Firebase
                firebaseSyncService.deletePageInFirebase(pageId)
            } catch (e: Exception) {
                Log.e("PageListVM", "deletePage failed", e)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                runCatching { repo.pullRemoteChanges(spaceIdFlow.value) }
                    .onFailure { Log.w("PageListVM", "refresh failed: ${it.message}") }
            } catch (e: Exception) {
                Log.e("PageListVM", "refresh failed", e)
            }
        }
    }
}
