package com.example.drawinggame.ui.drawing.models

import android.graphics.Path
import android.graphics.Paint

/**
 * Represents a single drawing stroke on the canvas.
 * Contains all information needed to render the stroke.
 */
data class Stroke(
    // The drawing tool used for this stroke
    val tool: DrawingTool,
    
    // Stroke visual properties
    val color: Int,
    var size: Float,
    var alpha: Int,
    
    // The path of the stroke
    var path: Path,
    
    // Individual points that make up the stroke
    val points: MutableList<StrokePoint> = mutableListOf(),
    
    // Timestamp when the stroke was created
    val timestamp: Long = System.currentTimeMillis(),
    
    // Advanced brush paint for Phase 5.1 (nullable for backward compatibility)
    var customPaint: Paint? = null
) {
    /**
     * Represents a single point in a stroke with pressure information
     */
    data class StrokePoint(
        val x: Float,
        val y: Float,
        val pressure: Float = 1.0f,
        val timestamp: Long = System.currentTimeMillis()
    )
} 