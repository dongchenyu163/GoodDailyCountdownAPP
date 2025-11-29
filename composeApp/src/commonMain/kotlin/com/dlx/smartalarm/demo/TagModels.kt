package com.dlx.smartalarm.demo

import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val id: String,
    val name: String,
    val color: TagColor = TagColor.Default
)

enum class TagColor {
    Default, Gray, Brown, Orange, Yellow, Green, Blue, Purple, Pink, Red
}

