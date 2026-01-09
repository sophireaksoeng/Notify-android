package com.team.notify.taskflow.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object NotionStyle {
    val PagePadding = 16.dp
    val BlockSpacing = 12.dp

    val TitleText = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold
    )

    val BodyText = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp
    )

    val SubtleText = TextStyle(
        fontSize = 13.sp,
        color = Color.Gray
    )

    val DividerColor = Color(0xFFEAEAEA)
}
