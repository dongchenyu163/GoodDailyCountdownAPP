package com.dlx.smartalarm.demo

import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual class CardDataManager {
    actual suspend fun saveCards(cards: List<CardData>) {
        CardDataStorage.saveCards(cards)
    }

    actual suspend fun loadCards(): List<CardData> {
        return CardDataStorage.loadCards()
    }
}

actual suspend fun readFile(fileName: String): String? = withContext(Dispatchers.IO) {
    try {
        val file = File(fileName)
        if (file.exists()) {
            file.readText()
        } else {
            null
        }
    } catch (e: Exception) {
        println("Error reading file: ${e.message}")
        null
    }
}

actual suspend fun writeFile(fileName: String, content: String) = withContext(Dispatchers.IO) {
    try {
        val file = File(fileName)
        file.writeText(content)
        println("Cards saved to: ${file.absolutePath}")
    } catch (e: Exception) {
        println("Error writing file: ${e.message}")
    }
}
