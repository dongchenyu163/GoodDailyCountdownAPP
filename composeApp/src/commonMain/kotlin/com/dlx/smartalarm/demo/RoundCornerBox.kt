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
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.dlx.smartalarm.demo.components.favorite.FavoriteButton
import dev.icerock.moko.resources.compose.stringResource
import com.dlx.smartalarm.demo.MR

// 已移除未使用的演示组件：GradientRoundedBox 与 GradientRoundedBoxCanvas

@Composable
fun CountdownCard(
    title: String,
    annotatedTitle: AnnotatedString? = null,
    date: String,
    remainingDays: Long,
    icon: String,
    titleImage: TitleImageInfo? = null,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {},
    onShowMenu: (position: DpOffset) -> Unit,
    isDeleting: Boolean = false,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {}
) {
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
        val cardBackgroundColor = Color(0xFF0F2E1F)
        val iconBackgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        val cardParams = titleImage?.paramsFor(TitleImageViewType.Card)
        val headerAspectRatio = cardParams?.aspectRatio?.takeIf { it > 0f }?.coerceIn(
            CardPreviewMinAspectRatio,
            CardPreviewMaxAspectRatio
        ) ?: 1.8f
        
        var threeDotsButtonPosition by remember { mutableStateOf<DpOffset?>(null) }
        var itemPositionInWindow by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
        val density = LocalDensity.current

        val isWeb = remember { getPlatform().name.startsWith("Web") }
        val emojiFamily = if (isWeb) getAppEmojiFontFamily() else androidx.compose.ui.text.font.FontFamily.Default

        Box(
            modifier = Modifier
                .scale(scale)
                .offset(x = offsetX.dp)
                .alpha(alpha)
                .fillMaxWidth(0.92f)
                .background(cardBackgroundColor, RoundedCornerShape(18.dp))
                .onGloballyPositioned {
                    itemPositionInWindow = it.positionInWindow()
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                                val localPosition = event.changes.first().position
                                val globalPosition = itemPositionInWindow + localPosition
                                with(density) {
                                    onShowMenu(DpOffset(globalPosition.x.toDp(), globalPosition.y.toDp()))
                                }
                                event.changes.forEach { it.consume() }
                            }
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { localPosition ->
                            val globalPosition = itemPositionInWindow + localPosition
                            with(density) {
                                onShowMenu(DpOffset(globalPosition.x.toDp(), globalPosition.y.toDp()))
                            }
                        },
                        onTap = { onClick() }
                    )
                }
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (titleImage != null) {
                    TitleImageBackground(
                        titleImage = titleImage,
                        viewType = TitleImageViewType.Card,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                            .aspectRatio(headerAspectRatio),
                        gradientSpec = DefaultGridImageGradient,
                        gradientOrientation = GradientOrientation.Vertical,
                        overlayColor = cardBackgroundColor
                    ) {
                        // Gradient is now part of the background, so this is empty
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(iconBackgroundColor, shape = RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = icon.ifBlank { "🎯" },
                            fontSize = 24.sp,
                            fontFamily = emojiFamily
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = annotatedTitle ?: AnnotatedString(title),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = date,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(MR.strings.remaining_days_short, remainingDays),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }

            // 顶部右侧三个点按钮，作为菜单触发入口
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(
                    onClick = {
                        threeDotsButtonPosition?.let { onShowMenu(it) }
                    },
                    modifier = Modifier.onGloballyPositioned {
                        val positionInWindow = it.positionInWindow()
                        with(density) {
                            threeDotsButtonPosition = DpOffset(positionInWindow.x.toDp(), positionInWindow.y.toDp())
                        }
                    }
                ) {
					Text("≡", color = Color.White.copy(alpha = 0.6f))  // The [⋮] char is not an Emoji.
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                FavoriteButton(
                    isFavorite = isFavorite,
                    onToggle = onToggleFavorite
                )
            }
        }
    }
}

@Composable
fun AnimatedCountdownCard(
    title: String,
    annotatedTitle: AnnotatedString? = null,
    date: String,
    remainingDays: Long,
    icon: String,
    titleImage: TitleImageInfo? = null,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {},
    onShowMenu: (position: DpOffset) -> Unit,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {}
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
        icon = icon,
        titleImage = titleImage,
        onClick = onClick,
        onDelete = { isDeleting = true }, // 触发删除动画
        onEdit = onEdit,
        onShowMenu = onShowMenu,
        isDeleting = isDeleting,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite
    )
}
