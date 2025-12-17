package com.dlx.smartalarm.demo.components.menu

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

private val MenuCornerShape = RoundedCornerShape(12.dp)

object AppMenuDefaults {
    val shape get() = MenuCornerShape

    @Composable
    fun containerColor(): Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
}

@Composable
fun AppDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset.Zero,
    content: @Composable ColumnScope.() -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        offset = offset,
        shape = AppMenuDefaults.shape,
        containerColor = AppMenuDefaults.containerColor(),
        tonalElevation = 0.dp,
        shadowElevation = 6.dp,
        content = content
    )
}

@Composable
fun AppContextMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    offset: DpOffset,
    content: @Composable ColumnScope.() -> Unit
) {
    AppDropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = offset,
        content = content
    )
}
