package com.team.notify.taskflow.presentation.tasks

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.notify.taskflow.mappers.toUiModel
import com.team.notify.taskflow.presentation.tasks.TaskListUiState
import com.team.notify.taskflow.data.repository.interfaces.TaskRepository
import com.team.notify.taskflow.data.sync.SyncScheduler
import com.team.notify.taskflow.model.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val repo: TaskRepository,
    savedStateHandle: SavedStateHandle,
    private val syncScheduler: SyncScheduler
) : ViewModel() {

    private val currentSpaceId: String =
        savedStateHandle.get<String>("spaceId")?.takeIf { it.isNotBlank() } ?: "notify-db"

    val tasks = repo.getTasksForSpace(currentSpaceId)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _uiState = MutableStateFlow<TaskListUiState>(TaskListUiState.Loading)
    val uiState: StateFlow<TaskListUiState> = _uiState

    init {
        Log.d("TaskListVM", "Using spaceId=$currentSpaceId")
        observeTasks()
    }

    private fun observeTasks() {
        viewModelScope.launch {
            repo.getTasksForSpace(currentSpaceId)
                .onStart { _uiState.value = TaskListUiState.Loading }
                .catch { e ->
                    Log.e("TaskListVM", "Fetch error", e)
                    _uiState.value = TaskListUiState.Error(e.message ?: "Unknown error")
                }
                .collectLatest { entities ->
                    val uiTasks = entities.map { it.toUiModel() }
                    _uiState.value = if (uiTasks.isEmpty()) {
                        TaskListUiState.Empty
                    } else {
                        TaskListUiState.Data(uiTasks)
                    }
                }
        }
    }

    fun refresh(context: android.content.Context) {
        syncScheduler.scheduleNow(context)
        observeTasks()
    }

    fun updateStatus(taskId: String, newStatus: TaskStatus) {
        viewModelScope.launch {
            val entity = repo.getTaskById(taskId).first() ?: return@launch
            repo.upsert(
                entity.copy(
                    status = newStatus,
                    isCompleted = (newStatus == TaskStatus.DONE),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
}
