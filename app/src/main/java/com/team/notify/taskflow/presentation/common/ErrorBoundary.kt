package com.team.notify.taskflow.presentation.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ErrorBoundary(
    message: String,
    onRetry: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(message, color = Color.Red)
        Spacer(Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
