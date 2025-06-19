package com.example.drawinggame.ui.drawing.advanced.brushes.factories

import android.graphics.Paint
import android.graphics.PathEffect
import android.graphics.DiscretePathEffect
import com.example.drawinggame.ui.drawing.advanced.brushes.AdvancedBrushSettings

/**
 * Factory for creating pencil brush effects
 * Implements pressure-sensitive drawing with graphite-like texture
 */
class PencilBrushFactory : BaseAdvancedBrushFactory() {
    
    override fun createPaint(settings: AdvancedBrushSettings): Paint {
        val paint = createBasePaint(settings)
        
        // Pencil specific properties
        paint.apply {
            // Variable opacity based on pressure (will be applied separately)
            alpha = (settings.opacity * 0.9f).toInt().coerceIn(80, 240)
            
            // Precise stroke width for pencil
            strokeWidth = settings.size * 0.8f
            
            // Sharp caps for pencil precision
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            
            // Add subtle texture for graphite effect
            pathEffect = createGraphiteTexture(settings.size)
            
            // Use multiply blend mode for pencil shading
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                blendMode = android.graphics.BlendMode.MULTIPLY
            } else {
                xfermode = android.graphics.PorterDuffXfermode(
                    android.graphics.PorterDuff.Mode.MULTIPLY
                )
            }
        }
        
        return paint
    }
    
    /**
     * Creates a subtle path effect for graphite texture
     */
    private fun createGraphiteTexture(size: Float): PathEffect {
        // Very subtle texture for smooth pencil strokes
        val deviation = (size * 0.05f).coerceIn(0.1f, 1f)
        return DiscretePathEffect(1f, deviation)
    }
    
    /**
     * Apply pencil pressure effect - lighter strokes with less pressure
     */
    fun applyPencilPressure(paint: Paint, pressure: Float) {
        // Pencil gets darker and thicker with more pressure
        val pressureAlpha = (80 + pressure * 160).toInt()
        paint.alpha = pressureAlpha.coerceIn(80, 240)
        
        // Stroke width varies significantly with pressure
        val baseFactor = 0.3f + pressure * 0.7f
        paint.strokeWidth = paint.strokeWidth * baseFactor
    }
    
    override fun createPreviewPaint(settings: AdvancedBrushSettings): Paint {
        val paint = super.createPreviewPaint(settings)
        
        // Remove texture for cleaner preview
        paint.pathEffect = null
        
        // Ensure good visibility
        paint.alpha = paint.alpha.coerceAtLeast(150)
        
        return paint
    }
}