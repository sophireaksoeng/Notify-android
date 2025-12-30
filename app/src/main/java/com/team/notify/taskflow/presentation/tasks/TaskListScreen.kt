package com.team.notify.taskflow.presentation.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.team.notify.taskflow.ui.NotionDivider

@Composable
fun TaskListScreen(
    onOpenTask: (String) -> Unit,
    vm: TaskViewModel = hiltViewModel()
) {
    val tasks by vm.tasks.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Tasks", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))

        tasks.forEach { t ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(Modifier.weight(1f)) {
                    Checkbox(
                        checked = t.isCompleted,
                        onCheckedChange = { vm.toggleCompleted(t.id) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        t.title.ifBlank { "Untitled task" },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                TextButton(onClick = { onOpenTask(t.id) }) { Text("Open") }
            }
            NotionDivider()
        }
    }
}
