package com.team.notify.taskflow.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.notify.taskflow.mappers.toUiModel
import com.team.notify.taskflow.repository.inmemory.DEFAULT_SPACE_ID
import com.team.notify.taskflow.repository.inmemory.InMemoryTaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TaskViewModel : ViewModel() {
    private val repo = InMemoryTaskRepository()

    val tasksFlow = repo.getTasksForSpace(DEFAULT_SPACE_ID)
        .map { list -> list.map { it.toUiModel() } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}