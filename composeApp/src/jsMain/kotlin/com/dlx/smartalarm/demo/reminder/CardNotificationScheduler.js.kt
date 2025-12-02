package com.dlx.smartalarm.demo.reminder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.dlx.smartalarm.demo.CardData

// Web/JS 端的占位实现：当前不做真正的通知，仅用于满足 expect/actual。
private class JsCardNotificationScheduler : CardNotificationScheduler {
    override fun schedule(card: CardData) {
        // 可以在这里用 window.setTimeout + console.log 做简单调试实现
    }

    override fun cancel(cardId: Int) {
        // 暂不实现取消逻辑
    }
}

@Composable
actual fun rememberCardNotificationScheduler(
    onFire: (CardData) -> Unit,
): CardNotificationScheduler {
    return remember { JsCardNotificationScheduler() }
}
