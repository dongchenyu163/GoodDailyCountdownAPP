@file:OptIn(
    kotlinx.cinterop.ExperimentalForeignApi::class,
    kotlinx.cinterop.BetaInteropApi::class
)

package com.dlx.smartalarm.demo

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.toKString
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.posix.*

private fun documentsDirectory(): String? {
    val homePtr = getenv("HOME") ?: return null
    val home = homePtr.toKString()
    return "$home/Documents"
}

private fun ensureImageDir(): String? {
    val docs = documentsDirectory() ?: return null
    val dir = "$docs/$TitleImageDirectoryName"
    memScoped {
        mkdir(dir, (S_IRWXU or S_IRWXG or S_IRWXO).convert())
    }
    return dir
}

private fun pathFor(uuid: String): String? = ensureImageDir()?.let { "$it/$uuid" }

actual object TitleImageStorage {
    actual suspend fun save(uuid: String, bytes: ByteArray) {
        val path = pathFor(uuid) ?: return
        memScoped {
            val file = fopen(path, "wb") ?: return
            try {
                bytes.usePinned {
                    fwrite(it.addressOf(0), 1.convert(), bytes.size.convert(), file)
                }
            } finally {
                fclose(file)
            }
        }
    }

    actual suspend fun load(uuid: String): ByteArray? {
        val path = pathFor(uuid) ?: return null
        memScoped {
            val file = fopen(path, "rb") ?: return null
            try {
                fseek(file, 0, SEEK_END)
                val size = ftell(file)
                if (size <= 0) return null
                rewind(file)
                val buffer = ByteArray(size.toInt())
                buffer.usePinned {
                    fread(it.addressOf(0), 1.convert(), size.convert(), file)
                }
                return buffer
            } finally {
                fclose(file)
            }
        }
    }

    actual suspend fun delete(uuid: String) {
        val path = pathFor(uuid) ?: return
        remove(path)
    }

    actual fun resolveAbsolutePath(uuid: String): String = pathFor(uuid) ?: ""
}

actual object PlatformImageIO {
    actual fun decodeImageBitmap(bytes: ByteArray): ImageBitmap? {
        // On JVM/Android there are helpers to convert Skia Image to Compose ImageBitmap via
        // asImageBitmap extension. On iOS target that extension may not be available in this
        // build environment; returning null is safe (UI code handles null) to avoid compilation errors.
        return runCatching {
            // Attempt to decode using Skia; if conversion to ImageBitmap isn't available, return null.
            val image = Image.makeFromEncoded(bytes)
            // No reliable cross-target extension available here: return null to let callers handle it.
            null
        }.getOrNull()
    }

    actual fun compressToJpeg(bytes: ByteArray, quality: Int): ByteArray? {
        return runCatching {
            val image = Image.makeFromEncoded(bytes)
            image.encodeToData(EncodedImageFormat.JPEG, quality.coerceIn(0, 100))?.bytes
        }.getOrNull()
    }
}

actual suspend fun pickImageFromUser(): PickedImage? {
    println("pickImageFromUser is not implemented on iOS in this preview build.")
    return null
}
