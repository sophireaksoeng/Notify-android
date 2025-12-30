package com.team.notify.taskflow.presentation.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.team.notify.taskflow.data.entities.TaskEntity
import com.team.notify.taskflow.model.TaskStatus

@Composable
fun TaskBoardScreen(
    tasks: List<TaskEntity>,
    onClick: (TaskEntity) -> Unit
) {
    val todo = tasks.filter { it.status == TaskStatus.TODO }
    val doing = tasks.filter { it.status == TaskStatus.DOING }
    val done = tasks.filter { it.status == TaskStatus.DONE }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BoardColumn(title = "TODO", items = todo, onClick = onClick)
        BoardColumn(title = "DOING", items = doing, onClick = onClick)
        BoardColumn(title = "DONE", items = done, onClick = onClick)
    }
}

@Composable
private fun BoardColumn(
    title: String,
    items: List<TaskEntity>,
    onClick: (TaskEntity) -> Unit
) {
    ElevatedCard(modifier = Modifier.width(300.dp).fillMaxHeight()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Divider()
            items.forEach { t ->
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClick(t) }
                ) {
                    Column(Modifier.padding(10.dp)) {
                        Text(t.title, style = MaterialTheme.typography.bodyLarge)
                        if (!t.description.isNullOrBlank()) {
                            Text(t.description!!, style = MaterialTheme.typography.bodySmall)
                        }
                        if (t.labels.isNotEmpty()) {
                            Text(t.labels.joinToString(", "), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
