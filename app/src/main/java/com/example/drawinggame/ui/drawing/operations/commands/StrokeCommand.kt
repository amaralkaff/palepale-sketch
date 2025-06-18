package com.example.drawinggame.ui.drawing.operations.commands

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import com.example.drawinggame.ui.drawing.DrawingEngine
import com.example.drawinggame.ui.drawing.models.DrawingTool
import com.example.drawinggame.ui.drawing.models.Stroke

/**
 * Command representing a single drawing stroke.
 * Stores all necessary information to execute and undo the stroke.
 */
class StrokeCommand(
    private val stroke: Stroke,
    private val drawingEngine: DrawingEngine,
    private val canvasBounds: Rect
) : DrawCommand {
    
    // Store the canvas state before this stroke for undo
    private var previousCanvasRegion: Bitmap? = null
    private val affectedRegion = Rect()
    
    // Timestamp for command merging decisions
    private val timestamp = System.currentTimeMillis()
    
    // Maximum time between strokes to allow merging (in milliseconds)
    private val mergeTimeThreshold = 500L
    
    init {
        calculateAffectedRegion()
    }
    
    override fun execute(): Boolean {
        return try {
            // Capture the affected region before drawing
            captureCanvasRegion()
            
            // Execute the stroke through the drawing engine
            drawingEngine.startStroke(
                stroke.points.firstOrNull()?.x ?: 0f,
                stroke.points.firstOrNull()?.y ?: 0f,
                stroke.tool
            )
            
            // Add all points to the stroke
            for (i in 1 until stroke.points.size) {
                val point = stroke.points[i]
                drawingEngine.addPointToStroke(point.x, point.y, point.pressure)
            }
            
            // Finalize the stroke
            drawingEngine.finishStroke(stroke.path)
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    override fun undo(): Boolean {
        return try {
            // Restore the previous canvas region
            previousCanvasRegion?.let { bitmap ->
                val canvas = Canvas(drawingEngine.getBitmap() ?: return false)
                canvas.drawBitmap(bitmap, affectedRegion, affectedRegion, null)
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    override fun getDescription(): String {
        return when (stroke.tool) {
            DrawingTool.PEN -> "Pen Stroke"
            DrawingTool.BRUSH -> "Brush Stroke" 
            DrawingTool.ERASER -> "Eraser Stroke"
        }
    }
    
    override fun getMemoryUsage(): Long {
        val bitmapSize = previousCanvasRegion?.let { bitmap ->
            bitmap.width * bitmap.height * 4L // 4 bytes per pixel for ARGB
        } ?: 0L
        
        val pathSize = stroke.points.size * 12L // Estimated 12 bytes per point (x, y, pressure)
        
        return bitmapSize + pathSize + 200L // Additional overhead
    }
    
    override fun canMerge(other: DrawCommand): Boolean {
        if (other !is StrokeCommand) return false
        
        // Only merge strokes with the same tool
        if (stroke.tool != other.stroke.tool) return false
        
        // Only merge if strokes are close in time
        val timeDiff = kotlin.math.abs(timestamp - other.timestamp)
        if (timeDiff > mergeTimeThreshold) return false
        
        // Only merge if strokes are spatially close
        val distance = calculateDistanceBetweenStrokes(other)
        if (distance > 50f) return false // 50 pixels threshold
        
        return true
    }
    
    override fun merge(other: DrawCommand): DrawCommand {
        if (other !is StrokeCommand || !canMerge(other)) {
            return this
        }
        
        // Create a merged stroke by combining paths
        val mergedPoints = mutableListOf<Stroke.StrokePoint>()
        mergedPoints.addAll(stroke.points)
        mergedPoints.addAll(other.stroke.points)
        
        val mergedPath = Path()
        mergedPath.addPath(stroke.path)
        mergedPath.addPath(other.stroke.path)
        
        val mergedStroke = Stroke(
            tool = stroke.tool,
            color = stroke.color,
            size = stroke.size,
            alpha = stroke.alpha,
            path = mergedPath,
            points = mergedPoints
        )
        
        return StrokeCommand(mergedStroke, drawingEngine, canvasBounds)
    }
    
    /**
     * Calculate the region of the canvas affected by this stroke
     */
    private fun calculateAffectedRegion() {
        val pathBounds = RectF()
        stroke.path.computeBounds(pathBounds, true)
        val strokeWidth = stroke.size
        val padding = (strokeWidth / 2f).toInt() + 10 // Extra padding for safety
        
        affectedRegion.set(
            (pathBounds.left - padding).toInt().coerceAtLeast(0),
            (pathBounds.top - padding).toInt().coerceAtLeast(0),
            (pathBounds.right + padding).toInt().coerceAtMost(canvasBounds.width()),
            (pathBounds.bottom + padding).toInt().coerceAtMost(canvasBounds.height())
        )
    }
    
    /**
     * Capture the canvas region that will be affected by this stroke
     */
    private fun captureCanvasRegion() {
        val canvasBitmap = drawingEngine.getBitmap() ?: return
        
        try {
            // Create a bitmap for the affected region
            val regionWidth = affectedRegion.width()
            val regionHeight = affectedRegion.height()
            
            if (regionWidth > 0 && regionHeight > 0) {
                previousCanvasRegion = Bitmap.createBitmap(
                    canvasBitmap,
                    affectedRegion.left,
                    affectedRegion.top,
                    regionWidth,
                    regionHeight
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            previousCanvasRegion = null
        }
    }
    
    /**
     * Calculate the distance between this stroke and another stroke
     */
    private fun calculateDistanceBetweenStrokes(other: StrokeCommand): Float {
        val thisBounds = RectF()
        val otherBounds = RectF()
        
        stroke.path.computeBounds(thisBounds, true)
        other.stroke.path.computeBounds(otherBounds, true)
        
        val thisCenterX = thisBounds.centerX()
        val thisCenterY = thisBounds.centerY()
        val otherCenterX = otherBounds.centerX()
        val otherCenterY = otherBounds.centerY()
        
        val dx = thisCenterX - otherCenterX
        val dy = thisCenterY - otherCenterY
        
        return kotlin.math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }
} 