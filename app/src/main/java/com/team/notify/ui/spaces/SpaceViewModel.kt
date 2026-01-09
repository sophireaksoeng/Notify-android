package com.team.notify.ui.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.notify.data.local.entity.SpaceEntity
import com.team.notify.data.repository.NotifyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SpaceViewModel @Inject constructor(
    private val repository: NotifyRepository,
) : ViewModel() {

    val spaces: StateFlow<List<SpaceEntity>> = repository.observeSpaces()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addSpace(name: String, ownerId: String = "local") {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val entity = SpaceEntity(
            id = id,
            name = name,
            ownerId = ownerId,
            updatedAt = now,
            isSynced = false,
            isDeleted = false,
        )
        viewModelScope.launch {
            try {
                repository.upsertSpace(entity)
            } catch (e: Exception) {
                // Handle database error
                e.printStackTrace()
            }
        }
    }

    fun deleteSpace(spaceId: String) {
        viewModelScope.launch {
            try {
                repository.deleteSpace(spaceId)
            } catch (e: Exception) {
                // Handle database error
                e.printStackTrace()
            }
        }
    }

    fun updateSpace(space: SpaceEntity) {
        viewModelScope.launch {
            try {
                repository.upsertSpace(space)
            } catch (e: Exception) {
                // Handle database error
                e.printStackTrace()
            }
        }
    }
}
