package com.team.notify.taskflow.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.notify.taskflow.data.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PageHistoryViewModel @Inject constructor(
    private val repo: HistoryRepository
) : ViewModel() {

    fun history(pageId: String) =
        repo.historyForPage(pageId)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
