package com.dlx.smartalarm.demo

import kotlinx.serialization.Serializable
import com.dlx.smartalarm.demo.components.image.TitleImageInfo
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
    val reminderOffsetMinutes: Int? = null,
    // 新增字段用于满足“添加表单”的描述与图标选择；保留默认值以兼容旧数据
    val description: String = "",
    val icon: String = "",
    val titleImage: TitleImageInfo? = null,
    // 提醒频率：none / once / daily / weekly
    val reminderFrequency: String = "none",
    // 提醒时间字符串，格式例如 "HH:mm"；仅用于 UI 展示和简单解析
    val reminderTime: String? = null,
    // 由计算器根据频率 + 时间计算出的下一次提醒触发时间（毫秒时间戳）；
    // 仅在需要调度通知时使用，存储在模型上方便各平台读取。
    val nextReminderTimeMillis: Long? = null,
    val isFavorite: Boolean = false,
    val tags: List<String> = emptyList()
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
