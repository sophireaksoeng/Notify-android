package com.team.notify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NotifyLogo(
    modifier: Modifier = Modifier,
    size: LogoSize = LogoSize.Medium,
    showText: Boolean = true
) {
    val dimensions = when (size) {
        LogoSize.Small -> Pair(24.dp, 14.sp)
        LogoSize.Medium -> Pair(32.dp, 18.sp)
        LogoSize.Large -> Pair(48.dp, 24.sp)
        LogoSize.ExtraLarge -> Pair(64.dp, 32.sp)
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Logo Icon - Modern N design
        Box(
            modifier = Modifier
                .size(dimensions.first)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Color.Black
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "N",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = dimensions.second,
                    color = Color.White
                )
            )
        }
        
        // Logo Text
        if (showText) {
            Text(
                text = "Notify",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = dimensions.second,
                    color = Color.Black
                )
            )
        }
    }
}

enum class LogoSize {
    Small,
    Medium,
    Large,
    ExtraLarge
}

@Composable
fun NotifyLogoIcon(
    modifier: Modifier = Modifier,
    size: LogoSize = LogoSize.Medium
) {
    NotifyLogo(
        modifier = modifier,
        size = size,
        showText = false
    )
}

@Composable
fun NotifyLogoText(
    modifier: Modifier = Modifier,
    size: LogoSize = LogoSize.Medium
) {
    val fontSize = when (size) {
        LogoSize.Small -> 14.sp
        LogoSize.Medium -> 18.sp
        LogoSize.Large -> 24.sp
        LogoSize.ExtraLarge -> 32.sp
    }
    
    Text(
        text = "Notify",
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = fontSize,
            color = Color.Black
        )
    )
}
