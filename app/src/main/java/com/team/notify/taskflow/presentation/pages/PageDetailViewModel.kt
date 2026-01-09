package com.team.notify.taskflow.presentation.pages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.taskflow.data.repository.interfaces.PageRepository
import com.team.notify.taskflow.domain.model.Block
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
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

    private var autoSaveJob: Job? = null

    fun load(pageId: String?) {
        if (pageId.isNullOrBlank()) {
            _uiState.update {
                PageDetailUiState(
                    id = UUID.randomUUID().toString(),
                    title = "",
                    description = "",
                    content = "",
                    version = 0,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    hasConflict = false
                )
            }
            return
        }

        viewModelScope.launch {
            repo.getPageById(pageId)
                .distinctUntilChanged()
                .collect { entity ->
                    entity?.let { page ->
                        _uiState.update { current ->
                            current.copy(
                                id = page.id,
                                title = page.title,
                                description = page.content ?: "",
                                version = page.version,
                                createdAt = page.createdAt,
                                updatedAt = page.updatedAt,
                                content = page.content ?: "",
                                hasConflict = page.hasConflict ?: false,
                                spaceId = page.spaceId
                            )
                        }
                    }
                }
        }
    }

    fun onBlocksChanged(blocks: List<Block>) {
        val newContentString = serializeBlocks(blocks)
        _uiState.update { it.copy(content = newContentString) }
        triggerAutoSave()
    }

    fun updateTitle(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
        triggerAutoSave()
    }

    fun updateDescription(newDesc: String) {
        _uiState.update { it.copy(description = newDesc, content = newDesc) }
        triggerAutoSave()
    }

    fun updateContent(newContent: String) {
        _uiState.update { it.copy(content = newContent) }
        triggerAutoSave()
    }

    fun save(spaceId: String) {
        autoSaveJob?.cancel()
        viewModelScope.launch {
            _saving.value = true
            saveToDb(spaceId)
            _saving.value = false
        }
    }

    private fun triggerAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            _saving.value = true
            delay(1000) // Debounce delay

            // Fallback to "default" if spaceId is missing to prevent data loss
            val targetSpaceId = _uiState.value.spaceId ?: "default"
            saveToDb(targetSpaceId)

            _saving.value = false
        }
    }

    private suspend fun saveToDb(spaceId: String) {
        val currentState = _uiState.value
        val now = System.currentTimeMillis()
        val id = currentState.id.ifBlank { UUID.randomUUID().toString() }

        val entity = PageEntity(
            id = id,
            spaceId = spaceId,
            title = currentState.title,
            content = currentState.content,
            createdAt = if (currentState.createdAt == 0L) now else currentState.createdAt,
            updatedAt = now,
            hasConflict = currentState.hasConflict,
            version = currentState.version + 1
        )

        repo.insert(entity)

        _uiState.update {
            it.copy(
                id = id,
                spaceId = spaceId,
                createdAt = entity.createdAt,
                updatedAt = now,
                version = entity.version
            )
        }
    }

    private fun serializeBlocks(blocks: List<Block>): String {
        // Implementation depends on your serialization library (Gson/Moshi)
        // Example using basic toString for now; replace with actual JSON serialization
        return blocks.toString()
    }
}