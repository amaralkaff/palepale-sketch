package com.example.drawinggame.ui.drawing

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import com.example.drawinggame.ui.drawing.contracts.DrawingConstants
import com.example.drawinggame.ui.drawing.contracts.DrawingListener
import com.example.drawinggame.ui.drawing.models.CanvasTransform
import com.example.drawinggame.ui.drawing.models.DrawingPath
import com.example.drawinggame.ui.drawing.models.DrawingState
import com.example.drawinggame.ui.drawing.models.DrawingTool
import com.example.drawinggame.ui.drawing.models.Stroke
import com.example.drawinggame.ui.drawing.utils.BitmapUtils
import java.util.Stack
import com.example.drawinggame.ui.drawing.operations.CommandManager
import com.example.drawinggame.ui.drawing.operations.commands.StrokeCommand
import com.example.drawinggame.ui.drawing.operations.commands.ClearCommand
// Layer system imports - Phase 5.2 implementation
import com.example.drawinggame.ui.drawing.layers.core.LayerManager
import com.example.drawinggame.ui.drawing.layers.core.LayerListener
import com.example.drawinggame.ui.drawing.layers.core.DrawingLayer

/**
 * Core drawing logic engine that manages drawing operations, coordinate transformations,
 * and drawing state. Implements the command pattern for undo/redo operations.
 * Enhanced with Phase 5.2 layer system support.
 */
class DrawingEngine {
    // Current drawing state
    private var drawingState = DrawingState()
    
    // Canvas transformation
    private var canvasTransform = CanvasTransform()
    
    // Drawing history for undo/redo
    private val undoStack = Stack<DrawingPath>()
    private val redoStack = Stack<DrawingPath>()
    
    // Command manager for undo/redo operations
    private val commandManager = CommandManager()
    
    // Bitmap for the complete drawing
    private var drawingBitmap: Bitmap? = null
    
    // Listeners
    private val listeners = mutableListOf<DrawingListener>()
    
    // Canvas state
    private var canvasBitmap: Bitmap? = null
    private var drawCanvas: Canvas? = null
    
    // Layer system (Phase 5.2) - implemented
    private var layerManager: LayerManager? = null
    private var layerSystemEnabled = false
    
    // Drawing state
    private val strokes = mutableListOf<Stroke>()
    private var currentStroke: Stroke? = null
    private var currentPath = Path()
    
    // Drawing settings
    private var currentTool = DrawingTool.PEN
    private var brushSize = 10f
    private var brushColor = Color.BLACK
    private var brushAlpha = 255
    
    // Canvas paint for drawing operations
    private val canvasPaint = Paint(Paint.DITHER_FLAG).apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    
    /**
     * Initialize or reset the drawing engine
     */
    fun initialize(width: Int, height: Int, context: android.content.Context? = null) {
        // Reset the stacks
        undoStack.clear()
        redoStack.clear()
        
        // Create a new bitmap
        recycleBitmap()
        drawingBitmap = BitmapUtils.createBitmap(
            width, 
            height, 
            DrawingConstants.DEFAULT_BACKGROUND_COLOR
        )
        
        // Reset state
        drawingState = DrawingState()
        
        canvasTransform = CanvasTransform()
        
        // Initialize layer system if context provided (Phase 5.2 - implemented)
        context?.let {
            layerManager = LayerManager(it).apply {
                initialize(width, height)
            }
            layerSystemEnabled = true
        }
        
        // Notify listeners
        notifyStateChanged()
    }
    
    /**
     * Handle canvas size changes
     */
    fun onCanvasSizeChanged(width: Int, height: Int) {
        // If no previous bitmap, just initialize
        if (drawingBitmap == null) {
            initialize(width, height)
            return
        }
        
        // Save old bitmap
        val oldBitmap = drawingBitmap
        
        // Create new bitmap with new dimensions
        drawingBitmap = BitmapUtils.createBitmap(
            width,
            height,
            DrawingConstants.DEFAULT_BACKGROUND_COLOR
        )
        
        // Copy old content centered in new bitmap
        if (oldBitmap != null) {
            val canvas = android.graphics.Canvas(drawingBitmap!!)
            val left = (width - oldBitmap.width) / 2f
            val top = (height - oldBitmap.height) / 2f
            canvas.drawBitmap(oldBitmap, left, top, null)
            
            // Recycle old bitmap
            BitmapUtils.recycleBitmap(oldBitmap)
        }
        
        // Update state
        drawingState = drawingState.copy()
        
        canvasTransform = canvasTransform.copy()
        
        // Notify listeners
        notifyStateChanged()
    }
    
    /**
     * Add a drawing listener
     */
    fun addListener(listener: DrawingListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }
    
    /**
     * Remove a drawing listener
     */
    fun removeListener(listener: DrawingListener) {
        listeners.remove(listener)
    }
    
    /**
     * Get the current drawing state
     */
    fun getCurrentState(): DrawingState {
        return drawingState
    }
    
    /**
     * Update the current drawing tool
     */
    fun setTool(tool: DrawingTool) {
        currentTool = tool
        
        // Notify listeners of tool change
        notifyToolChanged(tool)
    }
    
    /**
     * Update the brush size
     */
    fun setBrushSize(size: Float) {
        brushSize = size
        
        // Notify listeners
        notifyBrushSizeChanged(size)
    }
    
    /**
     * Update the brush color
     */
    fun setBrushColor(color: Int) {
        brushColor = color
        
        // Notify listeners
        notifyBrushColorChanged(color)
    }
    
    /**
     * Set the brush opacity (alpha)
     */
    fun setBrushAlpha(alpha: Int) {
        brushAlpha = alpha.coerceIn(0, 255)
        
        // Notify listeners
        notifyBrushAlphaChanged(alpha)
    }
    
    /**
     * Add a drawing path to history
     */
    fun addPath(path: DrawingPath) {
        // Add to undo stack
        undoStack.push(path)
        
        // Clear redo stack when new path is added
        redoStack.clear()
        
        // Keep undo stack size in check
        if (undoStack.size > DrawingConstants.MAX_UNDO_STEPS) {
            undoStack.removeAt(0)
        }
    }
    
    /**
     * Undo the last drawing operation using command manager
     */
    fun undo(): Boolean {
        return commandManager.undo()
    }
    
    /**
     * Redo the last undone operation using command manager
     */
    fun redo(): Boolean {
        return commandManager.redo()
    }
    
    /**
     * Clear the canvas using command manager
     */
    fun clearCanvas() {
        val clearCommand = ClearCommand(this)
        commandManager.executeCommand(clearCommand)
    }
    
    /**
     * Clear the canvas immediately (for command execution)
     */
    fun clearCanvasImmediate() {
        // Clear the lists
        strokes.clear()
        redoStack.clear()
        
        // Clear the canvas
        drawCanvas?.drawColor(Color.WHITE)
        
        // Notify listeners
        notifyCanvasCleared()
    }
    
    /**
     * Check if undo is available
     */
    fun canUndo(): Boolean = commandManager.canUndo()
    
    /**
     * Check if redo is available
     */
    fun canRedo(): Boolean = commandManager.canRedo()
    
    /**
     * Get the command manager for external access
     */
    fun getCommandManager(): CommandManager = commandManager
    
    /**
     * Get the drawing bitmap
     */
    fun getDrawingBitmap(): Bitmap? {
        return drawingBitmap?.let { BitmapUtils.copyBitmap(it) }
    }
    
    // Layer System Integration (Phase 5.2)
    
    /**
     * Enable layer system for advanced drawing features
     */
    fun enableLayerSystem(context: android.content.Context) {
        // Layer system not yet implemented (Phase 5.2)
        // if (!layerSystemEnabled) {
        //     layerManager = LayerManager(context).apply {
        //         if (canvasBitmap != null) {
        //             initialize(canvasBitmap!!.width, canvasBitmap!!.height)
        //         }
        //     }
        //     layerSystemEnabled = true
        // }
    }
    
    /**
     * Get layer manager instance (Phase 5.2 - implemented)
     */
    fun getLayerManager(): LayerManager? = layerManager
    
    /**
     * Check if layer system is enabled
     */
    fun isLayerSystemEnabled(): Boolean = layerSystemEnabled
    
    /**
     * Get current active layer for drawing operations (Phase 5.2 - implemented)
     */
    fun getActiveLayer(): DrawingLayer? = layerManager?.getActiveLayer()
    
    /**
     * Start stroke on active layer (Phase 5.2 - not yet implemented)
     */
    fun startLayerStroke(x: Float, y: Float, tool: DrawingTool, paint: Paint) {
        // Layer system not yet implemented, fallback to advanced stroke
        startAdvancedStroke(x, y, tool, paint)
        // if (!layerSystemEnabled) {
        //     startAdvancedStroke(x, y, tool, paint)
        //     return
        // }
        // 
        // val activeLayer = layerManager?.getActiveLayer()
        // if (activeLayer?.canDraw() == true) {
        //     // Draw directly on layer bitmap
        //     activeLayer.bitmap?.let { layerBitmap ->
        //         val layerCanvas = Canvas(layerBitmap)
        //         currentPath = Path()
        //         currentPath.moveTo(x, y)
        //         
        //         currentStroke = Stroke(
        //             tool = tool,
        //             color = paint.color,
        //             size = paint.strokeWidth,
        //             alpha = paint.alpha,
        //             path = Path(currentPath),
        //             customPaint = paint
        //         )
        //     }
        // }
    }
    
    /**
     * Continue stroke on active layer (Phase 5.2 - not yet implemented)
     */
    fun continueLayerStroke(x: Float, y: Float, pressure: Float, paint: Paint) {
        // Layer system not yet implemented, fallback to advanced stroke
        addPointToAdvancedStroke(x, y, pressure, paint)
        // if (!layerSystemEnabled) {
        //     addPointToAdvancedStroke(x, y, pressure, paint)
        //     return
        // }
        // 
        // val activeLayer = layerManager?.getActiveLayer()
        // if (activeLayer?.canDraw() == true && currentStroke != null) {
        //     currentPath.lineTo(x, y)
        //     
        //     // Update stroke
        //     currentStroke?.apply {
        //         path = Path(currentPath)
        //         customPaint = paint
        //         size = paint.strokeWidth
        //         alpha = paint.alpha
        //     }
        //     
        //     // Draw to layer bitmap
        //     activeLayer.bitmap?.let { layerBitmap ->
        //         val layerCanvas = Canvas(layerBitmap)
        //         layerCanvas.drawPath(currentPath, paint)
        //     }
        // }
    }
    
    /**
     * Finish stroke on active layer (Phase 5.2 - not yet implemented)
     */
    fun finishLayerStroke() {
        // Layer system not yet implemented, fallback to standard stroke
        finishStroke()
        // if (!layerSystemEnabled) {
        //     finishStroke()
        //     return
        // }
        // 
        // val activeLayer = layerManager?.getActiveLayer()
        // if (activeLayer?.canDraw() == true && currentStroke != null) {
        //     // Add stroke to layer (for undo/redo if needed)
        //     currentStroke?.let { stroke ->
        //         strokes.add(stroke)
        //     }
        //     
        //     // Mark layer as modified
        //     activeLayer.markAsModified()
        //     
        //     // Generate thumbnail
        //     layerManager?.getLayerManager()?.let { manager ->
        //         // Update layer thumbnail would go here
        //     }
        //     
        //     // Reset current stroke
        //     currentStroke = null
        //     currentPath = Path()
        // }
    }
    
    /**
     * Get composite image from layer system
     */
    fun getLayerComposite(): Bitmap? {
        return if (layerSystemEnabled) {
            getBitmap() // layerManager?.getCompositeImage()
        } else {
            getBitmap()
        }
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        recycleBitmap()
        listeners.clear()
        undoStack.clear()
        redoStack.clear()
        // layerManager?.cleanup()
        // layerManager = null
        layerSystemEnabled = false
    }
    
    /**
     * Recycle the bitmap to prevent memory leaks
     */
    private fun recycleBitmap() {
        BitmapUtils.recycleBitmap(drawingBitmap)
        drawingBitmap = null
    }
    
    /**
     * Redraw the canvas from scratch using the undo stack
     */
    private fun redrawCanvas() {
        drawingBitmap?.let { bitmap ->
            // Clear the canvas
            val canvas = android.graphics.Canvas(bitmap)
            canvas.drawColor(DrawingConstants.DEFAULT_BACKGROUND_COLOR)
            
            // Redraw all paths in the undo stack
            for (path in undoStack) {
                canvas.drawPath(path.path, path.paint)
            }
        }
    }
    
    /**
     * Draw a single path
     */
    private fun drawPath(path: DrawingPath) {
        drawingBitmap?.let { bitmap ->
            val canvas = android.graphics.Canvas(bitmap)
            canvas.drawPath(path.path, path.paint)
        }
    }
    
    /**
     * Notify listeners of state change
     */
    private fun notifyStateChanged() {
        // Create a copy of the listeners to avoid concurrent modification
        val listenersCopy = ArrayList(listeners)
        listenersCopy.forEach { listener ->
            // Notify with current state (will be expanded in Phase 4.2)
        }
    }
    
    /**
     * Initialize the engine with a bitmap and canvas
     */
    fun init(width: Int, height: Int) {
        // Create the drawing canvas
        canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(canvasBitmap!!)
        
        // Clear the canvas to white
        drawCanvas?.drawColor(Color.WHITE)
    }
    
    /**
     * Resize the canvas
     */
    fun resize(width: Int, height: Int) {
        // Create a new bitmap with the new dimensions
        val newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val newCanvas = Canvas(newBitmap)
        
        // Draw white background
        newCanvas.drawColor(Color.WHITE)
        
        // Copy existing content if available
        canvasBitmap?.let {
            newCanvas.drawBitmap(it, 0f, 0f, null)
        }
        
        // Update the canvas and bitmap
        canvasBitmap = newBitmap
        drawCanvas = newCanvas
    }
    
    /**
     * Draw the current state to a canvas
     */
    fun draw(canvas: Canvas) {
        // Apply canvas transformations
        canvas.save()
        canvas.translate(canvasTransform.panOffsetX, canvasTransform.panOffsetY)
        canvas.scale(canvasTransform.zoomLevel, canvasTransform.zoomLevel)
        
        // Draw the bitmap
        canvasBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
        
        // Draw the current stroke if active
        currentStroke?.let {
            val paint = createPaintForTool(it.tool, it.size, it.color, it.alpha)
            canvas.drawPath(currentPath, paint)
        }
        
        canvas.restore()
    }
    
    /**
     * Start a new stroke with the specified tool
     */
    fun startStroke(x: Float, y: Float, tool: DrawingTool) {
        // Create a new path
        currentPath = Path()
        currentPath.moveTo(x, y)
        
        // Create a new stroke
        currentStroke = Stroke(
            tool = tool,
            color = brushColor,
            size = brushSize,
            alpha = brushAlpha,
            path = Path(currentPath)
        )
        
        // Clear redo stack when a new stroke is started
        redoStack.clear()
        
        // Notify listeners
        notifyStrokeStarted(x, y, tool)
    }
    
    /**
     * Add a point to the current stroke
     */
    fun addPointToStroke(x: Float, y: Float, pressure: Float = 1.0f) {
        if (currentStroke == null) return
        
        // Add point to path
        currentPath.lineTo(x, y)
        
        // Update the stroke's path
        currentStroke?.path = Path(currentPath)
        
        // Apply pressure sensitivity if applicable
        if (pressure != 1.0f) {
            val adjustedSize = brushSize * pressure
            currentStroke?.size = adjustedSize
        }
        
        // Notify listeners
        notifyStrokeContinued(x, y, pressure)
    }
    
    /**
     * Update the current stroke with a new path
     */
    fun updateStroke(path: Path, pressure: Float = 1.0f) {
        if (currentStroke == null) return
        
        // Update the current path
        currentPath = path
        
        // Update the stroke's path
        currentStroke?.path = Path(currentPath)
        
        // Apply pressure sensitivity if applicable
        if (pressure != 1.0f) {
            val adjustedSize = brushSize * pressure
            currentStroke?.size = adjustedSize
        }
        
        // Notify listeners
        notifyStrokeUpdated(path, pressure)
    }
    
    /**
     * Finish the current stroke and add it to the list
     */
    fun finishStroke(finalPath: Path? = null) {
        currentStroke?.let { stroke ->
            // Update with final path if provided
            finalPath?.let {
                stroke.path = it
                currentPath = it
            }
            
            // Add to strokes list
            strokes.add(stroke)
            
            // Draw the stroke to the canvas
            drawStroke(stroke)
            
            // Reset current stroke
            val completedStroke = stroke
            currentStroke = null
            currentPath = Path()
            
            // Notify listeners
            notifyStrokeFinished(completedStroke)
        }
    }
    
    /**
     * Cancel the current stroke
     */
    fun cancelStroke() {
        currentStroke = null
        currentPath = Path()
        
        // Notify listeners
        notifyStrokeCancelled()
    }
    
    // Advanced stroke methods for Phase 5.1
    
    /**
     * Start a new stroke with advanced paint for Phase 5.1 brushes
     */
    fun startAdvancedStroke(x: Float, y: Float, tool: DrawingTool, paint: Paint) {
        // Create a new path
        currentPath = Path()
        currentPath.moveTo(x, y)
        
        // Create a new stroke with advanced paint properties
        currentStroke = Stroke(
            tool = tool,
            color = paint.color,
            size = paint.strokeWidth,
            alpha = paint.alpha,
            path = Path(currentPath)
        )
        
        // Store the advanced paint for later use
        currentStroke?.customPaint = paint
        
        // Clear redo stack when a new stroke is started
        redoStack.clear()
        
        // Notify listeners
        notifyStrokeStarted(x, y, tool)
    }
    
    /**
     * Add a point to current stroke with advanced paint
     */
    fun addPointToAdvancedStroke(x: Float, y: Float, pressure: Float, paint: Paint) {
        if (currentStroke == null) return
        
        // Add point to path
        currentPath.lineTo(x, y)
        
        // Update the stroke's path and paint
        currentStroke?.path = Path(currentPath)
        currentStroke?.customPaint = paint
        
        // Update size from paint
        currentStroke?.size = paint.strokeWidth
        currentStroke?.alpha = paint.alpha
        
        // Notify listeners
        notifyStrokeContinued(x, y, pressure)
    }
    
    /**
     * Update stroke with advanced path and paint
     */
    fun updateAdvancedStroke(path: Path, paint: Paint, pressure: Float = 1.0f) {
        if (currentStroke == null) return
        
        // Update the current path and paint
        currentPath = path
        currentStroke?.path = Path(currentPath)
        currentStroke?.customPaint = paint
        
        // Update stroke properties from paint
        currentStroke?.size = paint.strokeWidth
        currentStroke?.alpha = paint.alpha
        
        // Notify listeners
        notifyStrokeUpdated(path, pressure)
    }
    
    /**
     * Finish advanced stroke with final paint
     */
    fun finishAdvancedStroke(finalPath: Path, finalPaint: Paint) {
        currentStroke?.let { stroke ->
            // Update with final path and paint
            stroke.path = finalPath
            stroke.customPaint = finalPaint
            stroke.size = finalPaint.strokeWidth
            stroke.alpha = finalPaint.alpha
            currentPath = finalPath
            
            // Add to strokes list
            strokes.add(stroke)
            
            // Draw the stroke with advanced paint
            drawAdvancedStroke(stroke)
            
            // Reset current stroke
            val completedStroke = stroke
            currentStroke = null
            currentPath = Path()
            
            // Notify listeners
            notifyStrokeFinished(completedStroke)
        }
    }
    
    /**
     * Cancel current stroke (alias for compatibility)
     */
    fun cancelCurrentStroke() {
        cancelStroke()
    }
    
    /**
     * Draw a stroke to the canvas
     */
    private fun drawStroke(stroke: Stroke) {
        val canvas = drawCanvas ?: return
        
        // Create paint for the stroke
        val paint = createPaintForTool(stroke.tool, stroke.size, stroke.color, stroke.alpha)
        
        // Draw the stroke
        canvas.drawPath(stroke.path, paint)
    }
    
    /**
     * Draw an advanced stroke with custom paint
     */
    private fun drawAdvancedStroke(stroke: Stroke) {
        val canvas = drawCanvas ?: return
        
        // Use custom paint if available, otherwise fall back to basic paint
        val paint = stroke.customPaint ?: createPaintForTool(stroke.tool, stroke.size, stroke.color, stroke.alpha)
        
        // Draw the stroke with advanced paint
        canvas.drawPath(stroke.path, paint)
    }
    
    /**
     * Create a paint object for the specified tool
     */
    private fun createPaintForTool(tool: DrawingTool, size: Float, color: Int, alpha: Int): Paint {
        return when (tool) {
            DrawingTool.PEN -> createPenPaint(size, color, alpha)
            DrawingTool.BRUSH -> createBrushPaint(size, color, alpha)
            DrawingTool.ERASER -> createEraserPaint(size)
        }
    }
    
    /**
     * Create paint for pen tool
     */
    private fun createPenPaint(size: Float, color: Int, alpha: Int): Paint {
        return Paint(canvasPaint).apply {
            strokeWidth = size
            this.color = color
            this.alpha = alpha
            style = Paint.Style.STROKE
        }
    }
    
    /**
     * Create paint for brush tool
     */
    private fun createBrushPaint(size: Float, color: Int, alpha: Int): Paint {
        return Paint(canvasPaint).apply {
            strokeWidth = size
            this.color = color
            this.alpha = alpha
            style = Paint.Style.STROKE
        }
    }
    
    /**
     * Create paint for eraser tool
     */
    private fun createEraserPaint(size: Float): Paint {
        return Paint(canvasPaint).apply {
            strokeWidth = size
            color = Color.WHITE
            style = Paint.Style.STROKE
        }
    }
    
    /**
     * Get the current tool
     */
    fun getCurrentTool(): DrawingTool {
        return currentTool
    }
    
    /**
     * Update the canvas transformation
     */
    fun updateCanvasTransform(transform: CanvasTransform) {
        canvasTransform = transform
        
        // Notify listeners
        notifyCanvasTransformChanged(transform)
    }
    
    /**
     * Get the current canvas transform
     */
    fun getCanvasTransform(): CanvasTransform {
        return canvasTransform
    }
    
    /**
     * Notify listeners that a stroke has started
     */
    private fun notifyStrokeStarted(x: Float, y: Float, tool: DrawingTool) {
        listeners.forEach { it.onStrokeStarted(x, y, tool) }
    }
    
    /**
     * Notify listeners that a stroke is continuing
     */
    private fun notifyStrokeContinued(x: Float, y: Float, pressure: Float) {
        listeners.forEach { it.onStrokeContinued(x, y, pressure) }
    }
    
    /**
     * Notify listeners that a stroke has been updated with a new path
     */
    private fun notifyStrokeUpdated(path: Path, pressure: Float) {
        listeners.forEach { it.onStrokeUpdated(path, pressure) }
    }
    
    /**
     * Notify listeners that a stroke has finished
     */
    private fun notifyStrokeFinished(stroke: Stroke) {
        listeners.forEach { it.onStrokeFinished(stroke) }
    }
    
    /**
     * Notify listeners that a stroke has been cancelled
     */
    private fun notifyStrokeCancelled() {
        listeners.forEach { it.onStrokeCancelled() }
    }
    
    /**
     * Notify listeners that an undo operation was performed
     */
    private fun notifyUndoPerformed() {
        listeners.forEach { it.onUndoPerformed() }
    }
    
    /**
     * Notify listeners that a redo operation was performed
     */
    private fun notifyRedoPerformed() {
        listeners.forEach { it.onRedoPerformed() }
    }
    
    /**
     * Notify listeners that the canvas was cleared
     */
    private fun notifyCanvasCleared() {
        listeners.forEach { it.onCanvasCleared() }
    }
    
    /**
     * Notify listeners that the tool was changed
     */
    private fun notifyToolChanged(tool: DrawingTool) {
        listeners.forEach { it.onToolChanged(tool) }
    }
    
    /**
     * Notify listeners that the brush size was changed
     */
    private fun notifyBrushSizeChanged(size: Float) {
        listeners.forEach { it.onBrushSizeChanged(size) }
    }
    
    /**
     * Notify listeners that the brush color was changed
     */
    private fun notifyBrushColorChanged(color: Int) {
        listeners.forEach { it.onBrushColorChanged(color) }
    }
    
    /**
     * Notify listeners that the brush alpha was changed
     */
    private fun notifyBrushAlphaChanged(alpha: Int) {
        listeners.forEach { it.onBrushAlphaChanged(alpha) }
    }
    
    /**
     * Notify listeners that the canvas transform was changed
     */
    private fun notifyCanvasTransformChanged(transform: CanvasTransform) {
        listeners.forEach { it.onCanvasTransformChanged(transform) }
    }
    
    /**
     * Notify listeners that the state was restored
     */
    private fun notifyStateRestored(state: DrawingState) {
        listeners.forEach { it.onStateRestored(state) }
    }
    
    /**
     * Get the current drawing bitmap (creates a copy for safety)
     */
    fun getBitmap(): Bitmap? {
        return canvasBitmap?.copy(Bitmap.Config.ARGB_8888, false)
    }
    
    /**
     * Restore the canvas from a bitmap
     */
    fun restoreCanvas(bitmap: Bitmap) {
        try {
            // Create new canvas with the same size as the restored bitmap
            canvasBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            drawCanvas = Canvas(canvasBitmap!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Restore the canvas from compressed bitmap data
     */
    fun restoreCanvas(bitmapData: ByteArray) {
        try {
            val bitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.size)
            bitmap?.let { restoreCanvas(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    companion object {
        private var instance: DrawingEngine? = null
        
        /**
         * Get the drawing engine instance (singleton)
         */
        @JvmStatic
        fun getInstance(): DrawingEngine {
            if (instance == null) {
                instance = DrawingEngine()
            }
            return instance!!
        }
        
        /**
         * Reset the singleton instance (useful for testing)
         */
        @JvmStatic
        fun resetInstance() {
            instance?.cleanup()
            instance = null
        }
    }
} 