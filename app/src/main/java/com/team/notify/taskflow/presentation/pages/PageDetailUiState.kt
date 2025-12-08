package com.team.notify.taskflow.presentation.pages

data class PageDetailUiState(
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
