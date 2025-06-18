package com.example.drawinggame.ui.drawing.models

import com.example.drawinggame.ui.drawing.touch.gestures.GestureConstants

/**
 * Represents the transformation state of the drawing canvas.
 * Tracks pan offset, zoom level, and rotation for coordinate mapping.
 */
data class CanvasTransform(
    // Pan offset (translation) in X and Y dimensions
    var panOffsetX: Float = 0f,
    var panOffsetY: Float = 0f,
    
    // Zoom level (scale factor)
    var zoomLevel: Float = 1.0f,
    
    // Rotation angle in degrees (not used in initial implementation)
    var rotationAngle: Float = 0f,
    
    // Momentum for pan operations
    var panMomentumX: Float = 0f,
    var panMomentumY: Float = 0f,
    
    // Whether canvas has momentum
    var hasMomentum: Boolean = false
) {
    /**
     * Apply pan (translation) to the canvas
     */
    fun applyPan(deltaX: Float, deltaY: Float) {
        panOffsetX += deltaX
        panOffsetY += deltaY
        
        // Update momentum
        panMomentumX = deltaX * GestureConstants.NavigationConstants.PAN_VELOCITY_MULTIPLIER
        panMomentumY = deltaY * GestureConstants.NavigationConstants.PAN_VELOCITY_MULTIPLIER
        hasMomentum = true
    }
    
    /**
     * Apply zoom (scaling) to the canvas with respect to a focal point
     */
    fun applyZoom(scaleFactor: Float, focalX: Float, focalY: Float) {
        // Calculate the point in canvas coordinates before zoom
        val oldCanvasX = (focalX - panOffsetX) / zoomLevel
        val oldCanvasY = (focalY - panOffsetY) / zoomLevel
        
        // Apply zoom level constraints
        val newZoomLevel = zoomLevel * scaleFactor
        zoomLevel = newZoomLevel.coerceIn(
            GestureConstants.MIN_ZOOM_SCALE_FACTOR,
            GestureConstants.MAX_ZOOM_SCALE_FACTOR
        )
        
        // Calculate the point in screen coordinates after zoom
        val newScreenX = oldCanvasX * zoomLevel + panOffsetX
        val newScreenY = oldCanvasY * zoomLevel + panOffsetY
        
        // Adjust pan offset to keep focal point at the same screen position
        panOffsetX += (focalX - newScreenX)
        panOffsetY += (focalY - newScreenY)
        
        // Reset momentum on zoom
        resetMomentum()
    }
    
    /**
     * Apply rotation to the canvas (not used in initial implementation)
     */
    fun applyRotation(deltaAngle: Float) {
        rotationAngle += deltaAngle
        rotationAngle %= 360f
        
        // Reset momentum on rotation
        resetMomentum()
    }
    
    /**
     * Apply momentum for smooth motion
     */
    fun applyMomentum() {
        if (!hasMomentum) return
        
        panOffsetX += panMomentumX
        panOffsetY += panMomentumY
        
        // Apply friction to momentum
        panMomentumX *= GestureConstants.NavigationConstants.MOMENTUM_DECAY_FACTOR
        panMomentumY *= GestureConstants.NavigationConstants.MOMENTUM_DECAY_FACTOR
        
        // Stop momentum when it gets very small
        if (Math.abs(panMomentumX) < GestureConstants.NavigationConstants.MOMENTUM_MIN_THRESHOLD &&
            Math.abs(panMomentumY) < GestureConstants.NavigationConstants.MOMENTUM_MIN_THRESHOLD) {
            resetMomentum()
        }
    }
    
    /**
     * Reset canvas transformation to default state
     */
    fun reset() {
        panOffsetX = 0f
        panOffsetY = 0f
        zoomLevel = 1.0f
        rotationAngle = 0f
        resetMomentum()
    }
    
    /**
     * Reset momentum
     */
    fun resetMomentum() {
        panMomentumX = 0f
        panMomentumY = 0f
        hasMomentum = false
    }
    
    /**
     * Create a copy of this transform
     */
    fun copy(): CanvasTransform {
        return CanvasTransform(
            panOffsetX = panOffsetX,
            panOffsetY = panOffsetY,
            zoomLevel = zoomLevel,
            rotationAngle = rotationAngle,
            panMomentumX = panMomentumX,
            panMomentumY = panMomentumY,
            hasMomentum = hasMomentum
        )
    }
} 