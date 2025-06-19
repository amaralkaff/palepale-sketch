package com.example.drawinggame.ui.drawing.advanced.brushes.factories

import android.graphics.Paint
import android.graphics.PathEffect
import android.graphics.CornerPathEffect
import android.graphics.ComposePathEffect
import android.graphics.DiscretePathEffect
import com.example.drawinggame.ui.drawing.advanced.brushes.AdvancedBrushSettings

/**
 * Factory for creating oil paint brush effects
 * Implements heavy brushstrokes with impasto effects and paint mixing
 */
class OilPaintBrushFactory : BaseAdvancedBrushFactory() {
    
    override fun createPaint(settings: AdvancedBrushSettings): Paint {
        val paint = createBasePaint(settings)
        
        // Oil paint specific properties
        paint.apply {
            // Full opacity for heavy paint effect
            alpha = settings.opacity.coerceIn(180, 255)
            
            // Thicker strokes for impasto effect
            strokeWidth = settings.size * 1.8f
            
            // Square stroke caps for palette knife effect
            strokeCap = Paint.Cap.SQUARE
            strokeJoin = Paint.Join.BEVEL
            
            // Add texture and roughness
            pathEffect = createImpastoPathEffect()
            
            // Apply blend mode for paint mixing
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                blendMode = settings.blendMode.toAndroidBlendMode()
            } else {
                xfermode = android.graphics.PorterDuffXfermode(
                    settings.blendMode.toPorterDuffMode()
                )
            }
        }
        
        return paint
    }
    
    /**
     * Creates a path effect that simulates impasto texture
     */
    private fun createImpastoPathEffect(): PathEffect {
        // Create rough, textured edges
        val discrete = DiscretePathEffect(2f, 1f)
        val corner = CornerPathEffect(3f)
        
        // Combine effects for complex texture
        return ComposePathEffect(discrete, corner)
    }
    
    /**
     * Apply heavy brush stroke effect with paint buildup
     */
    fun applyImpastoEffect(paint: Paint, thickness: Float) {
        // Increase stroke width for paint buildup
        paint.strokeWidth = paint.strokeWidth * (1f + thickness * 0.5f)
        
        // Adjust opacity for paint density
        val densityAlpha = (paint.alpha * (0.8f + thickness * 0.2f)).toInt()
        paint.alpha = densityAlpha.coerceIn(150, 255)
    }
    
    /**
     * Create palette knife effect with angular strokes
     */
    fun createPaletteKnifeEffect(settings: AdvancedBrushSettings): Paint {
        val paint = createPaint(settings)
        
        paint.apply {
            // Sharp, angular strokes
            strokeCap = Paint.Cap.SQUARE
            strokeJoin = Paint.Join.MITER
            strokeMiter = 10f
            
            // Remove texture for clean knife strokes
            pathEffect = null
            
            // Increase width for knife blade effect
            strokeWidth = settings.size * 2.5f
        }
        
        return paint
    }
    
    /**
     * Apply color mixing effect for wet paint blending
     */
    fun applyColorMixing(paint: Paint, mixingIntensity: Float) {
        // Use soft light blend mode for color mixing
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            paint.blendMode = android.graphics.BlendMode.SOFT_LIGHT
        } else {
            paint.xfermode = android.graphics.PorterDuffXfermode(
                android.graphics.PorterDuff.Mode.OVERLAY
            )
        }
        
        // Adjust opacity for mixing strength
        paint.alpha = (paint.alpha * mixingIntensity).toInt().coerceIn(100, 255)
    }
    
    override fun createPreviewPaint(settings: AdvancedBrushSettings): Paint {
        val paint = super.createPreviewPaint(settings)
        
        // Simplify path effect for preview
        paint.pathEffect = CornerPathEffect(2f)
        
        // Ensure good visibility
        paint.alpha = paint.alpha.coerceAtLeast(150)
        
        return paint
    }
}