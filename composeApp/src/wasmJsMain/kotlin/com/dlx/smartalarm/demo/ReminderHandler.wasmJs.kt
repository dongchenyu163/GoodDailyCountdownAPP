package com.dlx.smartalarm.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.browser.window
import org.w3c.notifications.DEFAULT
import org.w3c.notifications.GRANTED
import org.w3c.notifications.Notification
import org.w3c.notifications.NotificationOptions
import org.w3c.notifications.NotificationPermission
import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
private class WasmReminderHandler : ReminderHandler {
    override fun showReminder(card: CardData, message: String) {
        if (isNotificationSupported() && Notification.permission == NotificationPermission.GRANTED) {
            val title = card.title.ifBlank { "倒计时提醒" }
            Notification(title, NotificationOptions(body = message))
        } else {
            window.alert(message)
        }
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
@Composable
actual fun rememberReminderHandler(): ReminderHandler {
    val handler = remember { WasmReminderHandler() }
    LaunchedEffect(Unit) {
        if (isNotificationSupported() && Notification.permission == NotificationPermission.DEFAULT) {
            Notification.requestPermission {}
        }
    }
    return handler
}

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => typeof Notification !== 'undefined'")
private external fun isNotificationSupported(): Boolean
