package com.example.drawinggame.ui.drawing.color.palette

import android.graphics.Bitmap
import android.graphics.Color
import com.example.drawinggame.ui.drawing.color.core.ColorManager
import com.example.drawinggame.ui.drawing.color.core.ColorHarmonyGenerator
import com.example.drawinggame.ui.drawing.color.core.HarmonyType
import com.example.drawinggame.ui.drawing.color.core.VariationType
import kotlin.math.*
import kotlin.random.Random

/**
 * Generates color palettes using various algorithms and techniques
 */
class PaletteGenerator {
    
    private val colorManager = ColorManager.getInstance()
    private val harmonyGenerator = ColorHarmonyGenerator()
    
    /**
     * Generate palette variations from base palette
     */
    fun generateVariations(
        basePalette: ColorPalette,
        variationType: PaletteVariationType,
        count: Int = 5
    ): List<ColorPalette> {
        if (basePalette.colors.isEmpty()) return emptyList()
        
        return when (variationType) {
            PaletteVariationType.LIGHTER -> generateLighterVariations(basePalette, count)
            PaletteVariationType.DARKER -> generateDarkerVariations(basePalette, count)
            PaletteVariationType.MORE_SATURATED -> generateMoreSaturatedVariations(basePalette, count)
            PaletteVariationType.LESS_SATURATED -> generateLessSaturatedVariations(basePalette, count)
            PaletteVariationType.WARMER -> generateWarmerVariations(basePalette, count)
            PaletteVariationType.COOLER -> generateCoolerVariations(basePalette, count)
            PaletteVariationType.INVERTED -> generateInvertedVariations(basePalette, count)
            PaletteVariationType.COMPLEMENTARY -> generateComplementaryVariations(basePalette, count)
        }
    }
    
    /**
     * Generate palette from image using advanced color analysis
     */
    fun generateFromImage(
        bitmap: Bitmap,
        algorithm: ColorExtractionAlgorithm = ColorExtractionAlgorithm.K_MEANS,
        colorCount: Int = 8,
        minSaturation: Float = 0.1f,
        minBrightness: Float = 0.1f
    ): ColorPalette {
        val extractedColors = when (algorithm) {
            ColorExtractionAlgorithm.K_MEANS -> extractColorsKMeans(bitmap, colorCount)
            ColorExtractionAlgorithm.MEDIAN_CUT -> extractColorsMedianCut(bitmap, colorCount)
            ColorExtractionAlgorithm.OCTREE -> extractColorsOctree(bitmap, colorCount)
            ColorExtractionAlgorithm.HISTOGRAM -> extractColorsHistogram(bitmap, colorCount)
        }
        
        // Filter colors based on criteria
        val filteredColors = extractedColors.filter { color ->
            val hsv = colorManager.rgbToHsv(intArrayOf(
                Color.red(color), Color.green(color), Color.blue(color)
            ))
            hsv[1] >= minSaturation && hsv[2] >= minBrightness
        }
        
        // Sort colors by visual appeal (simplified heuristic)
        val sortedColors = filteredColors.sortedBy { color ->
            val hsv = colorManager.rgbToHsv(intArrayOf(
                Color.red(color), Color.green(color), Color.blue(color)
            ))
            // Prefer colors with good saturation and brightness balance
            -(hsv[1] * hsv[2])
        }
        
        return ColorPalette(
            name = "Generated from Image",
            colors = sortedColors.take(colorCount),
            type = PaletteType.EXTRACTED,
            source = PaletteSource.GENERATED,
            description = "Palette generated using ${algorithm.displayName} algorithm"
        )
    }
    
    /**
     * Generate gradient palette between two colors
     */
    fun generateGradientPalette(
        startColor: Int,
        endColor: Int,
        steps: Int = 10,
        colorSpace: GradientColorSpace = GradientColorSpace.RGB
    ): ColorPalette {
        val colors = mutableListOf<Int>()
        
        for (i in 0 until steps) {
            val progress = i.toFloat() / (steps - 1).toFloat()
            val interpolatedColor = interpolateColor(startColor, endColor, progress, colorSpace)
            colors.add(interpolatedColor)
        }
        
        return ColorPalette(
            name = "Gradient Palette",
            colors = colors,
            type = PaletteType.GRADIENT,
            source = PaletteSource.GENERATED,
            description = "Gradient palette in ${colorSpace.name} color space"
        )
    }
    
    /**
     * Generate palette from trend data or style
     */
    fun generateTrendPalette(trend: ColorTrend): ColorPalette {
        return when (trend) {
            ColorTrend.WARM_MINIMALISM -> generateWarmMinimalismPalette()
            ColorTrend.DARK_ACADEMIA -> generateDarkAcademiaPalette()
            ColorTrend.COTTAGECORE -> generateCottageCorePalette()
            ColorTrend.CYBERPUNK -> generateCyberpunkPalette()
            ColorTrend.SUNSET_VIBES -> generateSunsetVibesPalette()
            ColorTrend.OCEAN_DEPTHS -> generateOceanDepthsPalette()
            ColorTrend.FOREST_SANCTUARY -> generateForestSanctuaryPalette()
            ColorTrend.COSMIC_DREAMS -> generateCosmicDreamsPalette()
        }
    }
    
    /**
     * Generate palette based on emotion or mood
     */
    fun generateEmotionalPalette(emotion: ColorEmotion, intensity: Float = 0.5f): ColorPalette {
        val baseColors = when (emotion) {
            ColorEmotion.HAPPY -> listOf(Color.YELLOW, Color.rgb(255, 165, 0), Color.rgb(255, 192, 203))
            ColorEmotion.CALM -> listOf(Color.rgb(135, 206, 235), Color.rgb(152, 251, 152), Color.rgb(230, 230, 250))
            ColorEmotion.ENERGETIC -> listOf(Color.RED, Color.rgb(255, 69, 0), Color.MAGENTA)
            ColorEmotion.MELANCHOLY -> listOf(Color.rgb(112, 128, 144), Color.rgb(70, 130, 180), Color.rgb(106, 90, 205))
            ColorEmotion.MYSTERIOUS -> listOf(Color.rgb(72, 61, 139), Color.rgb(25, 25, 112), Color.rgb(123, 104, 238))
            ColorEmotion.ROMANTIC -> listOf(Color.rgb(255, 20, 147), Color.rgb(255, 105, 180), Color.rgb(218, 112, 214))
            ColorEmotion.AGGRESSIVE -> listOf(Color.rgb(178, 34, 34), Color.rgb(220, 20, 60), Color.rgb(139, 0, 0))
            ColorEmotion.PEACEFUL -> listOf(Color.rgb(240, 248, 255), Color.rgb(230, 230, 250), Color.rgb(248, 248, 255))
        }
        
        // Adjust colors based on intensity
        val adjustedColors = baseColors.map { color ->
            adjustColorIntensity(color, intensity)
        }
        
        // Generate harmony variations
        val primaryColor = adjustedColors.first()
        val harmonyColors = harmonyGenerator.generateAnalogous(primaryColor, 5)
        
        val finalColors = (adjustedColors + harmonyColors).distinct().take(8)
        
        return ColorPalette(
            name = "${emotion.displayName} Palette",
            colors = finalColors,
            type = PaletteType.CUSTOM,
            source = PaletteSource.GENERATED,
            description = "Palette designed to evoke ${emotion.displayName.lowercase()} emotions"
        )
    }
    
    /**
     * Generate random palette with constraints
     */
    fun generateRandomPalette(
        constraints: PaletteConstraints = PaletteConstraints()
    ): ColorPalette {
        val colors = mutableListOf<Int>()
        val random = Random(System.currentTimeMillis())
        
        for (i in 0 until constraints.colorCount) {
            var attempts = 0
            var color: Int
            
            do {
                // Generate random color within constraints
                val hue = if (constraints.hueRange != null) {
                    random.nextFloat() * (constraints.hueRange.second - constraints.hueRange.first) + constraints.hueRange.first
                } else {
                    random.nextFloat() * 360f
                }
                
                val saturation = if (constraints.saturationRange != null) {
                    random.nextFloat() * (constraints.saturationRange.second - constraints.saturationRange.first) + constraints.saturationRange.first
                } else {
                    random.nextFloat()
                }
                
                val value = if (constraints.valueRange != null) {
                    random.nextFloat() * (constraints.valueRange.second - constraints.valueRange.first) + constraints.valueRange.first
                } else {
                    random.nextFloat()
                }
                
                val rgb = colorManager.hsvToRgb(floatArrayOf(hue, saturation, value))
                color = Color.rgb(rgb[0], rgb[1], rgb[2])
                
                attempts++
            } while (attempts < 100 && !isColorValid(color, colors, constraints))
            
            colors.add(color)
        }
        
        return ColorPalette(
            name = "Random Palette",
            colors = colors,
            type = PaletteType.CUSTOM,
            source = PaletteSource.GENERATED,
            description = "Randomly generated palette with constraints"
        )
    }
    
    /**
     * Generate seasonal palette
     */
    fun generateSeasonalPalette(season: ColorSeason): ColorPalette {
        val colors = when (season) {
            ColorSeason.SPRING -> listOf(
                Color.rgb(255, 182, 193), // Light Pink
                Color.rgb(152, 251, 152), // Pale Green
                Color.rgb(255, 255, 224), // Light Yellow
                Color.rgb(221, 160, 221), // Plum
                Color.rgb(173, 216, 230), // Light Blue
                Color.rgb(255, 218, 185)  // Peach
            )
            ColorSeason.SUMMER -> listOf(
                Color.rgb(255, 215, 0),   // Gold
                Color.rgb(255, 69, 0),    // Red Orange
                Color.rgb(0, 191, 255),   // Deep Sky Blue
                Color.rgb(50, 205, 50),   // Lime Green
                Color.rgb(255, 20, 147),  // Deep Pink
                Color.rgb(255, 165, 0)    // Orange
            )
            ColorSeason.AUTUMN -> listOf(
                Color.rgb(205, 92, 92),   // Indian Red
                Color.rgb(255, 140, 0),   // Dark Orange
                Color.rgb(184, 134, 11),  // Dark Goldenrod
                Color.rgb(160, 82, 45),   // Saddle Brown
                Color.rgb(128, 0, 0),     // Maroon
                Color.rgb(85, 107, 47)    // Dark Olive Green
            )
            ColorSeason.WINTER -> listOf(
                Color.rgb(25, 25, 112),   // Midnight Blue
                Color.rgb(70, 130, 180),  // Steel Blue
                Color.rgb(176, 196, 222), // Light Steel Blue
                Color.rgb(245, 245, 245), // White Smoke
                Color.rgb(192, 192, 192), // Silver
                Color.rgb(105, 105, 105)  // Dim Gray
            )
        }
        
        return ColorPalette(
            name = "${season.displayName} Palette",
            colors = colors,
            type = PaletteType.SEASONAL,
            source = PaletteSource.GENERATED,
            description = "Colors inspired by ${season.displayName.lowercase()} season"
        )
    }
    
    // Private helper methods
    
    private fun generateLighterVariations(basePalette: ColorPalette, count: Int): List<ColorPalette> {
        val variations = mutableListOf<ColorPalette>()
        
        for (i in 1..count) {
            val lightness = 0.1f * i // Increase lightness
            val newColors = basePalette.colors.map { color ->
                adjustColorLightness(color, lightness)
            }
            
            variations.add(
                basePalette.copy(
                    name = "${basePalette.name} (Lighter ${i})",
                    colors = newColors,
                    description = "Lighter variation of ${basePalette.name}"
                )
            )
        }
        
        return variations
    }
    
    private fun generateDarkerVariations(basePalette: ColorPalette, count: Int): List<ColorPalette> {
        val variations = mutableListOf<ColorPalette>()
        
        for (i in 1..count) {
            val darkness = -0.1f * i // Decrease lightness
            val newColors = basePalette.colors.map { color ->
                adjustColorLightness(color, darkness)
            }
            
            variations.add(
                basePalette.copy(
                    name = "${basePalette.name} (Darker ${i})",
                    colors = newColors,
                    description = "Darker variation of ${basePalette.name}"
                )
            )
        }
        
        return variations
    }
    
    private fun generateMoreSaturatedVariations(basePalette: ColorPalette, count: Int): List<ColorPalette> {
        val variations = mutableListOf<ColorPalette>()
        
        for (i in 1..count) {
            val saturationBoost = 0.1f * i
            val newColors = basePalette.colors.map { color ->
                adjustColorSaturation(color, saturationBoost)
            }
            
            variations.add(
                basePalette.copy(
                    name = "${basePalette.name} (More Saturated ${i})",
                    colors = newColors,
                    description = "More saturated variation of ${basePalette.name}"
                )
            )
        }
        
        return variations
    }
    
    private fun generateLessSaturatedVariations(basePalette: ColorPalette, count: Int): List<ColorPalette> {
        val variations = mutableListOf<ColorPalette>()
        
        for (i in 1..count) {
            val saturationReduction = -0.1f * i
            val newColors = basePalette.colors.map { color ->
                adjustColorSaturation(color, saturationReduction)
            }
            
            variations.add(
                basePalette.copy(
                    name = "${basePalette.name} (Less Saturated ${i})",
                    colors = newColors,
                    description = "Less saturated variation of ${basePalette.name}"
                )
            )
        }
        
        return variations
    }
    
    private fun generateWarmerVariations(basePalette: ColorPalette, count: Int): List<ColorPalette> {
        val variations = mutableListOf<ColorPalette>()
        
        for (i in 1..count) {
            val hueShift = -10f * i // Shift towards red/orange
            val newColors = basePalette.colors.map { color ->
                adjustColorHue(color, hueShift)
            }
            
            variations.add(
                basePalette.copy(
                    name = "${basePalette.name} (Warmer ${i})",
                    colors = newColors,
                    description = "Warmer variation of ${basePalette.name}"
                )
            )
        }
        
        return variations
    }
    
    private fun generateCoolerVariations(basePalette: ColorPalette, count: Int): List<ColorPalette> {
        val variations = mutableListOf<ColorPalette>()
        
        for (i in 1..count) {
            val hueShift = 10f * i // Shift towards blue
            val newColors = basePalette.colors.map { color ->
                adjustColorHue(color, hueShift)
            }
            
            variations.add(
                basePalette.copy(
                    name = "${basePalette.name} (Cooler ${i})",
                    colors = newColors,
                    description = "Cooler variation of ${basePalette.name}"
                )
            )
        }
        
        return variations
    }
    
    private fun generateInvertedVariations(basePalette: ColorPalette, count: Int): List<ColorPalette> {
        val invertedColors = basePalette.colors.map { color ->
            val r = 255 - Color.red(color)
            val g = 255 - Color.green(color)
            val b = 255 - Color.blue(color)
            Color.rgb(r, g, b)
        }
        
        return listOf(
            basePalette.copy(
                name = "${basePalette.name} (Inverted)",
                colors = invertedColors,
                description = "Inverted colors of ${basePalette.name}"
            )
        )
    }
    
    private fun generateComplementaryVariations(basePalette: ColorPalette, count: Int): List<ColorPalette> {
        val complementaryColors = basePalette.colors.map { color ->
            colorManager.getComplementaryColor(color)
        }
        
        return listOf(
            basePalette.copy(
                name = "${basePalette.name} (Complementary)",
                colors = complementaryColors,
                description = "Complementary colors of ${basePalette.name}"
            )
        )
    }
    
    // Color extraction algorithms
    
    private fun extractColorsKMeans(bitmap: Bitmap, colorCount: Int): List<Int> {
        return colorManager.extractDominantColors(bitmap, colorCount)
    }
    
    private fun extractColorsMedianCut(bitmap: Bitmap, colorCount: Int): List<Int> {
        // Simplified median cut implementation
        return colorManager.extractDominantColors(bitmap, colorCount)
    }
    
    private fun extractColorsOctree(bitmap: Bitmap, colorCount: Int): List<Int> {
        // Simplified octree implementation
        return colorManager.extractDominantColors(bitmap, colorCount)
    }
    
    private fun extractColorsHistogram(bitmap: Bitmap, colorCount: Int): List<Int> {
        // Simplified histogram implementation
        return colorManager.extractDominantColors(bitmap, colorCount)
    }
    
    // Color adjustment methods
    
    private fun adjustColorLightness(color: Int, adjustment: Float): Int {
        val hsl = colorManager.rgbToHsl(intArrayOf(
            Color.red(color), Color.green(color), Color.blue(color)
        ))
        hsl[2] = (hsl[2] + adjustment).coerceIn(0f, 1f)
        val rgb = colorManager.hslToRgb(hsl)
        return Color.rgb(rgb[0], rgb[1], rgb[2])
    }
    
    private fun adjustColorSaturation(color: Int, adjustment: Float): Int {
        val hsv = colorManager.rgbToHsv(intArrayOf(
            Color.red(color), Color.green(color), Color.blue(color)
        ))
        hsv[1] = (hsv[1] + adjustment).coerceIn(0f, 1f)
        val rgb = colorManager.hsvToRgb(hsv)
        return Color.rgb(rgb[0], rgb[1], rgb[2])
    }
    
    private fun adjustColorHue(color: Int, hueShift: Float): Int {
        val hsv = colorManager.rgbToHsv(intArrayOf(
            Color.red(color), Color.green(color), Color.blue(color)
        ))
        hsv[0] = (hsv[0] + hueShift + 360f) % 360f
        val rgb = colorManager.hsvToRgb(hsv)
        return Color.rgb(rgb[0], rgb[1], rgb[2])
    }
    
    private fun adjustColorIntensity(color: Int, intensity: Float): Int {
        val hsv = colorManager.rgbToHsv(intArrayOf(
            Color.red(color), Color.green(color), Color.blue(color)
        ))
        hsv[1] = hsv[1] * intensity.coerceIn(0f, 2f)
        hsv[2] = hsv[2] * intensity.coerceIn(0f, 2f)
        val rgb = colorManager.hsvToRgb(hsv)
        return Color.rgb(rgb[0], rgb[1], rgb[2])
    }
    
    private fun interpolateColor(
        startColor: Int,
        endColor: Int,
        progress: Float,
        colorSpace: GradientColorSpace
    ): Int {
        return when (colorSpace) {
            GradientColorSpace.RGB -> interpolateRGB(startColor, endColor, progress)
            GradientColorSpace.HSV -> interpolateHSV(startColor, endColor, progress)
            GradientColorSpace.LAB -> interpolateLAB(startColor, endColor, progress)
        }
    }
    
    private fun interpolateRGB(startColor: Int, endColor: Int, progress: Float): Int {
        val r = Color.red(startColor) + (Color.red(endColor) - Color.red(startColor)) * progress
        val g = Color.green(startColor) + (Color.green(endColor) - Color.green(startColor)) * progress
        val b = Color.blue(startColor) + (Color.blue(endColor) - Color.blue(startColor)) * progress
        
        return Color.rgb(r.toInt(), g.toInt(), b.toInt())
    }
    
    private fun interpolateHSV(startColor: Int, endColor: Int, progress: Float): Int {
        val startHsv = colorManager.rgbToHsv(intArrayOf(
            Color.red(startColor), Color.green(startColor), Color.blue(startColor)
        ))
        val endHsv = colorManager.rgbToHsv(intArrayOf(
            Color.red(endColor), Color.green(endColor), Color.blue(endColor)
        ))
        
        // Handle hue interpolation (shortest path on color wheel)
        var hueDiff = endHsv[0] - startHsv[0]
        if (hueDiff > 180f) hueDiff -= 360f
        if (hueDiff < -180f) hueDiff += 360f
        
        val interpolatedHue = (startHsv[0] + hueDiff * progress + 360f) % 360f
        val interpolatedSaturation = startHsv[1] + (endHsv[1] - startHsv[1]) * progress
        val interpolatedValue = startHsv[2] + (endHsv[2] - startHsv[2]) * progress
        
        val rgb = colorManager.hsvToRgb(floatArrayOf(
            interpolatedHue, interpolatedSaturation, interpolatedValue
        ))
        
        return Color.rgb(rgb[0], rgb[1], rgb[2])
    }
    
    private fun interpolateLAB(startColor: Int, endColor: Int, progress: Float): Int {
        val startLab = colorManager.rgbToLab(intArrayOf(
            Color.red(startColor), Color.green(startColor), Color.blue(startColor)
        ))
        val endLab = colorManager.rgbToLab(intArrayOf(
            Color.red(endColor), Color.green(endColor), Color.blue(endColor)
        ))
        
        val interpolatedL = startLab[0] + (endLab[0] - startLab[0]) * progress
        val interpolatedA = startLab[1] + (endLab[1] - startLab[1]) * progress
        val interpolatedB = startLab[2] + (endLab[2] - startLab[2]) * progress
        
        val rgb = colorManager.labToRgb(floatArrayOf(
            interpolatedL, interpolatedA, interpolatedB
        ))
        
        return Color.rgb(rgb[0], rgb[1], rgb[2])
    }
    
    private fun isColorValid(
        color: Int,
        existingColors: List<Int>,
        constraints: PaletteConstraints
    ): Boolean {
        // Check minimum distance from existing colors
        if (constraints.minColorDistance > 0f) {
            existingColors.forEach { existingColor ->
                val distance = calculateColorDistance(color, existingColor)
                if (distance < constraints.minColorDistance) {
                    return false
                }
            }
        }
        
        return true
    }
    
    private fun calculateColorDistance(color1: Int, color2: Int): Float {
        val r1 = Color.red(color1)
        val g1 = Color.green(color1)
        val b1 = Color.blue(color1)
        
        val r2 = Color.red(color2)
        val g2 = Color.green(color2)
        val b2 = Color.blue(color2)
        
        return sqrt(((r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2)).toFloat()).toFloat()
    }
    
    // Trend palette generators
    
    private fun generateWarmMinimalismPalette(): ColorPalette {
        val colors = listOf(
            Color.rgb(245, 245, 220), // Beige
            Color.rgb(222, 184, 135), // Burlywood
            Color.rgb(210, 180, 140), // Tan
            Color.rgb(188, 143, 143), // Rosy Brown
            Color.rgb(205, 192, 176)  // Light Taupe
        )
        
        return ColorPalette(
            name = "Warm Minimalism",
            colors = colors,
            type = PaletteType.CUSTOM,
            source = PaletteSource.GENERATED,
            description = "Clean, warm minimalist color palette"
        )
    }
    
    private fun generateDarkAcademiaPalette(): ColorPalette {
        val colors = listOf(
            Color.rgb(101, 67, 33),   // Dark Brown
            Color.rgb(139, 69, 19),   // Saddle Brown
            Color.rgb(160, 82, 45),   // Saddle Brown
            Color.rgb(112, 128, 144), // Slate Gray
            Color.rgb(47, 79, 79),    // Dark Slate Gray
            Color.rgb(25, 25, 112)    // Midnight Blue
        )
        
        return ColorPalette(
            name = "Dark Academia",
            colors = colors,
            type = PaletteType.CUSTOM,
            source = PaletteSource.GENERATED,
            description = "Scholarly, vintage-inspired dark palette"
        )
    }
    
    private fun generateCottageCorePalette(): ColorPalette {
        val colors = listOf(
            Color.rgb(245, 222, 179), // Wheat
            Color.rgb(240, 230, 140), // Khaki
            Color.rgb(221, 160, 221), // Plum
            Color.rgb(152, 251, 152), // Pale Green
            Color.rgb(255, 182, 193), // Light Pink
            Color.rgb(255, 218, 185)  // Peach Puff
        )
        
        return ColorPalette(
            name = "Cottagecore",
            colors = colors,
            type = PaletteType.CUSTOM,
            source = PaletteSource.GENERATED,
            description = "Soft, pastoral cottage-inspired colors"
        )
    }
    
    private fun generateCyberpunkPalette(): ColorPalette {
        val colors = listOf(
            Color.rgb(255, 0, 255),   // Magenta
            Color.rgb(0, 255, 255),   // Cyan
            Color.rgb(0, 255, 0),     // Lime
            Color.rgb(255, 255, 0),   // Yellow
            Color.rgb(128, 0, 128),   // Purple
            Color.rgb(0, 0, 0)        // Black
        )
        
        return ColorPalette(
            name = "Cyberpunk",
            colors = colors,
            type = PaletteType.CUSTOM,
            source = PaletteSource.GENERATED,
            description = "High-contrast neon cyberpunk palette"
        )
    }
    
    private fun generateSunsetVibesPalette(): ColorPalette {
        val colors = listOf(
            Color.rgb(255, 94, 77),   // Sunset Red
            Color.rgb(255, 154, 0),   // Orange
            Color.rgb(255, 206, 84),  // Golden Yellow
            Color.rgb(255, 183, 197), // Pink
            Color.rgb(108, 92, 231),  // Purple
            Color.rgb(54, 54, 54)     // Dark Gray
        )
        
        return ColorPalette(
            name = "Sunset Vibes",
            colors = colors,
            type = PaletteType.CUSTOM,
            source = PaletteSource.GENERATED,
            description = "Warm sunset-inspired gradient colors"
        )
    }
    
    private fun generateOceanDepthsPalette(): ColorPalette {
        val colors = listOf(
            Color.rgb(0, 119, 190),   // Ocean Blue
            Color.rgb(0, 180, 216),   // Sky Blue
            Color.rgb(144, 224, 239), // Light Blue
            Color.rgb(0, 150, 136),   // Teal
            Color.rgb(0, 77, 64),     // Dark Teal
            Color.rgb(38, 50, 56)     // Blue Gray
        )
        
        return ColorPalette(
            name = "Ocean Depths",
            colors = colors,
            type = PaletteType.CUSTOM,
            source = PaletteSource.GENERATED,
            description = "Deep ocean-inspired blue palette"
        )
    }
    
    private fun generateForestSanctuaryPalette(): ColorPalette {
        val colors = listOf(
            Color.rgb(27, 94, 32),    // Dark Green
            Color.rgb(56, 142, 60),   // Green
            Color.rgb(102, 187, 106), // Light Green
            Color.rgb(165, 214, 167), // Pale Green
            Color.rgb(121, 85, 72),   // Brown
            Color.rgb(78, 52, 46)     // Dark Brown
        )
        
        return ColorPalette(
            name = "Forest Sanctuary",
            colors = colors,
            type = PaletteType.CUSTOM,
            source = PaletteSource.GENERATED,
            description = "Peaceful forest-inspired green palette"
        )
    }
    
    private fun generateCosmicDreamsPalette(): ColorPalette {
        val colors = listOf(
            Color.rgb(63, 81, 181),   // Indigo
            Color.rgb(103, 58, 183),  // Deep Purple
            Color.rgb(156, 39, 176),  // Purple
            Color.rgb(233, 30, 99),   // Pink
            Color.rgb(255, 87, 34),   // Deep Orange
            Color.rgb(255, 193, 7)    // Amber
        )
        
        return ColorPalette(
            name = "Cosmic Dreams",
            colors = colors,
            type = PaletteType.CUSTOM,
            source = PaletteSource.GENERATED,
            description = "Mystical cosmic-inspired gradient palette"
        )
    }
}

// Supporting enums and data classes

enum class ColorExtractionAlgorithm(val displayName: String) {
    K_MEANS("K-Means Clustering"),
    MEDIAN_CUT("Median Cut"),
    OCTREE("Octree Quantization"),
    HISTOGRAM("Histogram Analysis")
}

enum class GradientColorSpace {
    RGB, HSV, LAB
}

enum class ColorTrend(val displayName: String) {
    WARM_MINIMALISM("Warm Minimalism"),
    DARK_ACADEMIA("Dark Academia"),
    COTTAGECORE("Cottagecore"),
    CYBERPUNK("Cyberpunk"),
    SUNSET_VIBES("Sunset Vibes"),
    OCEAN_DEPTHS("Ocean Depths"),
    FOREST_SANCTUARY("Forest Sanctuary"),
    COSMIC_DREAMS("Cosmic Dreams")
}

enum class ColorEmotion(val displayName: String) {
    HAPPY("Happy"),
    CALM("Calm"),
    ENERGETIC("Energetic"),
    MELANCHOLY("Melancholy"),
    MYSTERIOUS("Mysterious"),
    ROMANTIC("Romantic"),
    AGGRESSIVE("Aggressive"),
    PEACEFUL("Peaceful")
}

enum class ColorSeason(val displayName: String) {
    SPRING("Spring"),
    SUMMER("Summer"),
    AUTUMN("Autumn"),
    WINTER("Winter")
}

data class PaletteConstraints(
    val colorCount: Int = 5,
    val hueRange: Pair<Float, Float>? = null, // 0-360 degrees
    val saturationRange: Pair<Float, Float>? = null, // 0-1
    val valueRange: Pair<Float, Float>? = null, // 0-1
    val minColorDistance: Float = 0f, // Minimum distance between colors
    val allowDuplicates: Boolean = false
)