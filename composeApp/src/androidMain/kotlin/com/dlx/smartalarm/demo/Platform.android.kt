package com.dlx.smartalarm.demo

import android.content.Context
import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

internal var appContext: Context? = null

actual fun getPlatform(context: Any?): Platform {
    if (context != null && context is Context) {
        appContext = context.applicationContext
    }
    return AndroidPlatform()
}

actual fun readTextFile(fileName: String): String? {
    val context = appContext ?: throw IllegalStateException("Android Context not initialized for file operations.")
    val file = java.io.File(context.filesDir, fileName)
    return if (file.exists()) {
        file.readText()
    }
    else {
        null
    }
}

actual fun writeTextFile(fileName: String, content: String) {
    val context = appContext ?: throw IllegalStateException("Android Context not initialized for file operations.")
    val file = java.io.File(context.filesDir, fileName)
    file.writeText(content)
}

actual fun getPlatformDataDirectory(): String {
    val context = appContext ?: throw IllegalStateException("Android Context not initialized for file operations.")
    return context.filesDir.absolutePath
}