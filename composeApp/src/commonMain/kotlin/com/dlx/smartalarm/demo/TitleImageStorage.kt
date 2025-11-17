package com.dlx.smartalarm.demo

import androidx.compose.ui.graphics.ImageBitmap

data class PickedImage(
    val bytes: ByteArray,
    val fileName: String? = null,
    val mimeType: String? = null
)

const val TitleImageDirectoryName = "images"
const val TitleImageDefaultQuality = 80

expect object TitleImageStorage {
    suspend fun save(uuid: String, bytes: ByteArray)
    suspend fun load(uuid: String): ByteArray?
    suspend fun delete(uuid: String)
    fun resolveAbsolutePath(uuid: String): String
}

expect object PlatformImageIO {
    fun decodeImageBitmap(bytes: ByteArray): ImageBitmap?
    fun compressToJpeg(bytes: ByteArray, quality: Int): ByteArray?
}

expect suspend fun pickImageFromUser(): PickedImage?
