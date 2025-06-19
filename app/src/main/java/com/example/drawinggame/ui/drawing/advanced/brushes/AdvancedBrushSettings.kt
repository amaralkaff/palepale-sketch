package com.example.drawinggame.ui.drawing.advanced.brushes

import com.example.drawinggame.ui.drawing.advanced.brushes.pressure.PressureSettings
import com.example.drawinggame.ui.drawing.advanced.textures.BrushTexture
import com.example.drawinggame.ui.drawing.advanced.blending.BlendMode

/**
 * Comprehensive settings for advanced brush functionality
 */
data class AdvancedBrushSettings(
    val type: AdvancedBrushType,
    val size: Float,
    val color: Int,
    val opacity: Int,
    val pressureSettings: PressureSettings,
    val texture: BrushTexture?,
    val blendMode: BlendMode,
    val customProperties: Map<String, Any> = emptyMap()
)

/**
 * Brush-specific effect properties
 */
data class BrushEffect(
    val type: BrushEffectType,
    val intensity: Float,
    val enabled: Boolean = true
)

/**
 * Available brush effect types
 */
enum class BrushEffectType {
    WET_ON_WET,
    IMPASTO,
    GRANULATION,
    BLEEDING,
    TEXTURE_OVERLAY,
    SCATTER,
    JITTER
}