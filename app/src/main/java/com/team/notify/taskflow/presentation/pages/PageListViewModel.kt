package com.team.notify.taskflow.presentation.pages

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.taskflow.data.repository.interfaces.PageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PageListViewModel @Inject constructor(
    private val repo: PageRepository
) : ViewModel() {

    private val spaceIdFlow = MutableStateFlow("default-space")

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

    fun createPage(title: String = "Untitled", onCreated: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val id = UUID.randomUUID().toString()
                val now = System.currentTimeMillis()
                repo.insert(
                    PageEntity(
                        id = id,
                        spaceId = spaceIdFlow.value,
                        title = title,
                        content = "",
                        version = 1,
                        createdAt = now,
                        updatedAt = now
                    )
                )
                onCreated(id)
            } catch (e: Exception) {
                Log.e("PageListVM", "createPage failed", e)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            runCatching { repo.pullRemoteChanges(spaceIdFlow.value) }
                .onFailure { Log.w("PageListVM", "refresh failed: ${it.message}") }
        }
    }

    fun deletePage(pageId: String) {
        viewModelScope.launch {
            runCatching { repo.deleteById(pageId) }
                .onFailure { Log.w("PageListVM", "delete failed: ${it.message}") }
        }
    }
}
