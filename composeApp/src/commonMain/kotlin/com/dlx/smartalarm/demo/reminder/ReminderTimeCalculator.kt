package com.dlx.smartalarm.demo.reminder

import com.dlx.smartalarm.demo.CardData
import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * 描述一次即将触发的提醒：包括触发时间和可选的重复间隔（毫秒）。
 */
data class NextReminder(
    val triggerAtMillis: Long,
    val repeatIntervalMillis: Long? = null,
)

/**
 * 根据卡片的目标日期、提醒频率与时间，计算下一次提醒时间。
 * 返回 null 表示当前不需要安排系统提醒（未设置或已过期）。
 */
@OptIn(kotlin.time.ExperimentalTime::class)
fun computeNextReminder(
    card: CardData,
    now: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): NextReminder? {
    if (card.reminderFrequency == "none") return null

    val targetDate = runCatching { LocalDate.parse(card.date) }.getOrNull() ?: return null
    val timeParts = card.reminderTime?.split(":") ?: return null
    val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: return null
    val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

    // 组合成本地日期时间
    val localDateTime = LocalDateTime(
        targetDate.year,
        targetDate.monthNumber,
        targetDate.dayOfMonth,
        hour,
        minute
    )
    // 转换为 Instant 便于做加减
    var trigger = localDateTime.toInstant(timeZone)

    return when (card.reminderFrequency) {
        "once" -> {
            if (trigger <= now) null else NextReminder(trigger.toEpochMilliseconds(), null)
        }
        "daily" -> {
            // 若今天的时间已过，就顺延到下一天同一时间
            while (trigger <= now) {
                trigger = trigger.plus(1, DateTimeUnit.DAY, timeZone)
            }
            NextReminder(
                triggerAtMillis = trigger.toEpochMilliseconds(),
                repeatIntervalMillis = 24L * 60L * 60L * 1000L,
            )
        }
        "weekly" -> {
            // 每周同一时间：以目标日期所在周为起点，每次 +7 天
            while (trigger <= now) {
                trigger = trigger.plus(7, DateTimeUnit.DAY, timeZone)
            }
            NextReminder(
                triggerAtMillis = trigger.toEpochMilliseconds(),
                repeatIntervalMillis = 7L * 24L * 60L * 60L * 1000L,
            )
        }
        else -> null
    }
}
