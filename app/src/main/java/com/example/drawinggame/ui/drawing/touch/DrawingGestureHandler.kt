package com.example.drawinggame.ui.drawing.touch

import android.graphics.Path
import android.graphics.PointF
import com.example.drawinggame.ui.drawing.DrawingEngine
import com.example.drawinggame.ui.drawing.models.CanvasTransform
import com.example.drawinggame.ui.drawing.touch.gestures.DrawingGestureListener
import com.example.drawinggame.ui.drawing.touch.models.GestureEvent
import com.example.drawinggame.ui.drawing.touch.utils.PathSmoothingUtils

/**
 * Handles drawing-specific gestures and converts them to drawing operations.
 * Acts as a bridge between the gesture detection system and the drawing engine.
 */
class DrawingGestureHandler(
    private val drawingEngine: DrawingEngine
) : DrawingGestureListener {
    
    // Store path points for the current stroke
    private val currentStrokePoints = mutableListOf<PointF>()
    
    // Canvas transformation state
    private var canvasTransform = CanvasTransform()
    
    // Whether a stroke is currently active
    private var isStrokeActive = false
    
    /**
     * Called when a drawing operation begins
     */
    override fun onDrawStart(event: GestureEvent): Boolean {
        if (isStrokeActive) {
            // Finalize any existing stroke first
            finishCurrentStroke()
        }
        
        // Clear previous points and start a new stroke
        currentStrokePoints.clear()
        isStrokeActive = true
        
        // Get the primary touch point
        val touchPoint = event.getPrimaryTouchPoint() ?: return false
        
        // Add the first point to the current stroke
        currentStrokePoints.add(PointF(touchPoint.canvasX, touchPoint.canvasY))
        
        // Start a new path with the current tool
        val tool = event.tool ?: return false
        drawingEngine.startStroke(touchPoint.canvasX, touchPoint.canvasY, tool)
        
        return true
    }
    
    /**
     * Called during an active drawing operation
     */
    override fun onDrawMove(event: GestureEvent): Boolean {
        if (!isStrokeActive) return false
        
        // Get the primary touch point
        val touchPoint = event.getPrimaryTouchPoint() ?: return false
        
        // Add point to the current stroke
        currentStrokePoints.add(PointF(touchPoint.canvasX, touchPoint.canvasY))
        
        // Apply smoothing if we have enough points
        val tool = event.tool ?: return false
        
        if (currentStrokePoints.size >= 3) {
            // Create a smoothed path from collected points
            val smoothedPath = PathSmoothingUtils.smoothPath(currentStrokePoints, tool)
            
            // Add the path to the current stroke
            drawingEngine.updateStroke(smoothedPath, touchPoint.pressure)
        } else {
            // Not enough points for smoothing, just add a line segment
            drawingEngine.addPointToStroke(touchPoint.canvasX, touchPoint.canvasY, touchPoint.pressure)
        }
        
        return true
    }
    
    /**
     * Called when a drawing operation ends
     */
    override fun onDrawEnd(event: GestureEvent): Boolean {
        if (!isStrokeActive) return false
        
        // Get the final touch point
        val touchPoint = event.getPrimaryTouchPoint()
        if (touchPoint != null) {
            // Add the final point to the current stroke
            currentStrokePoints.add(PointF(touchPoint.canvasX, touchPoint.canvasY))
        }
        
        // Finalize the stroke
        finishCurrentStroke()
        
        return true
    }
    
    /**
     * Called when a drawing operation is canceled
     */
    override fun onDrawingCanceled(): Boolean {
        if (!isStrokeActive) return false
        
        // Cancel current stroke
        drawingEngine.cancelStroke()
        
        // Reset stroke state
        currentStrokePoints.clear()
        isStrokeActive = false
        
        return true
    }
    
    /**
     * Called when a pan gesture is detected
     */
    override fun onPan(event: GestureEvent): Boolean {
        // Apply pan to canvas transform
        canvasTransform.applyPan(event.distance * Math.cos(event.angle.toDouble()).toFloat(), 
                                 event.distance * Math.sin(event.angle.toDouble()).toFloat())
        
        // Update the drawing engine with new transform
        drawingEngine.updateCanvasTransform(canvasTransform)
        
        return true
    }
    
    /**
     * Called when a zoom gesture is detected
     */
    override fun onZoom(event: GestureEvent): Boolean {
        // Apply zoom to canvas transform
        canvasTransform.applyZoom(event.scale, event.focalX, event.focalY)
        
        // Update the drawing engine with new transform
        drawingEngine.updateCanvasTransform(canvasTransform)
        
        return true
    }
    
    /**
     * Called when a fling gesture is detected
     */
    override fun onFling(event: GestureEvent): Boolean {
        // For now, just treat fling as a pan with momentum
        return true
    }
    
    /**
     * Called when a tap is detected
     */
    override fun onTap(event: GestureEvent): Boolean {
        // Single tap can be used for UI interactions or tool selection
        // For now, just return true to consume the event
        return true
    }
    
    /**
     * Called when a double tap is detected
     */
    override fun onDoubleTap(event: GestureEvent): Boolean {
        // Double tap to reset canvas transform
        canvasTransform.reset()
        drawingEngine.updateCanvasTransform(canvasTransform)
        return true
    }
    
    /**
     * Called when a long press is detected
     */
    override fun onLongPress(event: GestureEvent): Boolean {
        // Long press can be used for context menus or special tools
        // For now, just return true to consume the event
        return true
    }
    
    /**
     * Update the canvas transform
     */
    fun updateCanvasTransform(transform: CanvasTransform) {
        canvasTransform = transform
    }
    
    /**
     * Finish the current stroke by creating a final smoothed path
     */
    private fun finishCurrentStroke() {
        if (currentStrokePoints.isEmpty()) {
            drawingEngine.cancelStroke()
        } else {
            // Optimize the path to remove redundant points
            val optimizedPoints = PathSmoothingUtils.optimizePath(currentStrokePoints)
            
            // Create a final smoothed path
            val finalPath = PathSmoothingUtils.smoothPath(
                optimizedPoints,
                drawingEngine.getCurrentTool()
            )
            
            // Finalize the stroke
            drawingEngine.finishStroke(finalPath)
        }
        
        // Reset stroke state
        currentStrokePoints.clear()
        isStrokeActive = false
    }
} 