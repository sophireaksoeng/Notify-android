package com.team.notify.taskflow.presentation.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.team.notify.taskflow.ui.NotionPageRow

@Composable
fun PageListScreen(
    onOpenPage: (String) -> Unit,
    vm: PageListViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val pages by vm.pages.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Pages", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))

        when (state) {
            is PageListUiState.Loading -> Text("Loading…")
            is PageListUiState.Error -> Text("Error: ${(state as PageListUiState.Error).message}")
            is PageListUiState.Empty -> Text("No pages yet.")
            is PageListUiState.Data -> {
                pages.forEach { p ->
                    NotionPageRow(
                        title = p.title.ifBlank { "Untitled" },
                        subtitle = p.content?.take(60),
                        onClick = { onOpenPage(p.id) }
                    )
                }
            }
        }
    }
}
