package com.dlx.smartalarm.demo

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.browser.localStorage
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private fun storageKey(uuid: String) = "$TitleImageDirectoryName:$uuid"

@OptIn(ExperimentalEncodingApi::class)
actual object TitleImageStorage {
    actual suspend fun save(uuid: String, bytes: ByteArray) {
        localStorage.setItem(storageKey(uuid), Base64.encode(bytes))
    }

    actual suspend fun load(uuid: String): ByteArray? {
        val encoded = localStorage.getItem(storageKey(uuid)) ?: return null
        return Base64.decode(encoded)
    }

    actual suspend fun delete(uuid: String) {
        localStorage.removeItem(storageKey(uuid))
    }

    actual fun resolveAbsolutePath(uuid: String): String = storageKey(uuid)
}

actual object PlatformImageIO {
    actual fun decodeImageBitmap(bytes: ByteArray): ImageBitmap? = null
    actual fun compressToJpeg(bytes: ByteArray, quality: Int): ByteArray? = bytes
}

actual suspend fun pickImageFromUser(): PickedImage? {
    println("pickImageFromUser is not supported on Wasm JS in this build.")
    return null
}
