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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun GradientRoundedBox() {
	val shape = RoundedCornerShape(16.dp)
	Box(
		modifier = Modifier
			.size(width = 220.dp, height = 120.dp)
			.background(
				brush = Brush.horizontalGradient(
					colors = listOf(Color(0xFF6A11CB), Color(0xFF2575FC))
				),
				shape = shape
			)
			.clip(shape), // 可选: 裁剪子内容也跟随圆角
		contentAlignment = Alignment.Center
	) {
		Text("Rounded + Gradient", color = Color.White)
	}
}

@Composable
fun GradientRoundedBoxCanvas(isLarge: Boolean) {
    val boxSize = if (isLarge) Pair(320.dp, 180.dp) else Pair(220.dp, 120.dp)
    Box(
        modifier = Modifier
            .size(boxSize.first, boxSize.second)
            .drawWithCache {
                val corner = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                val brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF00C6FF), Color(0xFF0072FF))
                )
                onDrawBehind {
                    drawRoundRect(brush = brush, cornerRadius = corner)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text("Canvas Gradient", color = Color.White)
    }
}

@Composable
fun CountdownCard(
    title: String,
    date: String,
    remainingDays: Int,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {},
    isDeleting: Boolean = false
) {
    var showMenu by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    // 启动入场动画
    LaunchedEffect(Unit) {
        isVisible = true
    }

    // 入场缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isVisible && !isDeleting) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
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
        Box(
            modifier = Modifier
                .scale(scale)
                .offset(x = offsetX.dp)
                .alpha(alpha)
                .size(width = 240.dp, height = 140.dp)
                .drawWithCache {
                    val corner = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                    val brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
                    )
                    onDrawBehind {
                        drawRoundRect(brush = brush, cornerRadius = corner)
                    }
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Press -> {
                                    val isRightClick = event.buttons.isSecondaryPressed
                                    if (isRightClick) {
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
                            showMenu = true
                        }
                    )
                }
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top
            ) {
                // 标题
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 日期和剩余天数
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = date,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )

                    Text(
                        text = "${remainingDays}天",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 右键菜单
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
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
        }
    }
}

@Composable
fun AnimatedCountdownCard(
    title: String,
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
        date = date,
        remainingDays = remainingDays,
        onClick = onClick,
        onDelete = { isDeleting = true }, // 触发删除动画
        onEdit = onEdit,
        isDeleting = isDeleting
    )
}
