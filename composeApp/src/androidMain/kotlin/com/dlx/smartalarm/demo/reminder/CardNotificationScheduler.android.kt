package com.dlx.smartalarm.demo.reminder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.dlx.smartalarm.demo.CardData
import com.dlx.smartalarm.demo.ReminderHandler
import com.dlx.smartalarm.demo.rememberReminderHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

/**
 * Android 平台调度实现：使用 ReminderHandler 发送系统通知
 */
class AndroidCardNotificationScheduler(
    private val reminderHandler: ReminderHandler,
    private val scope: CoroutineScope,
) : CardNotificationScheduler {

    private val scheduledJobs = mutableMapOf<Int, kotlinx.coroutines.Job>()

    @OptIn(ExperimentalTime::class)
    override fun schedule(card: CardData) {
        cancel(card.id)
        val next = computeNextReminder(card) ?: return
        
        val job = scope.launch {
            val delayMillis = next.triggerAtMillis - System.currentTimeMillis()
            if (delayMillis > 0) {
                kotlinx.coroutines.delay(delayMillis)
                val title = card.title.ifBlank { "倒计时提醒" }
                val message = "《$title》倒计时已经到期啦！"
                reminderHandler.showReminder(card, message)
            }
        }
        scheduledJobs[card.id] = job
    }

    override fun cancel(cardId: Int) {
        scheduledJobs.remove(cardId)?.cancel()
    }
}

@Composable
actual fun rememberCardNotificationScheduler(
    onFire: (CardData) -> Unit,
): CardNotificationScheduler {
    val reminderHandler = rememberReminderHandler()
    val scope = remember { CoroutineScope(Dispatchers.Default + SupervisorJob()) }
    return remember {
        AndroidCardNotificationScheduler(reminderHandler, scope)
    }
}