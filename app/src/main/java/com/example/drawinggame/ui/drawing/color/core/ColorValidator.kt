package com.example.drawinggame.ui.drawing.color.core

import android.graphics.Color
import kotlin.math.*

/**
 * Color validation and accessibility checking utilities
 */
class ColorValidator {
    
    companion object {
        private val instance = ColorValidator()
        fun getInstance(): ColorValidator = instance
        
        // WCAG contrast ratio thresholds
        const val WCAG_AA_NORMAL = 4.5f
        const val WCAG_AA_LARGE = 3.0f
        const val WCAG_AAA_NORMAL = 7.0f
        const val WCAG_AAA_LARGE = 4.5f
        
        // Color vision deficiency simulation
        private val PROTANOPIA_MATRIX = floatArrayOf(
            0.567f, 0.433f, 0.0f,
            0.558f, 0.442f, 0.0f,
            0.0f, 0.242f, 0.758f
        )
        
        private val DEUTERANOPIA_MATRIX = floatArrayOf(
            0.625f, 0.375f, 0.0f,
            0.7f, 0.3f, 0.0f,
            0.0f, 0.3f, 0.7f
        )
        
        private val TRITANOPIA_MATRIX = floatArrayOf(
            0.95f, 0.05f, 0.0f,
            0.0f, 0.433f, 0.567f,
            0.0f, 0.475f, 0.525f
        )
    }
    
    private val colorManager = ColorManager.getInstance()
    
    /**
     * Validate color for accessibility standards
     */
    fun validateColor(color: Int, backgroundColor: Int = Color.WHITE): ColorValidationResult {
        val issues = mutableListOf<ValidationIssue>()
        val suggestions = mutableListOf<String>()
        
        // Check contrast ratio
        val contrastRatio = colorManager.getContrastRatio(color, backgroundColor)
        
        if (contrastRatio < WCAG_AA_NORMAL) {
            issues.add(ValidationIssue.LOW_CONTRAST)
            suggestions.add("Increase contrast to meet WCAG AA standards (4.5:1 minimum)")
        }
        
        if (contrastRatio < WCAG_AAA_NORMAL) {
            issues.add(ValidationIssue.AAA_CONTRAST)
            suggestions.add("Consider higher contrast for AAA compliance (7:1 minimum)")
        }
        
        // Check color blindness accessibility
        val colorBlindnessIssues = checkColorBlindnessAccessibility(color, backgroundColor)
        issues.addAll(colorBlindnessIssues)
        
        // Check if color is distinguishable
        if (isColorTooSimilar(color, backgroundColor)) {
            issues.add(ValidationIssue.SIMILAR_COLORS)
            suggestions.add("Colors are too similar - increase difference in hue, saturation, or brightness")
        }
        
        // Check for extreme values
        val hsv = colorManager.rgbToHsv(intArrayOf(Color.red(color), Color.green(color), Color.blue(color)))
        
        if (hsv[1] > 0.95f && hsv[2] > 0.95f) {
            issues.add(ValidationIssue.TOO_SATURATED)
            suggestions.add("Very saturated colors can cause eye strain - consider reducing saturation")
        }
        
        if (hsv[2] < 0.05f || hsv[2] > 0.95f) {
            issues.add(ValidationIssue.EXTREME_BRIGHTNESS)
            suggestions.add("Extreme brightness values can reduce readability")
        }
        
        val score = calculateAccessibilityScore(color, backgroundColor, issues)
        
        return ColorValidationResult(
            color = color,
            backgroundColor = backgroundColor,
            contrastRatio = contrastRatio,
            issues = issues,
            suggestions = suggestions,
            accessibilityScore = score,
            isAccessible = issues.none { it.severity == ValidationSeverity.ERROR }
        )
    }
    
    /**
     * Check accessibility for color blind users
     */
    private fun checkColorBlindnessAccessibility(color: Int, backgroundColor: Int): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()
        
        // Simulate different types of color blindness
        val protanopia = simulateColorBlindness(color, ColorBlindnessType.PROTANOPIA)
        val deuteranopia = simulateColorBlindness(color, ColorBlindnessType.DEUTERANOPIA)
        val tritanopia = simulateColorBlindness(color, ColorBlindnessType.TRITANOPIA)
        
        val protanopiaBackground = simulateColorBlindness(backgroundColor, ColorBlindnessType.PROTANOPIA)
        val deuteranopiaBackground = simulateColorBlindness(backgroundColor, ColorBlindnessType.DEUTERANOPIA)
        val tritanopiaBackground = simulateColorBlindness(backgroundColor, ColorBlindnessType.TRITANOPIA)
        
        // Check contrast ratios for color blind simulations
        if (colorManager.getContrastRatio(protanopia, protanopiaBackground) < WCAG_AA_NORMAL) {
            issues.add(ValidationIssue.PROTANOPIA_CONTRAST)
        }
        
        if (colorManager.getContrastRatio(deuteranopia, deuteranopiaBackground) < WCAG_AA_NORMAL) {
            issues.add(ValidationIssue.DEUTERANOPIA_CONTRAST)
        }
        
        if (colorManager.getContrastRatio(tritanopia, tritanopiaBackground) < WCAG_AA_NORMAL) {
            issues.add(ValidationIssue.TRITANOPIA_CONTRAST)
        }
        
        return issues
    }
    
    /**
     * Simulate color blindness for a given color
     */
    fun simulateColorBlindness(color: Int, type: ColorBlindnessType): Int {
        val r = Color.red(color) / 255f
        val g = Color.green(color) / 255f
        val b = Color.blue(color) / 255f
        
        val matrix = when (type) {
            ColorBlindnessType.PROTANOPIA -> PROTANOPIA_MATRIX
            ColorBlindnessType.DEUTERANOPIA -> DEUTERANOPIA_MATRIX
            ColorBlindnessType.TRITANOPIA -> TRITANOPIA_MATRIX
            ColorBlindnessType.NONE -> return color
        }
        
        val newR = (matrix[0] * r + matrix[1] * g + matrix[2] * b).coerceIn(0f, 1f)
        val newG = (matrix[3] * r + matrix[4] * g + matrix[5] * b).coerceIn(0f, 1f)
        val newB = (matrix[6] * r + matrix[7] * g + matrix[8] * b).coerceIn(0f, 1f)
        
        return Color.rgb(
            (newR * 255).roundToInt(),
            (newG * 255).roundToInt(),
            (newB * 255).roundToInt()
        )
    }
    
    /**
     * Check if two colors are too similar
     */
    private fun isColorTooSimilar(color1: Int, color2: Int): Boolean {
        val lab1 = colorManager.rgbToLab(intArrayOf(Color.red(color1), Color.green(color1), Color.blue(color1)))
        val lab2 = colorManager.rgbToLab(intArrayOf(Color.red(color2), Color.green(color2), Color.blue(color2)))
        
        // Calculate Delta E (CIE 2000) - simplified version
        val deltaE = calculateDeltaE(lab1, lab2)
        
        return deltaE < 5.0f // Values below 5 are considered too similar
    }
    
    /**
     * Calculate Delta E (color difference) using simplified CIE76 formula
     */
    private fun calculateDeltaE(lab1: FloatArray, lab2: FloatArray): Float {
        val deltaL = lab2[0] - lab1[0]
        val deltaA = lab2[1] - lab1[1]
        val deltaB = lab2[2] - lab1[2]
        
        return sqrt(deltaL * deltaL + deltaA * deltaA + deltaB * deltaB)
    }
    
    /**
     * Calculate overall accessibility score
     */
    private fun calculateAccessibilityScore(color: Int, backgroundColor: Int, issues: List<ValidationIssue>): Float {
        var score = 100f
        
        // Deduct points for each issue
        issues.forEach { issue ->
            score -= when (issue.severity) {
                ValidationSeverity.ERROR -> 25f
                ValidationSeverity.WARNING -> 15f
                ValidationSeverity.INFO -> 5f
            }
        }
        
        // Bonus points for high contrast
        val contrastRatio = colorManager.getContrastRatio(color, backgroundColor)
        if (contrastRatio >= WCAG_AAA_NORMAL) {
            score += 10f
        } else if (contrastRatio >= WCAG_AA_NORMAL) {
            score += 5f
        }
        
        return score.coerceIn(0f, 100f)
    }
    
    /**
     * Suggest accessible color alternatives
     */
    fun suggestAccessibleColors(color: Int, backgroundColor: Int, count: Int = 5): List<Int> {
        val suggestions = mutableListOf<Int>()
        
        val baseHsv = colorManager.rgbToHsv(intArrayOf(Color.red(color), Color.green(color), Color.blue(color)))
        val backgroundHsv = colorManager.rgbToHsv(intArrayOf(Color.red(backgroundColor), Color.green(backgroundColor), Color.blue(backgroundColor)))
        
        // Try different approaches to improve accessibility
        
        // Approach 1: Adjust brightness while keeping hue and saturation
        for (i in 0 until count / 2) {
            val newValue = if (backgroundHsv[2] > 0.5f) {
                // Dark text on light background
                0.1f + (i * 0.15f)
            } else {
                // Light text on dark background
                0.9f - (i * 0.15f)
            }
            
            val newHsv = floatArrayOf(baseHsv[0], baseHsv[1], newValue.coerceIn(0f, 1f))
            val rgb = colorManager.hsvToRgb(newHsv)
            val suggestedColor = Color.rgb(rgb[0], rgb[1], rgb[2])
            
            if (colorManager.getContrastRatio(suggestedColor, backgroundColor) >= WCAG_AA_NORMAL) {
                suggestions.add(suggestedColor)
            }
        }
        
        // Approach 2: Adjust saturation
        for (i in 0 until count / 2) {
            val newSaturation = baseHsv[1] * (0.5f + i * 0.2f)
            val newHsv = floatArrayOf(baseHsv[0], newSaturation.coerceIn(0f, 1f), baseHsv[2])
            val rgb = colorManager.hsvToRgb(newHsv)
            val suggestedColor = Color.rgb(rgb[0], rgb[1], rgb[2])
            
            if (colorManager.getContrastRatio(suggestedColor, backgroundColor) >= WCAG_AA_NORMAL) {
                suggestions.add(suggestedColor)
            }
        }
        
        // Remove duplicates and ensure we have enough suggestions
        val uniqueSuggestions = suggestions.distinct().toMutableList()
        
        // If we don't have enough suggestions, add some safe colors
        if (uniqueSuggestions.size < count) {
            val safeColors = if (backgroundHsv[2] > 0.5f) {
                // Light background - suggest dark colors
                listOf(Color.BLACK, Color.rgb(51, 51, 51), Color.rgb(85, 85, 85))
            } else {
                // Dark background - suggest light colors
                listOf(Color.WHITE, Color.rgb(238, 238, 238), Color.rgb(204, 204, 204))
            }
            
            safeColors.forEach { safeColor ->
                if (!uniqueSuggestions.contains(safeColor) && uniqueSuggestions.size < count) {
                    uniqueSuggestions.add(safeColor)
                }
            }
        }
        
        return uniqueSuggestions.take(count)
    }
    
    /**
     * Check if a color palette is accessible
     */
    fun validatePalette(colors: List<Int>): PaletteValidationResult {
        val issues = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        // Check for sufficient contrast between colors
        for (i in colors.indices) {
            for (j in i + 1 until colors.size) {
                val contrast = colorManager.getContrastRatio(colors[i], colors[j])
                if (contrast < 2.0f) {
                    issues.add("Colors ${i + 1} and ${j + 1} have very low contrast (${String.format("%.1f", contrast)}:1)")
                }
            }
        }
        
        // Check for color blindness accessibility
        val colorBlindTypes = listOf(
            ColorBlindnessType.PROTANOPIA,
            ColorBlindnessType.DEUTERANOPIA,
            ColorBlindnessType.TRITANOPIA
        )
        
        colorBlindTypes.forEach { type ->
            val simulatedColors = colors.map { simulateColorBlindness(it, type) }
            val distinctColors = simulatedColors.distinct()
            
            if (distinctColors.size < colors.size * 0.8f) {
                warnings.add("Palette may be difficult to distinguish for users with ${type.displayName}")
            }
        }
        
        // Check for balanced distribution
        val hues = colors.map { color ->
            val hsv = colorManager.rgbToHsv(intArrayOf(Color.red(color), Color.green(color), Color.blue(color)))
            hsv[0]
        }
        
        val hueRange = (hues.maxOrNull() ?: 0f) - (hues.minOrNull() ?: 0f)
        if (hueRange < 60f && colors.size > 3) {
            warnings.add("Palette has limited hue diversity")
        }
        
        val accessibilityScore = calculatePaletteScore(colors, issues, warnings)
        
        return PaletteValidationResult(
            colors = colors,
            issues = issues,
            warnings = warnings,
            accessibilityScore = accessibilityScore,
            isAccessible = issues.isEmpty()
        )
    }
    
    /**
     * Calculate palette accessibility score
     */
    private fun calculatePaletteScore(colors: List<Int>, issues: List<String>, warnings: List<String>): Float {
        var score = 100f
        
        score -= issues.size * 20f
        score -= warnings.size * 10f
        
        // Bonus for good color distribution
        val hues = colors.map { color ->
            val hsv = colorManager.rgbToHsv(intArrayOf(Color.red(color), Color.green(color), Color.blue(color)))
            hsv[0]
        }
        
        val hueRange = (hues.maxOrNull() ?: 0f) - (hues.minOrNull() ?: 0f)
        if (hueRange > 180f) score += 10f
        
        return score.coerceIn(0f, 100f)
    }
}

/**
 * Color validation result
 */
data class ColorValidationResult(
    val color: Int,
    val backgroundColor: Int,
    val contrastRatio: Float,
    val issues: List<ValidationIssue>,
    val suggestions: List<String>,
    val accessibilityScore: Float,
    val isAccessible: Boolean
) {
    val wcagLevel: String
        get() = when {
            contrastRatio >= ColorValidator.WCAG_AAA_NORMAL -> "AAA"
            contrastRatio >= ColorValidator.WCAG_AA_NORMAL -> "AA"
            contrastRatio >= ColorValidator.WCAG_AA_LARGE -> "AA Large"
            else -> "Fail"
        }
}

/**
 * Palette validation result
 */
data class PaletteValidationResult(
    val colors: List<Int>,
    val issues: List<String>,
    val warnings: List<String>,
    val accessibilityScore: Float,
    val isAccessible: Boolean
)

/**
 * Validation issues
 */
enum class ValidationIssue(val displayName: String, val severity: ValidationSeverity) {
    LOW_CONTRAST("Low contrast ratio", ValidationSeverity.ERROR),
    AAA_CONTRAST("Below AAA contrast standard", ValidationSeverity.WARNING),
    SIMILAR_COLORS("Colors too similar", ValidationSeverity.WARNING),
    TOO_SATURATED("Overly saturated color", ValidationSeverity.INFO),
    EXTREME_BRIGHTNESS("Extreme brightness", ValidationSeverity.WARNING),
    PROTANOPIA_CONTRAST("Poor contrast for protanopia", ValidationSeverity.WARNING),
    DEUTERANOPIA_CONTRAST("Poor contrast for deuteranopia", ValidationSeverity.WARNING),
    TRITANOPIA_CONTRAST("Poor contrast for tritanopia", ValidationSeverity.WARNING)
}

/**
 * Validation severity levels
 */
enum class ValidationSeverity {
    ERROR, WARNING, INFO
}

/**
 * Color blindness types
 */
enum class ColorBlindnessType(val displayName: String) {
    NONE("Normal Vision"),
    PROTANOPIA("Protanopia (Red-blind)"),
    DEUTERANOPIA("Deuteranopia (Green-blind)"),
    TRITANOPIA("Tritanopia (Blue-blind)")
}