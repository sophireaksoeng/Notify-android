package com.team.notify.data.sync

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.team.notify.data.repository.FirebasePageRepository
import com.team.notify.data.repository.FirebaseSpaceRepository
import com.team.notify.data.repository.FirebaseTaskRepository
import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.data.local.entity.SpaceEntity
import com.team.notify.taskflow.data.entities.TaskEntity
import com.team.notify.taskflow.data.repository.interfaces.PageRepository
import com.team.notify.taskflow.data.repository.interfaces.SpaceRepository
import com.team.notify.taskflow.data.repository.interfaces.TaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseSyncService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firebaseSpaceRepo: FirebaseSpaceRepository,
    private val firebasePageRepo: FirebasePageRepository,
    private val firebaseTaskRepo: FirebaseTaskRepository,
    private val localSpaceRepo: SpaceRepository,
    private val localPageRepo: PageRepository,
    private val localTaskRepo: TaskRepository
) {
    
    private val syncMutex = Mutex()
    
    suspend fun syncAll(): Result<Unit> {
        return syncMutex.withLock {
            try {
                if (auth.currentUser == null) {
                    return Result.failure(Exception("User not authenticated"))
                }
                
                // Sync spaces first
                val spacesResult = syncSpaces()
                if (spacesResult.isFailure) {
                    return Result.failure(spacesResult.exceptionOrNull() ?: Exception("Failed to sync spaces"))
                }
                
                // Then sync pages and tasks for each space
                val localSpaces = localSpaceRepo.getSpaces().first()
                for (space in localSpaces) {
                    val pagesResult = syncPagesForSpace(space.id)
                    if (pagesResult.isFailure) {
                        continue
                    }
                    
                    val tasksResult = syncTasksForSpace(space.id)
                    if (tasksResult.isFailure) {
                        continue
                    }
                }
                
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun syncSpaces(): Result<List<SpaceEntity>> {
        return try {
            if (auth.currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            // Get remote spaces
            val remoteSpacesResult = firebaseSpaceRepo.syncSpaces()
            if (remoteSpacesResult.isFailure) {
                return Result.failure(remoteSpacesResult.exceptionOrNull() ?: Exception("Failed to fetch remote spaces"))
            }
            
            val remoteSpaces = remoteSpacesResult.getOrThrow()
            
            // Get local spaces
            val localSpaces = localSpaceRepo.getSpaces().first()
            
            // Merge spaces (remote takes precedence)
            val mergedSpaces = mergeSpaces(localSpaces, remoteSpaces)
            
            // Update local database
            for (space in mergedSpaces) {
                localSpaceRepo.insert(space)
            }
            
            Result.success(mergedSpaces)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun syncPagesForSpace(spaceId: String): Result<List<PageEntity>> {
        return try {
            if (auth.currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            // Get remote pages
            val remotePagesResult = firebasePageRepo.syncPagesForSpace(spaceId)
            if (remotePagesResult.isFailure) {
                return Result.failure(remotePagesResult.exceptionOrNull() ?: Exception("Failed to fetch remote pages"))
            }
            
            val remotePages = remotePagesResult.getOrThrow()
            
            // Get local pages
            val localPages = localPageRepo.getPagesForSpace(spaceId).first()
            
            // Merge pages (remote takes precedence)
            val mergedPages = mergePages(localPages, remotePages)
            
            // Update local database
            for (page in mergedPages) {
                localPageRepo.insert(page)
            }
            
            Result.success(mergedPages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun syncTasksForSpace(spaceId: String): Result<List<TaskEntity>> {
        return try {
            if (auth.currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            // Get remote tasks
            val remoteTasksResult = firebaseTaskRepo.syncTasksForSpace(spaceId)
            if (remoteTasksResult.isFailure) {
                return Result.failure(remoteTasksResult.exceptionOrNull() ?: Exception("Failed to fetch remote tasks"))
            }
            
            val remoteTasks = remoteTasksResult.getOrThrow()
            
            // Get local tasks
            val localTasks = localTaskRepo.getTasksForSpace(spaceId).first()
            
            // Merge tasks (remote takes precedence)
            val mergedTasks = mergeTasks(localTasks, remoteTasks)
            
            // Update local database
            for (task in mergedTasks) {
                localTaskRepo.insert(task)
            }
            
            Result.success(mergedTasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun syncTasksForPage(pageId: String): Result<List<TaskEntity>> {
        return try {
            if (auth.currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            // Get remote tasks
            val remoteTasksResult = firebaseTaskRepo.syncTasksForPage(pageId)
            if (remoteTasksResult.isFailure) {
                return Result.failure(remoteTasksResult.exceptionOrNull() ?: Exception("Failed to fetch remote tasks"))
            }
            
            val remoteTasks = remoteTasksResult.getOrThrow()
            
            // Get local tasks for the same space (we need to determine spaceId from page)
            // For now, we'll use the first task's spaceId or default to current space
            val spaceId = remoteTasks.firstOrNull()?.spaceId ?: "notify-db"
            val localTasks = localTaskRepo.getTasksForSpace(spaceId).first()
            
            // Merge tasks (remote takes precedence)
            val mergedTasks = mergeTasks(localTasks, remoteTasks)
            
            // Update local database
            for (task in mergedTasks) {
                localTaskRepo.insert(task)
            }
            
            Result.success(mergedTasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createSpaceInFirebase(space: SpaceEntity): Result<String> {
        return try {
            Log.d("FirebaseSync", "Creating space in Firebase: ${space.name}")
            
            if (auth.currentUser == null) {
                Log.e("FirebaseSync", "User not authenticated")
                // Create locally only if not authenticated
                localSpaceRepo.insert(space.copy(ownerId = auth.currentUser?.uid ?: ""))
                return Result.success(space.id)
            }
            
            Log.d("FirebaseSync", "Current user: ${auth.currentUser!!.uid}")
            
            // Try to create in Firebase first
            val firebaseResult = firebaseSpaceRepo.createSpace(space)
            if (firebaseResult.isFailure) {
                val exception = firebaseResult.exceptionOrNull()
                Log.e("FirebaseSync", "Failed to create space in Firebase: ${exception?.message}")
                
                // Check if it's a permission error
                if (exception?.message?.contains("PERMISSION_DENIED") == true ||
                    exception?.message?.contains("Missing or insufficient permissions") == true) {
                    Log.w("FirebaseSync", "Firebase permissions not configured, creating locally only")
                    // Create locally only if Firebase permissions are not set up
                    localSpaceRepo.insert(space)
                    return Result.success(space.id)
                }
                
                return Result.failure(exception ?: Exception("Failed to create space in Firebase"))
            }
            
            val firebaseId = firebaseResult.getOrThrow()
            Log.d("FirebaseSync", "Space created in Firebase with ID: $firebaseId")
            
            // Create locally with the Firebase ID
            val spaceWithId = space.copy(id = firebaseId, ownerId = auth.currentUser!!.uid)
            localSpaceRepo.insert(spaceWithId)
            
            Log.d("FirebaseSync", "Space created locally with ID: $firebaseId")
            Result.success(spaceWithId.id)
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Exception creating space in Firebase: ${e.message}", e)
            
            // Check if it's a permission error
            if (e.message?.contains("PERMISSION_DENIED") == true ||
                e.message?.contains("Missing or insufficient permissions") == true) {
                Log.w("FirebaseSync", "Firebase permissions not configured, creating locally only")
                // Create locally only if Firebase permissions are not set up
                localSpaceRepo.insert(space)
                return Result.success(space.id)
            }
            
            Result.failure(e)
        }
    }
    
    suspend fun createPageInFirebase(page: PageEntity): Result<String> {
        return try {
            Log.d("FirebaseSync", "Creating page in Firebase: ${page.title}")
            
            if (auth.currentUser == null) {
                Log.e("FirebaseSync", "User not authenticated")
                // Create locally only if not authenticated
                localPageRepo.insert(page.copy(ownerId = auth.currentUser?.uid ?: ""))
                return Result.success(page.id)
            }
            
            Log.d("FirebaseSync", "Current user: ${auth.currentUser!!.uid}")
            
            // Try to create in Firebase first
            val firebaseResult = firebasePageRepo.createPage(page)
            if (firebaseResult.isFailure) {
                val exception = firebaseResult.exceptionOrNull()
                Log.e("FirebaseSync", "Failed to create page in Firebase: ${exception?.message}")
                
                // Check if it's a permission error
                if (exception?.message?.contains("PERMISSION_DENIED") == true ||
                    exception?.message?.contains("Missing or insufficient permissions") == true) {
                    Log.w("FirebaseSync", "Firebase permissions not configured, creating locally only")
                    // Create locally only if Firebase permissions are not set up
                    localPageRepo.insert(page)
                    return Result.success(page.id)
                }
                
                return Result.failure(exception ?: Exception("Failed to create page in Firebase"))
            }
            
            // Create locally with the Firebase ID
            val pageWithId = page.copy(id = firebaseResult.getOrThrow(), ownerId = auth.currentUser!!.uid)
            localPageRepo.insert(pageWithId)
            
            Log.d("FirebaseSync", "Page created successfully with ID: ${pageWithId.id}")
            Result.success(pageWithId.id)
        } catch (e: Exception) {
            Log.e("FirebaseSync", "createPageInFirebase failed", e)
            
            // Check if it's a permission error
            if (e.message?.contains("PERMISSION_DENIED") == true ||
                e.message?.contains("Missing or insufficient permissions") == true) {
                Log.w("FirebaseSync", "Firebase permissions not configured, creating locally only")
                // Create locally only if Firebase permissions are not set up
                localPageRepo.insert(page)
                return Result.success(page.id)
            }
            
            Result.failure(e)
        }
    }
    
    suspend fun createTaskInFirebase(task: TaskEntity): Result<String> {
        return try {
            if (auth.currentUser == null) {
                // Create locally only if not authenticated
                localTaskRepo.insert(task.copy(ownerId = auth.currentUser?.uid ?: ""))
                return Result.success(task.id)
            }
            
            // Try to create in Firebase first
            val firebaseResult = firebaseTaskRepo.createTask(task)
            if (firebaseResult.isFailure) {
                val exception = firebaseResult.exceptionOrNull()
                
                // Check if it's a permission error
                if (exception?.message?.contains("PERMISSION_DENIED") == true ||
                    exception?.message?.contains("Missing or insufficient permissions") == true) {
                    Log.w("FirebaseSync", "Firebase permissions not configured, creating locally only")
                    // Create locally only if Firebase permissions are not set up
                    localTaskRepo.insert(task.copy(ownerId = auth.currentUser?.uid ?: ""))
                    return Result.success(task.id)
                }
                
                return Result.failure(exception ?: Exception("Failed to create task in Firebase"))
            }
            
            // Create locally with the Firebase ID
            val taskWithId = task.copy(id = firebaseResult.getOrThrow(), ownerId = auth.currentUser!!.uid)
            localTaskRepo.insert(taskWithId)
            
            Result.success(taskWithId.id)
        } catch (e: Exception) {
            // Check if it's a permission error
            if (e.message?.contains("PERMISSION_DENIED") == true ||
                e.message?.contains("Missing or insufficient permissions") == true) {
                Log.w("FirebaseSync", "Firebase permissions not configured, creating locally only")
                // Create locally only if Firebase permissions are not set up
                localTaskRepo.insert(task.copy(ownerId = auth.currentUser?.uid ?: ""))
                return Result.success(task.id)
            }
            
            Result.failure(e)
        }
    }
    
    suspend fun updatePageInFirebase(page: PageEntity): Result<Unit> {
        return try {
            if (auth.currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            firebasePageRepo.updatePage(page)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deletePageInFirebase(pageId: String): Result<Unit> {
        return try {
            if (auth.currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            firebasePageRepo.deletePage(pageId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateTaskInFirebase(task: TaskEntity): Result<Unit> {
        return try {
            if (auth.currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            firebaseTaskRepo.updateTask(task)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteTaskInFirebase(taskId: String): Result<Unit> {
        return try {
            if (auth.currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            firebaseTaskRepo.deleteTask(taskId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun mergeSpaces(localSpaces: List<SpaceEntity>, remoteSpaces: List<SpaceEntity>): List<SpaceEntity> {
        val mergedMap = mutableMapOf<String, SpaceEntity>()
        
        // Add remote spaces first (they take precedence)
        remoteSpaces.forEach { space ->
            mergedMap[space.id] = space
        }
        
        // Add local spaces that don't exist remotely
        localSpaces.forEach { space ->
            if (!mergedMap.containsKey(space.id)) {
                mergedMap[space.id] = space
            }
        }
        
        return mergedMap.values.toList()
    }
    
    private fun mergePages(localPages: List<PageEntity>, remotePages: List<PageEntity>): List<PageEntity> {
        val mergedMap = mutableMapOf<String, PageEntity>()
        
        // Add remote pages first (they take precedence)
        remotePages.forEach { page ->
            mergedMap[page.id] = page
        }
        
        // Add local pages that don't exist remotely
        localPages.forEach { page ->
            if (!mergedMap.containsKey(page.id)) {
                mergedMap[page.id] = page
            }
        }
        
        return mergedMap.values.toList()
    }
    
    private fun mergeTasks(localTasks: List<TaskEntity>, remoteTasks: List<TaskEntity>): List<TaskEntity> {
        val mergedMap = mutableMapOf<String, TaskEntity>()
        
        // Add remote tasks first (they take precedence)
        remoteTasks.forEach { task ->
            mergedMap[task.id] = task
        }
        
        // Add local tasks that don't exist remotely
        localTasks.forEach { task ->
            if (!mergedMap.containsKey(task.id)) {
                mergedMap[task.id] = task
            }
        }
        
        return mergedMap.values.toList()
    }
}
