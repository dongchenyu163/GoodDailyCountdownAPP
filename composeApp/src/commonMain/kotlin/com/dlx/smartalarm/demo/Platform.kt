package com.dlx.smartalarm.demo

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.runtime.Composable

interface Platform {
	val name: String
}

expect fun getPlatform(context: Any? = null): Platform
@Composable
expect fun getAppFontFamily(): FontFamily
expect fun readTextFile(fileName: String): String?
expect fun writeTextFile(fileName: String, content: String)
expect fun getPlatformDataDirectory(): String