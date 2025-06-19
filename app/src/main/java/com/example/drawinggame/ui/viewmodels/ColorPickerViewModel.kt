package com.example.drawinggame.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.graphics.Color
import com.example.drawinggame.ui.drawing.color.core.*
import com.example.drawinggame.ui.drawing.color.palette.ColorPalette
import com.example.drawinggame.ui.drawing.color.palette.PaletteManager
import kotlinx.coroutines.launch

/**
 * ViewModel for managing color picker state and operations
 */
class ColorPickerViewModel : ViewModel() {
    
    private val colorManager = ColorManager.getInstance()
    private val harmonyGenerator = ColorHarmonyGenerator()
    
    // Current color state
    private val _currentColor = MutableLiveData<Int>()
    val currentColor: LiveData<Int> = _currentColor
    
    private val _previousColor = MutableLiveData<Int>()
    val previousColor: LiveData<Int> = _previousColor
    
    // Color space values
    private val _rgbValues = MutableLiveData<IntArray>()
    val rgbValues: LiveData<IntArray> = _rgbValues
    
    private val _hslValues = MutableLiveData<FloatArray>()
    val hslValues: LiveData<FloatArray> = _hslValues
    
    private val _hexValue = MutableLiveData<String>()
    val hexValue: LiveData<String> = _hexValue
    
    // Color harmony
    private val _harmonyColors = MutableLiveData<List<Int>>()
    val harmonyColors: LiveData<List<Int>> = _harmonyColors
    
    private val _currentHarmonyType = MutableLiveData<HarmonyType>()
    val currentHarmonyType: LiveData<HarmonyType> = _currentHarmonyType
    
    // Color palettes
    private val _availablePalettes = MutableLiveData<List<ColorPalette>>()
    val availablePalettes: LiveData<List<ColorPalette>> = _availablePalettes
    
    private val _recentColors = MutableLiveData<List<Int>>()
    val recentColors: LiveData<List<Int>> = _recentColors
    
    // Color validation
    private val _colorValidation = MutableLiveData<ColorValidationResult>()
    val colorValidation: LiveData<ColorValidationResult> = _colorValidation
    
    // Loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val recentColorsList = mutableListOf<Int>()
    private val maxRecentColors = 20
    
    init {
        // Initialize with a default color
        setColor(Color.RED)
        _currentHarmonyType.value = HarmonyType.ANALOGOUS
    }
    
    /**
     * Set the current color and update all related values
     */
    fun setColor(color: Int) {
        val previous = _currentColor.value ?: Color.RED
        _previousColor.value = previous
        _currentColor.value = color
        
        // Add to recent colors
        addToRecentColors(color)
        
        // Update color space values
        updateColorSpaceValues(color)
        
        // Update color harmony
        updateHarmonyColors(color)
        
        // Validate color accessibility
        validateColor(color)
    }
    
    /**
     * Set color from RGB values
     */
    fun setColorFromRGB(red: Int, green: Int, blue: Int) {
        val color = Color.rgb(red, green, blue)
        setColor(color)
    }
    
    /**
     * Set color from HSL values
     */
    fun setColorFromHSL(hue: Float, saturation: Float, lightness: Float) {
        val hsl = floatArrayOf(hue, saturation, lightness)
        val rgb = colorManager.hslToRgb(hsl)
        val color = Color.rgb(rgb[0], rgb[1], rgb[2])
        setColor(color)
    }
    
    /**
     * Set color from hex string
     */
    fun setColorFromHex(hexString: String): Boolean {
        return try {
            val cleanHex = hexString.replace("#", "")
            val color = Color.parseColor("#$cleanHex")
            setColor(color)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Revert to previous color
     */
    fun revertToPreviousColor() {
        _previousColor.value?.let { previousColor ->
            setColor(previousColor)
        }
    }
    
    /**
     * Update harmony type and regenerate harmony colors
     */
    fun setHarmonyType(harmonyType: HarmonyType) {
        _currentHarmonyType.value = harmonyType
        _currentColor.value?.let { color ->
            updateHarmonyColors(color, harmonyType)
        }
    }
    
    /**
     * Load available color palettes
     */
    fun loadPalettes(paletteManager: PaletteManager) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // TODO: Implement palette loading when PaletteManager methods are available
                _availablePalettes.value = emptyList()
            } catch (e: Exception) {
                // Handle error
                _availablePalettes.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Get color temperature information
     */
    fun getColorTemperature(): Float {
        return _currentColor.value?.let { color ->
            colorManager.getColorTemperature(color)
        } ?: ColorManager.NEUTRAL_TEMPERATURE
    }
    
    /**
     * Get color accessibility information
     */
    fun getAccessibilityInfo(backgroundColor: Int = Color.WHITE): String {
        return _currentColor.value?.let { color ->
            val contrastRatio = colorManager.getContrastRatio(color, backgroundColor)
            val isAccessible = colorManager.isAccessibleContrast(color, backgroundColor)
            
            buildString {
                append("Contrast Ratio: ${String.format("%.2f", contrastRatio)}")
                append("\n")
                append("WCAG AA: ${if (isAccessible) "Pass" else "Fail"}")
                append("\n")
                append("Temperature: ${getTemperatureName(getColorTemperature())}")
            }
        } ?: "No color selected"
    }
    
    /**
     * Generate analogous colors
     */
    fun generateAnalogousColors(count: Int = 5): List<Int> {
        return _currentColor.value?.let { color ->
            harmonyGenerator.generateAnalogous(color, count)
        } ?: emptyList()
    }
    
    /**
     * Generate complementary colors
     */
    fun generateComplementaryColors(): List<Int> {
        return _currentColor.value?.let { color ->
            listOf(color, colorManager.getComplementaryColor(color))
        } ?: emptyList()
    }
    
    /**
     * Generate triadic colors
     */
    fun generateTriadicColors(): List<Int> {
        return _currentColor.value?.let { color ->
            colorManager.getTriadicColors(color)
        } ?: emptyList()
    }
    
    /**
     * Generate color variations (tints, shades, tones)
     */
    fun generateColorVariations(variationType: VariationType, count: Int = 5): List<Int> {
        return _currentColor.value?.let { color ->
            harmonyGenerator.generateColorVariations(color, variationType, count)
        } ?: emptyList()
    }
    
    /**
     * Check if color is warm or cool
     */
    fun isWarmColor(): Boolean {
        return getColorTemperature() < ColorManager.NEUTRAL_TEMPERATURE
    }
    
    /**
     * Get dominant color name/description
     */
    fun getColorDescription(): String {
        return _currentColor.value?.let { color ->
            val hsl = colorManager.rgbToHsl(intArrayOf(
                Color.red(color), Color.green(color), Color.blue(color)
            ))
            
            val hue = hsl[0]
            val saturation = hsl[1]
            val lightness = hsl[2]
            
            val hueName = when {
                hue < 15 || hue >= 345 -> "Red"
                hue < 45 -> "Orange"
                hue < 75 -> "Yellow"
                hue < 105 -> "Yellow-Green"
                hue < 135 -> "Green"
                hue < 165 -> "Blue-Green"
                hue < 195 -> "Cyan"
                hue < 225 -> "Blue"
                hue < 255 -> "Blue-Purple"
                hue < 285 -> "Purple"
                hue < 315 -> "Purple-Red"
                else -> "Red-Purple"
            }
            
            val saturationDesc = when {
                saturation < 0.1f -> "Very desaturated"
                saturation < 0.3f -> "Desaturated"
                saturation < 0.7f -> "Moderately saturated"
                else -> "Highly saturated"
            }
            
            val lightnessDesc = when {
                lightness < 0.2f -> "Very dark"
                lightness < 0.4f -> "Dark"
                lightness < 0.6f -> "Medium"
                lightness < 0.8f -> "Light"
                else -> "Very light"
            }
            
            "$lightnessDesc $saturationDesc $hueName"
        } ?: "No color selected"
    }
    
    // Private helper methods
    
    private fun updateColorSpaceValues(color: Int) {
        // RGB values
        _rgbValues.value = intArrayOf(
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        )
        
        // HSL values
        _hslValues.value = colorManager.rgbToHsl(intArrayOf(
            Color.red(color), Color.green(color), Color.blue(color)
        ))
        
        // Hex value
        _hexValue.value = String.format("#%06X", 0xFFFFFF and color)
    }
    
    private fun updateHarmonyColors(
        color: Int, 
        harmonyType: HarmonyType = _currentHarmonyType.value ?: HarmonyType.ANALOGOUS
    ) {
        viewModelScope.launch {
            val harmony = harmonyGenerator.generateHarmony(color, harmonyType, 6)
            _harmonyColors.value = harmony.colors
        }
    }
    
    private fun addToRecentColors(color: Int) {
        // Remove if already exists
        recentColorsList.remove(color)
        
        // Add to beginning
        recentColorsList.add(0, color)
        
        // Limit size
        if (recentColorsList.size > maxRecentColors) {
            recentColorsList.removeAt(recentColorsList.size - 1)
        }
        
        _recentColors.value = recentColorsList.toList()
    }
    
    private fun validateColor(color: Int) {
        // For now, validate against white background
        val backgroundColor = Color.WHITE
        val contrastRatio = colorManager.getContrastRatio(color, backgroundColor)
        val isAccessible = colorManager.isAccessibleContrast(color, backgroundColor)
        
        _colorValidation.value = ColorValidationResult(
            color = color,
            backgroundColor = backgroundColor,
            contrastRatio = contrastRatio,
            isAccessible = isAccessible,
            wcagLevel = if (contrastRatio >= 7.0f) "AAA" else if (contrastRatio >= 4.5f) "AA" else "FAIL"
        )
    }
    
    private fun getTemperatureName(temperature: Float): String {
        return when {
            temperature < ColorManager.WARM_TEMPERATURE -> "Very Warm"
            temperature < ColorManager.NEUTRAL_TEMPERATURE -> "Warm"
            temperature < ColorManager.COOL_TEMPERATURE -> "Cool"
            else -> "Very Cool"
        }
    }
}

/**
 * Data class for color validation results
 */
data class ColorValidationResult(
    val color: Int,
    val backgroundColor: Int,
    val contrastRatio: Float,
    val isAccessible: Boolean,
    val wcagLevel: String
)