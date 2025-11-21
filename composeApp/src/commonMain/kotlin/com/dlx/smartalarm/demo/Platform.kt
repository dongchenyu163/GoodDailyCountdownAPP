package com.dlx.smartalarm.demo

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.Font
import demo.composeapp.generated.resources.Res
import demo.composeapp.generated.resources.NotoSansSC
import demo.composeapp.generated.resources.NotoEmoji_VariableFont_wght

interface Platform {
	val name: String
}

expect fun getPlatform(context: Any? = null): Platform

@Composable
fun getAppFontFamily(): FontFamily = FontFamily(
    Font(Res.font.NotoSansSC, weight = FontWeight.Normal),
    Font(Res.font.NotoEmoji_VariableFont_wght, weight = FontWeight.Normal)
)

expect fun readTextFile(fileName: String): String?
expect fun writeTextFile(fileName: String, content: String)
expect fun getPlatformDataDirectory(): String