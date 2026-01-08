package com.team.notify.taskflow.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.taskflow.data.repository.interfaces.PageRepository
import com.team.notify.data.sync.FirebaseSyncService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotesPageUiState(
    val isLoading: Boolean = false,
    val content: String = "",
    val error: String? = null
)

@HiltViewModel
class NotesPageViewModel @Inject constructor(
    private val repo: PageRepository,
    private val firebaseSyncService: FirebaseSyncService
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesPageUiState())
    val uiState: StateFlow<NotesPageUiState> = _uiState.asStateFlow()

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    private var currentPageId: String? = null

    fun load(pageId: String?) {
        currentPageId = pageId
        if (pageId == null) {
            _uiState.value = NotesPageUiState()
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repo.getPageById(pageId).collect { page ->
                    if (page != null) {
                        _uiState.value = NotesPageUiState(
                            isLoading = false,
                            content = page.content ?: ""
                        )
                    } else {
                        _uiState.value = NotesPageUiState(
                            isLoading = false,
                            error = "Page not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = NotesPageUiState(
                    isLoading = false,
                    error = e.message ?: "Failed to load page"
                )
            }
        }
    }

    fun updateContent(content: String) {
        _uiState.value = _uiState.value.copy(content = content)
    }

    fun save(spaceId: String) {
        val pageId = currentPageId ?: return
        val currentContent = _uiState.value.content

        viewModelScope.launch {
            _saving.value = true
            try {
                // Get the current page and update it
                repo.getPageById(pageId).collect { page ->
                    if (page != null) {
                        val updatedPage = page.copy(content = currentContent)
                        
                        // Update locally first
                        repo.update(updatedPage)
                        
                        // Then sync to Firebase
                        try {
                            firebaseSyncService.updatePageInFirebase(updatedPage)
                        } catch (e: Exception) {
                            // Handle Firebase sync error gracefully
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to save")
            } finally {
                _saving.value = false
            }
        }
    }
}
