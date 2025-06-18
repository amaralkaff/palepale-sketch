package com.example.drawinggame.ui.drawing.touch.gestures

/**
 * Constants for the gesture detection and handling system.
 */
object GestureConstants {
    // Minimum movement required to start drawing (to filter out noise)
    const val TOUCH_SLOP_DP = 2f
    
    // Maximum touch latency for 60 FPS response
    const val MAX_TOUCH_LATENCY_MS = 16
    
    // Minimum distance between touch points for a multi-touch gesture
    const val MIN_MULTI_TOUCH_DISTANCE_DP = 50f
    
    // Thresholds for gesture recognition
    const val TAP_TIMEOUT_MS = 300
    const val LONG_PRESS_TIMEOUT_MS = 500
    const val DOUBLE_TAP_TIMEOUT_MS = 300
    const val DOUBLE_TAP_MAX_DISTANCE_DP = 20f
    
    // Pan gesture parameters
    const val MIN_PAN_DISTANCE_DP = 10f
    const val PAN_FRICTION_FACTOR = 0.95f
    
    // Zoom gesture parameters
    const val MIN_ZOOM_SCALE_FACTOR = 0.1f
    const val MAX_ZOOM_SCALE_FACTOR = 10.0f
    const val ZOOM_FRICTION_FACTOR = 0.9f
    
    // Fling gesture parameters
    const val MIN_FLING_VELOCITY = 1000f
    const val FLING_DECELERATION_FACTOR = 0.9f
    
    // Path smoothing parameters
    const val PATH_OPTIMIZATION_THRESHOLD = 5
    const val PATH_SMOOTHING_FACTOR = 0.3f
    
    // Touch event buffer size (for performance optimization)
    const val TOUCH_EVENT_BUFFER_SIZE = 32
    
    // Maximum number of tracked pointers
    const val MAX_POINTER_COUNT = 10
    
    // Accessibility constants
    const val ACCESSIBILITY_TOUCH_SLOP_MULTIPLIER = 1.5f
    const val ACCESSIBILITY_TIMEOUT_MULTIPLIER = 1.5f
    
    // Constants for handling drawing with different tools
    object DrawingToolConstants {
        // Pen constants
        const val PEN_SMOOTHING_FACTOR = 0.2f
        const val PEN_VELOCITY_IMPACT_FACTOR = 0.1f
        
        // Brush constants
        const val BRUSH_SMOOTHING_FACTOR = 0.5f
        const val BRUSH_PRESSURE_IMPACT_FACTOR = 0.8f
        
        // Eraser constants
        const val ERASER_SMOOTHING_FACTOR = 0.3f
        const val ERASER_SIZE_MULTIPLIER = 1.5f
    }
    
    // Constants for navigation gestures
    object NavigationConstants {
        // Pan constants
        const val PAN_EDGE_RESISTANCE = 0.8f
        const val PAN_VELOCITY_MULTIPLIER = 1.2f
        
        // Zoom constants
        const val ZOOM_SCALING_FACTOR = 1.5f
        const val ZOOM_FOCAL_POINT_THRESHOLD = 20f
        
        // Momentum constants
        const val MOMENTUM_DECAY_FACTOR = 0.95f
        const val MOMENTUM_MIN_THRESHOLD = 0.1f
    }
} 