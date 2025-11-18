package com.dlx.smartalarm.demo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlin.math.PI
import kotlin.math.abs

private val ListPreviewMaxWidth = 500.dp

@Composable
fun ImageOffsetEditorDialog(
    titleImageInfo: TitleImageInfo,
    onDismiss: () -> Unit,
    onApply: (TitleImageInfo) -> Unit
) {
    var workingInfo by remember(titleImageInfo) { mutableStateOf(titleImageInfo) }
    var selectedView by remember { mutableStateOf(TitleImageViewType.Card) }
    val imageState = produceState(initialValue = null as androidx.compose.ui.graphics.ImageBitmap?, key1 = titleImageInfo.uuid) {
        value = loadImageBitmap(titleImageInfo)
    }
    val imageBitmap = imageState.value

    Dialog(onDismissRequest = onDismiss) {
        val cardModifier = when (selectedView) {
            TitleImageViewType.Card -> Modifier.fillMaxWidth(0.7f)
            else -> Modifier.widthIn(max = 600.dp)
        }
        Card(
            modifier = cardModifier,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "拖拽图片以调整显示效果",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "调整视图选择",
                    style = MaterialTheme.typography.labelLarge
                )
                // 使“编辑视图选择”的 Tab 更易区分：提供背景与前景的对比，高亮当前选中项
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    TabRow(
                        selectedTabIndex = selectedView.ordinal,
                        containerColor = Color.Transparent,
                        divider = {},
                        indicator = {}
                    ) {
                        TitleImageViewType.all.forEach { view ->
                            val selected = selectedView == view
                            Tab(
                                selected = selected,
                                onClick = { selectedView = view },
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primary
                                        else Color.Transparent
                                    ),
                                selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                text = {
                                    Text(
                                        view.key,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageBitmap == null) {
                        CircularProgressIndicator()
                    } else {
                        OffsetPreview(
                            imageBitmap = imageBitmap,
                            selectedView = selectedView,
                            parameters = workingInfo.paramsFor(selectedView),
                            onParametersChange = { updated ->
                                workingInfo = workingInfo.update(selectedView, updated)
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = {
                            val defaults = defaultDisplayInfo(
                                imageBitmap?.let { bmp ->
                                    if (bmp.height != 0) bmp.width.toFloat() / bmp.height else 0f
                                } ?: 0f
                            )
                            workingInfo = workingInfo.copy(displayInfo = defaults)
                        }) {
                            Text("重置")
                        }
                        Button(onClick = { onApply(workingInfo) }) {
                            Text("应用")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OffsetPreview(
    imageBitmap: androidx.compose.ui.graphics.ImageBitmap,
    selectedView: TitleImageViewType,
    parameters: TitleImageDisplayParameters,
    onParametersChange: (TitleImageDisplayParameters) -> Unit
) {
    var previewSize by remember { mutableStateOf(IntSize.Zero) }
    val defaultCardRatio = remember(imageBitmap) {
        if (imageBitmap.height != 0) imageBitmap.width.toFloat() / imageBitmap.height else 1.8f
    }

    val currentParameters by rememberUpdatedState(parameters)
    val onParametersChanged by rememberUpdatedState(onParametersChange)

    val baseModifier = Modifier
        .clip(RoundedCornerShape(18.dp))
        .background(Color.Black.copy(alpha = 0.85f))
        .pointerInput(selectedView, previewSize) {
            detectTransformGestures { _, pan, zoom, rotation ->
                val width = previewSize.width.takeIf { it > 0 } ?: return@detectTransformGestures
                val height = previewSize.height.takeIf { it > 0 } ?: return@detectTransformGestures
                val next = currentParameters.copy(
                    offsetX = currentParameters.offsetX + (pan.x / width),
                    offsetY = currentParameters.offsetY + (pan.y / height),
                    scale = (currentParameters.scale * zoom).coerceIn(0.2f, 6f),
                    rotation = normalizeAngle(currentParameters.rotation + rotation.toDegrees())
                )
                onParametersChanged(next)
            }
        }
        .pointerInput(selectedView, previewSize) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    if (event.type == PointerEventType.Scroll) {
                        val change = event.changes.firstOrNull() ?: continue
                        val delta = change.scrollDelta.y
                        val updated = if (event.keyboardModifiers.isShiftPressed) {
                            currentParameters.copy(rotation = normalizeAngle(currentParameters.rotation + delta * -5f))
                        } else {
                            currentParameters.copy(scale = (currentParameters.scale + delta * -0.01f).coerceIn(0.2f, 6f))
                        }
                        onParametersChanged(updated)
                        change.consume()
                    }
                }
            }
        }

    val sizedModifier = when (selectedView) {
        TitleImageViewType.List -> {
            baseModifier
                .fillMaxWidth()
                .height(96.dp)
                .widthIn(max = ListPreviewMaxWidth)
        }
        TitleImageViewType.Grid -> {
            baseModifier
                .fillMaxWidth()
                .aspectRatio(GridPreviewAspectRatio)
        }
        TitleImageViewType.Card -> {
            val ratio = if (parameters.aspectRatio > 0f) parameters.aspectRatio else defaultCardRatio
            val coercedRatio = ratio.coerceIn(CardPreviewMinAspectRatio, CardPreviewMaxAspectRatio)
            baseModifier
                .fillMaxWidth()
                .aspectRatio(coercedRatio)
        }
    }

    Box(
        modifier = sizedModifier.onGloballyPositioned { layoutCoordinates ->
            previewSize = layoutCoordinates.size
        },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val (imageAnchor, controlAnchor) = when (selectedView) {
                TitleImageViewType.List -> ViewAnchors.ListImageAnchor to ViewAnchors.ListControlAnchor
                TitleImageViewType.Grid -> ViewAnchors.GridImageAnchor to ViewAnchors.GridControlAnchor
                TitleImageViewType.Card -> ViewAnchors.CardImageAnchor to ViewAnchors.CardControlAnchor
            }
            drawTitleImage(imageBitmap, parameters, imageAnchor, controlAnchor)
        }

        if (selectedView == TitleImageViewType.Card) {
            AspectRatioHandles(
                previewSize = previewSize,
                parameters = parameters,
                onParametersChange = onParametersChange,
                defaultRatio = defaultCardRatio
            )
        }
    }
}

@Composable
private fun AspectRatioHandles(
    previewSize: IntSize,
    parameters: TitleImageDisplayParameters,
    onParametersChange: (TitleImageDisplayParameters) -> Unit,
    defaultRatio: Float
) {
    val currentRatio = if (parameters.aspectRatio > 0f) parameters.aspectRatio else defaultRatio
    val handleModifier = Modifier
        .size(16.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primary)

    Box(modifier = Modifier.fillMaxSize()) {
        RatioHandle(
            modifier = handleModifier.align(Alignment.TopCenter),
            previewSize = previewSize,
            currentRatio = currentRatio,
            onRatioChange = { ratio ->
                onParametersChange(parameters.copy(aspectRatio = ratio))
            }
        )
        RatioHandle(
            modifier = handleModifier.align(Alignment.BottomCenter),
            previewSize = previewSize,
            currentRatio = currentRatio,
            onRatioChange = { ratio ->
                onParametersChange(parameters.copy(aspectRatio = ratio))
            }
        )
    }
}

@Composable
private fun RatioHandle(
    modifier: Modifier,
    previewSize: IntSize,
    currentRatio: Float,
    onRatioChange: (Float) -> Unit
) {
    Box(
        modifier = modifier.pointerInput(previewSize, currentRatio) {
            detectDragGestures { change, drag ->
                change.consume()
                val width = previewSize.width.toFloat().coerceAtLeast(1f)
                val height = previewSize.height.toFloat().coerceAtLeast(1f)
                val newHeight = (height + drag.y).coerceIn(
                    width / CardPreviewMaxAspectRatio,
                    width / CardPreviewMinAspectRatio
                )
                if (!newHeight.isFinite()) return@detectDragGestures
                val nextRatio = (width / newHeight).coerceIn(CardPreviewMinAspectRatio, CardPreviewMaxAspectRatio)
                if (abs(nextRatio - currentRatio) > 0.0001f) {
                    onRatioChange(nextRatio)
                }
            }
        }
    )
}

private fun Float.toDegrees(): Float = (this * (180f / PI.toFloat()))

private fun normalizeAngle(value: Float): Float {
    var angle = value
    while (angle < 0f) angle += 360f
    while (angle >= 360f) angle -= 360f
    return angle
}
