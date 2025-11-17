package com.dlx.smartalarm.demo

import androidx.compose.ui.graphics.ImageBitmap

object TitleImageBitmapCache {
    private val cache = mutableMapOf<String, ImageBitmap?>()

    fun get(uuid: String): ImageBitmap? = cache[uuid]

    fun put(uuid: String, bitmap: ImageBitmap?) {
        cache[uuid] = bitmap
    }

    fun remove(uuid: String) {
        cache.remove(uuid)
    }

    fun clear() {
        cache.clear()
    }
}
