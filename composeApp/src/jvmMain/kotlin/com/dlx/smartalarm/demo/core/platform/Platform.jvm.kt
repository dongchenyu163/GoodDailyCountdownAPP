package com.dlx.smartalarm.demo.core.platform

import java.io.File

class JVMPlatform : Platform {
	override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(context: Any?): Platform = JVMPlatform()

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