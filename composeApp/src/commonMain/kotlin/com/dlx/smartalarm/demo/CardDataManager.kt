package com.dlx.smartalarm.demo

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

@Serializable
data class CardData(
    val id: Int,
    val title: String,
    val date: String,
	val remainingDays: Long,
    val reminderSent: Boolean = false,
    // 新增字段用于满足“添加表单”的描述与图标选择；保留默认值以兼容旧数据
    val description: String = "",
    val icon: String = "",
    val titleImage: TitleImageInfo? = null
)

expect class CardDataManager {
    suspend fun saveCards(cards: List<CardData>)
    suspend fun loadCards(): List<CardData>
}

// 跨平台文件操作的期望声明
expect suspend fun readFile(fileName: String): String?
expect suspend fun writeFile(fileName: String, content: String)

object CardDataStorage {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private const val FILE_NAME = "countdown_cards.json"

    suspend fun saveCards(cards: List<CardData>) {
        try {
            val jsonString = json.encodeToString(cards)
            println("saveCards():: Pre saving cards.")
            writeFile(FILE_NAME, jsonString)
        } catch (e: Exception) {
            println("Error saving cards: ${e.message}")
        }
    }

    suspend fun loadCards(): List<CardData> {
        return try {
            val jsonString = readFile(FILE_NAME)
            if (jsonString != null) {
                json.decodeFromString<List<CardData>>(jsonString)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("Error loading cards: ${e.message}")
            emptyList()
        }
    }
}
