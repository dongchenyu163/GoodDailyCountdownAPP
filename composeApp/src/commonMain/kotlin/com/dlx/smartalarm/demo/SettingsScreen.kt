package com.dlx.smartalarm.demo

import com.dlx.smartalarm.demo.MR
import dev.icerock.moko.resources.compose.stringResource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState // New import
import androidx.compose.foundation.verticalScroll // New import

// Import VerticalScrollbar
import com.dlx.smartalarm.demo.components.scroll.VerticalScrollbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    useCloud: Boolean,
    displayStyle: DisplayStyle,
    currentLanguage: String,
    onBack: () -> Unit,
    onToggleCloud: (Boolean) -> Unit,
    onChangeDisplay: (DisplayStyle) -> Unit,
    onLanguageChange: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(MR.strings.settings)) }, 
                navigationIcon = { TextButton(onClick = onBack) { Text("←") } }
            )
        }
    ) { padding ->
        val scrollState = rememberScrollState() 
        Box(modifier = Modifier.fillMaxSize()) { 
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(scrollState), 
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                Text(stringResource(MR.strings.countdown_display_style), style = MaterialTheme.typography.titleMedium)
                
                DisplayStyleItem(
                    title = stringResource(MR.strings.display_style_grid),
                    subtitle = stringResource(MR.strings.display_style_grid_description),
                    selected = displayStyle == DisplayStyle.Grid,
                    onClick = { onChangeDisplay(DisplayStyle.Grid) }
                )
                DisplayStyleItem(
                    title = stringResource(MR.strings.display_style_list),
                    subtitle = stringResource(MR.strings.display_style_list_description),
                    selected = displayStyle == DisplayStyle.List,
                    onClick = { onChangeDisplay(DisplayStyle.List) }
                )
                DisplayStyleItem(
                    title = stringResource(MR.strings.display_style_card),
                    subtitle = stringResource(MR.strings.display_style_card_description),
                    selected = displayStyle == DisplayStyle.Card,
                    onClick = { onChangeDisplay(DisplayStyle.Card) }
                )

                HorizontalDivider()

                Text(stringResource(MR.strings.language), style = MaterialTheme.typography.titleMedium)
                LanguageItem(
                    title = "中文",
                    selected = currentLanguage == "zh",
                    onClick = { onLanguageChange("zh") }
                )
                LanguageItem(
                    title = "English",
                    selected = currentLanguage == "en",
                    onClick = { onLanguageChange("en") }
                )
                LanguageItem(
                    title = "日本語",
                    selected = currentLanguage == "ja",
                    onClick = { onLanguageChange("ja") }
                )
            }
            VerticalScrollbar( 
                scrollState = scrollState,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
private fun DisplayStyleItem(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        tonalElevation = if (selected) 4.dp else 0.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick) // Moved onClick here
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp), // Reduced vertical padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            RadioButton(selected = selected, onClick = null) // onClick set to null to disable its own click handling
        }
    }
    Spacer(Modifier.height(4.dp)) // Reduced Spacer height
}

@Composable
private fun LanguageItem(title: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        tonalElevation = if (selected) 4.dp else 0.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, modifier = Modifier.weight(1f))
            RadioButton(selected = selected, onClick = null)
        }
    }
    Spacer(Modifier.height(4.dp))
}
