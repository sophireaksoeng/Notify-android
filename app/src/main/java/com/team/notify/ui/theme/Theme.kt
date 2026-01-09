package com.team.notify.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun NotifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = NotionColors.DarkText,
            onPrimary = NotionColors.DarkBackground,
            background = NotionColors.DarkBackground,
            onBackground = NotionColors.DarkText,
            surface = NotionColors.DarkSurface,
            onSurface = NotionColors.DarkText,
            outline = NotionColors.DarkDivider
        )
    } else {
        lightColorScheme(
            primary = NotionColors.LightText,
            onPrimary = NotionColors.LightBackground,
            background = NotionColors.LightBackground,
            onBackground = NotionColors.LightText,
            surface = NotionColors.LightSurface,
            onSurface = NotionColors.LightText,
            outline = NotionColors.LightDivider
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NotionTypography,
        content = content
    )
}
