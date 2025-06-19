package com.example.drawinggame.ui.drawing.selection.tools

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Bitmap
import android.view.View
import com.example.drawinggame.ui.drawing.selection.core.Selection
import com.example.drawinggame.ui.drawing.selection.core.SelectionType
import com.example.drawinggame.ui.drawing.selection.algorithms.MagicWandAlgorithm

/**
 * Base class for selection tools
 * Phase 5.3: Selection and Transformation Tools
 */
abstract class SelectionTool {
    abstract val type: SelectionType
    abstract val cursor: Int
    
    protected var __isActive = false
    protected var startX = 0f
    protected var startY = 0f
    protected var currentX = 0f
    protected var currentY = 0f
    
    /**
     * Start selection operation
     */
    abstract fun startSelection(x: Float, y: Float, canvas: Canvas)
    
    /**
     * Update selection during drag
     */
    abstract fun updateSelection(x: Float, y: Float, canvas: Canvas)
    
    /**
     * Finish selection operation
     */
    abstract fun finishSelection(canvas: Canvas): Selection?
    
    /**
     * Cancel selection operation
     */
    open fun cancelSelection() {
        setActive(false)
    }
    
    /**
     * Get tool-specific settings panel
     */
    abstract fun getSettingsPanel(): View?
    
    /**
     * Apply tool settings
     */
    abstract fun applySettings(settings: Map<String, Any>)
    
    /**
     * Check if tool is currently active
     */
    fun isActive(): Boolean = __isActive
    
    /**
     * Set active state
     */
    protected fun setActive(active: Boolean) {
        __isActive = active
    }
}

/**
 * Rectangular selection tool
 */
class RectangularSelectionTool : SelectionTool() {
    override val type = SelectionType.RECTANGULAR
    override val cursor = android.R.drawable.ic_menu_crop
    
    private var selectionPath = Path()
    
    override fun startSelection(x: Float, y: Float, canvas: Canvas) {
        setActive(true)
        startX = x
        startY = y
        currentX = x
        currentY = y
        
        selectionPath.reset()
    }
    
    override fun updateSelection(x: Float, y: Float, canvas: Canvas) {
        if (!isActive()) return
        
        currentX = x
        currentY = y
        
        // Create rectangular path
        selectionPath.reset()
        val left = minOf(startX, currentX)
        val top = minOf(startY, currentY)
        val right = maxOf(startX, currentX)
        val bottom = maxOf(startY, currentY)
        
        selectionPath.addRect(left, top, right, bottom, Path.Direction.CW)
    }
    
    override fun finishSelection(canvas: Canvas): Selection? {
        if (!isActive()) return null
        
        setActive(false)
        
        val bounds = RectF()
        selectionPath.computeBounds(bounds, true)
        
        // Check if selection is large enough
        if (bounds.width() < 2 || bounds.height() < 2) {
            return null
        }
        
        return Selection(
            path = Path(selectionPath),
            bounds = bounds,
            type = type
        )
    }
    
    override fun getSettingsPanel(): View? = null
    
    override fun applySettings(settings: Map<String, Any>) {
        // No specific settings for rectangular selection
    }
    
    /**
     * Create selection with specific bounds
     */
    fun createSelection(left: Float, top: Float, right: Float, bottom: Float): Path {
        val path = Path()
        path.addRect(left, top, right, bottom, Path.Direction.CW)
        return path
    }
}

/**
 * Elliptical selection tool
 */
class EllipticalSelectionTool : SelectionTool() {
    override val type = SelectionType.ELLIPTICAL
    override val cursor = android.R.drawable.ic_menu_crop
    
    private var selectionPath = Path()
    
    override fun startSelection(x: Float, y: Float, canvas: Canvas) {
        setActive(true)
        startX = x
        startY = y
        currentX = x
        currentY = y
        
        selectionPath.reset()
    }
    
    override fun updateSelection(x: Float, y: Float, canvas: Canvas) {
        if (!isActive()) return
        
        currentX = x
        currentY = y
        
        // Create elliptical path
        selectionPath.reset()
        val left = minOf(startX, currentX)
        val top = minOf(startY, currentY)
        val right = maxOf(startX, currentX)
        val bottom = maxOf(startY, currentY)
        
        selectionPath.addOval(left, top, right, bottom, Path.Direction.CW)
    }
    
    override fun finishSelection(canvas: Canvas): Selection? {
        if (!isActive()) return null
        
        setActive(false)
        
        val bounds = RectF()
        selectionPath.computeBounds(bounds, true)
        
        // Check if selection is large enough
        if (bounds.width() < 2 || bounds.height() < 2) {
            return null
        }
        
        return Selection(
            path = Path(selectionPath),
            bounds = bounds,
            type = type
        )
    }
    
    override fun getSettingsPanel(): View? = null
    
    override fun applySettings(settings: Map<String, Any>) {
        // No specific settings for elliptical selection
    }
    
    /**
     * Create elliptical selection with specific bounds
     */
    fun createSelection(left: Float, top: Float, right: Float, bottom: Float): Path {
        val path = Path()
        path.addOval(left, top, right, bottom, Path.Direction.CW)
        return path
    }
}

/**
 * Freehand selection tool
 */
class FreehandSelectionTool : SelectionTool() {
    override val type = SelectionType.FREEHAND
    override val cursor = android.R.drawable.ic_menu_edit
    
    private var selectionPath = Path()
    
    override fun startSelection(x: Float, y: Float, canvas: Canvas) {
        setActive(true)
        startX = x
        startY = y
        
        selectionPath.reset()
        selectionPath.moveTo(x, y)
    }
    
    override fun updateSelection(x: Float, y: Float, canvas: Canvas) {
        if (!isActive()) return
        
        currentX = x
        currentY = y
        
        selectionPath.lineTo(x, y)
    }
    
    override fun finishSelection(canvas: Canvas): Selection? {
        if (!isActive()) return null
        
        setActive(false)
        
        // Close the path
        selectionPath.close()
        
        val bounds = RectF()
        selectionPath.computeBounds(bounds, true)
        
        // Check if selection is large enough
        if (bounds.width() < 2 || bounds.height() < 2) {
            return null
        }
        
        return Selection(
            path = Path(selectionPath),
            bounds = bounds,
            type = type
        )
    }
    
    override fun getSettingsPanel(): View? = null
    
    override fun applySettings(settings: Map<String, Any>) {
        // No specific settings for freehand selection
    }
    
    /**
     * Create freehand selection from points
     */
    fun createSelectionFromPoints(points: List<android.graphics.PointF>): Path {
        val path = Path()
        if (points.isNotEmpty()) {
            path.moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                path.lineTo(points[i].x, points[i].y)
            }
            path.close()
        }
        return path
    }
}

/**
 * Magic wand selection tool
 */
class MagicWandTool : SelectionTool() {
    override val type = SelectionType.MAGIC_WAND
    override val cursor = android.R.drawable.ic_menu_search
    
    private var tolerance = 32 // Color tolerance (0-255)
    private var contiguous = true // Only select connected pixels
    private val magicWandAlgorithm = MagicWandAlgorithm()
    
    override fun startSelection(x: Float, y: Float, canvas: Canvas) {
        setActive(true)
        startX = x
        startY = y
    }
    
    override fun updateSelection(x: Float, y: Float, canvas: Canvas) {
        // Magic wand doesn't update during drag
    }
    
    override fun finishSelection(canvas: Canvas): Selection? {
        if (!isActive()) return null
        
        setActive(false)
        
        // Get canvas bitmap for magic wand algorithm
        val bitmap = getCanvasBitmap(canvas)
        if (bitmap == null) {
            // Fallback to small rectangular selection
            val size = 10f
            val path = Path().apply {
                addRect(
                    startX - size, startY - size,
                    startX + size, startY + size,
                    Path.Direction.CW
                )
            }
            
            val bounds = RectF()
            path.computeBounds(bounds, true)
            
            return Selection(
                path = path,
                bounds = bounds,
                type = type
            )
        }
        
        // Perform magic wand selection
        val selectionPath = magicWandAlgorithm.performSelection(
            bitmap = bitmap,
            startX = startX.toInt(),
            startY = startY.toInt(),
            tolerance = tolerance,
            contiguous = contiguous
        )
        
        val bounds = RectF()
        selectionPath.computeBounds(bounds, true)
        
        // Check if selection is valid
        if (bounds.width() < 1 || bounds.height() < 1) {
            return null
        }
        
        return Selection(
            path = selectionPath,
            bounds = bounds,
            type = type
        )
    }
    
    private fun getCanvasBitmap(canvas: Canvas): Bitmap? {
        return try {
            // Try to get the canvas bitmap
            // This is a simplified approach - in a real implementation,
            // you would need to get the actual drawing surface bitmap
            val width = canvas.width
            val height = canvas.height
            
            if (width <= 0 || height <= 0) return null
            
            // Create a bitmap from the canvas (simplified)
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        } catch (e: Exception) {
            null
        }
    }
    
    override fun getSettingsPanel(): View? = null
    
    override fun applySettings(settings: Map<String, Any>) {
        tolerance = (settings["tolerance"] as? Int) ?: tolerance
        contiguous = (settings["contiguous"] as? Boolean) ?: contiguous
    }
} 
