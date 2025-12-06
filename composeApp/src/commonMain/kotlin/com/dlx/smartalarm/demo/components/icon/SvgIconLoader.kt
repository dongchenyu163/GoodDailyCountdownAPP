package com.dlx.smartalarm.demo.components.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

/**
 * Loads an SVG icon by name (without extension) for use in controls like buttons.
 */
@Composable
expect fun rememberSvgIcon(name: String): Painter
