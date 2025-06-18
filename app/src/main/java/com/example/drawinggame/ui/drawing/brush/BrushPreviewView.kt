package com.example.drawinggame.ui.drawing.brush

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import com.example.drawinggame.ui.drawing.models.DrawingTool
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Custom view that displays a preview of the current brush settings.
 * Part of Phase 4.2: Basic Brush System implementation.
 */
class BrushPreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // The paint object for the brush preview
    private val previewPaint = Paint()
    private val backgroundPaint = Paint().apply {
        color = Color.LTGRAY
        style = Paint.Style.FILL
    }
    private val backgroundCheckeredPaint = Paint().apply {
        color = Color.DKGRAY
        style = Paint.Style.FILL
    }
    
    // Center point for the preview
    private val center = PointF()
    
    // Current tool being previewed
    private var currentTool = DrawingTool.PEN
    
    // Preview circle size (percentage of view size)
    private var previewSizePercent = 0.5f
    
    /**
     * Update the preview with the current brush settings
     */
    fun updatePreview(paint: Paint, tool: DrawingTool) {
        previewPaint.set(paint)
        currentTool = tool
        
        // Adjust preview size based on the tool
        previewSizePercent = when (tool) {
            DrawingTool.PEN -> 0.3f
            DrawingTool.BRUSH -> 0.5f
            DrawingTool.ERASER -> 0.7f
        }
        
        invalidate()
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        center.set(w / 2f, h / 2f)
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val size = min(width, height)
        val radius = size * previewSizePercent / 2
        
        // Draw background (checkered for eraser preview)
        if (currentTool == DrawingTool.ERASER) {
            drawCheckeredBackground(canvas)
        } else {
            canvas.drawCircle(center.x, center.y, radius + 5, backgroundPaint)
        }
        
        // Draw brush preview
        if (currentTool == DrawingTool.ERASER) {
            // For eraser, draw an outline circle
            val outlinePaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 2f
                color = Color.BLACK
            }
            canvas.drawCircle(center.x, center.y, radius, outlinePaint)
        } else {
            // For normal brushes, draw a filled circle with the brush color
            val displayPaint = Paint(previewPaint)
            displayPaint.style = Paint.Style.FILL
            canvas.drawCircle(center.x, center.y, radius, displayPaint)
        }
    }
    
    /**
     * Draw a checkered background pattern for eraser preview
     */
    private fun drawCheckeredBackground(canvas: Canvas) {
        val size = min(width, height)
        val radius = size * previewSizePercent / 2
        val tileSize = radius / 3
        
        // Draw base gray background
        canvas.drawCircle(center.x, center.y, radius + 5, backgroundPaint)
        
        // Draw checkered pattern inside
        val left = center.x - radius
        val top = center.y - radius
        val right = center.x + radius
        val bottom = center.y + radius
        
        var x = left
        while (x < right) {
            var y = top
            while (y < bottom) {
                // Only draw if point is within the circle
                val distX = center.x - (x + tileSize/2)
                val distY = center.y - (y + tileSize/2)
                val distance = sqrt(distX * distX + distY * distY)
                
                if (distance <= radius) {
                    // Draw checkered pattern
                    if ((x.toInt() / tileSize.toInt() + y.toInt() / tileSize.toInt()) % 2 == 0) {
                        canvas.drawRect(x, y, x + tileSize, y + tileSize, backgroundCheckeredPaint)
                    }
                }
                
                y += tileSize
            }
            x += tileSize
        }
    }
} 