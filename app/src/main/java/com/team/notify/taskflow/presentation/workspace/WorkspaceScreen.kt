package com.team.notify.taskflow.presentation.workspace

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.team.notify.taskflow.data.sync.SyncViewModel
import com.team.notify.taskflow.presentation.pages.PageDetailScreen
import com.team.notify.taskflow.presentation.pages.PageListViewModel
import com.team.notify.taskflow.presentation.tasks.TaskDetailScreen
import com.team.notify.taskflow.presentation.tasks.TaskViewModel
import com.team.notify.taskflow.presentation.notes.NotesPageScreen
import com.team.notify.taskflow.presentation.document.DocumentPageScreen
import com.team.notify.taskflow.model.PageType
import com.team.notify.taskflow.ui.BreadcrumbHeader
import com.team.notify.taskflow.ui.NotionShell
import com.team.notify.taskflow.ui.NotionSidebar
import com.team.notify.taskflow.ui.PageListView
import com.team.notify.taskflow.ui.TaskListView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(
    spaceId: String = "notify-db",
    vmPages: PageListViewModel = hiltViewModel(),
    vmTasks: TaskViewModel = hiltViewModel(),
    vmSync: SyncViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val pages by vmPages.pages.collectAsState()
    val tasks by vmTasks.tasks.collectAsState()
    val members by vmTasks.members.collectAsState()

    var selectedPageId by remember { mutableStateOf<String?>(null) }
    var selectedTaskId by remember { mutableStateOf<String?>(null) }

    val selectedTask = tasks.firstOrNull { it.id == selectedTaskId }
    val showPhoneList = selectedPageId == null
    val showPageList = selectedPageId == null
    val showTaskList = selectedTaskId == null && selectedPageId != null

    LaunchedEffect(spaceId) {
        vmPages.onEnter(spaceId)
        vmTasks.onEnter(spaceId)
    }

    LaunchedEffect(selectedPageId) {
        selectedPageId?.let { pageId ->
            vmTasks.setSelectedPage(pageId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Color(0xFF4A90E2),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "N",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Workspace",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFF1F2937)
                        )
                        Text(
                            text = "Pages & Tasks",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { vmSync.manualSync() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync",
                            tint = Color(0xFF4A90E2),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = { /* TODO: Add page */ },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Page",
                            tint = Color(0xFF4A90E2),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF4A90E2),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val isWide = maxWidth >= 840.dp
                val selectedPageTitle = pages.firstOrNull { it.id == selectedPageId }?.title

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
                            },
                            onRenamePage = { pageId, newName ->
                                vmPages.renamePage(pageId, newName)
                            },
                            onDeletePage = { pageId ->
                                vmPages.deletePage(pageId)
                                if (selectedPageId == pageId && pages.isNotEmpty()) {
                                    selectedPageId = pages.first { it.id != pageId }?.id
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
                            },
                            onRenamePage = { pageId, newName ->
                                vmPages.renamePage(pageId, newName)
                            },
                            onDeletePage = { pageId ->
                                vmPages.deletePage(pageId)
                                if (selectedPageId == pageId && pages.isNotEmpty()) {
                                    selectedPageId = pages.first { it.id != pageId }?.id
                                }
                            }
                        )
                    },
                    showPhoneList = showPhoneList,
                    onSync = { vmSync.manualSync() },
                    onAdd = { title, pageType ->
                        Log.d("WorkspaceScreen", "Add button clicked")
                        vmPages.createPage(title = title, pageType = pageType) { newId ->
                            Log.d("WorkspaceScreen", "Page created with ID: $newId")
                            selectedPageId = newId
                            selectedTaskId = null
                        }
                    },
                    content = {
                        if (isWide) {
                            Row(Modifier.fillMaxSize()) {
                                Column(
                                    Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(20.dp)
                                ) {
                                    if (showPageList) {
                                        PageListView(
                                            pages = pages,
                                            onPageClick = { pageId ->
                                                selectedPageId = pageId
                                            },
                                            onRenamePage = { pageId, newName ->
                                                vmPages.renamePage(pageId, newName)
                                            },
                                            onDeletePage = { pageId ->
                                                vmPages.deletePage(pageId)
                                            },
                                            onAddPage = { title, pageType ->
                                                vmPages.createPage(title = title, pageType = pageType) { newId ->
                                                    selectedPageId = newId
                                                }
                                            }
                                        )
                                    } else {
                                        val selectedPage = pages.find { it.id == selectedPageId }
                                        BreadcrumbHeader(
                                            workspace = "Notify Workspace",
                                            pageTitle = selectedPageTitle
                                        )

                                        when (selectedPage?.pageType) {
                                            PageType.TASKS -> {
                                                if (showTaskList) {
                                                    TaskListView(
                                                        tasks = tasks.filter { it.pageId == selectedPageId },
                                                        onTaskClick = { taskId ->
                                                            selectedTaskId = taskId
                                                        },
                                                        onAddTask = { title ->
                                                            vmTasks.createTask(selectedPageId ?: "", title)
                                                        },
                                                        onRenameTask = { taskId, newName ->
                                                            vmTasks.updateTitle(taskId, newName)
                                                        },
                                                        onDeleteTask = { taskId ->
                                                            vmTasks.deleteTask(taskId)
                                                        },
                                                        onToggleComplete = { taskId ->
                                                            vmTasks.toggleCompleted(taskId)
                                                        },
                                                        onSetStatus = { taskId, status ->
                                                            vmTasks.setStatus(taskId, status)
                                                        }
                                                    )
                                                } else {
                                                    selectedTask?.let { task ->
                                                        TaskDetailScreen(
                                                            task = task,
                                                            members = members,
                                                            onBack = { selectedTaskId = null },
                                                            onDelete = {
                                                                vmTasks.deleteTask(task.id)
                                                                selectedTaskId = null
                                                            },
                                                            onTitleChange = { vmTasks.updateTitle(task.id, it) },
                                                            onDescriptionChange = { vmTasks.updateDescription(task.id, it) },
                                                            onSetStatus = { vmTasks.setStatus(task.id, it) },
                                                            onAssigneeChange = { vmTasks.updateAssignee(task.id, it) },
                                                            onDeadlineChange = { vmTasks.updateDeadline(task.id, it) },
                                                            onToggleCompleted = { vmTasks.toggleCompleted(task.id) }
                                                        )
                                                    }
                                                }
                                            }
                                            PageType.NOTES -> {
                                                NotesPageScreen(
                                                    pageId = selectedPageId,
                                                    spaceId = spaceId,
                                                    onBack = { selectedPageId = null }
                                                )
                                            }
                                            PageType.DOCUMENT -> {
                                                DocumentPageScreen(
                                                    pageId = selectedPageId,
                                                    spaceId = spaceId,
                                                    onBack = { selectedPageId = null }
                                                )
                                            }
                                            null -> {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("Select a page to start")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Column(
                                Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                if (showPageList) {
                                    PageListView(
                                        pages = pages,
                                        onPageClick = { pageId ->
                                            selectedPageId = pageId
                                        },
                                        onRenamePage = { pageId, newName ->
                                            vmPages.renamePage(pageId, newName)
                                        },
                                        onDeletePage = { pageId ->
                                            vmPages.deletePage(pageId)
                                        },
                                        onAddPage = { title, pageType ->
                                            vmPages.createPage(title = title, pageType = pageType) { newId ->
                                                selectedPageId = newId
                                            }
                                        }
                                    )
                                } else {
                                    val selectedPage = pages.find { it.id == selectedPageId }
                                    BreadcrumbHeader(
                                        workspace = "Notify Workspace",
                                        pageTitle = selectedPageTitle
                                    )

                                    when (selectedPage?.pageType) {
                                        PageType.TASKS -> {
                                            if (showTaskList) {
                                                TaskListView(
                                                    tasks = tasks.filter { it.pageId == selectedPageId },
                                                    onTaskClick = { taskId ->
                                                        selectedTaskId = taskId
                                                    },
                                                    onAddTask = { title ->
                                                        vmTasks.createTask(selectedPageId ?: "", title)
                                                    },
                                                    onRenameTask = { taskId, newName ->
                                                        vmTasks.updateTitle(taskId, newName)
                                                    },
                                                    onDeleteTask = { taskId ->
                                                        vmTasks.deleteTask(taskId)
                                                    },
                                                    onToggleComplete = { taskId ->
                                                        vmTasks.toggleCompleted(taskId)
                                                    },
                                                    onSetStatus = { taskId, status ->
                                                        vmTasks.setStatus(taskId, status)
                                                    }
                                                )
                                            } else {
                                                selectedTask?.let { task ->
                                                    TaskDetailScreen(
                                                        task = task,
                                                        members = members,
                                                        onBack = { selectedTaskId = null },
                                                        onDelete = {
                                                            vmTasks.deleteTask(task.id)
                                                            selectedTaskId = null
                                                        },
                                                        onTitleChange = { vmTasks.updateTitle(task.id, it) },
                                                        onDescriptionChange = { vmTasks.updateDescription(task.id, it) },
                                                        onSetStatus = { vmTasks.setStatus(task.id, it) },
                                                        onAssigneeChange = { vmTasks.updateAssignee(task.id, it) },
                                                        onDeadlineChange = { vmTasks.updateDeadline(task.id, it) },
                                                        onToggleCompleted = { vmTasks.toggleCompleted(task.id) }
                                                    )
                                                }
                                            }
                                        }
                                        PageType.NOTES -> {
                                            NotesPageScreen(
                                                pageId = selectedPageId,
                                                spaceId = spaceId,
                                                onBack = { selectedPageId = null }
                                            )
                                        }
                                        PageType.DOCUMENT -> {
                                            DocumentPageScreen(
                                                pageId = selectedPageId,
                                                spaceId = spaceId,
                                                onBack = { selectedPageId = null }
                                            )
                                        }
                                        null -> {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("Select a page to start")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}
