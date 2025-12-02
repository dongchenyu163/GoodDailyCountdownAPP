package com.dlx.smartalarm.demo.features.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.dlx.smartalarm.demo.*
import com.dlx.smartalarm.demo.features.cards.logic.TagRepository
import com.dlx.smartalarm.demo.components.card.AnimatedCountdownCard
import dev.icerock.moko.resources.compose.stringResource
import com.dlx.smartalarm.demo.components.scroll.VerticalScrollbar
import com.dlx.smartalarm.demo.features.main.logic.highlight
import com.dlx.smartalarm.demo.components.favorite.FavoriteButton
import com.dlx.smartalarm.demo.components.image.TitleImageViewType
import com.dlx.smartalarm.demo.components.image.TitleImageBackground
import com.dlx.smartalarm.demo.components.image.DefaultListImageGradient
import com.dlx.smartalarm.demo.components.image.GradientOrientation
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MainListContent(
    filtered: List<CardData>,
    today: LocalDate,
    listState: LazyListState,
    emojiFamily: FontFamily,
    tokens: List<String>,
    showMenu: (CardData, DpOffset) -> Unit,
    onUpdateDynamic: (CardData) -> Unit,
    onDelete: (Int) -> Unit,
    onEdit: (CardData) -> Unit,
    onReminderDialog: (CardData) -> Unit,
    reminderHandler: ReminderHandler,
    displayStyle: DisplayStyle
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(filtered, key = { it.id }) { cardData ->
            val dynamicRemaining = remember(today, cardData.date) {
                runCatching { kotlinx.datetime.LocalDate.parse(cardData.date) }
                    .getOrNull()
                    ?.let { targetDate ->
                        (targetDate.toEpochDays() - today.toEpochDays()).coerceAtLeast(0)
                    } ?: cardData.remainingDays
            }

            CountdownReminderObserver(
                card = cardData,
                reminderHandler = reminderHandler,
                onCardUpdate = { updated -> onUpdateDynamic(updated) },
                onDialogRequest = { onReminderDialog(it) }
            )

            val dismissState = rememberDismissState(confirmStateChange = { value: DismissValue ->
                if (value == DismissValue.DismissedToStart) {
                    onDelete(cardData.id)
                    true
                } else false
            })

            SwipeToDismiss(
                state = dismissState,
                modifier = Modifier.clip(MaterialTheme.shapes.large),
                background = {
                    val isActive = dismissState.dismissDirection != null ||
                        dismissState.targetValue != DismissValue.Default ||
                        dismissState.currentValue != DismissValue.Default
                    val progress = if (isActive) dismissState.progress.fraction.coerceIn(0f, 1f) else 0f
                    val bg = MaterialTheme.colorScheme.errorContainer.copy(alpha = progress)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (displayStyle == DisplayStyle.Card) 220.dp else 96.dp)
                            .background(bg),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        if (progress > 0.02f) {
                            Row(
                                modifier = Modifier
                                    .padding(end = 16.dp + (40f * (1f - progress)).dp)
                                    .graphicsLayer(
                                        alpha = progress,
                                        scaleX = 0.85f + 0.15f * progress,
                                        scaleY = 0.85f + 0.15f * progress
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🗑", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onErrorContainer, fontFamily = emojiFamily)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(MR.strings.delete), color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                },
                dismissContent = {
                    if (displayStyle == DisplayStyle.List) {
                        var itemPositionInWindow by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
                        val density = LocalDensity.current
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            tonalElevation = 1.dp,
                            modifier = Modifier
                                .onGloballyPositioned {
                                    itemPositionInWindow = it.positionInWindow()
                                }
                                .pointerInput(cardData.id) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                                                val localPosition = event.changes.first().position
                                                val globalPosition = itemPositionInWindow + localPosition
                                                with(density) {
                                                    showMenu(cardData, DpOffset(globalPosition.x.toDp(), globalPosition.y.toDp()))
                                                }
                                                event.changes.forEach { it.consume() }
                                            }
                                        }
                                    }
                                }
                        ) {
                            var appeared by remember { mutableStateOf(false) }
                            val alpha by androidx.compose.animation.core.animateFloatAsState(if (appeared) 1f else 0f, label = "lAlpha")
                            val ty by androidx.compose.animation.core.animateFloatAsState(if (appeared) 0f else 8f, label = "lTy")
                            LaunchedEffect(Unit) { appeared = true }

                            var threeDotsButtonPosition by remember { mutableStateOf<DpOffset?>(null) }

                            Surface(
                                shape = MaterialTheme.shapes.large,
                                tonalElevation = 1.dp,
                                modifier = Modifier.graphicsLayer(alpha = alpha, translationY = ty)
                            ) {
                                TitleImageBackground(
                                    titleImage = cardData.titleImage,
                                    viewType = TitleImageViewType.List,
                                    modifier = Modifier.fillMaxWidth(),
                                    gradientSpec = DefaultListImageGradient,
                                    gradientOrientation = GradientOrientation.Horizontal,
                                    overlayColor = Color(0xFF0F2E1F)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = MaterialTheme.shapes.large,
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                                                Text(cardData.icon.ifBlank { "🎯" }, fontFamily = emojiFamily)
                                            }
                                        }
                                        Spacer(Modifier.width(16.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(highlight(cardData.title, tokens), style = MaterialTheme.typography.titleMedium)
                                            val endText = runCatching { kotlinx.datetime.LocalDate.parse(cardData.date) }.getOrNull()?.let { d ->
                                                stringResource(MR.strings.ends_on_date, d.monthNumber, d.dayOfMonth, d.year)
                                            } ?: cardData.date
                                            Text(endText, style = MaterialTheme.typography.bodyMedium)
                                        }
                                        Text(stringResource(MR.strings.remaining_days_short, dynamicRemaining), style = MaterialTheme.typography.titleMedium)
                                        val favId = TagRepository.favoriteId()
                                        FavoriteButton(
                                            isFavorite = cardData.tags.contains(favId),
                                            onToggle = {
                                                val nowFav = !cardData.tags.contains(favId)
                                                val nextTags = if (nowFav) (cardData.tags + favId).distinct() else cardData.tags.filter { it != favId }
                                                onUpdateDynamic(cardData.copy(isFavorite = false, tags = nextTags))
                                            }
                                        )
                                        IconButton(
                                            onClick = {
                                                threeDotsButtonPosition?.let { showMenu(cardData, it) }
                                            },
                                            modifier = Modifier.onGloballyPositioned {
                                                val positionInWindow = it.positionInWindow()
                                                with(density) {
                                                    threeDotsButtonPosition = DpOffset(positionInWindow.x.toDp(), positionInWindow.y.toDp())
                                                }
                                            }
                                        ) { Text("≡", fontFamily = getAppFontFamily()) }
                                    }
                                }
                            }
                        }
                    } else {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            AnimatedCountdownCard(
                                title = cardData.title,
                                annotatedTitle = highlight(cardData.title, tokens),
                                date = cardData.date,
                                remainingDays = dynamicRemaining,
                                icon = cardData.icon,
                                titleImage = cardData.titleImage,
                                onClick = { },
                                onDelete = { onDelete(cardData.id) },
                                onEdit = { onEdit(cardData) },
                                onShowMenu = { position -> showMenu(cardData, position) },
                                isFavorite = cardData.tags.contains(TagRepository.favoriteId()),
                                onToggleFavorite = {
                                    val fav = TagRepository.favoriteId()
                                    val nowFav = !cardData.tags.contains(fav)
                                    val nextTags = if (nowFav) (cardData.tags + fav).distinct() else cardData.tags.filter { it != fav }
                                    onUpdateDynamic(cardData.copy(isFavorite = false, tags = nextTags))
                                }
                            )
                        }
                    }
                },
                directions = setOf(DismissDirection.EndToStart)
            )
        }
        if (filtered.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(MR.strings.no_results), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
