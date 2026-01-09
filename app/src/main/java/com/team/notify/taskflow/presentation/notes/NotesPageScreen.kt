package com.team.notify.taskflow.presentation.notes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.team.notify.taskflow.ui.NotionBodyField
import com.team.notify.taskflow.ui.NotionDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesPageScreen(
    pageId: String?,
    spaceId: String,
    onBack: () -> Unit,
    vm: NotesPageViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val saving by vm.saving.collectAsState()

    LaunchedEffect(pageId) {
        vm.load(pageId)
    }

    if (pageId == null) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                "Select a page to start",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .widthIn(max = 900.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onBack) { Text("Back") }
            Spacer(Modifier.weight(1f))
            if (saving) {
                Text(
                    "Saving…",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }
            TextButton(onClick = { vm.save(spaceId) }) { Text("Save") }
        }
        
        NotionDivider()
        
        Text(
            text = "📝 Notes",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        NotionBodyField(
            value = state.content,
            onValueChange = vm::updateContent,
            placeholder = "Start writing your notes here..."
        )
    }
}
