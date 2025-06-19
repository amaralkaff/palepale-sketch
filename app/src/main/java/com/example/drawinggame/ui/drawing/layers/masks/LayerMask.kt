package com.example.drawinggame.ui.drawing.layers.masks

import android.graphics.Bitmap

/**
 * Interface for layer masks
 * Phase 5.2: Layer System - Layer Masks
 */
interface LayerMask {
    val id: String
    val name: String
    val enabled: Boolean
    
    /**
     * Apply mask to a bitmap
     */
    fun applyMask(source: Bitmap): Bitmap
    
    /**
     * Get mask bitmap
     */
    fun getMaskBitmap(): Bitmap?
    
    /**
     * Set mask bitmap
     */
    fun setMaskBitmap(bitmap: Bitmap)
    
    /**
     * Clean up resources
     */
    fun cleanup()
}

/**
 * Alpha mask implementation
 */
data class AlphaMask(
    override val id: String,
    override val name: String,
    override val enabled: Boolean = true,
    private var maskBitmap: Bitmap? = null,
    val inverted: Boolean = false
) : LayerMask {
    
    override fun applyMask(source: Bitmap): Bitmap {
        val mask = maskBitmap ?: return source
        
        // TODO: Implement alpha mask application
        // For now, return the original bitmap
        return source
    }
    
    override fun getMaskBitmap(): Bitmap? = maskBitmap
    
    override fun setMaskBitmap(bitmap: Bitmap) {
        maskBitmap = bitmap
    }
    
    override fun cleanup() {
        maskBitmap?.let { 
            if (!it.isRecycled) {
                it.recycle()
            }
        }
        maskBitmap = null
    }
}

/**
 * Vector mask implementation
 */
data class VectorMask(
    override val id: String,
    override val name: String,
    override val enabled: Boolean = true,
    private var maskBitmap: Bitmap? = null
) : LayerMask {
    
    override fun applyMask(source: Bitmap): Bitmap {
        val mask = maskBitmap ?: return source
        
        // TODO: Implement vector mask application
        return source
    }
    
    override fun getMaskBitmap(): Bitmap? = maskBitmap
    
    override fun setMaskBitmap(bitmap: Bitmap) {
        maskBitmap = bitmap
    }
    
    override fun cleanup() {
        maskBitmap?.let { 
            if (!it.isRecycled) {
                it.recycle()
            }
        }
        maskBitmap = null
    }
} 