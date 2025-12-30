package com.team.notify.taskflow.presentation.pages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.taskflow.data.repository.interfaces.PageRepository
import com.team.notify.taskflow.domain.model.Block
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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

    private val _saving = MutableStateFlow(false)
    val saving = _saving.asStateFlow()

    fun load(pageId: String?) {
        if (pageId.isNullOrBlank()) {
            _uiState.value = PageDetailUiState(
                id = "",
                title = "",
                description = "",
                version = 0,
                createdAt = 0L,
                updatedAt = 0L,
                content = "",
                hasConflict = false
            )
            return
        }

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

    fun onBlocksChanged(blocks: List<Block>) {
        _saving.value = true
        viewModelScope.launch {
            delay(600)
            saveToDb(spaceId = _uiState.value.spaceId ?: "")
            _saving.value = false
        }
    }

    fun updateTitle(newTitle: String) {
        _uiState.value = _uiState.value.copy(title = newTitle)
    }

    fun updateDescription(newDesc: String) {
        _uiState.value = _uiState.value.copy(description = newDesc, content = newDesc)
    }

    fun updateContent(newContent: String) {
        updateDescription(newContent)
    }

    fun save(spaceId: String) {
        viewModelScope.launch {
            saveToDb(spaceId)
        }
    }

    private suspend fun saveToDb(spaceId: String) {
        val s = _uiState.value
        val now = System.currentTimeMillis()
        val id = if (s.id.isBlank()) UUID.randomUUID().toString() else s.id

        val entity = PageEntity(
            id = id,
            spaceId = spaceId,
            title = s.title,
            content = s.description,
            createdAt = if (s.createdAt == 0L) now else s.createdAt,
            updatedAt = now,
            hasConflict = s.hasConflict
        )

        repo.insert(entity)

        _uiState.value = s.copy(
            id = id,
            createdAt = entity.createdAt,
            updatedAt = now
        )
    }
}
