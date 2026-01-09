package com.team.notify.data.remote

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.team.notify.data.local.dao.PageDao
import com.team.notify.data.local.dao.SpaceDao
import com.team.notify.data.local.dao.TaskDao
import com.team.notify.data.local.entity.PageEntity
import com.team.notify.data.local.entity.SpaceEntity
import com.team.notify.data.local.entity.TaskEntity
import com.team.notify.work.DueReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreSyncer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val spaceDao: SpaceDao,
    private val pageDao: PageDao,
    private val taskDao: TaskDao,
) {

    suspend fun sync() {
        try {
            val auth = firebaseAuthOrNull()
                ?: throw IllegalStateException("Firebase is not configured. Add google-services.json.")
            val firestore = firestoreOrNull()
                ?: throw IllegalStateException("Firestore is not available.")
            val currentUser = auth.currentUser
                ?: throw IllegalStateException("No current user found.")
            val uid = currentUser.uid
                ?: throw IllegalStateException("User UID is null.")

            Log.d("FirestoreSyncer", "Starting sync for user: $uid")
            Log.d("FirestoreSyncer", "User email: ${currentUser.email}")
            
            pushUnsynced(firestore, uid)
            pullRemote(firestore, uid)
            
            Log.d("FirestoreSyncer", "Sync completed successfully for user: $uid")
        } catch (e: Exception) {
            Log.e("FirestoreSyncer", "Sync failed", e)
            throw e // Re-throw to let the caller handle it
        }
    }

    private fun userRoot(firestore: FirebaseFirestore, uid: String) = firestore.collection("users").document(uid)

    private suspend fun pushUnsynced(firestore: FirebaseFirestore, uid: String) {
        val root = userRoot(firestore, uid)
        Log.d("FirestoreSyncer", "Pushing unsynced data for user: $uid")

        try {
            val spaces = spaceDao.getUnsynced()
            Log.d("FirestoreSyncer", "Found ${spaces.size} unsynced spaces")
            for (space in spaces) {
                try {
                    val spacePath = "users/$uid/spaces/${space.id}"
                    Log.d("FirestoreSyncer", "Syncing space to: $spacePath")
                    root.collection("spaces").document(space.id)
                        .set(space.toMap())
                        .await()
                    spaceDao.markSynced(space.id)
                    Log.d("FirestoreSyncer", "Successfully synced space: ${space.id}")
                } catch (e: Exception) {
                    Log.e("FirestoreSyncer", "Failed to sync space ${space.id}", e)
                    // Continue with other spaces even if one fails
                }
            }

            val pages = pageDao.getUnsynced()
            Log.d("FirestoreSyncer", "Found ${pages.size} unsynced pages")
            for (page in pages) {
                try {
                    val pagePath = "users/$uid/pages/${page.id}"
                    Log.d("FirestoreSyncer", "Syncing page to: $pagePath")
                    root.collection("pages").document(page.id)
                        .set(page.toMap())
                        .await()
                    pageDao.markSynced(page.id)
                    Log.d("FirestoreSyncer", "Successfully synced page: ${page.id}")
                } catch (e: Exception) {
                    Log.e("FirestoreSyncer", "Failed to sync page ${page.id}", e)
                    // Continue with other pages even if one fails
                }
            }

            val tasks = taskDao.getUnsynced()
            Log.d("FirestoreSyncer", "Found ${tasks.size} unsynced tasks")
            for (task in tasks) {
                try {
                    val taskPath = "users/$uid/tasks/${task.id}"
                    Log.d("FirestoreSyncer", "Syncing task to: $taskPath")
                    root.collection("tasks").document(task.id)
                        .set(task.toMap())
                        .await()
                    taskDao.markSynced(task.id)
                    Log.d("FirestoreSyncer", "Successfully synced task: ${task.id}")
                } catch (e: Exception) {
                    Log.e("FirestoreSyncer", "Failed to sync task ${task.id}", e)
                    // Continue with other tasks even if one fails
                }
            }
        } catch (e: Exception) {
            Log.e("FirestoreSyncer", "Failed to get unsynced data", e)
            throw e // Re-throw this one as it's a critical failure
        }
    }

    private suspend fun pullRemote(firestore: FirebaseFirestore, uid: String) {
        val root = userRoot(firestore, uid)

        val remoteSpaces = root.collection("spaces").get().await()
        for (doc in remoteSpaces.documents) {
            val remote = doc.toSpaceEntity() ?: continue
            val local = spaceDao.getById(remote.id)
            if (local == null || remote.updatedAt > local.updatedAt) {
                spaceDao.upsert(remote.copy(isSynced = true))
            }
        }

        val remotePages = root.collection("pages").get().await()
        for (doc in remotePages.documents) {
            val remote = doc.toPageEntity() ?: continue
            val local = pageDao.getById(remote.id)
            if (local == null || remote.updatedAt > local.updatedAt) {
                pageDao.upsert(remote.copy(isSynced = true))
            }
        }

        val remoteTasks = root.collection("tasks").get().await()
        for (doc in remoteTasks.documents) {
            val remote = doc.toTaskEntity() ?: continue
            val local = taskDao.getById(remote.id)
            if (local == null || remote.updatedAt > local.updatedAt) {
                taskDao.upsert(remote.copy(isSynced = true))

                if (remote.isDeleted) {
                    DueReminderScheduler.cancel(context, remote.id)
                }
            }
        }
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

    private fun firestoreOrNull(): FirebaseFirestore? {
        val apps = FirebaseApp.getApps(context)
        if (apps.isEmpty()) return null

        return try {
            FirebaseFirestore.getInstance("notify-db")
        } catch (_: Exception) {
            null
        }
    }
}

private fun SpaceEntity.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "name" to name,
        "ownerId" to ownerId,
        "updatedAt" to updatedAt,
        "isDeleted" to isDeleted,
        "deletedAt" to deletedAt,
    )
}

private fun PageEntity.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "spaceId" to spaceId,
        "title" to title,
        "content" to content,
        "updatedAt" to updatedAt,
        "isDeleted" to isDeleted,
        "deletedAt" to deletedAt,
    )
}

private fun TaskEntity.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "pageId" to pageId,
        "title" to title,
        "status" to status,
        "dueDate" to dueDate,
        "assigneeId" to assigneeId,
        "priority" to priority,
        "updatedAt" to updatedAt,
        "isDeleted" to isDeleted,
        "deletedAt" to deletedAt,
    )
}
