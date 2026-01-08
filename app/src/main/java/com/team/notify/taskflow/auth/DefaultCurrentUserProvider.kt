package com.team.notify.taskflow.auth

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultCurrentUserProvider @Inject constructor() : CurrentUserProvider {
    override fun getCurrentUserId(): String? {
        return null
    }
}
