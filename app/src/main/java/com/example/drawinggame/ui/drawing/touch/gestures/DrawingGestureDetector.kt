package com.example.drawinggame.ui.drawing.touch.gestures

import android.content.Context
import android.graphics.PointF
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.example.drawinggame.ui.drawing.models.DrawingTool
import com.example.drawinggame.ui.drawing.touch.models.GestureEvent
import com.example.drawinggame.ui.drawing.touch.models.GestureType
import com.example.drawinggame.ui.drawing.touch.models.TouchPoint
import com.example.drawinggame.ui.drawing.touch.models.TouchState
import com.example.drawinggame.ui.drawing.touch.utils.CoordinateUtils

/**
 * Specialized detector for drawing-specific gestures.
 * Combines Android's built-in gesture detectors with custom touch tracking for drawing.
 */
class DrawingGestureDetector(
    private val context: Context,
    private val touchState: TouchState,
    private val gestureListener: DrawingGestureListener
) : View.OnTouchListener {
    
    // Android's built-in gesture detectors for standard gestures
    private val gestureDetector: GestureDetector
    private val scaleGestureDetector: ScaleGestureDetector
    
    // Track active touch points for multi-touch
    private val activePoints = HashMap<Int, TouchPoint>()
    
    // Track the last touch positions for velocity calculation
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var lastTouchTime = 0L
    
    // Minimum movement threshold in pixels
    private val touchSlop: Float
    
    init {
        // Initialize standard gesture detector
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                // Always consume onDown to ensure other gestures can be detected
                return true
            }
            
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                return handleTap(e)
            }
            
            override fun onDoubleTap(e: MotionEvent): Boolean {
                return handleDoubleTap(e)
            }
            
            override fun onLongPress(e: MotionEvent) {
                handleLongPress(e)
            }
            
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                // Scrolling is handled in onTouch
                return false
            }
            
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                return handleFling(e2, velocityX, velocityY)
            }
        })
        
        // Initialize scale gesture detector for pinch-to-zoom
        scaleGestureDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                return handleScale(detector)
            }
            
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                // Always return true to start scaling
                return true
            }
            
            override fun onScaleEnd(detector: ScaleGestureDetector) {
                // Handle scale end if needed
            }
        })
        
        // Convert touch slop from dp to pixels
        touchSlop = CoordinateUtils.dpToPx(
            context,
            GestureConstants.TOUCH_SLOP_DP
        )
    }
    
    /**
     * Main touch event handler that routes events to appropriate gesture detectors
     */
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        // Pass the event to the built-in detectors
        scaleGestureDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        
        // Track the current time for velocity calculations
        val currentTime = System.currentTimeMillis()
        
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // Initialize first touch
                lastTouchX = event.x
                lastTouchY = event.y
                lastTouchTime = currentTime
                
                // Track the touch point
                trackTouchPoint(event, 0, true)
                
                // Reset any active drawing state
                if (!scaleGestureDetector.isInProgress) {
                    touchState.setCurrentGestureType(GestureType.NONE)
                    // Start drawing if not in a navigation gesture
                    return handleDrawStart(event)
                }
            }
            
            MotionEvent.ACTION_POINTER_DOWN -> {
                // Track additional fingers
                val pointerIndex = event.actionIndex
                trackTouchPoint(event, pointerIndex, false)
                
                // If we now have two fingers, cancel drawing and prepare for navigation
                if (activePoints.size == 2 && touchState.isDrawing) {
                    // Cancel drawing and switch to navigation mode
                    gestureListener.onDrawingCanceled()
                    touchState.setCurrentGestureType(GestureType.NONE)
                }
            }
            
            MotionEvent.ACTION_MOVE -> {
                // Calculate velocity
                val deltaTime = currentTime - lastTouchTime
                val deltaX = event.x - lastTouchX
                val deltaY = event.y - lastTouchY
                val velocity = if (deltaTime > 0) {
                    Math.sqrt((deltaX * deltaX + deltaY * deltaY).toDouble()).toFloat() / deltaTime
                } else {
                    0f
                }
                
                // Update last touch values
                lastTouchX = event.x
                lastTouchY = event.y
                lastTouchTime = currentTime
                
                // Update all active touch points
                for (i in 0 until event.pointerCount) {
                    val pointerId = event.getPointerId(i)
                    updateTouchPoint(event, i, pointerId)
                }
                
                // Handle the move based on current gesture state
                when {
                    scaleGestureDetector.isInProgress -> {
                        // Scale gesture handled by the detector
                        touchState.setCurrentGestureType(GestureType.ZOOM)
                    }
                    touchState.getTouchCount() >= 2 -> {
                        // Multi-touch navigation (pan)
                        handlePan(event, deltaX, deltaY)
                    }
                    touchState.isDrawing -> {
                        // Continue drawing with a single finger
                        handleDrawMove(event, velocity)
                    }
                    touchState.getTouchCount() == 1 && !touchState.isNavigating -> {
                        // Check if we've moved enough to start drawing
                        val moveDistance = Math.sqrt((deltaX * deltaX + deltaY * deltaY).toDouble()).toFloat()
                        if (moveDistance > touchSlop) {
                            // Start drawing
                            handleDrawStart(event)
                            handleDrawMove(event, velocity)
                        }
                    }
                }
            }
            
            MotionEvent.ACTION_POINTER_UP -> {
                // Remove the lifted finger from tracking
                val pointerIndex = event.actionIndex
                val pointerId = event.getPointerId(pointerIndex)
                activePoints.remove(pointerId)
                touchState.removeTouchPoint(pointerId)
                
                // If only one finger remains and we were zooming/panning, reset to neutral state
                if (activePoints.size == 1 && (touchState.currentGestureType == GestureType.ZOOM ||
                            touchState.currentGestureType == GestureType.PAN)) {
                    touchState.setCurrentGestureType(GestureType.NONE)
                }
            }
            
            MotionEvent.ACTION_UP -> {
                // End of all touches
                val pointerId = event.getPointerId(0)
                activePoints.remove(pointerId)
                touchState.removeTouchPoint(pointerId)
                
                // If we were drawing, end the stroke
                if (touchState.isDrawing) {
                    handleDrawEnd(event)
                }
                
                // Reset to no gesture
                touchState.setCurrentGestureType(GestureType.NONE)
            }
            
            MotionEvent.ACTION_CANCEL -> {
                // Clear all touch tracking
                activePoints.clear()
                touchState.clearTouchState()
                
                // Cancel any active drawing
                if (touchState.isDrawing) {
                    gestureListener.onDrawingCanceled()
                }
            }
        }
        
        return true
    }
    
    /**
     * Track a new touch point
     */
    private fun trackTouchPoint(event: MotionEvent, pointerIndex: Int, isPrimary: Boolean) {
        val pointerId = event.getPointerId(pointerIndex)
        val x = event.getX(pointerIndex)
        val y = event.getY(pointerIndex)
        val pressure = if (event.getToolType(pointerIndex) == MotionEvent.TOOL_TYPE_STYLUS) {
            event.getPressure(pointerIndex)
        } else {
            1.0f // Default pressure for finger
        }
        
        // Transform screen to canvas coordinates
        val (canvasX, canvasY) = touchState.screenToCanvasCoordinates(x, y)
        
        // Create and store touch point
        val touchPoint = TouchPoint.create(
            pointerId = pointerId,
            pointerIndex = pointerIndex,
            x = x,
            y = y,
            canvasX = canvasX,
            canvasY = canvasY,
            pressure = pressure,
            isPrimary = isPrimary
        )
        
        activePoints[pointerId] = touchPoint
        touchState.addOrUpdateTouchPoint(touchPoint)
    }
    
    /**
     * Update an existing touch point
     */
    private fun updateTouchPoint(event: MotionEvent, pointerIndex: Int, pointerId: Int) {
        val existingPoint = activePoints[pointerId] ?: return
        
        val x = event.getX(pointerIndex)
        val y = event.getY(pointerIndex)
        val pressure = if (event.getToolType(pointerIndex) == MotionEvent.TOOL_TYPE_STYLUS) {
            event.getPressure(pointerIndex)
        } else {
            1.0f
        }
        
        // Transform screen to canvas coordinates
        val (canvasX, canvasY) = touchState.screenToCanvasCoordinates(x, y)
        
        // Update touch point
        existingPoint.update(x, y, canvasX, canvasY, pressure, System.currentTimeMillis())
        
        // Update in touch state
        touchState.addOrUpdateTouchPoint(existingPoint)
    }
    
    /**
     * Handle the start of a drawing operation
     */
    private fun handleDrawStart(event: MotionEvent): Boolean {
        if (touchState.getTouchCount() != 1 || scaleGestureDetector.isInProgress) {
            return false
        }
        
        val primaryPoint = touchState.getPrimaryTouchPoint() ?: return false
        
        // Create and dispatch drawing start event
        val gestureEvent = GestureEvent.createDrawStart(
            primaryPoint,
            touchState.currentTool
        )
        
        touchState.setCurrentGestureType(GestureType.DRAW_START)
        touchState.setLastGestureEvent(gestureEvent)
        
        return gestureListener.onDrawStart(gestureEvent)
    }
    
    /**
     * Handle movement during drawing
     */
    private fun handleDrawMove(event: MotionEvent, velocity: Float): Boolean {
        if (!touchState.isDrawing || touchState.getTouchCount() != 1) {
            return false
        }
        
        val primaryPoint = touchState.getPrimaryTouchPoint() ?: return false
        
        // Create and dispatch drawing event
        val gestureEvent = GestureEvent.createDraw(
            primaryPoint,
            touchState.currentTool,
            velocity
        )
        
        touchState.setCurrentGestureType(GestureType.DRAW)
        touchState.setLastGestureEvent(gestureEvent)
        
        return gestureListener.onDrawMove(gestureEvent)
    }
    
    /**
     * Handle the end of a drawing operation
     */
    private fun handleDrawEnd(event: MotionEvent): Boolean {
        if (!touchState.isDrawing) {
            return false
        }
        
        val lastPoint = activePoints.values.firstOrNull() ?: touchState.getPrimaryTouchPoint() ?: return false
        
        // Create and dispatch drawing end event
        val gestureEvent = GestureEvent.createDrawEnd(
            lastPoint,
            touchState.currentTool
        )
        
        touchState.setCurrentGestureType(GestureType.DRAW_END)
        touchState.setLastGestureEvent(gestureEvent)
        
        return gestureListener.onDrawEnd(gestureEvent)
    }
    
    /**
     * Handle pan (translation) gesture
     */
    private fun handlePan(event: MotionEvent, deltaX: Float, deltaY: Float): Boolean {
        if (scaleGestureDetector.isInProgress || touchState.getTouchCount() < 2) {
            return false
        }
        
        val touchPoints = touchState.getAllTouchPoints()
        
        // Create and dispatch pan event
        val gestureEvent = GestureEvent.createPan(
            touchPoints,
            -deltaX,
            -deltaY
        )
        
        touchState.setCurrentGestureType(GestureType.PAN)
        touchState.setLastGestureEvent(gestureEvent)
        
        return gestureListener.onPan(gestureEvent)
    }
    
    /**
     * Handle pinch-to-zoom gesture
     */
    private fun handleScale(detector: ScaleGestureDetector): Boolean {
        val touchPoints = touchState.getAllTouchPoints()
        
        // Create and dispatch zoom event
        val gestureEvent = GestureEvent.createZoom(
            touchPoints,
            detector.scaleFactor
        )
        
        touchState.setCurrentGestureType(GestureType.ZOOM)
        touchState.setLastGestureEvent(gestureEvent)
        
        return gestureListener.onZoom(gestureEvent)
    }
    
    /**
     * Handle fling gesture for momentum scrolling
     */
    private fun handleFling(
        event: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (Math.abs(velocityX) < GestureConstants.MIN_FLING_VELOCITY && 
            Math.abs(velocityY) < GestureConstants.MIN_FLING_VELOCITY) {
            return false
        }
        
        // Create focal point from current touch
        val focalX = event.x
        val focalY = event.y
        
        // Create event for fling
        val gestureEvent = GestureEvent(
            type = GestureType.FLING,
            touchPoints = touchState.getAllTouchPoints(),
            focalX = focalX,
            focalY = focalY,
            velocity = Math.sqrt((velocityX * velocityX + velocityY * velocityY).toDouble()).toFloat(),
            distance = 0f,
            angle = Math.atan2(velocityY.toDouble(), velocityX.toDouble()).toFloat()
        )
        
        touchState.setLastGestureEvent(gestureEvent)
        
        return gestureListener.onFling(gestureEvent)
    }
    
    /**
     * Handle tap gesture
     */
    private fun handleTap(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        val (canvasX, canvasY) = touchState.screenToCanvasCoordinates(x, y)
        
        // Create tap event
        val gestureEvent = GestureEvent(
            type = GestureType.TAP,
            touchPoints = touchState.getAllTouchPoints(),
            focalX = canvasX,
            focalY = canvasY
        )
        
        touchState.setLastGestureEvent(gestureEvent)
        
        return gestureListener.onTap(gestureEvent)
    }
    
    /**
     * Handle double tap gesture
     */
    private fun handleDoubleTap(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        val (canvasX, canvasY) = touchState.screenToCanvasCoordinates(x, y)
        
        // Create double tap event
        val gestureEvent = GestureEvent(
            type = GestureType.DOUBLE_TAP,
            touchPoints = touchState.getAllTouchPoints(),
            focalX = canvasX,
            focalY = canvasY
        )
        
        touchState.setLastGestureEvent(gestureEvent)
        
        return gestureListener.onDoubleTap(gestureEvent)
    }
    
    /**
     * Handle long press gesture
     */
    private fun handleLongPress(event: MotionEvent) {
        val x = event.x
        val y = event.y
        val (canvasX, canvasY) = touchState.screenToCanvasCoordinates(x, y)
        
        // Create long press event
        val gestureEvent = GestureEvent(
            type = GestureType.LONG_PRESS,
            touchPoints = touchState.getAllTouchPoints(),
            focalX = canvasX,
            focalY = canvasY
        )
        
        touchState.setLastGestureEvent(gestureEvent)
        
        gestureListener.onLongPress(gestureEvent)
    }
    
    /**
     * Update the current drawing tool
     */
    fun setDrawingTool(tool: DrawingTool) {
        touchState.updateCurrentTool(tool)
    }
    
    /**
     * Enable or disable accessibility mode
     */
    fun setAccessibilityMode(enabled: Boolean) {
        touchState.setAccessibilityMode(enabled)
    }
}

/**
 * Interface for listening to drawing-specific gestures
 */
interface DrawingGestureListener {
    /**
     * Called when a drawing operation begins
     */
    fun onDrawStart(event: GestureEvent): Boolean
    
    /**
     * Called during an active drawing operation
     */
    fun onDrawMove(event: GestureEvent): Boolean
    
    /**
     * Called when a drawing operation ends
     */
    fun onDrawEnd(event: GestureEvent): Boolean
    
    /**
     * Called when a drawing operation is canceled
     */
    fun onDrawingCanceled(): Boolean
    
    /**
     * Called when a pan gesture is detected
     */
    fun onPan(event: GestureEvent): Boolean
    
    /**
     * Called when a zoom gesture is detected
     */
    fun onZoom(event: GestureEvent): Boolean
    
    /**
     * Called when a fling gesture is detected
     */
    fun onFling(event: GestureEvent): Boolean
    
    /**
     * Called when a tap is detected
     */
    fun onTap(event: GestureEvent): Boolean
    
    /**
     * Called when a double tap is detected
     */
    fun onDoubleTap(event: GestureEvent): Boolean
    
    /**
     * Called when a long press is detected
     */
    fun onLongPress(event: GestureEvent): Boolean
} 