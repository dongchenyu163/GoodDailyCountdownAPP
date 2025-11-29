package com.dlx.smartalarm.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagMultiselect(
    allTags: List<Tag>,
    selected: List<String>,
    onChange: (List<String>) -> Unit,
    onCreate: (String) -> Unit,
    onEditRequest: (Tag) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf(TextFieldValue("")) }

    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(MR.strings.tags_label), style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.width(12.dp))
            if (selected.isEmpty()) {
                Text(stringResource(MR.strings.empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    selected.mapNotNull { id -> allTags.find { it.id == id } }.forEach { tag ->
                        AssistChip(
                            onClick = { },
                            label = { Text(tag.name) },
                            trailingIcon = { Text("✖") },
                            colors = AssistChipDefaults.assistChipColors(containerColor = tagColor(tag.color))
                        )
                    }
                }
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(stringResource(MR.strings.search_placeholder)) },
                modifier = Modifier.padding(12.dp).width(280.dp)
            )
            val filtered = remember(query.text, allTags) {
                val q = query.text.trim().lowercase()
                if (q.isBlank()) allTags else allTags.filter { it.name.lowercase().contains(q) }
            }
            Box(Modifier.size(width = 280.dp, height = 300.dp)) {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(filtered) { tag ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val next = if (selected.contains(tag.id)) selected else selected + tag.id
                                    onChange(next)
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AssistChip(onClick = {}, label = { Text(tag.name) }, colors = AssistChipDefaults.assistChipColors(containerColor = tagColor(tag.color)))
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = { onEditRequest(tag) }) { Text("…") }
                        }
                    }
                }
            }
            val q = query.text.trim()
            if (q.isNotBlank() && allTags.none { it.name.equals(q, ignoreCase = true) }) {
                DropdownMenuItem(
                    text = { Row(verticalAlignment = Alignment.CenterVertically) { Text(stringResource(MR.strings.create_prefix)); Spacer(Modifier.width(8.dp)); AssistChip(onClick = {}, label = { Text(q) }, colors = AssistChipDefaults.assistChipColors(containerColor = tagColor(TagColor.Blue))) } },
                    onClick = {
                        onCreate(q)
                        query = TextFieldValue("")
                    }
                )
            }
        }
    }
}

@Composable
private fun tagColor(color: TagColor): Color {
    return when (color) {
        TagColor.Default -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        TagColor.Gray -> Color(0xFF888888).copy(alpha = 0.25f)
        TagColor.Brown -> Color(0xFF8D6E63).copy(alpha = 0.25f)
        TagColor.Orange -> Color(0xFFFFA726).copy(alpha = 0.25f)
        TagColor.Yellow -> Color(0xFFFFEB3B).copy(alpha = 0.35f)
        TagColor.Green -> Color(0xFF66BB6A).copy(alpha = 0.25f)
        TagColor.Blue -> Color(0xFF42A5F5).copy(alpha = 0.25f)
        TagColor.Purple -> Color(0xFFAB47BC).copy(alpha = 0.25f)
        TagColor.Pink -> Color(0xFFF06292).copy(alpha = 0.25f)
        TagColor.Red -> Color(0xFFE57373).copy(alpha = 0.25f)
    }
}
