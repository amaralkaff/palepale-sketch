package com.example.drawinggame.ui.drawing.operations.commands

import android.graphics.Bitmap
import com.example.drawinggame.ui.drawing.DrawingEngine

/**
 * Command representing a canvas clear operation.
 * Stores the entire canvas state for undo functionality.
 */
class ClearCommand(
    private val drawingEngine: DrawingEngine
) : DrawCommand {
    
    // Store the complete canvas state before clearing
    private var previousCanvasState: Bitmap? = null
    
    override fun execute(): Boolean {
        return try {
            // Capture the current canvas state before clearing
            previousCanvasState = drawingEngine.getBitmap()?.copy(Bitmap.Config.ARGB_8888, false)
            
            // Clear the canvas immediately
            drawingEngine.clearCanvasImmediate()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    override fun undo(): Boolean {
        return try {
            // Restore the previous canvas state
            previousCanvasState?.let { bitmap ->
                drawingEngine.restoreCanvas(bitmap)
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    override fun getDescription(): String {
        return "Clear Canvas"
    }
    
    override fun getMemoryUsage(): Long {
        return previousCanvasState?.let { bitmap ->
            bitmap.width * bitmap.height * 4L // 4 bytes per pixel for ARGB
        } ?: 0L
    }
    
    override fun canMerge(other: DrawCommand): Boolean {
        // Clear commands should never be merged
        return false
    }
    
    override fun merge(other: DrawCommand): DrawCommand {
        // Clear commands cannot be merged
        return this
    }
} 