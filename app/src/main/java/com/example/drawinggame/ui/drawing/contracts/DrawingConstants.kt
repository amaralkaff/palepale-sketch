package com.example.drawinggame.ui.drawing.contracts

import android.graphics.Color

/**
 * Constants used throughout the drawing system.
 */
object DrawingConstants {
    // Brush defaults
    const val DEFAULT_BRUSH_SIZE = 12f
    const val MAX_BRUSH_SIZE = 100f
    const val MIN_BRUSH_SIZE = 1f
    const val DEFAULT_OPACITY = 255 // Full opacity
    
    // Default colors
    val DEFAULT_COLOR = Color.BLACK
    val ERASER_COLOR = Color.WHITE
    
    // Canvas defaults
    const val DEFAULT_BACKGROUND_COLOR = Color.WHITE
    const val MAX_ZOOM_LEVEL = 5.0f
    const val MIN_ZOOM_LEVEL = 0.5f
    
    // Performance settings
    const val MAX_UNDO_STEPS = 50
    const val PATH_OPTIMIZATION_THRESHOLD = 5 // Optimize paths with more than this many points
    
    // Touch settings
    const val TOUCH_TOLERANCE = 4f // Minimum distance for registering movement
    const val GESTURE_SLOP = 10f // Distance for recognizing a gesture vs a drawing
} 