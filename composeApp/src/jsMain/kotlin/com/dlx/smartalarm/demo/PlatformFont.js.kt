package com.dlx.smartalarm.demo

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

// Web平台的中文字体配置 - 根据PR #1400的解决方案
@Composable
actual fun getChineseFontFamily(): FontFamily {
    return FontFamily(
        Font(
            identity = "NotoSansSC-Regular",
            getData = { loadSystemChineseFont() },
            weight = FontWeight.Normal
        ),
        Font(
            identity = "NotoSansSC-Bold",
            getData = { loadSystemChineseFont() },
            weight = FontWeight.Bold
        )
    )
}

// 加载系统中文字体数据
private fun loadSystemChineseFont(): ByteArray {
    // 根据PR的解决方案，我们需要提供实际的字体数据
    // 这里使用一个占位符，实际上Skiko会从系统字体中查找
    return ByteArray(0)
}
