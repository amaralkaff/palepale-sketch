package com.example.drawinggame.ui.drawing.touch.models

import com.example.drawinggame.ui.drawing.models.DrawingTool

/**
 * Represents a recognized gesture event in the drawing system.
 * Contains information about the type of gesture and associated parameters.
 */
data class GestureEvent(
    // Type of gesture detected
    val type: GestureType,
    
    // Touch points involved in this gesture
    val touchPoints: List<TouchPoint>,
    
    // Focal point of the gesture (e.g., center of pinch)
    val focalX: Float,
    val focalY: Float,
    
    // Additional parameters based on gesture type
    val distance: Float = 0f,        // For pinch/zoom gestures
    val angle: Float = 0f,           // For rotation gestures
    val velocity: Float = 0f,        // For fling gestures
    val scale: Float = 1.0f,         // For scale gestures
    
    // The drawing tool associated with the gesture
    val tool: DrawingTool? = null,
    
    // Timestamp when the gesture was recognized
    val timestamp: Long = System.currentTimeMillis()
) {

    /**
     * Check if this is a drawing gesture
     */
    fun isDrawingGesture(): Boolean {
        return type == GestureType.DRAW || type == GestureType.DRAW_START || type == GestureType.DRAW_END
    }
    
    /**
     * Check if this is a navigation gesture
     */
    fun isNavigationGesture(): Boolean {
        return type == GestureType.PAN || type == GestureType.ZOOM || type == GestureType.FLING
    }
    
    /**
     * Check if this is a special action gesture
     */
    fun isActionGesture(): Boolean {
        return type == GestureType.TAP || type == GestureType.LONG_PRESS || 
               type == GestureType.DOUBLE_TAP || type == GestureType.MULTI_TAP
    }
    
    /**
     * Get the primary touch point for this gesture
     */
    fun getPrimaryTouchPoint(): TouchPoint? {
        return touchPoints.firstOrNull { it.isPrimary } ?: touchPoints.firstOrNull()
    }
    
    companion object {
        /**
         * Create a drawing start gesture
         */
        fun createDrawStart(touchPoint: TouchPoint, tool: DrawingTool): GestureEvent {
            return GestureEvent(
                type = GestureType.DRAW_START,
                touchPoints = listOf(touchPoint),
                focalX = touchPoint.canvasX,
                focalY = touchPoint.canvasY,
                tool = tool
            )
        }
        
        /**
         * Create a drawing gesture
         */
        fun createDraw(touchPoint: TouchPoint, tool: DrawingTool, velocity: Float): GestureEvent {
            return GestureEvent(
                type = GestureType.DRAW,
                touchPoints = listOf(touchPoint),
                focalX = touchPoint.canvasX,
                focalY = touchPoint.canvasY,
                velocity = velocity,
                tool = tool
            )
        }
        
        /**
         * Create a drawing end gesture
         */
        fun createDrawEnd(touchPoint: TouchPoint, tool: DrawingTool): GestureEvent {
            return GestureEvent(
                type = GestureType.DRAW_END,
                touchPoints = listOf(touchPoint),
                focalX = touchPoint.canvasX,
                focalY = touchPoint.canvasY,
                tool = tool
            )
        }
        
        /**
         * Create a pan gesture
         */
        fun createPan(touchPoints: List<TouchPoint>, deltaX: Float, deltaY: Float): GestureEvent {
            val focal = calculateFocalPoint(touchPoints)
            return GestureEvent(
                type = GestureType.PAN,
                touchPoints = touchPoints,
                focalX = focal.first,
                focalY = focal.second,
                distance = Math.sqrt((deltaX * deltaX + deltaY * deltaY).toDouble()).toFloat(),
                velocity = touchPoints.firstOrNull()?.let { 
                    Math.sqrt((it.velocityX * it.velocityX + it.velocityY * it.velocityY).toDouble()).toFloat() 
                } ?: 0f
            )
        }
        
        /**
         * Create a zoom gesture
         */
        fun createZoom(touchPoints: List<TouchPoint>, scale: Float): GestureEvent {
            val focal = calculateFocalPoint(touchPoints)
            return GestureEvent(
                type = GestureType.ZOOM,
                touchPoints = touchPoints,
                focalX = focal.first,
                focalY = focal.second,
                scale = scale
            )
        }
        
        /**
         * Calculate the focal point from a list of touch points
         */
        private fun calculateFocalPoint(touchPoints: List<TouchPoint>): Pair<Float, Float> {
            if (touchPoints.isEmpty()) return Pair(0f, 0f)
            
            var sumX = 0f
            var sumY = 0f
            
            touchPoints.forEach { 
                sumX += it.canvasX
                sumY += it.canvasY
            }
            
            return Pair(sumX / touchPoints.size, sumY / touchPoints.size)
        }
    }
}

/**
 * Types of gestures supported by the drawing system.
 */
enum class GestureType {
    // Drawing gestures
    DRAW_START,     // Start of a drawing operation
    DRAW,           // Active drawing
    DRAW_END,       // End of a drawing operation
    
    // Navigation gestures
    PAN,            // Canvas translation
    ZOOM,           // Canvas scaling
    FLING,          // Quick movement with momentum
    
    // Action gestures
    TAP,            // Quick press and release
    DOUBLE_TAP,     // Two quick taps
    LONG_PRESS,     // Press and hold
    MULTI_TAP,      // Multiple fingers tap
    
    // Special gestures
    NONE            // No gesture detected
} 