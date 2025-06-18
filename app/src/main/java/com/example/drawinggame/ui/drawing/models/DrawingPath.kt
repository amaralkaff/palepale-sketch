package com.example.drawinggame.ui.drawing.models

import android.graphics.Paint
import android.graphics.Path

/**
 * Represents an individual drawing path with its properties.
 * Used for maintaining drawing history and undo/redo operations.
 */
data class DrawingPath(
    // The actual path geometry
    val path: Path,
    
    // Paint properties for this path
    val paint: Paint,
    
    // Metadata
    val timestamp: Long,
    
    // Unique identifier for this path (for undo/redo operations)
    val id: String = timestamp.toString()
) 