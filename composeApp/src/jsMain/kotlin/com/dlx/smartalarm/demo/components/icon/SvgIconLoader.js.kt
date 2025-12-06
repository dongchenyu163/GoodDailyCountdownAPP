package com.dlx.smartalarm.demo.components.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import demo.composeapp.generated.resources.FilterIcon
import demo.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun rememberSvgIcon(name: String): Painter {
    return when (name) {
        "FilterIcon" -> painterResource(Res.drawable.FilterIcon)
        else -> painterResource(Res.drawable.FilterIcon)
    }
}
