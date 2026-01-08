package com.team.notify.taskflow.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color

@Composable
fun Modifier.notionClickable(onClick: () -> Unit): Modifier {
    return this.notionClickable(
        onClick = onClick,
        backgroundColor = null
    )
}

@Composable
fun Modifier.notionClickable(
    onClick: () -> Unit,
    backgroundColor: Color? = null
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(
            durationMillis = 100,
            easing = EaseOutCubic
        ),
        label = "scale"
    )

    return this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = ripple(
                color = backgroundColor ?: Color.Unspecified
            ),
            onClick = onClick
        )
}
