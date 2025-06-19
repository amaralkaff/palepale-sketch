package com.example.drawinggame.ui.drawing.color.palette

import android.graphics.Color
import com.example.drawinggame.ui.drawing.color.core.HarmonyType
import java.util.*

/**
 * Color palette data model for Social Sketch
 */
data class ColorPalette(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val colors: List<Int>,
    val type: PaletteType = PaletteType.CUSTOM,
    val source: PaletteSource = PaletteSource.USER_CREATED,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val isReadOnly: Boolean = false,
    val metadata: PaletteMetadata = PaletteMetadata()
) {
    
    val colorCount: Int get() = colors.size
    val isEmpty: Boolean get() = colors.isEmpty()
    val isNotEmpty: Boolean get() = colors.isNotEmpty()
    
    /**
     * Get color at specific index with bounds checking
     */
    fun getColor(index: Int): Int? {
        return if (index in colors.indices) colors[index] else null
    }
    
    /**
     * Get primary color (first color in palette)
     */
    fun getPrimaryColor(): Int {
        return if (colors.isNotEmpty()) colors.first() else Color.BLACK
    }
    
    /**
     * Get secondary color (second color in palette)
     */
    fun getSecondaryColor(): Int {
        return if (colors.size > 1) colors[1] else getPrimaryColor()
    }
    
    /**
     * Add color to palette
     */
    fun addColor(color: Int): ColorPalette {
        if (isReadOnly) return this
        return copy(
            colors = colors + color,
            modifiedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Remove color at index
     */
    fun removeColor(index: Int): ColorPalette {
        if (isReadOnly || index !in colors.indices) return this
        return copy(
            colors = colors.toMutableList().apply { removeAt(index) },
            modifiedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Replace color at index
     */
    fun replaceColor(index: Int, newColor: Int): ColorPalette {
        if (isReadOnly || index !in colors.indices) return this
        return copy(
            colors = colors.toMutableList().apply { set(index, newColor) },
            modifiedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Reorder colors
     */
    fun reorderColors(newOrder: List<Int>): ColorPalette {
        if (isReadOnly) return this
        return copy(
            colors = newOrder,
            modifiedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Create a copy with new name
     */
    fun duplicate(newName: String): ColorPalette {
        return copy(
            id = UUID.randomUUID().toString(),
            name = newName,
            isReadOnly = false,
            source = PaletteSource.USER_CREATED,
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Check if palette contains a specific color
     */
    fun containsColor(color: Int): Boolean {
        return colors.contains(color)
    }
    
    /**
     * Get similar colors within tolerance
     */
    fun getSimilarColors(targetColor: Int, tolerance: Float = 30f): List<Pair<Int, Int>> {
        return colors.mapIndexedNotNull { index, paletteColor ->
            val distance = calculateColorDistance(targetColor, paletteColor)
            if (distance <= tolerance) {
                Pair(index, paletteColor)
            } else {
                null
            }
        }
    }
    
    /**
     * Calculate color distance (simplified RGB distance)
     */
    private fun calculateColorDistance(color1: Int, color2: Int): Float {
        val r1 = Color.red(color1)
        val g1 = Color.green(color1)
        val b1 = Color.blue(color1)
        
        val r2 = Color.red(color2)
        val g2 = Color.green(color2)
        val b2 = Color.blue(color2)
        
        return kotlin.math.sqrt(
            ((r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2)).toFloat()
        )
    }
    
    /**
     * Export palette as hex string
     */
    fun toHexString(): String {
        return colors.joinToString(",") { color ->
            "#${Integer.toHexString(color).uppercase().padStart(8, '0')}"
        }
    }
    
    /**
     * Get palette statistics
     */
    fun getStatistics(): PaletteStatistics {
        if (colors.isEmpty()) {
            return PaletteStatistics()
        }
        
        val hues = mutableListOf<Float>()
        val saturations = mutableListOf<Float>()
        val values = mutableListOf<Float>()
        
        colors.forEach { color ->
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            hues.add(hsv[0])
            saturations.add(hsv[1])
            values.add(hsv[2])
        }
        
        return PaletteStatistics(
            colorCount = colors.size,
            averageHue = hues.average().toFloat(),
            hueRange = (hues.maxOrNull() ?: 0f) - (hues.minOrNull() ?: 0f),
            averageSaturation = saturations.average().toFloat(),
            saturationRange = (saturations.maxOrNull() ?: 0f) - (saturations.minOrNull() ?: 0f),
            averageValue = values.average().toFloat(),
            valueRange = (values.maxOrNull() ?: 0f) - (values.minOrNull() ?: 0f),
            dominantHue = hues.groupingBy { (it / 30f).toInt() * 30f }.eachCount().maxByOrNull { it.value }?.key ?: 0f
        )
    }
}

/**
 * Palette metadata for additional information
 */
data class PaletteMetadata(
    val author: String = "",
    val version: String = "1.0",
    val license: String = "",
    val sourceUrl: String = "",
    val harmonyType: HarmonyType? = null,
    val baseColor: Int? = null,
    val exportFormat: String = "json",
    val customProperties: Map<String, String> = emptyMap()
)

/**
 * Palette statistics for analysis
 */
data class PaletteStatistics(
    val colorCount: Int = 0,
    val averageHue: Float = 0f,
    val hueRange: Float = 0f,
    val averageSaturation: Float = 0f,
    val saturationRange: Float = 0f,
    val averageValue: Float = 0f,
    val valueRange: Float = 0f,
    val dominantHue: Float = 0f
) {
    val isMonochromatic: Boolean get() = hueRange < 30f
    val isHighContrast: Boolean get() = valueRange > 0.7f
    val isVibrant: Boolean get() = averageSaturation > 0.7f
    val isMuted: Boolean get() = averageSaturation < 0.3f
}

/**
 * Types of color palettes
 */
enum class PaletteType(val displayName: String, val description: String) {
    CUSTOM("Custom", "User-created palette"),
    MATERIAL_DESIGN("Material Design", "Google Material Design colors"),
    FLAT_DESIGN("Flat Design", "Modern flat design colors"),
    VINTAGE("Vintage", "Retro and vintage colors"),
    NATURE("Nature", "Earth tones and natural colors"),
    MONOCHROMATIC("Monochromatic", "Single-hue variations"),
    ANALOGOUS("Analogous", "Adjacent color harmonies"),
    COMPLEMENTARY("Complementary", "Opposite color pairs"),
    TRIADIC("Triadic", "Three-color harmonies"),
    TETRADIC("Tetradic", "Four-color harmonies"),
    BRAND("Brand", "Brand color guidelines"),
    SEASONAL("Seasonal", "Season-inspired colors"),
    GRADIENT("Gradient", "Color gradient stops"),
    EXTRACTED("Extracted", "Colors extracted from images")
}

/**
 * Sources of color palettes
 */
enum class PaletteSource(val displayName: String) {
    USER_CREATED("User Created"),
    BUILT_IN("Built-in"),
    IMPORTED("Imported"),
    GENERATED("Generated"),
    EXTRACTED("Extracted from Image"),
    HARMONY("Color Harmony"),
    COMMUNITY("Community Shared"),
    BRAND_GUIDELINES("Brand Guidelines"),
    WEB_SERVICE("Web Service")
}

/**
 * Palette import/export formats
 */
enum class PaletteFormat(
    val extension: String,
    val displayName: String,
    val mimeType: String
) {
    JSON("json", "Social Sketch JSON", "application/json"),
    ASE("ase", "Adobe Swatch Exchange", "application/octet-stream"),
    ACO("aco", "Adobe Color", "application/octet-stream"),
    GPL("gpl", "GIMP Palette", "text/plain"),
    HEX("txt", "Hex Colors", "text/plain"),
    CSS("css", "CSS Variables", "text/css"),
    SCSS("scss", "SCSS Variables", "text/scss"),
    SKETCHPALETTE("sketchpalette", "Sketch Palette", "application/json")
}

/**
 * Palette collection for organizing multiple palettes
 */
data class PaletteCollection(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val paletteIds: List<String>,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis()
) {
    
    val paletteCount: Int get() = paletteIds.size
    val isEmpty: Boolean get() = paletteIds.isEmpty()
    
    /**
     * Add palette to collection
     */
    fun addPalette(paletteId: String): PaletteCollection {
        if (paletteIds.contains(paletteId)) return this
        return copy(
            paletteIds = paletteIds + paletteId,
            modifiedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Remove palette from collection
     */
    fun removePalette(paletteId: String): PaletteCollection {
        return copy(
            paletteIds = paletteIds - paletteId,
            modifiedAt = System.currentTimeMillis()
        )
    }
}