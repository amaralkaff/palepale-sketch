package com.example.drawinggame.ui.drawing.advanced.brushes.factories

import android.graphics.Paint
import android.graphics.BlurMaskFilter
import com.example.drawinggame.ui.drawing.advanced.brushes.AdvancedBrushSettings

/**
 * Factory for creating marker brush effects
 * Implements bold, consistent strokes with slight transparency
 */
class MarkerBrushFactory : BaseAdvancedBrushFactory() {
    
    override fun createPaint(settings: AdvancedBrushSettings): Paint {
        val paint = createBasePaint(settings)
        
        // Marker specific properties
        paint.apply {
            // Semi-transparent for marker layering effect
            alpha = (settings.opacity * 0.8f).toInt().coerceIn(150, 220)
            
            // Wide, consistent stroke
            strokeWidth = settings.size * 2f
            
            // Flat caps for marker tips
            strokeCap = Paint.Cap.SQUARE
            strokeJoin = Paint.Join.MITER
            
            // Slight blur for soft edges
            maskFilter = BlurMaskFilter(0.5f, BlurMaskFilter.Blur.NORMAL)
            
            // Use multiply for color layering
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
     * Create highlighter variant with high transparency
     */
    fun createHighlighterPaint(settings: AdvancedBrushSettings): Paint {
        val paint = createPaint(settings)
        
        paint.apply {
            // Very transparent for highlighting
            alpha = (settings.opacity * 0.3f).toInt().coerceIn(40, 100)
            
            // Even wider stroke for highlighting
            strokeWidth = settings.size * 3f
            
            // Use screen blend mode for highlighting effect
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
    
    override fun createPreviewPaint(settings: AdvancedBrushSettings): Paint {
        val paint = super.createPreviewPaint(settings)
        
        // Remove blur for cleaner preview
        paint.maskFilter = null
        
        // Increase visibility
        paint.alpha = paint.alpha.coerceAtLeast(180)
        
        return paint
    }
}