package com.example.drawinggame.ui.drawing.selection.core

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import java.util.*

/**
 * Represents a selection area in the drawing canvas
 * Phase 5.3: Selection and Transformation Tools
 */
data class Selection(
    val id: String = UUID.randomUUID().toString(),
    val path: Path,
    val bounds: RectF,
    val type: SelectionType,
    val mode: SelectionMode = SelectionMode.NEW,
    var featherRadius: Float = 0f,
    val antiAlias: Boolean = true,
    val transform: Matrix = Matrix(),
    val isActive: Boolean = true,
    val marching: Boolean = true, // Marching ants animation
    val createdAt: Long = System.currentTimeMillis()
) {
    
    /**
     * Check if a point is inside the selection
     */
    fun containsPoint(x: Float, y: Float): Boolean {
        return bounds.contains(x, y)
    }
    
    /**
     * Get the selection area in square pixels
     */
    fun getArea(): Float {
        return bounds.width() * bounds.height()
    }
    
    /**
     * Create a copy of this selection
     */
    fun copy(
        newId: String = UUID.randomUUID().toString(),
        newPath: Path = Path(this.path),
        newBounds: RectF = RectF(this.bounds)
    ): Selection {
        return this.copy(
            id = newId,
            path = newPath,
            bounds = newBounds,
            createdAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Apply transformation to selection
     */
    fun applyTransform(matrix: Matrix): Selection {
        val transformedPath = Path()
        path.transform(matrix, transformedPath)
        
        val transformedBounds = RectF()
        transformedPath.computeBounds(transformedBounds, true)
        
        return this.copy(
            path = transformedPath,
            bounds = transformedBounds,
            transform = Matrix(matrix)
        )
    }
}

/**
 * Types of selection tools
 */
enum class SelectionType(val displayName: String) {
    RECTANGULAR("Rectangular"),
    ELLIPTICAL("Elliptical"),
    FREEHAND("Freehand"),
    POLYGONAL("Polygonal"),
    MAGIC_WAND("Magic Wand"),
    QUICK_SELECT("Quick Select"),
    ALL("Select All"),
    SIMILAR("Select Similar");
    
    companion object {
        fun fromDisplayName(displayName: String): SelectionType? {
            return values().find { it.displayName == displayName }
        }
    }
}

/**
 * Selection combination modes
 */
enum class SelectionMode(val displayName: String) {
    NEW("New Selection"),
    ADD("Add to Selection"),
    SUBTRACT("Subtract from Selection"),
    INTERSECT("Intersect Selection");
    
    companion object {
        fun fromDisplayName(displayName: String): SelectionMode? {
            return values().find { it.displayName == displayName }
        }
    }
}

/**
 * Selection refinement operations
 */
enum class SelectionRefinement(val displayName: String) {
    FEATHER("Feather"),
    EXPAND("Expand"),
    CONTRACT("Contract"),
    SMOOTH("Smooth"),
    BORDER("Border");
    
    companion object {
        fun fromDisplayName(displayName: String): SelectionRefinement? {
            return values().find { it.displayName == displayName }
        }
    }
} 