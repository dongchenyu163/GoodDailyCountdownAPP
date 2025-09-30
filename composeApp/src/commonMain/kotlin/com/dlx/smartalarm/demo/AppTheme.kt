package com.dlx.smartalarm.demo

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val chineseTypography = Typography(
        displayLarge = MaterialTheme.typography.displayLarge.copy(
            fontFamily = getChineseFontFamily()
        ),
        displayMedium = MaterialTheme.typography.displayMedium.copy(
            fontFamily = getChineseFontFamily()
        ),
        displaySmall = MaterialTheme.typography.displaySmall.copy(
            fontFamily = getChineseFontFamily()
        ),
        headlineLarge = MaterialTheme.typography.headlineLarge.copy(
            fontFamily = getChineseFontFamily()
        ),
        headlineMedium = MaterialTheme.typography.headlineMedium.copy(
            fontFamily = getChineseFontFamily()
        ),
        headlineSmall = MaterialTheme.typography.headlineSmall.copy(
            fontFamily = getChineseFontFamily()
        ),
        titleLarge = MaterialTheme.typography.titleLarge.copy(
            fontFamily = getChineseFontFamily()
        ),
        titleMedium = MaterialTheme.typography.titleMedium.copy(
            fontFamily = getChineseFontFamily()
        ),
        titleSmall = MaterialTheme.typography.titleSmall.copy(
            fontFamily = getChineseFontFamily()
        ),
        bodyLarge = MaterialTheme.typography.bodyLarge.copy(
            fontFamily = getChineseFontFamily()
        ),
        bodyMedium = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = getChineseFontFamily()
        ),
        bodySmall = MaterialTheme.typography.bodySmall.copy(
            fontFamily = getChineseFontFamily()
        ),
        labelLarge = MaterialTheme.typography.labelLarge.copy(
            fontFamily = getChineseFontFamily()
        ),
        labelMedium = MaterialTheme.typography.labelMedium.copy(
            fontFamily = getChineseFontFamily()
        ),
        labelSmall = MaterialTheme.typography.labelSmall.copy(
            fontFamily = getChineseFontFamily()
        )
    )

    MaterialTheme(
        typography = chineseTypography,
        content = content
    )
}
