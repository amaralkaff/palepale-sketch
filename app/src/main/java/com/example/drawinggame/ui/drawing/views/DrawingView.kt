package com.example.drawinggame.ui.drawing.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.example.drawinggame.ui.drawing.DrawingEngine
import com.example.drawinggame.ui.drawing.models.CanvasTransform
import com.example.drawinggame.ui.drawing.models.DrawingTool
import com.example.drawinggame.ui.drawing.touch.TouchEventProcessor

/**
 * Custom view for drawing operations.
 * Handles rendering and integrates with the touch handling system.
 */
class DrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    // Drawing engine handles rendering and state
    private val drawingEngine = DrawingEngine()
    
    // Touch event processor handles all touch interactions
    private lateinit var touchEventProcessor: TouchEventProcessor
    
    init {
        // View setup
        isFocusable = true
        isFocusableInTouchMode = true
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        // Initialize or resize the drawing engine
        if (oldw == 0 && oldh == 0) {
            // First-time initialization
            drawingEngine.init(w, h)
            
            // Initialize touch processor after the view is sized
            setupTouchHandling()
        } else {
            // Resize existing canvas
            drawingEngine.resize(w, h)
        }
        
        // Set canvas boundaries for panning
        touchEventProcessor.setCanvasBoundaries(
            -w.toFloat(), // min X
            -h.toFloat(), // min Y
            w.toFloat() * 2, // max X
            h.toFloat() * 2  // max Y
        )
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Let the drawing engine handle rendering
        drawingEngine.draw(canvas)
    }
    
    /**
     * Set up touch handling system
     */
    private fun setupTouchHandling() {
        touchEventProcessor = TouchEventProcessor(context, drawingEngine, this)
        touchEventProcessor.setupTouchHandling()
    }
    
    /**
     * Set the active drawing tool
     */
    fun setTool(tool: DrawingTool) {
        drawingEngine.setTool(tool)
        touchEventProcessor.setDrawingTool(tool)
    }
    
    /**
     * Set the brush size
     */
    fun setBrushSize(size: Float) {
        drawingEngine.setBrushSize(size)
    }
    
    /**
     * Set the brush color
     */
    fun setBrushColor(color: Int) {
        drawingEngine.setBrushColor(color)
    }
    
    /**
     * Set the brush opacity (alpha)
     */
    fun setBrushAlpha(alpha: Int) {
        drawingEngine.setBrushAlpha(alpha)
    }
    
    /**
     * Undo the last drawing action
     */
    fun undo(): Boolean {
        val result = drawingEngine.undo()
        if (result) {
            invalidate()
        }
        return result
    }
    
    /**
     * Redo the last undone drawing action
     */
    fun redo(): Boolean {
        val result = drawingEngine.redo()
        if (result) {
            invalidate()
        }
        return result
    }
    
    /**
     * Clear the drawing canvas
     */
    fun clear() {
        drawingEngine.clearCanvasImmediate()
        invalidate()
    }
    
    /**
     * Reset the canvas view (zoom and pan)
     */
    fun resetView() {
        touchEventProcessor.resetCanvasTransform()
        invalidate()
    }
    
    /**
     * Enable or disable accessibility mode
     */
    fun setAccessibilityMode(enabled: Boolean) {
        touchEventProcessor.setAccessibilityMode(enabled)
    }
    
    /**
     * Update the canvas transformation
     */
    fun updateCanvasTransform(transform: CanvasTransform) {
        touchEventProcessor.updateCanvasTransform(transform)
        invalidate()
    }
    
    /**
     * Get the drawing engine
     */
    fun getDrawingEngine(): DrawingEngine {
        return drawingEngine
    }
} 