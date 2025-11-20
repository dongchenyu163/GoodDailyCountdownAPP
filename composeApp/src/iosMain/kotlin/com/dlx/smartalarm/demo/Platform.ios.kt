package com.dlx.smartalarm.demo

import platform.UIKit.UIDevice
import platform.Foundation.NSHomeDirectory

class IOSPlatform : Platform {
	override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(context: Any?): Platform = IOSPlatform()

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