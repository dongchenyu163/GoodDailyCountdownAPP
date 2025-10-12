package com.dlx.smartalarm.demo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

private const val REMINDER_CHANNEL_ID = "countdown_reminder_channel"
private const val REMINDER_CHANNEL_NAME = "Countdown Reminders"

private class AndroidReminderHandler(private val appContext: Context) : ReminderHandler {
    private val notificationManager = NotificationManagerCompat.from(appContext)

    fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                REMINDER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "提示倒计时到期的通知"
            }
            val systemManager = appContext.getSystemService(NotificationManager::class.java)
            systemManager?.createNotificationChannel(channel)
        }
    }

    override fun showReminder(card: CardData, message: String) {
        val title = card.title.ifBlank { appContext.getString(R.string.app_name) }
        val notification = NotificationCompat.Builder(appContext, REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        runCatching {
            notificationManager.notify(card.id, notification)
        }.onFailure { error ->
            println("Failed to show reminder notification: ${error.message}")
        }
    }
}

@Composable
actual fun rememberReminderHandler(): ReminderHandler {
    val context = LocalContext.current.applicationContext
    val handler = remember { AndroidReminderHandler(context) }
    LaunchedEffect(Unit) {
        handler.ensureChannel()
    }
    return handler
}
