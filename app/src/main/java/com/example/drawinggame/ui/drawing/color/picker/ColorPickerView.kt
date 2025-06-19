package com.example.drawinggame.ui.drawing.color.picker

import android.content.Context
import android.graphics.*
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.drawinggame.ui.drawing.color.core.ColorManager
import kotlin.math.*

/**
 * Advanced color picker with wheel, sliders, and input methods
 * Supports multiple color spaces and professional color selection
 */
class ColorPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    companion object {
        private const val WHEEL_WIDTH_RATIO = 0.1f
        private const val CENTER_CIRCLE_RATIO = 0.6f
        private const val TOUCH_TOLERANCE = 20f
    }
    
    // Color management
    private val colorManager = ColorManager.getInstance()
    private var currentColor = Color.RED
    private var currentHue = 0f
    private var currentSaturation = 1f
    private var currentLightness = 0.5f
    
    // UI components
    private var colorWheelRadius = 0f
    private var colorWheelCenterX = 0f
    private var colorWheelCenterY = 0f
    private var wheelInnerRadius = 0f
    private var centerCircleRadius = 0f
    
    // Touch interaction
    private var isDraggingWheel = false
    private var isDraggingCenter = false
    
    // Drawing objects
    private val wheelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        setShadowLayer(2f, 0f, 0f, Color.BLACK)
    }
    private val crosshairPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f
        setShadowLayer(1f, 0f, 0f, Color.BLACK)
    }
    
    // Bitmaps for color wheel and center
    private var wheelBitmap: Bitmap? = null
    private var centerBitmap: Bitmap? = null
    private var wheelCanvas: Canvas? = null
    private var centerCanvas: Canvas? = null
    
    // Color change listener
    private var colorChangeListener: ((Int) -> Unit)? = null
    
    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null) // Enable shadow layer
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        val size = min(w, h) - paddingLeft - paddingRight
        colorWheelRadius = size / 2f * 0.9f
        wheelInnerRadius = colorWheelRadius * (1f - WHEEL_WIDTH_RATIO)
        centerCircleRadius = wheelInnerRadius * CENTER_CIRCLE_RATIO
        
        colorWheelCenterX = w / 2f
        colorWheelCenterY = h / 2f
        
        createColorWheelBitmap()
        updateCenterBitmap()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw color wheel
        wheelBitmap?.let { bitmap ->
            canvas.drawBitmap(bitmap, 
                colorWheelCenterX - colorWheelRadius, 
                colorWheelCenterY - colorWheelRadius, 
                wheelPaint)
        }
        
        // Draw center circle
        centerBitmap?.let { bitmap ->
            canvas.drawBitmap(bitmap,
                colorWheelCenterX - centerCircleRadius,
                colorWheelCenterY - centerCircleRadius,
                centerPaint)
        }
        
        // Draw hue indicator on wheel
        drawHueIndicator(canvas)
        
        // Draw saturation/lightness crosshair
        drawCenterCrosshair(canvas)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        val dx = x - colorWheelCenterX
        val dy = y - colorWheelCenterY
        val distance = sqrt(dx * dx + dy * dy)
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (distance <= centerCircleRadius) {
                    isDraggingCenter = true
                    updateSaturationLightness(x, y)
                    return true
                } else if (distance >= wheelInnerRadius && distance <= colorWheelRadius) {
                    isDraggingWheel = true
                    updateHue(x, y)
                    return true
                }
            }
            
            MotionEvent.ACTION_MOVE -> {
                if (isDraggingCenter) {
                    updateSaturationLightness(x, y)
                    return true
                } else if (isDraggingWheel) {
                    updateHue(x, y)
                    return true
                }
            }
            
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDraggingCenter = false
                isDraggingWheel = false
                return true
            }
        }
        
        return false
    }
    
    /**
     * Create the color wheel bitmap
     */
    private fun createColorWheelBitmap() {
        if (colorWheelRadius <= 0) return
        
        val size = (colorWheelRadius * 2).toInt()
        wheelBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        wheelCanvas = Canvas(wheelBitmap!!)
        
        val center = colorWheelRadius
        val wheelWidth = colorWheelRadius - wheelInnerRadius
        
        // Create radial gradient for hue wheel
        for (angle in 0 until 360) {
            val hue = angle.toFloat()
            val color = Color.HSVToColor(floatArrayOf(hue, 1f, 1f))
            
            wheelPaint.color = color
            
            val startAngle = angle - 0.5f
            val sweepAngle = 1f
            
            val rect = RectF(
                center - colorWheelRadius,
                center - colorWheelRadius,
                center + colorWheelRadius,
                center + colorWheelRadius
            )
            
            wheelCanvas?.drawArc(rect, startAngle, sweepAngle, true, wheelPaint)
        }
        
        // Create inner circle mask
        wheelPaint.color = Color.TRANSPARENT
        wheelPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        wheelCanvas?.drawCircle(center, center, wheelInnerRadius, wheelPaint)
        wheelPaint.xfermode = null
    }
    
    /**
     * Update center bitmap with current hue
     */
    private fun updateCenterBitmap() {
        if (centerCircleRadius <= 0) return
        
        val size = (centerCircleRadius * 2).toInt()
        centerBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        centerCanvas = Canvas(centerBitmap!!)
        
        val center = centerCircleRadius
        
        // Create saturation/lightness gradient
        for (y in 0 until size) {
            for (x in 0 until size) {
                val dx = x - center
                val dy = y - center
                val distance = sqrt(dx * dx + dy * dy)
                
                if (distance <= centerCircleRadius) {
                    // Convert pixel position to saturation/lightness
                    val saturation = (distance / centerCircleRadius).coerceIn(0f, 1f)
                    val lightness = (1f - (y / size)).coerceIn(0f, 1f)
                    
                    val hsl = floatArrayOf(currentHue, saturation, lightness)
                    val rgb = colorManager.hslToRgb(hsl)
                    val color = Color.rgb(rgb[0], rgb[1], rgb[2])
                    
                    centerPaint.color = color
                    centerCanvas?.drawPoint(x.toFloat(), y.toFloat(), centerPaint)
                }
            }
        }
    }
    
    /**
     * Draw hue indicator on the color wheel
     */
    private fun drawHueIndicator(canvas: Canvas) {
        val angle = Math.toRadians(currentHue.toDouble())
        val indicatorRadius = (wheelInnerRadius + colorWheelRadius) / 2f
        
        val x = colorWheelCenterX + cos(angle).toFloat() * indicatorRadius
        val y = colorWheelCenterY + sin(angle).toFloat() * indicatorRadius
        
        // Draw indicator circle
        canvas.drawCircle(x, y, 8f, indicatorPaint)
        
        // Draw inner dot
        indicatorPaint.style = Paint.Style.FILL
        indicatorPaint.color = Color.BLACK
        canvas.drawCircle(x, y, 4f, indicatorPaint)
        
        // Reset paint
        indicatorPaint.style = Paint.Style.STROKE
        indicatorPaint.color = Color.WHITE
    }
    
    /**
     * Draw crosshair for saturation/lightness selection
     */
    private fun drawCenterCrosshair(canvas: Canvas) {
        // Calculate crosshair position
        val radius = currentSaturation * centerCircleRadius
        val angle = Math.toRadians(0.0) // Could be used for different representations
        
        val x = colorWheelCenterX + cos(angle).toFloat() * radius
        val y = colorWheelCenterY + (1f - currentLightness) * centerCircleRadius * 2f - centerCircleRadius
        
        // Ensure crosshair is within center circle
        val dx = x - colorWheelCenterX
        val dy = y - colorWheelCenterY
        val distance = sqrt(dx * dx + dy * dy)
        
        val finalX: Float
        val finalY: Float
        
        if (distance <= centerCircleRadius) {
            finalX = x
            finalY = y
        } else {
            // Project to circle edge
            val factor = centerCircleRadius / distance
            finalX = colorWheelCenterX + dx * factor
            finalY = colorWheelCenterY + dy * factor
        }
        
        // Draw crosshair
        val crosshairSize = 8f
        canvas.drawLine(finalX - crosshairSize, finalY, finalX + crosshairSize, finalY, crosshairPaint)
        canvas.drawLine(finalX, finalY - crosshairSize, finalX, finalY + crosshairSize, crosshairPaint)
        
        // Draw center dot
        crosshairPaint.style = Paint.Style.FILL
        canvas.drawCircle(finalX, finalY, 3f, crosshairPaint)
        crosshairPaint.style = Paint.Style.STROKE
    }
    
    /**
     * Update hue based on touch position
     */
    private fun updateHue(x: Float, y: Float) {
        val dx = x - colorWheelCenterX
        val dy = y - colorWheelCenterY
        val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
        
        currentHue = if (angle < 0) angle + 360f else angle
        updateCenterBitmap()
        updateCurrentColor()
        invalidate()
    }
    
    /**
     * Update saturation and lightness based on touch position
     */
    private fun updateSaturationLightness(x: Float, y: Float) {
        val dx = x - colorWheelCenterX
        val dy = y - colorWheelCenterY
        val distance = sqrt(dx * dx + dy * dy)
        
        // Calculate saturation (distance from center)
        currentSaturation = (distance / centerCircleRadius).coerceIn(0f, 1f)
        
        // Calculate lightness (vertical position)
        val relativeY = (y - (colorWheelCenterY - centerCircleRadius)) / (centerCircleRadius * 2f)
        currentLightness = (1f - relativeY).coerceIn(0f, 1f)
        
        updateCurrentColor()
        invalidate()
    }
    
    /**
     * Update current color from HSL values
     */
    private fun updateCurrentColor() {
        val hsl = floatArrayOf(currentHue, currentSaturation, currentLightness)
        val rgb = colorManager.hslToRgb(hsl)
        currentColor = Color.rgb(rgb[0], rgb[1], rgb[2])
        colorChangeListener?.invoke(currentColor)
    }
    
    /**
     * Set color programmatically
     */
    fun setColor(color: Int) {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        
        val hsl = colorManager.rgbToHsl(intArrayOf(r, g, b))
        currentHue = hsl[0]
        currentSaturation = hsl[1]
        currentLightness = hsl[2]
        currentColor = color
        
        updateCenterBitmap()
        invalidate()
    }
    
    /**
     * Get current color
     */
    fun getColor(): Int = currentColor
    
    /**
     * Get current HSL values
     */
    fun getHSL(): FloatArray = floatArrayOf(currentHue, currentSaturation, currentLightness)
    
    /**
     * Set color change listener
     */
    fun setOnColorChangeListener(listener: (Int) -> Unit) {
        colorChangeListener = listener
    }
    
    /**
     * Set hue programmatically
     */
    fun setHue(hue: Float) {
        currentHue = hue.coerceIn(0f, 360f)
        updateCenterBitmap()
        updateCurrentColor()
        invalidate()
    }
    
    /**
     * Set saturation programmatically
     */
    fun setSaturation(saturation: Float) {
        currentSaturation = saturation.coerceIn(0f, 1f)
        updateCurrentColor()
        invalidate()
    }
    
    /**
     * Set lightness programmatically
     */
    fun setLightness(lightness: Float) {
        currentLightness = lightness.coerceIn(0f, 1f)
        updateCurrentColor()
        invalidate()
    }
    
    /**
     * Get color at specific position (for eyedropper functionality)
     */
    fun getColorAt(x: Float, y: Float): Int? {
        val dx = x - colorWheelCenterX
        val dy = y - colorWheelCenterY
        val distance = sqrt(dx * dx + dy * dy)
        
        return when {
            // Color wheel area
            distance >= wheelInnerRadius && distance <= colorWheelRadius -> {
                val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                val hue = if (angle < 0) angle + 360f else angle
                Color.HSVToColor(floatArrayOf(hue, 1f, 1f))
            }
            
            // Center circle area
            distance <= centerCircleRadius -> {
                val saturation = (distance / centerCircleRadius).coerceIn(0f, 1f)
                val relativeY = (y - (colorWheelCenterY - centerCircleRadius)) / (centerCircleRadius * 2f)
                val lightness = (1f - relativeY).coerceIn(0f, 1f)
                
                val hsl = floatArrayOf(currentHue, saturation, lightness)
                val rgb = colorManager.hslToRgb(hsl)
                Color.rgb(rgb[0], rgb[1], rgb[2])
            }
            
            else -> null
        }
    }
}