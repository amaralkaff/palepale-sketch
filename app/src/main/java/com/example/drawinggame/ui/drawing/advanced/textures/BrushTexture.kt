package com.example.drawinggame.ui.drawing.advanced.textures

import android.graphics.Bitmap

/**
 * Represents a texture that can be applied to brush strokes
 */
data class BrushTexture(
    val id: String,
    val name: String,
    val bitmap: Bitmap,
    val type: TextureType,
    val scale: Float = 1.0f,
    val rotation: Float = 0.0f,
    val intensity: Float = 1.0f
)

/**
 * Built-in texture types for brushes
 */
enum class TextureType {
    PAPER_SMOOTH,
    PAPER_ROUGH,
    CANVAS_FINE,
    CANVAS_COARSE,
    WATERCOLOR_PAPER,
    CHARCOAL_PAPER,
    CUSTOM
}