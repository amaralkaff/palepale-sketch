package com.example.drawinggame.ui.drawing.color.adjustments

import android.graphics.*
import com.example.drawinggame.ui.drawing.color.core.ColorManager
import kotlin.math.*

/**
 * Hue/Saturation adjustment layer implementation
 * Provides selective color adjustments for different color ranges
 */
class HueSaturationAdjustment : AdjustmentLayer() {
    
    override val type = AdjustmentType.HUE_SATURATION
    override val name = "Hue/Saturation"
    override val description = "Adjust hue, saturation, and lightness for specific color ranges"
    
    companion object {
        const val PARAM_MASTER_HUE = "masterHue"
        const val PARAM_MASTER_SATURATION = "masterSaturation"
        const val PARAM_MASTER_LIGHTNESS = "masterLightness"
        const val PARAM_COLORIZE = "colorize"
        const val PARAM_COLORIZE_HUE = "colorizeHue"
        const val PARAM_COLORIZE_SATURATION = "colorizeSaturation"
        const val PARAM_COLORIZE_LIGHTNESS = "colorizeLightness"
    }
    
    private val colorManager = ColorManager.getInstance()
    
    // Color range adjustments
    private val colorRanges = mutableMapOf<ColorRange, ColorRangeAdjustment>()
    
    init {
        // Register master parameters
        registerParameter(PARAM_MASTER_HUE, 0f, -180f, 180f)
        registerParameter(PARAM_MASTER_SATURATION, 0f, -100f, 100f)
        registerParameter(PARAM_MASTER_LIGHTNESS, 0f, -100f, 100f)
        registerParameter(PARAM_COLORIZE, 0f, 0f, 1f) // Boolean
        registerParameter(PARAM_COLORIZE_HUE, 0f, 0f, 360f)
        registerParameter(PARAM_COLORIZE_SATURATION, 25f, 0f, 100f)
        registerParameter(PARAM_COLORIZE_LIGHTNESS, 0f, -100f, 100f)
        
        // Initialize color ranges
        initializeColorRanges()
    }
    
    override fun apply(source: Bitmap): Bitmap {
        if (!isEnabled || (!hasEffect() && !isColorizeEnabled())) {
            return source.copy(source.config ?: Bitmap.Config.ARGB_8888, false)
        }
        
        return if (isColorizeEnabled()) {
            applyColorize(source)
        } else {
            applyHueSaturationAdjustment(source)
        }
    }
    
    override fun copy(): AdjustmentLayer {
        val copy = HueSaturationAdjustment()
        copy.isEnabled = this.isEnabled
        copy.opacity = this.opacity
        copy.blendMode = this.blendMode
        copy.setParameters(this.getCurrentParameters())
        
        // Copy color range adjustments
        this.colorRanges.forEach { (range, adjustment) ->
            copy.colorRanges[range] = adjustment.copy()
        }
        
        return copy
    }
    
    override fun hasEffect(): Boolean {
        if (super.hasEffect()) return true
        
        // Check if any color range has adjustments
        return colorRanges.values.any { it.hasAdjustment() }
    }
    
    /**
     * Apply hue/saturation adjustments
     */
    private fun applyHueSaturationAdjustment(source: Bitmap): Bitmap {
        val masterHue = getParameter(PARAM_MASTER_HUE)
        val masterSaturation = getParameter(PARAM_MASTER_SATURATION) / 100f
        val masterLightness = getParameter(PARAM_MASTER_LIGHTNESS) / 100f
        
        return applyPixelProcessing(source) { pixel ->
            val a = Color.alpha(pixel)
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)
            
            // Convert to HSL for processing
            val hsl = colorManager.rgbToHsl(intArrayOf(r, g, b))
            var hue = hsl[0]
            var saturation = hsl[1]
            var lightness = hsl[2]
            
            // Apply color range-specific adjustments first
            val rangeAdjustment = getColorRangeAdjustment(hue)
            if (rangeAdjustment.hasAdjustment()) {
                val rangeFactor = calculateRangeFactor(hue, rangeAdjustment.range)
                hue += rangeAdjustment.hue * rangeFactor
                saturation = (saturation + rangeAdjustment.saturation * rangeFactor).coerceIn(0f, 1f)
                lightness = (lightness + rangeAdjustment.lightness * rangeFactor).coerceIn(0f, 1f)
            }
            
            // Apply master adjustments
            if (abs(masterHue) > 0.1f || abs(masterSaturation) > 0.01f || abs(masterLightness) > 0.01f) {
                hue = (hue + masterHue + 360f) % 360f
                saturation = (saturation * (1f + masterSaturation)).coerceIn(0f, 1f)
                lightness = (lightness + masterLightness).coerceIn(0f, 1f)
            }
            
            // Convert back to RGB
            val newRgb = colorManager.hslToRgb(floatArrayOf(hue, saturation, lightness))
            Color.argb(a, newRgb[0], newRgb[1], newRgb[2])
        }
    }
    
    /**
     * Apply colorize effect
     */
    private fun applyColorize(source: Bitmap): Bitmap {
        val colorizeHue = getParameter(PARAM_COLORIZE_HUE)
        val colorizeSaturation = getParameter(PARAM_COLORIZE_SATURATION) / 100f
        val colorizeLightness = getParameter(PARAM_COLORIZE_LIGHTNESS) / 100f
        
        return applyPixelProcessing(source) { pixel ->
            val a = Color.alpha(pixel)
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)
            
            // Convert to HSL to preserve luminance
            val hsl = colorManager.rgbToHsl(intArrayOf(r, g, b))
            var lightness = hsl[2]
            
            // Apply lightness adjustment
            lightness = (lightness + colorizeLightness).coerceIn(0f, 1f)
            
            // Apply colorize with specified hue and saturation
            val newHsl = floatArrayOf(colorizeHue, colorizeSaturation, lightness)
            val newRgb = colorManager.hslToRgb(newHsl)
            
            Color.argb(a, newRgb[0], newRgb[1], newRgb[2])
        }
    }
    
    /**
     * Initialize color ranges
     */
    private fun initializeColorRanges() {
        ColorRange.values().forEach { range ->
            colorRanges[range] = ColorRangeAdjustment(range)
        }
    }
    
    /**
     * Get color range adjustment for a specific hue
     */
    private fun getColorRangeAdjustment(hue: Float): ColorRangeAdjustment {
        // Find the most appropriate color range for this hue
        val normalizedHue = (hue + 360f) % 360f
        
        return colorRanges.values.maxByOrNull { adjustment ->
            calculateRangeFactor(normalizedHue, adjustment.range)
        } ?: colorRanges[ColorRange.MASTER]!!
    }
    
    /**
     * Calculate how much a hue falls within a color range (0-1)
     */
    private fun calculateRangeFactor(hue: Float, range: ColorRange): Float {
        val normalizedHue = (hue + 360f) % 360f
        
        return when (range) {
            ColorRange.MASTER -> 1f
            ColorRange.REDS -> calculateRangeFactor(normalizedHue, 345f, 15f, 30f)
            ColorRange.YELLOWS -> calculateRangeFactor(normalizedHue, 30f, 75f, 30f)
            ColorRange.GREENS -> calculateRangeFactor(normalizedHue, 75f, 165f, 30f)
            ColorRange.CYANS -> calculateRangeFactor(normalizedHue, 165f, 195f, 30f)
            ColorRange.BLUES -> calculateRangeFactor(normalizedHue, 195f, 285f, 30f)
            ColorRange.MAGENTAS -> calculateRangeFactor(normalizedHue, 285f, 345f, 30f)
        }
    }
    
    /**
     * Calculate range factor with falloff
     */
    private fun calculateRangeFactor(hue: Float, centerStart: Float, centerEnd: Float, falloff: Float): Float {
        val normalizedHue = (hue + 360f) % 360f
        val normalizedStart = (centerStart + 360f) % 360f
        val normalizedEnd = (centerEnd + 360f) % 360f
        
        // Handle wrap-around cases
        val distanceToRange = if (normalizedStart > normalizedEnd) {
            // Range wraps around 0 degrees
            if (normalizedHue >= normalizedStart || normalizedHue <= normalizedEnd) {
                0f // Inside range
            } else {
                minOf(
                    abs(normalizedHue - normalizedStart),
                    abs(normalizedHue - normalizedEnd),
                    360f - abs(normalizedHue - normalizedStart),
                    360f - abs(normalizedHue - normalizedEnd)
                )
            }
        } else {
            // Normal range
            if (normalizedHue >= normalizedStart && normalizedHue <= normalizedEnd) {
                0f // Inside range
            } else {
                minOf(
                    abs(normalizedHue - normalizedStart),
                    abs(normalizedHue - normalizedEnd)
                )
            }
        }
        
        return if (distanceToRange <= falloff) {
            1f - (distanceToRange / falloff)
        } else {
            0f
        }
    }
    
    // Public API methods
    
    /**
     * Set master hue adjustment
     */
    fun setMasterHue(hue: Float) {
        setParameter(PARAM_MASTER_HUE, hue)
    }
    
    /**
     * Set master saturation adjustment
     */
    fun setMasterSaturation(saturation: Float) {
        setParameter(PARAM_MASTER_SATURATION, saturation)
    }
    
    /**
     * Set master lightness adjustment
     */
    fun setMasterLightness(lightness: Float) {
        setParameter(PARAM_MASTER_LIGHTNESS, lightness)
    }
    
    /**
     * Enable/disable colorize mode
     */
    fun setColorizeEnabled(enabled: Boolean) {
        setParameter(PARAM_COLORIZE, if (enabled) 1f else 0f)
    }
    
    /**
     * Check if colorize mode is enabled
     */
    fun isColorizeEnabled(): Boolean {
        return getParameter(PARAM_COLORIZE) > 0.5f
    }
    
    /**
     * Set colorize hue
     */
    fun setColorizeHue(hue: Float) {
        setParameter(PARAM_COLORIZE_HUE, hue)
    }
    
    /**
     * Set colorize saturation
     */
    fun setColorizeSaturation(saturation: Float) {
        setParameter(PARAM_COLORIZE_SATURATION, saturation)
    }
    
    /**
     * Set colorize lightness
     */
    fun setColorizeLightness(lightness: Float) {
        setParameter(PARAM_COLORIZE_LIGHTNESS, lightness)
    }
    
    /**
     * Set color range adjustment
     */
    fun setColorRangeAdjustment(
        range: ColorRange,
        hue: Float,
        saturation: Float,
        lightness: Float
    ) {
        val adjustment = colorRanges[range] ?: return
        adjustment.hue = hue.coerceIn(-180f, 180f)
        adjustment.saturation = saturation.coerceIn(-1f, 1f)
        adjustment.lightness = lightness.coerceIn(-1f, 1f)
        notifyParametersChanged()
    }
    
    /**
     * Get color range adjustment
     */
    fun getColorRangeAdjustment(range: ColorRange): ColorRangeAdjustment? {
        return colorRanges[range]
    }
    
    /**
     * Reset color range to default
     */
    fun resetColorRange(range: ColorRange) {
        colorRanges[range]?.reset()
        notifyParametersChanged()
    }
    
    /**
     * Reset all color ranges to default
     */
    fun resetAllColorRanges() {
        colorRanges.values.forEach { it.reset() }
        notifyParametersChanged()
    }
}

/**
 * Color ranges for selective adjustments
 */
enum class ColorRange(val displayName: String, val color: Int) {
    MASTER("Master", Color.GRAY),
    REDS("Reds", Color.RED),
    YELLOWS("Yellows", Color.YELLOW),
    GREENS("Greens", Color.GREEN),
    CYANS("Cyans", Color.CYAN),
    BLUES("Blues", Color.BLUE),
    MAGENTAS("Magentas", Color.MAGENTA)
}

/**
 * Color range adjustment data
 */
data class ColorRangeAdjustment(
    val range: ColorRange,
    var hue: Float = 0f,          // -180 to 180 degrees
    var saturation: Float = 0f,   // -1 to 1 (relative)
    var lightness: Float = 0f     // -1 to 1 (relative)
) {
    
    /**
     * Check if this range has any adjustments
     */
    fun hasAdjustment(): Boolean {
        return abs(hue) > 0.1f || abs(saturation) > 0.01f || abs(lightness) > 0.01f
    }
    
    /**
     * Reset adjustments to default
     */
    fun reset() {
        hue = 0f
        saturation = 0f
        lightness = 0f
    }
    
    /**
     * Copy adjustment settings
     */
    fun copy(): ColorRangeAdjustment {
        return ColorRangeAdjustment(range, hue, saturation, lightness)
    }
    
    /**
     * Get adjustment strength (0-1)
     */
    fun getStrength(): Float {
        val hueStrength = abs(hue) / 180f
        val saturationStrength = abs(saturation)
        val lightnessStrength = abs(lightness)
        
        return maxOf(hueStrength, saturationStrength, lightnessStrength)
    }
}