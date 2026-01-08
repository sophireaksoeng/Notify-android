package com.team.notify.taskflow.presentation.pages

import com.team.notify.taskflow.data.entities.PageEntity

sealed interface PageListUiState {
    object Loading : PageListUiState
    object Empty : PageListUiState
    data class Error(val message: String) : PageListUiState
    data class Data(val pages: List<PageEntity>) : PageListUiState
}
