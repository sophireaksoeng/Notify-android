package com.team.notify.taskflow.presentation.pages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.taskflow.data.entities.PageHistoryEntity
import com.team.notify.taskflow.data.repository.interfaces.PageRepository
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
                        version = it.version,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                        content = it.content ?: "",
                        hasConflict = it.hasConflict ?: false
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

    fun updateContent(newContent: String) {
        updateDescription(newContent)
    }

    fun save(spaceId: String) {
        viewModelScope.launch {
            val s = _uiState.value
            val now = System.currentTimeMillis()
            val id = if (s.id.isBlank()) UUID.randomUUID().toString() else s.id

            val newVersion = if (s.version <= 0) 1 else s.version + 1

            val entity = PageEntity(
                id = id,
                spaceId = spaceId,
                title = s.title,
                content = s.description,
                version = newVersion,
                createdAt = if (s.createdAt == 0L) now else s.createdAt,
                updatedAt = now
            )

            repo.insert(entity)

            repo.insertHistory(
                PageHistoryEntity(
                    id = UUID.randomUUID().toString(),
                    pageId = id,
                    version = newVersion,
                    content = s.description.orEmpty(),
                    timestamp = now
                )
            )

            _uiState.value = s.copy(
                id = id,
                version = newVersion,
                createdAt = entity.createdAt,
                updatedAt = now
            )
        }
    }
}