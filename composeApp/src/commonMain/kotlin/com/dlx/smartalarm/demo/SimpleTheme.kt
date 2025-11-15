package com.dlx.smartalarm.demo

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkGreenScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFF13EC80),
    onPrimary = Color(0xFF00210F),
    background = Color(0xFF102219),
    onBackground = Color(0xFFE6F3EC),
    surface = Color(0xFF0F2018),
    onSurface = Color(0xFFE6F3EC),
    surfaceVariant = Color(0xFF11221A),
    onSurfaceVariant = Color(0xFFBBD8C9),
    secondary = Color(0xFF42F59E),
    onSecondary = Color(0xFF00210F),
    error = Color(0xFFFF3B30),
    onError = Color.White,
    errorContainer = Color(0xFFB3261E),
    onErrorContainer = Color.White
)

@Composable
fun AppTheme(typography: Typography = Typography(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkGreenScheme,
        typography = typography,
        content = content
    )
}

// 兼容旧引用
@Composable
fun AppTheme2(content: @Composable () -> Unit) = AppTheme(typography = Typography(), content = content)
