package com.team.notify.taskflow.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TaskDrawerPanel(
    title: String = "Task",
    onClose: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxHeight().width(380.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onClose) { Text("Close") }
            }
            Spacer(Modifier.height(8.dp))
            NotionDivider()
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}
