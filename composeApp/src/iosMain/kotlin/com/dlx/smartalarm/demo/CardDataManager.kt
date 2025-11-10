@file:OptIn(
    kotlinx.cinterop.BetaInteropApi::class,
    kotlinx.cinterop.ExperimentalForeignApi::class
)

package com.dlx.smartalarm.demo

import kotlinx.cinterop.*
import platform.posix.*

/**
 * iOS平台的CardDataManager实现
 * 使用iOS的Documents目录进行数据存储（通过 POSIX 文件 API）
 */
actual class CardDataManager {
    /**
     * 保存卡片数据到iOS Documents目录
     */
    actual suspend fun saveCards(cards: List<CardData>) {
        CardDataStorage.saveCards(cards)
    }

    /**
     * 从iOS Documents目录加载卡片数据
     */
    actual suspend fun loadCards(): List<CardData> {
        return CardDataStorage.loadCards()
    }
}

private fun getDocumentsPath(): String? {
    // 使用环境变量 HOME 来定位用户目录（在模拟器和设备上通常可用）
    val homePtr = getenv("HOME") ?: return null
    val home = homePtr.toKString()
    return "$home/Documents"
}

/**
 * POSIX 读取文件实现
 */
actual suspend fun readFile(fileName: String): String? {
    val documentsPath = getDocumentsPath() ?: return null
    val fullPath = "$documentsPath/$fileName"

    memScoped {
        val file = fopen(fullPath, "rb") ?: return null
        try {
            if (fseek(file, 0, SEEK_END) != 0) return null
            val size = ftell(file)
            if (size <= 0) return null
            rewind(file)

            val bytes = ByteArray(size.toInt())
            val buffer = allocArray<ByteVar>(size.toInt())
            val read = fread(buffer, 1.convert(), size.convert(), file).toInt()
            if (read > 0) {
                for (i in 0 until read) {
                    bytes[i] = buffer[i]
                }
                return bytes.decodeToString()
            }
            return null
        } finally {
            fclose(file)
        }
    }
}

/**
 * POSIX 写文件实现（原子性写入未严格保证，使用覆盖写）
 */
actual suspend fun writeFile(fileName: String, content: String) {
    val documentsPath = getDocumentsPath()
    if (documentsPath == null) {
        println("Could not access Documents directory")
        return
    }
    val fullPath = "$documentsPath/$fileName"

    memScoped {
        val file = fopen(fullPath, "wb")
        if (file == null) {
            println("Failed to open file for writing: $fullPath")
            return
        }
        try {
            val bytes = content.encodeToByteArray()
            val buffer = allocArray<ByteVar>(bytes.size)
            for (i in bytes.indices) buffer[i] = bytes[i]
            val written = fwrite(buffer, 1.convert(), bytes.size.convert(), file).toLong()
            if (written != bytes.size.toLong()) {
                println("Warning: not all bytes were written to $fullPath")
            }
        } finally {
            fclose(file)
        }
    }
}