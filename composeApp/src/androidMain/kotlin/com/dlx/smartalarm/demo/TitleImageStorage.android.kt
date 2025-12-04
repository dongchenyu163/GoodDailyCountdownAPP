package com.dlx.smartalarm.demo

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import com.dlx.smartalarm.demo.core.platform.getPlatformDataDirectory

private fun imageDir(): File? {
    val dataDir = getPlatformDataDirectory()
    return File(dataDir, TitleImageDirectoryName).apply {
        if (!exists()) mkdirs()
    }
}

actual object TitleImageStorage {
    actual suspend fun save(uuid: String, bytes: ByteArray) = withContext(Dispatchers.IO) {
        val dir = imageDir() ?: return@withContext
        File(dir, uuid).writeBytes(bytes)
    }

    actual suspend fun load(uuid: String): ByteArray? = withContext(Dispatchers.IO) {
        val dir = imageDir() ?: return@withContext null
        File(dir, uuid).takeIf { it.exists() }?.readBytes()
    }

    actual suspend fun delete(uuid: String) = withContext(Dispatchers.IO) {
        val dir = imageDir() ?: return@withContext
        File(dir, uuid).takeIf { it.exists() }?.delete()
    }

    actual fun resolveAbsolutePath(uuid: String): String {
        val dir = imageDir()
        return if (dir != null) File(dir, uuid).absolutePath else ""
    }
}

actual object PlatformImageIO {
    actual fun decodeImageBitmap(bytes: ByteArray): ImageBitmap? {
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
        return bitmap.asImageBitmap()
    }

    actual fun compressToJpeg(bytes: ByteArray, quality: Int): ByteArray? {
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
        val output = ByteArrayOutputStream()
        bitmap.compress(
            android.graphics.Bitmap.CompressFormat.JPEG,
            quality.coerceIn(0, 100),
            output
        )
        return output.toByteArray()
    }
}

actual suspend fun pickImageFromUser(): PickedImage? {
    return ImagePicker.pickImage()
}
