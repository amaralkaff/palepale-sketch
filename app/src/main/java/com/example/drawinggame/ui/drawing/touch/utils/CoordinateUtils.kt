package com.example.drawinggame.ui.drawing.touch.utils

import android.content.Context
import android.graphics.PointF
import android.util.DisplayMetrics
import android.util.TypedValue
import com.example.drawinggame.ui.drawing.models.CanvasTransform

/**
 * Utility class for coordinate transformations and conversions.
 */
object CoordinateUtils {
    
    /**
     * Convert dp to pixels
     */
    fun dpToPx(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )
    }
    
    /**
     * Convert pixels to dp
     */
    fun pxToDp(context: Context, px: Float): Float {
        val metrics = context.resources.displayMetrics
        return px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }
    
    /**
     * Transform screen coordinates to canvas coordinates
     */
    fun screenToCanvasCoordinates(
        screenX: Float,
        screenY: Float,
        transform: CanvasTransform
    ): PointF {
        // Apply inverse of canvas transformation
        val canvasX = (screenX - transform.panOffsetX) / transform.zoomLevel
        val canvasY = (screenY - transform.panOffsetY) / transform.zoomLevel
        return PointF(canvasX, canvasY)
    }
    
    /**
     * Transform canvas coordinates to screen coordinates
     */
    fun canvasToScreenCoordinates(
        canvasX: Float,
        canvasY: Float,
        transform: CanvasTransform
    ): PointF {
        // Apply canvas transformation
        val screenX = canvasX * transform.zoomLevel + transform.panOffsetX
        val screenY = canvasY * transform.zoomLevel + transform.panOffsetY
        return PointF(screenX, screenY)
    }
    
    /**
     * Calculate distance between two points
     */
    fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }
    
    /**
     * Calculate angle between two points (in radians)
     */
    fun angle(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return Math.atan2((y2 - y1).toDouble(), (x2 - x1).toDouble()).toFloat()
    }
    
    /**
     * Calculate midpoint between two points
     */
    fun midpoint(x1: Float, y1: Float, x2: Float, y2: Float): PointF {
        return PointF((x1 + x2) / 2, (y1 + y2) / 2)
    }
    
    /**
     * Check if a point is within bounds
     */
    fun isPointWithinBounds(
        x: Float,
        y: Float,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ): Boolean {
        return x >= left && x <= right && y >= top && y <= bottom
    }
    
    /**
     * Calculate velocity from distance and time
     */
    fun calculateVelocity(distance: Float, timeMs: Long): Float {
        return if (timeMs > 0) distance / timeMs * 1000 else 0f
    }
    
    /**
     * Apply friction to velocity
     */
    fun applyFriction(velocity: Float, frictionFactor: Float): Float {
        return velocity * frictionFactor
    }
    
    /**
     * Calculate bounding box for a set of points
     */
    fun calculateBoundingBox(points: List<PointF>): Array<Float> {
        if (points.isEmpty()) {
            return arrayOf(0f, 0f, 0f, 0f) // left, top, right, bottom
        }
        
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE
        
        for (point in points) {
            minX = Math.min(minX, point.x)
            minY = Math.min(minY, point.y)
            maxX = Math.max(maxX, point.x)
            maxY = Math.max(maxY, point.y)
        }
        
        return arrayOf(minX, minY, maxX, maxY)
    }
} 