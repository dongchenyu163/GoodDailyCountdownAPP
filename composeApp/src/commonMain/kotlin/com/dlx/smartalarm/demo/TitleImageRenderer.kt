package com.dlx.smartalarm.demo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.max

@Composable
fun TitleImageBackground(
    titleImage: TitleImageInfo?,
    viewType: TitleImageViewType,
    modifier: Modifier = Modifier,
    gradientSpec: TitleImageGradientSpec? = null,
    gradientOrientation: GradientOrientation = GradientOrientation.Horizontal,
    overlayColor: Color = MaterialTheme.colorScheme.surface,
    bitmapOverride: ImageBitmap? = null,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val imageState: State<ImageBitmap?> = if (bitmapOverride == null) {
        rememberTitleImageBitmap(titleImage)
    } else {
        androidx.compose.runtime.rememberUpdatedState(bitmapOverride)
    }
    val bitmap = imageState.value
    val params = titleImage?.paramsFor(viewType) ?: TitleImageDisplayParameters()

    Box(modifier = modifier) {
        if (bitmap != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawTitleImage(bitmap, params)
            }
            if (gradientSpec != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(gradientSpec.asBrush(gradientOrientation, overlayColor))
                )
            }
        }

        content()
    }
}

@Composable
fun rememberTitleImageBitmap(info: TitleImageInfo?): State<ImageBitmap?> {
    val uuid = info?.uuid
    return produceState(initialValue = uuid?.let { TitleImageBitmapCache.get(it) }, key1 = uuid) {
        if (uuid == null) {
            value = null
            return@produceState
        }
        val cached = TitleImageBitmapCache.get(uuid)
        if (cached != null) {
            value = cached
            return@produceState
        }
        val loaded = loadImageBitmap(info)
        value = loaded
    }
}

fun DrawScope.drawTitleImage(
    bitmap: ImageBitmap,
    params: TitleImageDisplayParameters
) {
    val canvasWidth = size.width
    val canvasHeight = size.height
    val imageWidth = bitmap.width.toFloat()
    val imageHeight = bitmap.height.toFloat()
    val baseScale = if (imageWidth > 0 && imageHeight > 0) {
        max(canvasWidth / imageWidth, canvasHeight / imageHeight)
    } else 1f
    val translationX = params.offsetX * canvasWidth
    val translationY = params.offsetY * canvasHeight
    val resolvedScale = baseScale * params.scale

    withTransform({
        translate(left = canvasWidth / 2f, top = canvasHeight / 2f)
        translate(left = translationX, top = translationY)
        rotate(params.rotation)
        scale(resolvedScale, resolvedScale)
        translate(left = -imageWidth / 2f, top = -imageHeight / 2f)
    }) {
        drawImage(bitmap)
    }
}
