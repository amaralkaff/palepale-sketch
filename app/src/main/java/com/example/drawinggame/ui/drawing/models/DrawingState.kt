package com.example.drawinggame.ui.drawing.models

import android.graphics.Color

/**
 * Represents the current state of the drawing canvas.
 */
data class DrawingState(
    // Current selected tool
    val currentTool: DrawingTool = DrawingTool.PEN,
    
    // Current brush properties
    val brushSize: Float = 12f,
    val currentColor: Int = Color.BLACK,
    val opacity: Int = 255, // 0-255
    
    // Canvas transformation
    val zoomLevel: Float = 1.0f,
    val panOffsetX: Float = 0f,
    val panOffsetY: Float = 0f,
    
    // Canvas dimensions
    val canvasWidth: Int = 0,
    val canvasHeight: Int = 0
)

/**
 * Drawing tool types supported by the application.
 */
enum class DrawingTool {
    PEN,
    BRUSH,
    ERASER
} 