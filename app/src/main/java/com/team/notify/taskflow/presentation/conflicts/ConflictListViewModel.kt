package com.team.notify.taskflow.presentation.conflicts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.notify.taskflow.data.repository.ConflictRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ConflictListViewModel @Inject constructor(
    private val repo: ConflictRepository
) : ViewModel() {

    val conflicts = repo.observeConflicts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
}
