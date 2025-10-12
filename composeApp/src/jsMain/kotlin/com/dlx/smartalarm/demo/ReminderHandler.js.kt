package com.dlx.smartalarm.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.browser.window
import org.w3c.notifications.Notification
import org.w3c.notifications.NotificationOptions
import org.w3c.notifications.NotificationPermission

private class WebReminderHandler : ReminderHandler {
    override fun showReminder(card: CardData, message: String) {
        if (isNotificationSupported() && Notification.permission == NotificationPermission.granted) {
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
        if (isNotificationSupported() && Notification.permission == NotificationPermission.default) {
            Notification.requestPermission {}
        }
    }
    return handler
}

private fun isNotificationSupported(): Boolean {
    return js("typeof Notification !== 'undefined'") as Boolean
}
