package com.example.drawinggame.ui.drawing.advanced.brushes.factories

import android.graphics.Paint
import android.graphics.PathEffect
import android.graphics.DiscretePathEffect
import android.graphics.ComposePathEffect
import android.graphics.CornerPathEffect
import com.example.drawinggame.ui.drawing.advanced.brushes.AdvancedBrushSettings

/**
 * Factory for creating charcoal brush effects
 * Implements textured strokes with granular appearance and pressure sensitivity
 */
class CharcoalBrushFactory : BaseAdvancedBrushFactory() {
    
    override fun createPaint(settings: AdvancedBrushSettings): Paint {
        val paint = createBasePaint(settings)
        
        // Charcoal specific properties
        paint.apply {
            // Variable opacity for natural charcoal effect
            alpha = (settings.opacity * 0.8f).toInt().coerceIn(100, 220)
            
            // Slightly larger stroke for charcoal texture
            strokeWidth = settings.size * 1.3f
            
            // Rounded caps for natural drawing feel
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            
            // Add charcoal texture effect
            pathEffect = createCharcoalTexture(settings.size)
            
            // Use multiply blend mode for natural darkening
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
     * Creates a textured path effect for charcoal granulation
     */
    private fun createCharcoalTexture(size: Float): PathEffect {
        // Create granular texture based on brush size
        val deviation = (size * 0.1f).coerceIn(0.5f, 3f)
        val segmentLength = (size * 0.2f).coerceIn(1f, 5f)
        
        val discrete = DiscretePathEffect(segmentLength, deviation)
        val corner = CornerPathEffect(1f)
        
        return ComposePathEffect(discrete, corner)
    }
    
    /**
     * Apply charcoal pressure effect - lighter strokes with less pressure
     */
    fun applyCharcoalPressure(paint: Paint, pressure: Float) {
        // Charcoal gets darker with more pressure
        val pressureAlpha = (50 + pressure * 170).toInt()
        paint.alpha = pressureAlpha.coerceIn(50, 220)
        
        // Stroke width varies with pressure
        val baseFactor = 0.5f + pressure * 0.5f
        paint.strokeWidth = paint.strokeWidth * baseFactor
        
        // Texture intensity varies with pressure
        val deviation = (pressure * 2f).coerceAtLeast(0.5f)
        paint.pathEffect = DiscretePathEffect(2f, deviation)
    }
    
    /**
     * Create side stroke effect for broad charcoal shading
     */
    fun createSideStrokeEffect(settings: AdvancedBrushSettings): Paint {
        val paint = createPaint(settings)
        
        paint.apply {
            // Much wider stroke for side drawing
            strokeWidth = settings.size * 4f
            
            // Lower opacity for shading
            alpha = (alpha * 0.6f).toInt()
            
            // Square caps for edge shading
            strokeCap = Paint.Cap.SQUARE
            
            // Reduce texture for smoother shading
            pathEffect = DiscretePathEffect(5f, 1f)
        }
        
        return paint
    }
    
    /**
     * Apply paper texture interaction
     */
    fun applyPaperTexture(paint: Paint, paperRoughness: Float) {
        // Increase texture variation based on paper roughness
        val textureDeviation = (1f + paperRoughness * 2f)
        val segmentLength = (2f + paperRoughness)
        
        paint.pathEffect = DiscretePathEffect(segmentLength, textureDeviation)
        
        // Adjust opacity - rougher paper holds less charcoal
        paint.alpha = (paint.alpha * (1f - paperRoughness * 0.2f)).toInt().coerceIn(50, 255)
    }
    
    override fun createPreviewPaint(settings: AdvancedBrushSettings): Paint {
        val paint = super.createPreviewPaint(settings)
        
        // Reduce texture for cleaner preview
        paint.pathEffect = DiscretePathEffect(1f, 0.5f)
        
        // Ensure good visibility
        paint.alpha = paint.alpha.coerceAtLeast(120)
        
        return paint
    }
}