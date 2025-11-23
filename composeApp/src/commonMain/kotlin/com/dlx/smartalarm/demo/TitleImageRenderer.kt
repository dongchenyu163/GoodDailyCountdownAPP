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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Matrix
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
            Canvas(modifier = Modifier.matchParentSize()) {
                drawTitleImage(bitmap, params, imageAnchor, controlAnchor, viewType.name)
            }
            if (gradientSpec != null) {
                Box(
                    modifier = Modifier
						.matchParentSize()
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
    controlAnchor: Anchor,
	tag: String = "Unknown" // <--- 新增参数
) {
    val canvasWidth = size.width
    val canvasHeight = size.height
//    println("--- TitleImageRenderer DEBUG ---")
//	// 只有高度为0时才打印，避免日志刷屏
//	if (canvasHeight <= 0f) {
//		println("⚠️ [Drawing Error] Tag: $tag | Size: ${canvasWidth}w x ${canvasHeight}h")
//	} else {
//		// 正常情况也可以打印一下看对比
//		 println("✅ [Drawing OK] Tag: $tag | Size: ${canvasWidth}w x ${canvasHeight}h")
//	}
    val imageWidth = bitmap.width.toFloat()
    val imageHeight = bitmap.height.toFloat()

	val baseScale = 1.0f
    val resolvedScale: Float = baseScale * params.scale

    // User-defined offset in canvas pixels
    val translationX = params.offsetX
    val translationY = params.offsetY

    // The pivot point on the image, in original image pixels
    val imagePivotX = imageWidth * imageAnchor.x
    val imagePivotY = imageHeight * imageAnchor.y

    // The target point on the canvas, in canvas pixels
    val canvasPivotX = canvasWidth * controlAnchor.x
    val canvasPivotY = canvasHeight * controlAnchor.y

//    println("Image anchor point in pixels: x=${imagePivotX}, y=${imagePivotY}")
//    println("Display control anchor point in pixels: x=${canvasPivotX}, y=${canvasPivotY}")
//    println("Bitmap original size: width=${imageWidth}, height=${imageHeight}")
//    println("Scaling: baseScale=${baseScale}, resolvedScale=${resolvedScale}")
//    println("Translation: x=${translationX}, y=${translationY}")
//    println("--- End of DEBUG ---")

    withTransform({
		// 1. Move to the canvas anchor point
        translate(left = canvasPivotX, top = canvasPivotY)
		// 2. Apply user offset
        translate(left = translationX, top = translationY)
        // 3. Rotate and scale, the calculation is based on the top-left side pivot. But the default pivot is the center.
        rotate(params.rotation, pivot = Offset(0f, 0f))
        scale(resolvedScale, resolvedScale, pivot = Offset(0f, 0f))

        // 4. Move image anchor to origin
        translate(left = -imagePivotX, top = -imagePivotY)
    }) {
        drawImage(bitmap)
    }
}
