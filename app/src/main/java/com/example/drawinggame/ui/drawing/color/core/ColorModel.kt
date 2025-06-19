package com.example.drawinggame.ui.drawing.color.core

/**
 * Color model definitions and enumerations for Social Sketch color system
 */

/**
 * Supported color models for color picker and conversion
 */
enum class ColorModel(
    val displayName: String,
    val componentNames: Array<String>,
    val componentRanges: Array<Pair<Float, Float>>,
    val componentFormats: Array<String>
) {
    RGB(
        "RGB",
        arrayOf("Red", "Green", "Blue"),
        arrayOf(0f to 255f, 0f to 255f, 0f to 255f),
        arrayOf("%.0f", "%.0f", "%.0f")
    ),
    
    HSV(
        "HSV",
        arrayOf("Hue", "Saturation", "Value"),
        arrayOf(0f to 360f, 0f to 100f, 0f to 100f),
        arrayOf("%.0f°", "%.0f%%", "%.0f%%")
    ),
    
    HSL(
        "HSL",
        arrayOf("Hue", "Saturation", "Lightness"),
        arrayOf(0f to 360f, 0f to 100f, 0f to 100f),
        arrayOf("%.0f°", "%.0f%%", "%.0f%%")
    ),
    
    CMYK(
        "CMYK",
        arrayOf("Cyan", "Magenta", "Yellow", "Key"),
        arrayOf(0f to 100f, 0f to 100f, 0f to 100f, 0f to 100f),
        arrayOf("%.0f%%", "%.0f%%", "%.0f%%", "%.0f%%")
    ),
    
    LAB(
        "LAB",
        arrayOf("Lightness", "A*", "B*"),
        arrayOf(0f to 100f, -128f to 127f, -128f to 127f),
        arrayOf("%.1f", "%.1f", "%.1f")
    );
    
    val componentCount: Int get() = componentNames.size
    
    /**
     * Format component value for display
     */
    fun formatComponent(index: Int, value: Float): String {
        return if (index < componentFormats.size) {
            componentFormats[index].format(value)
        } else {
            "%.1f".format(value)
        }
    }
    
    /**
     * Get component range
     */
    fun getComponentRange(index: Int): Pair<Float, Float> {
        return if (index < componentRanges.size) {
            componentRanges[index]
        } else {
            0f to 100f
        }
    }
}

/**
 * Color harmony types for automatic palette generation
 */
enum class HarmonyType(
    val displayName: String,
    val description: String,
    val colorCount: Int
) {
    MONOCHROMATIC(
        "Monochromatic",
        "Different shades of the same color",
        5
    ),
    
    ANALOGOUS(
        "Analogous",
        "Colors adjacent on the color wheel",
        3
    ),
    
    COMPLEMENTARY(
        "Complementary",
        "Colors opposite on the color wheel",
        2
    ),
    
    TRIADIC(
        "Triadic",
        "Three colors evenly spaced on the color wheel",
        3
    ),
    
    TETRADIC(
        "Tetradic",
        "Four colors forming a rectangle on the color wheel",
        4
    ),
    
    SPLIT_COMPLEMENTARY(
        "Split Complementary",
        "Base color with two colors adjacent to its complement",
        3
    ),
    
    SQUARE(
        "Square",
        "Four colors evenly spaced on the color wheel",
        4
    ),
    
    COMPOUND(
        "Compound",
        "Complex harmony with multiple relationships",
        6
    )
}

/**
 * Blend modes for color mixing
 */
enum class BlendMode(
    val displayName: String,
    val description: String
) {
    NORMAL(
        "Normal",
        "Standard color blending"
    ),
    
    MULTIPLY(
        "Multiply",
        "Darkens by multiplying colors"
    ),
    
    SCREEN(
        "Screen",
        "Lightens by inverting, multiplying, and inverting again"
    ),
    
    OVERLAY(
        "Overlay",
        "Combines multiply and screen based on base color"
    ),
    
    SOFT_LIGHT(
        "Soft Light",
        "Subtle lighting effect"
    ),
    
    HARD_LIGHT(
        "Hard Light",
        "Strong lighting effect"
    ),
    
    COLOR_DODGE(
        "Color Dodge",
        "Brightens base color by decreasing contrast"
    ),
    
    COLOR_BURN(
        "Color Burn",
        "Darkens base color by increasing contrast"
    ),
    
    DARKEN(
        "Darken",
        "Keeps the darker of the two colors"
    ),
    
    LIGHTEN(
        "Lighten",
        "Keeps the lighter of the two colors"
    ),
    
    DIFFERENCE(
        "Difference",
        "Subtracts colors for high contrast effects"
    ),
    
    EXCLUSION(
        "Exclusion",
        "Similar to difference but with lower contrast"
    )
}

/**
 * Color temperature categories
 */
enum class ColorTemperature(
    val displayName: String,
    val kelvinRange: IntRange,
    val description: String
) {
    VERY_WARM(
        "Very Warm",
        1000..2500,
        "Candle light, sunset"
    ),
    
    WARM(
        "Warm",
        2500..3500,
        "Incandescent bulbs, golden hour"
    ),
    
    NEUTRAL(
        "Neutral",
        3500..5500,
        "Fluorescent lights, midday sun"
    ),
    
    COOL(
        "Cool",
        5500..7500,
        "Daylight, flash photography"
    ),
    
    VERY_COOL(
        "Very Cool",
        7500..12000,
        "Overcast sky, blue hour"
    );
    
    companion object {
        fun fromKelvin(kelvin: Int): ColorTemperature {
            return values().firstOrNull { kelvin in it.kelvinRange } ?: NEUTRAL
        }
    }
}

/**
 * Color accessibility standards
 */
enum class ContrastLevel(
    val displayName: String,
    val ratio: Float,
    val description: String
) {
    AA_NORMAL(
        "AA Normal",
        4.5f,
        "WCAG AA standard for normal text"
    ),
    
    AA_LARGE(
        "AA Large",
        3.0f,
        "WCAG AA standard for large text"
    ),
    
    AAA_NORMAL(
        "AAA Normal",
        7.0f,
        "WCAG AAA standard for normal text"
    ),
    
    AAA_LARGE(
        "AAA Large",
        4.5f,
        "WCAG AAA standard for large text"
    );
    
    fun isAccessible(actualRatio: Float): Boolean {
        return actualRatio >= ratio
    }
}

/**
 * Color picker types
 */
enum class ColorPickerType(
    val displayName: String,
    val description: String
) {
    WHEEL(
        "Color Wheel",
        "Traditional color wheel with saturation triangle"
    ),
    
    SLIDER(
        "Sliders",
        "Separate sliders for each color component"
    ),
    
    GRID(
        "Color Grid",
        "Grid of predefined color swatches"
    ),
    
    EYEDROPPER(
        "Eyedropper",
        "Sample colors from artwork or images"
    ),
    
    GRADIENT(
        "Gradient",
        "Select colors from custom gradients"
    ),
    
    HARMONY(
        "Harmony",
        "Generate colors based on color theory"
    ),
    
    PALETTE(
        "Palette",
        "Choose from saved color palettes"
    )
}

/**
 * Color component for individual color values
 */
data class ColorComponent(
    val name: String,
    val value: Float,
    val range: Pair<Float, Float>,
    val format: String
) {
    val normalizedValue: Float
        get() = (value - range.first) / (range.second - range.first)
    
    fun formatValue(): String = format.format(value)
    
    fun withValue(newValue: Float): ColorComponent {
        return copy(value = newValue.coerceIn(range.first, range.second))
    }
}

/**
 * Color space conversion parameters
 */
data class ColorSpace(
    val name: String,
    val gamut: String,
    val whitePoint: String,
    val gamma: Float
) {
    companion object {
        val SRGB = ColorSpace("sRGB", "sRGB", "D65", 2.2f)
        val ADOBE_RGB = ColorSpace("Adobe RGB", "Adobe RGB", "D65", 2.2f)
        val PROPHOTO_RGB = ColorSpace("ProPhoto RGB", "ProPhoto RGB", "D50", 1.8f)
        val DISPLAY_P3 = ColorSpace("Display P3", "P3", "D65", 2.2f)
    }
}