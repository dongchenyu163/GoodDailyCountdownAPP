package com.dlx.smartalarm.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.Toolkit
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

private class DesktopReminderHandler : ReminderHandler {
    override fun showReminder(card: CardData, message: String) {
        Toolkit.getDefaultToolkit().beep()
        val title = card.title.ifBlank { "倒计时提醒" }
        SwingUtilities.invokeLater {
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE)
        }
    }
}

@Composable
actual fun rememberReminderHandler(): ReminderHandler = remember { DesktopReminderHandler() }
