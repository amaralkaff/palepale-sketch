package com.example.drawinggame.ui.drawing.touch.models

import com.example.drawinggame.ui.drawing.models.CanvasTransform
import com.example.drawinggame.ui.drawing.models.DrawingTool

/**
 * Represents the current state of touch interactions on the drawing canvas.
 * Maintains active touch points, current gesture, and context information.
 */
class TouchState {
    // Active touch points, mapped by pointer ID
    private val activeTouchPoints = mutableMapOf<Int, TouchPoint>()
    
    // The currently active gesture type
    var currentGestureType: GestureType = GestureType.NONE
        private set
    
    // Canvas transformation state
    var canvasTransform = CanvasTransform()
        private set
    
    // Current drawing tool context
    var currentTool: DrawingTool = DrawingTool.PEN
        private set
    
    // Accessibility mode status
    var isAccessibilityModeEnabled: Boolean = false
        private set
    
    // Whether drawing is currently in progress
    var isDrawing: Boolean = false
        private set
    
    // Whether navigation gestures (pan/zoom) are currently in progress
    var isNavigating: Boolean = false
        private set
    
    // Last recognized gesture event
    var lastGestureEvent: GestureEvent? = null
        private set
    
    /**
     * Add or update a touch point
     */
    fun addOrUpdateTouchPoint(touchPoint: TouchPoint) {
        activeTouchPoints[touchPoint.pointerId] = touchPoint
    }
    
    /**
     * Remove a touch point
     */
    fun removeTouchPoint(pointerId: Int): TouchPoint? {
        return activeTouchPoints.remove(pointerId)
    }
    
    /**
     * Get a touch point by pointer ID
     */
    fun getTouchPoint(pointerId: Int): TouchPoint? {
        return activeTouchPoints[pointerId]
    }
    
    /**
     * Get all active touch points
     */
    fun getAllTouchPoints(): List<TouchPoint> {
        return activeTouchPoints.values.toList()
    }
    
    /**
     * Get count of active touch points
     */
    fun getTouchCount(): Int {
        return activeTouchPoints.size
    }
    
    /**
     * Check if there are any active touch points
     */
    fun hasTouches(): Boolean {
        return activeTouchPoints.isNotEmpty()
    }
    
    /**
     * Get the primary touch point (first touch or specifically marked as primary)
     */
    fun getPrimaryTouchPoint(): TouchPoint? {
        return activeTouchPoints.values.firstOrNull { it.isPrimary } 
            ?: activeTouchPoints.values.firstOrNull()
    }
    
    /**
     * Update the current gesture type
     */
    fun setCurrentGestureType(gestureType: GestureType) {
        currentGestureType = gestureType
        
        // Update drawing and navigation states based on gesture type
        isDrawing = gestureType == GestureType.DRAW || 
                    gestureType == GestureType.DRAW_START
        
        isNavigating = gestureType == GestureType.PAN || 
                       gestureType == GestureType.ZOOM
    }
    
    /**
     * Update the canvas transformation
     */
    fun updateCanvasTransform(transform: CanvasTransform) {
        canvasTransform = transform
    }
    
    /**
     * Update the current drawing tool
     */
    fun updateCurrentTool(tool: DrawingTool) {
        currentTool = tool
    }
    
    /**
     * Update accessibility mode
     */
    fun setAccessibilityMode(enabled: Boolean) {
        isAccessibilityModeEnabled = enabled
    }
    
    /**
     * Update the last gesture event
     */
    fun setLastGestureEvent(event: GestureEvent) {
        lastGestureEvent = event
    }
    
    /**
     * Clear all touch state (on action cancel or pointer up for all touches)
     */
    fun clearTouchState() {
        activeTouchPoints.clear()
        currentGestureType = GestureType.NONE
        isDrawing = false
        isNavigating = false
    }
    
    /**
     * Transform screen coordinates to canvas coordinates
     */
    fun screenToCanvasCoordinates(screenX: Float, screenY: Float): Pair<Float, Float> {
        // Apply inverse of canvas transformation to get canvas coordinates
        val canvasX = (screenX - canvasTransform.panOffsetX) / canvasTransform.zoomLevel
        val canvasY = (screenY - canvasTransform.panOffsetY) / canvasTransform.zoomLevel
        return Pair(canvasX, canvasY)
    }
} 