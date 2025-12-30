package com.team.notify.taskflow.presentation.workspace

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.team.notify.taskflow.presentation.pages.PageDetailScreen
import com.team.notify.taskflow.presentation.pages.PageListViewModel
import com.team.notify.taskflow.presentation.tasks.TaskDetailScreen
import com.team.notify.taskflow.presentation.tasks.TaskViewModel
import com.team.notify.taskflow.ui.BreadcrumbHeader
import com.team.notify.taskflow.ui.NotionShell
import com.team.notify.taskflow.ui.NotionSidebar
import com.team.notify.taskflow.ui.NotionTasksSection
import com.team.notify.taskflow.ui.TaskDrawerPanel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(
    spaceId: String = "default-space",
    vmPages: PageListViewModel = hiltViewModel(),
    vmTasks: TaskViewModel = hiltViewModel()
) {
    LaunchedEffect(spaceId) {
        vmPages.onEnter(spaceId)
        vmTasks.onEnter(spaceId)
    }

    val pages by vmPages.pages.collectAsState()
    val tasks by vmTasks.tasks.collectAsState()
    val members by vmTasks.members.collectAsState()

    var selectedPageId by remember { mutableStateOf<String?>(null) }
    var selectedTaskId by remember { mutableStateOf<String?>(null) }

    val selectedTask = tasks.firstOrNull { it.id == selectedTaskId }
    val showPhoneList = selectedPageId == null

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 840.dp

        LaunchedEffect(pages) {
            if (selectedPageId == null && pages.isNotEmpty()) {
                selectedPageId = pages.first().id
            }
        }

        val selectedPageTitle = pages.firstOrNull { it.id == selectedPageId }?.title

        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val showSheet = !isWide && selectedTaskId != null && selectedTask != null

        NotionShell(
            sidebar = {
                NotionSidebar(
                    title = "Notify Workspace",
                    pages = pages,
                    selectedPageId = selectedPageId,
                    onSelectPage = { id ->
                        selectedPageId = id
                        if (!isWide) selectedTaskId = null
                    },
                    onNewPage = {
                        vmPages.createPage(title = "Untitled") { newId ->
                            selectedPageId = newId
                            selectedTaskId = null
                        }
                    }
                )
            },
            phoneList = {
                NotionSidebar(
                    title = "Notify Workspace",
                    pages = pages,
                    selectedPageId = selectedPageId,
                    onSelectPage = { selectedPageId = it },
                    onNewPage = {
                        vmPages.createPage(title = "Untitled") { newId ->
                            selectedPageId = newId
                            selectedTaskId = null
                        }
                    }
                )
            },
            showPhoneList = showPhoneList,
            content = {
                if (isWide) {
                    Row(Modifier.fillMaxSize()) {
                        Column(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(16.dp)
                        ) {
                            BreadcrumbHeader(
                                workspace = "Notify Workspace",
                                pageTitle = selectedPageTitle
                            )

                            PageDetailScreen(
                                pageId = selectedPageId,
                                spaceId = spaceId,
                                onBack = {}
                            )

                            NotionTasksSection(
                                title = "Tasks",
                                tasks = tasks.filter { it.pageId == selectedPageId },
                                onToggleDone = { t -> vmTasks.toggleCompleted(t.id) },
                                onOpenTask = { t -> selectedTaskId = t.id },
                                onAddTask = {
                                    vmTasks.createTask(selectedPageId)
                                }
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
                                        vmTasks.deleteTask(selectedTask.id)
                                        selectedTaskId = null
                                    },
                                    onTitleChange = { vmTasks.updateTitle(selectedTask.id, it) },
                                    onDescriptionChange = { vmTasks.updateDescription(selectedTask.id, it) },
                                    onSetStatus = { vmTasks.setStatus(selectedTask.id, it) },
                                    onAssigneeChange = { vmTasks.updateAssignee(selectedTask.id, it) },
                                    onDeadlineChange = { vmTasks.updateDeadline(selectedTask.id, it) },
                                    onToggleCompleted = { vmTasks.toggleCompleted(selectedTask.id) }
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        BreadcrumbHeader(
                            workspace = "Notify Workspace",
                            pageTitle = selectedPageTitle
                        )

                        PageDetailScreen(
                            pageId = selectedPageId,
                            spaceId = spaceId,
                            onBack = { selectedPageId = null }
                        )

                        NotionTasksSection(
                            title = "Tasks",
                            tasks = tasks.filter { it.pageId == selectedPageId },
                            onToggleDone = { t -> vmTasks.toggleCompleted(t.id) },
                            onOpenTask = { t -> selectedTaskId = t.id },
                            onAddTask = {
                                vmTasks.createTask(selectedPageId)
                            }
                        )
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
                                        vmTasks.deleteTask(selectedTask!!.id)
                                        selectedTaskId = null
                                    },
                                    onTitleChange = { vmTasks.updateTitle(selectedTask!!.id, it) },
                                    onDescriptionChange = { vmTasks.updateDescription(selectedTask!!.id, it) },
                                    onSetStatus = { vmTasks.setStatus(selectedTask!!.id, it) },
                                    onAssigneeChange = { vmTasks.updateAssignee(selectedTask!!.id, it) },
                                    onDeadlineChange = { vmTasks.updateDeadline(selectedTask!!.id, it) },
                                    onToggleCompleted = { vmTasks.toggleCompleted(selectedTask!!.id) }
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}
