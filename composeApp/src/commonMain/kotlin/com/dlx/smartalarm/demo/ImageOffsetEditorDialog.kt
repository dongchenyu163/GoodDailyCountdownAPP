package com.dlx.smartalarm.demo

import com.dlx.smartalarm.demo.MR
import dev.icerock.moko.resources.compose.stringResource
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
import androidx.compose.ui.window.DialogProperties
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private val ListPreviewMaxWidth = 500.dp

@Composable
fun ImageOffsetEditorDialog(
    titleImageInfo: TitleImageInfo,
    onDismiss: () -> Unit,
    onApply: (TitleImageInfo) -> Unit
) {
    // Dialog state controls
    var workingInfo by remember(titleImageInfo) { mutableStateOf(titleImageInfo) }
    var selectedView by remember { mutableStateOf(TitleImageViewType.Card) }
    val imageState = produceState(initialValue = null as androidx.compose.ui.graphics.ImageBitmap?, key1 = titleImageInfo.uuid) {
        value = loadImageBitmap(titleImageInfo)
    }
    val imageBitmap = imageState.value

    // Root dialog container
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val cardModifier = when (selectedView) {
            TitleImageViewType.Card -> Modifier.fillMaxWidth(0.7f)
            else -> Modifier.widthIn(max = 600.dp)
        }
        // Dialog card shell
        Card(
            modifier = cardModifier,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            // Main dialog column
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Instruction text
                Text(
                    text = stringResource(MR.strings.drag_image_to_adjust),
                    style = MaterialTheme.typography.titleMedium
                )

                // Secondary instruction text
                Text(
                    text = stringResource(MR.strings.adjust_view_selection),
                    style = MaterialTheme.typography.labelLarge
                )
                // Tab selector background container
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    // View type tab row
                    TabRow(
                        selectedTabIndex = selectedView.ordinal,
                        containerColor = Color.Transparent,
                        divider = {},
                        indicator = {}
                    ) {
                        TitleImageViewType.all.forEach { view ->
                            val selected = selectedView == view
                            // Individual view-type tab
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
                                    val textRes = when (view) {
                                        TitleImageViewType.List -> MR.strings.view_list
                                        TitleImageViewType.Grid -> MR.strings.view_grid
                                        TitleImageViewType.Card -> MR.strings.view_card
                                    }
                                    // Tab label text
                                    Text(
                                        stringResource(textRes),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                // Preview canvas container
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageBitmap == null) {
                        // Loading indicator
                        CircularProgressIndicator()
                    } else {
                        // Preview composable
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

                // Bottom action row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Cancel button
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(MR.strings.cancel))
                    }
                    // Reset/apply button group
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Reset button
                        TextButton(onClick = {
                            val defaults = defaultDisplayInfo(
                                imageBitmap?.let { bmp ->
                                    if (bmp.height != 0) bmp.width.toFloat() / bmp.height else 0f
                                } ?: 0f
                            )
                            workingInfo = workingInfo.copy(displayInfo = defaults)
                        }) {
                            Text(stringResource(MR.strings.reset))
                        }
                        // Apply button
                        Button(onClick = { onApply(workingInfo) }) {
                            Text(stringResource(MR.strings.apply))
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

    // Base preview modifier (handles gestures)
    val baseModifier = Modifier
        .clip(RoundedCornerShape(18.dp))
        .background(Color.Black.copy(alpha = 0.85f))
        .pointerInput(selectedView, previewSize) {
            detectTransformGestures { centroid, pan, zoom, rotation ->
                val (_, controlAnchor) = when (selectedView) {
                    TitleImageViewType.List -> ViewAnchors.ListImageAnchor to ViewAnchors.ListControlAnchor
                    TitleImageViewType.Grid -> ViewAnchors.GridImageAnchor to ViewAnchors.GridControlAnchor
                    TitleImageViewType.Card -> ViewAnchors.CardImageAnchor to ViewAnchors.CardControlAnchor
                }
                val canvasAnchor = Offset(
                    previewSize.width * controlAnchor.x,
                    previewSize.height * controlAnchor.y
                )

                val oldParams = currentParameters
                val currentOffset = Offset(oldParams.offsetX, oldParams.offsetY)

                val newScale = (oldParams.scale * zoom).coerceIn(0.2f, 6f)
                val actualZoom = if (oldParams.scale > 0) newScale / oldParams.scale else 1f

                val newOffset = calculateNewOffset(
                    cursorPos = centroid,
                    canvasAnchor = canvasAnchor,
                    currentOffset = currentOffset,
                    zoomChange = actualZoom,
                    rotationChange = rotation
                ) + pan

                val next = oldParams.copy(
                    offsetX = newOffset.x,
                    offsetY = newOffset.y,
                    scale = newScale,
                    rotation = normalizeAngle(oldParams.rotation + rotation)
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
                        change.consume()

                        val delta = change.scrollDelta.y
                        val cursorPos = change.position
                        val (_, controlAnchor) = when (selectedView) {
                            TitleImageViewType.List -> ViewAnchors.ListImageAnchor to ViewAnchors.ListControlAnchor
                            TitleImageViewType.Grid -> ViewAnchors.GridImageAnchor to ViewAnchors.GridControlAnchor
                            TitleImageViewType.Card -> ViewAnchors.CardImageAnchor to ViewAnchors.CardControlAnchor
                        }
                        val canvasAnchor = Offset(
                            previewSize.width * controlAnchor.x,
                            previewSize.height * controlAnchor.y
                        )

                        val oldParams = currentParameters
                        val isCtrl = event.keyboardModifiers.isCtrlPressed

                        val nextParams = if (isCtrl) {
                            val rotDelta = delta * -5f
                            val newOffset = calculateNewOffset(
                                cursorPos = cursorPos,
                                canvasAnchor = canvasAnchor,
                                currentOffset = Offset(oldParams.offsetX, oldParams.offsetY),
                                zoomChange = 1f,
                                rotationChange = rotDelta
                            )
                            oldParams.copy(
                                rotation = normalizeAngle(oldParams.rotation + rotDelta),
                                offsetX = newOffset.x,
                                offsetY = newOffset.y
                            )
                        } else {
                            val zoomFactor = 1f - (delta * 0.1f)
                            val newScale = (oldParams.scale * zoomFactor).coerceIn(0.2f, 6f)
                            val actualZoom = if (oldParams.scale > 0) newScale / oldParams.scale else 1f

                            val newOffset = calculateNewOffset(
                                cursorPos = cursorPos,
                                canvasAnchor = canvasAnchor,
                                currentOffset = Offset(oldParams.offsetX, oldParams.offsetY),
                                zoomChange = actualZoom,
                                rotationChange = 0f
                            )
                            oldParams.copy(
                                scale = newScale,
                                offsetX = newOffset.x,
                                offsetY = newOffset.y
                            )
                        }
                        onParametersChanged(nextParams)
                    }
                }
            }
        }

    val sizedModifier = when (selectedView) {
        TitleImageViewType.List -> {
            // List preview frame
            baseModifier
                .fillMaxWidth()
                .height(96.dp)
                .widthIn(max = ListPreviewMaxWidth)
        }
        TitleImageViewType.Grid -> {
            // Grid preview frame
            baseModifier
                .fillMaxWidth()
                .aspectRatio(GridPreviewAspectRatio)
        }
        TitleImageViewType.Card -> {
            // Card preview frame
            val ratio = if (parameters.aspectRatio > 0f) parameters.aspectRatio else defaultCardRatio
            val coercedRatio = ratio.coerceIn(CardPreviewMinAspectRatio, CardPreviewMaxAspectRatio)
            baseModifier
                .fillMaxWidth()
                .aspectRatio(coercedRatio)
        }
    }

    // Preview wrapper
    Box(
        modifier = sizedModifier.onGloballyPositioned { layoutCoordinates ->
            previewSize = layoutCoordinates.size
        },
        contentAlignment = Alignment.Center
    ) {
        // Preview canvas drawing surface
        Canvas(modifier = Modifier.fillMaxSize()) {
            val (imageAnchor, controlAnchor) = when (selectedView) {
                TitleImageViewType.List -> ViewAnchors.ListImageAnchor to ViewAnchors.ListControlAnchor
                TitleImageViewType.Grid -> ViewAnchors.GridImageAnchor to ViewAnchors.GridControlAnchor
                TitleImageViewType.Card -> ViewAnchors.CardImageAnchor to ViewAnchors.CardControlAnchor
            }
            drawTitleImage(imageBitmap, parameters, imageAnchor, controlAnchor)
        }

        if (selectedView == TitleImageViewType.Card) {
            // Aspect ratio handles overlay
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

    // Container for two ratio handles
    Box(modifier = Modifier.fillMaxSize()) {
        // Top handle
        RatioHandle(
            modifier = handleModifier.align(Alignment.TopCenter),
            previewSize = previewSize,
            currentRatio = currentRatio,
            onRatioChange = { ratio ->
                onParametersChange(parameters.copy(aspectRatio = ratio))
            }
        )
        // Bottom handle
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
    // Handle touch target
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

/**
 * Normalizes a given angle to ensure it falls within the range [0, 360).
 *
 * @param value The angle in degrees that needs to be normalized.
 * @return The normalized angle within the range [0, 360).
 */
private fun normalizeAngle(value: Float): Float {
    var angle = value
    while (angle < 0f) angle += 360f
    while (angle >= 360f) angle -= 360f
    return angle
}

private fun calculateNewOffset(
    cursorPos: Offset,
    canvasAnchor: Offset,
    currentOffset: Offset,
    zoomChange: Float,
    rotationChange: Float
): Offset {
    val oldVector = cursorPos - (canvasAnchor + currentOffset)
    val radians = (rotationChange * PI / 180.0).toFloat()
    val cos = cos(radians)
    val sin = sin(radians)

    val scaledX = oldVector.x * zoomChange
    val scaledY = oldVector.y * zoomChange

    val rotatedX = scaledX * cos - scaledY * sin
    val rotatedY = scaledX * sin + scaledY * cos

    return cursorPos - canvasAnchor - Offset(rotatedX, rotatedY)
}
