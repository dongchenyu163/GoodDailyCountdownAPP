package com.dlx.smartalarm.demo

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

// 跨平台字体配置的期望声明
@Composable
expect fun getChineseFontFamily(): FontFamily
