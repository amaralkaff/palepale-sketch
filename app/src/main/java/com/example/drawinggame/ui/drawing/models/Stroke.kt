package com.example.drawinggame.ui.drawing.models

import android.graphics.Path

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
    val alpha: Int,
    
    // The path of the stroke
    var path: Path,
    
    // Individual points that make up the stroke
    val points: MutableList<StrokePoint> = mutableListOf(),
    
    // Timestamp when the stroke was created
    val timestamp: Long = System.currentTimeMillis()
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