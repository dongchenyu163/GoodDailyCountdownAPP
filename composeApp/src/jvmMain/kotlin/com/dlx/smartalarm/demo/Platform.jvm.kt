package com.dlx.smartalarm.demo

import java.io.File
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.Font
import demo.composeapp.generated.resources.Res
import demo.composeapp.generated.resources.NotoSansSC

class JVMPlatform : Platform {
	override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(context: Any?): Platform = JVMPlatform()

@Composable
actual fun getAppFontFamily(): FontFamily = FontFamily(Font(Res.font.NotoSansSC, weight = FontWeight.Normal))

actual fun readTextFile(fileName: String): String? {
    val file = File(getPlatformDataDirectory(), fileName)
    return if (file.exists()) {
        file.readText()
    } else {
        null
    }
}

actual fun writeTextFile(fileName: String, content: String) {
    val dir = File(getPlatformDataDirectory())
    if (!dir.exists()) {
        dir.mkdirs()
    }
    val file = File(dir, fileName)
    file.writeText(content)
}

actual fun getPlatformDataDirectory(): String {
    val userHome = System.getProperty("user.home")
    val appDataDir = File(userHome, ".smartalarm")
    if (!appDataDir.exists()) {
        appDataDir.mkdirs()
    }
    return appDataDir.absolutePath
}