package com.team.notify.taskflow.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .background(Color.LightGray.copy(alpha = 0.3f))
            .height(16.dp)
            .fillMaxWidth()
    )
}
