package com.example.drawinggame.ui.drawing.selection.core

import android.graphics.Path
import android.graphics.RectF
import com.example.drawinggame.ui.drawing.selection.tools.SelectionTool
import com.example.drawinggame.ui.drawing.selection.tools.RectangularSelectionTool
import com.example.drawinggame.ui.drawing.selection.tools.EllipticalSelectionTool
import com.example.drawinggame.ui.drawing.selection.tools.FreehandSelectionTool
import com.example.drawinggame.ui.drawing.selection.tools.MagicWandTool
import java.util.*

/**
 * Manages selection operations and tools
 * Phase 5.3: Selection & Transformation Tools implementation
 */
class SelectionManager {
    
    private val selections = mutableMapOf<String, Selection>()
    private var activeSelectionId: String? = null
    private var activeTool: SelectionTool? = null
    private val selectionListeners = mutableListOf<SelectionListener>()
    
    // Selection tools
    private val rectangularTool = RectangularSelectionTool()
    private val ellipticalTool = EllipticalSelectionTool()
    private val freehandTool = FreehandSelectionTool()
    private val magicWandTool = MagicWandTool()
    
    /**
     * Set the active selection tool
     */
    fun setActiveTool(toolType: SelectionType) {
        activeTool = when (toolType) {
            SelectionType.RECTANGULAR -> rectangularTool
            SelectionType.ELLIPTICAL -> ellipticalTool
            SelectionType.FREEHAND -> freehandTool
            SelectionType.MAGIC_WAND -> magicWandTool
            SelectionType.POLYGONAL -> freehandTool // Use freehand for polygonal for now
            SelectionType.QUICK_SELECT -> magicWandTool // Use magic wand for quick select
            SelectionType.ALL -> rectangularTool // Use rectangular for select all
            SelectionType.SIMILAR -> magicWandTool // Use magic wand for similar
        }
    }
    
    /**
     * Create rectangular selection
     */
    fun createRectangularSelection(left: Float, top: Float, right: Float, bottom: Float): Selection {
        val selection = Selection(
            id = UUID.randomUUID().toString(),
            type = SelectionType.RECTANGULAR,
            path = rectangularTool.createSelection(left, top, right, bottom),
            bounds = RectF(left, top, right, bottom)
        )
        
        selections[selection.id] = selection
        activeSelectionId = selection.id
        notifySelectionCreated(selection)
        
        return selection
    }
    
    /**
     * Create elliptical selection
     */
    fun createEllipticalSelection(left: Float, top: Float, right: Float, bottom: Float): Selection {
        val selection = Selection(
            id = UUID.randomUUID().toString(),
            type = SelectionType.ELLIPTICAL,
            path = ellipticalTool.createSelection(left, top, right, bottom),
            bounds = RectF(left, top, right, bottom)
        )
        
        selections[selection.id] = selection
        activeSelectionId = selection.id
        notifySelectionCreated(selection)
        
        return selection
    }
    
    /**
     * Create freehand selection
     */
    fun createFreehandSelection(points: List<android.graphics.PointF>): Selection {
        val path = freehandTool.createSelectionFromPoints(points)
        val bounds = RectF()
        path.computeBounds(bounds, true)
        
        val selection = Selection(
            id = UUID.randomUUID().toString(),
            type = SelectionType.FREEHAND,
            path = path,
            bounds = bounds
        )
        
        selections[selection.id] = selection
        activeSelectionId = selection.id
        notifySelectionCreated(selection)
        
        return selection
    }
    
    /**
     * Get selection by ID
     */
    fun getSelection(selectionId: String): Selection? {
        return selections[selectionId]
    }
    
    /**
     * Get current active selection
     */
    fun getCurrentSelection(): Selection? {
        return activeSelectionId?.let { selections[it] }
    }
    
    /**
     * Set active selection
     */
    fun setActiveSelection(selectionId: String) {
        if (selections.containsKey(selectionId)) {
            activeSelectionId = selectionId
            notifyActiveSelectionChanged(selectionId)
        }
    }
    
    /**
     * Clear current selection
     */
    fun clearSelection() {
        activeSelectionId?.let { id ->
            selections.remove(id)
            activeSelectionId = null
            notifySelectionCleared(id)
        }
    }
    
    /**
     * Select all
     */
    fun selectAll(canvasWidth: Float = 1000f, canvasHeight: Float = 1000f) {
        val selection = createRectangularSelection(0f, 0f, canvasWidth, canvasHeight)
        activeSelectionId = selection.id
    }
    
    /**
     * Invert selection
     */
    fun invertSelection() {
        getCurrentSelection()?.let { selection ->
            // TODO: Implement selection inversion
            notifySelectionModified(selection)
        }
    }
    
    /**
     * Feather selection edges
     */
    fun featherSelection(selectionId: String, radius: Float) {
        getSelection(selectionId)?.let { selection ->
            selection.featherRadius = radius
            notifySelectionModified(selection)
        }
    }
    
    /**
     * Expand selection
     */
    fun expandSelection(selectionId: String, pixels: Float) {
        getSelection(selectionId)?.let { selection ->
            // TODO: Implement selection expansion
            notifySelectionModified(selection)
        }
    }
    
    /**
     * Contract selection
     */
    fun contractSelection(selectionId: String, pixels: Float) {
        getSelection(selectionId)?.let { selection ->
            // TODO: Implement selection contraction
            notifySelectionModified(selection)
        }
    }
    
    /**
     * Add selection listener
     */
    fun addListener(listener: SelectionListener) {
        if (!selectionListeners.contains(listener)) {
            selectionListeners.add(listener)
        }
    }
    
    /**
     * Remove selection listener
     */
    fun removeListener(listener: SelectionListener) {
        selectionListeners.remove(listener)
    }
    
    // Notification methods
    
    private fun notifySelectionCreated(selection: Selection) {
        selectionListeners.forEach { it.onSelectionCreated(selection) }
    }
    
    private fun notifySelectionModified(selection: Selection) {
        selectionListeners.forEach { it.onSelectionModified(selection) }
    }
    
    private fun notifySelectionCleared(selectionId: String) {
        selectionListeners.forEach { it.onSelectionCleared(selectionId) }
    }
    
    private fun notifyActiveSelectionChanged(selectionId: String) {
        selectionListeners.forEach { it.onActiveSelectionChanged(selectionId) }
    }

    /**
     * Get appropriate selection tool for type
     */
    private fun getSelectionTool(type: SelectionType): SelectionTool {
        return when (type) {
            SelectionType.RECTANGULAR -> rectangularTool
            SelectionType.ELLIPTICAL -> ellipticalTool
            SelectionType.FREEHAND -> freehandTool
            SelectionType.MAGIC_WAND -> magicWandTool
            SelectionType.POLYGONAL -> freehandTool // Use freehand for polygonal for now
            SelectionType.QUICK_SELECT -> magicWandTool // Use magic wand for quick select
            SelectionType.ALL -> rectangularTool // Use rectangular for select all
            SelectionType.SIMILAR -> magicWandTool // Use magic wand for similar
        }
    }
}



/**
 * Selection listener interface
 */
interface SelectionListener {
    fun onSelectionCreated(selection: Selection) {}
    fun onSelectionModified(selection: Selection) {}
    fun onSelectionCleared(selectionId: String) {}
    fun onActiveSelectionChanged(selectionId: String) {}
} 