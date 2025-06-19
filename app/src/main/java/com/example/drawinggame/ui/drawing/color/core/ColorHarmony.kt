package com.example.drawinggame.ui.drawing.color.core

import android.graphics.Color
import kotlin.math.*

/**
 * Color harmony data class containing harmony information
 */
data class ColorHarmony(
    val type: HarmonyType,
    val baseColor: Int,
    val colors: List<Int>,
    val harmony: Float // 0.0 to 1.0 harmony score
) {
    val isHarmonious: Boolean get() = harmony >= 0.7f
    
    /**
     * Get harmony as percentage
     */
    fun getHarmonyPercentage(): Int = (harmony * 100).roundToInt()
    
    /**
     * Get dominant color temperature
     */
    fun getDominantTemperature(): ColorTemperature {
        val colorManager = ColorManager.getInstance()
        val temperatures = colors.map { colorManager.getColorTemperature(it) }
        val avgTemperature = temperatures.average()
        return ColorTemperature.fromKelvin(avgTemperature.toInt())
    }
}

/**
 * Color harmony generator for creating pleasing color combinations
 */
class ColorHarmonyGenerator {
    
    private val colorManager = ColorManager.getInstance()
    
    /**
     * Generate color harmony based on base color and harmony type
     */
    fun generateHarmony(baseColor: Int, harmonyType: HarmonyType, variations: Int = 5): ColorHarmony {
        val colors = when (harmonyType) {
            HarmonyType.MONOCHROMATIC -> generateMonochromatic(baseColor, variations)
            HarmonyType.ANALOGOUS -> generateAnalogous(baseColor, variations)
            HarmonyType.COMPLEMENTARY -> generateComplementary(baseColor)
            HarmonyType.TRIADIC -> generateTriadic(baseColor)
            HarmonyType.TETRADIC -> generateTetradic(baseColor)
            HarmonyType.SPLIT_COMPLEMENTARY -> generateSplitComplementary(baseColor)
            HarmonyType.SQUARE -> generateSquare(baseColor)
            HarmonyType.COMPOUND -> generateCompound(baseColor, variations)
        }
        
        val harmonyScore = calculateHarmonyScore(colors, harmonyType)
        return ColorHarmony(harmonyType, baseColor, colors, harmonyScore)
    }
    
    /**
     * Generate monochromatic harmony (different shades/tints of same hue)
     */
    private fun generateMonochromatic(baseColor: Int, count: Int): List<Int> {
        val baseHsv = colorManager.rgbToHsv(intArrayOf(
            Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor)
        ))
        
        val colors = mutableListOf<Int>()
        val baseHue = baseHsv[0]
        val baseSaturation = baseHsv[1]
        
        // Generate variations by changing saturation and value
        for (i in 0 until count) {
            val progress = i.toFloat() / (count - 1).toFloat()
            
            // Create variations with different value levels
            val newValue = when {
                progress < 0.2f -> 0.3f + progress * 2f  // Darker shades
                progress < 0.4f -> 0.5f + (progress - 0.2f) * 2.5f  // Mid tones
                progress < 0.6f -> baseHsv[2]  // Base color
                progress < 0.8f -> baseHsv[2] + (1f - baseHsv[2]) * (progress - 0.6f) * 2.5f  // Lighter tints
                else -> 0.9f + (progress - 0.8f) * 0.5f  // Very light tints
            }
            
            // Slightly adjust saturation for more natural variations
            val newSaturation = baseSaturation * (0.7f + progress * 0.3f)
            
            val newHsv = floatArrayOf(baseHue, newSaturation.coerceIn(0f, 1f), newValue.coerceIn(0f, 1f))
            val rgb = colorManager.hsvToRgb(newHsv)
            colors.add(Color.rgb(rgb[0], rgb[1], rgb[2]))
        }
        
        return colors.distinct()
    }
    
    /**
     * Generate analogous harmony (adjacent colors on color wheel)
     */
    fun generateAnalogous(baseColor: Int, count: Int): List<Int> {
        val baseHsv = colorManager.rgbToHsv(intArrayOf(
            Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor)
        ))
        
        val colors = mutableListOf<Int>()
        val totalAngle = 60f // Total spread in degrees
        val stepAngle = if (count > 1) totalAngle / (count - 1) else 0f
        val startAngle = baseHsv[0] - totalAngle / 2f
        
        for (i in 0 until count) {
            val hue = (startAngle + i * stepAngle + 360f) % 360f
            val newHsv = floatArrayOf(hue, baseHsv[1], baseHsv[2])
            val rgb = colorManager.hsvToRgb(newHsv)
            colors.add(Color.rgb(rgb[0], rgb[1], rgb[2]))
        }
        
        return colors
    }
    
    /**
     * Generate complementary harmony (opposite colors)
     */
    private fun generateComplementary(baseColor: Int): List<Int> {
        val complementary = colorManager.getComplementaryColor(baseColor)
        return listOf(baseColor, complementary)
    }
    
    /**
     * Generate triadic harmony (120 degrees apart)
     */
    private fun generateTriadic(baseColor: Int): List<Int> {
        return colorManager.getTriadicColors(baseColor)
    }
    
    /**
     * Generate tetradic harmony (90 degrees apart)
     */
    private fun generateTetradic(baseColor: Int): List<Int> {
        return colorManager.getTetradicColors(baseColor)
    }
    
    /**
     * Generate split complementary harmony
     */
    private fun generateSplitComplementary(baseColor: Int): List<Int> {
        return colorManager.getSplitComplementaryColors(baseColor, 30f)
    }
    
    /**
     * Generate square harmony (90 degrees apart, like tetradic)
     */
    private fun generateSquare(baseColor: Int): List<Int> {
        return generateTetradic(baseColor)
    }
    
    /**
     * Generate compound harmony (complex relationships)
     */
    private fun generateCompound(baseColor: Int, count: Int): List<Int> {
        val colors = mutableListOf<Int>()
        colors.add(baseColor)
        
        // Add complementary
        colors.add(colorManager.getComplementaryColor(baseColor))
        
        // Add triadic colors
        val triadic = colorManager.getTriadicColors(baseColor)
        colors.addAll(triadic.drop(1)) // Skip the base color
        
        // Add analogous colors if we need more
        if (colors.size < count) {
            val analogous = generateAnalogous(baseColor, count - colors.size + 1)
            colors.addAll(analogous.drop(1)) // Skip the base color
        }
        
        return colors.take(count).distinct()
    }
    
    /**
     * Calculate harmony score based on color relationships
     */
    private fun calculateHarmonyScore(colors: List<Int>, harmonyType: HarmonyType): Float {
        if (colors.size < 2) return 1f
        
        val hues = colors.map { color ->
            val hsv = colorManager.rgbToHsv(intArrayOf(
                Color.red(color), Color.green(color), Color.blue(color)
            ))
            hsv[0]
        }
        
        val saturations = colors.map { color ->
            val hsv = colorManager.rgbToHsv(intArrayOf(
                Color.red(color), Color.green(color), Color.blue(color)
            ))
            hsv[1]
        }
        
        val values = colors.map { color ->
            val hsv = colorManager.rgbToHsv(intArrayOf(
                Color.red(color), Color.green(color), Color.blue(color)
            ))
            hsv[2]
        }
        
        // Calculate hue harmony
        val hueHarmony = calculateHueHarmony(hues, harmonyType)
        
        // Calculate saturation harmony (prefer similar saturations)
        val saturationVariance = calculateVariance(saturations)
        val saturationHarmony = 1f - (saturationVariance * 2f).coerceIn(0f, 1f)
        
        // Calculate value harmony (some contrast is good)
        val valueVariance = calculateVariance(values)
        val valueHarmony = when {
            valueVariance < 0.1f -> 0.6f // Too little contrast
            valueVariance > 0.4f -> 0.6f // Too much contrast
            else -> 1f - abs(valueVariance - 0.25f) * 2f // Optimal around 0.25
        }
        
        // Weighted combination
        return (hueHarmony * 0.5f + saturationHarmony * 0.3f + valueHarmony * 0.2f).coerceIn(0f, 1f)
    }
    
    /**
     * Calculate hue harmony based on expected relationships
     */
    private fun calculateHueHarmony(hues: List<Float>, harmonyType: HarmonyType): Float {
        if (hues.size < 2) return 1f
        
        val expectedAngles = when (harmonyType) {
            HarmonyType.MONOCHROMATIC -> listOf(0f) // All same hue
            HarmonyType.ANALOGOUS -> (0..2).map { it * 30f } // 30 degree steps
            HarmonyType.COMPLEMENTARY -> listOf(0f, 180f)
            HarmonyType.TRIADIC -> listOf(0f, 120f, 240f)
            HarmonyType.TETRADIC, HarmonyType.SQUARE -> listOf(0f, 90f, 180f, 270f)
            HarmonyType.SPLIT_COMPLEMENTARY -> listOf(0f, 150f, 210f)
            HarmonyType.COMPOUND -> return 0.8f // Compound is inherently less harmonious
        }
        
        if (harmonyType == HarmonyType.MONOCHROMATIC) {
            // For monochromatic, check if all hues are similar
            val hueVariance = calculateVariance(hues)
            return (1f - hueVariance * 10f).coerceIn(0f, 1f)
        }
        
        // Normalize hues relative to first hue
        val baseHue = hues.first()
        val normalizedHues = hues.map { hue ->
            val diff = hue - baseHue
            when {
                diff > 180f -> diff - 360f
                diff < -180f -> diff + 360f
                else -> diff
            }.let { it + 360f } % 360f
        }
        
        // Calculate deviations from expected angles
        val deviations = normalizedHues.mapIndexed { index, hue ->
            if (index < expectedAngles.size) {
                val expected = expectedAngles[index]
                val deviation = minOf(abs(hue - expected), 360f - abs(hue - expected))
                deviation
            } else {
                0f
            }
        }
        
        val avgDeviation = deviations.average()
        return (1f - avgDeviation.toFloat() / 90f).coerceIn(0f, 1f) // 90 degrees = 50% harmony
    }
    
    /**
     * Calculate variance of a list of values
     */
    private fun calculateVariance(values: List<Float>): Float {
        if (values.size < 2) return 0f
        
        val mean = values.average().toFloat()
        val squaredDiffs = values.map { (it - mean) * (it - mean) }
        return squaredDiffs.average().toFloat()
    }
    
    /**
     * Generate color variations with different approaches
     */
    fun generateColorVariations(baseColor: Int, type: VariationType, count: Int = 5): List<Int> {
        return when (type) {
            VariationType.TINTS -> generateTints(baseColor, count)
            VariationType.SHADES -> generateShades(baseColor, count)
            VariationType.TONES -> generateTones(baseColor, count)
            VariationType.TEMPERATURE_WARM -> generateTemperatureVariations(baseColor, count, true)
            VariationType.TEMPERATURE_COOL -> generateTemperatureVariations(baseColor, count, false)
            VariationType.SATURATION -> generateSaturationVariations(baseColor, count)
        }
    }
    
    /**
     * Generate tints (add white)
     */
    private fun generateTints(baseColor: Int, count: Int): List<Int> {
        val colors = mutableListOf<Int>()
        val baseHsv = colorManager.rgbToHsv(intArrayOf(
            Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor)
        ))
        
        for (i in 0 until count) {
            val progress = i.toFloat() / (count - 1).toFloat()
            val newValue = baseHsv[2] + (1f - baseHsv[2]) * progress
            val newSaturation = baseHsv[1] * (1f - progress * 0.5f) // Reduce saturation as we add white
            
            val newHsv = floatArrayOf(baseHsv[0], newSaturation, newValue.coerceIn(0f, 1f))
            val rgb = colorManager.hsvToRgb(newHsv)
            colors.add(Color.rgb(rgb[0], rgb[1], rgb[2]))
        }
        
        return colors
    }
    
    /**
     * Generate shades (add black)
     */
    private fun generateShades(baseColor: Int, count: Int): List<Int> {
        val colors = mutableListOf<Int>()
        val baseHsv = colorManager.rgbToHsv(intArrayOf(
            Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor)
        ))
        
        for (i in 0 until count) {
            val progress = i.toFloat() / (count - 1).toFloat()
            val newValue = baseHsv[2] * (1f - progress)
            
            val newHsv = floatArrayOf(baseHsv[0], baseHsv[1], newValue.coerceIn(0f, 1f))
            val rgb = colorManager.hsvToRgb(newHsv)
            colors.add(Color.rgb(rgb[0], rgb[1], rgb[2]))
        }
        
        return colors
    }
    
    /**
     * Generate tones (add gray)
     */
    private fun generateTones(baseColor: Int, count: Int): List<Int> {
        val colors = mutableListOf<Int>()
        val baseHsv = colorManager.rgbToHsv(intArrayOf(
            Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor)
        ))
        
        for (i in 0 until count) {
            val progress = i.toFloat() / (count - 1).toFloat()
            val newSaturation = baseHsv[1] * (1f - progress)
            
            val newHsv = floatArrayOf(baseHsv[0], newSaturation.coerceIn(0f, 1f), baseHsv[2])
            val rgb = colorManager.hsvToRgb(newHsv)
            colors.add(Color.rgb(rgb[0], rgb[1], rgb[2]))
        }
        
        return colors
    }
    
    /**
     * Generate temperature variations
     */
    private fun generateTemperatureVariations(baseColor: Int, count: Int, warmer: Boolean): List<Int> {
        val colors = mutableListOf<Int>()
        val baseHsv = colorManager.rgbToHsv(intArrayOf(
            Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor)
        ))
        
        for (i in 0 until count) {
            val progress = i.toFloat() / (count - 1).toFloat()
            val hueShift = if (warmer) -30f * progress else 30f * progress // Shift towards orange/red or blue
            val newHue = (baseHsv[0] + hueShift + 360f) % 360f
            
            val newHsv = floatArrayOf(newHue, baseHsv[1], baseHsv[2])
            val rgb = colorManager.hsvToRgb(newHsv)
            colors.add(Color.rgb(rgb[0], rgb[1], rgb[2]))
        }
        
        return colors
    }
    
    /**
     * Generate saturation variations
     */
    private fun generateSaturationVariations(baseColor: Int, count: Int): List<Int> {
        val colors = mutableListOf<Int>()
        val baseHsv = colorManager.rgbToHsv(intArrayOf(
            Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor)
        ))
        
        for (i in 0 until count) {
            val progress = i.toFloat() / (count - 1).toFloat()
            val newSaturation = progress
            
            val newHsv = floatArrayOf(baseHsv[0], newSaturation, baseHsv[2])
            val rgb = colorManager.hsvToRgb(newHsv)
            colors.add(Color.rgb(rgb[0], rgb[1], rgb[2]))
        }
        
        return colors
    }
}

/**
 * Types of color variations
 */
enum class VariationType(val displayName: String) {
    TINTS("Tints"),
    SHADES("Shades"),
    TONES("Tones"),
    TEMPERATURE_WARM("Warmer"),
    TEMPERATURE_COOL("Cooler"),
    SATURATION("Saturation")
}