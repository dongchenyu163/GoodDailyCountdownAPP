package com.dlx.smartalarm.demo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.*
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlinx.datetime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.hours

import dev.icerock.moko.resources.compose.stringResource

// 滚动条组件
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

import demo.composeapp.generated.resources.Res
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.scale
import demo.composeapp.generated.resources.FilterIcon
 
import org.jetbrains.compose.resources.painterResource
import com.dlx.smartalarm.demo.features.app.navigation.Screen
import com.dlx.smartalarm.demo.components.favorite.FavoriteButton
import com.dlx.smartalarm.demo.features.main.logic.FilterMenuState
import com.dlx.smartalarm.demo.features.main.logic.applyFilterSelection
import com.dlx.smartalarm.demo.features.main.logic.highlight
import com.dlx.smartalarm.demo.features.main.components.MainGridContent
import com.dlx.smartalarm.demo.features.main.components.MainListContent
import com.dlx.smartalarm.demo.components.menu.AppContextMenu
import com.dlx.smartalarm.demo.features.cards.dialogs.AddCardDialog
import com.dlx.smartalarm.demo.features.cards.dialogs.EditCardDialog
import com.dlx.smartalarm.demo.components.scroll.VerticalScrollbar
import com.dlx.smartalarm.demo.TitleImageStorage
import com.dlx.smartalarm.demo.TitleImageBitmapCache
import com.dlx.smartalarm.demo.DisplayStyle
import com.dlx.smartalarm.demo.features.settings.SettingsScreen
import com.dlx.smartalarm.demo.features.cards.logic.validateAndFixCardData

// 简单导航目的的屏幕定义（顶层，避免局部enum限制）
 

var gIsInitLoad = true  // 全局标志，指示是否为初始化加载

//   taskkill /im node.exe /f



@OptIn(ExperimentalTime::class)
@Composable
@Preview
fun App() {
	val base = Typography()
	val jpFamily = getAppFontFamily()

	val jpTypography = base.copy(
		bodyLarge = base.bodyLarge.copy(fontFamily = jpFamily),
		bodyMedium = base.bodyMedium.copy(fontFamily = jpFamily),
		bodySmall = base.bodySmall.copy(fontFamily = jpFamily),
		labelLarge = base.labelLarge.copy(fontFamily = jpFamily),
		labelMedium = base.labelMedium.copy(fontFamily = jpFamily),
		labelSmall = base.labelSmall.copy(fontFamily = jpFamily),
		titleLarge = base.titleLarge.copy(fontFamily = jpFamily),
		titleMedium = base.titleMedium.copy(fontFamily = jpFamily),
		titleSmall = base.titleSmall.copy(fontFamily = jpFamily),
		displayLarge = base.displayLarge.copy(fontFamily = jpFamily),
		displayMedium = base.displayMedium.copy(fontFamily = jpFamily),
		displaySmall = base.displaySmall.copy(fontFamily = jpFamily),
		headlineLarge = base.headlineLarge.copy(fontFamily = jpFamily),
		headlineMedium = base.headlineMedium.copy(fontFamily = jpFamily),
		headlineSmall = base.headlineSmall.copy(fontFamily = jpFamily),
	)

	AppTheme(typography = jpTypography) {
        // 简单导航与设置状态
        var currentScreen by remember { mutableStateOf(Screen.Main) }

        // 设置项
        var useCloudAccount by remember { mutableStateOf(false) }
        var appSettings by remember {
            val settings = AppSettingsManager.loadSettings()
            LocaleManager.setLocale(settings.language)
            mutableStateOf(settings)
        }

        // 搜索相关
        var searchQuery by remember { mutableStateOf("") }
        var showSearch by remember { mutableStateOf(false) }
        var filterFavorites by remember { mutableStateOf(false) }

        var cardList by remember { mutableStateOf(listOf<CardData>()) }
        var nextId by remember { mutableStateOf(0) }
        var showAddDialog by remember { mutableStateOf(false) }
        var showEditDialog by remember { mutableStateOf(false) }
        var editingCard by remember { mutableStateOf<CardData?>(null) }

        val reminderHandler = rememberReminderHandler()
        var reminderDialogCard by remember { mutableStateOf<CardData?>(null) }
        val coroutineScope = rememberCoroutineScope()

        val timeZone = remember { TimeZone.currentSystemDefault() }
        val today by produceState(initialValue = kotlin.time.Clock.System.todayIn(timeZone)) {
            while (true) {
                val now = Clock.System.now()
                val todayDate = now.toLocalDateTime(timeZone).date
                value = todayDate
                val nextDayInstant = todayDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)
                val delayDuration: Duration = nextDayInstant - now
                if (delayDuration > ZERO) {
                    delay(delayDuration)
                } else {
                    delay(1.hours)
                }
            }
        }

        fun updateCard(updated: CardData) {
            val currentCard = cardList.find { it.id == updated.id }
            if (currentCard != updated) {
                cardList = cardList.map { existing -> if (existing.id == updated.id) updated else existing }
            }
        }

        // 添加标志来区分是否为初始化加载
        var isInitialLoad by remember { mutableStateOf(gIsInitLoad) }

        // 协程作用域：此前用于测试用的手动保存功能，现已移除

        println("===")
        // 程序启动时加载卡片数据
        LaunchedEffect(Unit) {
            try {
                var loadedCards = CardDataStorage.loadCards()
                println("Pre-change")
                // 验证并更新加载的卡片数据
                loadedCards = loadedCards.map { card ->
                    val fixed = validateAndFixCardData(card)
                    val favId = TagRepository.favoriteId()
                    val withTag = if (fixed.isFavorite && !fixed.tags.contains(favId)) fixed.copy(tags = fixed.tags + favId) else fixed
                    withTag.copy(isFavorite = false)
                }
                cardList = loadedCards
                println("Post-change")
                // 计算下一个ID，确保唯一性
                nextId = if (loadedCards.isNotEmpty()) {
                    loadedCards.maxOf { it.id } + 1
                } else {
                    0
                }
                println("Loaded ${loadedCards.size} cards from file")
                // 初始化完成后，标记为非初始加载状态
                // isInitialLoad = false
            } catch (e: Exception) {
                println("Failed to load cards: ${e.message}")
                isInitialLoad = false
            }
        }

        // 监听cardList变化，自动保存到文件（但跳过初始化加载）
        LaunchedEffect(cardList) {
            if (isInitialLoad) {
                println("First load, skipping save")
                if (!cardList.isEmpty())  // 如果加载的列表不为空，说明初始化加载完成；不知为何会在列表空的时候触发一次保存。
                {
                    isInitialLoad = false
                }
                return@LaunchedEffect
            }
            // 只有在非初始化状态且列表不为空时才保存
            if (!isInitialLoad && cardList.isNotEmpty()) {
                try {
                    CardDataStorage.saveCards(cardList)
                    println("Saved ${cardList.size} cards to file")
                } catch (e: Exception) {
                    println("Failed to save cards: ${e.message}")
                }
            }
            else {
                // 如果列表为空且不是初始化，说明用户删除了所有卡片，也需要保存
                try {
                    CardDataStorage.saveCards(cardList)
                    println("Saved empty card list to file")
                } catch (e: Exception) {
                    println("Failed to save empty card list: ${e.message}")
                }
            }
        }

        // 已移除：用于测试的手动保存功能与按钮（自动保存逻辑已覆盖正常使用场景）

        // 主界面与设置/引导页的简单切换
        key(appSettings.language) {
            when (currentScreen) {
            Screen.Settings -> SettingsScreen(
                useCloud = useCloudAccount,
                displayStyle = appSettings.selectedView,
                currentLanguage = appSettings.language,
                onBack = { currentScreen = Screen.Main },
                onToggleCloud = { useCloudAccount = it },
                onChangeDisplay = { newStyle ->
                    appSettings = appSettings.copy(selectedView = newStyle)
                    AppSettingsManager.saveSettings(appSettings)
                },
                onLanguageChange = { newLanguage ->
                    LocaleManager.setLocale(newLanguage)
                    appSettings = appSettings.copy(language = newLanguage)
                    AppSettingsManager.saveSettings(appSettings)
                }
            )
            Screen.OnboardingWelcome -> WelcomeScreen(onNext = { currentScreen = Screen.OnboardingPermissions })
            Screen.OnboardingPermissions -> PermissionsScreen(onGrant = { currentScreen = Screen.Main })
            else -> MainScreen(
                cardList = cardList,
                today = today,
                searchQuery = searchQuery,
                showSearch = showSearch,
                displayStyle = appSettings.selectedView,
                onSearchChange = { searchQuery = it },
                onToggleSearch = { showSearch = !showSearch },
                onOpenSettings = { currentScreen = Screen.Settings },
                onAddClick = { showAddDialog = true },
                onEdit = { card -> editingCard = card; showEditDialog = true },
                onDelete = { id ->
                    val removed = cardList.firstOrNull { it.id == id }
                    if (removed != null) {
                        coroutineScope.launch {
                            removed.titleImage?.uuid?.let {
                                TitleImageStorage.delete(it)
                                TitleImageBitmapCache.remove(it)
                            }
                        }
                    }
                    cardList = cardList.filter { it.id != id }
                },
                onUpdateDynamic = { updated -> updateCard(updated) },
                reminderHandler = reminderHandler,
                onReminderDialog = { reminderDialogCard = it },
                filterFavorites = filterFavorites,
                onFilterChange = { filterFavorites = it }
            )
        }

        // 弹窗区（主流程共享）
        // 添加卡片
        if (showAddDialog) {
            AddCardDialog(
                nextId = nextId,
                onDismiss = { showAddDialog = false },
                onConfirm = { newCard ->
                    cardList = cardList + newCard
                    nextId++
                    showAddDialog = false
                }
            )
        }
        // 编辑卡片
        if (showEditDialog && editingCard != null) {
            EditCardDialog(
                cardData = editingCard!!,
                onDismiss = { 
                    showEditDialog = false
                    editingCard = null
                },
                onConfirm = { updatedCard ->
                    cardList = cardList.map { card -> if (card.id == updatedCard.id) updatedCard else card }
                    showEditDialog = false
                    editingCard = null
                }
            )
        }
        // 到期提醒
        reminderDialogCard?.let { dueCard ->
            AlertDialog(
                onDismissRequest = { reminderDialogCard = null },
                confirmButton = { TextButton(onClick = { reminderDialogCard = null }) { Text(stringResource(MR.strings.ok)) } },
                title = { Text(stringResource(MR.strings.reminder)) },
                text = {
                    val title = dueCard.title.ifBlank { stringResource(MR.strings.app_name) }
                    Text(stringResource(MR.strings.countdown_due_message, title))
                }
            )
        }
        }
    }
}

// CardData已在CardDataManager.kt中定义，移除重复定义

// 统一头部 + 列表的主页面
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
private fun MainScreen(
    cardList: List<CardData>,
    today: LocalDate,
    searchQuery: String,
    showSearch: Boolean,
    displayStyle: DisplayStyle,
    onSearchChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onAddClick: () -> Unit,
    onEdit: (CardData) -> Unit,
    onDelete: (Int) -> Unit,
    onUpdateDynamic: (CardData) -> Unit,
    reminderHandler: ReminderHandler,
    onReminderDialog: (CardData) -> Unit,
    filterFavorites: Boolean,
    onFilterChange: (Boolean) -> Unit
) {    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    val isWeb = remember { getPlatform().name.startsWith("Web") }
    val emojiFamily = if (isWeb) getAppEmojiFontFamily() else FontFamily.Default
    val scope = rememberCoroutineScope()

    // 显示搜索栏的条件：靠近顶部（下滑）显示
    val revealSearch by remember(displayStyle) {
        derivedStateOf {
            when (displayStyle) {
                DisplayStyle.Grid -> gridState.firstVisibleItemIndex == 0 && gridState.firstVisibleItemScrollOffset < 10
                else -> listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < 10
            }
        }
    }

    // 逗号分词（或关系）
    val tokens = remember(searchQuery) {
        searchQuery.trim().split(Regex("\\s*,\\s*")).filter { it.isNotBlank() }
    }
    var activeTagId by remember { mutableStateOf<String?>(null) }
    val filtered = remember(cardList, tokens, filterFavorites, activeTagId) {
        val favId = TagRepository.favoriteId()
        var list = if (tokens.isEmpty()) cardList
        else cardList.filter { c ->
            tokens.any { t ->
                c.title.contains(t, ignoreCase = true) || c.description.contains(t, ignoreCase = true)
            }
        }
        if (filterFavorites) {
            list = list.filter { it.tags.contains(favId) }
        }
        activeTagId?.let { tid ->
            list = list.filter { it.tags.contains(tid) }
        }
        list.sortedWith(compareByDescending<CardData> { it.tags.contains(favId) }.thenBy { it.remainingDays })
    }

    // 统一的菜单状态
    var contextMenuCard by remember { mutableStateOf<CardData?>(null) }
    var menuPosition by remember { mutableStateOf(DpOffset.Zero) }

    val showMenu: (CardData, DpOffset) -> Unit = { card, position ->
        contextMenuCard = card
        menuPosition = position
    }

    val dismissMenu = { contextMenuCard = null }

 

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = { Text(stringResource(MR.strings.app_name)) },
                        actions = {
                            var filterMenuState by remember { mutableStateOf(FilterMenuState(expanded = false, filterFavorites = filterFavorites)) }
                            var allTags by remember { mutableStateOf<List<Tag>>(emptyList()) }
                            LaunchedEffect(Unit) { allTags = TagRepository.load() }
                            LaunchedEffect(filterFavorites) { filterMenuState = filterMenuState.copy(filterFavorites = filterFavorites) }
                            Box {
                                IconButton(onClick = {
                                    scope.launch {
                                        allTags = TagRepository.load()
                                        filterMenuState = filterMenuState.copy(expanded = true)
                                    }
                                }) {
                                    Icon(
                                        painter = painterResource(Res.drawable.FilterIcon),
										modifier = Modifier.size(18.dp),
                                        contentDescription = stringResource(MR.strings.filter)
                                    )
                                }
                                DropdownMenu(
                                    expanded = filterMenuState.expanded,
                                    onDismissRequest = { filterMenuState = filterMenuState.copy(expanded = false) }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(MR.strings.filter_favorites)) },
                                        onClick = {
                                            val ns = applyFilterSelection(filterMenuState, selectFavorites = true)
                                            filterMenuState = ns
                                            onFilterChange(ns.filterFavorites)
                                            activeTagId = null
                                        },
                                        trailingIcon = if (filterMenuState.filterFavorites) { { Text("✓") } } else null
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(MR.strings.filter_all)) },
                                        onClick = {
                                            val ns = applyFilterSelection(filterMenuState, selectFavorites = false)
                                            filterMenuState = ns
                                            onFilterChange(ns.filterFavorites)
                                            activeTagId = null
                                        },
                                        trailingIcon = if (!filterMenuState.filterFavorites) { { Text("✓") } } else null
                                    )
                                    val favId = TagRepository.favoriteId()
                                    allTags.filter { it.id != favId }.forEach { tag ->
                                        DropdownMenuItem(
                                            text = { AssistChip(onClick = {}, label = { Text(tag.name) }) },
                                            onClick = {
                                                activeTagId = tag.id
                                            },
                                            trailingIcon = if (activeTagId == tag.id) { { Text("✓") } } else null
                                        )
                                    }
                                }
                            }
                            TextButton(onClick = onToggleSearch) { Text(if (showSearch) "✖" else "🔍", fontFamily = emojiFamily) }
                            TextButton(onClick = onOpenSettings) { Text("⚙", fontFamily = emojiFamily) }
                        }
                    )
                    AnimatedVisibility(visible = showSearch || revealSearch) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            placeholder = { Text(stringResource(MR.strings.search_countdown_placeholder)) },
                            singleLine = true,
                            shape = MaterialTheme.shapes.extraLarge,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onAddClick, containerColor = MaterialTheme.colorScheme.primary) {
                    Text("+", color = MaterialTheme.colorScheme.onPrimary, fontFamily = emojiFamily)
                }
            }
        ) { padding ->
            // 三种显示样式
            if (displayStyle == DisplayStyle.Grid) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)) {
                    MainGridContent(
                        filtered = filtered,
                        today = today,
                        gridState = gridState,
                        emojiFamily = emojiFamily,
                        tokens = tokens,
                        showMenu = showMenu,
                        onUpdateDynamic = onUpdateDynamic
                    )
                    VerticalScrollbar(
                        gridState = gridState,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 4.dp)
                    )
                }
            } else {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)) {
                    MainListContent(
                        filtered = filtered,
                        today = today,
                        listState = listState,
                        emojiFamily = emojiFamily,
                        tokens = tokens,
                        showMenu = showMenu,
                        onUpdateDynamic = onUpdateDynamic,
                        onDelete = onDelete,
                        onEdit = onEdit,
                        onReminderDialog = onReminderDialog,
                        reminderHandler = reminderHandler,
                        displayStyle = displayStyle
                    )
                    VerticalScrollbar(
                        listState = listState,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 4.dp)
                    )
                }
            }
        }

        // 统一的上下文菜单
        contextMenuCard?.let { card ->
            AppContextMenu(
                expanded = true,
                onDismissRequest = dismissMenu,
                offset = menuPosition,
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(MR.strings.edit)) },
                    onClick = {
                        dismissMenu()
                        onEdit(card)
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(MR.strings.delete)) },
                    onClick = {
                        dismissMenu()
                        onDelete(card.id)
                    }
                )
            }
        }
    }
}

// 相关 Composable 已拆分至独立文件：
// - CardDialogs.kt: CardDialog / AddCardDialog / EditCardDialog / DatePickerDialog
// - SettingsScreen.kt: SettingsScreen
// - Onboarding.kt: WelcomeScreen / PermissionsScreen

 
