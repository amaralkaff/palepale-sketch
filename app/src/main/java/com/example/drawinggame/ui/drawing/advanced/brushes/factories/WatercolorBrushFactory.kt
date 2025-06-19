package com.example.drawinggame.ui.drawing.advanced.brushes.factories

import android.graphics.BlurMaskFilter
import android.graphics.Paint
import android.graphics.PathEffect
import android.graphics.DashPathEffect
import com.example.drawinggame.ui.drawing.advanced.brushes.AdvancedBrushSettings
import com.example.drawinggame.ui.drawing.advanced.blending.BlendMode

/**
 * Factory for creating watercolor brush effects
 * Implements wet-on-wet blending and transparent, flowing strokes
 */
class WatercolorBrushFactory : BaseAdvancedBrushFactory() {
    
    override fun createPaint(settings: AdvancedBrushSettings): Paint {
        val paint = createBasePaint(settings)
        
        // Watercolor-specific properties
        paint.apply {
            // Reduce opacity for more transparent, layered effect
            alpha = (settings.opacity * 0.6f).toInt().coerceIn(50, 200)
            
            // Add slight blur for soft edges
            maskFilter = BlurMaskFilter(2f, BlurMaskFilter.Blur.NORMAL)
            
            // Set blend mode for color mixing
            when (settings.blendMode) {
                BlendMode.NORMAL -> {
                    // Use multiply for natural color mixing
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        blendMode = android.graphics.BlendMode.MULTIPLY
                    } else {
                        xfermode = android.graphics.PorterDuffXfermode(
                            android.graphics.PorterDuff.Mode.MULTIPLY
                        )
                    }
                }
                else -> {
                    // Apply the specified blend mode
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        blendMode = settings.blendMode.toAndroidBlendMode()
                    } else {
                        xfermode = android.graphics.PorterDuffXfermode(
                            settings.blendMode.toPorterDuffMode()
                        )
                    }
                }
            }
            
            // Increase stroke width slightly for watercolor flow
            strokeWidth = settings.size * 1.2f
            
            // Add subtle texture variation
            pathEffect = createWatercolorPathEffect()
        }
        
        return paint
    }
    
    /**
     * Creates a subtle path effect for watercolor texture
     */
    private fun createWatercolorPathEffect(): PathEffect {
        // Create a subtle dash effect for texture variation
        val intervals = floatArrayOf(1f, 0.5f)
        return DashPathEffect(intervals, 0f)
    }
    
    /**
     * Apply watercolor-specific wet-on-wet blending effect
     */
    fun applyWetOnWetEffect(paint: Paint, wetness: Float) {
        // Increase blur and reduce opacity for wet areas
        val blurRadius = 2f + (wetness * 3f)
        paint.maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
        
        // Reduce opacity in wet areas for bleeding effect
        val currentAlpha = paint.alpha
        paint.alpha = (currentAlpha * (1f - wetness * 0.3f)).toInt().coerceIn(30, 255)
    }
    
    /**
     * Create a watercolor wash effect with multiple transparency layers
     */
    fun createWashEffect(settings: AdvancedBrushSettings): List<Paint> {
        val paints = mutableListOf<Paint>()
        
        // Create multiple paint layers for wash effect
        for (i in 0 until 3) {
            val paint = createPaint(settings)
            paint.apply {
                // Each layer has different opacity and size
                alpha = (alpha * (0.3f + i * 0.2f)).toInt()
                strokeWidth = strokeWidth * (1f + i * 0.1f)
            }
            paints.add(paint)
        }
        
        return paints
    }
    
    override fun createPreviewPaint(settings: AdvancedBrushSettings): Paint {
        val paint = super.createPreviewPaint(settings)
        
        // Remove blur for cleaner preview
        paint.maskFilter = null
        
        // Slightly increase opacity for better visibility in preview
        paint.alpha = (paint.alpha * 1.5f).toInt().coerceIn(100, 255)
        
        return paint
    }
}