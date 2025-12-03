package com.dlx.smartalarm.demo.features.cards.dialogs

import com.dlx.smartalarm.demo.MR
import dev.icerock.moko.resources.compose.stringResource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import kotlin.time.ExperimentalTime
import kotlin.time.Clock
import kotlinx.coroutines.launch
import com.dlx.smartalarm.demo.CardData
import com.dlx.smartalarm.demo.core.model.Tag
import com.dlx.smartalarm.demo.core.model.TagColor
import com.dlx.smartalarm.demo.features.cards.logic.TagRepository
import com.dlx.smartalarm.demo.TitleImageStorage
import com.dlx.smartalarm.demo.components.image.TitleImageBitmapCache
import com.dlx.smartalarm.demo.components.image.TitleImageDefaultQuality
import com.dlx.smartalarm.demo.components.image.replaceCardImage
import com.dlx.smartalarm.demo.components.image.ImageOffsetEditorDialog
import com.dlx.smartalarm.demo.components.menu.TagMultiselect
import com.dlx.smartalarm.demo.components.image.TitleImageInfo
import com.dlx.smartalarm.demo.components.image.TitleImageViewType
import com.dlx.smartalarm.demo.components.image.replaceCardImage
import com.dlx.smartalarm.demo.components.scroll.VerticalScrollbar
import com.dlx.smartalarm.demo.pickImageFromUser
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState

// 本文件中时间格式统一使用简单的 "HH:mm" 字符串，并且完全不调用任何 format(...) 扩展，避免与 kotlinx-datetime 的 format 重载冲突
private fun parseTimeString(time: String?): Pair<Int, Int> {
    if (time.isNullOrBlank()) return 9 to 0
    val parts = time.split(":")
    val h = parts.getOrNull(0)?.toIntOrNull() ?: 9
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return h.coerceIn(0, 23) to m.coerceIn(0, 59)
}

// 手动补零，不使用 String.format / "%02d".format
private fun formatTimeString(hour: Int, minute: Int): String {
    val h = hour.coerceIn(0, 23)
    val m = minute.coerceIn(0, 59)
    val hh = if (h < 10) "0$h" else h.toString()
    val mm = if (m < 10) "0$m" else m.toString()
    return "$hh:$mm"
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalTime::class, ExperimentalMaterial3Api::class)
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
    val defaultTitle = cardData?.title ?: stringResource(MR.strings.default_test_title_prefix, nextId)
    val defaultRemainingDays = cardData?.let {
        runCatching {
            val targetDate = LocalDate.parse(it.date)
            val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
            (targetDate.toEpochDays() - currentDate.toEpochDays()).coerceAtLeast(0).toString()
        }.getOrDefault(it.remainingDays.toString())
    } ?: "1"

    var title by remember { mutableStateOf(defaultTitle) }
    var description by remember { mutableStateOf(cardData?.description ?: "") }
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

    // Reminder settings state
    var reminderFrequency by remember { mutableStateOf(cardData?.reminderFrequency ?: "none") }
    var reminderTime by remember { mutableStateOf(cardData?.reminderTime ?: "09:00") }
    val (initHour, initMinute) = remember { parseTimeString(reminderTime) }
    var selectedHour by remember { mutableStateOf(initHour) }
    var selectedMinute by remember { mutableStateOf(initMinute) }

    val errorPickingImage = stringResource(MR.strings.error_picking_image)
    val noImagePickedOrUnsupported = stringResource(MR.strings.no_image_picked_or_unsupported)
    val cannotReadImage = stringResource(MR.strings.cannot_read_image)

    val selectTitleImage: () -> Unit = {
        coroutineScope.launch {
            isPickingImage = true
            imagePickerMessage = null
            val picked = runCatching { pickImageFromUser() }.getOrElse {
                imagePickerMessage = it.message ?: errorPickingImage
                isPickingImage = false
                return@launch
            }
            if (picked == null) {
                imagePickerMessage = noImagePickedOrUnsupported
                isPickingImage = false
                return@launch
            }
            val updated = replaceCardImage(titleImage, picked, TitleImageDefaultQuality)
            if (updated == null) {
                imagePickerMessage = cannotReadImage
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

    fun calculateRemainingDays(targetDate: LocalDate): Long {
        val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return targetDate.toEpochDays() - currentDate.toEpochDays()
    }

    fun calculateTargetDate(remainingDays: Long): LocalDate {
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

    var allTags by remember { mutableStateOf<List<Tag>>(emptyList()) }
    var selectedTagIds by remember { mutableStateOf(cardData?.tags ?: emptyList()) }
    var editingTagState by remember { mutableStateOf<Tag?>(null) }
    LaunchedEffect(Unit) { allTags = TagRepository.load() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val scrollState = rememberScrollState()
        Card(
            modifier = Modifier
                .widthIn(max = 600.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (cardData != null) stringResource(MR.strings.edit_card) else stringResource(MR.strings.add_new_card),
                        style = MaterialTheme.typography.headlineSmall
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(stringResource(MR.strings.title)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(stringResource(MR.strings.description)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = selectedDate.toString(),
                        onValueChange = { },
                        label = { Text(stringResource(MR.strings.target_date)) },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            TextButton(onClick = { showDatePicker = true }) {
                                Text(stringResource(MR.strings.select_date))
                            }
                        }
                    )

                    OutlinedTextField(
                        value = remainingDaysText,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() } || newValue.isEmpty()) {
                                remainingDaysText = newValue
                                newValue.toLongOrNull()?.let { days: Long ->
                                    if (days >= 0 && !isUpdatingFromDate) {
                                        selectedDate = calculateTargetDate(days)
                                    }
                                }
                            }
                        },
                        label = { Text(stringResource(MR.strings.remaining_days_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(stringResource(MR.strings.select_icon), style = MaterialTheme.typography.titleSmall)
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

                    Text(stringResource(MR.strings.title_image), style = MaterialTheme.typography.titleSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { selectTitleImage() },
                            enabled = !isPickingImage
                        ) {
                            Text(if (isPickingImage) stringResource(MR.strings.picking_in_progress) else stringResource(MR.strings.select_file))
                        }
                        OutlinedButton(
                            onClick = { showImageEditor = true },
                            enabled = titleImage != null
                        ) {
                            Text(stringResource(MR.strings.edit_image_size))
                        }
                        TextButton(
                            onClick = { clearTitleImage() },
                            enabled = titleImage != null
                        ) {
                            Text(stringResource(MR.strings.clear_image))
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
                            text = stringResource(MR.strings.current_image_id, it.uuid),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    TagMultiselect(
                        allTags = allTags,
                        selected = selectedTagIds,
                        onChange = { selectedTagIds = it },
                        onCreate = { name ->
                            val newTag = TagRepository.create(name)
                            val next = allTags + newTag
                            allTags = next
                            coroutineScope.launch { TagRepository.save(next) }
                            selectedTagIds = selectedTagIds + newTag.id
                        },
                        onEditRequest = { tag -> editingTagState = tag }
                    )

                    // Reminder settings title
                    Text(stringResource(MR.strings.reminder_settings), style = MaterialTheme.typography.titleSmall)

                    // Reminder frequency dropdown
                    var frequencyExpanded by remember { mutableStateOf(false) }
                    val frequencyOptions = listOf(
                        "none" to stringResource(MR.strings.reminder_frequency_none),
                        "once" to stringResource(MR.strings.reminder_frequency_once),
                        "daily" to stringResource(MR.strings.reminder_frequency_daily),
                        "weekly" to stringResource(MR.strings.reminder_frequency_weekly),
                    )

                    ExposedDropdownMenuBox(
                        expanded = frequencyExpanded,
                        onExpandedChange = { frequencyExpanded = !frequencyExpanded }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = frequencyOptions.firstOrNull { it.first == reminderFrequency }?.second
                                ?: stringResource(MR.strings.reminder_frequency_none),
                            onValueChange = {},
                            label = { Text(stringResource(MR.strings.reminder_frequency_label)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                        )
                        ExposedDropdownMenu(
                            expanded = frequencyExpanded,
                            onDismissRequest = { frequencyExpanded = false }
                        ) {
                            frequencyOptions.forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        reminderFrequency = value
                                        frequencyExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (reminderFrequency != "none") {
                        Text(stringResource(MR.strings.reminder_time_label), style = MaterialTheme.typography.titleSmall)

                        // 只读展示当前时间
                        OutlinedTextField(
                            value = formatTimeString(selectedHour, selectedMinute),
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* 占位：未来如需弹出独立对话框，可在此处理 */ },
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TimeWheel(
                                range = 0..23,
                                selected = selectedHour,
                                onSelectedChange = { h ->
                                    selectedHour = h
                                    reminderTime = formatTimeString(selectedHour, selectedMinute)
                                },
                                modifier = Modifier.weight(1f)
                            )
                            TimeWheel(
                                range = 0..59,
                                selected = selectedMinute,
                                onSelectedChange = { m ->
                                    selectedMinute = m
                                    reminderTime = formatTimeString(selectedHour, selectedMinute)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    val summaryText = when (reminderFrequency) {
                        "none" -> stringResource(MR.strings.reminder_summary_none)
                        "once" -> stringResource(MR.strings.reminder_summary_once, reminderTime)
                        "daily" -> stringResource(MR.strings.reminder_summary_daily, reminderTime)
                        "weekly" -> stringResource(MR.strings.reminder_summary_weekly, reminderTime)
                        else -> stringResource(MR.strings.reminder_summary_none)
                    }

                    Text(
                        text = summaryText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text(stringResource(MR.strings.cancel)) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val remainingDays = maxOf(0, remainingDaysText.toLongOrNull() ?: (cardData?.remainingDays ?: 1))
                                val reminderSent = if (remainingDays > 0) false else cardData?.reminderSent ?: false
                                val reminderOffsetMinutes = when (reminderFrequency) {
                                    "none" -> 0
                                    "once" -> 0
                                    "daily" -> 1440
                                    "weekly" -> 10080
                                    else -> 0
                                }
                                val card = if (cardData != null) {
                                    cardData.copy(
                                        title = title,
                                        date = selectedDate.toString(),
                                        remainingDays = remainingDays,
                                        reminderSent = reminderSent,
                                        reminderOffsetMinutes = reminderOffsetMinutes,
                                        description = description,
                                        icon = icon,
                                        titleImage = titleImage,
                                        reminderFrequency = reminderFrequency,
                                        reminderTime = reminderTime,
                                        tags = selectedTagIds
                                    )
                                } else {
                                    CardData(
                                        id = nextId,
                                        title = title,
                                        date = selectedDate.toString(),
                                        remainingDays = remainingDays,
                                        reminderSent = reminderSent,
                                        reminderOffsetMinutes = reminderOffsetMinutes,
                                        description = description,
                                        icon = icon,
                                        titleImage = titleImage,
                                        reminderFrequency = reminderFrequency,
                                        reminderTime = reminderTime,
                                        tags = selectedTagIds
                                    )
                                }
                                onConfirm(card)
                            }
                        ) { Text(stringResource(MR.strings.confirm)) }
                    }
                }
                VerticalScrollbar(
                    scrollState = scrollState,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }

    editingTagState?.let { tag ->
        AlertDialog(
            onDismissRequest = { editingTagState = null },
            confirmButton = {},
            title = { Text(stringResource(MR.strings.tags_label)) },
            text = {
                var name by remember(tag.id) { mutableStateOf(tag.name) }
                var color by remember(tag.id) { mutableStateOf(tag.color) }
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it })
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(MR.strings.colors))
                    val options = listOf(
                        TagColor.Default, TagColor.Gray, TagColor.Brown, TagColor.Orange, TagColor.Yellow,
                        TagColor.Green, TagColor.Blue, TagColor.Purple, TagColor.Pink, TagColor.Red
                    )
                    options.forEach { c ->
                        TextButton(onClick = { color = c }) { Text(if (c == color) "✓" else " "); Spacer(Modifier.width(8.dp)); Text(c.name) }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            val updated = Tag(id = tag.id, name = name.trim(), color = color)
                            val next = allTags.map { if (it.id == tag.id) updated else it }
                            allTags = next
                            coroutineScope.launch { TagRepository.save(next) }
                            editingTagState = null
                        }) { Text(stringResource(MR.strings.confirm)) }
                        TextButton(onClick = {
                            val next = allTags.filter { it.id != tag.id }
                            allTags = next
                            selectedTagIds = selectedTagIds.filter { it != tag.id }
                            coroutineScope.launch { TagRepository.save(next) }
                            editingTagState = null
                        }) { Text(stringResource(MR.strings.delete)) }
                    }
                }
            }
        )
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    TextButton(onClick = onDismiss) { Text(stringResource(MR.strings.cancel)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val instant = Instant.fromEpochMilliseconds(millis)
                                val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                                onDateSelected(localDate)
                            }
                        }
                    ) { Text(stringResource(MR.strings.confirm)) }
                }
            }
        }
    }
}

@Composable
private fun TimeWheel(
    range: IntProgression,
    selected: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = remember(range) { range.toList() }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = items.indexOf(selected).coerceAtLeast(0)
    )

    LaunchedEffect(selected) {
        val index = items.indexOf(selected)
        if (index >= 0) listState.animateScrollToItem(index)
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        LazyColumn(
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(items.size) { index ->
                val value = items[index]
                val isSelected = value == selected
                val text = if (value in 0..9) "0$value" else value.toString()
                TextButton(onClick = { onSelectedChange(value) }) {
                    Text(
                        text = text,
                        style = if (isSelected) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
