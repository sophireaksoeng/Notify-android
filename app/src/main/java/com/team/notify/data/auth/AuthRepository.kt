package com.team.notify.data.auth

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val authState: Flow<FirebaseUser?>

    suspend fun login(email: String, password: String)
    suspend fun register(email: String, password: String)
    fun logout()
}
