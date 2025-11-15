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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlinx.datetime.*
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.hours

import demo.composeapp.generated.resources.Res
import com.dlx.smartalarm.demo.AnimatedCountdownCard
import org.jetbrains.compose.resources.Font

import demo.composeapp.generated.resources.NotoSansSC

// 简单导航目的的屏幕定义（顶层，避免局部enum限制）
private enum class Screen { OnboardingWelcome, OnboardingPermissions, Main, Settings }

var gIsInitLoad = true  // 全局标志，指示是否为初始化加载
//   taskkill /im node.exe /f
@Composable
@Preview
fun App() {

	val base = Typography()
	val jpFamily = FontFamily(Font(Res.font.NotoSansSC, weight = FontWeight.Normal))

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
 MaterialTheme (typography = jpTypography) {

        // 简单导航与设置状态
        var currentScreen by remember { mutableStateOf(Screen.Main) }

        // 设置项
        var useCloudAccount by remember { mutableStateOf(false) }
        var displayStyle by remember { mutableStateOf(DisplayStyle.List) }

        // 搜索相关
        var searchQuery by remember { mutableStateOf("") }
        var showSearch by remember { mutableStateOf(false) }

        var cardList by remember { mutableStateOf(listOf<CardData>()) }
        var nextId by remember { mutableStateOf(0) }
        var showAddDialog by remember { mutableStateOf(false) }
        var showEditDialog by remember { mutableStateOf(false) }
        var editingCard by remember { mutableStateOf<CardData?>(null) }

        val reminderHandler = rememberReminderHandler()
        var reminderDialogCard by remember { mutableStateOf<CardData?>(null) }

        val timeZone = remember { TimeZone.currentSystemDefault() }
        val today by produceState(initialValue = Clock.System.todayIn(timeZone)) {
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
            cardList = cardList.map { existing -> if (existing.id == updated.id) updated else existing }
        }

        // 添加标志来区分是否为初始化加载
        var isInitialLoad by remember { mutableStateOf(gIsInitLoad) }

        // 协程作用域：此前用于测试用的手动保存功能，现已移除

		println("===")
        // 程序启动时加载卡片数据
        LaunchedEffect(Unit) {
            try {
                val loadedCards = CardDataStorage.loadCards()
				println("Pre-change")
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
            } else {
                // 如果列表为空且不是初始化，说明用户删除了所有卡片，也需要保存
                try {
                    CardDataStorage.saveCards(cardList)
                    println("Saved empty card list to file")
                } catch (e: Exception) {
                    println("Failed to save empty card list: ${e.message}")
                }
            }
        }

        // 已移除：用于测试的手动保存函数与按钮（自动保存逻辑已覆盖正常使用场景）

        // 主界面与设置/引导页的简单切换
        when (currentScreen) {
            Screen.Settings -> SettingsScreen(
                useCloud = useCloudAccount,
                displayStyle = displayStyle,
                onBack = { currentScreen = Screen.Main },
                onToggleCloud = { useCloudAccount = it },
                onChangeDisplay = { displayStyle = it }
            )
            Screen.OnboardingWelcome -> WelcomeScreen(onNext = { currentScreen = Screen.OnboardingPermissions })
            Screen.OnboardingPermissions -> PermissionsScreen(onGrant = { currentScreen = Screen.Main })
            else -> MainScreen(
                cardList = cardList,
                today = today,
                searchQuery = searchQuery,
                showSearch = showSearch,
                displayStyle = displayStyle,
                onSearchChange = { searchQuery = it },
                onToggleSearch = { showSearch = !showSearch },
                onOpenSettings = { currentScreen = Screen.Settings },
                onAddClick = { showAddDialog = true },
                onEdit = { card -> editingCard = card; showEditDialog = true },
                onDelete = { id -> cardList = cardList.filter { it.id != id } },
                onUpdateDynamic = { updated -> updateCard(updated) },
                reminderHandler = reminderHandler,
                onReminderDialog = { reminderDialogCard = it }
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
                confirmButton = { TextButton(onClick = { reminderDialogCard = null }) { Text("知道了") } },
                title = { Text("提醒") },
                text = { val t = dueCard.title.ifBlank { "倒计时提醒" }; Text("《$t》的倒计时已经到期啦！") }
            )
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
) {
    val listState = rememberLazyListState()
    val revealSearch by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 20 } }

    val filtered = remember(cardList, searchQuery) {
        if (searchQuery.isBlank()) cardList
        else cardList.filter { it.title.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("倒计时") },
                    navigationIcon = { TextButton(onClick = onToggleSearch) { Text(if (showSearch) "✖" else "🔍") } },
                    actions = { TextButton(onClick = onOpenSettings) { Text("⚙") } }
                )
                AnimatedVisibility(visible = showSearch || revealSearch) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("搜索...") }
                    )
                }
            }
        },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = onAddClick) { Text("新增") } }
    ) { padding ->
        // 三种显示样式
        if (displayStyle == DisplayStyle.Grid) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { cardData ->
                    val dynamicRemaining = remember(today, cardData.date) {
                        runCatching { LocalDate.parse(cardData.date) }
                            .getOrNull()
                            ?.let { targetDate ->
                                (targetDate.toEpochDays() - today.toEpochDays()).coerceAtLeast(0)
                            } ?: cardData.remainingDays
                    }

                    // 网格项（简化视觉占位）
                    Surface(tonalElevation = 2.dp, shape = MaterialTheme.shapes.large) {
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
                            Box(Modifier.fillMaxWidth()) {
                                Text(text = cardData.icon.ifBlank { "🎯" }, style = MaterialTheme.typography.headlineSmall)
                            }
                            Column { 
                                Text(cardData.title, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(2.dp))
                                Text("剩余 ${dynamicRemaining} 天", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { cardData ->
                    val dynamicRemaining = remember(today, cardData.date) {
                        runCatching { LocalDate.parse(cardData.date) }
                            .getOrNull()
                            ?.let { targetDate ->
                                (targetDate.toEpochDays() - today.toEpochDays()).coerceAtLeast(0)
                            } ?: cardData.remainingDays
                    }

                    LaunchedEffect(cardData.id, dynamicRemaining) {
                        if (dynamicRemaining != cardData.remainingDays) {
                            onUpdateDynamic(cardData.copy(remainingDays = dynamicRemaining))
                        }
                    }

                    CountdownReminderObserver(
                        card = cardData,
                        reminderHandler = reminderHandler,
                        onCardUpdate = { updated -> onUpdateDynamic(updated) },
                        onDialogRequest = { onReminderDialog(it) }
                    )

                    // 左滑删除背景（按进度渐显）
                    val dismissState = rememberDismissState(confirmStateChange = { value: DismissValue ->
                        if (value == DismissValue.DismissedToStart) {
                            onDelete(cardData.id)
                            true
                        } else false
                    })

                    SwipeToDismiss(
                        state = dismissState,
                        background = {
                            val progress = dismissState.progress.fraction.coerceIn(0f, 1f)
                            val bg = MaterialTheme.colorScheme.errorContainer.copy(alpha = progress)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (displayStyle == DisplayStyle.Card) 150.dp else 96.dp)
                                    .padding(horizontal = 8.dp)
                                    .background(bg),
                                contentAlignment = Alignment.CenterEnd
                            ) {
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
                                    Text("🗑", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onErrorContainer)
                                    Spacer(Modifier.width(8.dp))
                                    Text("删除", color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        },
                        dismissContent = {
                            if (displayStyle == DisplayStyle.List) {
                                // 紧凑行样式
                                Surface(shape = MaterialTheme.shapes.large, tonalElevation = 1.dp) {
                                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) {
                                            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                                                Text(cardData.icon.ifBlank { "🎯" })
                                            }
                                        }
                                        Spacer(Modifier.width(16.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(cardData.title, style = MaterialTheme.typography.titleMedium)
                                            val endText = runCatching { LocalDate.parse(cardData.date) }.getOrNull()?.let { d ->
                                                "ends on ${d.monthNumber}/${d.dayOfMonth}/${d.year}"
                                            } ?: cardData.date
                                            Text(endText, style = MaterialTheme.typography.bodyMedium)
                                        }
                                        Text("${dynamicRemaining}d", style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            } else {
                                // 大卡样式
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    AnimatedCountdownCard(
                                        title = cardData.title,
                                        date = cardData.date,
                                        remainingDays = dynamicRemaining,
                                        onClick = { /* 预留 */ },
                                        onDelete = { onDelete(cardData.id) },
                                        onEdit = { onEdit(cardData) }
                                    )
                                }
                            }
                        },
                        directions = setOf(DismissDirection.EndToStart)
                    )
                }
            }
        }
    }
}

// 相关 Composable 已拆分至独立文件：
// - CardDialogs.kt: CardDialog / AddCardDialog / EditCardDialog / DatePickerDialog
// - SettingsScreen.kt: SettingsScreen
// - Onboarding.kt: WelcomeScreen / PermissionsScreen
