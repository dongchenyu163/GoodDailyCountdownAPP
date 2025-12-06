package com.dlx.smartalarm.demo.components.icon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil3.ImageLoader
import coil3.compose.rememberAsyncImagePainter
import coil3.svg.SvgDecoder
import demo.composeapp.generated.resources.Res
import kotlinx.coroutines.runBlocking

@Composable
actual fun rememberSvgIcon(name: String): Painter {
    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }

    val bytes = remember(name) {
        runCatching {
            runBlocking { Res.readBytes("drawable/$name.svg") }
        }.getOrNull()
    }

    return rememberAsyncImagePainter(
        model = bytes ?: android.R.drawable.ic_menu_report_image,
        imageLoader = imageLoader,
        fallback = painterResource(android.R.drawable.ic_menu_report_image),
        error = painterResource(android.R.drawable.ic_menu_report_image)
    )
}
