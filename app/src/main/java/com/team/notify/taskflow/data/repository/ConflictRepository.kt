package com.team.notify.taskflow.data.repository

import com.team.notify.taskflow.data.dao.ConflictDao
import javax.inject.Inject

class ConflictRepository @Inject constructor(
    private val conflictDao: ConflictDao
) {
    fun observeConflicts() = conflictDao.getUnresolved()

    suspend fun resolve(conflictId: String) {
        conflictDao.markResolved(conflictId)
    }
}
