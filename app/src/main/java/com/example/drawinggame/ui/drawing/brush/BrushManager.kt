package com.example.drawinggame.ui.drawing.brush

import android.graphics.Paint
import com.example.drawinggame.ui.drawing.contracts.DrawingConstants
import com.example.drawinggame.ui.drawing.models.DrawingTool

/**
 * Manages brush configuration and creation for different drawing tools.
 * Part of Phase 4.2: Basic Brush System implementation.
 */
class BrushManager {
    
    // Brush property defaults
    private var currentSize = DrawingConstants.DEFAULT_BRUSH_SIZE
    private var currentColor = DrawingConstants.DEFAULT_COLOR
    private var currentOpacity = DrawingConstants.DEFAULT_OPACITY
    private var currentTool = DrawingTool.PEN
    
    /**
     * Set the current brush size
     */
    fun setBrushSize(size: Float) {
        currentSize = size.coerceIn(DrawingConstants.MIN_BRUSH_SIZE, DrawingConstants.MAX_BRUSH_SIZE)
    }
    
    /**
     * Set the current brush color
     */
    fun setBrushColor(color: Int) {
        currentColor = color
    }
    
    /**
     * Set the current brush opacity (0-255)
     */
    fun setBrushOpacity(opacity: Int) {
        currentOpacity = opacity.coerceIn(0, 255)
    }
    
    /**
     * Set the current drawing tool
     */
    fun setTool(tool: DrawingTool) {
        currentTool = tool
    }
    
    /**
     * Get a configured Paint object for the current brush settings
     */
    fun getCurrentPaint(): Paint {
        return when (currentTool) {
            DrawingTool.PEN -> getPenPaint()
            DrawingTool.BRUSH -> getBrushPaint()
            DrawingTool.ERASER -> getEraserPaint()
        }
    }
    
    /**
     * Create a Paint object configured for the pen tool
     */
    private fun getPenPaint(): Paint {
        return Paint().apply {
            isAntiAlias = true
            isDither = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = currentSize
            color = currentColor
            alpha = currentOpacity
        }
    }
    
    /**
     * Create a Paint object configured for the brush tool
     */
    private fun getBrushPaint(): Paint {
        return Paint().apply {
            isAntiAlias = true
            isDither = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = currentSize * 1.5f // Brush is thicker than pen
            color = currentColor
            alpha = currentOpacity
            // Optional: Add blur mask filter or path effect for softer brush
        }
    }
    
    /**
     * Create a Paint object configured for the eraser tool
     */
    private fun getEraserPaint(): Paint {
        return Paint().apply {
            isAntiAlias = true
            isDither = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = currentSize * 2f // Eraser is larger than standard brushes
            color = DrawingConstants.ERASER_COLOR
            alpha = 255 // Full opacity for eraser
        }
    }
    
    /**
     * Get the current brush size
     */
    fun getCurrentSize(): Float = currentSize
    
    /**
     * Get the current color
     */
    fun getCurrentColor(): Int = currentColor
    
    /**
     * Get the current opacity
     */
    fun getCurrentOpacity(): Int = currentOpacity
    
    /**
     * Get the current tool
     */
    fun getCurrentTool(): DrawingTool = currentTool
    
    /**
     * Get a paint object for previewing the current brush
     */
    fun getPreviewPaint(): Paint {
        val paint = getCurrentPaint()
        // Make preview paint suitable for display
        if (currentTool == DrawingTool.ERASER) {
            // For eraser preview, use a hollow stroke with border color
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            paint.color = 0xFF666666.toInt()
        }
        return paint
    }
    
    companion object {
        // Singleton instance
        private var instance: BrushManager? = null
        
        @JvmStatic
        fun getInstance(): BrushManager {
            if (instance == null) {
                instance = BrushManager()
            }
            return instance!!
        }
    }
} 