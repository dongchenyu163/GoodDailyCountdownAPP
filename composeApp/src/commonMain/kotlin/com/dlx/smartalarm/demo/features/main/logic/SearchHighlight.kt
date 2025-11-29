package com.dlx.smartalarm.demo.features.main.logic

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme

@Composable
fun highlight(text: String, tokens: List<String>): AnnotatedString {
    if (tokens.isEmpty()) return AnnotatedString(text)
    val lower = text.lowercase()
    return buildAnnotatedString {
        append(text)
        tokens.forEach { raw ->
            val key = raw.lowercase()
            var start = 0
            while (true) {
                val idx = lower.indexOf(key, startIndex = start)
                if (idx < 0) break
                addStyle(
                    SpanStyle(color = MaterialTheme.colorScheme.primary),
                    idx,
                    idx + key.length
                )
                start = idx + key.length
            }
        }
    }
}
