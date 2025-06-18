package com.example.drawinggame.ui.drawing.touch

import com.example.drawinggame.ui.drawing.models.CanvasTransform
import com.example.drawinggame.ui.drawing.touch.gestures.GestureConstants
import com.example.drawinggame.ui.drawing.touch.models.GestureEvent
import com.example.drawinggame.ui.drawing.touch.models.GestureType

/**
 * Handles navigation-specific gestures (pan, zoom, fling).
 * Manages canvas transformations and momentum scrolling.
 */
class NavigationGestureHandler {
    
    // Current canvas transformation state
    private var canvasTransform = CanvasTransform()
    
    // Boundary constraints
    private var minX: Float = Float.NEGATIVE_INFINITY
    private var maxX: Float = Float.POSITIVE_INFINITY
    private var minY: Float = Float.NEGATIVE_INFINITY
    private var maxY: Float = Float.POSITIVE_INFINITY
    
    // Momentum tracking
    private var hasMomentum = false
    private var momentumVelocityX = 0f
    private var momentumVelocityY = 0f
    
    /**
     * Handle a pan gesture event
     */
    fun onPan(event: GestureEvent): Boolean {
        if (event.type != GestureType.PAN) return false
        
        // Extract pan deltas from event
        val deltaX = event.distance * Math.cos(event.angle.toDouble()).toFloat()
        val deltaY = event.distance * Math.sin(event.angle.toDouble()).toFloat()
        
        // Apply pan to canvas transform
        canvasTransform.applyPan(deltaX, deltaY)
        
        // Apply boundary constraints
        applyBoundaryConstraints()
        
        // Update momentum
        momentumVelocityX = deltaX * GestureConstants.NavigationConstants.PAN_VELOCITY_MULTIPLIER
        momentumVelocityY = deltaY * GestureConstants.NavigationConstants.PAN_VELOCITY_MULTIPLIER
        hasMomentum = true
        
        return true
    }
    
    /**
     * Handle a zoom gesture event
     */
    fun onZoom(event: GestureEvent): Boolean {
        if (event.type != GestureType.ZOOM) return false
        
        // Apply zoom to canvas transform
        canvasTransform.applyZoom(event.scale, event.focalX, event.focalY)
        
        // Apply boundary constraints
        applyBoundaryConstraints()
        
        // Reset momentum when zooming
        resetMomentum()
        
        return true
    }
    
    /**
     * Handle a fling gesture event
     */
    fun onFling(event: GestureEvent): Boolean {
        if (event.type != GestureType.FLING) return false
        
        // Calculate fling velocity
        val velocityX = event.velocity * Math.cos(event.angle.toDouble()).toFloat()
        val velocityY = event.velocity * Math.sin(event.angle.toDouble()).toFloat()
        
        // Only process if velocity is above threshold
        if (Math.abs(velocityX) < GestureConstants.MIN_FLING_VELOCITY && 
            Math.abs(velocityY) < GestureConstants.MIN_FLING_VELOCITY) {
            return false
        }
        
        // Set momentum values
        momentumVelocityX = velocityX * 0.05f // Scale down the velocity
        momentumVelocityY = velocityY * 0.05f
        hasMomentum = true
        
        return true
    }
    
    /**
     * Apply momentum for smooth scrolling
     * Call this in animation loop to update position
     * 
     * @return true if momentum is still active
     */
    fun applyMomentum(): Boolean {
        if (!hasMomentum) return false
        
        // Apply momentum to canvas transform
        canvasTransform.panOffsetX += momentumVelocityX
        canvasTransform.panOffsetY += momentumVelocityY
        
        // Apply boundary constraints
        applyBoundaryConstraints()
        
        // Apply friction to slow down momentum
        momentumVelocityX *= GestureConstants.NavigationConstants.MOMENTUM_DECAY_FACTOR
        momentumVelocityY *= GestureConstants.NavigationConstants.MOMENTUM_DECAY_FACTOR
        
        // Check if momentum is below threshold
        if (Math.abs(momentumVelocityX) < GestureConstants.NavigationConstants.MOMENTUM_MIN_THRESHOLD &&
            Math.abs(momentumVelocityY) < GestureConstants.NavigationConstants.MOMENTUM_MIN_THRESHOLD) {
            resetMomentum()
            return false
        }
        
        return true
    }
    
    /**
     * Reset the momentum
     */
    fun resetMomentum() {
        momentumVelocityX = 0f
        momentumVelocityY = 0f
        hasMomentum = false
    }
    
    /**
     * Apply boundary constraints to the canvas transform
     */
    private fun applyBoundaryConstraints() {
        // Apply zoom level constraints
        canvasTransform.zoomLevel = canvasTransform.zoomLevel.coerceIn(
            GestureConstants.MIN_ZOOM_SCALE_FACTOR,
            GestureConstants.MAX_ZOOM_SCALE_FACTOR
        )
        
        // Apply pan offset constraints if boundaries are set
        if (minX != Float.NEGATIVE_INFINITY && maxX != Float.POSITIVE_INFINITY) {
            canvasTransform.panOffsetX = canvasTransform.panOffsetX.coerceIn(minX, maxX)
        }
        
        if (minY != Float.NEGATIVE_INFINITY && maxY != Float.POSITIVE_INFINITY) {
            canvasTransform.panOffsetY = canvasTransform.panOffsetY.coerceIn(minY, maxY)
        }
    }
    
    /**
     * Set boundary constraints for panning
     */
    fun setBoundaries(minX: Float, minY: Float, maxX: Float, maxY: Float) {
        this.minX = minX
        this.minY = minY
        this.maxX = maxX
        this.maxY = maxY
        
        // Apply constraints immediately
        applyBoundaryConstraints()
    }
    
    /**
     * Reset canvas transformation to default state
     */
    fun resetTransform() {
        canvasTransform.reset()
        resetMomentum()
    }
    
    /**
     * Get the current canvas transform
     */
    fun getCanvasTransform(): CanvasTransform {
        return canvasTransform.copy()
    }
    
    /**
     * Update the canvas transform
     */
    fun updateCanvasTransform(transform: CanvasTransform) {
        canvasTransform = transform.copy()
        applyBoundaryConstraints()
    }
} 