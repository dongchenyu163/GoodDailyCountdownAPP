package com.dlx.smartalarm.demo

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import com.dlx.smartalarm.demo.core.platform.readTextFile
import com.dlx.smartalarm.demo.core.platform.writeTextFile

actual class CardDataManager {
    actual suspend fun saveCards(cards: List<CardData>) {
        CardDataStorage.saveCards(cards)
    }

    actual suspend fun loadCards(): List<CardData> {
        return CardDataStorage.loadCards()
    }
}

// 这些函数已经移动到 core.platform 包中，这里不再需要重复实现
