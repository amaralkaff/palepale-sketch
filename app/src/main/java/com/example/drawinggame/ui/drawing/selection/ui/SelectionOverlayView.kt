package com.example.drawinggame.ui.drawing.selection.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.drawinggame.ui.drawing.selection.core.Selection
import kotlin.math.cos
import kotlin.math.sin

/**
 * Overlay view for displaying selection feedback with marching ants animation
 * Phase 5.3: Selection & Transformation Tools
 */
class SelectionOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var selection: Selection? = null
    private val marchingAntsPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.BLACK
        pathEffect = null
    }
    
    private val selectionHandlePaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.WHITE
        strokeWidth = 2f
    }
    
    private val selectionHandleStrokePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = 1f
    }
    
    private var dashOffset = 0f
    private var marchingAntsAnimator: ValueAnimator? = null
    
    private val handleSize = 16f
    private val selectionHandles = mutableListOf<RectF>()
    
    init {
        setupMarchingAntsAnimation()
    }
    
    private fun setupMarchingAntsAnimation() {
        marchingAntsAnimator = ValueAnimator.ofFloat(0f, 20f).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            addUpdateListener { animator ->
                dashOffset = animator.animatedValue as Float
                updateMarchingAntsPaint()
                invalidate()
            }
        }
    }
    
    private fun updateMarchingAntsPaint() {
        val dashArray = floatArrayOf(10f, 10f)
        marchingAntsPaint.pathEffect = DashPathEffect(dashArray, dashOffset)
    }
    
    fun setSelection(selection: Selection?) {
        this.selection = selection
        updateSelectionHandles()
        
        if (selection != null) {
            startMarchingAnts()
        } else {
            stopMarchingAnts()
        }
        
        invalidate()
    }
    
    private fun updateSelectionHandles() {
        selectionHandles.clear()
        
        selection?.let { sel ->
            val bounds = sel.bounds
            
            // Corner handles
            selectionHandles.add(createHandle(bounds.left, bounds.top))
            selectionHandles.add(createHandle(bounds.right, bounds.top))
            selectionHandles.add(createHandle(bounds.left, bounds.bottom))
            selectionHandles.add(createHandle(bounds.right, bounds.bottom))
            
            // Edge handles
            selectionHandles.add(createHandle(bounds.centerX(), bounds.top))
            selectionHandles.add(createHandle(bounds.centerX(), bounds.bottom))
            selectionHandles.add(createHandle(bounds.left, bounds.centerY()))
            selectionHandles.add(createHandle(bounds.right, bounds.centerY()))
        }
    }
    
    private fun createHandle(x: Float, y: Float): RectF {
        val halfSize = handleSize / 2
        return RectF(x - halfSize, y - halfSize, x + halfSize, y + halfSize)
    }
    
    private fun startMarchingAnts() {
        marchingAntsAnimator?.start()
    }
    
    private fun stopMarchingAnts() {
        marchingAntsAnimator?.cancel()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        selection?.let { sel ->
            drawSelection(canvas, sel)
            drawSelectionHandles(canvas)
        }
    }
    
    private fun drawSelection(canvas: Canvas, selection: Selection) {
        // Draw marching ants outline
        canvas.drawPath(selection.path, marchingAntsPaint)
        
        // Draw selection bounds for rectangular selections
        if (selection.bounds.width() > 0 && selection.bounds.height() > 0) {
            // Draw a subtle background overlay
            val overlayPaint = Paint().apply {
                style = Paint.Style.FILL
                color = Color.argb(30, 0, 150, 255)
            }
            canvas.drawRect(selection.bounds, overlayPaint)
        }
    }
    
    private fun drawSelectionHandles(canvas: Canvas) {
        for (handle in selectionHandles) {
            // Draw handle background
            canvas.drawRect(handle, selectionHandlePaint)
            
            // Draw handle border
            canvas.drawRect(handle, selectionHandleStrokePaint)
        }
    }
    
    fun getHandleAt(x: Float, y: Float): Int? {
        for (i in selectionHandles.indices) {
            if (selectionHandles[i].contains(x, y)) {
                return i
            }
        }
        return null
    }
    
    fun isPointInSelection(x: Float, y: Float): Boolean {
        return selection?.bounds?.contains(x, y) == true
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopMarchingAnts()
    }
} 