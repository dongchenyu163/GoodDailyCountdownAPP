package com.dlx.smartalarm.demo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// 已移除未使用的演示组件：GradientRoundedBox 与 GradientRoundedBoxCanvas

@Composable
fun CountdownCard(
    title: String,
    annotatedTitle: AnnotatedString? = null,
    date: String,
    remainingDays: Int,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {},
    isDeleting: Boolean = false
) {
    var showMenu by remember { mutableStateOf(false) }
        var menuPosition by remember { mutableStateOf(0.dp to 0.dp) }
        var isVisible by remember { mutableStateOf(false) }

        // 启动入场动画
        LaunchedEffect(Unit) {
            isVisible = true
        }

        // 入场缩放动画
        val scale by animateFloatAsState(
            targetValue = if (isVisible && !isDeleting) 1f else 0.97f,
            animationSpec = tween(
                durationMillis = 220,
                easing = FastOutSlowInEasing
            ),
            label = "scale"
        )

        // 删除动画 - 水平偏移
        val offsetX by animateFloatAsState(
            targetValue = if (isDeleting) 300f else 0f,
            animationSpec = tween(
                durationMillis = 400,
                easing = FastOutSlowInEasing
            ),
            label = "offsetX"
        )

        // 删除动画 - 透明度
        val alpha by animateFloatAsState(
            targetValue = if (isDeleting) 0f else 1f,
            animationSpec = tween(
                durationMillis = 400,
                easing = LinearEasing
            ),
            label = "alpha"
        )

        AnimatedVisibility(
            visible = !isDeleting,
            exit = fadeOut(
                animationSpec = tween(400)
            ) + slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(400)
            ) + shrinkVertically(
                animationSpec = tween(400)
            )
        ) {
            // 参考图片中的卡片背景色: dark:bg-zinc-800/50
            // 在Compose中使用相似的颜色 #303030 (zinc-800) 并设置50%透明度
            val cardBackgroundColor = Color(0xFF303030).copy(alpha = 0.5f)
            // 图标背景色: bg-primary/20 (主色的20%透明度)
            val iconBackgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            
            // 用于获取全局位置的LayoutCoordinates
            val coordinates = remember { mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(null) }

            Box(
                modifier = Modifier
                    .scale(scale)
                    .offset(x = offsetX.dp)
                    .alpha(alpha)
                    // Card 视图宽度改为相对窗口的百分比（约 92%），高度保持卡片风格
                    .fillMaxWidth(0.92f)
                    .height(140.dp)
                    .background(cardBackgroundColor, RoundedCornerShape(18.dp))
                    .onGloballyPositioned { layoutCoordinates ->
                        coordinates.value = layoutCoordinates
                    }
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                when (event.type) {
                                    PointerEventType.Press -> {
                                        val isRightClick = event.buttons.isSecondaryPressed
                                        if (isRightClick) {
                                            // 获取点击位置
                                            val position = event.changes.first().position
                                            // 将局部坐标转换为全局坐标
                                            coordinates.value?.let { coords ->
                                                val globalPosition = coords.localToWindow(position)
                                                menuPosition = globalPosition.x.toDp() to globalPosition.y.toDp()
                                            }
                                            showMenu = true
                                        }
                                    }
                                    PointerEventType.Release -> {
                                        val isLeftClick = !event.buttons.isSecondaryPressed
                                        if (isLeftClick && !showMenu) {
                                            onClick()
                                        }
                                    }
                                }
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                // 长按也显示菜单，但需要获取位置
                                showMenu = true
                            }
                        )
                    }
                    .padding(16.dp)
            ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 图标区域 - 使用主色的20%透明度作为背景
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(50)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎯", // 默认图标，实际使用时应根据需要更换
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 文字内容区域
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // 标题
                    Text(
                        text = annotatedTitle ?: AnnotatedString(title),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 日期
                    Text(
                        text = date,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 剩余天数
                    Text(
                        text = "${remainingDays}d",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }

            // 右键菜单 - 显示在点击位置
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier
                    .offset(x = menuPosition.first, y = menuPosition.second)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                offset = DpOffset(0.dp, 0.dp) // 重置默认偏移
            ) {
                DropdownMenuItem(
                    text = { Text("编辑") },
                    onClick = {
                        showMenu = false
                        onEdit()
                    }
                )
                DropdownMenuItem(
                    text = { Text("删除") },
                    onClick = {
                        showMenu = false
                        onDelete()
                    }
                )
            }

            // 顶部右侧三个点按钮，作为菜单触发入口
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
                IconButton(onClick = { showMenu = true }) {
                    Text("⋮", color = Color.White.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
fun AnimatedCountdownCard(
    title: String,
    annotatedTitle: AnnotatedString? = null,
    date: String,
    remainingDays: Int,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {}
) {
    var isDeleting by remember { mutableStateOf(false) }

    // 当开始删除动画时，延迟调用真正的删除回调
    LaunchedEffect(isDeleting) {
        if (isDeleting) {
            delay(400) // 等待删除动画完成
            onDelete()
        }
    }

    CountdownCard(
        title = title,
        annotatedTitle = annotatedTitle,
        date = date,
        remainingDays = remainingDays,
        onClick = onClick,
        onDelete = { isDeleting = true }, // 触发删除动画
        onEdit = onEdit,
        isDeleting = isDeleting
    )
}
