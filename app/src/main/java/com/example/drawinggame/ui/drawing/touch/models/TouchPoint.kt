package com.example.drawinggame.ui.drawing.touch.models

/**
 * Represents a single touch point on the screen.
 * Used for tracking and processing touch events in the drawing system.
 */
data class TouchPoint(
    // Touch identifier from MotionEvent
    val pointerId: Int,
    
    // Touch index in the current event
    val pointerIndex: Int,
    
    // Screen coordinates
    var screenX: Float,
    var screenY: Float,
    
    // Canvas coordinates (after transformation)
    var canvasX: Float,
    var canvasY: Float,
    
    // Touch pressure (0.0 - 1.0), defaults to 1.0 if not available
    var pressure: Float = 1.0f,
    
    // Timestamp when this touch point was created/updated
    var timestamp: Long = System.currentTimeMillis(),
    
    // Duration of this touch (updated for ongoing touches)
    var duration: Long = 0,
    
    // Movement velocity
    var velocityX: Float = 0f,
    var velocityY: Float = 0f,
    
    // Whether this is the primary pointer
    val isPrimary: Boolean = false
) {
    /**
     * Updates this touch point with new information
     */
    fun update(x: Float, y: Float, canvasX: Float, canvasY: Float, pressure: Float, timestamp: Long) {
        // Calculate velocity based on previous position and time delta
        val timeDelta = timestamp - this.timestamp
        if (timeDelta > 0) {
            velocityX = (x - screenX) / timeDelta
            velocityY = (y - screenY) / timeDelta
        }
        
        // Update position and pressure
        screenX = x
        screenY = y
        this.canvasX = canvasX
        this.canvasY = canvasY
        this.pressure = pressure
        
        // Update timing
        duration = timestamp - this.timestamp
        this.timestamp = timestamp
    }
    
    /**
     * Calculate distance moved from previous position
     */
    fun distanceMoved(prevX: Float, prevY: Float): Float {
        val dx = screenX - prevX
        val dy = screenY - prevY
        return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }
    
    /**
     * Create a copy of this touch point
     */
    fun copy(): TouchPoint {
        return TouchPoint(
            pointerId = pointerId,
            pointerIndex = pointerIndex,
            screenX = screenX,
            screenY = screenY,
            canvasX = canvasX,
            canvasY = canvasY,
            pressure = pressure,
            timestamp = timestamp,
            duration = duration,
            velocityX = velocityX,
            velocityY = velocityY,
            isPrimary = isPrimary
        )
    }
    
    companion object {
        /**
         * Create a TouchPoint from raw coordinates
         */
        fun create(
            pointerId: Int,
            pointerIndex: Int,
            x: Float,
            y: Float,
            canvasX: Float,
            canvasY: Float,
            pressure: Float = 1.0f,
            isPrimary: Boolean = false
        ): TouchPoint {
            return TouchPoint(
                pointerId = pointerId,
                pointerIndex = pointerIndex,
                screenX = x,
                screenY = y,
                canvasX = canvasX,
                canvasY = canvasY,
                pressure = pressure,
                isPrimary = isPrimary
            )
        }
    }
} 