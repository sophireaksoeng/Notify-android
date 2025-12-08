package com.team.notify.taskflow.ui.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.team.notify.taskflow.presentation.pages.PageListUiState
import com.team.notify.taskflow.presentation.pages.PageListViewModel

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
            }
            is PageListUiState.Empty -> {
            }
            is PageListUiState.Error -> {
                val message = (state as PageListUiState.Error).message
                Text(
                    text = message,
                    modifier = Modifier.fillMaxSize()
                )
            }
            is PageListUiState.Data -> {
                val pages = (state as PageListUiState.Data).pages
                LazyColumn(contentPadding = padding) {
                    items(pages) { page ->
                        ListItem(
                            headlineContent = { Text(page.title) },
                            modifier = Modifier.clickable {
                                onPageClick(page.id)
                            }
                        )
                    }
                }
            }
        }
    }
}