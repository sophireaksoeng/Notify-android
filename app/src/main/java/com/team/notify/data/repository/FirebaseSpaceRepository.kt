package com.team.notify.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.team.notify.data.local.entity.SpaceEntity
import com.team.notify.data.remote.FirestoreSpace
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseSpaceRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    private val spacesCollection = firestore.collection("spaces")
    
    fun getSpacesFlow(): Flow<List<SpaceEntity>> = callbackFlow {
        if (auth.currentUser == null) {
            trySend(emptyList())
            close(Exception("User not authenticated"))
            return@callbackFlow
        }
        
        val subscription = spacesCollection
            .whereEqualTo("ownerId", auth.currentUser!!.uid)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val spaces = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(SpaceEntity::class.java)?.copy(
                            id = doc.id
                        )
                    } catch (e: Exception) {
                        Log.w("FirebaseSpaceRepo", "Error parsing space document", e)
                        null
                    }
                } ?: emptyList()
                
                trySend(spaces)
            }
        
        awaitClose { subscription.remove() }
    }
    
    suspend fun createSpace(space: SpaceEntity): Result<String> {
        return try {
            if (auth.currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            // Create space with ownerId for Firebase
            val spaceForFirebase = space.copy(
                ownerId = auth.currentUser!!.uid,
                isSynced = true,
                isDeleted = false
            )
            
            val docRef = spacesCollection.add(spaceForFirebase).await()
            
            Log.d("FirebaseSpaceRepo", "Space created in Firebase with ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e("FirebaseSpaceRepo", "Failed to create space in Firebase", e)
            Result.failure(e)
        }
    }
    
    suspend fun updateSpace(space: SpaceEntity): Result<Unit> {
        return try {
            if (auth.currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            spacesCollection.document(space.id).set(space).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteSpace(spaceId: String): Result<Unit> {
        return try {
            if (auth.currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            spacesCollection.document(spaceId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun syncSpaces(): Result<List<SpaceEntity>> {
        return try {
            if (auth.currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            val snapshot = spacesCollection
                .whereEqualTo("userId", auth.currentUser!!.uid)
                .get()
                .await()
            
            val spaces = snapshot.documents.mapNotNull { doc ->
                doc.toObject(SpaceEntity::class.java)?.copy(id = doc.id)
            }
            
            Result.success(spaces)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
