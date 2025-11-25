package com.team.notify.taskflow.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.team.notify.taskflow.presentation.tasks.TaskDetailViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.team.notify.taskflow.model.Task
import com.team.notify.taskflow.model.TaskStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    viewModel: TaskDetailViewModel = hiltViewModel(),
    onSaved: () -> Unit = {}
) {
    LaunchedEffect(taskId) { viewModel.load(taskId) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Task Detail") }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            val task = uiState ?: Task(
                id = "",
                title = "",
                description = "",
                status = TaskStatus.TODO,
                dueDate = Date(0)
            )

            OutlinedTextField(
                value = task.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = task.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            val formatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
            Text("Due: ${formatter.format(task.dueDate)}", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                val newMillis = System.currentTimeMillis() + 60 * 60 * 1000
                viewModel.updateDueDate(newMillis)
            }) { Text("Set due +1h (test)") }
            Spacer(Modifier.height(24.dp))
            Row {
                Button(onClick = {
                    viewModel.save(taskId)
                    onSaved()
                }) { Text("Save") }
                Spacer(Modifier.width(12.dp))
                Button(onClick = { viewModel.cancelReminder(taskId) }) { Text("Cancel reminder") }
            }
        }
    }
}
