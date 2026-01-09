package com.team.notify.taskflow.data.sync

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncScheduler: SyncScheduler
) : ViewModel() {

    fun manualSync() {
        viewModelScope.launch {
            syncScheduler.scheduleNow(context)
        }
    }
}
