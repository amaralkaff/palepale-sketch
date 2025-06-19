package com.example.drawinggame.ui.drawing.advanced.brushes.factories

import android.graphics.Paint
import com.example.drawinggame.ui.drawing.advanced.brushes.AdvancedBrushSettings
import com.example.drawinggame.ui.drawing.advanced.brushes.pressure.PressureSettings
import com.example.drawinggame.ui.drawing.advanced.textures.BrushTexture

/**
 * Factory interface for creating specialized Paint objects for advanced brushes
 */
interface AdvancedBrushFactory {
    /**
     * Create a Paint object configured for this brush type
     */
    fun createPaint(settings: AdvancedBrushSettings): Paint
    
    /**
     * Apply pressure sensitivity to the paint object
     */
    fun applyPressure(paint: Paint, pressure: Float, settings: PressureSettings)
    
    /**
     * Apply texture to the paint object
     */
    fun applyTexture(paint: Paint, texture: BrushTexture?)
    
    /**
     * Get a preview paint for UI display
     */
    fun createPreviewPaint(settings: AdvancedBrushSettings): Paint
}

/**
 * Base implementation providing common functionality
 */
abstract class BaseAdvancedBrushFactory : AdvancedBrushFactory {
    
    override fun applyPressure(paint: Paint, pressure: Float, settings: PressureSettings) {
        if (settings.sizeEnabled) {
            val pressureSize = settings.sizeMin + (settings.sizeMax - settings.sizeMin) * pressure
            paint.strokeWidth = paint.strokeWidth * pressureSize
        }
        
        if (settings.opacityEnabled) {
            val pressureOpacity = settings.opacityMin + (settings.opacityMax - settings.opacityMin) * pressure
            paint.alpha = (paint.alpha * pressureOpacity).toInt().coerceIn(0, 255)
        }
    }
    
    override fun applyTexture(paint: Paint, texture: BrushTexture?) {
        texture?.let {
            // Apply texture as a shader or path effect
            // This is a simplified implementation - full texture support would require
            // more complex shader or custom drawing operations
            paint.alpha = (paint.alpha * it.intensity).toInt().coerceIn(0, 255)
        }
    }
    
    override fun createPreviewPaint(settings: AdvancedBrushSettings): Paint {
        val paint = createPaint(settings)
        // Adjust for preview display
        paint.strokeWidth = paint.strokeWidth.coerceAtMost(50f)
        return paint
    }
    
    /**
     * Helper method to create base paint with common properties
     */
    protected fun createBasePaint(settings: AdvancedBrushSettings): Paint {
        return Paint().apply {
            isAntiAlias = true
            isDither = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = settings.size
            color = settings.color
            alpha = settings.opacity
        }
    }
}