package com.team.notify.taskflow.presentation.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.team.notify.taskflow.data.entities.PageHistoryEntity
import java.util.Date

@Composable
fun PageHistoryScreen(
    viewModel: PageHistoryViewModel,
    pageId: String,
    onRestore: (PageHistoryEntity) -> Unit
) {
    val history by viewModel.history(pageId).collectAsState(initial = emptyList())

    LazyColumn {
        items(history) { item: PageHistoryEntity ->
            ListItem(
                headlineContent = { Text("Version ${item.version}") },
                supportingContent = { Text(Date(item.timestamp).toString()) },
                trailingContent = {
                    TextButton(onClick = { onRestore(item) }) {
                        Text("Restore")
                    }
                }
            )
        }
    }
}
