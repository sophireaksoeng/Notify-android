package com.team.notify.taskflow.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NotionShell(
    sidebar: @Composable () -> Unit,
    content: @Composable () -> Unit,
    phoneList: (@Composable () -> Unit)? = null,
    showPhoneList: Boolean = false
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 840.dp

        if (isWide) {
            Row(Modifier.fillMaxSize()) {
                Surface(
                    tonalElevation = 0.dp,
                    modifier = Modifier.width(320.dp).fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surface
                ) { sidebar() }

                Surface(
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) { content() }
            }
        } else {
            Surface(
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                if (showPhoneList && phoneList != null) phoneList() else content()
            }
        }
    }
}
