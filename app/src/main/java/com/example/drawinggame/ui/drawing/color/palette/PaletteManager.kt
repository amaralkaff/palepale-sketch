package com.example.drawinggame.ui.drawing.color.palette

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.example.drawinggame.ui.drawing.color.core.ColorManager
import com.example.drawinggame.ui.drawing.color.core.ColorHarmonyGenerator
import com.example.drawinggame.ui.drawing.color.core.HarmonyType
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.collections.mutableListOf

/**
 * Manages color palettes for Social Sketch
 * Handles storage, organization, import/export, and generation of color palettes
 */
class PaletteManager(private val context: Context) {
    
    companion object {
        private const val PALETTES_DIR = "palettes"
        private const val COLLECTIONS_DIR = "collections"
        private const val MAX_PALETTES = 500
        private const val MAX_RECENT_PALETTES = 20
    }
    
    private val colorManager = ColorManager.getInstance()
    private val harmonyGenerator = ColorHarmonyGenerator()
    private val paletteGenerator = PaletteGenerator()
    
    // In-memory storage
    private val palettes = mutableMapOf<String, ColorPalette>()
    private val collections = mutableMapOf<String, PaletteCollection>()
    private val recentPalettes = mutableListOf<String>()
    
    // Listeners
    private val paletteListeners = mutableListOf<PaletteListener>()
    
    init {
        // Initialize with built-in palettes
        loadBuiltInPalettes()
        
        // Load user palettes
        loadUserPalettes()
    }
    
    /**
     * Get all palettes
     */
    fun getAllPalettes(): List<ColorPalette> {
        return palettes.values.toList().sortedBy { it.name }
    }
    
    /**
     * Get palettes by type
     */
    fun getPalettesByType(type: PaletteType): List<ColorPalette> {
        return palettes.values.filter { it.type == type }.sortedBy { it.name }
    }
    
    /**
     * Get palettes by source
     */
    fun getPalettesBySource(source: PaletteSource): List<ColorPalette> {
        return palettes.values.filter { it.source == source }.sortedBy { it.name }
    }
    
    /**
     * Get palette by ID
     */
    fun getPalette(id: String): ColorPalette? {
        return palettes[id]
    }
    
    /**
     * Get recent palettes
     */
    fun getRecentPalettes(): List<ColorPalette> {
        return recentPalettes.mapNotNull { palettes[it] }
    }
    
    /**
     * Search palettes by name or tags
     */
    fun searchPalettes(query: String): List<ColorPalette> {
        if (query.isBlank()) return getAllPalettes()
        
        val lowerQuery = query.lowercase()
        return palettes.values.filter { palette ->
            palette.name.lowercase().contains(lowerQuery) ||
            palette.description.lowercase().contains(lowerQuery) ||
            palette.tags.any { it.lowercase().contains(lowerQuery) }
        }.sortedBy { it.name }
    }
    
    /**
     * Create new palette
     */
    fun createPalette(
        name: String,
        colors: List<Int>,
        type: PaletteType = PaletteType.CUSTOM,
        description: String = ""
    ): ColorPalette {
        if (palettes.size >= MAX_PALETTES) {
            throw IllegalStateException("Maximum number of palettes reached")
        }
        
        val palette = ColorPalette(
            name = name,
            colors = colors,
            type = type,
            description = description,
            source = PaletteSource.USER_CREATED
        )
        
        palettes[palette.id] = palette
        addToRecent(palette.id)
        savePalette(palette)
        
        notifyPaletteCreated(palette)
        return palette
    }
    
    /**
     * Update existing palette
     */
    fun updatePalette(palette: ColorPalette): ColorPalette {
        if (palette.isReadOnly) {
            throw IllegalStateException("Cannot modify read-only palette")
        }
        
        val updatedPalette = palette.copy(modifiedAt = System.currentTimeMillis())
        palettes[updatedPalette.id] = updatedPalette
        addToRecent(updatedPalette.id)
        savePalette(updatedPalette)
        
        notifyPaletteUpdated(updatedPalette)
        return updatedPalette
    }
    
    /**
     * Delete palette
     */
    fun deletePalette(id: String): Boolean {
        val palette = palettes[id] ?: return false
        
        if (palette.isReadOnly) {
            throw IllegalStateException("Cannot delete read-only palette")
        }
        
        palettes.remove(id)
        recentPalettes.remove(id)
        deletePaletteFile(id)
        
        notifyPaletteDeleted(palette)
        return true
    }
    
    /**
     * Duplicate palette
     */
    fun duplicatePalette(id: String, newName: String): ColorPalette? {
        val original = palettes[id] ?: return null
        val duplicate = original.duplicate(newName)
        
        palettes[duplicate.id] = duplicate
        addToRecent(duplicate.id)
        savePalette(duplicate)
        
        notifyPaletteCreated(duplicate)
        return duplicate
    }
    
    /**
     * Generate palette from base color
     */
    fun generatePalette(
        baseColor: Int,
        harmonyType: HarmonyType,
        name: String,
        colorCount: Int = harmonyType.colorCount
    ): ColorPalette {
        val harmony = harmonyGenerator.generateHarmony(baseColor, harmonyType, colorCount)
        
        return createPalette(
            name = name,
            colors = harmony.colors,
            type = when (harmonyType) {
                HarmonyType.MONOCHROMATIC -> PaletteType.MONOCHROMATIC
                HarmonyType.ANALOGOUS -> PaletteType.ANALOGOUS
                HarmonyType.COMPLEMENTARY -> PaletteType.COMPLEMENTARY
                HarmonyType.TRIADIC -> PaletteType.TRIADIC
                HarmonyType.TETRADIC -> PaletteType.TETRADIC
                else -> PaletteType.CUSTOM
            },
            description = "Generated ${harmonyType.displayName} palette"
        )
    }
    
    /**
     * Extract palette from bitmap
     */
    fun extractPaletteFromBitmap(
        bitmap: Bitmap,
        name: String,
        colorCount: Int = 8
    ): ColorPalette {
        val extractedColors = colorManager.extractDominantColors(bitmap, colorCount)
        
        return createPalette(
            name = name,
            colors = extractedColors,
            type = PaletteType.EXTRACTED,
            description = "Colors extracted from image"
        )
    }
    
    /**
     * Generate palette variations
     */
    fun generatePaletteVariations(
        basePalette: ColorPalette,
        variationType: PaletteVariationType
    ): List<ColorPalette> {
        return paletteGenerator.generateVariations(basePalette, variationType)
    }
    
    /**
     * Import palette from file
     */
    fun importPalette(file: File, format: PaletteFormat): List<ColorPalette> {
        return when (format) {
            PaletteFormat.JSON -> importJsonPalette(file)
            PaletteFormat.ASE -> importAsePalette(file)
            PaletteFormat.ACO -> importAcoPalette(file)
            PaletteFormat.GPL -> importGplPalette(file)
            PaletteFormat.HEX -> importHexPalette(file)
            else -> throw UnsupportedOperationException("Format ${format.displayName} not supported for import")
        }
    }
    
    /**
     * Export palette to file
     */
    fun exportPalette(
        palette: ColorPalette,
        file: File,
        format: PaletteFormat
    ): Boolean {
        return try {
            when (format) {
                PaletteFormat.JSON -> exportJsonPalette(palette, file)
                PaletteFormat.ASE -> exportAsePalette(palette, file)
                PaletteFormat.GPL -> exportGplPalette(palette, file)
                PaletteFormat.HEX -> exportHexPalette(palette, file)
                PaletteFormat.CSS -> exportCssPalette(palette, file)
                PaletteFormat.SCSS -> exportScssPalette(palette, file)
                else -> throw UnsupportedOperationException("Format ${format.displayName} not supported for export")
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Create palette collection
     */
    fun createCollection(
        name: String,
        paletteIds: List<String>,
        description: String = ""
    ): PaletteCollection {
        val collection = PaletteCollection(
            name = name,
            paletteIds = paletteIds.filter { palettes.containsKey(it) },
            description = description
        )
        
        collections[collection.id] = collection
        saveCollection(collection)
        
        notifyCollectionCreated(collection)
        return collection
    }
    
    /**
     * Get all collections
     */
    fun getAllCollections(): List<PaletteCollection> {
        return collections.values.toList().sortedBy { it.name }
    }
    
    /**
     * Get collection by ID
     */
    fun getCollection(id: String): PaletteCollection? {
        return collections[id]
    }
    
    /**
     * Get palettes in collection
     */
    fun getPalettesInCollection(collectionId: String): List<ColorPalette> {
        val collection = collections[collectionId] ?: return emptyList()
        return collection.paletteIds.mapNotNull { palettes[it] }
    }
    
    /**
     * Add listener
     */
    fun addListener(listener: PaletteListener) {
        paletteListeners.add(listener)
    }
    
    /**
     * Remove listener
     */
    fun removeListener(listener: PaletteListener) {
        paletteListeners.remove(listener)
    }
    
    // Private helper methods
    
    private fun loadBuiltInPalettes() {
        // Material Design palette
        val materialColors = listOf(
            Color.parseColor("#F44336"), // Red
            Color.parseColor("#E91E63"), // Pink
            Color.parseColor("#9C27B0"), // Purple
            Color.parseColor("#673AB7"), // Deep Purple
            Color.parseColor("#3F51B5"), // Indigo
            Color.parseColor("#2196F3"), // Blue
            Color.parseColor("#03A9F4"), // Light Blue
            Color.parseColor("#00BCD4"), // Cyan
            Color.parseColor("#009688"), // Teal
            Color.parseColor("#4CAF50"), // Green
            Color.parseColor("#8BC34A"), // Light Green
            Color.parseColor("#CDDC39"), // Lime
            Color.parseColor("#FFEB3B"), // Yellow
            Color.parseColor("#FFC107"), // Amber
            Color.parseColor("#FF9800"), // Orange
            Color.parseColor("#FF5722")  // Deep Orange
        )
        
        val materialPalette = ColorPalette(
            id = "material_design",
            name = "Material Design",
            colors = materialColors,
            type = PaletteType.MATERIAL_DESIGN,
            source = PaletteSource.BUILT_IN,
            description = "Google Material Design color palette",
            isReadOnly = true
        )
        
        palettes[materialPalette.id] = materialPalette
        
        // Flat Design palette
        val flatColors = listOf(
            Color.parseColor("#1ABC9C"), // Turquoise
            Color.parseColor("#16A085"), // Green Sea
            Color.parseColor("#2ECC71"), // Emerald
            Color.parseColor("#27AE60"), // Nephritis
            Color.parseColor("#3498DB"), // Peter River
            Color.parseColor("#2980B9"), // Belize Hole
            Color.parseColor("#9B59B6"), // Amethyst
            Color.parseColor("#8E44AD"), // Wisteria
            Color.parseColor("#34495E"), // Wet Asphalt
            Color.parseColor("#2C3E50"), // Midnight Blue
            Color.parseColor("#F1C40F"), // Sun Flower
            Color.parseColor("#F39C12"), // Orange
            Color.parseColor("#E67E22"), // Carrot
            Color.parseColor("#D35400"), // Pumpkin
            Color.parseColor("#E74C3C"), // Alizarin
            Color.parseColor("#C0392B")  // Pomegranate
        )
        
        val flatPalette = ColorPalette(
            id = "flat_design",
            name = "Flat Design",
            colors = flatColors,
            type = PaletteType.FLAT_DESIGN,
            source = PaletteSource.BUILT_IN,
            description = "Modern flat design color palette",
            isReadOnly = true
        )
        
        palettes[flatPalette.id] = flatPalette
        
        // Nature palette
        val natureColors = listOf(
            Color.parseColor("#8B4513"), // Saddle Brown
            Color.parseColor("#A0522D"), // Sienna
            Color.parseColor("#CD853F"), // Peru
            Color.parseColor("#DEB887"), // Burlywood
            Color.parseColor("#F4A460"), // Sandy Brown
            Color.parseColor("#228B22"), // Forest Green
            Color.parseColor("#32CD32"), // Lime Green
            Color.parseColor("#90EE90"), // Light Green
            Color.parseColor("#87CEEB"), // Sky Blue
            Color.parseColor("#4682B4"), // Steel Blue
            Color.parseColor("#708090"), // Slate Gray
            Color.parseColor("#2F4F4F")  // Dark Slate Gray
        )
        
        val naturePalette = ColorPalette(
            id = "nature",
            name = "Nature",
            colors = natureColors,
            type = PaletteType.NATURE,
            source = PaletteSource.BUILT_IN,
            description = "Earth tones and natural colors",
            isReadOnly = true
        )
        
        palettes[naturePalette.id] = naturePalette
        
        // Vintage palette
        val vintageColors = listOf(
            Color.parseColor("#8B0000"), // Dark Red
            Color.parseColor("#A0522D"), // Sienna
            Color.parseColor("#D2691E"), // Chocolate
            Color.parseColor("#DAA520"), // Goldenrod
            Color.parseColor("#B8860B"), // Dark Goldenrod
            Color.parseColor("#556B2F"), // Dark Olive Green
            Color.parseColor("#6B8E23"), // Olive Drab
            Color.parseColor("#483D8B"), // Dark Slate Blue
            Color.parseColor("#2F4F4F"), // Dark Slate Gray
            Color.parseColor("#696969")  // Dim Gray
        )
        
        val vintagePalette = ColorPalette(
            id = "vintage",
            name = "Vintage",
            colors = vintageColors,
            type = PaletteType.VINTAGE,
            source = PaletteSource.BUILT_IN,
            description = "Retro and vintage color palette",
            isReadOnly = true
        )
        
        palettes[vintagePalette.id] = vintagePalette
    }
    
    private fun loadUserPalettes() {
        val palettesDir = File(context.filesDir, PALETTES_DIR)
        if (!palettesDir.exists()) return
        
        palettesDir.listFiles()?.forEach { file ->
            if (file.extension == "json") {
                try {
                    val palette = loadPaletteFromFile(file)
                    palettes[palette.id] = palette
                } catch (e: Exception) {
                    // Log error but continue loading other palettes
                }
            }
        }
    }
    
    private fun savePalette(palette: ColorPalette) {
        if (palette.source == PaletteSource.BUILT_IN) return
        
        val palettesDir = File(context.filesDir, PALETTES_DIR)
        if (!palettesDir.exists()) {
            palettesDir.mkdirs()
        }
        
        val file = File(palettesDir, "${palette.id}.json")
        savePaletteToFile(palette, file)
    }
    
    private fun deletePaletteFile(id: String) {
        val palettesDir = File(context.filesDir, PALETTES_DIR)
        val file = File(palettesDir, "$id.json")
        if (file.exists()) {
            file.delete()
        }
    }
    
    private fun saveCollection(collection: PaletteCollection) {
        val collectionsDir = File(context.filesDir, COLLECTIONS_DIR)
        if (!collectionsDir.exists()) {
            collectionsDir.mkdirs()
        }
        
        val file = File(collectionsDir, "${collection.id}.json")
        saveCollectionToFile(collection, file)
    }
    
    private fun addToRecent(paletteId: String) {
        recentPalettes.remove(paletteId)
        recentPalettes.add(0, paletteId)
        
        if (recentPalettes.size > MAX_RECENT_PALETTES) {
            recentPalettes.removeAt(recentPalettes.size - 1)
        }
    }
    
    // Import/Export implementations (simplified)
    
    private fun importJsonPalette(file: File): List<ColorPalette> {
        // JSON import implementation
        return emptyList()
    }
    
    private fun importAsePalette(file: File): List<ColorPalette> {
        // ASE import implementation
        return emptyList()
    }
    
    private fun importAcoPalette(file: File): List<ColorPalette> {
        // ACO import implementation
        return emptyList()
    }
    
    private fun importGplPalette(file: File): List<ColorPalette> {
        // GPL import implementation
        return emptyList()
    }
    
    private fun importHexPalette(file: File): List<ColorPalette> {
        // HEX import implementation
        val lines = file.readLines()
        val colors = mutableListOf<Int>()
        
        lines.forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("#") && trimmed.length == 7) {
                try {
                    colors.add(Color.parseColor(trimmed))
                } catch (e: Exception) {
                    // Skip invalid colors
                }
            }
        }
        
        if (colors.isNotEmpty()) {
            val palette = ColorPalette(
                name = file.nameWithoutExtension,
                colors = colors,
                type = PaletteType.CUSTOM,
                source = PaletteSource.IMPORTED,
                description = "Imported from ${file.name}"
            )
            
            palettes[palette.id] = palette
            savePalette(palette)
            notifyPaletteCreated(palette)
            
            return listOf(palette)
        }
        
        return emptyList()
    }
    
    private fun exportJsonPalette(palette: ColorPalette, file: File): Boolean {
        // JSON export implementation
        return true
    }
    
    private fun exportAsePalette(palette: ColorPalette, file: File): Boolean {
        // ASE export implementation
        return true
    }
    
    private fun exportGplPalette(palette: ColorPalette, file: File): Boolean {
        // GPL export implementation
        return true
    }
    
    private fun exportHexPalette(palette: ColorPalette, file: File): Boolean {
        val hexColors = palette.colors.map { color ->
            "#${Integer.toHexString(color).uppercase().padStart(8, '0').substring(2)}"
        }
        
        file.writeText(hexColors.joinToString("\n"))
        return true
    }
    
    private fun exportCssPalette(palette: ColorPalette, file: File): Boolean {
        val cssVars = palette.colors.mapIndexed { index, color ->
            val hexColor = "#${Integer.toHexString(color).uppercase().padStart(8, '0').substring(2)}"
            "  --color-${palette.name.lowercase().replace(" ", "-")}-${index + 1}: $hexColor;"
        }
        
        val css = ":root {\n${cssVars.joinToString("\n")}\n}"
        file.writeText(css)
        return true
    }
    
    private fun exportScssPalette(palette: ColorPalette, file: File): Boolean {
        val scssVars = palette.colors.mapIndexed { index, color ->
            val hexColor = "#${Integer.toHexString(color).uppercase().padStart(8, '0').substring(2)}"
            "\$color-${palette.name.lowercase().replace(" ", "-")}-${index + 1}: $hexColor;"
        }
        
        file.writeText(scssVars.joinToString("\n"))
        return true
    }
    
    private fun loadPaletteFromFile(file: File): ColorPalette {
        // Simplified JSON loading - in real implementation use proper JSON parser
        throw NotImplementedError("JSON palette loading not implemented")
    }
    
    private fun savePaletteToFile(palette: ColorPalette, file: File) {
        // Simplified JSON saving - in real implementation use proper JSON serialization
    }
    
    private fun saveCollectionToFile(collection: PaletteCollection, file: File) {
        // Simplified JSON saving - in real implementation use proper JSON serialization
    }
    
    // Notification methods
    
    private fun notifyPaletteCreated(palette: ColorPalette) {
        paletteListeners.forEach { it.onPaletteCreated(palette) }
    }
    
    private fun notifyPaletteUpdated(palette: ColorPalette) {
        paletteListeners.forEach { it.onPaletteUpdated(palette) }
    }
    
    private fun notifyPaletteDeleted(palette: ColorPalette) {
        paletteListeners.forEach { it.onPaletteDeleted(palette) }
    }
    
    private fun notifyCollectionCreated(collection: PaletteCollection) {
        paletteListeners.forEach { it.onCollectionCreated(collection) }
    }
}

/**
 * Palette manager listener interface
 */
interface PaletteListener {
    fun onPaletteCreated(palette: ColorPalette) {}
    fun onPaletteUpdated(palette: ColorPalette) {}
    fun onPaletteDeleted(palette: ColorPalette) {}
    fun onCollectionCreated(collection: PaletteCollection) {}
    fun onCollectionUpdated(collection: PaletteCollection) {}
    fun onCollectionDeleted(collection: PaletteCollection) {}
}

/**
 * Types of palette variations
 */
enum class PaletteVariationType(val displayName: String) {
    LIGHTER("Lighter"),
    DARKER("Darker"),
    MORE_SATURATED("More Saturated"),
    LESS_SATURATED("Less Saturated"),
    WARMER("Warmer"),
    COOLER("Cooler"),
    INVERTED("Inverted"),
    COMPLEMENTARY("Complementary")
}