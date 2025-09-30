package com.dlx.smartalarm.demo

import kotlinx.browser.localStorage

actual class CardDataManager {
    actual suspend fun saveCards(cards: List<CardData>) {
        CardDataStorage.saveCards(cards)
    }

    actual suspend fun loadCards(): List<CardData> {
        return CardDataStorage.loadCards()
    }
}

actual suspend fun readFile(fileName: String): String? {
    return try {
        // 在WASM平台使用localStorage
        localStorage.getItem(fileName)
    } catch (e: Exception) {
        println("Error reading from localStorage: ${e.message}")
        null
    }
}

actual suspend fun writeFile(fileName: String, content: String) {
    try {
        // 在WASM平台使用localStorage
        localStorage.setItem(fileName, content)
        println("Cards saved to localStorage with key: $fileName")
    } catch (e: Exception) {
        println("Error writing to localStorage: ${e.message}")
    }
}
