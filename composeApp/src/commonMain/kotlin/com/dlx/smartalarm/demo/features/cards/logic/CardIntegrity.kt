package com.dlx.smartalarm.demo.features.cards.logic

import com.dlx.smartalarm.demo.CardData
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.*

@OptIn(ExperimentalTime::class)
fun validateAndFixCardData(card: CardData): CardData {
    val parsedDate = runCatching { LocalDate.parse(card.date) }.getOrNull()
    val timeZone = TimeZone.currentSystemDefault()
    val today = kotlin.time.Clock.System.todayIn(timeZone)
    val newRemainingDays = if (parsedDate != null) {
        (parsedDate.toEpochDays() - today.toEpochDays()).coerceAtLeast(0)
    } else {
        card.remainingDays
    }
    val shouldResetReminder = parsedDate != null && parsedDate >= today && card.reminderSent
    return card.copy(
        remainingDays = newRemainingDays,
        reminderSent = if (shouldResetReminder) false else card.reminderSent
    )
}