package com.dlx.smartalarm.demo

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.runtime.Composable

class JsPlatform : Platform {
	override val name: String = "Web with Kotlin/JS"
}

actual fun getPlatform(context: Any?): Platform = JsPlatform()

actual fun readTextFile(fileName: String): String? {
    // LocalStorage can be used for web persistence
    return kotlinx.browser.localStorage.getItem(fileName)
}

actual fun writeTextFile(fileName: String, content: String) {
    kotlinx.browser.localStorage.setItem(fileName, content)
}

actual fun getPlatformDataDirectory(): String {
    // Not applicable for JS, but must be implemented
    return ""
}