package com.dlx.smartalarm.demo

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

// Android平台的中文字体配置
@Composable
actual fun getChineseFontFamily(): FontFamily {
    return FontFamily.Default // Android平台通常有内置的中文字体支持
}
