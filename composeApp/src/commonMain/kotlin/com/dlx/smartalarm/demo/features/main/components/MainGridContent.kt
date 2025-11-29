package com.dlx.smartalarm.demo.features.main.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.dlx.smartalarm.demo.CardData
import com.dlx.smartalarm.demo.TagRepository
import com.dlx.smartalarm.demo.features.main.logic.highlight
import com.dlx.smartalarm.demo.components.favorite.FavoriteButton
import com.dlx.smartalarm.demo.TitleImageViewType
import com.dlx.smartalarm.demo.TitleImageBackground
import com.dlx.smartalarm.demo.DefaultGridImageGradient
import com.dlx.smartalarm.demo.GradientOrientation
import com.dlx.smartalarm.demo.MR
import dev.icerock.moko.resources.compose.stringResource
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.datetime.LocalDate

@Composable
fun MainGridContent(
    filtered: List<CardData>,
    today: LocalDate,
    gridState: LazyGridState,
    emojiFamily: FontFamily,
    tokens: List<String>,
    showMenu: (CardData, DpOffset) -> Unit,
    onUpdateDynamic: (CardData) -> Unit
) {
    if (filtered.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(MR.strings.no_results), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
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

                var threeDotsButtonPosition by remember { mutableStateOf<DpOffset?>(null) }
                var itemPositionInWindow by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
                val density = LocalDensity.current

                Surface(
                    tonalElevation = 2.dp,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .onGloballyPositioned {
                            itemPositionInWindow = it.positionInWindow()
                        }
                ) {
                    var appeared by remember { mutableStateOf(false) }
                    val alpha by animateFloatAsState(if (appeared) 1f else 0f, label = "gAlpha")
                    val ty by animateFloatAsState(if (appeared) 0f else 12f, label = "gTy")
                    LaunchedEffect(Unit) { appeared = true }

                    TitleImageBackground(
                        titleImage = cardData.titleImage,
                        viewType = TitleImageViewType.Grid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .graphicsLayer(alpha = alpha, translationY = ty),
                        gradientSpec = DefaultGridImageGradient,
                        gradientOrientation = GradientOrientation.Vertical,
                        overlayColor = Color(0xFF0F2E1F)
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(Modifier.fillMaxWidth()) {
                                    Text(text = cardData.icon.ifBlank { "🎯" }, style = MaterialTheme.typography.headlineSmall, fontFamily = emojiFamily)
                                }
                                Column {
                                    Text(highlight(cardData.title, tokens), style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(2.dp))
                                    Text(stringResource(MR.strings.remaining_days, dynamicRemaining), style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
                                androidx.compose.material3.IconButton(
                                    onClick = {
                                        threeDotsButtonPosition?.let { showMenu(cardData, it) }
                                    },
                                    modifier = Modifier.onGloballyPositioned {
                                        val positionInWindow = it.positionInWindow()
                                        with(density) {
                                            threeDotsButtonPosition = DpOffset(positionInWindow.x.toDp(), positionInWindow.y.toDp())
                                        }
                                    }
                                ) { androidx.compose.material3.Text("≡", fontFamily = com.dlx.smartalarm.demo.getAppFontFamily()) }
                            }

                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
                                val favId = TagRepository.favoriteId()
                                FavoriteButton(
                                    isFavorite = cardData.tags.contains(favId),
                                    onToggle = {
                                        val nowFav = !cardData.tags.contains(favId)
                                        val nextTags = if (nowFav) (cardData.tags + favId).distinct() else cardData.tags.filter { it != favId }
                                        onUpdateDynamic(cardData.copy(isFavorite = false, tags = nextTags))
                                    },
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
