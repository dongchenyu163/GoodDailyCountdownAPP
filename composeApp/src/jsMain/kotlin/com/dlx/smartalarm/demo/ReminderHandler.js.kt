package com.dlx.smartalarm.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.browser.window

import org.w3c.notifications.Notification
import org.w3c.notifications.NotificationOptions


private class WebReminderHandler : ReminderHandler {
    override fun showReminder(card: CardData, message: String) {
        if (isNotificationSupported() && js("Notification.permission") == "granted") {
            val title = card.title.ifBlank { "倒计时提醒" }
            Notification(title, NotificationOptions(body = message))
        } else {
            window.alert(message)
        }
    }
}

@Composable
actual fun rememberReminderHandler(): ReminderHandler {
    val handler = remember { WebReminderHandler() }
    LaunchedEffect(Unit) {
        if (isNotificationSupported() && js("Notification.permission") == "default") {
            Notification.requestPermission {}
        }
    }
    return handler
}

private fun isNotificationSupported(): Boolean {
    return js("typeof Notification !== 'undefined'") as Boolean
}