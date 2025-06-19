package com.example.drawinggame.ui.drawing.color.adjustments

import android.graphics.*
import kotlin.math.*

/**
 * Brightness and contrast adjustment layer
 * Provides basic tonal adjustments similar to Photoshop's Brightness/Contrast
 */
class BrightnessContrastAdjustment : AdjustmentLayer() {
    
    override val type = AdjustmentType.BRIGHTNESS_CONTRAST
    override val name = "Brightness/Contrast"
    override val description = "Adjust the overall brightness and contrast of the image"
    
    companion object {
        const val PARAM_BRIGHTNESS = "brightness"
        const val PARAM_CONTRAST = "contrast"
        const val PARAM_USE_LEGACY = "useLegacy"
    }
    
    init {
        // Register parameters: brightness and contrast from -100 to +100
        registerParameter(PARAM_BRIGHTNESS, 0f, -100f, 100f)
        registerParameter(PARAM_CONTRAST, 0f, -100f, 100f)
        registerParameter(PARAM_USE_LEGACY, 0f, 0f, 1f) // Boolean: 0 = modern, 1 = legacy
    }
    
    override fun apply(source: Bitmap): Bitmap {
        if (!isEnabled || !hasEffect()) {
            return source.copy(source.config ?: Bitmap.Config.ARGB_8888, false)
        }
        
        val brightness = getParameter(PARAM_BRIGHTNESS)
        val contrast = getParameter(PARAM_CONTRAST)
        val useLegacy = getParameter(PARAM_USE_LEGACY) > 0.5f
        
        return if (useLegacy) {
            applyLegacyBrightnessContrast(source, brightness, contrast)
        } else {
            applyModernBrightnessContrast(source, brightness, contrast)
        }
    }
    
    override fun copy(): AdjustmentLayer {
        val copy = BrightnessContrastAdjustment()
        copy.isEnabled = this.isEnabled
        copy.opacity = this.opacity
        copy.blendMode = this.blendMode
        copy.setParameters(this.getCurrentParameters())
        return copy
    }
    
    /**
     * Modern brightness/contrast algorithm (similar to Photoshop CS3+)
     * Uses a more sophisticated curve-based approach
     */
    private fun applyModernBrightnessContrast(source: Bitmap, brightness: Float, contrast: Float): Bitmap {
        val brightnessValue = brightness / 100f // Normalize to -1 to 1
        val contrastValue = tan((contrast + 100f) * PI / 400f).toFloat() // Convert to multiplier
        
        return applyPixelProcessing(source) { pixel ->
            val a = Color.alpha(pixel)
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)
            
            // Apply modern brightness/contrast formula
            val newR = applyModernCurve(r, brightnessValue, contrastValue)
            val newG = applyModernCurve(g, brightnessValue, contrastValue)
            val newB = applyModernCurve(b, brightnessValue, contrastValue)
            
            Color.argb(a, newR, newG, newB)
        }
    }
    
    /**
     * Legacy brightness/contrast algorithm (Photoshop CS2 and earlier)
     * Simple linear adjustments
     */
    private fun applyLegacyBrightnessContrast(source: Bitmap, brightness: Float, contrast: Float): Bitmap {
        val brightnessValue = brightness * 255f / 100f // Convert to 0-255 range
        val contrastValue = (contrast + 100f) / 100f // Convert to multiplier
        
        return applyPixelProcessing(source) { pixel ->
            val a = Color.alpha(pixel)
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)
            
            // Apply legacy brightness/contrast formula
            val newR = applyLegacyCurve(r, brightnessValue, contrastValue)
            val newG = applyLegacyCurve(g, brightnessValue, contrastValue)
            val newB = applyLegacyCurve(b, brightnessValue, contrastValue)
            
            Color.argb(a, newR, newG, newB)
        }
    }
    
    /**
     * Apply modern brightness/contrast curve to a single channel
     */
    private fun applyModernCurve(value: Int, brightness: Float, contrast: Float): Int {
        // Normalize to 0-1 range
        var normalized = value / 255f
        
        // Apply brightness
        normalized += brightness
        
        // Apply contrast around midpoint (0.5)
        normalized = ((normalized - 0.5f) * contrast) + 0.5f
        
        // Clamp and convert back to 0-255
        return (normalized * 255f).roundToInt().coerceIn(0, 255)
    }
    
    /**
     * Apply legacy brightness/contrast curve to a single channel
     */
    private fun applyLegacyCurve(value: Int, brightness: Float, contrast: Float): Int {
        // Apply brightness first
        var result = value + brightness
        
        // Apply contrast
        result = ((result - 128f) * contrast + 128f)
        
        return result.roundToInt().coerceIn(0, 255)
    }
    
    /**
     * Alternative implementation using ColorMatrix for better performance
     */
    private fun applyColorMatrix(source: Bitmap, brightness: Float, contrast: Float): Bitmap {
        val brightnessValue = brightness / 100f
        val contrastValue = (contrast + 100f) / 100f
        
        val colorMatrix = ColorMatrix().apply {
            // Create contrast matrix
            val contrastMatrix = ColorMatrix(floatArrayOf(
                contrastValue, 0f, 0f, 0f, brightnessValue * 255f,
                0f, contrastValue, 0f, 0f, brightnessValue * 255f,
                0f, 0f, contrastValue, 0f, brightnessValue * 255f,
                0f, 0f, 0f, 1f, 0f
            ))
            
            postConcat(contrastMatrix)
        }
        
        return applyColorMatrix(source, colorMatrix)
    }
    
    // Convenience methods for external use
    
    /**
     * Set brightness value (-100 to 100)
     */
    fun setBrightness(brightness: Float) {
        setParameter(PARAM_BRIGHTNESS, brightness)
    }
    
    /**
     * Get current brightness value
     */
    fun getBrightness(): Float {
        return getParameter(PARAM_BRIGHTNESS)
    }
    
    /**
     * Set contrast value (-100 to 100)
     */
    fun setContrast(contrast: Float) {
        setParameter(PARAM_CONTRAST, contrast)
    }
    
    /**
     * Get current contrast value
     */
    fun getContrast(): Float {
        return getParameter(PARAM_CONTRAST)
    }
    
    /**
     * Set whether to use legacy algorithm
     */
    fun setUseLegacy(useLegacy: Boolean) {
        setParameter(PARAM_USE_LEGACY, if (useLegacy) 1f else 0f)
    }
    
    /**
     * Check if using legacy algorithm
     */
    fun isUsingLegacy(): Boolean {
        return getParameter(PARAM_USE_LEGACY) > 0.5f
    }
    
    /**
     * Set both brightness and contrast at once
     */
    fun setBrightnessContrast(brightness: Float, contrast: Float) {
        setParameters(mapOf(
            PARAM_BRIGHTNESS to brightness,
            PARAM_CONTRAST to contrast
        ))
    }
    
    /**
     * Get adjustment statistics for UI display
     */
    fun getAdjustmentStats(): BrightnessContrastStats {
        val brightness = getBrightness()
        val contrast = getContrast()
        
        return BrightnessContrastStats(
            brightness = brightness,
            contrast = contrast,
            brightnessPercentage = (brightness + 100f) / 200f,
            contrastPercentage = (contrast + 100f) / 200f,
            isIncreasingBrightness = brightness > 0f,
            isIncreasingContrast = contrast > 0f,
            overallAdjustmentStrength = getAdjustmentStrength()
        )
    }
}

/**
 * Statistics for brightness/contrast adjustment
 */
data class BrightnessContrastStats(
    val brightness: Float,
    val contrast: Float,
    val brightnessPercentage: Float,
    val contrastPercentage: Float,
    val isIncreasingBrightness: Boolean,
    val isIncreasingContrast: Boolean,
    val overallAdjustmentStrength: Float
)