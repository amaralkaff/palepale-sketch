package com.example.drawinggame.ui.drawing.color.effects

import android.graphics.Bitmap
import com.example.drawinggame.ui.drawing.color.adjustments.AdjustmentLayer
import kotlinx.coroutines.*
import kotlin.math.max

/**
 * Central manager for image effects and adjustments
 * Handles both real-time preview and final processing
 */
class EffectManager private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: EffectManager? = null
        
        fun getInstance(): EffectManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EffectManager().also { INSTANCE = it }
            }
        }
    }
    
    private val filterEngine = FilterEngine.getInstance()
    private val effectStack = mutableListOf<Effect>()
    private var previewJob: Job? = null
    private var previewCallback: ((Bitmap?) -> Unit)? = null
    
    // Performance settings
    private var maxPreviewSize = 512
    private var previewQuality = 0.8f
    private var usePreviewOptimizations = true
    
    /**
     * Initialize the effect manager
     */
    fun initialize(context: android.content.Context) {
        filterEngine.initialize(context)
    }
    
    /**
     * Release resources
     */
    fun release() {
        previewJob?.cancel()
        effectStack.clear()
        filterEngine.release()
    }
    
    /**
     * Add effect to the stack
     */
    fun addEffect(effect: Effect) {
        effectStack.add(effect)
        invalidatePreview()
    }
    
    /**
     * Insert effect at specific position
     */
    fun insertEffect(position: Int, effect: Effect) {
        if (position in 0..effectStack.size) {
            effectStack.add(position, effect)
            invalidatePreview()
        }
    }
    
    /**
     * Remove effect from stack
     */
    fun removeEffect(effect: Effect): Boolean {
        val removed = effectStack.remove(effect)
        if (removed) {
            invalidatePreview()
        }
        return removed
    }
    
    /**
     * Remove effect by index
     */
    fun removeEffectAt(index: Int): Effect? {
        if (index in effectStack.indices) {
            val removed = effectStack.removeAt(index)
            invalidatePreview()
            return removed
        }
        return null
    }
    
    /**
     * Move effect to new position
     */
    fun moveEffect(fromIndex: Int, toIndex: Int): Boolean {
        if (fromIndex in effectStack.indices && toIndex in 0..effectStack.size) {
            val effect = effectStack.removeAt(fromIndex)
            val insertIndex = if (toIndex > fromIndex) toIndex - 1 else toIndex
            effectStack.add(insertIndex, effect)
            invalidatePreview()
            return true
        }
        return false
    }
    
    /**
     * Clear all effects
     */
    fun clearEffects() {
        effectStack.clear()
        invalidatePreview()
    }
    
    /**
     * Get current effect stack
     */
    fun getEffects(): List<Effect> = effectStack.toList()
    
    /**
     * Enable or disable effect
     */
    fun setEffectEnabled(effect: Effect, enabled: Boolean) {
        val index = effectStack.indexOf(effect)
        if (index >= 0) {
            effectStack[index] = effect.copy(enabled = enabled)
            invalidatePreview()
        }
    }
    
    /**
     * Update effect parameters
     */
    fun updateEffect(effect: Effect, newEffect: Effect) {
        val index = effectStack.indexOf(effect)
        if (index >= 0) {
            effectStack[index] = newEffect
            invalidatePreview()
        }
    }
    
    /**
     * Set preview callback for real-time updates
     */
    fun setPreviewCallback(callback: (Bitmap?) -> Unit) {
        previewCallback = callback
    }
    
    /**
     * Generate preview with current effects
     */
    fun generatePreview(source: Bitmap) {
        previewJob?.cancel()
        previewJob = CoroutineScope(Dispatchers.Default).launch {
            try {
                val preview = if (usePreviewOptimizations) {
                    val scaledSource = scaleForPreview(source)
                    val result = applyEffects(scaledSource, forPreview = true)
                    result
                } else {
                    applyEffects(source, forPreview = true)
                }
                
                withContext(Dispatchers.Main) {
                    if (isActive) {
                        previewCallback?.invoke(preview)
                    }
                }
            } catch (e: CancellationException) {
                // Preview was cancelled, ignore
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    previewCallback?.invoke(null)
                }
            }
        }
    }
    
    /**
     * Apply all effects to source bitmap (final processing)
     */
    suspend fun processImage(source: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        applyEffects(source, forPreview = false)
    }
    
    /**
     * Apply effects with optional preview optimizations
     */
    private suspend fun applyEffects(source: Bitmap, forPreview: Boolean): Bitmap {
        var result = source.copy(source.config ?: Bitmap.Config.ARGB_8888, false)
        
        for (effect in effectStack) {
            if (!effect.enabled) continue
            
            result = when (effect.type) {
                EffectType.FILTER -> {
                    filterEngine.applyFilter(result, effect.filter!!, useCache = forPreview)
                }
                EffectType.ADJUSTMENT -> {
                    applyAdjustmentLayer(result, effect.adjustment!!)
                }
                EffectType.BLEND -> {
                    applyBlendEffect(result, effect)
                }
            }
            
            // Yield to allow cancellation
            yield()
        }
        
        return result
    }
    
    /**
     * Apply adjustment layer to bitmap
     */
    private fun applyAdjustmentLayer(source: Bitmap, adjustment: AdjustmentLayer): Bitmap {
        return adjustment.apply(source)
    }
    
    /**
     * Apply blend effect
     */
    private fun applyBlendEffect(source: Bitmap, effect: Effect): Bitmap {
        val blendLayer = effect.blendLayer ?: return source
        val blendMode = effect.blendMode ?: BlendMode.NORMAL
        val opacity = effect.opacity
        
        return blendBitmaps(source, blendLayer, blendMode, opacity)
    }
    
    /**
     * Blend two bitmaps with specified mode and opacity
     */
    private fun blendBitmaps(
        base: Bitmap,
        overlay: Bitmap,
        mode: BlendMode,
        opacity: Float
    ): Bitmap {
        if (base.width != overlay.width || base.height != overlay.height) {
            return base // Size mismatch, return base
        }
        
        val width = base.width
        val height = base.height
        val basePixels = IntArray(width * height)
        val overlayPixels = IntArray(width * height)
        val resultPixels = IntArray(width * height)
        
        base.getPixels(basePixels, 0, width, 0, 0, width, height)
        overlay.getPixels(overlayPixels, 0, width, 0, 0, width, height)
        
        for (i in basePixels.indices) {
            resultPixels[i] = blendPixels(basePixels[i], overlayPixels[i], mode, opacity)
        }
        
        val result = Bitmap.createBitmap(width, height, base.config ?: Bitmap.Config.ARGB_8888)
        result.setPixels(resultPixels, 0, width, 0, 0, width, height)
        return result
    }
    
    /**
     * Blend two pixels with specified mode and opacity
     */
    private fun blendPixels(base: Int, overlay: Int, mode: BlendMode, opacity: Float): Int {
        val baseA = android.graphics.Color.alpha(base)
        val baseR = android.graphics.Color.red(base)
        val baseG = android.graphics.Color.green(base)
        val baseB = android.graphics.Color.blue(base)
        
        val overlayA = android.graphics.Color.alpha(overlay)
        val overlayR = android.graphics.Color.red(overlay)
        val overlayG = android.graphics.Color.green(overlay)
        val overlayB = android.graphics.Color.blue(overlay)
        
        // Apply blend mode
        val blendedR = when (mode) {
            BlendMode.NORMAL -> overlayR
            BlendMode.MULTIPLY -> (baseR * overlayR) / 255
            BlendMode.SCREEN -> 255 - ((255 - baseR) * (255 - overlayR)) / 255
            BlendMode.OVERLAY -> if (baseR < 128) {
                (2 * baseR * overlayR) / 255
            } else {
                255 - (2 * (255 - baseR) * (255 - overlayR)) / 255
            }
            BlendMode.SOFT_LIGHT -> applySoftLight(baseR, overlayR)
            BlendMode.HARD_LIGHT -> if (overlayR < 128) {
                (2 * baseR * overlayR) / 255
            } else {
                255 - (2 * (255 - baseR) * (255 - overlayR)) / 255
            }
            BlendMode.DARKEN -> kotlin.math.min(baseR, overlayR)
            BlendMode.LIGHTEN -> kotlin.math.max(baseR, overlayR)
        }
        
        val blendedG = when (mode) {
            BlendMode.NORMAL -> overlayG
            BlendMode.MULTIPLY -> (baseG * overlayG) / 255
            BlendMode.SCREEN -> 255 - ((255 - baseG) * (255 - overlayG)) / 255
            BlendMode.OVERLAY -> if (baseG < 128) {
                (2 * baseG * overlayG) / 255
            } else {
                255 - (2 * (255 - baseG) * (255 - overlayG)) / 255
            }
            BlendMode.SOFT_LIGHT -> applySoftLight(baseG, overlayG)
            BlendMode.HARD_LIGHT -> if (overlayG < 128) {
                (2 * baseG * overlayG) / 255
            } else {
                255 - (2 * (255 - baseG) * (255 - overlayG)) / 255
            }
            BlendMode.DARKEN -> kotlin.math.min(baseG, overlayG)
            BlendMode.LIGHTEN -> kotlin.math.max(baseG, overlayG)
        }
        
        val blendedB = when (mode) {
            BlendMode.NORMAL -> overlayB
            BlendMode.MULTIPLY -> (baseB * overlayB) / 255
            BlendMode.SCREEN -> 255 - ((255 - baseB) * (255 - overlayB)) / 255
            BlendMode.OVERLAY -> if (baseB < 128) {
                (2 * baseB * overlayB) / 255
            } else {
                255 - (2 * (255 - baseB) * (255 - overlayB)) / 255
            }
            BlendMode.SOFT_LIGHT -> applySoftLight(baseB, overlayB)
            BlendMode.HARD_LIGHT -> if (overlayB < 128) {
                (2 * baseB * overlayB) / 255
            } else {
                255 - (2 * (255 - baseB) * (255 - overlayB)) / 255
            }
            BlendMode.DARKEN -> kotlin.math.min(baseB, overlayB)
            BlendMode.LIGHTEN -> kotlin.math.max(baseB, overlayB)
        }
        
        // Apply opacity
        val finalR = (baseR + (blendedR - baseR) * opacity).toInt().coerceIn(0, 255)
        val finalG = (baseG + (blendedG - baseG) * opacity).toInt().coerceIn(0, 255)
        val finalB = (baseB + (blendedB - baseB) * opacity).toInt().coerceIn(0, 255)
        val finalA = (baseA + (overlayA - baseA) * opacity).toInt().coerceIn(0, 255)
        
        return android.graphics.Color.argb(finalA, finalR, finalG, finalB)
    }
    
    /**
     * Apply soft light blend mode calculation
     */
    private fun applySoftLight(base: Int, overlay: Int): Int {
        val b = base / 255f
        val o = overlay / 255f
        
        val result = if (o <= 0.5f) {
            b - (1f - 2f * o) * b * (1f - b)
        } else {
            val d = if (b <= 0.25f) {
                ((16f * b - 12f) * b + 4f) * b
            } else {
                kotlin.math.sqrt(b)
            }
            b + (2f * o - 1f) * (d - b)
        }
        
        return (result * 255f).toInt().coerceIn(0, 255)
    }
    
    /**
     * Scale bitmap for preview if needed
     */
    private fun scaleForPreview(source: Bitmap): Bitmap {
        val maxDimension = max(source.width, source.height)
        if (maxDimension <= maxPreviewSize) {
            return source
        }
        
        val scaleFactor = maxPreviewSize.toFloat() / maxDimension
        val newWidth = (source.width * scaleFactor).toInt()
        val newHeight = (source.height * scaleFactor).toInt()
        
        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true)
    }
    
    /**
     * Invalidate current preview
     */
    private fun invalidatePreview() {
        previewJob?.cancel()
    }
    
    /**
     * Performance settings
     */
    fun setPreviewSettings(
        maxSize: Int = 512,
        quality: Float = 0.8f,
        useOptimizations: Boolean = true
    ) {
        maxPreviewSize = maxSize
        previewQuality = quality
        usePreviewOptimizations = useOptimizations
    }
    
    /**
     * Get effect statistics
     */
    fun getEffectStats(): EffectStats {
        val enabled = effectStack.count { it.enabled }
        val total = effectStack.size
        val hasFilters = effectStack.any { it.type == EffectType.FILTER && it.enabled }
        val hasAdjustments = effectStack.any { it.type == EffectType.ADJUSTMENT && it.enabled }
        val hasBlends = effectStack.any { it.type == EffectType.BLEND && it.enabled }
        
        return EffectStats(
            totalEffects = total,
            enabledEffects = enabled,
            hasFilters = hasFilters,
            hasAdjustments = hasAdjustments,
            hasBlendEffects = hasBlends,
            processingComplexity = calculateComplexity()
        )
    }
    
    /**
     * Calculate processing complexity score
     */
    private fun calculateComplexity(): Float {
        var complexity = 0f
        
        for (effect in effectStack) {
            if (!effect.enabled) continue
            
            complexity += when (effect.type) {
                EffectType.FILTER -> when (effect.filter) {
                    is BlurFilter -> 0.3f
                    is SharpenFilter -> 0.2f
                    is NoiseFilter -> 0.1f
                    is EdgeDetectionFilter -> 0.4f
                    is EmbossFilter -> 0.3f
                    is OilPaintingFilter -> 0.8f
                    is WatercolorFilter -> 0.7f
                    is VintageFilter -> 0.2f
                    is DistortionFilter -> 0.6f
                    is ColorMatrixFilter -> 0.1f
                    else -> 0.5f
                }
                EffectType.ADJUSTMENT -> 0.2f
                EffectType.BLEND -> 0.4f
            }
        }
        
        return complexity
    }
}

/**
 * Effect container for the effect stack
 */
data class Effect(
    val type: EffectType,
    val enabled: Boolean = true,
    val opacity: Float = 1f,
    val filter: ImageFilter? = null,
    val adjustment: AdjustmentLayer? = null,
    val blendLayer: Bitmap? = null,
    val blendMode: BlendMode? = null
) {
    
    fun copy(
        enabled: Boolean = this.enabled,
        opacity: Float = this.opacity,
        filter: ImageFilter? = this.filter,
        adjustment: AdjustmentLayer? = this.adjustment,
        blendLayer: Bitmap? = this.blendLayer,
        blendMode: BlendMode? = this.blendMode
    ): Effect {
        return Effect(type, enabled, opacity, filter, adjustment, blendLayer, blendMode)
    }
    
    fun getName(): String {
        return when (type) {
            EffectType.FILTER -> filter?.name ?: "Filter"
            EffectType.ADJUSTMENT -> adjustment?.name ?: "Adjustment"
            EffectType.BLEND -> "Blend Layer"
        }
    }
    
    fun getDescription(): String {
        return when (type) {
            EffectType.FILTER -> filter?.description ?: "Image filter effect"
            EffectType.ADJUSTMENT -> adjustment?.description ?: "Image adjustment"
            EffectType.BLEND -> "Blend layer effect"
        }
    }
}

/**
 * Effect types
 */
enum class EffectType {
    FILTER, ADJUSTMENT, BLEND
}

/**
 * Effect statistics
 */
data class EffectStats(
    val totalEffects: Int,
    val enabledEffects: Int,
    val hasFilters: Boolean,
    val hasAdjustments: Boolean,
    val hasBlendEffects: Boolean,
    val processingComplexity: Float
)