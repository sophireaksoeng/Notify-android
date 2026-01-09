package com.team.notify.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.team.notify.taskflow.data.entities.PageEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebasePageRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    private val pagesCollection = firestore.collection("pages")
    
    fun getPagesForSpaceFlow(spaceId: String): Flow<List<PageEntity>> = callbackFlow {
        if (auth.currentUser == null) {
            trySend(emptyList())
            close(Exception("User not authenticated"))
            return@callbackFlow
        }
        
        val subscription = pagesCollection
            .whereEqualTo("spaceId", spaceId)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val pages = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(PageEntity::class.java)?.copy(
                            id = doc.id
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(pages)
            }
        
        awaitClose { subscription.remove() }
    }
    
    suspend fun createPage(page: PageEntity): Result<String> {
        return try {
            if (auth.currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            // Create page without ID first, let Firebase generate it
            val docRef = pagesCollection.add(page).await()
            
            // Update the document with the generated ID
            docRef.update("id", docRef.id).await()
            
            Log.d("FirebasePageRepo", "Page created in Firebase with ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e("FirebasePageRepo", "Failed to create page in Firebase", e)
            Result.failure(e)
        }
    }
    
    suspend fun updatePage(page: PageEntity): Result<Unit> {
        return try {
            if (auth.currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            pagesCollection.document(page.id).set(page).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deletePage(pageId: String): Result<Unit> {
        return try {
            if (auth.currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            pagesCollection.document(pageId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun syncPagesForSpace(spaceId: String): Result<List<PageEntity>> {
        return try {
            if (auth.currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            val snapshot = pagesCollection
                .whereEqualTo("spaceId", spaceId)
                .get()
                .await()
            
            val pages = snapshot.documents.mapNotNull { doc ->
                doc.toObject(PageEntity::class.java)?.copy(id = doc.id)
            }
            
            Result.success(pages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
