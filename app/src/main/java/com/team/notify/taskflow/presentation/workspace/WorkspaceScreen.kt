package com.team.notify.taskflow.presentation.workspace

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.Subject
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.team.notify.taskflow.data.entities.PageEntity
import com.team.notify.taskflow.data.entities.SpaceMemberEntity
import com.team.notify.taskflow.data.entities.TaskEntity
import com.team.notify.taskflow.data.sync.SyncViewModel
import com.team.notify.taskflow.model.PageType
import com.team.notify.taskflow.presentation.document.DocumentPageScreen
import com.team.notify.taskflow.presentation.notes.NotesPageScreen
import com.team.notify.taskflow.presentation.pages.PageListViewModel
import com.team.notify.taskflow.presentation.tasks.TaskDetailScreen
import com.team.notify.taskflow.presentation.tasks.TaskViewModel
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
    // --- Data ---
    val pages by vmPages.pages.collectAsState()
    val tasks by vmTasks.tasks.collectAsState()
    val members by vmTasks.members.collectAsState()

    // --- State ---
    var selectedPageId by remember { mutableStateOf<String?>(null) }
    var selectedTaskId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Menu State
    var activeMenuPageId by remember { mutableStateOf<String?>(null) }
    var pageToRename by remember { mutableStateOf<PageEntity?>(null) }

    // Derived State
    val selectedPage = remember(selectedPageId, pages) { pages.find { it.id == selectedPageId } }
    val selectedTask = remember(selectedTaskId, tasks) { tasks.find { it.id == selectedTaskId } }

    val filteredPages = remember(pages, searchQuery) {
        if (searchQuery.isBlank()) pages
        else pages.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    LaunchedEffect(spaceId) {
        vmPages.onEnter(spaceId)
        vmTasks.onEnter(spaceId)
    }

    LaunchedEffect(selectedPageId) {
        selectedPageId?.let { vmTasks.setSelectedPage(it) }
    }

    // --- Back Handler ---
    BackHandler(enabled = true) {
        when {
            selectedTaskId != null -> selectedTaskId = null
            selectedPageId != null -> selectedPageId = null
            else -> onBack()
        }
    }

    // --- Rename Dialog ---
    if (pageToRename != null) {
        RenamePageDialog(
            currentName = pageToRename!!.title,
            onDismiss = { pageToRename = null },
            onConfirm = { newName ->
                vmPages.renamePage(pageToRename!!.id, newName)
                pageToRename = null
            }
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        val isWideScreen = maxWidth >= 840.dp

        if (isWideScreen) {
            Row(Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .width(340.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(16.dp)
                ) {
                    WorkspaceHeader(onSync = { vmSync.manualSync() })
                    Spacer(modifier = Modifier.height(16.dp))
                    SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredPages) { page ->
                            PageCard(
                                page = page,
                                isSelected = selectedPageId == page.id,
                                isMenuOpen = activeMenuPageId == page.id,
                                onToggleMenu = {
                                    activeMenuPageId = if (activeMenuPageId == page.id) null else page.id
                                },
                                onClick = {
                                    selectedPageId = page.id
                                    selectedTaskId = null
                                },
                                onRename = {
                                    activeMenuPageId = null
                                    pageToRename = page
                                },
                                onDelete = {
                                    activeMenuPageId = null
                                    vmPages.deletePage(page.id)
                                    if (selectedPageId == page.id) selectedPageId = null
                                }
                            )
                        }
                    }
                }

                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Box(Modifier.weight(1f)) {
                    // Safe type conversion for layout
                    val safeType = try {
                        PageType.valueOf(selectedPage?.pageType.toString())
                    } catch (e: Exception) {
                        PageType.NOTES
                    }

                    if (selectedPageId == null) {
                        EmptyWorkspaceState()
                    } else {
                        ActivePageContent(
                            spaceId = spaceId,
                            selectedPageId = selectedPageId,
                            selectedPageType = safeType,
                            tasks = tasks,
                            selectedTaskId = selectedTaskId,
                            selectedTask = selectedTask,
                            members = members,
                            vmTasks = vmTasks,
                            onTaskClick = { selectedTaskId = it },
                            onCloseTask = { selectedTaskId = null }
                        )
                    }
                }
            }
        } else {
            Scaffold(
                topBar = {
                    if (selectedPageId != null) {
                        TopAppBar(
                            title = {
                                Text(
                                    selectedPage?.title ?: "Page",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    if (selectedTaskId != null) selectedTaskId = null else selectedPageId = null
                                }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                                }
                            }
                        )
                    }
                },
                floatingActionButton = {
                    if (selectedPageId == null) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                // Direct call to create page, ensure only one call happens
                                vmPages.createPage("Untitled Page", PageType.NOTES) { newPageId ->
                                    selectedPageId = newPageId
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            icon = { Icon(Icons.Default.Add, "New Page") },
                            text = { Text("New Page") }
                        )
                    }
                }
            ) { paddingValues ->
                Box(Modifier.padding(paddingValues)) {
                    AnimatedContent(
                        targetState = selectedPageId,
                        label = "PageTransition",
                        transitionSpec = {
                            if (targetState != null) {
                                slideInHorizontally { it } + fadeIn() togetherWith fadeOut() + slideOutHorizontally { -it / 4 }
                            } else {
                                slideInHorizontally { -it } + fadeIn() togetherWith fadeOut() + slideOutHorizontally { it / 4 }
                            }
                        }
                    ) { pageId ->
                        if (pageId == null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Spacer(modifier = Modifier.height(16.dp))
                                WorkspaceHeader(onSync = { vmSync.manualSync() })
                                Spacer(modifier = Modifier.height(24.dp))
                                SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    "Your Pages",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(bottom = 80.dp)
                                ) {
                                    items(filteredPages) { page ->
                                        PageCard(
                                            page = page,
                                            isSelected = false,
                                            isMenuOpen = activeMenuPageId == page.id,
                                            onToggleMenu = {
                                                activeMenuPageId = if (activeMenuPageId == page.id) null else page.id
                                            },
                                            onClick = { selectedPageId = page.id },
                                            onRename = {
                                                activeMenuPageId = null
                                                pageToRename = page
                                            },
                                            onDelete = {
                                                activeMenuPageId = null
                                                vmPages.deletePage(page.id)
                                            }
                                        )
                                    }

                                    if (filteredPages.isEmpty()) {
                                        item {
                                            EmptyWorkspaceState(message = "No pages found. Create one!")
                                        }
                                    }
                                }
                            }
                        } else {
                            // Safe type conversion for mobile
                            val safeType = try {
                                PageType.valueOf(selectedPage?.pageType.toString())
                            } catch (e: Exception) {
                                PageType.NOTES
                            }

                            ActivePageContent(
                                spaceId = spaceId,
                                selectedPageId = pageId,
                                selectedPageType = safeType,
                                tasks = tasks,
                                selectedTaskId = selectedTaskId,
                                selectedTask = selectedTask,
                                members = members,
                                vmTasks = vmTasks,
                                onTaskClick = { selectedTaskId = it },
                                onCloseTask = { selectedTaskId = null }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorkspaceHeader(onSync: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "U",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "Notify Workspace",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Let's get productive",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(
            onClick = onSync,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.Default.Sync, "Sync", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search pages...") },
        leadingIcon = { Icon(Icons.Outlined.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}

@Composable
fun PageCard(
    page: PageEntity,
    isSelected: Boolean,
    isMenuOpen: Boolean,
    onToggleMenu: () -> Unit,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    // FIX: Safely convert entity pageType (likely String) to Enum
    val safePageType = try {
        PageType.valueOf(page.pageType.toString())
    } catch (e: Exception) {
        PageType.NOTES
    }

    val (icon, color) = when (safePageType) {
        PageType.TASKS -> Icons.Outlined.CheckCircle to MaterialTheme.colorScheme.tertiary
        // FIX: Use standard Subject icon to prevent unresolved reference
        PageType.NOTES -> Icons.AutoMirrored.Outlined.Subject to MaterialTheme.colorScheme.secondary
        PageType.DOCUMENT -> Icons.Filled.Description to MaterialTheme.colorScheme.primary
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${safePageType.name.lowercase().replaceFirstChar { it.uppercase() }} • Updated recently",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                IconButton(onClick = onToggleMenu) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = isMenuOpen,
                    onDismissRequest = onToggleMenu
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        leadingIcon = { Icon(Icons.Outlined.Edit, null) },
                        onClick = onRename
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) },
                        onClick = onDelete
                    )
                }
            }
        }
    }
}

@Composable
fun RenamePageDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(currentName) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Rename Page",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(onClick = { onConfirm(text) }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun ActivePageContent(
    spaceId: String,
    selectedPageId: String?,
    selectedPageType: PageType?,
    tasks: List<TaskEntity>,
    selectedTaskId: String?,
    selectedTask: TaskEntity?,
    members: List<SpaceMemberEntity>,
    vmTasks: TaskViewModel,
    onTaskClick: (String) -> Unit,
    onCloseTask: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        when (selectedPageType) {
            PageType.TASKS -> {
                AnimatedContent(
                    targetState = selectedTaskId,
                    label = "TaskTransition"
                ) { taskId ->
                    if (taskId == null) {
                        TaskListView(
                            tasks = tasks.filter { it.pageId == selectedPageId },
                            onTaskClick = onTaskClick,
                            onAddTask = { title ->
                                vmTasks.createTask(selectedPageId ?: "", title)
                            },
                            onRenameTask = { id, newName -> vmTasks.updateTitle(id, newName) },
                            onDeleteTask = { id -> vmTasks.deleteTask(id) },
                            onToggleComplete = { id -> vmTasks.toggleCompleted(id) },
                            onSetStatus = { id, status -> vmTasks.setStatus(id, status) }
                        )
                    } else {
                        selectedTask?.let { task ->
                            TaskDetailScreen(
                                task = task,
                                members = members,
                                onBack = onCloseTask,
                                onDelete = {
                                    vmTasks.deleteTask(task.id)
                                    onCloseTask()
                                },
                                onTitleChange = { vmTasks.updateTitle(task.id, it) },
                                onDescriptionChange = { vmTasks.updateDescription(task.id, it) },
                                onSetStatus = { vmTasks.setStatus(task.id, it) },
                                onAssigneeChange = { vmTasks.updateAssignee(task.id, it) },
                                onDeadlineChange = { vmTasks.updateDeadline(task.id, it) },
                                onToggleCompleted = { vmTasks.toggleCompleted(task.id) }
                            )
                        } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
            PageType.NOTES -> {
                NotesPageScreen(
                    pageId = selectedPageId,
                    spaceId = spaceId,
                    onBack = { /* Handled by BackHandler */ }
                )
            }
            PageType.DOCUMENT -> {
                DocumentPageScreen(
                    pageId = selectedPageId,
                    spaceId = spaceId,
                    onBack = { /* Handled by BackHandler */ }
                )
            }
            null -> {
                EmptyWorkspaceState(message = "Page type not recognized")
            }
        }
    }
}

@Composable
fun EmptyWorkspaceState(message: String = "Select a page to start working") {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Dashboard,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}