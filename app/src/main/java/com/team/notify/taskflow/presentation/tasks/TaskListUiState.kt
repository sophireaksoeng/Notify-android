package com.team.notify.taskflow.presentation.tasks

import com.team.notify.taskflow.model.Task

sealed interface TaskListUiState {
    object Loading : TaskListUiState
    object Empty : TaskListUiState
    data class Error(val message: String) : TaskListUiState
    data class Data(val tasks: List<Task>) : TaskListUiState
}