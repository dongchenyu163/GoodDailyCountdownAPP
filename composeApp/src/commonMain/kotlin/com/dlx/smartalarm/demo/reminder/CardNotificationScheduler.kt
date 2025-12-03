package com.dlx.smartalarm.demo.reminder

import androidx.compose.runtime.Composable
import com.dlx.smartalarm.demo.CardData

/**
 * 跨平台卡片提醒调度接口，由各平台实现系统级或模拟提醒。
 */
interface CardNotificationScheduler {
    fun schedule(card: CardData)
    fun cancel(cardId: Int)
}

/**
 * 在 Compose 环境下记忆一个平台实现的调度器。
 * onFire 回调用于在到点时触发 UI 弹框或其他行为。
 */
@Composable
expect fun rememberCardNotificationScheduler(
    onFire: (CardData) -> Unit = {},
): CardNotificationScheduler
