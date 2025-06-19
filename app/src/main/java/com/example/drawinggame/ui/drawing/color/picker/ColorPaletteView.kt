package com.example.drawinggame.ui.drawing.color.picker

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.drawinggame.ui.drawing.color.palette.ColorPalette
import kotlin.math.*

/**
 * Color palette display and selection view
 * Shows color swatches in a grid layout with touch selection
 */
class ColorPaletteView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    companion object {
        private const val DEFAULT_COLUMNS = 8
        private const val MIN_SWATCH_SIZE = 32f
        private const val SWATCH_SPACING = 4f
        private const val SELECTION_STROKE_WIDTH = 3f
    }
    
    // Palette data
    private var palette: ColorPalette? = null
    private var selectedColorIndex = -1
    
    // Layout
    private var columns = DEFAULT_COLUMNS
    private var swatchSize = MIN_SWATCH_SIZE
    private var spacing = SWATCH_SPACING
    
    // Touch interaction
    private var colorSelectionListener: ((Int, Int) -> Unit)? = null // color, index
    private var longPressListener: ((Int, Int) -> Unit)? = null // color, index
    
    // Drawing
    private val swatchPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = SELECTION_STROKE_WIDTH
        color = Color.BLACK
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f
        color = Color.GRAY
    }
    
    // Grid calculation
    private var rows = 0
    private var actualSwatchSize = MIN_SWATCH_SIZE
    
    init {
        calculateLayout()
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateLayout()
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val colors = palette?.colors?.size ?: 0
        if (colors == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        
        val availableWidth = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        val cols = min(columns, colors)
        val rows = ceil(colors.toFloat() / cols).toInt()
        
        // Calculate swatch size based on available width
        val swatchAndSpacing = (availableWidth - spacing * (cols - 1)) / cols
        actualSwatchSize = max(MIN_SWATCH_SIZE, swatchAndSpacing)
        
        val totalWidth = (actualSwatchSize * cols + spacing * (cols - 1)).toInt() + paddingLeft + paddingRight
        val totalHeight = (actualSwatchSize * rows + spacing * (rows - 1)).toInt() + paddingTop + paddingBottom
        
        setMeasuredDimension(
            resolveSize(totalWidth, widthMeasureSpec),
            resolveSize(totalHeight, heightMeasureSpec)
        )
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val colors = palette?.colors ?: return
        if (colors.isEmpty()) return
        
        val cols = min(columns, colors.size)
        val startX = paddingLeft.toFloat()
        val startY = paddingTop.toFloat()
        
        for (i in colors.indices) {
            val row = i / cols
            val col = i % cols
            
            val x = startX + col * (actualSwatchSize + spacing)
            val y = startY + row * (actualSwatchSize + spacing)
            
            drawColorSwatch(canvas, colors[i], x, y, i == selectedColorIndex)
        }
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val index = getColorIndexAt(event.x, event.y)
                if (index >= 0) {
                    selectedColorIndex = index
                    val color = palette?.colors?.get(index) ?: return false
                    colorSelectionListener?.invoke(color, index)
                    invalidate()
                    return true
                }
            }
        }
        return false
    }
    
    /**
     * Draw a single color swatch
     */
    private fun drawColorSwatch(canvas: Canvas, color: Int, x: Float, y: Float, isSelected: Boolean) {
        val rect = RectF(x, y, x + actualSwatchSize, y + actualSwatchSize)
        
        // Draw color swatch
        swatchPaint.color = color
        canvas.drawRoundRect(rect, 4f, 4f, swatchPaint)
        
        // Draw border
        canvas.drawRoundRect(rect, 4f, 4f, borderPaint)
        
        // Draw selection indicator
        if (isSelected) {
            val selectionRect = RectF(
                x - SELECTION_STROKE_WIDTH / 2,
                y - SELECTION_STROKE_WIDTH / 2,
                x + actualSwatchSize + SELECTION_STROKE_WIDTH / 2,
                y + actualSwatchSize + SELECTION_STROKE_WIDTH / 2
            )
            
            // Use white or black selection border based on color brightness
            val brightness = (Color.red(color) * 0.299 + Color.green(color) * 0.587 + Color.blue(color) * 0.114) / 255
            selectionPaint.color = if (brightness > 0.5) Color.BLACK else Color.WHITE
            
            canvas.drawRoundRect(selectionRect, 6f, 6f, selectionPaint)
        }
    }
    
    /**
     * Get color index at touch coordinates
     */
    private fun getColorIndexAt(x: Float, y: Float): Int {
        val colors = palette?.colors ?: return -1
        if (colors.isEmpty()) return -1
        
        val cols = min(columns, colors.size)
        val startX = paddingLeft.toFloat()
        val startY = paddingTop.toFloat()
        
        val col = ((x - startX) / (actualSwatchSize + spacing)).toInt()
        val row = ((y - startY) / (actualSwatchSize + spacing)).toInt()
        
        if (col < 0 || row < 0 || col >= cols) return -1
        
        val index = row * cols + col
        return if (index < colors.size) index else -1
    }
    
    /**
     * Calculate layout parameters
     */
    private fun calculateLayout() {
        val colors = palette?.colors?.size ?: 0
        if (colors == 0) {
            rows = 0
            return
        }
        
        val cols = min(columns, colors)
        rows = ceil(colors.toFloat() / cols).toInt()
        
        // Ensure minimum swatch size
        swatchSize = max(MIN_SWATCH_SIZE, actualSwatchSize)
    }
    
    /**
     * Set color palette to display
     */
    fun setPalette(palette: ColorPalette?) {
        this.palette = palette
        selectedColorIndex = -1
        calculateLayout()
        requestLayout()
        invalidate()
    }
    
    /**
     * Get current palette
     */
    fun getPalette(): ColorPalette? = palette
    
    /**
     * Set number of columns
     */
    fun setColumns(columns: Int) {
        this.columns = max(1, columns)
        calculateLayout()
        requestLayout()
        invalidate()
    }
    
    /**
     * Get number of columns
     */
    fun getColumns(): Int = columns
    
    /**
     * Set selected color by index
     */
    fun setSelectedIndex(index: Int) {
        val colors = palette?.colors ?: return
        selectedColorIndex = if (index in colors.indices) index else -1
        invalidate()
    }
    
    /**
     * Get selected color index
     */
    fun getSelectedIndex(): Int = selectedColorIndex
    
    /**
     * Get selected color
     */
    fun getSelectedColor(): Int? {
        val colors = palette?.colors ?: return null
        return if (selectedColorIndex in colors.indices) {
            colors[selectedColorIndex]
        } else null
    }
    
    /**
     * Set color selection listener
     */
    fun setOnColorSelectedListener(listener: (color: Int, index: Int) -> Unit) {
        colorSelectionListener = listener
    }
    
    /**
     * Set long press listener for color editing
     */
    fun setOnColorLongPressListener(listener: (color: Int, index: Int) -> Unit) {
        longPressListener = listener
    }
    
    /**
     * Add color to palette
     */
    fun addColor(color: Int) {
        val currentPalette = palette ?: return
        val newColors = currentPalette.colors.toMutableList()
        newColors.add(color)
        
        palette = currentPalette.copy(colors = newColors)
        calculateLayout()
        requestLayout()
        invalidate()
    }
    
    /**
     * Remove color at index
     */
    fun removeColorAt(index: Int) {
        val currentPalette = palette ?: return
        val colors = currentPalette.colors.toMutableList()
        
        if (index in colors.indices) {
            colors.removeAt(index)
            palette = currentPalette.copy(colors = colors)
            
            // Adjust selection
            if (selectedColorIndex == index) {
                selectedColorIndex = -1
            } else if (selectedColorIndex > index) {
                selectedColorIndex--
            }
            
            calculateLayout()
            requestLayout()
            invalidate()
        }
    }
    
    /**
     * Replace color at index
     */
    fun replaceColorAt(index: Int, newColor: Int) {
        val currentPalette = palette ?: return
        val colors = currentPalette.colors.toMutableList()
        
        if (index in colors.indices) {
            colors[index] = newColor
            palette = currentPalette.copy(colors = colors)
            invalidate()
        }
    }
    
    /**
     * Clear selection
     */
    fun clearSelection() {
        selectedColorIndex = -1
        invalidate()
    }
    
    /**
     * Get optimal height for current palette
     */
    fun getOptimalHeight(): Int {
        val colors = palette?.colors?.size ?: 0
        if (colors == 0) return 0
        
        val cols = min(columns, colors)
        val rows = ceil(colors.toFloat() / cols).toInt()
        
        return (actualSwatchSize * rows + spacing * (rows - 1)).toInt() + paddingTop + paddingBottom
    }
    
    /**
     * Set swatch spacing
     */
    fun setSwatchSpacing(spacing: Float) {
        this.spacing = max(0f, spacing)
        requestLayout()
        invalidate()
    }
    
    /**
     * Get swatch spacing
     */
    fun getSwatchSpacing(): Float = spacing
    
    /**
     * Check if palette is empty
     */
    fun isEmpty(): Boolean = palette?.colors?.isEmpty() ?: true
    
    /**
     * Get color count
     */
    fun getColorCount(): Int = palette?.colors?.size ?: 0
    
    /**
     * Get color at index
     */
    fun getColorAt(index: Int): Int? {
        val colors = palette?.colors ?: return null
        return if (index in colors.indices) colors[index] else null
    }
    
    /**
     * Find index of color (returns first match)
     */
    fun findColorIndex(color: Int): Int {
        val colors = palette?.colors ?: return -1
        return colors.indexOf(color)
    }
    
    /**
     * Check if palette contains color
     */
    fun containsColor(color: Int): Boolean {
        return findColorIndex(color) >= 0
    }
}