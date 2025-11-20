package com.dlx.smartalarm.demo

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val selectedView: DisplayStyle = DisplayStyle.Card
)
