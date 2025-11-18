package com.dlx.smartalarm.demo

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.max

enum class TitleImageViewType(val key: String) {
    List("List"),
    Grid("Grid"),
    Card("Card");

    companion object {
        val all = values().toList()
        fun fromKey(key: String) = all.firstOrNull { it.key == key }
    }
}

@Serializable
data class TitleImageDisplayParameters(
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val aspectRatio: Float = 0f
)

@Serializable
data class TitleImageInfo(
    val uuid: String = "",
    val displayInfo: Map<String, TitleImageDisplayParameters> = emptyMap()
) {
    fun paramsFor(viewType: TitleImageViewType): TitleImageDisplayParameters {
        return displayInfo[viewType.key] ?: TitleImageDisplayParameters()
    }

    fun update(viewType: TitleImageViewType, params: TitleImageDisplayParameters): TitleImageInfo {
        val next = displayInfo.toMutableMap()
        next[viewType.key] = params
        return copy(displayInfo = next.toMap())
    }
}

data class TitleImageGradientSpec(
    val stops: Map<Float, Float>
) {
    private val normalizedStops = stops
        .entries
        .sortedBy { it.key }
        .map { entry -> entry.key.coerceIn(0f, 1f) to entry.value.coerceIn(0f, 1f) }
        .ifEmpty { listOf(0f to 0f, 1f to 1f) }

    fun opacityAt(position: Float): Float {
        val clamped = position.coerceIn(0f, 1f)
        val lower = normalizedStops.lastOrNull { it.first <= clamped }
        val upper = normalizedStops.firstOrNull { it.first >= clamped }
        if (lower == null || upper == null) return normalizedStops.last().second
        if (abs(upper.first - lower.first) < 0.0001f) return upper.second
        val progress = (clamped - lower.first) / (upper.first - lower.first)
        val eased = progress * progress
        return lower.second + (upper.second - lower.second) * eased
    }

    fun asBrush(
        orientation: GradientOrientation,
        color: Color,
        samples: Int = 24
    ): Brush {
        val clampedSamples = max(2, samples)
        val colorStops = Array(clampedSamples + 1) { index ->
            val position = index / clampedSamples.toFloat()
            val opacity = opacityAt(position)
            val resolved = color.copy(alpha = opacity)
            position to resolved
        }
        return when (orientation) {
            GradientOrientation.Horizontal -> Brush.horizontalGradient(
                colorStops = colorStops,
                tileMode = TileMode.Clamp
            )
            GradientOrientation.Vertical -> Brush.verticalGradient(
                colorStops = colorStops,
                tileMode = TileMode.Clamp
            )
        }
    }
}

enum class GradientOrientation { Horizontal, Vertical }

val DefaultListImageGradient = TitleImageGradientSpec(
    stops = linkedMapOf(
        0f to 0f,
        0.3f to 0f,
        0.5f to 1f,
        1f to 1f
    )
)

val DefaultGridImageGradient = TitleImageGradientSpec(
    stops = linkedMapOf(
        0f to 1f,
        0.7f to 1f,
        0.8f to 0f,
        1f to 0f
    )
)


const val GridPreviewAspectRatio = 1.0f
const val CardPreviewMinAspectRatio = 1.2f
const val CardPreviewMaxAspectRatio = 2.6f

fun defaultDisplayInfo(initialAspectRatio: Float): Map<String, TitleImageDisplayParameters> {
    return TitleImageViewType.all.associate { view ->
        val params = if (view == TitleImageViewType.Card && initialAspectRatio > 0f) {
            TitleImageDisplayParameters(aspectRatio = initialAspectRatio)
        } else {
            TitleImageDisplayParameters()
        }
        view.key to params
    }
}
