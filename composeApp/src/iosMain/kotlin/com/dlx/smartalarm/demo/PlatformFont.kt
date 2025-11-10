package com.dlx.smartalarm.demo

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

@Composable
actual fun getChineseFontFamily(): FontFamily {
    // iOS 平台使用系统默认字体，支持中文显示
    return FontFamily.Default
}