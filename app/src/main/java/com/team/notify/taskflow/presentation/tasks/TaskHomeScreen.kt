package com.team.notify.taskflow.presentation.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.team.notify.taskflow.ui.NotionDivider
import com.team.notify.taskflow.ui.NotionTasksSection
import com.team.notify.taskflow.ui.TaskDrawerPanel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskHomeScreen(
    vm: TaskViewModel = hiltViewModel()
) {
    val tasks by vm.tasks.collectAsState()
    val members by vm.members.collectAsState()

    var selectedTaskId by remember { mutableStateOf<String?>(null) }
    val selectedTask = tasks.firstOrNull { it.id == selectedTaskId }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 840.dp

        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val showSheet = !isWide && selectedTask != null

        if (isWide) {
            Row(Modifier.fillMaxSize()) {

                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    NotionTasksSection(
                        title = "Tasks",
                        tasks = tasks,
                        onToggleDone = { t -> vm.toggleCompleted(t.id) },
                        onOpenTask = { t -> selectedTaskId = t.id },
                        onAddTask = { vm.createTask() }
                    )
                }

                if (selectedTask != null) {
                    TaskDrawerPanel(
                        title = "Task",
                        onClose = { selectedTaskId = null }
                    ) {
                        TaskDetailScreen(
                            task = selectedTask,
                            members = members,
                            onBack = { selectedTaskId = null },
                            onDelete = {
                                vm.deleteTask(selectedTask.id)
                                selectedTaskId = null
                            },
                            onTitleChange = { vm.updateTitle(selectedTask.id, it) },
                            onDescriptionChange = { vm.updateDescription(selectedTask.id, it) },
                            onSetStatus = { vm.setStatus(selectedTask.id, it) },
                            onAssigneeChange = { vm.updateAssignee(selectedTask.id, it) },
                            onDeadlineChange = { vm.updateDeadline(selectedTask.id, it) },
                            onToggleCompleted = { vm.toggleCompleted(selectedTask.id) }
                        )
                    }
                }
            }
        } else {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                NotionTasksSection(
                    title = "Tasks",
                    tasks = tasks,
                    onToggleDone = { t -> vm.toggleCompleted(t.id) },
                    onOpenTask = { t -> selectedTaskId = t.id },
                    onAddTask = { vm.createTask() }
                )
                Spacer(Modifier.height(8.dp))
                NotionDivider()
            }

            if (showSheet) {
                ModalBottomSheet(
                    onDismissRequest = { selectedTaskId = null },
                    sheetState = sheetState
                ) {
                    Column(Modifier.padding(16.dp)) {
                        TaskDetailScreen(
                            task = selectedTask!!,
                            members = members,
                            onBack = { selectedTaskId = null },
                            onDelete = {
                                vm.deleteTask(selectedTask!!.id)
                                selectedTaskId = null
                            },
                            onTitleChange = { vm.updateTitle(selectedTask!!.id, it) },
                            onDescriptionChange = { vm.updateDescription(selectedTask!!.id, it) },
                            onSetStatus = { vm.setStatus(selectedTask!!.id, it) },
                            onAssigneeChange = { vm.updateAssignee(selectedTask!!.id, it) },
                            onDeadlineChange = { vm.updateDeadline(selectedTask!!.id, it) },
                            onToggleCompleted = { vm.toggleCompleted(selectedTask!!.id) }
                        )
                    }
                }
            }
        }
    }
}
