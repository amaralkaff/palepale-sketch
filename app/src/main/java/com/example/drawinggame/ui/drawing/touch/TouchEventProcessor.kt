package com.example.drawinggame.ui.drawing.touch

import android.content.Context
import android.view.View
import com.example.drawinggame.ui.drawing.DrawingEngine
import com.example.drawinggame.ui.drawing.models.CanvasTransform
import com.example.drawinggame.ui.drawing.models.DrawingTool
import com.example.drawinggame.ui.drawing.touch.gestures.DrawingGestureDetector
import com.example.drawinggame.ui.drawing.touch.models.GestureEvent
import com.example.drawinggame.ui.drawing.touch.models.TouchState
import com.example.drawinggame.ui.drawing.advanced.brushes.AdvancedBrushManager
import com.example.drawinggame.ui.drawing.advanced.brushes.AdvancedBrushType

/**
 * Central coordinator for all touch event processing.
 * Acts as a bridge between the UI, gesture detection, and drawing systems.
 */
class TouchEventProcessor(
    private val context: Context,
    private val drawingEngine: DrawingEngine,
    private val drawingView: View
) {
    // Touch state management
    private val touchState = TouchState()
    
    // Gesture handlers
    private val drawingGestureHandler = DrawingGestureHandler(drawingEngine)
    private val navigationGestureHandler = NavigationGestureHandler()
    
    // Advanced brush support
    private val advancedBrushManager = AdvancedBrushManager.getInstance(context)
    
    // Gesture detector
    private val gestureDetector: DrawingGestureDetector
    
    // Animation callback for momentum
    private var animationFrameCallback: Runnable? = null
    
    init {
        // Create gesture detector with callbacks to handlers
        gestureDetector = DrawingGestureDetector(
            context,
            touchState,
            object : com.example.drawinggame.ui.drawing.touch.gestures.DrawingGestureListener {
                override fun onDrawStart(event: GestureEvent): Boolean {
                    return drawingGestureHandler.onDrawStart(event)
                }
                
                override fun onDrawMove(event: GestureEvent): Boolean {
                    return drawingGestureHandler.onDrawMove(event)
                }
                
                override fun onDrawEnd(event: GestureEvent): Boolean {
                    return drawingGestureHandler.onDrawEnd(event)
                }
                
                override fun onDrawingCanceled(): Boolean {
                    return drawingGestureHandler.onDrawingCanceled()
                }
                
                override fun onPan(event: GestureEvent): Boolean {
                    val handled = navigationGestureHandler.onPan(event)
                    if (handled) {
                        // Update drawing view with new transform
                        updateCanvasTransform(navigationGestureHandler.getCanvasTransform())
                    }
                    return handled
                }
                
                override fun onZoom(event: GestureEvent): Boolean {
                    val handled = navigationGestureHandler.onZoom(event)
                    if (handled) {
                        // Update drawing view with new transform
                        updateCanvasTransform(navigationGestureHandler.getCanvasTransform())
                    }
                    return handled
                }
                
                override fun onFling(event: GestureEvent): Boolean {
                    val handled = navigationGestureHandler.onFling(event)
                    if (handled) {
                        // Start momentum animation
                        startMomentumAnimation()
                    }
                    return handled
                }
                
                override fun onTap(event: GestureEvent): Boolean {
                    // Handle tap (could be used for tool selection)
                    return false
                }
                
                override fun onDoubleTap(event: GestureEvent): Boolean {
                    // Double tap to reset canvas transform
                    navigationGestureHandler.resetTransform()
                    updateCanvasTransform(navigationGestureHandler.getCanvasTransform())
                    return true
                }
                
                override fun onLongPress(event: GestureEvent): Boolean {
                    // Handle long press (could be used for context menu)
                    return false
                }
            }
        )
        
        // Set initial tool
        setDrawingTool(DrawingTool.PEN)
    }
    
    /**
     * Set up touch handling for the drawing view
     */
    fun setupTouchHandling() {
        drawingView.setOnTouchListener(gestureDetector)
    }
    
    /**
     * Set the active drawing tool
     */
    fun setDrawingTool(tool: DrawingTool) {
        touchState.updateCurrentTool(tool)
        gestureDetector.setDrawingTool(tool)
        // Switch to basic mode when using basic tools
        advancedBrushManager.setBasicMode()
    }
    
    /**
     * Set advanced brush tool
     */
    fun setAdvancedBrushTool(tool: AdvancedBrushType) {
        advancedBrushManager.setAdvancedTool(tool)
        // Advanced tools work with drawing gestures, so update state
        touchState.updateCurrentTool(DrawingTool.BRUSH) // Use BRUSH as base tool for advanced brushes
        gestureDetector.setDrawingTool(DrawingTool.BRUSH)
    }
    
    /**
     * Get advanced brush manager for configuration
     */
    fun getAdvancedBrushManager(): AdvancedBrushManager = advancedBrushManager
    
    /**
     * Update canvas transform in all components
     */
    fun updateCanvasTransform(transform: CanvasTransform) {
        touchState.updateCanvasTransform(transform)
        drawingGestureHandler.updateCanvasTransform(transform)
        navigationGestureHandler.updateCanvasTransform(transform)
        drawingEngine.updateCanvasTransform(transform)
        
        // Request redraw
        drawingView.invalidate()
    }
    
    /**
     * Set canvas boundaries for panning
     */
    fun setCanvasBoundaries(minX: Float, minY: Float, maxX: Float, maxY: Float) {
        navigationGestureHandler.setBoundaries(minX, minY, maxX, maxY)
    }
    
    /**
     * Reset canvas transformation to default
     */
    fun resetCanvasTransform() {
        navigationGestureHandler.resetTransform()
        updateCanvasTransform(navigationGestureHandler.getCanvasTransform())
    }
    
    /**
     * Enable or disable accessibility mode
     */
    fun setAccessibilityMode(enabled: Boolean) {
        touchState.setAccessibilityMode(enabled)
        gestureDetector.setAccessibilityMode(enabled)
    }
    
    /**
     * Start momentum animation after a fling
     */
    private fun startMomentumAnimation() {
        // Cancel any existing animation
        stopMomentumAnimation()
        
        // Create new animation frame callback
        animationFrameCallback = object : Runnable {
            override fun run() {
                // Apply momentum
                val momentumActive = navigationGestureHandler.applyMomentum()
                
                // Update canvas transform
                updateCanvasTransform(navigationGestureHandler.getCanvasTransform())
                
                // Continue animation if momentum is still active
                if (momentumActive) {
                    drawingView.postOnAnimation(this)
                } else {
                    animationFrameCallback = null
                }
            }
        }
        
        // Start animation
        drawingView.postOnAnimation(animationFrameCallback)
    }
    
    /**
     * Stop momentum animation
     */
    private fun stopMomentumAnimation() {
        animationFrameCallback?.let {
            drawingView.removeCallbacks(it)
            animationFrameCallback = null
        }
        
        navigationGestureHandler.resetMomentum()
    }
} 