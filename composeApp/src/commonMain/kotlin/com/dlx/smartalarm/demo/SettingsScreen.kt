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
    grid: Boolean,
    onBack: () -> Unit,
    onToggleCloud: (Boolean) -> Unit,
    onToggleGrid: (Boolean) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("账户与设置") },
                navigationIcon = { TextButton(onClick = onBack) { Text("←") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("账户", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("本地")
                Switch(checked = useCloud, onCheckedChange = { onToggleCloud(it) })
                Text("云端")
            }
            Divider()
            Text("显示方式", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("列表")
                Switch(checked = grid, onCheckedChange = { onToggleGrid(it) })
                Text("网格")
            }
            Text("说明：以上为占位设置，外观与参考图保持一致风格。")
        }
    }
}
