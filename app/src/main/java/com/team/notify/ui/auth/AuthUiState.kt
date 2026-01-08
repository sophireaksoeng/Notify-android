package com.team.notify.ui.auth

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val username: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val userId: String? = null,
    val userEmail: String? = null,
)
