package com.example.drawinggame.ui.drawing.touch

import android.graphics.Path
import android.graphics.PointF
import android.view.MotionEvent
import com.example.drawinggame.ui.drawing.DrawingEngine
import com.example.drawinggame.ui.drawing.models.CanvasTransform
import com.example.drawinggame.ui.drawing.touch.gestures.DrawingGestureListener
import com.example.drawinggame.ui.drawing.touch.models.GestureEvent
import com.example.drawinggame.ui.drawing.touch.utils.PathSmoothingUtils
import com.example.drawinggame.ui.drawing.advanced.brushes.AdvancedBrushManager
import com.example.drawinggame.ui.drawing.advanced.brushes.pressure.PressurePoint

/**
 * Enhanced drawing gesture handler with advanced brush and pressure sensitivity support
 * Extends the basic DrawingGestureHandler with Phase 5.1 advanced features
 */
class AdvancedDrawingGestureHandler(
    private val drawingEngine: DrawingEngine,
    private val advancedBrushManager: AdvancedBrushManager
) : DrawingGestureListener {
    
    // Store path points with pressure for the current stroke
    private val currentStrokePoints = mutableListOf<PressurePoint>()
    
    // Canvas transformation state
    private var canvasTransform = CanvasTransform()
    
    // Whether a stroke is currently active
    private var isStrokeActive = false
    
    // Current motion event for pressure extraction
    private var currentMotionEvent: MotionEvent? = null
    
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
        currentMotionEvent = null // TODO: Get MotionEvent from event
        
        // Get the primary touch point
        val touchPoint = event.getPrimaryTouchPoint() ?: return false
        
        // Create pressure point with timestamp
        val pressurePoint = PressurePoint(
            x = touchPoint.canvasX,
            y = touchPoint.canvasY,
            pressure = touchPoint.pressure,
            timestamp = System.currentTimeMillis()
        )
        currentStrokePoints.add(pressurePoint)
        
        // Get paint with pressure applied
        val paint = if (advancedBrushManager.isAdvancedMode()) {
            // TODO: Get MotionEvent from event to apply pressure
            advancedBrushManager.getAdvancedPaint()
        } else {
            advancedBrushManager.getCurrentPaint()
        }
        
        // Start a new path with the current tool and advanced paint
        val tool = event.tool ?: return false
        drawingEngine.startAdvancedStroke(touchPoint.canvasX, touchPoint.canvasY, tool, paint)
        
        return true
    }
    
    /**
     * Called during an active drawing operation
     */
    override fun onDrawMove(event: GestureEvent): Boolean {
        if (!isStrokeActive) return false
        
        // Get the primary touch point
        val touchPoint = event.getPrimaryTouchPoint() ?: return false
        currentMotionEvent = null // TODO: Get MotionEvent from event
        
        // Create pressure point with timestamp
        val pressurePoint = PressurePoint(
            x = touchPoint.canvasX,
            y = touchPoint.canvasY,
            pressure = touchPoint.pressure,
            timestamp = System.currentTimeMillis()
        )
        currentStrokePoints.add(pressurePoint)
        
        // Apply advanced brush effects and pressure sensitivity
        val tool = event.tool ?: return false
        
        if (advancedBrushManager.isAdvancedMode()) {
            // Apply special effects based on brush type
            val paint = advancedBrushManager.getAdvancedPaint()
            
            // Apply special effects based on pressure and stroke velocity
            val velocity = calculateVelocity()
            val pressure = touchPoint.pressure
            
            // Apply brush-specific effects
            advancedBrushManager.applySpecialEffects(paint, pressure)
            
            // Update stroke with advanced features
            if (currentStrokePoints.size >= 3) {
                val smoothedPath = createPressureSensitivePath()
                drawingEngine.updateAdvancedStroke(smoothedPath, paint, touchPoint.pressure)
            } else {
                drawingEngine.addPointToAdvancedStroke(
                    touchPoint.canvasX, 
                    touchPoint.canvasY, 
                    touchPoint.pressure,
                    paint
                )
            }
        } else {
            // Use basic drawing behavior
            if (currentStrokePoints.size >= 3) {
                val basicPoints = currentStrokePoints.map { PointF(it.x, it.y) }
                val smoothedPath = PathSmoothingUtils.smoothPath(basicPoints, tool)
                drawingEngine.updateStroke(smoothedPath, touchPoint.pressure)
            } else {
                drawingEngine.addPointToStroke(touchPoint.canvasX, touchPoint.canvasY, touchPoint.pressure)
            }
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
            val pressurePoint = PressurePoint(
                x = touchPoint.canvasX,
                y = touchPoint.canvasY,
                pressure = touchPoint.pressure,
                timestamp = System.currentTimeMillis()
            )
            currentStrokePoints.add(pressurePoint)
        }
        
        // Finalize the stroke
        finishCurrentStroke()
        
        return true
    }
    
    /**
     * Called when drawing is canceled
     */
    override fun onDrawingCanceled(): Boolean {
        if (isStrokeActive) {
            drawingEngine.cancelCurrentStroke()
            currentStrokePoints.clear()
            isStrokeActive = false
        }
        return true
    }
    
    /**
     * Called when panning gesture is detected
     */
    override fun onPan(event: GestureEvent): Boolean {
        // Handle canvas panning when not in drawing mode
        if (!isStrokeActive) {
            // TODO: Implement canvas panning
            return true
        }
        return false
    }
    
    /**
     * Called when zoom gesture is detected
     */
    override fun onZoom(event: GestureEvent): Boolean {
        // Handle canvas zooming
        if (!isStrokeActive) {
            // TODO: Implement canvas zooming
            return true
        }
        return false
    }
    
    /**
     * Called when fling gesture is detected
     */
    override fun onFling(event: GestureEvent): Boolean {
        // Handle canvas flinging
        if (!isStrokeActive) {
            // TODO: Implement canvas flinging
            return true
        }
        return false
    }
    
    /**
     * Called when tap gesture is detected
     */
    override fun onTap(event: GestureEvent): Boolean {
        // Handle single tap
        if (!isStrokeActive) {
            // TODO: Implement tap functionality (e.g., place stamp, select tool)
            return true
        }
        return false
    }
    
    /**
     * Called when double tap gesture is detected
     */
    override fun onDoubleTap(event: GestureEvent): Boolean {
        // Handle double tap
        if (!isStrokeActive) {
            // TODO: Implement double tap functionality (e.g., zoom to fit, tool options)
            return true
        }
        return false
    }
    
    /**
     * Called when long press gesture is detected
     */
    override fun onLongPress(event: GestureEvent): Boolean {
        // Handle long press
        if (!isStrokeActive) {
            // TODO: Implement long press functionality (e.g., context menu, tool picker)
            return true
        }
        return false
    }
    
    /**
     * Update canvas transform
     */
    fun updateCanvasTransform(transform: CanvasTransform) {
        canvasTransform = transform
    }
    
    /**
     * Finish the current stroke and commit it to the drawing engine
     */
    private fun finishCurrentStroke() {
        if (currentStrokePoints.isNotEmpty()) {
            if (advancedBrushManager.isAdvancedMode()) {
                // Apply final smoothing and effects for advanced brushes
                val finalPath = createPressureSensitivePath()
                val finalPaint = currentMotionEvent?.let { motionEvent ->
                    advancedBrushManager.getPaintWithPressure(motionEvent)
                } ?: advancedBrushManager.getAdvancedPaint()
                
                drawingEngine.finishAdvancedStroke(finalPath, finalPaint)
            } else {
                // Use basic finish behavior
                drawingEngine.finishStroke()
            }
        }
        
        // Reset state
        currentStrokePoints.clear()
        isStrokeActive = false
        currentMotionEvent = null
    }
    
    /**
     * Create a pressure-sensitive path from collected points
     */
    private fun createPressureSensitivePath(): Path {
        val path = Path()
        
        if (currentStrokePoints.isEmpty()) return path
        
        // Start path at first point
        val firstPoint = currentStrokePoints.first()
        path.moveTo(firstPoint.x, firstPoint.y)
        
        // Create smooth curves with pressure variation
        for (i in 1 until currentStrokePoints.size) {
            val prevPoint = currentStrokePoints[i - 1]
            val currentPoint = currentStrokePoints[i]
            
            // Create smooth curve between points
            // The pressure will be handled by the paint object
            val controlX = (prevPoint.x + currentPoint.x) / 2
            val controlY = (prevPoint.y + currentPoint.y) / 2
            
            path.quadTo(prevPoint.x, prevPoint.y, controlX, controlY)
        }
        
        // Connect to the final point
        if (currentStrokePoints.size > 1) {
            val lastPoint = currentStrokePoints.last()
            path.lineTo(lastPoint.x, lastPoint.y)
        }
        
        return path
    }
    
    /**
     * Calculate stroke velocity for dynamic effects
     */
    private fun calculateVelocity(): Float {
        if (currentStrokePoints.size < 2) return 0f
        
        val recent = currentStrokePoints.takeLast(2)
        val deltaX = recent[1].x - recent[0].x
        val deltaY = recent[1].y - recent[0].y
        val deltaTime = recent[1].timestamp - recent[0].timestamp
        
        if (deltaTime <= 0) return 0f
        
        val distance = kotlin.math.sqrt(deltaX * deltaX + deltaY * deltaY)
        return distance / deltaTime.toFloat()
    }
}