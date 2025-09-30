package com.dlx.smartalarm.demo

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

// JVM平台的中文字体配置
@Composable
actual fun getChineseFontFamily(): FontFamily {
    return FontFamily.Default // JVM平台使用系统默认字体
}
