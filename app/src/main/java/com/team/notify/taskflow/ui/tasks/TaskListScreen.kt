package com.team.notify.taskflow.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.team.notify.taskflow.model.Task
import com.team.notify.taskflow.presentation.tasks.TaskViewModel
import com.team.notify.taskflow.ui.tasks.components.TaskItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onTaskSelected: (Task) -> Unit,
    viewModel: TaskViewModel = viewModel()
) {
    val tasks by viewModel.tasksFlow.collectAsStateWithLifecycle(emptyList())

    Scaffold(
        topBar = { TopAppBar(title = { Text("Task List") }) }
    ) { padding ->
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No tasks found")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    TaskItem(task = task) { onTaskSelected(task) }
                }
            }
        }
    }
}