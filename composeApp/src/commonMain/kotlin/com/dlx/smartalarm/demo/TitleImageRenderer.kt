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
        rememberUpdatedState(bitmapOverride)
    }
    val bitmap = imageState.value
    val params = titleImage?.paramsFor(viewType) ?: TitleImageDisplayParameters()
    val (imageAnchor, controlAnchor) = when (viewType) {
        TitleImageViewType.List -> ViewAnchors.ListImageAnchor to ViewAnchors.ListControlAnchor
        TitleImageViewType.Grid -> ViewAnchors.GridImageAnchor to ViewAnchors.GridControlAnchor
        TitleImageViewType.Card -> ViewAnchors.CardImageAnchor to ViewAnchors.CardControlAnchor
    }

    Box(modifier = modifier) {
        if (bitmap != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawTitleImage(bitmap, params, imageAnchor, controlAnchor)
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
    params: TitleImageDisplayParameters,
    imageAnchor: Anchor,
    controlAnchor: Anchor
) {
    val canvasWidth = size.width
    val canvasHeight = size.height
    val imageWidth = bitmap.width.toFloat()
    val imageHeight = bitmap.height.toFloat()

    val baseScale = if (imageWidth > 0 && imageHeight > 0) {
        max(canvasWidth / imageWidth, canvasHeight / imageHeight)
    } else 1f
    val resolvedScale = baseScale * params.scale

    // User-defined offset in canvas pixels
    val translationX = params.offsetX * canvasWidth
    val translationY = params.offsetY * canvasHeight

    // The pivot point on the image, in original image pixels
    val imagePivotX = imageWidth * imageAnchor.x
    val imagePivotY = imageHeight * imageAnchor.y

    // The target point on the canvas, in canvas pixels
    val canvasPivotX = canvasWidth * controlAnchor.x
    val canvasPivotY = canvasHeight * controlAnchor.y

    println("--- DEBUG IMAGE DRAW ---")
    println("Canvas Size: ${canvasWidth}w x ${canvasHeight}h")
    println("Image Anchor (px): $imagePivotX, $imagePivotY")
    println("Control Anchor (px): $canvasPivotX, $canvasPivotY")
    println("------------------------")

    withTransform({
        // 4. Move to the canvas anchor point
        translate(left = canvasPivotX, top = canvasPivotY)
        // 3. Apply user offset
        translate(left = translationX, top = translationY)
        // 2. Rotate and scale
        rotate(params.rotation)
        scale(resolvedScale, resolvedScale)
        // 1. Move image anchor to origin
        translate(left = -imagePivotX, top = -imagePivotY)
    }) {
        drawImage(bitmap)
    }
}
