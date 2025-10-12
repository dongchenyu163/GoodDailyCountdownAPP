package com.dlx.smartalarm.demo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlinx.datetime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.hours

import demo.composeapp.generated.resources.Res
import demo.composeapp.generated.resources.compose_multiplatform
import com.dlx.smartalarm.demo.AnimatedCountdownCard
import org.jetbrains.compose.resources.Font

import demo.composeapp.generated.resources.NotoSansSC

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
		var showContent by remember { mutableStateOf(false) }
		var isLarge by remember { mutableStateOf(false) }

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

        // 添加协程作用域用于文件操作
        val coroutineScope = rememberCoroutineScope()

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

        // 用于手动保存的函数
        fun saveCards() {
            coroutineScope.launch {
                try {
                    CardDataStorage.saveCards(cardList)
                    println("Cards manually saved")
                } catch (e: Exception) {
                    println("Failed to manually save cards: ${e.message}")
                }
            }
        }

        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(onClick = { showContent = !showContent }) {
                    Text("Click me!")
                }
				Button(onClick = { isLarge = !isLarge }) {
					Text("Switch size!")
				}
                // 添加手动保存按钮用于测试
                Button(onClick = { saveCards() }) {
                    Text("手动保存")
                }
                AnimatedVisibility(showContent) {
                    val greeting = remember { Greeting().greet() }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(painterResource(Res.drawable.compose_multiplatform), null)
                        Text("Compose: $greeting")
                    }
                }
                // 渲染所有 AnimatedCountdownCard 控件
                cardList.forEach { cardData ->
					AnimatedCountdownCard(
						title = cardData.title,
						date = cardData.date,
						remainingDays = cardData.remainingDays,
						onClick = { println("Card ${cardData.id} clicked") },
						onDelete = {
                            // 基于ID删除指定卡片
                            cardList = cardList.filter { it.id != cardData.id }
                            // 自动保存会通过LaunchedEffect触发
                        },
						onEdit = {
                            // 设置要编辑的卡片并打开编辑对话框
                            editingCard = cardData
                            showEditDialog = true
                        }
					)
				}
            }
            // 屏幕右下角添加按钮
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
            ) {
                Text("添加控件")
            }

            // 添加卡片的浮动窗口
            if (showAddDialog) {
                AddCardDialog(
                    nextId = nextId,
                    onDismiss = { showAddDialog = false },
                    onConfirm = { newCard ->
                        cardList = cardList + newCard
                        nextId++
                        showAddDialog = false
                        // 自动保存会通过LaunchedEffect触发
                    }
                )
            }
            
            // 编辑卡片的浮动窗口
            if (showEditDialog && editingCard != null) {
                EditCardDialog(
                    cardData = editingCard!!,
                    onDismiss = { 
                        showEditDialog = false 
                        editingCard = null
                    },
                    onConfirm = { updatedCard ->
                        // 更新卡片列表中的卡片
                        cardList = cardList.map { card ->
                            if (card.id == updatedCard.id) updatedCard else card
                        }
                        showEditDialog = false
                        editingCard = null
                        // 自动保存会通过LaunchedEffect触发
                    }
                )
            }

            reminderDialogCard?.let { dueCard ->
                AlertDialog(
                    onDismissRequest = { reminderDialogCard = null },
                    confirmButton = {
                        TextButton(onClick = { reminderDialogCard = null }) {
                            Text("知道了")
                        }
                    },
                    title = { Text("提醒") },
                    text = {
                        val title = dueCard.title.ifBlank { "倒计时提醒" }
                        Text("《$title》的倒计时已经到期啦！")
                    }
                )
            }
        }
    }
}

@Composable
fun CardDialog(
    cardData: CardData?,
    nextId: Int,
    onDismiss: () -> Unit,
    onConfirm: (CardData) -> Unit
) {
    // 获取当前日期作为默认值 - 使用正确的 kotlinx-datetime 0.6.0 API
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val defaultDate = if (cardData != null) {
        LocalDate.parse(cardData.date)
    } else {
        today.plus(1, DateTimeUnit.DAY)
    }
    val defaultTitle = cardData?.title ?: "测试Test #$nextId"
    val defaultRemainingDays = cardData?.remainingDays?.toString() ?: "1"

    var title by remember { mutableStateOf(defaultTitle) }
    var selectedDate by remember { mutableStateOf(defaultDate) }
    var remainingDaysText by remember { mutableStateOf(defaultRemainingDays) }
    var showDatePicker by remember { mutableStateOf(false) }

    // 添加标志来区分更新来源，避免循环引用
    var isUpdatingFromDate by remember { mutableStateOf(false) }

    // 计算剩余天数的函数
    fun calculateRemainingDays(targetDate: LocalDate): Int {
        val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return targetDate.toEpochDays() - currentDate.toEpochDays()
    }

    // 根据剩余天数计算目标日期的函数
    fun calculateTargetDate(remainingDays: Int): LocalDate {
        val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return currentDate.plus(remainingDays, DateTimeUnit.DAY)
    }

    // 只有当日期是通过日历选择器改变时才更新剩余天数
    LaunchedEffect(selectedDate) {
        if (isUpdatingFromDate) {
            val days = calculateRemainingDays(selectedDate)
            remainingDaysText = maxOf(0, days).toString()
            isUpdatingFromDate = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (cardData != null) "编辑卡片" else "添加新卡片",
                    style = MaterialTheme.typography.headlineSmall
                )

                // 标题输入框
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题") },
                    modifier = Modifier.fillMaxWidth()
                )

                // 日期选择
                OutlinedTextField(
                    value = selectedDate.toString(),
                    onValueChange = { },
                    label = { Text("目标日期") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        TextButton(onClick = { showDatePicker = true }) {
                            Text("选择日期")
                        }
                    }
                )

                // 剩余天数输入框
                OutlinedTextField(
                    value = remainingDaysText,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                            remainingDaysText = newValue
                            // 当用户输入剩余天数时，更新目标日期
                            newValue.toIntOrNull()?.let { days ->
                                if (days >= 0 && !isUpdatingFromDate) {
                                    selectedDate = calculateTargetDate(days)
                                }
                            }
                        }
                    },
                    label = { Text("剩余天数") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val remainingDays = remainingDaysText.toIntOrNull() ?: 
                                if (cardData != null) cardData.remainingDays else 1
                            val card = if (cardData != null) {
                                CardData(
                                    id = cardData.id,
                                    title = title,
                                    date = selectedDate.toString(),
                                    remainingDays = remainingDays
                                )
                            } else {
                                CardData(
                                    id = nextId,
                                    title = title,
                                    date = selectedDate.toString(),
                                    remainingDays = remainingDays
                                )
                            }
                            onConfirm(card)
                        }
                    ) {
                        Text("确认")
                    }
                }
            }
        }
    }

    // 日期选择器
    if (showDatePicker) {
        DatePickerDialog(
            selectedDate = selectedDate,
            onDateSelected = { date ->
                isUpdatingFromDate = true
                selectedDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
fun EditCardDialog(
    cardData: CardData,
    onDismiss: () -> Unit,
    onConfirm: (CardData) -> Unit
) {
    CardDialog(
        cardData = cardData,
        nextId = 0, // This won't be used since we're editing
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}

@Composable
fun AddCardDialog(
    nextId: Int,
    onDismiss: () -> Unit,
    onConfirm: (CardData) -> Unit
) {
    CardDialog(
        cardData = null,
        nextId = nextId,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    // 使用正确的kotlinx.datetime 0.6.0 API转换时间戳
    val selectedInstant = selectedDate.atStartOfDayIn(TimeZone.currentSystemDefault())
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedInstant.toEpochMilliseconds()
    )

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                // 使用正确的LocalDate转换方法
                                val instant = Instant.fromEpochMilliseconds(millis)
                                val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                                onDateSelected(localDate)
                            }
                        }
                    ) {
                        Text("确认")
                    }
                }
            }
        }
    }
}

// CardData已在CardDataManager.kt中定义，移除重复定义
