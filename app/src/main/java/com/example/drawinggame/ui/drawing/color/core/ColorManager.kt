package com.example.drawinggame.ui.drawing.color.core

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.*

/**
 * Core color management system for Social Sketch
 * Handles color space conversions, harmony generation, and color analysis
 */
class ColorManager {
    
    companion object {
        private val instance = ColorManager()
        fun getInstance(): ColorManager = instance
        
        // Color temperature reference points
        const val WARM_TEMPERATURE = 3000f
        const val NEUTRAL_TEMPERATURE = 5500f
        const val COOL_TEMPERATURE = 8000f
    }
    
    // Color conversion cache for performance
    private val conversionCache = mutableMapOf<Pair<Int, String>, Any>()
    private val maxCacheSize = 1000
    
    /**
     * Convert RGB to HSV color space
     */
    fun rgbToHsv(rgb: IntArray): FloatArray {
        require(rgb.size == 3) { "RGB array must have 3 components" }
        
        val cacheKey = Pair(Color.rgb(rgb[0], rgb[1], rgb[2]), "hsv")
        conversionCache[cacheKey]?.let { return it as FloatArray }
        
        val r = rgb[0] / 255f
        val g = rgb[1] / 255f
        val b = rgb[2] / 255f
        
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min
        
        // Hue calculation
        val hue = when {
            delta == 0f -> 0f
            max == r -> 60f * ((g - b) / delta % 6f)
            max == g -> 60f * ((b - r) / delta + 2f)
            else -> 60f * ((r - g) / delta + 4f)
        }.let { if (it < 0) it + 360f else it }
        
        // Saturation calculation
        val saturation = if (max == 0f) 0f else (delta / max)
        
        // Value calculation
        val value = max
        
        val result = floatArrayOf(hue, saturation, value)
        cacheIfNeeded(cacheKey, result)
        return result
    }
    
    /**
     * Convert HSV to RGB color space
     */
    fun hsvToRgb(hsv: FloatArray): IntArray {
        require(hsv.size == 3) { "HSV array must have 3 components" }
        
        val h = hsv[0] % 360f
        val s = hsv[1].coerceIn(0f, 1f)
        val v = hsv[2].coerceIn(0f, 1f)
        
        val c = v * s
        val x = c * (1f - abs((h / 60f) % 2f - 1f))
        val m = v - c
        
        val (r1, g1, b1) = when {
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        
        return intArrayOf(
            ((r1 + m) * 255f).roundToInt().coerceIn(0, 255),
            ((g1 + m) * 255f).roundToInt().coerceIn(0, 255),
            ((b1 + m) * 255f).roundToInt().coerceIn(0, 255)
        )
    }
    
    /**
     * Convert RGB to HSL color space
     */
    fun rgbToHsl(rgb: IntArray): FloatArray {
        require(rgb.size == 3) { "RGB array must have 3 components" }
        
        val r = rgb[0] / 255f
        val g = rgb[1] / 255f
        val b = rgb[2] / 255f
        
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min
        
        // Lightness calculation
        val lightness = (max + min) / 2f
        
        // Saturation calculation
        val saturation = when {
            delta == 0f -> 0f
            lightness < 0.5f -> delta / (max + min)
            else -> delta / (2f - max - min)
        }
        
        // Hue calculation (same as HSV)
        val hue = when {
            delta == 0f -> 0f
            max == r -> 60f * ((g - b) / delta % 6f)
            max == g -> 60f * ((b - r) / delta + 2f)
            else -> 60f * ((r - g) / delta + 4f)
        }.let { if (it < 0) it + 360f else it }
        
        return floatArrayOf(hue, saturation, lightness)
    }
    
    /**
     * Convert HSL to RGB color space
     */
    fun hslToRgb(hsl: FloatArray): IntArray {
        require(hsl.size == 3) { "HSL array must have 3 components" }
        
        val h = hsl[0] % 360f
        val s = hsl[1].coerceIn(0f, 1f)
        val l = hsl[2].coerceIn(0f, 1f)
        
        val c = (1f - abs(2f * l - 1f)) * s
        val x = c * (1f - abs((h / 60f) % 2f - 1f))
        val m = l - c / 2f
        
        val (r1, g1, b1) = when {
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        
        return intArrayOf(
            ((r1 + m) * 255f).roundToInt().coerceIn(0, 255),
            ((g1 + m) * 255f).roundToInt().coerceIn(0, 255),
            ((b1 + m) * 255f).roundToInt().coerceIn(0, 255)
        )
    }
    
    /**
     * Convert RGB to CMYK color space
     */
    fun rgbToCmyk(rgb: IntArray): FloatArray {
        require(rgb.size == 3) { "RGB array must have 3 components" }
        
        val r = rgb[0] / 255f
        val g = rgb[1] / 255f
        val b = rgb[2] / 255f
        
        val k = 1f - maxOf(r, g, b)
        
        if (k == 1f) {
            return floatArrayOf(0f, 0f, 0f, 1f)
        }
        
        val c = (1f - r - k) / (1f - k)
        val m = (1f - g - k) / (1f - k)
        val y = (1f - b - k) / (1f - k)
        
        return floatArrayOf(c, m, y, k)
    }
    
    /**
     * Convert CMYK to RGB color space
     */
    fun cmykToRgb(cmyk: FloatArray): IntArray {
        require(cmyk.size == 4) { "CMYK array must have 4 components" }
        
        val c = cmyk[0].coerceIn(0f, 1f)
        val m = cmyk[1].coerceIn(0f, 1f)
        val y = cmyk[2].coerceIn(0f, 1f)
        val k = cmyk[3].coerceIn(0f, 1f)
        
        val r = 255f * (1f - c) * (1f - k)
        val g = 255f * (1f - m) * (1f - k)
        val b = 255f * (1f - y) * (1f - k)
        
        return intArrayOf(
            r.roundToInt().coerceIn(0, 255),
            g.roundToInt().coerceIn(0, 255),
            b.roundToInt().coerceIn(0, 255)
        )
    }
    
    /**
     * Convert RGB to LAB color space (simplified approximation)
     */
    fun rgbToLab(rgb: IntArray): FloatArray {
        // First convert to XYZ, then to LAB
        val xyz = rgbToXyz(rgb)
        return xyzToLab(xyz)
    }
    
    /**
     * Convert LAB to RGB color space
     */
    fun labToRgb(lab: FloatArray): IntArray {
        // First convert to XYZ, then to RGB
        val xyz = labToXyz(lab)
        return xyzToRgb(xyz)
    }
    
    /**
     * Get complementary color (180 degrees on color wheel)
     */
    fun getComplementaryColor(color: Int): Int {
        val hsv = rgbToHsv(intArrayOf(Color.red(color), Color.green(color), Color.blue(color)))
        hsv[0] = (hsv[0] + 180f) % 360f
        val rgb = hsvToRgb(hsv)
        return Color.rgb(rgb[0], rgb[1], rgb[2])
    }
    
    /**
     * Get analogous colors (adjacent colors on color wheel)
     */
    fun getAnalogousColors(color: Int, angle: Float = 30f): List<Int> {
        val hsv = rgbToHsv(intArrayOf(Color.red(color), Color.green(color), Color.blue(color)))
        
        val colors = mutableListOf<Int>()
        
        // Left analogous
        val leftHsv = hsv.clone()
        leftHsv[0] = (leftHsv[0] - angle + 360f) % 360f
        val leftRgb = hsvToRgb(leftHsv)
        colors.add(Color.rgb(leftRgb[0], leftRgb[1], leftRgb[2]))
        
        // Original color
        colors.add(color)
        
        // Right analogous
        val rightHsv = hsv.clone()
        rightHsv[0] = (rightHsv[0] + angle) % 360f
        val rightRgb = hsvToRgb(rightHsv)
        colors.add(Color.rgb(rightRgb[0], rightRgb[1], rightRgb[2]))
        
        return colors
    }
    
    /**
     * Get triadic colors (120 degrees apart)
     */
    fun getTriadicColors(color: Int): List<Int> {
        val hsv = rgbToHsv(intArrayOf(Color.red(color), Color.green(color), Color.blue(color)))
        
        val colors = mutableListOf<Int>()
        colors.add(color)
        
        for (i in 1..2) {
            val newHsv = hsv.clone()
            newHsv[0] = (newHsv[0] + i * 120f) % 360f
            val rgb = hsvToRgb(newHsv)
            colors.add(Color.rgb(rgb[0], rgb[1], rgb[2]))
        }
        
        return colors
    }
    
    /**
     * Get tetradic colors (90 degrees apart)
     */
    fun getTetradicColors(color: Int): List<Int> {
        val hsv = rgbToHsv(intArrayOf(Color.red(color), Color.green(color), Color.blue(color)))
        
        val colors = mutableListOf<Int>()
        colors.add(color)
        
        for (i in 1..3) {
            val newHsv = hsv.clone()
            newHsv[0] = (newHsv[0] + i * 90f) % 360f
            val rgb = hsvToRgb(newHsv)
            colors.add(Color.rgb(rgb[0], rgb[1], rgb[2]))
        }
        
        return colors
    }
    
    /**
     * Get split complementary colors
     */
    fun getSplitComplementaryColors(color: Int, angle: Float = 30f): List<Int> {
        val hsv = rgbToHsv(intArrayOf(Color.red(color), Color.green(color), Color.blue(color)))
        
        val colors = mutableListOf<Int>()
        colors.add(color)
        
        // Split complementary colors (complement ± angle)
        val complementHue = (hsv[0] + 180f) % 360f
        
        val leftHsv = hsv.clone()
        leftHsv[0] = (complementHue - angle + 360f) % 360f
        val leftRgb = hsvToRgb(leftHsv)
        colors.add(Color.rgb(leftRgb[0], leftRgb[1], leftRgb[2]))
        
        val rightHsv = hsv.clone()
        rightHsv[0] = (complementHue + angle) % 360f
        val rightRgb = hsvToRgb(rightHsv)
        colors.add(Color.rgb(rightRgb[0], rightRgb[1], rightRgb[2]))
        
        return colors
    }
    
    /**
     * Extract dominant colors from bitmap using k-means clustering
     */
    fun extractDominantColors(bitmap: Bitmap, count: Int): List<Int> {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        
        // Sample pixels for performance (take every nth pixel)
        val sampleRate = maxOf(1, pixels.size / 10000)
        val sampledPixels = pixels.filterIndexed { index, _ -> index % sampleRate == 0 }
        
        // Use simplified k-means clustering
        return kMeansColors(sampledPixels, count)
    }
    
    /**
     * Calculate color harmony for a set of colors
     */
    fun calculateColorHarmony(colors: List<Int>): ColorHarmony {
        if (colors.isEmpty()) {
            return ColorHarmony(HarmonyType.MONOCHROMATIC, Color.BLACK, emptyList(), 0f)
        }
        
        val baseColor = colors.first()
        val harmonyType = detectHarmonyType(colors)
        val harmonyScore = calculateHarmonyScore(colors, harmonyType)
        
        return ColorHarmony(harmonyType, baseColor, colors, harmonyScore)
    }
    
    /**
     * Get color temperature (warm/cool estimation)
     */
    fun getColorTemperature(color: Int): Float {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        
        // Simplified color temperature estimation
        val warmness = (r + g * 0.5f) / (b + 1f)
        
        return when {
            warmness > 2f -> WARM_TEMPERATURE
            warmness < 1f -> COOL_TEMPERATURE
            else -> NEUTRAL_TEMPERATURE
        }
    }
    
    /**
     * Calculate contrast ratio between two colors (WCAG standard)
     */
    fun getContrastRatio(color1: Int, color2: Int): Float {
        val luminance1 = getRelativeLuminance(color1)
        val luminance2 = getRelativeLuminance(color2)
        
        val lighter = maxOf(luminance1, luminance2)
        val darker = minOf(luminance1, luminance2)
        
        return (lighter + 0.05f) / (darker + 0.05f)
    }
    
    /**
     * Check if contrast is accessible (WCAG AA compliant)
     */
    fun isAccessibleContrast(color1: Int, color2: Int, isLargeText: Boolean = false): Boolean {
        val ratio = getContrastRatio(color1, color2)
        return if (isLargeText) ratio >= 3f else ratio >= 4.5f
    }
    
    /**
     * Blend two colors using specified blend mode
     */
    fun blendColors(color1: Int, color2: Int, blendMode: BlendMode, opacity: Float = 1f): Int {
        val r1 = Color.red(color1) / 255f
        val g1 = Color.green(color1) / 255f
        val b1 = Color.blue(color1) / 255f
        
        val r2 = Color.red(color2) / 255f
        val g2 = Color.green(color2) / 255f
        val b2 = Color.blue(color2) / 255f
        
        val (r, g, b) = when (blendMode) {
            BlendMode.NORMAL -> Triple(r2, g2, b2)
            BlendMode.MULTIPLY -> Triple(r1 * r2, g1 * g2, b1 * b2)
            BlendMode.SCREEN -> Triple(1f - (1f - r1) * (1f - r2), 1f - (1f - g1) * (1f - g2), 1f - (1f - b1) * (1f - b2))
            BlendMode.OVERLAY -> Triple(
                if (r1 < 0.5f) 2f * r1 * r2 else 1f - 2f * (1f - r1) * (1f - r2),
                if (g1 < 0.5f) 2f * g1 * g2 else 1f - 2f * (1f - g1) * (1f - g2),
                if (b1 < 0.5f) 2f * b1 * b2 else 1f - 2f * (1f - b1) * (1f - b2)
            )
            BlendMode.SOFT_LIGHT -> Triple(
                softLightBlend(r1, r2),
                softLightBlend(g1, g2),
                softLightBlend(b1, b2)
            )
            BlendMode.HARD_LIGHT -> Triple(
                if (r2 < 0.5f) 2f * r1 * r2 else 1f - 2f * (1f - r1) * (1f - r2),
                if (g2 < 0.5f) 2f * g1 * g2 else 1f - 2f * (1f - g1) * (1f - g2),
                if (b2 < 0.5f) 2f * b1 * b2 else 1f - 2f * (1f - b1) * (1f - b2)
            )
            BlendMode.COLOR_DODGE -> Triple(
                if (r2 >= 1f) 1f else minOf(1f, r1 / (1f - r2)),
                if (g2 >= 1f) 1f else minOf(1f, g1 / (1f - g2)),
                if (b2 >= 1f) 1f else minOf(1f, b1 / (1f - b2))
            )
            BlendMode.COLOR_BURN -> Triple(
                if (r2 <= 0f) 0f else maxOf(0f, 1f - (1f - r1) / r2),
                if (g2 <= 0f) 0f else maxOf(0f, 1f - (1f - g1) / g2),
                if (b2 <= 0f) 0f else maxOf(0f, 1f - (1f - b1) / b2)
            )
            BlendMode.DARKEN -> Triple(minOf(r1, r2), minOf(g1, g2), minOf(b1, b2))
            BlendMode.LIGHTEN -> Triple(maxOf(r1, r2), maxOf(g1, g2), maxOf(b1, b2))
            BlendMode.DIFFERENCE -> Triple(abs(r1 - r2), abs(g1 - g2), abs(b1 - b2))
            BlendMode.EXCLUSION -> Triple(
                r1 + r2 - 2f * r1 * r2,
                g1 + g2 - 2f * g1 * g2,
                b1 + b2 - 2f * b1 * b2
            )
        }
        
        // Apply opacity
        val finalR = (r1 * (1f - opacity) + r * opacity).coerceIn(0f, 1f)
        val finalG = (g1 * (1f - opacity) + g * opacity).coerceIn(0f, 1f)
        val finalB = (b1 * (1f - opacity) + b * opacity).coerceIn(0f, 1f)
        
        return Color.rgb(
            (finalR * 255f).roundToInt(),
            (finalG * 255f).roundToInt(),
            (finalB * 255f).roundToInt()
        )
    }
    
    // Private helper methods
    
    private fun rgbToXyz(rgb: IntArray): FloatArray {
        var r = rgb[0] / 255f
        var g = rgb[1] / 255f
        var b = rgb[2] / 255f
        
        // Gamma correction
        r = if (r > 0.04045f) ((r + 0.055f) / 1.055f).pow(2.4f) else r / 12.92f
        g = if (g > 0.04045f) ((g + 0.055f) / 1.055f).pow(2.4f) else g / 12.92f
        b = if (b > 0.04045f) ((b + 0.055f) / 1.055f).pow(2.4f) else b / 12.92f
        
        // Convert to XYZ using sRGB matrix
        val x = r * 0.4124f + g * 0.3576f + b * 0.1805f
        val y = r * 0.2126f + g * 0.7152f + b * 0.0722f
        val z = r * 0.0193f + g * 0.1192f + b * 0.9505f
        
        return floatArrayOf(x * 100f, y * 100f, z * 100f)
    }
    
    private fun xyzToRgb(xyz: FloatArray): IntArray {
        val x = xyz[0] / 100f
        val y = xyz[1] / 100f
        val z = xyz[2] / 100f
        
        // Convert from XYZ to RGB using sRGB matrix
        var r = x * 3.2406f + y * -1.5372f + z * -0.4986f
        var g = x * -0.9689f + y * 1.8758f + z * 0.0415f
        var b = x * 0.0557f + y * -0.2040f + z * 1.0570f
        
        // Gamma correction
        r = if (r > 0.0031308f) 1.055f * r.pow(1f / 2.4f) - 0.055f else 12.92f * r
        g = if (g > 0.0031308f) 1.055f * g.pow(1f / 2.4f) - 0.055f else 12.92f * g
        b = if (b > 0.0031308f) 1.055f * b.pow(1f / 2.4f) - 0.055f else 12.92f * b
        
        return intArrayOf(
            (r * 255f).roundToInt().coerceIn(0, 255),
            (g * 255f).roundToInt().coerceIn(0, 255),
            (b * 255f).roundToInt().coerceIn(0, 255)
        )
    }
    
    private fun xyzToLab(xyz: FloatArray): FloatArray {
        // D65 illuminant values
        val xn = 95.047f
        val yn = 100.000f
        val zn = 108.883f
        
        val fx = labFunction(xyz[0] / xn)
        val fy = labFunction(xyz[1] / yn)
        val fz = labFunction(xyz[2] / zn)
        
        val l = 116f * fy - 16f
        val a = 500f * (fx - fy)
        val b = 200f * (fy - fz)
        
        return floatArrayOf(l, a, b)
    }
    
    private fun labToXyz(lab: FloatArray): FloatArray {
        val l = lab[0]
        val a = lab[1]
        val b = lab[2]
        
        val fy = (l + 16f) / 116f
        val fx = a / 500f + fy
        val fz = fy - b / 200f
        
        val xn = 95.047f
        val yn = 100.000f
        val zn = 108.883f
        
        val x = xn * invLabFunction(fx)
        val y = yn * invLabFunction(fy)
        val z = zn * invLabFunction(fz)
        
        return floatArrayOf(x, y, z)
    }
    
    private fun labFunction(t: Float): Float {
        return if (t > 0.008856f) t.pow(1f / 3f) else (7.787f * t + 16f / 116f)
    }
    
    private fun invLabFunction(t: Float): Float {
        return if (t > 0.206893f) t.pow(3f) else (t - 16f / 116f) / 7.787f
    }
    
    private fun getRelativeLuminance(color: Int): Float {
        val r = Color.red(color) / 255f
        val g = Color.green(color) / 255f
        val b = Color.blue(color) / 255f
        
        val rLin = if (r <= 0.03928f) r / 12.92f else ((r + 0.055f) / 1.055f).pow(2.4f)
        val gLin = if (g <= 0.03928f) g / 12.92f else ((g + 0.055f) / 1.055f).pow(2.4f)
        val bLin = if (b <= 0.03928f) b / 12.92f else ((b + 0.055f) / 1.055f).pow(2.4f)
        
        return 0.2126f * rLin + 0.7152f * gLin + 0.0722f * bLin
    }
    
    private fun kMeansColors(pixels: List<Int>, k: Int): List<Int> {
        if (pixels.isEmpty() || k <= 0) return emptyList()
        if (pixels.size <= k) return pixels.distinct()
        
        val maxIterations = 20
        var centroids = pixels.shuffled().take(k).toMutableList()
        
        var iteration = 0
        while (iteration < maxIterations) {
            val clusters = mutableMapOf<Int, MutableList<Int>>()
            centroids.forEach { clusters[it] = mutableListOf() }
            
            // Assign pixels to nearest centroid
            pixels.forEach { pixel ->
                val nearestCentroid = centroids.minByOrNull { centroid ->
                    colorDistance(pixel, centroid)
                } ?: centroids.first()
                clusters[nearestCentroid]?.add(pixel)
            }
            
            // Update centroids
            val newCentroids = mutableListOf<Int>()
            clusters.forEach { (centroid, clusterPixels) ->
                if (clusterPixels.isNotEmpty()) {
                    newCentroids.add(averageColor(clusterPixels))
                } else {
                    newCentroids.add(centroid)
                }
            }
            
            if (newCentroids == centroids) break
            centroids = newCentroids
            iteration++
        }
        
        return centroids.distinct()
    }
    
    private fun colorDistance(color1: Int, color2: Int): Float {
        val r1 = Color.red(color1)
        val g1 = Color.green(color1)
        val b1 = Color.blue(color1)
        
        val r2 = Color.red(color2)
        val g2 = Color.green(color2)
        val b2 = Color.blue(color2)
        
        return sqrt(((r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2)).toFloat()).toFloat()
    }
    
    private fun averageColor(colors: List<Int>): Int {
        if (colors.isEmpty()) return Color.BLACK
        
        val avgR = colors.map { Color.red(it) }.average()
        val avgG = colors.map { Color.green(it) }.average()
        val avgB = colors.map { Color.blue(it) }.average()
        
        return Color.rgb(avgR.roundToInt(), avgG.roundToInt(), avgB.roundToInt())
    }
    
    private fun detectHarmonyType(colors: List<Int>): HarmonyType {
        if (colors.size < 2) return HarmonyType.MONOCHROMATIC
        
        val hues = colors.map { color ->
            val hsv = rgbToHsv(intArrayOf(Color.red(color), Color.green(color), Color.blue(color)))
            hsv[0]
        }
        
        val hueRange = hues.maxOrNull()!! - hues.minOrNull()!!
        
        return when {
            hueRange < 30f -> HarmonyType.MONOCHROMATIC
            hueRange < 60f -> HarmonyType.ANALOGOUS
            colors.size == 2 && abs(hues[1] - hues[0]) > 150f -> HarmonyType.COMPLEMENTARY
            colors.size == 3 -> HarmonyType.TRIADIC
            colors.size == 4 -> HarmonyType.TETRADIC
            else -> HarmonyType.COMPLEMENTARY
        }
    }
    
    private fun calculateHarmonyScore(colors: List<Int>, harmonyType: HarmonyType): Float {
        // Simplified harmony scoring based on hue relationships
        if (colors.size < 2) return 1f
        
        val hues = colors.map { color ->
            val hsv = rgbToHsv(intArrayOf(Color.red(color), Color.green(color), Color.blue(color)))
            hsv[0]
        }
        
        val expectedAngles = when (harmonyType) {
            HarmonyType.COMPLEMENTARY -> listOf(0f, 180f)
            HarmonyType.TRIADIC -> listOf(0f, 120f, 240f)
            HarmonyType.TETRADIC -> listOf(0f, 90f, 180f, 270f)
            HarmonyType.ANALOGOUS -> (0 until colors.size).map { it * 30f }
            else -> listOf(0f)
        }
        
        // Calculate how close the actual hues are to expected harmony
        val deviations = hues.mapIndexed { index, hue ->
            val expectedIndex = index % expectedAngles.size
            val expected = expectedAngles[expectedIndex]
            val deviation = minOf(abs(hue - expected), 360f - abs(hue - expected))
            deviation
        }
        
        val avgDeviation = deviations.average()
        return (1f - avgDeviation.toFloat() / 180f).coerceIn(0f, 1f)
    }
    
    private fun softLightBlend(base: Float, blend: Float): Float {
        return if (blend < 0.5f) {
            2f * base * blend + base * base * (1f - 2f * blend)
        } else {
            2f * base * (1f - blend) + sqrt(base) * (2f * blend - 1f)
        }
    }
    
    private fun cacheIfNeeded(key: Pair<Int, String>, value: Any) {
        if (conversionCache.size >= maxCacheSize) {
            // Remove oldest entries
            val toRemove = conversionCache.keys.take(maxCacheSize / 4)
            toRemove.forEach { conversionCache.remove(it) }
        }
        conversionCache[key] = value
    }
}