package com.team.notify.taskflow.presentation.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun PageListScreen(
    spaceId: String,
    viewModel: PageListViewModel,
    onPageClick: (String) -> Unit,
    onAddPage: () -> Unit,
) {
    val state: PageListUiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPage) {
                Icon(Icons.Default.Add, contentDescription = "Add Page")
            }
        }
    ) { padding: PaddingValues ->
        when (state) {
            is PageListUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is PageListUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No pages yet. Tap + to create one.")
                }
            }

            is PageListUiState.Error -> {
                val message = (state as PageListUiState.Error).message
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error loading pages: $message")
                }
            }

            is PageListUiState.Data -> {
                val pages = (state as PageListUiState.Data).pages
                LazyColumn(contentPadding = padding) {
                    items(pages) { page ->
                        ListItem(
                            headlineContent = { Text(page.title) },
                            trailingContent = {
                                if (page.isShared) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Shared"
                                    )
                                }
                            },
                            modifier = Modifier.clickable { onPageClick(page.id) }
                        )
                    }
                }
            }
        }
    }
}
