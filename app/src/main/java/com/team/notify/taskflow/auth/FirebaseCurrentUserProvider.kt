package com.team.notify.taskflow.auth

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class FirebaseCurrentUserProvider @Inject constructor(
    private val auth: FirebaseAuth
) : CurrentUserProvider {
    override fun getCurrentUserId(): String? = auth.currentUser?.uid
}
