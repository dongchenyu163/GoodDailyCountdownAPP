package com.dlx.smartalarm.demo.reminder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.dlx.smartalarm.demo.CardData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.datetime.*
import platform.Foundation.NSDateComponents
import platform.UserNotifications.*
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * iOS 端使用系统通知中心 (UNUserNotificationCenter) 的提醒调度器：
 * - schedule(card): 为卡片安排本地通知（通知栏/锁屏横幅）
 * - cancel(cardId): 取消对应卡片的通知
 */
@OptIn(ExperimentalForeignApi::class)
private class IosCardNotificationScheduler(
    private val onFire: (CardData) -> Unit, // 目前未使用，保留以便将来前台同时弹 Compose 对话框
) : CardNotificationScheduler {

    private val center: UNUserNotificationCenter =
        UNUserNotificationCenter.currentNotificationCenter()

    // 在前台也显示通知的 delegate（横幅/声音/角标）
    private class ForegroundDelegate : NSObject(), UNUserNotificationCenterDelegateProtocol {
        override fun userNotificationCenter(
            center: UNUserNotificationCenter,
            willPresentNotification: UNNotification,
            withCompletionHandler: (UNNotificationPresentationOptions) -> Unit
        ) {
            withCompletionHandler(
                UNNotificationPresentationOptionAlert or
                    UNNotificationPresentationOptionSound or
                    UNNotificationPresentationOptionBadge
            )
        }

        override fun userNotificationCenter(
            center: UNUserNotificationCenter,
            didReceiveNotificationResponse: UNNotificationResponse,
            withCompletionHandler: () -> Unit
        ) { withCompletionHandler() }
    }

    // 持有引用以避免被 GC
    private val foregroundDelegate = ForegroundDelegate()

    // 开关：应用启动后安排一条 5 秒后的调试通知，验证通知通道
    private val DEBUG_BOOT_NOTIFICATION = true

    init {
        println("iOS scheduler init starting")
        // 主线程：设置 delegate 与请求权限；必要时安排调试通知
        dispatch_async(dispatch_get_main_queue()) {
            center.setDelegate(foregroundDelegate)
            center.requestAuthorizationWithOptions(
                options = UNAuthorizationOptionAlert or
                        UNAuthorizationOptionSound or
                        UNAuthorizationOptionBadge,
            ) { granted, error ->
                if (!granted) {
                    println("iOS notifications: permission NOT granted, error=$error")
                } else {
                    println("iOS notifications: permission granted")
                    if (DEBUG_BOOT_NOTIFICATION) {
                        try {
                            val content = UNMutableNotificationContent().apply {
                                setTitle("调试：iOS 通知测试")
                                setBody("如果你看到这条通知，说明 iOS 通知通道正常（前台也会显示）。")
                                setSound(UNNotificationSound.defaultSound())
                            }
                            val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
                                timeInterval = 5.0,
                                repeats = false,
                            )
                            val request = UNNotificationRequest.requestWithIdentifier(
                                identifier = "debug_boot_notification",
                                content = content,
                                trigger = trigger,
                            )
                            dispatch_async(dispatch_get_main_queue()) {
                                center.addNotificationRequest(request) { err ->
                                    if (err != null) println("iOS debug boot notification: failed to add, error=$err")
                                    else println("iOS debug boot notification: scheduled (in 5s)")
                                }
                            }
                        } catch (t: Throwable) {
                            println("iOS debug boot notification: exception ${t.message}")
                        }
                    }
                }
            }
        }
    }

    override fun schedule(card: CardData) {
        println("iOS schedule(): called for card=${card.id}, freq=${card.reminderFrequency}, time=${card.reminderTime}")

        val next = computeNextReminder(
            card = card,
            now = Clock.System.now(),
            timeZone = TimeZone.currentSystemDefault(),
        ) ?: run {
            println("iOS schedule(): no next reminder for card=${card.id}, cancel")
            cancel(card.id)
            return
        }

        cancel(card.id)

        val triggerAtMillis = next.triggerAtMillis
        val nowMillis = Clock.System.now().toEpochMilliseconds().toDouble()
        val delaySeconds = (triggerAtMillis.toDouble() - nowMillis) / 1000.0
        if (delaySeconds <= 0.0) {
            println("iOS schedule(): trigger time already passed for card=${card.id}, triggerAt=$triggerAtMillis, now=$nowMillis")
            return
        }

        val content = UNMutableNotificationContent().apply {
            setTitle(card.title)
            val bodyText = if (card.description.isNotBlank()) card.description else "你的倒计时任务到了：${card.title}"
            setBody(bodyText)
            setSound(UNNotificationSound.defaultSound())
        }

        // 重复类型：使用 Calendar 触发，确保首个触发点就是 next.triggerAtMillis 对应的时间点
        val trigger = if (next.repeatIntervalMillis != null) {
            val tz = TimeZone.currentSystemDefault()
            val firstInstant = Instant.fromEpochMilliseconds(triggerAtMillis)
            val ldt = firstInstant.toLocalDateTime(tz)

            val comps = NSDateComponents().apply {
                // 每日：设置小时/分钟；每周：再设置 weekday（iOS: 周日=1 ... 周六=7）
                setHour(ldt.hour.toLong())
                setMinute(ldt.minute.toLong())
                setSecond(0L)
                if (card.reminderFrequency == "weekly") {
                    val iosWeekday = when (ldt.date.dayOfWeek) {
                        DayOfWeek.MONDAY -> 2
                        DayOfWeek.TUESDAY -> 3
                        DayOfWeek.WEDNESDAY -> 4
                        DayOfWeek.THURSDAY -> 5
                        DayOfWeek.FRIDAY -> 6
                        DayOfWeek.SATURDAY -> 7
                        DayOfWeek.SUNDAY -> 1
                    }.toLong()
                    setWeekday(iosWeekday)
                }
            }
            println("iOS schedule(): calendar repeating for card=${card.id}, hour=${ldt.hour}, minute=${ldt.minute}, freq=${card.reminderFrequency}")
            UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                comps,
                true,
            )
        } else {
            println("iOS schedule(): one-shot trigger for card=${card.id}, delaySeconds=$delaySeconds")
            UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
                timeInterval = delaySeconds,
                repeats = false,
            )
        }

        val identifier = notificationIdentifier(card.id)
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = identifier,
            content = content,
            trigger = trigger,
        )

        // 主线程提交请求更稳妥
        dispatch_async(dispatch_get_main_queue()) {
            center.addNotificationRequest(request) { error ->
                if (error != null) println("iOS schedule(): failed to add notification for card=${card.id}, error=$error")
                else println("iOS schedule(): notification scheduled for card=${card.id}, triggerAt=$triggerAtMillis")
            }
        }
    }

    override fun cancel(cardId: Int) {
        val identifier = notificationIdentifier(cardId)
        center.removePendingNotificationRequestsWithIdentifiers(listOf(identifier))
        center.removeDeliveredNotificationsWithIdentifiers(listOf(identifier))
    }

    private fun notificationIdentifier(cardId: Int): String = "card_$cardId"
}

@Composable
actual fun rememberCardNotificationScheduler(
    onFire: (CardData) -> Unit,
): CardNotificationScheduler {
    return remember(onFire) { IosCardNotificationScheduler(onFire) }
}
