package com.example.drawinggame.ui.drawing.color.adjustments

import android.graphics.*
import kotlin.math.*

/**
 * Levels adjustment layer implementation
 * Provides histogram-based input/output level control for precise tonal adjustments
 */
class LevelsAdjustment : AdjustmentLayer() {
    
    override val type = AdjustmentType.LEVELS
    override val name = "Levels"
    override val description = "Adjust input and output levels using histogram controls"
    
    companion object {
        // Master channel parameters
        const val PARAM_INPUT_BLACK = "inputBlack"
        const val PARAM_INPUT_WHITE = "inputWhite"
        const val PARAM_INPUT_GAMMA = "inputGamma"
        const val PARAM_OUTPUT_BLACK = "outputBlack"
        const val PARAM_OUTPUT_WHITE = "outputWhite"
        
        // Individual channel parameters
        const val PARAM_RED_INPUT_BLACK = "redInputBlack"
        const val PARAM_RED_INPUT_WHITE = "redInputWhite"
        const val PARAM_RED_INPUT_GAMMA = "redInputGamma"
        const val PARAM_RED_OUTPUT_BLACK = "redOutputBlack"
        const val PARAM_RED_OUTPUT_WHITE = "redOutputWhite"
        
        const val PARAM_GREEN_INPUT_BLACK = "greenInputBlack"
        const val PARAM_GREEN_INPUT_WHITE = "greenInputWhite"
        const val PARAM_GREEN_INPUT_GAMMA = "greenInputGamma"
        const val PARAM_GREEN_OUTPUT_BLACK = "greenOutputBlack"
        const val PARAM_GREEN_OUTPUT_WHITE = "greenOutputWhite"
        
        const val PARAM_BLUE_INPUT_BLACK = "blueInputBlack"
        const val PARAM_BLUE_INPUT_WHITE = "blueInputWhite"
        const val PARAM_BLUE_INPUT_GAMMA = "blueInputGamma"
        const val PARAM_BLUE_OUTPUT_BLACK = "blueOutputBlack"
        const val PARAM_BLUE_OUTPUT_WHITE = "blueOutputWhite"
    }
    
    // Lookup tables for fast processing
    private val masterLUT = IntArray(256)
    private val redLUT = IntArray(256)
    private val greenLUT = IntArray(256)
    private val blueLUT = IntArray(256)
    
    // Current active channel
    var activeChannel: LevelsChannel = LevelsChannel.RGB
    
    init {
        initializeParameters()
        updateLookupTables()
    }
    
    override fun apply(source: Bitmap): Bitmap {
        if (!isEnabled || !hasEffect()) {
            return source.copy(source.config ?: Bitmap.Config.ARGB_8888, false)
        }
        
        // Update lookup tables if parameters have changed
        updateLookupTables()
        
        return applyPixelProcessing(source) { pixel ->
            val a = Color.alpha(pixel)
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)
            
            // Apply master levels first
            var newR = masterLUT[r]
            var newG = masterLUT[g]
            var newB = masterLUT[b]
            
            // Apply individual channel levels
            newR = redLUT[newR]
            newG = greenLUT[newG]
            newB = blueLUT[newB]
            
            Color.argb(a, newR, newG, newB)
        }
    }
    
    override fun copy(): AdjustmentLayer {
        val copy = LevelsAdjustment()
        copy.isEnabled = this.isEnabled
        copy.opacity = this.opacity
        copy.blendMode = this.blendMode
        copy.setParameters(this.getCurrentParameters())
        copy.updateLookupTables()
        return copy
    }
    
    /**
     * Initialize all parameters with default values
     */
    private fun initializeParameters() {
        // Master channel parameters
        registerParameter(PARAM_INPUT_BLACK, 0f, 0f, 255f)
        registerParameter(PARAM_INPUT_WHITE, 255f, 0f, 255f)
        registerParameter(PARAM_INPUT_GAMMA, 1f, 0.1f, 9.99f)
        registerParameter(PARAM_OUTPUT_BLACK, 0f, 0f, 255f)
        registerParameter(PARAM_OUTPUT_WHITE, 255f, 0f, 255f)
        
        // Red channel parameters
        registerParameter(PARAM_RED_INPUT_BLACK, 0f, 0f, 255f)
        registerParameter(PARAM_RED_INPUT_WHITE, 255f, 0f, 255f)
        registerParameter(PARAM_RED_INPUT_GAMMA, 1f, 0.1f, 9.99f)
        registerParameter(PARAM_RED_OUTPUT_BLACK, 0f, 0f, 255f)
        registerParameter(PARAM_RED_OUTPUT_WHITE, 255f, 0f, 255f)
        
        // Green channel parameters
        registerParameter(PARAM_GREEN_INPUT_BLACK, 0f, 0f, 255f)
        registerParameter(PARAM_GREEN_INPUT_WHITE, 255f, 0f, 255f)
        registerParameter(PARAM_GREEN_INPUT_GAMMA, 1f, 0.1f, 9.99f)
        registerParameter(PARAM_GREEN_OUTPUT_BLACK, 0f, 0f, 255f)
        registerParameter(PARAM_GREEN_OUTPUT_WHITE, 255f, 0f, 255f)
        
        // Blue channel parameters
        registerParameter(PARAM_BLUE_INPUT_BLACK, 0f, 0f, 255f)
        registerParameter(PARAM_BLUE_INPUT_WHITE, 255f, 0f, 255f)
        registerParameter(PARAM_BLUE_INPUT_GAMMA, 1f, 0.1f, 9.99f)
        registerParameter(PARAM_BLUE_OUTPUT_BLACK, 0f, 0f, 255f)
        registerParameter(PARAM_BLUE_OUTPUT_WHITE, 255f, 0f, 255f)
    }
    
    /**
     * Update all lookup tables
     */
    private fun updateLookupTables() {
        updateChannelLUT(masterLUT, 
            getParameter(PARAM_INPUT_BLACK),
            getParameter(PARAM_INPUT_WHITE),
            getParameter(PARAM_INPUT_GAMMA),
            getParameter(PARAM_OUTPUT_BLACK),
            getParameter(PARAM_OUTPUT_WHITE)
        )
        
        updateChannelLUT(redLUT,
            getParameter(PARAM_RED_INPUT_BLACK),
            getParameter(PARAM_RED_INPUT_WHITE),
            getParameter(PARAM_RED_INPUT_GAMMA),
            getParameter(PARAM_RED_OUTPUT_BLACK),
            getParameter(PARAM_RED_OUTPUT_WHITE)
        )
        
        updateChannelLUT(greenLUT,
            getParameter(PARAM_GREEN_INPUT_BLACK),
            getParameter(PARAM_GREEN_INPUT_WHITE),
            getParameter(PARAM_GREEN_INPUT_GAMMA),
            getParameter(PARAM_GREEN_OUTPUT_BLACK),
            getParameter(PARAM_GREEN_OUTPUT_WHITE)
        )
        
        updateChannelLUT(blueLUT,
            getParameter(PARAM_BLUE_INPUT_BLACK),
            getParameter(PARAM_BLUE_INPUT_WHITE),
            getParameter(PARAM_BLUE_INPUT_GAMMA),
            getParameter(PARAM_BLUE_OUTPUT_BLACK),
            getParameter(PARAM_BLUE_OUTPUT_WHITE)
        )
    }
    
    /**
     * Update lookup table for a specific channel
     */
    private fun updateChannelLUT(
        lut: IntArray,
        inputBlack: Float,
        inputWhite: Float,
        gamma: Float,
        outputBlack: Float,
        outputWhite: Float
    ) {
        val inputRange = inputWhite - inputBlack
        val outputRange = outputWhite - outputBlack
        
        for (i in lut.indices) {
            val input = i.toFloat()
            
            // Step 1: Apply input levels (black and white point)
            val normalized = if (inputRange > 0f) {
                ((input - inputBlack) / inputRange).coerceIn(0f, 1f)
            } else {
                0f
            }
            
            // Step 2: Apply gamma correction
            val gammaAdjusted = if (gamma != 1f) {
                normalized.pow(1f / gamma)
            } else {
                normalized
            }
            
            // Step 3: Apply output levels
            val output = outputBlack + (gammaAdjusted * outputRange)
            
            lut[i] = output.roundToInt().coerceIn(0, 255)
        }
    }
    
    /**
     * Auto-adjust levels based on histogram
     */
    fun autoLevels(source: Bitmap, channel: LevelsChannel = LevelsChannel.RGB) {
        val histogram = calculateHistogram(source, channel)
        val autoLevels = calculateAutoLevels(histogram)
        
        when (channel) {
            LevelsChannel.RGB -> {
                setParameter(PARAM_INPUT_BLACK, autoLevels.inputBlack)
                setParameter(PARAM_INPUT_WHITE, autoLevels.inputWhite)
            }
            LevelsChannel.RED -> {
                setParameter(PARAM_RED_INPUT_BLACK, autoLevels.inputBlack)
                setParameter(PARAM_RED_INPUT_WHITE, autoLevels.inputWhite)
            }
            LevelsChannel.GREEN -> {
                setParameter(PARAM_GREEN_INPUT_BLACK, autoLevels.inputBlack)
                setParameter(PARAM_GREEN_INPUT_WHITE, autoLevels.inputWhite)
            }
            LevelsChannel.BLUE -> {
                setParameter(PARAM_BLUE_INPUT_BLACK, autoLevels.inputBlack)
                setParameter(PARAM_BLUE_INPUT_WHITE, autoLevels.inputWhite)
            }
        }
        
        updateLookupTables()
    }
    
    /**
     * Calculate histogram for specified channel
     */
    private fun calculateHistogram(source: Bitmap, channel: LevelsChannel): IntArray {
        val histogram = IntArray(256)
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        
        for (pixel in pixels) {
            val value = when (channel) {
                LevelsChannel.RGB -> {
                    // Use luminance for RGB channel
                    val r = Color.red(pixel)
                    val g = Color.green(pixel)
                    val b = Color.blue(pixel)
                    (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                }
                LevelsChannel.RED -> Color.red(pixel)
                LevelsChannel.GREEN -> Color.green(pixel)
                LevelsChannel.BLUE -> Color.blue(pixel)
            }
            
            histogram[value.coerceIn(0, 255)]++
        }
        
        return histogram
    }
    
    /**
     * Calculate auto levels from histogram
     */
    private fun calculateAutoLevels(histogram: IntArray): AutoLevelsResult {
        val totalPixels = histogram.sum()
        val clipPercentage = 0.01f // Clip 1% from each end
        val clipCount = (totalPixels * clipPercentage).toInt()
        
        var inputBlack = 0f
        var inputWhite = 255f
        
        // Find input black point
        var accumulator = 0
        for (i in histogram.indices) {
            accumulator += histogram[i]
            if (accumulator >= clipCount) {
                inputBlack = i.toFloat()
                break
            }
        }
        
        // Find input white point
        accumulator = 0
        for (i in histogram.indices.reversed()) {
            accumulator += histogram[i]
            if (accumulator >= clipCount) {
                inputWhite = i.toFloat()
                break
            }
        }
        
        return AutoLevelsResult(inputBlack, inputWhite)
    }
    
    // Convenience methods for channel-specific adjustments
    
    /**
     * Set levels for master channel
     */
    fun setMasterLevels(
        inputBlack: Float,
        inputWhite: Float,
        gamma: Float = 1f,
        outputBlack: Float = 0f,
        outputWhite: Float = 255f
    ) {
        setParameters(mapOf(
            PARAM_INPUT_BLACK to inputBlack,
            PARAM_INPUT_WHITE to inputWhite,
            PARAM_INPUT_GAMMA to gamma,
            PARAM_OUTPUT_BLACK to outputBlack,
            PARAM_OUTPUT_WHITE to outputWhite
        ))
        updateLookupTables()
    }
    
    /**
     * Set levels for red channel
     */
    fun setRedLevels(
        inputBlack: Float,
        inputWhite: Float,
        gamma: Float = 1f,
        outputBlack: Float = 0f,
        outputWhite: Float = 255f
    ) {
        setParameters(mapOf(
            PARAM_RED_INPUT_BLACK to inputBlack,
            PARAM_RED_INPUT_WHITE to inputWhite,
            PARAM_RED_INPUT_GAMMA to gamma,
            PARAM_RED_OUTPUT_BLACK to outputBlack,
            PARAM_RED_OUTPUT_WHITE to outputWhite
        ))
        updateLookupTables()
    }
    
    /**
     * Set levels for green channel
     */
    fun setGreenLevels(
        inputBlack: Float,
        inputWhite: Float,
        gamma: Float = 1f,
        outputBlack: Float = 0f,
        outputWhite: Float = 255f
    ) {
        setParameters(mapOf(
            PARAM_GREEN_INPUT_BLACK to inputBlack,
            PARAM_GREEN_INPUT_WHITE to inputWhite,
            PARAM_GREEN_INPUT_GAMMA to gamma,
            PARAM_GREEN_OUTPUT_BLACK to outputBlack,
            PARAM_GREEN_OUTPUT_WHITE to outputWhite
        ))
        updateLookupTables()
    }
    
    /**
     * Set levels for blue channel
     */
    fun setBlueLevels(
        inputBlack: Float,
        inputWhite: Float,
        gamma: Float = 1f,
        outputBlack: Float = 0f,
        outputWhite: Float = 255f
    ) {
        setParameters(mapOf(
            PARAM_BLUE_INPUT_BLACK to inputBlack,
            PARAM_BLUE_INPUT_WHITE to inputWhite,
            PARAM_BLUE_INPUT_GAMMA to gamma,
            PARAM_BLUE_OUTPUT_BLACK to outputBlack,
            PARAM_BLUE_OUTPUT_WHITE to outputWhite
        ))
        updateLookupTables()
    }
    
    /**
     * Reset channel to default levels
     */
    fun resetChannel(channel: LevelsChannel) {
        when (channel) {
            LevelsChannel.RGB -> {
                setMasterLevels(0f, 255f, 1f, 0f, 255f)
            }
            LevelsChannel.RED -> {
                setRedLevels(0f, 255f, 1f, 0f, 255f)
            }
            LevelsChannel.GREEN -> {
                setGreenLevels(0f, 255f, 1f, 0f, 255f)
            }
            LevelsChannel.BLUE -> {
                setBlueLevels(0f, 255f, 1f, 0f, 255f)
            }
        }
    }
    
    /**
     * Get levels data for specified channel
     */
    fun getChannelLevels(channel: LevelsChannel): LevelsData {
        return when (channel) {
            LevelsChannel.RGB -> LevelsData(
                getParameter(PARAM_INPUT_BLACK),
                getParameter(PARAM_INPUT_WHITE),
                getParameter(PARAM_INPUT_GAMMA),
                getParameter(PARAM_OUTPUT_BLACK),
                getParameter(PARAM_OUTPUT_WHITE)
            )
            LevelsChannel.RED -> LevelsData(
                getParameter(PARAM_RED_INPUT_BLACK),
                getParameter(PARAM_RED_INPUT_WHITE),
                getParameter(PARAM_RED_INPUT_GAMMA),
                getParameter(PARAM_RED_OUTPUT_BLACK),
                getParameter(PARAM_RED_OUTPUT_WHITE)
            )
            LevelsChannel.GREEN -> LevelsData(
                getParameter(PARAM_GREEN_INPUT_BLACK),
                getParameter(PARAM_GREEN_INPUT_WHITE),
                getParameter(PARAM_GREEN_INPUT_GAMMA),
                getParameter(PARAM_GREEN_OUTPUT_BLACK),
                getParameter(PARAM_GREEN_OUTPUT_WHITE)
            )
            LevelsChannel.BLUE -> LevelsData(
                getParameter(PARAM_BLUE_INPUT_BLACK),
                getParameter(PARAM_BLUE_INPUT_WHITE),
                getParameter(PARAM_BLUE_INPUT_GAMMA),
                getParameter(PARAM_BLUE_OUTPUT_BLACK),
                getParameter(PARAM_BLUE_OUTPUT_WHITE)
            )
        }
    }
    
    /**
     * Get lookup table for specified channel
     */
    fun getChannelLUT(channel: LevelsChannel): IntArray {
        return when (channel) {
            LevelsChannel.RGB -> masterLUT.clone()
            LevelsChannel.RED -> redLUT.clone()
            LevelsChannel.GREEN -> greenLUT.clone()
            LevelsChannel.BLUE -> blueLUT.clone()
        }
    }
}

/**
 * Levels channels
 */
enum class LevelsChannel(val displayName: String, val color: Int) {
    RGB("RGB", Color.GRAY),
    RED("Red", Color.RED),
    GREEN("Green", Color.GREEN),
    BLUE("Blue", Color.BLUE)
}

/**
 * Levels data container
 */
data class LevelsData(
    val inputBlack: Float,
    val inputWhite: Float,
    val gamma: Float,
    val outputBlack: Float,
    val outputWhite: Float
) {
    /**
     * Check if levels are at default values
     */
    fun isDefault(): Boolean {
        return abs(inputBlack - 0f) < 0.1f &&
               abs(inputWhite - 255f) < 0.1f &&
               abs(gamma - 1f) < 0.01f &&
               abs(outputBlack - 0f) < 0.1f &&
               abs(outputWhite - 255f) < 0.1f
    }
    
    /**
     * Get input range
     */
    fun getInputRange(): Float = inputWhite - inputBlack
    
    /**
     * Get output range
     */
    fun getOutputRange(): Float = outputWhite - outputBlack
    
    /**
     * Get contrast adjustment (based on input range)
     */
    fun getContrastAdjustment(): Float {
        val inputRange = getInputRange()
        return if (inputRange > 0f) 255f / inputRange else 1f
    }
}

/**
 * Auto levels calculation result
 */
private data class AutoLevelsResult(
    val inputBlack: Float,
    val inputWhite: Float
)