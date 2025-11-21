package com.dlx.smartalarm.demo

import platform.UIKit.UIDevice
import platform.Foundation.NSHomeDirectory
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.Font
import demo.composeapp.generated.resources.Res
import demo.composeapp.generated.resources.NotoSansSC

class IOSPlatform : Platform {
	override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(context: Any?): Platform = IOSPlatform()

@Composable
actual fun getAppFontFamily(): FontFamily = FontFamily(Font(Res.font.NotoSansSC, weight = FontWeight.Normal))

actual fun readTextFile(fileName: String): String? {
    // A real implementation would use NSFileManager to read from the documents directory
    return null
}

actual fun writeTextFile(fileName: String, content: String) {
    // A real implementation would use NSFileManager to write to the documents directory
}

actual fun getPlatformDataDirectory(): String {
    return NSHomeDirectory() + "/Documents"
}