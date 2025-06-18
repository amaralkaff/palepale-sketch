package com.example.drawinggame.ui.drawing.utils

import android.graphics.PointF
import kotlin.math.sqrt

/**
 * Mathematical utilities for drawing operations.
 */
object DrawingMath {
    /**
     * Calculate the distance between two points
     */
    fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return sqrt(dx * dx + dy * dy)
    }
    
    /**
     * Calculate the distance between two points
     */
    fun distance(p1: PointF, p2: PointF): Float {
        return distance(p1.x, p1.y, p2.x, p2.y)
    }
    
    /**
     * Linear interpolation between two values
     */
    fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + (end - start) * fraction
    }
    
    /**
     * Calculate the angle between two points in radians
     */
    fun angle(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return kotlin.math.atan2(y2 - y1, x2 - x1)
    }
    
    /**
     * Constrain a value between a minimum and maximum
     */
    fun constrain(value: Float, min: Float, max: Float): Float {
        return when {
            value < min -> min
            value > max -> max
            else -> value
        }
    }
} 