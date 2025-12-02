package com.dlx.smartalarm.demo.reminder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.dlx.smartalarm.demo.CardData
import kotlinx.coroutines.*

/**
 * JVM 桌面/浏览器调度实现：使用协程定时，到点打印日志或触发回调。
 */
class JvmCardNotificationScheduler(
    private val scope: CoroutineScope,
    private val onFire: (CardData) -> Unit,
) : CardNotificationScheduler {

    private val jobs = mutableMapOf<Int, Job>()

    override fun schedule(card: CardData) {
        cancel(card.id)
        val next = computeNextReminder(card) ?: return
        val delayMillis = next.triggerAtMillis - System.currentTimeMillis()
        if (delayMillis <= 0) return

        val job = scope.launch {
            delay(delayMillis)
            onFire(card)
            // 简易实现：不做重复调度；如需每日/每周循环，可在这里再次调用 schedule(card)
        }
        jobs[card.id] = job
    }

    override fun cancel(cardId: Int) {
        jobs.remove(cardId)?.cancel()
    }
}

@Composable
actual fun rememberCardNotificationScheduler(
    onFire: (CardData) -> Unit,
): CardNotificationScheduler {
    val scope = remember { CoroutineScope(Dispatchers.Default + SupervisorJob()) }
    return remember {
        JvmCardNotificationScheduler(scope, onFire)
    }
}
