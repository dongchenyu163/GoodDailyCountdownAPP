package com.dlx.smartalarm.demo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class TagLogicTest {
    @Test
    fun ensureFavoriteAddsWhenMissing() {
        val base = listOf(Tag(id = "a", name = "Work"))
        val ensured = TagRepository.ensureFavorite(base)
        assertTrue(ensured.any { it.id == TagRepository.favoriteId() })
    }

    @Test
    fun favoriteToggleUpdatesTags() {
        val fav = TagRepository.favoriteId()
        val card = CardData(id = 1, title = "t", date = "2025-01-01", remainingDays = 1, isFavorite = false, tags = emptyList())
        val nextFav = card.copy(isFavorite = true, tags = (card.tags + fav).distinct())
        assertTrue(nextFav.isFavorite)
        assertTrue(nextFav.tags.contains(fav))
        val nextUnfav = nextFav.copy(isFavorite = false, tags = nextFav.tags.filter { it != fav })
        assertFalse(nextUnfav.isFavorite)
        assertFalse(nextUnfav.tags.contains(fav))
    }
}

