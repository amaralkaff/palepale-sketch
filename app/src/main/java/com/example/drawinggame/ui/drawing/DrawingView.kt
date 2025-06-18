package com.example.drawinggame.ui.drawing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.drawinggame.ui.drawing.brush.BrushManager
import com.example.drawinggame.ui.drawing.contracts.DrawingConstants
import com.example.drawinggame.ui.drawing.contracts.DrawingListener
import com.example.drawinggame.ui.drawing.models.DrawingPath
import com.example.drawinggame.ui.drawing.models.DrawingState
import com.example.drawinggame.ui.drawing.models.DrawingTool
import kotlin.math.min

/**
 * Custom view component that handles all drawing operations, touch events, and canvas rendering.
 * This is the primary drawing surface for the Social Sketch app.
 */
class DrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Drawing engine reference
    private var drawingEngine: DrawingEngine? = null
    
    // Brush manager
    private val brushManager = BrushManager.getInstance()
    
    // Bitmap and Canvas for drawing operations
    private var drawBitmap: Bitmap? = null
    private var drawCanvas: Canvas? = null
    
    // Current drawing properties
    private val drawPaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = DrawingConstants.DEFAULT_BRUSH_SIZE
        color = DrawingConstants.DEFAULT_COLOR
    }
    
    // Current drawing path
    private val currentPath = Path()
    
    // Drawing listener for external callbacks
    private var drawingListener: DrawingListener? = null
    
    // Touch tracking
    private var lastX = 0f
    private var lastY = 0f
    
    init {
        // Set initial brush properties
        updatePaintFromBrushManager()
    }
    
    /**
     * Initialize the drawing engine connection
     */
    fun setDrawingEngine(engine: DrawingEngine) {
        drawingEngine = engine
        drawingEngine?.getCurrentState()?.let { updateFromState(it) }
    }
    
    /**
     * Set a listener to receive drawing events
     */
    fun setDrawingListener(listener: DrawingListener) {
        drawingListener = listener
    }
    
    /**
     * Get the current drawing listener
     */
    fun getDrawingListener(): DrawingListener? {
        return drawingListener
    }
    
    /**
     * Update the paint properties from the BrushManager
     */
    private fun updatePaintFromBrushManager() {
        // Get a new paint from the brush manager
        val newPaint = brushManager.getCurrentPaint()
        
        // Copy all properties to our working paint
        drawPaint.set(newPaint)
    }
    
    /**
     * Update view from a drawing state
     */
    fun updateFromState(state: DrawingState) {
        // Update brush manager with new state
        brushManager.setBrushColor(state.currentColor)
        brushManager.setBrushSize(state.brushSize)
        brushManager.setBrushOpacity(state.opacity)
        brushManager.setTool(state.currentTool)
        
        // Update paint properties
        updatePaintFromBrushManager()
        
        invalidate()
    }
    
    /**
     * Update brush properties
     */
    fun updateBrush(tool: DrawingTool? = null, size: Float? = null, color: Int? = null, opacity: Int? = null) {
        // Update brush manager with any non-null values
        tool?.let { brushManager.setTool(it) }
        size?.let { brushManager.setBrushSize(it) }
        color?.let { brushManager.setBrushColor(it) }
        opacity?.let { brushManager.setBrushOpacity(it) }
        
        // Update paint from brush manager
        updatePaintFromBrushManager()
        
        // Update drawing engine with new state
        drawingEngine?.setBrushColor(brushManager.getCurrentColor())
        drawingEngine?.setBrushSize(brushManager.getCurrentSize())
        drawingEngine?.setTool(brushManager.getCurrentTool())
        
        invalidate()
    }
    
    /**
     * Clear the entire canvas
     */
    fun clearCanvas() {
        drawCanvas?.drawColor(Color.WHITE)
        currentPath.reset()
        invalidate()
        drawingListener?.onCanvasCleared()
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        // Create new bitmap and canvas when size changes
        recycleBitmap()
        
        drawBitmap = Bitmap.createBitmap(
            max(1, w), // Ensure minimum 1px dimensions
            max(1, h),
            Bitmap.Config.ARGB_8888
        )
        drawCanvas = Canvas(drawBitmap!!)
        
        // Initialize with white background
        drawCanvas?.drawColor(Color.WHITE)
        
        // Notify engine of new dimensions
        drawingEngine?.onCanvasSizeChanged(w, h)
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw the bitmap containing all previous drawing
        drawBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        
        // Draw the current path that's in progress
        canvas.drawPath(currentPath, drawPaint)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
                invalidate()
            }
        }
        
        return true
    }
    
    private fun touchStart(x: Float, y: Float) {
        // Make sure paint is using the latest brush settings
        updatePaintFromBrushManager()
        
        currentPath.reset()
        currentPath.moveTo(x, y)
        lastX = x
        lastY = y
        
        drawingListener?.onDrawingStarted(x, y)
    }
    
    private fun touchMove(x: Float, y: Float) {
        // Use quadratic bezier for smoother curves
        val dx = Math.abs(x - lastX)
        val dy = Math.abs(y - lastY)
        
        if (dx >= DrawingConstants.TOUCH_TOLERANCE || dy >= DrawingConstants.TOUCH_TOLERANCE) {
            // Add a quadratic bezier from last point, approaching new point
            currentPath.quadTo(
                lastX, lastY, 
                (x + lastX) / 2, (y + lastY) / 2
            )
            
            lastX = x
            lastY = y
            
            // Draw the path to the canvas
            drawCanvas?.drawPath(currentPath, drawPaint)
            
            // Reset for next segment
            currentPath.reset()
            currentPath.moveTo(lastX, lastY)
            
            drawingListener?.onDrawingProgress(x, y)
        }
    }
    
    private fun touchUp() {
        // Complete the path and add to canvas
        drawCanvas?.drawPath(currentPath, drawPaint)
        
        // Create path object for history
        val pathCopy = DrawingPath(
            path = Path(currentPath), // Make a copy of the path
            paint = Paint(drawPaint), // Make a copy of the paint
            timestamp = System.currentTimeMillis()
        )
        
        // Add to drawing engine history
        drawingEngine?.addPath(pathCopy)
        
        // Reset the current path
        currentPath.reset()
        
        drawingListener?.onDrawingFinished()
    }
    
    /**
     * Get the current brush manager
     */
    fun getBrushManager(): BrushManager {
        return brushManager
    }
    
    /**
     * Clean up resources when view is detached
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        recycleBitmap()
    }
    
    private fun recycleBitmap() {
        if (drawBitmap != null && !drawBitmap!!.isRecycled) {
            drawBitmap!!.recycle()
            drawBitmap = null
        }
    }
    
    private fun max(a: Int, b: Int): Int = if (a > b) a else b
    
    companion object {
        private const val DEFAULT_BRUSH_SIZE = 12f
        private const val DEFAULT_COLOR = Color.BLACK
        private const val TOUCH_TOLERANCE = 4f
    }
} 