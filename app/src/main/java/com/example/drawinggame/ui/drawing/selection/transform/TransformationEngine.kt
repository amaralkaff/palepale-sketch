package com.example.drawinggame.ui.drawing.selection.transform

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.util.LruCache

/**
 * Handles transformations of selected content
 * Phase 5.3: Selection and Transformation Tools
 */
class TransformationEngine {
    
    private val transformCache = LruCache<String, Bitmap>(10)
    private var previewMode = false
    private var originalBitmap: Bitmap? = null
    private var previewBitmap: Bitmap? = null
    
    /**
     * Translate (move) bitmap
     */
    fun translate(bitmap: Bitmap, dx: Float, dy: Float): Bitmap {
        val matrix = Matrix().apply {
            setTranslate(dx, dy)
        }
        return applyMatrix(bitmap, matrix)
    }
    
    /**
     * Scale bitmap
     */
    fun scale(bitmap: Bitmap, scaleX: Float, scaleY: Float, pivot: PointF): Bitmap {
        val matrix = Matrix().apply {
            setScale(scaleX, scaleY, pivot.x, pivot.y)
        }
        return applyMatrix(bitmap, matrix)
    }
    
    /**
     * Rotate bitmap
     */
    fun rotate(bitmap: Bitmap, degrees: Float, pivot: PointF): Bitmap {
        val matrix = Matrix().apply {
            setRotate(degrees, pivot.x, pivot.y)
        }
        return applyMatrix(bitmap, matrix)
    }
    
    /**
     * Skew bitmap
     */
    fun skew(bitmap: Bitmap, skewX: Float, skewY: Float): Bitmap {
        val matrix = Matrix().apply {
            setSkew(skewX, skewY)
        }
        return applyMatrix(bitmap, matrix)
    }
    
    /**
     * Flip bitmap horizontally or vertically
     */
    fun flip(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
        val scaleX = if (horizontal) -1f else 1f
        val scaleY = if (vertical) -1f else 1f
        
        val matrix = Matrix().apply {
            setScale(scaleX, scaleY, bitmap.width / 2f, bitmap.height / 2f)
        }
        return applyMatrix(bitmap, matrix)
    }
    
    /**
     * Apply perspective transformation
     */
    fun perspective(bitmap: Bitmap, corners: Array<PointF>): Bitmap {
        if (corners.size != 4) {
            throw IllegalArgumentException("Perspective transformation requires exactly 4 corner points")
        }
        
        val matrix = Matrix()
        val src = floatArrayOf(
            0f, 0f,                           // Top-left
            bitmap.width.toFloat(), 0f,       // Top-right
            bitmap.width.toFloat(), bitmap.height.toFloat(), // Bottom-right
            0f, bitmap.height.toFloat()       // Bottom-left
        )
        
        val dst = floatArrayOf(
            corners[0].x, corners[0].y,  // Top-left
            corners[1].x, corners[1].y,  // Top-right
            corners[2].x, corners[2].y,  // Bottom-right
            corners[3].x, corners[3].y   // Bottom-left
        )
        
        matrix.setPolyToPoly(src, 0, dst, 0, 4)
        return applyMatrix(bitmap, matrix)
    }
    
    /**
     * Apply warp transformation using mesh points
     */
    fun warp(bitmap: Bitmap, meshPoints: Array<PointF>): Bitmap {
        // TODO: Implement mesh-based warping
        // For now, return the original bitmap
        return bitmap
    }
    
    /**
     * Apply liquify distortions
     */
    fun liquify(bitmap: Bitmap, distortions: List<LiquifyDistortion>): Bitmap {
        // TODO: Implement liquify distortions
        // For now, return the original bitmap
        return bitmap
    }
    
    /**
     * Start preview mode
     */
    fun startPreview(transformation: TransformData) {
        previewMode = true
        originalBitmap = transformation.sourceBitmap
    }
    
    /**
     * Update preview with new transformation
     */
    fun updatePreview(transformation: TransformData): Bitmap? {
        if (!previewMode || originalBitmap == null) return null
        
        previewBitmap = applyTransformation(originalBitmap!!, transformation)
        return previewBitmap
    }
    
    /**
     * Commit preview transformation
     */
    fun commitPreview(): Bitmap? {
        if (!previewMode) return null
        
        val result = previewBitmap
        cancelPreview()
        return result
    }
    
    /**
     * Cancel preview transformation
     */
    fun cancelPreview() {
        previewMode = false
        previewBitmap?.recycle()
        previewBitmap = null
        originalBitmap = null
    }
    
    /**
     * Apply complete transformation data
     */
    private fun applyTransformation(bitmap: Bitmap, transformation: TransformData): Bitmap {
        var result = bitmap
        
        // Apply transformations in order
        transformation.translate?.let { (dx, dy) ->
            result = translate(result, dx, dy)
        }
        
        transformation.scale?.let { (scaleX, scaleY, pivot) ->
            result = scale(result, scaleX, scaleY, pivot)
        }
        
        transformation.rotation?.let { (degrees, pivot) ->
            result = rotate(result, degrees, pivot)
        }
        
        transformation.skew?.let { (skewX, skewY) ->
            result = skew(result, skewX, skewY)
        }
        
        transformation.flip?.let { (horizontal, vertical) ->
            result = flip(result, horizontal, vertical)
        }
        
        transformation.perspective?.let { corners ->
            result = perspective(result, corners)
        }
        
        return result
    }
    
    /**
     * Apply matrix transformation to bitmap
     */
    private fun applyMatrix(bitmap: Bitmap, matrix: Matrix): Bitmap {
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        transformCache.evictAll()
        previewBitmap?.recycle()
        previewBitmap = null
        originalBitmap = null
        previewMode = false
    }
}

/**
 * Data class for transformation parameters
 */
data class TransformData(
    val sourceBitmap: Bitmap,
    val translate: Pair<Float, Float>? = null,
    val scale: Triple<Float, Float, PointF>? = null,
    val rotation: Pair<Float, PointF>? = null,
    val skew: Pair<Float, Float>? = null,
    val flip: Pair<Boolean, Boolean>? = null,
    val perspective: Array<PointF>? = null
)

/**
 * Liquify distortion data
 */
data class LiquifyDistortion(
    val center: PointF,
    val radius: Float,
    val strength: Float,
    val type: LiquifyType
)

/**
 * Types of liquify distortions
 */
enum class LiquifyType {
    PUSH,
    PULL,
    TWIRL_CLOCKWISE,
    TWIRL_COUNTER_CLOCKWISE,
    BLOAT,
    PUCKER
} 