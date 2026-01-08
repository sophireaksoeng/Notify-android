package com.team.notify.taskflow.data.repository.interfaces

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    suspend fun setOnboardingCompleted(completed: Boolean)
    fun hasCompletedOnboarding(): Flow<Boolean>
}
