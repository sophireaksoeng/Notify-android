package com.team.notify.taskflow.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.team.notify.taskflow.data.entities.PageEntity

@Composable
fun NotionSidebar(
    title: String = "Workspace",
    pages: List<PageEntity>,
    selectedPageId: String?,
    onSelectPage: (String) -> Unit,
    onNewPage: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    val filtered = remember(pages, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) pages
        else pages.filter { it.title.lowercase().contains(q) }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 16.dp)
    ) {

        Text(title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(10.dp))

        OutlinedButton(onClick = onNewPage, modifier = Modifier.fillMaxWidth()) {
            Text("+ New page")
        }

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Search pages…") }
        )

        Spacer(Modifier.height(12.dp))
        NotionDivider()
        Spacer(Modifier.height(10.dp))

        if (filtered.isEmpty()) {
            Text(
                "No pages yet.\nCreate your first page.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            return
        }

        filtered.forEach { p ->
            val selected = p.id == selectedPageId
            SidebarItem(
                emoji = "📄",
                title = p.title.ifBlank { "Untitled" },
                selected = selected,
                onClick = { onSelectPage(p.id) }
            )
        }
    }
}

@Composable
private fun SidebarItem(
    emoji: String,
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected)
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
    else
        MaterialTheme.colorScheme.surface

    Surface(
        color = bg,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 10.dp, horizontal = 12.dp)
        ) {
            Row(Modifier.fillMaxWidth()) {
                Text(emoji)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    Spacer(Modifier.height(6.dp))
}
