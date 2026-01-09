package com.team.notify.taskflow.presentation.tasks

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.notify.taskflow.data.entities.TaskEntity
import com.team.notify.taskflow.data.repository.interfaces.TaskRepository
import com.team.notify.taskflow.data.sync.SyncScheduler
import com.team.notify.taskflow.model.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val repo: TaskRepository,
    savedStateHandle: SavedStateHandle,
    private val syncScheduler: SyncScheduler
) : ViewModel() {

    private val currentSpaceId: String =
        savedStateHandle.get<String>("spaceId")?.takeIf { it.isNotBlank() } ?: "notify-db"

    val tasks: StateFlow<List<TaskEntity>> = repo.getTasksForSpace(currentSpaceId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        Log.d("TaskListVM", "Initialized with spaceId=$currentSpaceId")
    }

    fun createTask(title: String) {
        viewModelScope.launch {
            val newTask = TaskEntity(
                id = UUID.randomUUID().toString(),
                spaceId = currentSpaceId,
                pageId = "inbox",
                title = title,
                description = "",
                status = TaskStatus.TODO,
                isCompleted = false,
                assigneeId = null,
                updatedAt = System.currentTimeMillis()
                // REMOVED: dueDate and createdAt (fields missing in your Entity)
            )
            repo.upsert(newTask)
        }
    }

    fun toggleCompleted(taskId: String) {
        viewModelScope.launch {
            val task = repo.getTaskById(taskId).first() ?: return@launch
            val newCompleted = !task.isCompleted
            val newStatus = if (newCompleted) TaskStatus.DONE else TaskStatus.TODO

            repo.upsert(
                task.copy(
                    isCompleted = newCompleted,
                    status = newStatus,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                // FIX: Your repo expects a String ID, not the object
                repo.delete(taskId)
            } catch (e: Exception) {
                Log.e("TaskListVM", "Error deleting task: ${e.message}")
            }
        }
    }

    fun refresh(context: android.content.Context) {
        syncScheduler.scheduleNow(context)
    }
}