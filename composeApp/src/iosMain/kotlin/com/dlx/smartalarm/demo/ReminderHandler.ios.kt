package com.dlx.smartalarm.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import platform.UIKit.*
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * iOS 平台的提醒处理器实现
 * 使用 UIKit 的 UIAlertController 在屏幕上显示一个简单的弹窗提醒。
 *
 * 说明：
 * - 由于 iOS 上没有 LocalContext（Android）或浏览器全局对象（Web），
 *   我们使用 UIApplication 获取当前可见的 UIViewController 来展示弹窗。
 * - 保证在主线程上执行 UI 操作（使用 dispatch_async 到主队列）。
 */
private class IOSReminderHandler : ReminderHandler {
    override fun showReminder(card: CardData, message: String) {
        val title = if (card.title.isBlank()) "倒计时提醒" else card.title

        // 在主线程展示 UIAlertController
        dispatch_async(dispatch_get_main_queue()) {
            try {
                val application = UIApplication.sharedApplication

                // 获取根控制器（尽量兼容 scene 与非 scene 的情况）
                var rootController: UIViewController? = null

                // 1) 尝试通过 keyWindow 并安全 cast 成 UIWindow
                rootController = (application.keyWindow as? UIWindow)?.rootViewController

                // 2) 如果上面为空，尝试通过 delegate 的 window
                if (rootController == null) {
                    rootController = (application.delegate?.window as? UIWindow)?.rootViewController
                }

                // 3) 仍为空则遍历 application.windows，逐一 cast 为 UIWindow 并取 rootViewController
                if (rootController == null) {
                    val windows = application.windows
                    if (!windows.isEmpty()) {
                        for (winAny in windows) {
                            val win = winAny as? UIWindow
                            if (win != null) {
                                rootController = win.rootViewController
                                if (rootController != null) break
                            }
                        }
                    }
                }

                if (rootController == null) {
                    println("Unable to find root view controller to present alert")
                    return@dispatch_async
                }

                // 找到最上层呈现的控制器
                var topController: UIViewController? = rootController
                while (topController?.presentedViewController != null) {
                    topController = topController.presentedViewController
                }

                val alert = UIAlertController.alertControllerWithTitle(
                    title = title,
                    message = message,
                    preferredStyle = UIAlertControllerStyleAlert
                )
                val okAction = UIAlertAction.actionWithTitle(
                    title = "确定",
                    style = UIAlertActionStyleDefault,
                    handler = null
                )
                alert.addAction(okAction)

                topController?.presentViewController(alert, animated = true, completion = null)
            } catch (t: Throwable) {
                println("Failed to show iOS reminder: ${t.message}")
            }
        }
    }
}

/**
 * Compose 端的记忆化函数，返回平台对应的 ReminderHandler 实例
 */
@Composable
actual fun rememberReminderHandler(): ReminderHandler {
    val handler = remember { IOSReminderHandler() }
    // 如果需要在启动时请求通知权限，可以在这里使用 LaunchedEffect 调用相关 API
    LaunchedEffect(Unit) {
        // no-op for now; keep for parity with other platforms
    }
    return handler
}
