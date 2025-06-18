package com.example.drawinggame.ui.drawing.operations.models

import com.example.drawinggame.ui.drawing.models.CanvasTransform
import com.example.drawinggame.ui.drawing.models.DrawingTool

/**
 * Represents a complete canvas state that can be saved and restored.
 * Contains all information needed to recreate the canvas exactly.
 */
data class CanvasState(
    // Compressed canvas bitmap data
    val canvasBitmap: ByteArray? = null,
    
    // Canvas transformation (pan, zoom)
    val canvasTransform: CanvasTransform = CanvasTransform(),
    
    // Current drawing tool
    val currentTool: DrawingTool = DrawingTool.PEN,
    
    // Canvas dimensions
    val canvasWidth: Int,
    val canvasHeight: Int,
    
    // State creation timestamp
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CanvasState

        if (canvasBitmap != null) {
            if (other.canvasBitmap == null) return false
            if (!canvasBitmap.contentEquals(other.canvasBitmap)) return false
        } else if (other.canvasBitmap != null) return false
        if (canvasTransform != other.canvasTransform) return false
        if (currentTool != other.currentTool) return false
        if (canvasWidth != other.canvasWidth) return false
        if (canvasHeight != other.canvasHeight) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = canvasBitmap?.contentHashCode() ?: 0
        result = 31 * result + canvasTransform.hashCode()
        result = 31 * result + currentTool.hashCode()
        result = 31 * result + canvasWidth
        result = 31 * result + canvasHeight
        result = 31 * result + timestamp.hashCode()
        return result
    }
} 