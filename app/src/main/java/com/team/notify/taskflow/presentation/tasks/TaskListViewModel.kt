package com.team.notify.taskflow.presentation.tasks

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.notify.taskflow.model.TaskStatus
import com.team.notify.taskflow.mappers.toUiModel
import com.team.notify.taskflow.repository.interfaces.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val repo: TaskRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val spaceId: String = savedStateHandle.get<String>("spaceId") ?: ""

    private val currentSpaceId = "default-space"
    val tasks = repo.getTasksForSpace(currentSpaceId)
        .map { list -> list.map { it.toUiModel() } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _uiState = MutableStateFlow<TaskListUiState>(TaskListUiState.Loading)
    val uiState: StateFlow<TaskListUiState> = _uiState

    init {
        if (currentSpaceId.isBlank()) {
            Log.e("TaskListVM", "Blank spaceId")
            _uiState.value = TaskListUiState.Error("Missing spaceId")
        } else {
            observeTasks()
        }
    }

    private fun observeTasks() {
        viewModelScope.launch {
            repo.getTasksForSpace(currentSpaceId)
                .onStart {
                    _uiState.value = TaskListUiState.Loading
                }
                .catch { e ->
                    Log.e("TaskListVM", "Fetch error", e)
                    _uiState.value = TaskListUiState.Error(e.message ?: "Unknown error")
                }
                .collectLatest { entities ->
                    val tasks = entities.map { it.toUiModel() }
                    _uiState.value = if (tasks.isEmpty()) {
                        TaskListUiState.Empty
                    } else {
                        TaskListUiState.Data(tasks)
                    }
                }
        }
    }

    fun refresh() {
        if (currentSpaceId.isBlank()) {
            _uiState.value = TaskListUiState.Error("Missing spaceId")
            return
        }
        observeTasks()
    }

    fun updateStatus(taskId: String, newStatus: TaskStatus) {
        viewModelScope.launch {
            val entity = repo.getTaskById(taskId).first() ?: return@launch
            repo.insert(entity.copy(
                status = newStatus.name,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }
}
