package com.dlx.smartalarm.demo

actual class CardDataManager {
    actual suspend fun saveCards(cards: List<CardData>) {
        CardDataStorage.saveCards(cards)
    }

    actual suspend fun loadCards(): List<CardData> {
        return CardDataStorage.loadCards()
    }
}
