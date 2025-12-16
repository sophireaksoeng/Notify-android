package com.team.notify.taskflow.presentation.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.team.notify.taskflow.model.Task
import com.team.notify.taskflow.data.sync.SyncViewModel
import com.team.notify.taskflow.ui.tasks.components.TaskItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel = hiltViewModel(),
    onTaskSelected: (Task) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task List") },
                actions = {
                    IconButton(onClick = { viewModel.refresh(context) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh list")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            TaskListUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            TaskListUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tasks found.")
                }
            }

            is TaskListUiState.Error -> {
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: ${state.message}")
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.refresh(context) }) {
                            Text("Retry")
                        }
                    }
                }
            }

            is TaskListUiState.Data -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    items(state.tasks) { task ->
                        TaskItem(
                            task = task,
                            onClick = { onTaskSelected(task) },
                            onStatusChange = { newStatus ->
                                viewModel.updateStatus(task.id, newStatus)
                            }
                        )
                    }
                }
            }
        }
    }
}
