package com.team.notify.taskflow.presentation.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.team.notify.taskflow.ui.NotionBodyField
import com.team.notify.taskflow.ui.NotionDivider
import com.team.notify.taskflow.ui.NotionPageHeader

@Composable
fun PageDetailScreen(
    pageId: String?,
    spaceId: String,
    onBack: () -> Unit,
    vm: PageDetailViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val saving by vm.saving.collectAsState()

    LaunchedEffect(pageId) {
        vm.load(pageId)
    }

    if (pageId == null) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
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

        Spacer(Modifier.height(8.dp))
        NotionDivider()
        Spacer(Modifier.height(12.dp))

        NotionPageHeader(
            emoji = "📄",
            title = state.title,
            onTitleChange = { vm.updateTitle(it) }
        )

        Spacer(Modifier.height(12.dp))

        NotionBodyField(
            value = state.description,
            onValueChange = { vm.updateDescription(it) },
            placeholder = "Type / for blocks…",
            minLines = 12
        )
    }
}
