package com.team.notify.taskflow.presentation.tasks

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.team.notify.taskflow.mappers.toUiModel
import com.team.notify.taskflow.model.Task
import com.team.notify.taskflow.model.TaskStatus
import com.team.notify.taskflow.repository.interfaces.TaskRepository
import com.team.notify.taskflow.data.entities.TaskEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val repo: TaskRepository,
    private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<Task?>(null)
    val uiState = _uiState.asStateFlow()

    fun load(taskId: String) {
        viewModelScope.launch {
            repo.getTaskById(taskId)
                .map { it?.toUiModel() }
                .collect { task ->
                    _uiState.value = task
                }
        }
    }

    fun updateTitle(newTitle: String) {
        val t = _uiState.value ?: return
        val updated = t.copy(title = newTitle)
        _uiState.value = updated
    }

    fun updateDescription(newDesc: String) {
        val t = _uiState.value ?: return
        _uiState.value = t.copy(description = newDesc)
    }

    fun updateDueDate(newDueMillis: Long) {
        val t = _uiState.value ?: return
        _uiState.value = t.copy(dueDate = java.util.Date(newDueMillis))
    }

    fun save(taskId: String) {
        val t = _uiState.value ?: return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val entity = TaskEntity(
                id = taskId.ifEmpty { UUID.randomUUID().toString() },
                spaceId = "",
                title = t.title,
                description = t.description,
                status = t.status.name,
                assigneeId = null,
                labelsCsv = null,
                dueAt = t.dueDate.time,
                createdAt = now,
                updatedAt = now
            )
            repo.insert(entity)
            // schedule reminder
            scheduleReminder(entity.id, entity.dueAt ?: return@launch)
        }
    }

    private fun scheduleReminder(taskId: String, dueAtMillis: Long) {
        // compute delay
        val now = System.currentTimeMillis()
        val delayMs = dueAtMillis - now
        if (delayMs <= 0) return

        val data = Data.Builder()
            .putString("taskId", taskId)
            .build()

        val request = OneTimeWorkRequestBuilder<com.team.notify.taskflow.presentation.tasks.ReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(appContext).enqueueUniqueWork(
            "reminder_$taskId",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelReminder(taskId: String) {
        WorkManager.getInstance(appContext).cancelUniqueWork("reminder_$taskId")
    }
}
