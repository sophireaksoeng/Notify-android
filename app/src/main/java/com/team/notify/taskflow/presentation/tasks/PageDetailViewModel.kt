package com.team.notify.taskflow.presentation.pages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.taskflow.repository.interfaces.PageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PageDetailViewModel @Inject constructor(
    private val repo: PageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PageDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun load(pageId: String?) {
        if (pageId.isNullOrBlank()) return
        viewModelScope.launch {
            repo.getPageById(pageId).collect { entity ->
                entity?.let {
                    _uiState.value = PageDetailUiState(
                        id = it.id,
                        title = it.title,
                        description = it.content,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt
                    )
                }
            }
        }
    }

    fun updateTitle(newTitle: String) {
        _uiState.value = _uiState.value.copy(title = newTitle)
    }

    fun updateDescription(newDesc: String) {
        _uiState.value = _uiState.value.copy(description = newDesc)
    }

    fun save(spaceId: String) {
        viewModelScope.launch {
            val s = _uiState.value
            val now = System.currentTimeMillis()
            val id = if (s.id.isBlank()) UUID.randomUUID().toString() else s.id
            val entity = PageEntity(
                id = id,
                spaceId = spaceId,
                title = s.title,
                content = s.description,
                createdAt = if (s.createdAt == 0L) now else s.createdAt,
                updatedAt = now
            )
            repo.insert(entity)
        }
    }
}