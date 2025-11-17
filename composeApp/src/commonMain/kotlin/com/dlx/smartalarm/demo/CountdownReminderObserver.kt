package com.dlx.smartalarm.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO

@Composable
fun CountdownReminderObserver(
	card: CardData,
	reminderHandler: ReminderHandler,
	onCardUpdate: (CardData) -> Unit,
	onDialogRequest: (CardData) -> Unit
) {
	LaunchedEffect(card.id, card.date, card.reminderSent) {
		val targetDate = runCatching { LocalDate.parse(card.date) }.getOrNull() ?: return@LaunchedEffect
		val timeZone = TimeZone.currentSystemDefault()
		val now = Clock.System.now()
		val today = now.toLocalDateTime(timeZone).date
		val daysRemaining = (targetDate.toEpochDays() - today.toEpochDays()).coerceAtLeast(0)

		// 仅当计算出的剩余天数与卡片中的剩余天数不匹配时才更新（但不更新已发送提醒的卡片的剩余天数）
		if (!card.reminderSent && daysRemaining != card.remainingDays) {
			onCardUpdate(card.copy(remainingDays = daysRemaining))
		}

		if (card.reminderSent) {
			// 如果卡片已发送提醒但剩余天数不是0，说明用户更新了截止日期，应该重置提醒状态
			if (card.remainingDays != 0) {
				onCardUpdate(card.copy(reminderSent = false)) // 重置提醒发送状态
			}
			return@LaunchedEffect
		}

		val dueInstant = targetDate.atStartOfDayIn(timeZone)
		val durationUntilDue: Duration = dueInstant - now
		if (!durationUntilDue.isPositive()) {
			val updatedCard = card.copy(remainingDays = 0, reminderSent = true)
			onCardUpdate(updatedCard)
			onDialogRequest(updatedCard)
			reminderHandler.showReminder(updatedCard, buildReminderMessage(updatedCard))
			return@LaunchedEffect
		}

		delay(durationUntilDue)
		val updatedCard = card.copy(remainingDays = 0, reminderSent = true)
		onCardUpdate(updatedCard)
		onDialogRequest(updatedCard)
		reminderHandler.showReminder(updatedCard, buildReminderMessage(updatedCard))
	}
}

private fun buildReminderMessage(card: CardData): String {
	val title = card.title.ifBlank { "倒计时提醒" }
	return "《$title》倒计时已经到期啦！"
}

private fun Duration.isPositive(): Boolean = this > ZERO
