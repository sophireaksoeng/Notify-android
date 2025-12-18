package com.team.notify.taskflow.presentation.pages

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.notify.taskflow.data.repository.interfaces.PageRepository
import com.team.notify.taskflow.data.sync.RealtimeSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PageListViewModel @Inject constructor(
    private val repo: PageRepository,
    private val realtime: RealtimeSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<PageListUiState>(PageListUiState.Loading)
    val uiState: StateFlow<PageListUiState> = _uiState

    private var currentSpaceId: String = "space-1"

    val pages = repo.getPagesForSpace(currentSpaceId)
        .stateIn(viewModelScope, SharingStarted.Companion.Lazily, emptyList())

    init {
        observePages()
    }

    fun onEnter(spaceId: String) {
        realtime.start(spaceId)
    }

    override fun onCleared() {
        realtime.stop()
        super.onCleared()
    }

    private fun observePages() {
        viewModelScope.launch {
            repo.getPagesForSpace(currentSpaceId)
                .map { it.sortedBy { p -> p.title } }
                .onStart { _uiState.value = PageListUiState.Loading }
                .catch { e ->
                    Log.e("PageListVM", "Error observing pages", e)
                    _uiState.value = PageListUiState.Error(e.message ?: "Unknown")
                }
                .collect { list ->
                    _uiState.value = if (list.isEmpty()) {
                        PageListUiState.Empty
                    } else {
                        PageListUiState.Data(list)
                    }
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                repo.pullRemoteChanges(currentSpaceId)
            } catch (e: Exception) {
                Log.w("PageListVM", "refresh failed: ${e.message}")
            }
        }
    }

    fun deletePage(pageId: String) {
        viewModelScope.launch {
            try {
                repo.deleteById(pageId)
            } catch (e: Exception) {
                Log.w("PageListVM", "delete failed: ${e.message}")
            }
        }
    }
}