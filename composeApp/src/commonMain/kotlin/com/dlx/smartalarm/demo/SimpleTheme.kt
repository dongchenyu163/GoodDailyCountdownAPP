package com.dlx.smartalarm.demo

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

@Composable
fun AppTheme2(content: @Composable () -> Unit) {
    // 简化的主题配置，不使用自定义字体
    MaterialTheme(
        content = content
    )
}
