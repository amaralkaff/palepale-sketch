package com.example.drawinggame.ui.drawing.color.adjustments

import android.graphics.*
import java.util.*

/**
 * Abstract base class for non-destructive adjustment layers in Social Sketch
 * Provides framework for color and tonal adjustments that can be applied to layers
 */
abstract class AdjustmentLayer {
    
    val id: String = UUID.randomUUID().toString()
    abstract val type: AdjustmentType
    abstract val name: String
    abstract val description: String
    
    // Adjustment properties
    var isEnabled: Boolean = true
    var opacity: Float = 1f
        set(value) {
            field = value.coerceIn(0f, 1f)
        }
    var blendMode: BlendMode = BlendMode.NORMAL
    
    // Parameter management
    protected val parameters = mutableMapOf<String, Float>()
    private val parameterRanges = mutableMapOf<String, Pair<Float, Float>>()
    private val parameterDefaults = mutableMapOf<String, Float>()
    
    // Listeners
    private val changeListeners = mutableListOf<AdjustmentChangeListener>()
    
    /**
     * Apply adjustment to source bitmap
     */
    abstract fun apply(source: Bitmap): Bitmap
    
    /**
     * Get preview of adjustment on a smaller region
     */
    open fun getPreview(source: Bitmap, region: Rect? = null): Bitmap {
        val previewSource = if (region != null && region.width() > 0 && region.height() > 0) {
            Bitmap.createBitmap(source, region.left, region.top, region.width(), region.height())
        } else {
            // Create a scaled-down version for faster preview
            val maxSize = 256
            val scale = minOf(maxSize.toFloat() / source.width, maxSize.toFloat() / source.height, 1f)
            if (scale < 1f) {
                val newWidth = (source.width * scale).toInt()
                val newHeight = (source.height * scale).toInt()
                Bitmap.createScaledBitmap(source, newWidth, newHeight, true)
            } else {
                source
            }
        }
        
        return apply(previewSource)
    }
    
    /**
     * Reset all parameters to default values
     */
    open fun reset() {
        parameterDefaults.forEach { (key, defaultValue) ->
            parameters[key] = defaultValue
        }
        notifyParametersChanged()
    }
    
    /**
     * Get parameter value
     */
    fun getParameter(name: String): Float {
        return parameters[name] ?: parameterDefaults[name] ?: 0f
    }
    
    /**
     * Set parameter value with validation
     */
    fun setParameter(name: String, value: Float) {
        val range = parameterRanges[name]
        val clampedValue = if (range != null) {
            value.coerceIn(range.first, range.second)
        } else {
            value
        }
        
        if (parameters[name] != clampedValue) {
            parameters[name] = clampedValue
            notifyParameterChanged(name, clampedValue)
        }
    }
    
    /**
     * Get parameter range
     */
    fun getParameterRange(name: String): Pair<Float, Float> {
        return parameterRanges[name] ?: (0f to 1f)
    }
    
    /**
     * Get all parameter names
     */
    fun getParameterNames(): List<String> {
        return parameterDefaults.keys.toList()
    }
    
    /**
     * Get current parameter values
     */
    fun getCurrentParameters(): Map<String, Float> {
        return parameters.toMap()
    }
    
    /**
     * Set multiple parameters at once
     */
    fun setParameters(newParameters: Map<String, Float>) {
        var changed = false
        newParameters.forEach { (name, value) ->
            val range = parameterRanges[name]
            val clampedValue = if (range != null) {
                value.coerceIn(range.first, range.second)
            } else {
                value
            }
            
            if (parameters[name] != clampedValue) {
                parameters[name] = clampedValue
                changed = true
            }
        }
        
        if (changed) {
            notifyParametersChanged()
        }
    }
    
    /**
     * Check if adjustment has any effect (non-default parameters)
     */
    open fun hasEffect(): Boolean {
        return parameters.any { (name, value) ->
            val defaultValue = parameterDefaults[name] ?: 0f
            kotlin.math.abs(value - defaultValue) > 0.001f
        }
    }
    
    /**
     * Create a copy of this adjustment layer
     */
    abstract fun copy(): AdjustmentLayer
    
    /**
     * Get adjustment strength (0-1) for UI feedback
     */
    open fun getAdjustmentStrength(): Float {
        if (parameterDefaults.isEmpty()) return 0f
        
        var totalStrength = 0f
        var paramCount = 0
        
        parameters.forEach { (name, value) ->
            val defaultValue = parameterDefaults[name] ?: 0f
            val range = parameterRanges[name] ?: (0f to 1f)
            val rangeSize = range.second - range.first
            
            if (rangeSize > 0f) {
                val strength = kotlin.math.abs(value - defaultValue) / rangeSize
                totalStrength += strength
                paramCount++
            }
        }
        
        return if (paramCount > 0) totalStrength / paramCount else 0f
    }
    
    /**
     * Export adjustment settings to string
     */
    open fun exportSettings(): String {
        val settings = mutableMapOf<String, Any>()
        settings["type"] = type.name
        settings["enabled"] = isEnabled
        settings["opacity"] = opacity
        settings["blendMode"] = blendMode.name
        settings["parameters"] = parameters
        
        // In real implementation, use proper JSON serialization
        return settings.toString()
    }
    
    /**
     * Import adjustment settings from string
     */
    open fun importSettings(settingsString: String): Boolean {
        return try {
            // In real implementation, use proper JSON deserialization
            // For now, return true to indicate success
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // Protected helper methods for subclasses
    
    /**
     * Register a parameter with its range and default value
     */
    protected fun registerParameter(name: String, defaultValue: Float, min: Float, max: Float) {
        parameterDefaults[name] = defaultValue
        parameterRanges[name] = min to max
        parameters[name] = defaultValue
    }
    
    /**
     * Apply adjustment to individual pixel
     */
    protected fun processPixel(pixel: Int): Int {
        // Default implementation - subclasses should override for pixel-level processing
        return pixel
    }
    
    /**
     * Apply adjustment using pixel processing
     */
    protected fun applyPixelProcessing(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        
        for (i in pixels.indices) {
            pixels[i] = processPixel(pixels[i])
        }
        
        val result = Bitmap.createBitmap(width, height, source.config ?: Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        
        return result
    }
    
    /**
     * Apply adjustment using pixel processing with lambda
     */
    protected fun applyPixelProcessing(source: Bitmap, processor: (Int) -> Int): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        
        for (i in pixels.indices) {
            pixels[i] = processor(pixels[i])
        }
        
        val result = Bitmap.createBitmap(width, height, source.config ?: Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        
        return result
    }
    
    /**
     * Apply adjustment using Canvas operations
     */
    protected fun applyCanvasProcessing(source: Bitmap, operation: (Canvas, Paint) -> Unit): Bitmap {
        val result = Bitmap.createBitmap(source.width, source.height, source.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        
        // Draw original image
        canvas.drawBitmap(source, 0f, 0f, paint)
        
        // Apply custom operation
        operation(canvas, paint)
        
        return result
    }
    
    /**
     * Apply adjustment using ColorMatrix
     */
    protected fun applyColorMatrix(source: Bitmap, colorMatrix: ColorMatrix): Bitmap {
        val result = Bitmap.createBitmap(source.width, source.height, source.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
        
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }
    
    /**
     * Blend two bitmaps using specified blend mode
     */
    protected fun blendBitmaps(base: Bitmap, overlay: Bitmap, blendMode: BlendMode, opacity: Float): Bitmap {
        val result = Bitmap.createBitmap(base.width, base.height, base.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        
        // Draw base image
        canvas.drawBitmap(base, 0f, 0f, null)
        
        // Draw overlay with blend mode and opacity
        val paint = Paint().apply {
            alpha = (opacity * 255).toInt()
            // Note: In real implementation, you'd need to handle different blend modes
            // Android's PorterDuff modes don't directly map to Photoshop blend modes
        }
        
        canvas.drawBitmap(overlay, 0f, 0f, paint)
        return result
    }
    
    // Listener management
    
    fun addChangeListener(listener: AdjustmentChangeListener) {
        changeListeners.add(listener)
    }
    
    fun removeChangeListener(listener: AdjustmentChangeListener) {
        changeListeners.remove(listener)
    }
    
    private fun notifyParameterChanged(parameterName: String, value: Float) {
        changeListeners.forEach { it.onParameterChanged(this, parameterName, value) }
    }
    
    protected fun notifyParametersChanged() {
        changeListeners.forEach { it.onParametersChanged(this) }
    }
}

/**
 * Types of adjustment layers
 */
enum class AdjustmentType(val displayName: String, val description: String) {
    BRIGHTNESS_CONTRAST("Brightness/Contrast", "Adjust brightness and contrast"),
    LEVELS("Levels", "Adjust input and output levels"),
    CURVES("Curves", "Precise tonal curve adjustments"),
    HUE_SATURATION("Hue/Saturation", "Adjust hue, saturation, and lightness"),
    COLOR_BALANCE("Color Balance", "Adjust color balance in shadows, midtones, and highlights"),
    VIBRANCE("Vibrance", "Smart saturation enhancement"),
    EXPOSURE("Exposure", "Photographic exposure simulation"),
    SHADOWS_HIGHLIGHTS("Shadows/Highlights", "Recover detail in shadows and highlights"),
    SELECTIVE_COLOR("Selective Color", "Target specific color ranges"),
    CHANNEL_MIXER("Channel Mixer", "Blend color channels creatively"),
    PHOTO_FILTER("Photo Filter", "Color temperature and tint adjustments"),
    BLACK_WHITE("Black & White", "Intelligent grayscale conversion"),
    GRADIENT_MAP("Gradient Map", "Map tones to gradient colors"),
    COLOR_LOOKUP("Color Lookup", "Apply LUT (Look-Up Table) effects"),
    TEMPERATURE_TINT("Temperature/Tint", "Adjust color temperature and tint"),
    HIGHLIGHTS_SHADOWS("Highlights/Shadows", "Advanced highlight and shadow recovery")
}

/**
 * Blend modes for adjustment layers
 */
enum class BlendMode(val displayName: String) {
    NORMAL("Normal"),
    MULTIPLY("Multiply"),
    SCREEN("Screen"),
    OVERLAY("Overlay"),
    SOFT_LIGHT("Soft Light"),
    HARD_LIGHT("Hard Light"),
    COLOR_DODGE("Color Dodge"),
    COLOR_BURN("Color Burn"),
    DARKEN("Darken"),
    LIGHTEN("Lighten"),
    DIFFERENCE("Difference"),
    EXCLUSION("Exclusion"),
    HUE("Hue"),
    SATURATION("Saturation"),
    COLOR("Color"),
    LUMINOSITY("Luminosity")
}

/**
 * Listener interface for adjustment changes
 */
interface AdjustmentChangeListener {
    fun onParameterChanged(adjustment: AdjustmentLayer, parameterName: String, value: Float)
    fun onParametersChanged(adjustment: AdjustmentLayer)
    fun onEnabledChanged(adjustment: AdjustmentLayer, enabled: Boolean) {}
    fun onOpacityChanged(adjustment: AdjustmentLayer, opacity: Float) {}
    fun onBlendModeChanged(adjustment: AdjustmentLayer, blendMode: BlendMode) {}
}