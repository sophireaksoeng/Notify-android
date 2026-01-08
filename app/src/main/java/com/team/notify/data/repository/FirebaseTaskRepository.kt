package com.team.notify.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.team.notify.taskflow.data.entities.TaskEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseTaskRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    private val tasksCollection = firestore.collection("tasks")
    
    fun getTasksForSpaceFlow(spaceId: String): Flow<List<TaskEntity>> = callbackFlow {
        if (auth.currentUser == null) {
            trySend(emptyList())
            close(Exception("User not authenticated"))
            return@callbackFlow
        }
        
        val subscription = tasksCollection
            .whereEqualTo("spaceId", spaceId)
            .whereEqualTo("ownerId", auth.currentUser!!.uid)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val tasks = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(TaskEntity::class.java)?.copy(
                            id = doc.id
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(tasks)
            }
        
        awaitClose { subscription.remove() }
    }
    
    fun getTasksForPageFlow(pageId: String): Flow<List<TaskEntity>> = callbackFlow {
        if (auth.currentUser == null) {
            trySend(emptyList())
            close(Exception("User not authenticated"))
            return@callbackFlow
        }
        
        val subscription = tasksCollection
            .whereEqualTo("pageId", pageId)
            .whereEqualTo("ownerId", auth.currentUser!!.uid)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val tasks = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(TaskEntity::class.java)?.copy(
                            id = doc.id
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(tasks)
            }
        
        awaitClose { subscription.remove() }
    }
    
    suspend fun createTask(task: TaskEntity): Result<String> {
        return try {
            if (auth.currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            val docRef = tasksCollection.add(task).await()
            
            // Update the document with the generated ID
            docRef.update("id", docRef.id).await()
            
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateTask(task: TaskEntity): Result<Unit> {
        return try {
            if (auth.currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            tasksCollection.document(task.id).set(task).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            if (auth.currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            tasksCollection.document(taskId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun syncTasksForSpace(spaceId: String): Result<List<TaskEntity>> {
        return try {
            if (auth.currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            val snapshot = tasksCollection
                .whereEqualTo("spaceId", spaceId)
                .whereEqualTo("userId", auth.currentUser!!.uid)
                .get()
                .await()
            
            val tasks = snapshot.documents.mapNotNull { doc ->
                doc.toObject(TaskEntity::class.java)?.copy(id = doc.id)
            }
            
            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun syncTasksForPage(pageId: String): Result<List<TaskEntity>> {
        return try {
            if (auth.currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            val snapshot = tasksCollection
                .whereEqualTo("pageId", pageId)
                .whereEqualTo("userId", auth.currentUser!!.uid)
                .get()
                .await()
            
            val tasks = snapshot.documents.mapNotNull { doc ->
                doc.toObject(TaskEntity::class.java)?.copy(id = doc.id)
            }
            
            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
