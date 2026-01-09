package com.team.notify.taskflow.presentation.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.team.notify.taskflow.data.entities.SpaceMemberEntity
import com.team.notify.taskflow.data.entities.TaskEntity
import com.team.notify.taskflow.model.TaskStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFiltersBar(
    filters: TaskFilters,
    members: List<SpaceMemberEntity>,
    tasks: List<TaskEntity>,
    onQuery: (String) -> Unit,
    onToggleStatus: (TaskStatus) -> Unit,
    onAssignee: (String?) -> Unit,
    onLabel: (String?) -> Unit
) {
    val allLabels = remember(tasks) {
        tasks.flatMap { it.labels }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = filters.query,
            onValueChange = onQuery,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search title/description") },
            singleLine = true
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            StatusChips(selected = filters.status, onToggle = onToggleStatus)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            AssigneeFilterDropdown(
                members = members,
                selectedUserId = filters.assigneeId,
                onChange = onAssignee,
                modifier = Modifier.weight(1f)
            )
            LabelFilterDropdown(
                labels = allLabels,
                selected = filters.label,
                onChange = onLabel,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatusChips(
    selected: Set<TaskStatus>,
    onToggle: (TaskStatus) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selected.contains(TaskStatus.TODO),
            onClick = { onToggle(TaskStatus.TODO) },
            label = { Text("TODO") }
        )
        FilterChip(
            selected = selected.contains(TaskStatus.DOING),
            onClick = { onToggle(TaskStatus.DOING) },
            label = { Text("DOING") }
        )
        FilterChip(
            selected = selected.contains(TaskStatus.DONE),
            onClick = { onToggle(TaskStatus.DONE) },
            label = { Text("DONE") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssigneeFilterDropdown(
    members: List<SpaceMemberEntity>,
    selectedUserId: String?,
    onChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedLabel = members.firstOrNull { it.userId == selectedUserId }
        ?.userId
        ?: "All assignees"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Assignee") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("All assignees") }, onClick = {
                onChange(null); expanded = false
            })
            members.forEach { m ->
                DropdownMenuItem(
                    text = { Text(m.userId) },
                    onClick = {
                        onChange(m.userId); expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabelFilterDropdown(
    labels: List<String>,
    selected: String?,
    onChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = selected ?: "All labels"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Label") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("All labels") }, onClick = {
                onChange(null); expanded = false
            })
            labels.forEach { l ->
                DropdownMenuItem(text = { Text(l) }, onClick = {
                    onChange(l); expanded = false
                })
            }
        }
    }
}
