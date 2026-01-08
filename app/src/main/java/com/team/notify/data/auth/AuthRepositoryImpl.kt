package com.team.notify.data.auth

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AuthRepository {

    override val authState: Flow<FirebaseUser?> = callbackFlow {
        val auth = firebaseAuthOrNull()
        if (auth == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }

        auth.addAuthStateListener(listener)
        trySend(auth.currentUser)

        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun login(email: String, password: String) {
        val auth = firebaseAuthOrNull()
            ?: throw IllegalStateException("Firebase is not configured. Add google-services.json.")
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    override suspend fun register(email: String, password: String) {
        val auth = firebaseAuthOrNull()
            ?: throw IllegalStateException("Firebase is not configured. Add google-services.json.")
        auth.createUserWithEmailAndPassword(email.trim(), password).await()
    }

    override fun logout() {
        firebaseAuthOrNull()?.signOut()
    }

    private fun firebaseAuthOrNull(): FirebaseAuth? {
        val apps = FirebaseApp.getApps(context)
        if (apps.isEmpty()) return null

        return try {
            FirebaseAuth.getInstance()
        } catch (_: Exception) {
            null
        }
    }
}
