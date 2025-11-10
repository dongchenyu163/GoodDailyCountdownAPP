package com.dlx.smartalarm.demo

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.darwin.NSObject
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * iOS平台的CardDataManager实现
 * 使用iOS的Documents目录进行数据存储
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

/**
 * 在iOS平台读取文件
 * 使用iOS的Documents目录作为存储位置
 * @param fileName 文件名
 * @return 文件内容，如果文件不存在则创建默认内容
 */
@OptIn(ExperimentalForeignApi::class)
actual suspend fun readFile(fileName: String): String? {
    return try {
        // 获取iOS Documents目录路径
        val documentsPath = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        ).firstOrNull() as? String

        if (documentsPath != null) {
            val filePath = "$documentsPath/$fileName"
            val fileManager = NSFileManager.defaultManager

            // 检查文件是否存在
            if (fileManager.fileExistsAtPath(filePath)) {
                // 读取文件内容
                NSString.stringWithContentsOfFile(
                    filePath,
                    encoding = NSUTF8StringEncoding,
                    error = null
                )
            } else {
                // 初次打开时创建默认空数组
                if (fileName == "cards.json") {
                    val defaultContent = "[]"
                    writeFile(fileName, defaultContent)
                    defaultContent
                } else {
                    null
                }
            }
        } else {
            null
        }
    } catch (e: Exception) {
        println("Error reading file on iOS: ${e.message}")
        null
    }
}


/**
 * 在iOS平台写入文件
 * 将内容保存到iOS的Documents目录
 * @param fileName 文件名
 * @param content 要写入的内容
 */
@OptIn(ExperimentalForeignApi::class)
actual suspend fun writeFile(fileName: String, content: String) {
    try {
        // 获取iOS Documents目录路径
        val documentsPath = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        ).firstOrNull() as? String

        if (documentsPath != null) {
            val filePath = "$documentsPath/$fileName"
            val nsString = NSString.create(string = content)

            // 原子性写入文件，确保数据完整性
            nsString.writeToFile(
                filePath,
                atomically = true,
                encoding = NSUTF8StringEncoding,
                error = null
            )
            println("Cards saved to iOS Documents directory: $filePath")
        } else {
            println("Could not access iOS Documents directory")
        }
    } catch (e: Exception) {
        println("Error writing file on iOS: ${e.message}")
    }
}