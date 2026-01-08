package com.team.notify.taskflow.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BreadcrumbHeader(
    workspace: String,
    pageTitle: String?
) {
    Column(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Text(
            text = "$workspace / ${pageTitle ?: "Untitled"}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
        )
        Spacer(Modifier.height(6.dp))
        NotionDivider()
    }
}
