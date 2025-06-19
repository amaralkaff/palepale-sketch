package com.example.drawinggame.ui.drawing.layers.effects

import android.graphics.Bitmap
import android.graphics.Color

/**
 * Interface for layer effects that can be applied to layers
 * Phase 5.2: Layer System - Layer Effects
 */
interface LayerEffect {
    val id: String
    val name: String
    val enabled: Boolean
    
    /**
     * Apply the effect to a bitmap
     */
    fun apply(source: Bitmap): Bitmap
    
    /**
     * Get effect settings as a map
     */
    fun getSettings(): Map<String, Any>
    
    /**
     * Set effect settings from a map
     */
    fun setSettings(settings: Map<String, Any>)
    
    /**
     * Clean up resources
     */
    fun cleanup() {}
}

/**
 * Drop shadow effect implementation
 */
data class DropShadowEffect(
    override val id: String = "drop_shadow",
    override val name: String = "Drop Shadow",
    override val enabled: Boolean = true,
    val offsetX: Float = 4f,
    val offsetY: Float = 4f,
    val blurRadius: Float = 8f,
    val color: Int = Color.BLACK,
    val opacity: Float = 0.75f
) : LayerEffect {
    
    override fun apply(source: Bitmap): Bitmap {
        // TODO: Implement drop shadow rendering
        // For now, return the original bitmap
        return source
    }
    
    override fun getSettings(): Map<String, Any> {
        return mapOf(
            "offsetX" to offsetX,
            "offsetY" to offsetY,
            "blurRadius" to blurRadius,
            "color" to color,
            "opacity" to opacity
        )
    }
    
    override fun setSettings(settings: Map<String, Any>) {
        // TODO: Implement settings update
    }
}

/**
 * Glow effect implementation
 */
data class GlowEffect(
    override val id: String = "glow",
    override val name: String = "Glow",
    override val enabled: Boolean = true,
    val radius: Float = 8f,
    val color: Int = Color.WHITE,
    val opacity: Float = 0.8f,
    val inner: Boolean = false
) : LayerEffect {
    
    override fun apply(source: Bitmap): Bitmap {
        // TODO: Implement glow rendering
        return source
    }
    
    override fun getSettings(): Map<String, Any> {
        return mapOf(
            "radius" to radius,
            "color" to color,
            "opacity" to opacity,
            "inner" to inner
        )
    }
    
    override fun setSettings(settings: Map<String, Any>) {
        // TODO: Implement settings update
    }
}

/**
 * Stroke effect implementation
 */
data class StrokeEffect(
    override val id: String = "stroke",
    override val name: String = "Stroke",
    override val enabled: Boolean = true,
    val width: Float = 2f,
    val color: Int = Color.BLACK,
    val position: StrokePosition = StrokePosition.OUTSIDE
) : LayerEffect {
    
    override fun apply(source: Bitmap): Bitmap {
        // TODO: Implement stroke rendering
        return source
    }
    
    override fun getSettings(): Map<String, Any> {
        return mapOf(
            "width" to width,
            "color" to color,
            "position" to position.name
        )
    }
    
    override fun setSettings(settings: Map<String, Any>) {
        // TODO: Implement settings update
    }
}

/**
 * Stroke position options
 */
enum class StrokePosition {
    INSIDE,
    CENTER,
    OUTSIDE
} 