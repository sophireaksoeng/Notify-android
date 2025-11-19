package com.team.notify.taskflow.ui.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.team.notify.taskflow.model.Task
import com.team.notify.taskflow.presentation.tasks.TaskViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.team.notify.taskflow.ui.tasks.components.TaskItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel = viewModel(),
    onTaskSelected: (Task) -> Unit
) {
    val tasks = viewModel.tasks

    Scaffold(
        topBar = { TopAppBar(title = { Text("Task List") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            items(tasks) { task ->
                TaskItem(task = task) { onTaskSelected(task) }
            }
        }
    }
}