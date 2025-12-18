package com.team.notify.taskflow.presentation.conflicts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.team.notify.taskflow.data.entities.ConflictEntity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp

@Composable
fun ConflictResolutionScreen(
    conflict: ConflictEntity,
    onKeepLocal: () -> Unit,
    onKeepRemote: () -> Unit
) {
    Column(Modifier.padding(16.dp)) {
        Text("Resolve Conflict", style = MaterialTheme.typography.titleLarge)

        Text("Local version: ${conflict.localVersion}")
        Text("Remote version: ${conflict.remoteVersion}")

        Spacer(Modifier.height(16.dp))

        Button(onClick = onKeepLocal) {
            Text("Keep Local")
        }

        Spacer(Modifier.height(8.dp))

        Button(onClick = onKeepRemote) {
            Text("Keep Remote")
        }
    }
}
