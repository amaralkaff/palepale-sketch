package com.example.drawinggame.ui.drawing.touch.utils

import android.graphics.Path
import android.graphics.PointF
import com.example.drawinggame.ui.drawing.models.DrawingTool
import com.example.drawinggame.ui.drawing.touch.gestures.GestureConstants
import kotlin.math.abs

/**
 * Utility class for smoothing and optimizing drawing paths.
 */
object PathSmoothingUtils {
    
    /**
     * Apply path smoothing to a list of points based on drawing tool
     * Returns a new smoothed path
     */
    fun smoothPath(points: List<PointF>, tool: DrawingTool): Path {
        val path = Path()
        if (points.isEmpty()) return path
        
        // Get smoothing factor based on tool
        val smoothingFactor = when (tool) {
            DrawingTool.PEN -> GestureConstants.DrawingToolConstants.PEN_SMOOTHING_FACTOR
            DrawingTool.BRUSH -> GestureConstants.DrawingToolConstants.BRUSH_SMOOTHING_FACTOR
            DrawingTool.ERASER -> GestureConstants.DrawingToolConstants.ERASER_SMOOTHING_FACTOR
        }
        
        // Apply appropriate smoothing algorithm
        return when {
            points.size < 3 -> createSimplePath(points)
            smoothingFactor < 0.1f -> createSimplePath(points) // Minimal smoothing
            smoothingFactor < 0.3f -> createBezierPath(points)
            else -> createCatmullRomPath(points, smoothingFactor)
        }
    }
    
    /**
     * Create a simple path without smoothing
     */
    private fun createSimplePath(points: List<PointF>): Path {
        val path = Path()
        if (points.isEmpty()) return path
        
        path.moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            path.lineTo(points[i].x, points[i].y)
        }
        
        return path
    }
    
    /**
     * Create a path using quadratic bezier curves for smoothing
     */
    private fun createBezierPath(points: List<PointF>): Path {
        val path = Path()
        if (points.isEmpty()) return path
        
        path.moveTo(points[0].x, points[0].y)
        
        if (points.size == 2) {
            path.lineTo(points[1].x, points[1].y)
            return path
        }
        
        // Use quadratic bezier curves for smoothing
        for (i in 1 until points.size - 1) {
            val midX = (points[i].x + points[i + 1].x) / 2
            val midY = (points[i].y + points[i + 1].y) / 2
            
            path.quadTo(
                points[i].x, points[i].y,
                midX, midY
            )
        }
        
        // Add the last point
        path.lineTo(points.last().x, points.last().y)
        
        return path
    }
    
    /**
     * Create a path using Catmull-Rom spline interpolation for smooth curves
     * This provides higher quality smoothing for artistic drawing
     */
    private fun createCatmullRomPath(points: List<PointF>, alpha: Float): Path {
        val path = Path()
        if (points.size < 4) return createBezierPath(points)
        
        path.moveTo(points[0].x, points[0].y)
        
        // Create a temporary list with duplicated endpoints for better curve fitting
        val tempPoints = ArrayList<PointF>()
        tempPoints.add(points[0])
        tempPoints.addAll(points)
        tempPoints.add(points.last())
        
        // Generate Catmull-Rom spline segments
        for (i in 0 until tempPoints.size - 3) {
            val p0 = tempPoints[i]
            val p1 = tempPoints[i + 1]
            val p2 = tempPoints[i + 2]
            val p3 = tempPoints[i + 3]
            
            // Add curve segments with interpolation steps
            val steps = 10
            for (j in 0 until steps) {
                val t = j.toFloat() / steps
                val point = catmullRomPoint(p0, p1, p2, p3, alpha, t)
                path.lineTo(point.x, point.y)
            }
        }
        
        return path
    }
    
    /**
     * Calculate a point on a Catmull-Rom spline
     */
    private fun catmullRomPoint(
        p0: PointF, p1: PointF, p2: PointF, p3: PointF,
        alpha: Float, t: Float
    ): PointF {
        // Catmull-Rom spline formula
        val t2 = t * t
        val t3 = t2 * t
        
        val b0 = -alpha * t + 2 * alpha * t2 - alpha * t3
        val b1 = 1 + (alpha - 3) * t2 + (2 - alpha) * t3
        val b2 = alpha * t + (3 - 2 * alpha) * t2 + (alpha - 2) * t3
        val b3 = -alpha * t2 + alpha * t3
        
        val x = b0 * p0.x + b1 * p1.x + b2 * p2.x + b3 * p3.x
        val y = b0 * p0.y + b1 * p1.y + b2 * p2.y + b3 * p3.y
        
        return PointF(x, y)
    }
    
    /**
     * Optimize a path by removing redundant points
     * Uses the Douglas-Peucker algorithm for path simplification
     */
    fun optimizePath(points: List<PointF>, epsilon: Float = 1.0f): List<PointF> {
        if (points.size <= 2) return points
        
        val result = ArrayList<PointF>()
        douglasPeucker(points, 0, points.size - 1, epsilon, result)
        
        // Sort the result by the original point order
        result.sortBy { points.indexOf(it) }
        
        return result
    }
    
    /**
     * Douglas-Peucker recursive algorithm for path simplification
     */
    private fun douglasPeucker(
        points: List<PointF>,
        startIdx: Int,
        endIdx: Int,
        epsilon: Float,
        result: MutableList<PointF>
    ) {
        // Base case
        if (endIdx <= startIdx + 1) {
            result.add(points[startIdx])
            result.add(points[endIdx])
            return
        }
        
        var maxDistance = 0f
        var maxDistanceIdx = 0
        
        // Find point with maximum distance from line segment
        val startPoint = points[startIdx]
        val endPoint = points[endIdx]
        
        for (i in startIdx + 1 until endIdx) {
            val distance = perpendicularDistance(points[i], startPoint, endPoint)
            if (distance > maxDistance) {
                maxDistance = distance
                maxDistanceIdx = i
            }
        }
        
        // If max distance is greater than epsilon, recursively simplify
        if (maxDistance > epsilon) {
            douglasPeucker(points, startIdx, maxDistanceIdx, epsilon, result)
            douglasPeucker(points, maxDistanceIdx, endIdx, epsilon, result)
        } else {
            // Discard all points between start and end
            result.add(startPoint)
            result.add(endPoint)
        }
    }
    
    /**
     * Calculate perpendicular distance from a point to a line segment
     */
    private fun perpendicularDistance(point: PointF, lineStart: PointF, lineEnd: PointF): Float {
        val dx = lineEnd.x - lineStart.x
        val dy = lineEnd.y - lineStart.y
        
        // If line is just a point, return distance to that point
        val lineLengthSquared = dx * dx + dy * dy
        if (lineLengthSquared == 0f) {
            val distX = point.x - lineStart.x
            val distY = point.y - lineStart.y
            return Math.sqrt((distX * distX + distY * distY).toDouble()).toFloat()
        }
        
        // Calculate projection factor
        val t = ((point.x - lineStart.x) * dx + (point.y - lineStart.y) * dy) / lineLengthSquared
        
        if (t < 0) {
            // Point is beyond lineStart
            val distX = point.x - lineStart.x
            val distY = point.y - lineStart.y
            return Math.sqrt((distX * distX + distY * distY).toDouble()).toFloat()
        }
        
        if (t > 1) {
            // Point is beyond lineEnd
            val distX = point.x - lineEnd.x
            val distY = point.y - lineEnd.y
            return Math.sqrt((distX * distX + distY * distY).toDouble()).toFloat()
        }
        
        // Perpendicular distance
        val projX = lineStart.x + t * dx
        val projY = lineStart.y + t * dy
        val distX = point.x - projX
        val distY = point.y - projY
        
        return Math.sqrt((distX * distX + distY * distY).toDouble()).toFloat()
    }
} 