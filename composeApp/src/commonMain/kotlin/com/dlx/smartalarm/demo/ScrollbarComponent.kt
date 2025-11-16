package com.dlx.smartalarm.demo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.onGloballyPositioned

/**
 * 仅用于显示滚动位置的滚动条，无法拖动
 */
@Composable
fun VerticalScrollbar(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    visible: Boolean = true
) {
    val scrollbarVisible = visible && listState.layoutInfo.totalItemsCount > 0
    
    if (!scrollbarVisible || listState.layoutInfo.totalItemsCount == 0) return

    // 计算滚动位置 (0.0 到 1.0)
    val firstVisibleItemIndex = listState.firstVisibleItemIndex
    val firstVisibleItemScrollOffset = listState.firstVisibleItemScrollOffset
    val totalItems = listState.layoutInfo.totalItemsCount
    
    // 计算滚动百分比
    val scrollOffset by animateFloatAsState(
        targetValue = if (totalItems > 1) {
            val estimatedScrollY = firstVisibleItemIndex + (firstVisibleItemScrollOffset / 1000f)
            (estimatedScrollY / (totalItems - 1)).coerceIn(0f, 1f)
        } else 0f,
        label = "scrollbarOffset"
    )

    // 计算滚动条高度百分比
    val itemsInViewport = listState.layoutInfo.visibleItemsInfo.size.toFloat()
    val scrollbarHeight by animateFloatAsState(
        targetValue = if (totalItems > 0) {
            (itemsInViewport / totalItems).coerceIn(0.01f, 1f)
        } else 0.1f,
        label = "scrollbarHeight"
    )
    
    var parentHeight by remember { mutableStateOf(100f) }
    
    AnimatedVisibility(
        visible = scrollbarVisible,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .width(6.dp)
                .fillMaxHeight()
                .background(Color.Transparent)
                .padding(end = 4.dp) // 稍微内缩
                .onGloballyPositioned { coordinates ->
                    parentHeight = coordinates.size.height.toFloat()
                }
        ) {
            val scrollbarHeightDp = ((scrollbarHeight * parentHeight * 0.95f).dp).coerceAtLeast(5.dp)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(y = (scrollOffset * parentHeight * 0.95f).dp) // 95% 用作滚动范围
                    .width(6.dp)
                    .height(scrollbarHeightDp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}

@Composable
fun VerticalScrollbar(
    gridState: LazyGridState,
    modifier: Modifier = Modifier,
    visible: Boolean = true
) {
    val scrollbarVisible = visible && gridState.layoutInfo.totalItemsCount > 0
    
    if (!scrollbarVisible || gridState.layoutInfo.totalItemsCount == 0) return

    // 计算滚动位置 (0.0 到 1.0)
    val firstVisibleItemIndex = gridState.firstVisibleItemIndex
    val firstVisibleItemScrollOffset = gridState.firstVisibleItemScrollOffset
    val totalItems = gridState.layoutInfo.totalItemsCount
    
    // 计算滚动百分比
    val scrollOffset by animateFloatAsState(
        targetValue = if (totalItems > 1) {
            val estimatedScrollY = firstVisibleItemIndex + (firstVisibleItemScrollOffset / 1000f)
            (estimatedScrollY / (totalItems - 1)).coerceIn(0f, 1f)
        } else 0f,
        label = "scrollbarOffset"
    )

    // 计算滚动条高度百分比
    val itemsInViewport = gridState.layoutInfo.visibleItemsInfo.size.toFloat()
    val scrollbarHeight by animateFloatAsState(
        targetValue = if (totalItems > 0) {
            (itemsInViewport / totalItems).coerceIn(0.01f, 1f)
        } else 0.1f,
        label = "scrollbarHeight"
    )
    
    var parentHeight by remember { mutableStateOf(100f) }
    
    AnimatedVisibility(
        visible = scrollbarVisible,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .width(6.dp)
                .fillMaxHeight()
                .background(Color.Transparent)
                .padding(end = 4.dp) // 稍微内缩
                .onGloballyPositioned { coordinates ->
                    parentHeight = coordinates.size.height.toFloat()
                }
        ) {
            val scrollbarHeightDp = ((scrollbarHeight * parentHeight * 0.95f).dp).coerceAtLeast(5.dp)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(y = (scrollOffset * parentHeight * 0.95f).dp) // 95% 用作滚动范围
                    .width(6.dp)
                    .height(scrollbarHeightDp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}