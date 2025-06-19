package com.example.drawinggame.ui.drawing.color.effects

import android.graphics.ColorMatrix

/**
 * Base class for all image filters
 */
abstract class ImageFilter {
    abstract val name: String
    abstract val description: String
    
    /**
     * Check if this filter supports GPU acceleration
     */
    open fun supportsGPU(): Boolean = false
    
    /**
     * Get filter parameters for UI display
     */
    open fun getParameters(): Map<String, Any> = emptyMap()
    
    /**
     * Set filter parameter
     */
    open fun setParameter(key: String, value: Any) {}
    
    /**
     * Reset filter to default parameters
     */
    open fun reset() {}
    
    /**
     * Create a copy of this filter
     */
    abstract fun copy(): ImageFilter
    
    override fun hashCode(): Int {
        return javaClass.hashCode() * 31 + getParameters().hashCode()
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        
        other as ImageFilter
        return getParameters() == other.getParameters()
    }
}

/**
 * Blur filter with adjustable radius
 */
data class BlurFilter(
    var radius: Float = 5f
) : ImageFilter() {
    
    override val name = "Blur"
    override val description = "Apply Gaussian blur to the image"
    
    override fun supportsGPU(): Boolean = true
    
    override fun getParameters(): Map<String, Any> {
        return mapOf("radius" to radius)
    }
    
    override fun setParameter(key: String, value: Any) {
        when (key) {
            "radius" -> radius = (value as? Number)?.toFloat() ?: radius
        }
    }
    
    override fun reset() {
        radius = 5f
    }
    
    override fun copy(): ImageFilter = BlurFilter(radius)
}

/**
 * Sharpen filter with adjustable strength
 */
data class SharpenFilter(
    var strength: Float = 0.5f
) : ImageFilter() {
    
    override val name = "Sharpen"
    override val description = "Enhance image sharpness and detail"
    
    override fun supportsGPU(): Boolean = true
    
    override fun getParameters(): Map<String, Any> {
        return mapOf("strength" to strength)
    }
    
    override fun setParameter(key: String, value: Any) {
        when (key) {
            "strength" -> strength = (value as? Number)?.toFloat() ?: strength
        }
    }
    
    override fun reset() {
        strength = 0.5f
    }
    
    override fun copy(): ImageFilter = SharpenFilter(strength)
}

/**
 * Noise filter with different noise types
 */
data class NoiseFilter(
    var type: NoiseType = NoiseType.GAUSSIAN,
    var amount: Float = 0.1f
) : ImageFilter() {
    
    override val name = "Noise"
    override val description = "Add noise to the image"
    
    override fun getParameters(): Map<String, Any> {
        return mapOf("type" to type, "amount" to amount)
    }
    
    override fun setParameter(key: String, value: Any) {
        when (key) {
            "type" -> type = value as? NoiseType ?: type
            "amount" -> amount = (value as? Number)?.toFloat() ?: amount
        }
    }
    
    override fun reset() {
        type = NoiseType.GAUSSIAN
        amount = 0.1f
    }
    
    override fun copy(): ImageFilter = NoiseFilter(type, amount)
}

enum class NoiseType {
    GAUSSIAN, UNIFORM, SALT_PEPPER
}

/**
 * Edge detection filter
 */
data class EdgeDetectionFilter(
    var type: EdgeDetectionType = EdgeDetectionType.SOBEL_X,
    var strength: Float = 1f
) : ImageFilter() {
    
    override val name = "Edge Detection"
    override val description = "Detect and enhance edges in the image"
    
    override fun getParameters(): Map<String, Any> {
        return mapOf("type" to type, "strength" to strength)
    }
    
    override fun setParameter(key: String, value: Any) {
        when (key) {
            "type" -> type = value as? EdgeDetectionType ?: type
            "strength" -> strength = (value as? Number)?.toFloat() ?: strength
        }
    }
    
    override fun reset() {
        type = EdgeDetectionType.SOBEL_X
        strength = 1f
    }
    
    override fun copy(): ImageFilter = EdgeDetectionFilter(type, strength)
}

enum class EdgeDetectionType {
    SOBEL_X, SOBEL_Y, LAPLACIAN, ROBERTS
}

/**
 * Emboss filter
 */
data class EmbossFilter(
    var strength: Float = 1f,
    var brightness: Float = 0.5f
) : ImageFilter() {
    
    override val name = "Emboss"
    override val description = "Create embossed relief effect"
    
    override fun getParameters(): Map<String, Any> {
        return mapOf("strength" to strength, "brightness" to brightness)
    }
    
    override fun setParameter(key: String, value: Any) {
        when (key) {
            "strength" -> strength = (value as? Number)?.toFloat() ?: strength
            "brightness" -> brightness = (value as? Number)?.toFloat() ?: brightness
        }
    }
    
    override fun reset() {
        strength = 1f
        brightness = 0.5f
    }
    
    override fun copy(): ImageFilter = EmbossFilter(strength, brightness)
}

/**
 * Oil painting artistic filter
 */
data class OilPaintingFilter(
    var radius: Float = 4f,
    var intensityLevels: Int = 20
) : ImageFilter() {
    
    override val name = "Oil Painting"
    override val description = "Apply oil painting artistic effect"
    
    override fun getParameters(): Map<String, Any> {
        return mapOf("radius" to radius, "intensityLevels" to intensityLevels)
    }
    
    override fun setParameter(key: String, value: Any) {
        when (key) {
            "radius" -> radius = (value as? Number)?.toFloat() ?: radius
            "intensityLevels" -> intensityLevels = (value as? Number)?.toInt() ?: intensityLevels
        }
    }
    
    override fun reset() {
        radius = 4f
        intensityLevels = 20
    }
    
    override fun copy(): ImageFilter = OilPaintingFilter(radius, intensityLevels)
}

/**
 * Watercolor artistic filter
 */
data class WatercolorFilter(
    var blurRadius: Float = 3f,
    var colorLevels: Int = 8,
    var edgeStrength: Float = 0.3f
) : ImageFilter() {
    
    override val name = "Watercolor"
    override val description = "Apply watercolor painting effect"
    
    override fun getParameters(): Map<String, Any> {
        return mapOf(
            "blurRadius" to blurRadius,
            "colorLevels" to colorLevels,
            "edgeStrength" to edgeStrength
        )
    }
    
    override fun setParameter(key: String, value: Any) {
        when (key) {
            "blurRadius" -> blurRadius = (value as? Number)?.toFloat() ?: blurRadius
            "colorLevels" -> colorLevels = (value as? Number)?.toInt() ?: colorLevels
            "edgeStrength" -> edgeStrength = (value as? Number)?.toFloat() ?: edgeStrength
        }
    }
    
    override fun reset() {
        blurRadius = 3f
        colorLevels = 8
        edgeStrength = 0.3f
    }
    
    override fun copy(): ImageFilter = WatercolorFilter(blurRadius, colorLevels, edgeStrength)
}

/**
 * Vintage photo filter
 */
data class VintageFilter(
    var sepiaStrength: Float = 0.8f,
    var vignetteStrength: Float = 0.4f
) : ImageFilter() {
    
    override val name = "Vintage"
    override val description = "Apply vintage photo effect with sepia and vignette"
    
    override fun getParameters(): Map<String, Any> {
        return mapOf("sepiaStrength" to sepiaStrength, "vignetteStrength" to vignetteStrength)
    }
    
    override fun setParameter(key: String, value: Any) {
        when (key) {
            "sepiaStrength" -> sepiaStrength = (value as? Number)?.toFloat() ?: sepiaStrength
            "vignetteStrength" -> vignetteStrength = (value as? Number)?.toFloat() ?: vignetteStrength
        }
    }
    
    override fun reset() {
        sepiaStrength = 0.8f
        vignetteStrength = 0.4f
    }
    
    override fun copy(): ImageFilter = VintageFilter(sepiaStrength, vignetteStrength)
}

/**
 * Distortion filter
 */
data class DistortionFilter(
    var type: DistortionType = DistortionType.BARREL,
    var strength: Float = 0.2f,
    var radius: Float = 100f
) : ImageFilter() {
    
    override val name = "Distortion"
    override val description = "Apply geometric distortion effects"
    
    override fun getParameters(): Map<String, Any> {
        return mapOf("type" to type, "strength" to strength, "radius" to radius)
    }
    
    override fun setParameter(key: String, value: Any) {
        when (key) {
            "type" -> type = value as? DistortionType ?: type
            "strength" -> strength = (value as? Number)?.toFloat() ?: strength
            "radius" -> radius = (value as? Number)?.toFloat() ?: radius
        }
    }
    
    override fun reset() {
        type = DistortionType.BARREL
        strength = 0.2f
        radius = 100f
    }
    
    override fun copy(): ImageFilter = DistortionFilter(type, strength, radius)
}

enum class DistortionType {
    BARREL, PINCUSHION, FISHEYE, SWIRL
}

/**
 * Color matrix filter for color transformations
 */
data class ColorMatrixFilter(
    var matrix: ColorMatrix = ColorMatrix()
) : ImageFilter() {
    
    override val name = "Color Matrix"
    override val description = "Apply color transformation matrix"
    
    override fun getParameters(): Map<String, Any> {
        return mapOf("matrix" to matrix.array.contentToString())
    }
    
    override fun setParameter(key: String, value: Any) {
        when (key) {
            "matrix" -> {
                if (value is ColorMatrix) {
                    matrix = ColorMatrix(value)
                } else if (value is FloatArray && value.size == 20) {
                    matrix = ColorMatrix(value)
                }
            }
        }
    }
    
    override fun reset() {
        matrix.reset()
    }
    
    override fun copy(): ImageFilter = ColorMatrixFilter(ColorMatrix(matrix))
}

/**
 * Filter presets for common effects
 */
object FilterPresets {
    
    /**
     * Create preset filters
     */
    fun getPresetFilters(): List<ImageFilter> = listOf(
        // Blur variations
        BlurFilter(2f),
        BlurFilter(8f),
        BlurFilter(15f),
        
        // Artistic filters
        OilPaintingFilter(3f, 15),
        OilPaintingFilter(6f, 25),
        WatercolorFilter(2f, 6, 0.4f),
        WatercolorFilter(4f, 10, 0.2f),
        
        // Vintage effects
        VintageFilter(0.6f, 0.3f),
        VintageFilter(1f, 0.6f),
        
        // Technical filters
        SharpenFilter(0.3f),
        SharpenFilter(0.8f),
        EdgeDetectionFilter(EdgeDetectionType.SOBEL_X, 0.8f),
        EdgeDetectionFilter(EdgeDetectionType.LAPLACIAN, 1f),
        
        // Distortions
        DistortionFilter(DistortionType.FISHEYE, 0.3f),
        DistortionFilter(DistortionType.SWIRL, 0.2f, 80f),
        
        // Noise
        NoiseFilter(NoiseType.GAUSSIAN, 0.05f),
        NoiseFilter(NoiseType.SALT_PEPPER, 0.02f)
    )
    
    /**
     * Create sepia color matrix
     */
    fun createSepiaMatrix(): ColorMatrix {
        return ColorMatrix().apply {
            setSaturation(0f)
            val sepiaMatrix = ColorMatrix(floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
            postConcat(sepiaMatrix)
        }
    }
    
    /**
     * Create black and white color matrix
     */
    fun createBlackWhiteMatrix(): ColorMatrix {
        return ColorMatrix().apply {
            setSaturation(0f)
        }
    }
    
    /**
     * Create high contrast color matrix
     */
    fun createHighContrastMatrix(): ColorMatrix {
        return ColorMatrix(floatArrayOf(
            2f, 0f, 0f, 0f, -128f,
            0f, 2f, 0f, 0f, -128f,
            0f, 0f, 2f, 0f, -128f,
            0f, 0f, 0f, 1f, 0f
        ))
    }
    
    /**
     * Create inverted color matrix
     */
    fun createInvertMatrix(): ColorMatrix {
        return ColorMatrix(floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
        ))
    }
}