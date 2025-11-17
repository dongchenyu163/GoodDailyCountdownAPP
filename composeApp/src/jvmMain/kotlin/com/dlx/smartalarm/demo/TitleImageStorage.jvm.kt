package com.dlx.smartalarm.demo

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import java.awt.FileDialog
import java.awt.Frame
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

actual object TitleImageStorage {
    private val imageDir: File by lazy {
        File(TitleImageDirectoryName).apply { if (!exists()) mkdirs() }
    }

    private fun fileFor(uuid: String) = File(imageDir, uuid)

    actual suspend fun save(uuid: String, bytes: ByteArray) = withContext(Dispatchers.IO) {
        fileFor(uuid).writeBytes(bytes)
    }

    actual suspend fun load(uuid: String): ByteArray? = withContext(Dispatchers.IO) {
        val file = fileFor(uuid)
        if (file.exists()) file.readBytes() else null
    }

    actual suspend fun delete(uuid: String): Unit = withContext(Dispatchers.IO) {
        runCatching { fileFor(uuid).takeIf { it.exists() }?.delete() }
        Unit
    }

    actual fun resolveAbsolutePath(uuid: String): String = fileFor(uuid).absolutePath
}

actual object PlatformImageIO {
    actual fun decodeImageBitmap(bytes: ByteArray): ImageBitmap? {
        return runCatching {
            val image = Image.makeFromEncoded(bytes)
            image.asImageBitmap()
        }.getOrNull()
    }

    actual fun compressToJpeg(bytes: ByteArray, quality: Int): ByteArray? {
        return runCatching {
            val bufferedImage = ImageIO.read(ByteArrayInputStream(bytes)) ?: return null
            val writer = ImageIO.getImageWritersByFormatName("jpeg").asSequence().firstOrNull() ?: return null
            val output = ByteArrayOutputStream()
            val ios = ImageIO.createImageOutputStream(output)
            writer.output = ios
            val params = writer.defaultWriteParam
            params.compressionMode = ImageWriteParam.MODE_EXPLICIT
            params.compressionQuality = (quality.coerceIn(0, 100)) / 100f
            writer.write(null, IIOImage(bufferedImage, null, null), params)
            writer.dispose()
            ios.close()
            output.toByteArray()
        }.getOrNull()
    }
}

actual suspend fun pickImageFromUser(): PickedImage? = withContext(Dispatchers.IO) {
    val dialog = FileDialog(null as Frame?, "选择标题图片", FileDialog.LOAD)
    dialog.isVisible = true
    val file = dialog.files?.firstOrNull() ?: dialog.file?.let { File(dialog.directory, it) }
    if (file == null || !file.exists()) return@withContext null
    val bytes = file.readBytes()
    val mime = runCatching { Files.probeContentType(file.toPath()) }.getOrNull()
    PickedImage(bytes = bytes, fileName = file.name, mimeType = mime)
}
