package com.dlx.smartalarm.demo

import kotlinx.browser.localStorage

class WasmPlatform : Platform {
	override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(context: Any?): Platform = WasmPlatform()

actual fun readTextFile(fileName: String): String? {
    return localStorage.getItem(fileName)
}

actual fun writeTextFile(fileName: String, content: String) {
    localStorage.setItem(fileName, content)
}

actual fun getPlatformDataDirectory(): String {
    // Not applicable for wasmJs, but must be implemented
    return ""
}