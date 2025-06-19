package com.example.drawinggame.ui.drawing.color.effects

import android.graphics.*
import android.renderscript.*
import kotlinx.coroutines.*
import kotlin.math.*

/**
 * High-performance filter and effect engine
 * Provides GPU-accelerated image processing with RenderScript fallback
 */
class FilterEngine private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: FilterEngine? = null
        
        fun getInstance(): FilterEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FilterEngine().also { INSTANCE = it }
            }
        }
    }
    
    private var renderScript: RenderScript? = null
    private val filterCache = mutableMapOf<String, FilterResult>()
    private val maxCacheSize = 10
    
    /**
     * Initialize engine with RenderScript context
     */
    fun initialize(context: android.content.Context) {
        try {
            renderScript = RenderScript.create(context)
        } catch (e: Exception) {
            // RenderScript not available, use CPU fallback
            renderScript = null
        }
    }
    
    /**
     * Release resources
     */
    fun release() {
        renderScript?.destroy()
        renderScript = null
        filterCache.clear()
    }
    
    /**
     * Apply filter to bitmap with caching
     */
    suspend fun applyFilter(
        source: Bitmap,
        filter: ImageFilter,
        useCache: Boolean = true
    ): Bitmap = withContext(Dispatchers.Default) {
        
        val cacheKey = if (useCache) {
            generateCacheKey(source, filter)
        } else null
        
        // Check cache first
        if (cacheKey != null && filterCache.containsKey(cacheKey)) {
            val cached = filterCache[cacheKey]!!
            if (cached.isValid()) {
                return@withContext cached.bitmap.copy(cached.bitmap.config ?: Bitmap.Config.ARGB_8888, false)
            } else {
                filterCache.remove(cacheKey)
            }
        }
        
        // Apply filter
        val result = if (renderScript != null && filter.supportsGPU()) {
            applyFilterGPU(source, filter)
        } else {
            applyFilterCPU(source, filter)
        }
        
        // Cache result
        if (cacheKey != null && filterCache.size < maxCacheSize) {
            filterCache[cacheKey] = FilterResult(result.copy(result.config ?: Bitmap.Config.ARGB_8888, false))
        }
        
        result
    }
    
    /**
     * Apply filter using GPU acceleration (RenderScript)
     */
    private fun applyFilterGPU(source: Bitmap, filter: ImageFilter): Bitmap {
        val rs = renderScript ?: return applyFilterCPU(source, filter)
        
        return try {
            when (filter) {
                is BlurFilter -> applyBlurGPU(rs, source, filter)
                is SharpenFilter -> applySharpenGPU(rs, source, filter)
                is NoiseFilter -> applyNoiseGPU(rs, source, filter)
                else -> applyFilterCPU(source, filter)
            }
        } catch (e: Exception) {
            // GPU processing failed, fallback to CPU
            applyFilterCPU(source, filter)
        }
    }
    
    /**
     * Apply filter using CPU processing
     */
    private fun applyFilterCPU(source: Bitmap, filter: ImageFilter): Bitmap {
        return when (filter) {
            is BlurFilter -> applyBlurCPU(source, filter)
            is SharpenFilter -> applySharpenCPU(source, filter)
            is NoiseFilter -> applyNoiseCPU(source, filter)
            is EdgeDetectionFilter -> applyEdgeDetectionCPU(source, filter)
            is EmbossFilter -> applyEmbossCPU(source, filter)
            is OilPaintingFilter -> applyOilPaintingCPU(source, filter)
            is WatercolorFilter -> applyWatercolorCPU(source, filter)
            is VintageFilter -> applyVintageCPU(source, filter)
            is DistortionFilter -> applyDistortionCPU(source, filter)
            is ColorMatrixFilter -> applyColorMatrixCPU(source, filter)
            else -> source.copy(source.config ?: Bitmap.Config.ARGB_8888, false)
        }
    }
    
    // GPU implementations
    
    private fun applyBlurGPU(rs: RenderScript, source: Bitmap, filter: BlurFilter): Bitmap {
        val input = Allocation.createFromBitmap(rs, source)
        val output = Allocation.createTyped(rs, input.type)
        
        val blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        blurScript.setRadius(filter.radius.coerceIn(0f, 25f))
        blurScript.setInput(input)
        blurScript.forEach(output)
        
        val result = source.copy(source.config ?: Bitmap.Config.ARGB_8888, true)
        output.copyTo(result)
        
        input.destroy()
        output.destroy()
        blurScript.destroy()
        
        return result
    }
    
    private fun applySharpenGPU(rs: RenderScript, source: Bitmap, filter: SharpenFilter): Bitmap {
        val input = Allocation.createFromBitmap(rs, source)
        val output = Allocation.createTyped(rs, input.type)
        
        val convolutionScript = ScriptIntrinsicConvolve3x3.create(rs, Element.U8_4(rs))
        val sharpenKernel = floatArrayOf(
            0f, -filter.strength, 0f,
            -filter.strength, 1f + 4f * filter.strength, -filter.strength,
            0f, -filter.strength, 0f
        )
        convolutionScript.setCoefficients(sharpenKernel)
        convolutionScript.setInput(input)
        convolutionScript.forEach(output)
        
        val result = source.copy(source.config ?: Bitmap.Config.ARGB_8888, true)
        output.copyTo(result)
        
        input.destroy()
        output.destroy()
        convolutionScript.destroy()
        
        return result
    }
    
    private fun applyNoiseGPU(rs: RenderScript, source: Bitmap, filter: NoiseFilter): Bitmap {
        // Noise filter requires custom script - fallback to CPU for now
        return applyNoiseCPU(source, filter)
    }
    
    // CPU implementations
    
    private fun applyBlurCPU(source: Bitmap, filter: BlurFilter): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val radius = filter.radius.roundToInt().coerceIn(1, 50)
        val blurredPixels = applyGaussianBlur(pixels, width, height, radius)
        
        val result = Bitmap.createBitmap(width, height, source.config ?: Bitmap.Config.ARGB_8888)
        result.setPixels(blurredPixels, 0, width, 0, 0, width, height)
        return result
    }
    
    private fun applySharpenCPU(source: Bitmap, filter: SharpenFilter): Bitmap {
        val kernel = floatArrayOf(
            0f, -filter.strength, 0f,
            -filter.strength, 1f + 4f * filter.strength, -filter.strength,
            0f, -filter.strength, 0f
        )
        return applyConvolutionFilter(source, kernel, 3)
    }
    
    private fun applyNoiseCPU(source: Bitmap, filter: NoiseFilter): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val random = kotlin.random.Random(System.currentTimeMillis())
        val noiseAmount = (filter.amount * 255f).toInt()
        
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val a = Color.alpha(pixel)
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)
            
            val noise = random.nextInt(-noiseAmount, noiseAmount + 1)
            
            when (filter.type) {
                NoiseType.GAUSSIAN -> {
                    val gaussianNoise = (generateGaussianNoise(random) * noiseAmount * 0.3f).toInt()
                    pixels[i] = Color.argb(a,
                        (r + gaussianNoise).coerceIn(0, 255),
                        (g + gaussianNoise).coerceIn(0, 255),
                        (b + gaussianNoise).coerceIn(0, 255)
                    )
                }
                NoiseType.UNIFORM -> {
                    pixels[i] = Color.argb(a,
                        (r + noise).coerceIn(0, 255),
                        (g + noise).coerceIn(0, 255),
                        (b + noise).coerceIn(0, 255)
                    )
                }
                NoiseType.SALT_PEPPER -> {
                    if (random.nextFloat() < filter.amount) {
                        val value = if (random.nextBoolean()) 255 else 0
                        pixels[i] = Color.argb(a, value, value, value)
                    }
                }
            }
        }
        
        val result = Bitmap.createBitmap(width, height, source.config ?: Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }
    
    private fun applyEdgeDetectionCPU(source: Bitmap, filter: EdgeDetectionFilter): Bitmap {
        val kernel = when (filter.type) {
            EdgeDetectionType.SOBEL_X -> floatArrayOf(
                -1f, 0f, 1f,
                -2f, 0f, 2f,
                -1f, 0f, 1f
            )
            EdgeDetectionType.SOBEL_Y -> floatArrayOf(
                -1f, -2f, -1f,
                0f, 0f, 0f,
                1f, 2f, 1f
            )
            EdgeDetectionType.LAPLACIAN -> floatArrayOf(
                0f, -1f, 0f,
                -1f, 4f, -1f,
                0f, -1f, 0f
            )
            EdgeDetectionType.ROBERTS -> floatArrayOf(
                1f, 0f, 0f,
                0f, -1f, 0f,
                0f, 0f, 0f
            )
        }
        
        return applyConvolutionFilter(source, kernel, 3, filter.strength)
    }
    
    private fun applyEmbossCPU(source: Bitmap, filter: EmbossFilter): Bitmap {
        val kernel = floatArrayOf(
            -2f, -1f, 0f,
            -1f, 1f, 1f,
            0f, 1f, 2f
        )
        
        val result = applyConvolutionFilter(source, kernel, 3, filter.strength)
        
        // Add emboss effect by adjusting brightness
        return adjustBrightness(result, filter.brightness)
    }
    
    private fun applyOilPaintingCPU(source: Bitmap, filter: OilPaintingFilter): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val radius = filter.radius.roundToInt().coerceIn(1, 10)
        val intensityLevels = filter.intensityLevels.coerceIn(1, 256)
        val result = IntArray(width * height)
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val intensityCount = IntArray(intensityLevels)
                val averageR = IntArray(intensityLevels)
                val averageG = IntArray(intensityLevels)
                val averageB = IntArray(intensityLevels)
                
                // Sample neighborhood
                for (dy in -radius..radius) {
                    for (dx in -radius..radius) {
                        val nx = (x + dx).coerceIn(0, width - 1)
                        val ny = (y + dy).coerceIn(0, height - 1)
                        val pixel = pixels[ny * width + nx]
                        
                        val r = Color.red(pixel)
                        val g = Color.green(pixel)
                        val b = Color.blue(pixel)
                        
                        val intensity = ((r + g + b) / 3 * intensityLevels / 256).coerceIn(0, intensityLevels - 1)
                        
                        intensityCount[intensity]++
                        averageR[intensity] += r
                        averageG[intensity] += g
                        averageB[intensity] += b
                    }
                }
                
                // Find most frequent intensity
                val maxIndex = intensityCount.indices.maxByOrNull { intensityCount[it] } ?: 0
                val count = intensityCount[maxIndex]
                
                if (count > 0) {
                    val a = Color.alpha(pixels[y * width + x])
                    result[y * width + x] = Color.argb(a,
                        averageR[maxIndex] / count,
                        averageG[maxIndex] / count,
                        averageB[maxIndex] / count
                    )
                } else {
                    result[y * width + x] = pixels[y * width + x]
                }
            }
        }
        
        val resultBitmap = Bitmap.createBitmap(width, height, source.config ?: Bitmap.Config.ARGB_8888)
        resultBitmap.setPixels(result, 0, width, 0, 0, width, height)
        return resultBitmap
    }
    
    private fun applyWatercolorCPU(source: Bitmap, filter: WatercolorFilter): Bitmap {
        // Apply blur first
        val blurred = applyBlurCPU(source, BlurFilter(filter.blurRadius))
        
        // Reduce color palette
        val quantized = quantizeColors(blurred, filter.colorLevels)
        
        // Add edge enhancement
        val edges = applyEdgeDetectionCPU(source, EdgeDetectionFilter(EdgeDetectionType.SOBEL_X, 0.3f))
        
        return blendImages(quantized, edges, BlendMode.MULTIPLY, filter.edgeStrength)
    }
    
    private fun applyVintageCPU(source: Bitmap, filter: VintageFilter): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val centerX = width / 2f
        val centerY = height / 2f
        val maxDistance = sqrt(centerX * centerX + centerY * centerY)
        
        for (i in pixels.indices) {
            val x = i % width
            val y = i / width
            val pixel = pixels[i]
            
            val a = Color.alpha(pixel)
            var r = Color.red(pixel)
            var g = Color.green(pixel)
            var b = Color.blue(pixel)
            
            // Apply sepia tone
            if (filter.sepiaStrength > 0f) {
                val tr = (0.393 * r + 0.769 * g + 0.189 * b).toInt()
                val tg = (0.349 * r + 0.686 * g + 0.168 * b).toInt()
                val tb = (0.272 * r + 0.534 * g + 0.131 * b).toInt()
                
                r = (r + (tr - r) * filter.sepiaStrength).toInt().coerceIn(0, 255)
                g = (g + (tg - g) * filter.sepiaStrength).toInt().coerceIn(0, 255)
                b = (b + (tb - b) * filter.sepiaStrength).toInt().coerceIn(0, 255)
            }
            
            // Apply vignette
            if (filter.vignetteStrength > 0f) {
                val distance = sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY))
                val vignetteFactor = 1f - (distance / maxDistance * filter.vignetteStrength).coerceIn(0f, 1f)
                
                r = (r * vignetteFactor).toInt().coerceIn(0, 255)
                g = (g * vignetteFactor).toInt().coerceIn(0, 255)
                b = (b * vignetteFactor).toInt().coerceIn(0, 255)
            }
            
            pixels[i] = Color.argb(a, r, g, b)
        }
        
        val result = Bitmap.createBitmap(width, height, source.config ?: Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }
    
    private fun applyDistortionCPU(source: Bitmap, filter: DistortionFilter): Bitmap {
        val width = source.width
        val height = source.height
        val result = Bitmap.createBitmap(width, height, source.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        
        when (filter.type) {
            DistortionType.BARREL -> applyBarrelDistortion(canvas, source, paint, filter.strength)
            DistortionType.PINCUSHION -> applyPincushionDistortion(canvas, source, paint, filter.strength)
            DistortionType.FISHEYE -> applyFisheyeDistortion(canvas, source, paint, filter.strength)
            DistortionType.SWIRL -> applySwirlDistortion(canvas, source, paint, filter.strength, filter.radius)
        }
        
        return result
    }
    
    private fun applyColorMatrixCPU(source: Bitmap, filter: ColorMatrixFilter): Bitmap {
        val result = Bitmap.createBitmap(source.width, source.height, source.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(filter.matrix)
        }
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }
    
    // Helper methods
    
    private fun generateCacheKey(bitmap: Bitmap, filter: ImageFilter): String {
        return "${bitmap.width}x${bitmap.height}_${filter.hashCode()}"
    }
    
    private fun applyGaussianBlur(pixels: IntArray, width: Int, height: Int, radius: Int): IntArray {
        val kernel = generateGaussianKernel(radius)
        val temp = pixels.clone()
        
        // Horizontal pass
        for (y in 0 until height) {
            for (x in 0 until width) {
                var a = 0f; var r = 0f; var g = 0f; var b = 0f
                var weightSum = 0f
                
                for (i in kernel.indices) {
                    val dx = i - radius
                    val nx = (x + dx).coerceIn(0, width - 1)
                    val pixel = pixels[y * width + nx]
                    val weight = kernel[i]
                    
                    a += Color.alpha(pixel) * weight
                    r += Color.red(pixel) * weight
                    g += Color.green(pixel) * weight
                    b += Color.blue(pixel) * weight
                    weightSum += weight
                }
                
                temp[y * width + x] = Color.argb(
                    (a / weightSum).roundToInt(),
                    (r / weightSum).roundToInt(),
                    (g / weightSum).roundToInt(),
                    (b / weightSum).roundToInt()
                )
            }
        }
        
        // Vertical pass
        for (x in 0 until width) {
            for (y in 0 until height) {
                var a = 0f; var r = 0f; var g = 0f; var b = 0f
                var weightSum = 0f
                
                for (i in kernel.indices) {
                    val dy = i - radius
                    val ny = (y + dy).coerceIn(0, height - 1)
                    val pixel = temp[ny * width + x]
                    val weight = kernel[i]
                    
                    a += Color.alpha(pixel) * weight
                    r += Color.red(pixel) * weight
                    g += Color.green(pixel) * weight
                    b += Color.blue(pixel) * weight
                    weightSum += weight
                }
                
                pixels[y * width + x] = Color.argb(
                    (a / weightSum).roundToInt(),
                    (r / weightSum).roundToInt(),
                    (g / weightSum).roundToInt(),
                    (b / weightSum).roundToInt()
                )
            }
        }
        
        return pixels
    }
    
    private fun generateGaussianKernel(radius: Int): FloatArray {
        val size = radius * 2 + 1
        val kernel = FloatArray(size)
        val sigma = radius / 3f
        val twoSigmaSquared = 2 * sigma * sigma
        var sum = 0f
        
        for (i in 0 until size) {
            val x = i - radius
            kernel[i] = exp(-(x * x) / twoSigmaSquared)
            sum += kernel[i]
        }
        
        // Normalize
        for (i in kernel.indices) {
            kernel[i] /= sum
        }
        
        return kernel
    }
    
    private fun applyConvolutionFilter(
        source: Bitmap,
        kernel: FloatArray,
        kernelSize: Int,
        strength: Float = 1f
    ): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)
        val result = IntArray(width * height)
        
        val radius = kernelSize / 2
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                var r = 0f; var g = 0f; var b = 0f
                
                for (ky in 0 until kernelSize) {
                    for (kx in 0 until kernelSize) {
                        val nx = (x + kx - radius).coerceIn(0, width - 1)
                        val ny = (y + ky - radius).coerceIn(0, height - 1)
                        val pixel = pixels[ny * width + nx]
                        val weight = kernel[ky * kernelSize + kx] * strength
                        
                        r += Color.red(pixel) * weight
                        g += Color.green(pixel) * weight
                        b += Color.blue(pixel) * weight
                    }
                }
                
                val a = Color.alpha(pixels[y * width + x])
                result[y * width + x] = Color.argb(a,
                    r.roundToInt().coerceIn(0, 255),
                    g.roundToInt().coerceIn(0, 255),
                    b.roundToInt().coerceIn(0, 255)
                )
            }
        }
        
        val resultBitmap = Bitmap.createBitmap(width, height, source.config ?: Bitmap.Config.ARGB_8888)
        resultBitmap.setPixels(result, 0, width, 0, 0, width, height)
        return resultBitmap
    }
    
    // Additional helper methods would be implemented here...
    private fun quantizeColors(bitmap: Bitmap, levels: Int): Bitmap { /* Implementation */ return bitmap }
    private fun blendImages(bitmap1: Bitmap, bitmap2: Bitmap, mode: BlendMode, strength: Float): Bitmap { /* Implementation */ return bitmap1 }
    private fun adjustBrightness(bitmap: Bitmap, brightness: Float): Bitmap { /* Implementation */ return bitmap }
    private fun applyBarrelDistortion(canvas: Canvas, source: Bitmap, paint: Paint, strength: Float) { /* Implementation */ }
    private fun applyPincushionDistortion(canvas: Canvas, source: Bitmap, paint: Paint, strength: Float) { /* Implementation */ }
    private fun applyFisheyeDistortion(canvas: Canvas, source: Bitmap, paint: Paint, strength: Float) { /* Implementation */ }
    private fun applySwirlDistortion(canvas: Canvas, source: Bitmap, paint: Paint, strength: Float, radius: Float) { /* Implementation */ }
    
    /**
     * Generate Gaussian noise using Box-Muller transform
     */
    private fun generateGaussianNoise(random: kotlin.random.Random): Float {
        // Box-Muller transform to generate Gaussian distribution
        val u1 = random.nextFloat()
        val u2 = random.nextFloat()
        return sqrt(-2.0 * ln(u1.toDouble())).toFloat() * cos(2.0 * PI * u2).toFloat()
    }
}

/**
 * Filter result with validation
 */
private data class FilterResult(
    val bitmap: Bitmap,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun isValid(): Boolean {
        return !bitmap.isRecycled && System.currentTimeMillis() - timestamp < 300000 // 5 minutes
    }
}

/**
 * Blend modes for combining images
 */
enum class BlendMode {
    NORMAL, MULTIPLY, SCREEN, OVERLAY, SOFT_LIGHT, HARD_LIGHT, DARKEN, LIGHTEN
}