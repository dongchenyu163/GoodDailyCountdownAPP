package com.dlx.smartalarm.demo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    useCloud: Boolean,
    displayStyle: DisplayStyle,
    onBack: () -> Unit,
    onToggleCloud: (Boolean) -> Unit,
    onChangeDisplay: (DisplayStyle) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") }, // Changed title
                navigationIcon = { TextButton(onClick = onBack) { Text("←") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Temporarily disabled account settings
            /*
            Text("账户", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("本地")
                Switch(checked = useCloud, onCheckedChange = { onToggleCloud(it) })
                Text("云端")
            }
            Divider()
            */
            Text("Countdown Display Style", style = MaterialTheme.typography.titleMedium)
            // 三种显示样式单选
            DisplayStyleItem(
                title = "Grid View",
                subtitle = "See more countdowns at a glance.",
                selected = displayStyle == DisplayStyle.Grid,
                onClick = { onChangeDisplay(DisplayStyle.Grid) }
            )
            DisplayStyleItem(
                title = "List View",
                subtitle = "A compact view for many items.",
                selected = displayStyle == DisplayStyle.List,
                onClick = { onChangeDisplay(DisplayStyle.List) }
            )
            DisplayStyleItem(
                title = "Card View",
                subtitle = "A detailed, full-width card for each item.",
                selected = displayStyle == DisplayStyle.Card,
                onClick = { onChangeDisplay(DisplayStyle.Card) }
            )
        }
    }
}

@Composable
private fun DisplayStyleItem(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        tonalElevation = if (selected) 4.dp else 0.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .let { if (selected) it else it },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            RadioButton(selected = selected, onClick = onClick)
        }
    }
    Spacer(Modifier.height(8.dp))
}
