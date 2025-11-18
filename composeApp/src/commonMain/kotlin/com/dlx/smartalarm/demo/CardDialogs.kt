package com.dlx.smartalarm.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.datetime.*
import androidx.compose.foundation.rememberScrollState // New import
import androidx.compose.foundation.verticalScroll // New import
import androidx.compose.ui.Alignment // New import
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CardDialog(
    cardData: CardData?,
    nextId: Int,
    onDismiss: () -> Unit,
    onConfirm: (CardData) -> Unit
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val defaultDate = if (cardData != null) {
        LocalDate.parse(cardData.date)
    } else {
        today.plus(1, DateTimeUnit.DAY)
    }
    val defaultTitle = cardData?.title ?: "测试Test #$nextId"
    val defaultRemainingDays = cardData?.let {
        runCatching {
            val targetDate = LocalDate.parse(it.date)
            val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
            (targetDate.toEpochDays() - currentDate.toEpochDays()).coerceAtLeast(0).toString()
        }.getOrDefault(it.remainingDays.toString())
    } ?: "1"

    var title by remember { mutableStateOf(defaultTitle) }
    var description by remember { mutableStateOf(cardData?.description ?: "") }
    // 图标选择器：预设若干 emoji 图标，默认第一个或已有值
    val presetIcons = listOf("🎉", "✈️", "🎂", "🎓", "💼", "🖥️", "🏖️", "📅", "⭐")
    var icon by remember { mutableStateOf(cardData?.icon?.takeIf { it.isNotBlank() } ?: presetIcons.first()) }
    var selectedDate by remember { mutableStateOf(defaultDate) }
    var remainingDaysText by remember { mutableStateOf(defaultRemainingDays) }
    var showDatePicker by remember { mutableStateOf(false) }

    var isUpdatingFromDate by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var titleImage by remember(cardData?.id, cardData?.titleImage) { mutableStateOf(cardData?.titleImage) }
    var showImageEditor by remember { mutableStateOf(false) }
    var imagePickerMessage by remember { mutableStateOf<String?>(null) }
    var isPickingImage by remember { mutableStateOf(false) }

    val selectTitleImage: () -> Unit = {
        coroutineScope.launch {
            isPickingImage = true
            imagePickerMessage = null
            val picked = runCatching { pickImageFromUser() }.getOrElse {
                imagePickerMessage = it.message ?: "选择图片时发生错误"
                isPickingImage = false
                return@launch
            }
            if (picked == null) {
                imagePickerMessage = "未选择图片或当前平台暂不支持文件选择"
                isPickingImage = false
                return@launch
            }
            val updated = replaceCardImage(titleImage, picked, TitleImageDefaultQuality)
            if (updated == null) {
                imagePickerMessage = "无法读取所选图片，请尝试其他文件"
            } else {
                titleImage = updated
                showImageEditor = true
            }
            isPickingImage = false
        }
    }

    val clearTitleImage: () -> Unit = {
        coroutineScope.launch {
            titleImage?.uuid?.let {
                TitleImageStorage.delete(it)
                TitleImageBitmapCache.remove(it)
            }
            titleImage = null
            imagePickerMessage = null
            showImageEditor = false
        }
    }

    fun calculateRemainingDays(targetDate: LocalDate): Int {
        val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return targetDate.toEpochDays() - currentDate.toEpochDays()
    }

    fun calculateTargetDate(remainingDays: Int): LocalDate {
        val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return currentDate.plus(remainingDays, DateTimeUnit.DAY)
    }

    LaunchedEffect(selectedDate) {
        if (isUpdatingFromDate) {
            val days = calculateRemainingDays(selectedDate)
            remainingDaysText = maxOf(0, days).toString()
            isUpdatingFromDate = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val scrollState = rememberScrollState() // New
        Card(
            modifier = Modifier
                .widthIn(max = 600.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), // New
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // New
        ) {
            Box(modifier = Modifier.fillMaxSize()) { // New Box to hold scrollable content and scrollbar
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(scrollState), // Added verticalScroll
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (cardData != null) "编辑卡片" else "添加新卡片",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("标题") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("描述") },
                        modifier = Modifier.fillMaxWidth()
                    )

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

                    OutlinedTextField(
                        value = remainingDaysText,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                                remainingDaysText = newValue
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

                    Text("选择图标", style = MaterialTheme.typography.titleSmall)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetIcons.forEach { ic ->
                            val selected = icon == ic
                            AssistChip(
                                onClick = { icon = ic },
                                label = { Text(ic) },
                                leadingIcon = null,
                                modifier = Modifier.padding(bottom = 8.dp),
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    labelColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    Text("标题图片", style = MaterialTheme.typography.titleSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { selectTitleImage() },
                            enabled = !isPickingImage
                        ) {
                            Text(if (isPickingImage) "选择中..." else "选择文件")
                        }
                        OutlinedButton(
                            onClick = { showImageEditor = true },
                            enabled = titleImage != null
                        ) {
                            Text("编辑图片大小")
                        }
                        TextButton(
                            onClick = { clearTitleImage() },
                            enabled = titleImage != null
                        ) {
                            Text("清除图片")
                        }
                    }
                    imagePickerMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    titleImage?.let {
                        Text(
                            text = "当前图片ID: ${it.uuid}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text("取消") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val remainingDays = maxOf(0, remainingDaysText.toIntOrNull() ?: (cardData?.remainingDays ?: 1))
                                val reminderSent = if (remainingDays > 0) false else cardData?.reminderSent ?: false
                                val card = if (cardData != null) {
                                    CardData(
                                        id = cardData.id,
                                        title = title,
                                        date = selectedDate.toString(),
                                        remainingDays = remainingDays,
                                        reminderSent = reminderSent,
                                        description = description,
                                        icon = icon,
                                        titleImage = titleImage
                                    )
                                } else {
                                    CardData(
                                        id = nextId,
                                        title = title,
                                        date = selectedDate.toString(),
                                        remainingDays = remainingDays,
                                        reminderSent = reminderSent,
                                        description = description,
                                        icon = icon,
                                        titleImage = titleImage
                                    )
                                }
                                onConfirm(card)
                            }
                        ) { Text("确认") }
                    }
                }
                VerticalScrollbar( // New
                    scrollState = scrollState,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }

    if (showImageEditor && titleImage != null) {
        ImageOffsetEditorDialog(
            titleImageInfo = titleImage!!,
            onDismiss = { showImageEditor = false },
            onApply = { updated ->
                titleImage = updated
                showImageEditor = false
            }
        )
    }

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
        nextId = 0,
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
    val selectedInstant = selectedDate.atStartOfDayIn(TimeZone.currentSystemDefault())
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedInstant.toEpochMilliseconds()
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), // New
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // New
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("取消") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val instant = Instant.fromEpochMilliseconds(millis)
                                val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                                onDateSelected(localDate)
                            }
                        }
                    ) { Text("确认") }
                }
            }
        }
    }
}
