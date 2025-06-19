package com.example.drawinggame.ui.drawing.advanced.brushes.factories

import android.graphics.Paint
import android.graphics.BlurMaskFilter
import android.graphics.PathEffect
import android.graphics.DashPathEffect
import com.example.drawinggame.ui.drawing.advanced.brushes.AdvancedBrushSettings

/**
 * Factory for creating spray paint brush effects
 * Implements airbrush effects with adjustable spray pattern and density
 */
class SprayPaintBrushFactory : BaseAdvancedBrushFactory() {
    
    override fun createPaint(settings: AdvancedBrushSettings): Paint {
        val paint = createBasePaint(settings)
        
        // Spray paint specific properties
        paint.apply {
            // Semi-transparent for spray buildup effect
            alpha = (settings.opacity * 0.4f).toInt().coerceInRange(30, 150)
            
            // Larger, softer brush area
            strokeWidth = settings.size * 3f
            
            // Soft edges for spray effect
            maskFilter = BlurMaskFilter(settings.size * 0.3f, BlurMaskFilter.Blur.NORMAL)
            
            // Round caps for smooth spray
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            
            // Add spray pattern texture
            pathEffect = createSprayPattern(settings.size)
        }
        
        return paint
    }
    
    /**
     * Creates a spray pattern effect using dashed paths
     */
    private fun createSprayPattern(size: Float): PathEffect {
        // Create irregular dash pattern for spray texture
        val sprayDensity = (size * 0.1f).coerceIn(0.5f, 2f)
        val intervals = floatArrayOf(sprayDensity, sprayDensity * 0.5f, sprayDensity * 0.3f, sprayDensity)
        return DashPathEffect(intervals, 0f)
    }
    
    /**
     * Apply spray density effect
     */
    fun applySprayDensity(paint: Paint, density: Float) {
        // Adjust opacity based on spray density
        val baseAlpha = paint.alpha
        paint.alpha = (baseAlpha * density).toInt().coerceIn(20, 200)
        
        // Adjust blur based on density
        val blurRadius = (3f - density * 2f).coerceAtLeast(0.5f)
        paint.maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
    }
    
    /**
     * Create multiple paint layers for realistic spray buildup
     */
    fun createSprayLayers(settings: AdvancedBrushSettings, layerCount: Int = 3): List<Paint> {
        val paints = mutableListOf<Paint>()
        
        for (i in 0 until layerCount) {
            val paint = createPaint(settings)
            paint.apply {
                // Each layer has different properties for depth
                alpha = (alpha * (0.3f + i * 0.2f)).toInt()
                strokeWidth = strokeWidth * (0.8f + i * 0.1f)
                
                // Vary blur for each layer
                val blurRadius = (1f + i * 0.5f)
                maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
            }
            paints.add(paint)
        }
        
        return paints
    }
    
    /**
     * Apply airbrush pressure effect
     */
    fun applyAirbrushPressure(paint: Paint, pressure: Float) {
        // Higher pressure means more concentrated spray
        val concentrationFactor = 0.5f + pressure * 0.5f
        
        // Adjust stroke width based on pressure
        paint.strokeWidth = paint.strokeWidth * concentrationFactor
        
        // Adjust blur - less blur with higher pressure
        val blurRadius = (4f - pressure * 2f).coerceAtLeast(0.5f)
        paint.maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
        
        // Increase opacity with pressure
        paint.alpha = (paint.alpha * (0.5f + pressure * 0.5f)).toInt().coerceIn(30, 200)
    }
    
    override fun createPreviewPaint(settings: AdvancedBrushSettings): Paint {
        val paint = super.createPreviewPaint(settings)
        
        // Reduce blur for cleaner preview
        paint.maskFilter = BlurMaskFilter(1f, BlurMaskFilter.Blur.NORMAL)
        
        // Increase visibility
        paint.alpha = paint.alpha.coerceAtLeast(100)
        
        return paint
    }
}

// Extension function for coercing int values in range
private fun Int.coerceInRange(min: Int, max: Int): Int = this.coerceIn(min, max)