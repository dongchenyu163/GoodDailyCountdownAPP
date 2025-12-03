package com.dlx.smartalarm.demo

import androidx.compose.runtime.Composable

interface ReminderHandler {
    fun showReminder(card: CardData, message: String)
}

@Composable
expect fun rememberReminderHandler(): ReminderHandler
