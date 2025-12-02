package com.dlx.smartalarm.demo.components.image

import androidx.compose.ui.graphics.ImageBitmap
import kotlin.random.Random
import com.dlx.smartalarm.demo.PickedImage
import com.dlx.smartalarm.demo.PlatformImageIO
import com.dlx.smartalarm.demo.TitleImageStorage
import com.dlx.smartalarm.demo.components.image.TitleImageBitmapCache

const val TitleImageDefaultQuality = 80

fun generateImageUuid(): String {
    val chars = "0123456789abcdef"
    val builder = StringBuilder()
    repeat(32) {
        builder.append(chars[Random.nextInt(chars.length)])
    }
    return builder.toString()
}

suspend fun persistPickedImage(
    pickedImage: PickedImage,
    quality: Int = TitleImageDefaultQuality
): TitleImageInfo? {
    val decoded = PlatformImageIO.decodeImageBitmap(pickedImage.bytes) ?: return null
    val aspectRatio = if (decoded.height != 0) decoded.width.toFloat() / decoded.height else 0f
    val uuid = generateImageUuid()
    val jpegBytes = PlatformImageIO.compressToJpeg(pickedImage.bytes, quality) ?: pickedImage.bytes
    TitleImageStorage.save(uuid, jpegBytes)
    TitleImageBitmapCache.put(uuid, decoded)
    return TitleImageInfo(
        uuid = uuid,
        displayInfo = defaultDisplayInfo(aspectRatio)
    )
}

suspend fun replaceCardImage(
    existing: TitleImageInfo?,
    pickedImage: PickedImage,
    quality: Int = TitleImageDefaultQuality
): TitleImageInfo? {
    val info = persistPickedImage(pickedImage, quality)
    if (info != null) {
        existing?.uuid?.let {
            TitleImageStorage.delete(it)
            TitleImageBitmapCache.remove(it)
        }
    }
    return info
}

suspend fun loadImageBitmap(info: TitleImageInfo?): ImageBitmap? {
    val uuid = info?.uuid ?: return null
    TitleImageBitmapCache.get(uuid)?.let { return it }
    val bytes = TitleImageStorage.load(uuid) ?: return null
    val bitmap = PlatformImageIO.decodeImageBitmap(bytes)
    if (bitmap != null) {
        TitleImageBitmapCache.put(uuid, bitmap)
    }
    return bitmap
}
