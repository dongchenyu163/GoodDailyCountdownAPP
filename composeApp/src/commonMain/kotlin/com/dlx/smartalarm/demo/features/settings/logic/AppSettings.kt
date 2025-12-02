package com.dlx.smartalarm.demo.features.settings.logic

import com.dlx.smartalarm.demo.DisplayStyle
import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val selectedView: DisplayStyle = DisplayStyle.Card,
    val language: String = "en" // Add language setting
)
