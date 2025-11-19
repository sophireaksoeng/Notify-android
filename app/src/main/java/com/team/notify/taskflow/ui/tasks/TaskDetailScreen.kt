package com.team.notify.taskflow.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.team.notify.taskflow.model.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(task: Task) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Task Detail") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Title: ${task.title}", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Description: ${task.description}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Status: ${task.status}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Due: ${task.dueDate}")
        }
    }
}