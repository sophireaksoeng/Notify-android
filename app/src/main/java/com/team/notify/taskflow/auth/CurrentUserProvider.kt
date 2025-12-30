package com.team.notify.taskflow.auth

interface CurrentUserProvider {
    fun getCurrentUserId(): String?
}
