package com.example.drawinggame.ui.drawing.contracts

import android.graphics.Path
import com.example.drawinggame.ui.drawing.models.CanvasTransform
import com.example.drawinggame.ui.drawing.models.DrawingState
import com.example.drawinggame.ui.drawing.models.DrawingTool
import com.example.drawinggame.ui.drawing.models.Stroke

/**
 * Interface for listening to drawing events.
 * Used to notify external components about drawing operations.
 */
interface DrawingListener {
    /**
     * Called when a drawing stroke begins
     */
    fun onDrawingStarted(x: Float, y: Float) {}
    
    /**
     * Called continuously during drawing
     */
    fun onDrawingProgress(x: Float, y: Float) {}
    
    /**
     * Called when a drawing stroke is completed
     */
    fun onDrawingFinished() {}
    
    /**
     * Called when the canvas is cleared
     */
    fun onCanvasCleared() {}
    
    /**
     * Called when an undo operation is performed
     */
    fun onUndoPerformed() {}
    
    /**
     * Called when a redo operation is performed
     */
    fun onRedoPerformed() {}
    
    /**
     * Called when a stroke has started
     */
    fun onStrokeStarted(x: Float, y: Float, tool: DrawingTool) {}
    
    /**
     * Called when a stroke is continuing
     */
    fun onStrokeContinued(x: Float, y: Float, pressure: Float) {}
    
    /**
     * Called when a stroke has been updated with a new path
     */
    fun onStrokeUpdated(path: Path, pressure: Float) {}
    
    /**
     * Called when a stroke has finished
     */
    fun onStrokeFinished(stroke: Stroke) {}
    
    /**
     * Called when a stroke has been cancelled
     */
    fun onStrokeCancelled() {}
    
    /**
     * Called when the tool was changed
     */
    fun onToolChanged(tool: DrawingTool) {}
    
    /**
     * Called when the brush size was changed
     */
    fun onBrushSizeChanged(size: Float) {}
    
    /**
     * Called when the brush color was changed
     */
    fun onBrushColorChanged(color: Int) {}
    
    /**
     * Called when the brush alpha was changed
     */
    fun onBrushAlphaChanged(alpha: Int) {}
    
    /**
     * Called when the canvas transform was changed
     */
    fun onCanvasTransformChanged(transform: CanvasTransform) {}
    
    /**
     * Called when the state was restored
     */
    fun onStateRestored(state: DrawingState) {}
} 