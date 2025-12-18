package com.team.notify.taskflow.presentation.conflicts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import com.team.notify.taskflow.data.entities.ConflictEntity

@Composable
fun ConflictListScreen(
    viewModel: ConflictListViewModel,
    onConflictClick: (ConflictEntity) -> Unit
) {
    val conflicts by viewModel.conflicts.collectAsState(initial = emptyList())

    LazyColumn {
        items(conflicts) { conflict ->
            ListItem(
                headlineContent = { Text("Conflict on ${conflict.entityType}") },
                supportingContent = {
                    Text("Local v${conflict.localVersion} vs Remote v${conflict.remoteVersion}")
                },
                modifier = Modifier.clickable {
                    onConflictClick(conflict)
                }
            )
        }
    }
}