package com.example.drawinggame.ui.drawing.layers.core

import android.graphics.Bitmap
import com.example.drawinggame.ui.drawing.layers.effects.LayerEffect
import com.example.drawinggame.ui.drawing.layers.masks.LayerMask
import com.example.drawinggame.ui.drawing.advanced.blending.BlendMode
import java.util.*

/**
 * Represents a drawing layer with all its properties and content
 * Phase 5.2: Layer System implementation
 */
data class DrawingLayer(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var isVisible: Boolean = true,
    var opacity: Float = 1.0f, // 0.0 to 1.0
    var blendMode: BlendMode = BlendMode.NORMAL,
    var isLocked: Boolean = false,
    val type: LayerType = LayerType.DRAWING,
    var bitmap: Bitmap? = null,
    var effects: List<LayerEffect> = emptyList(),
    var mask: LayerMask? = null,
    var clippingMask: String? = null, // ID of layer to clip to
    var parent: String? = null, // For group layers
    val createdAt: Long = System.currentTimeMillis(),
    var modifiedAt: Long = System.currentTimeMillis()
) {
    
    /**
     * Update the modification timestamp
     */
    fun markModified() {
        modifiedAt = System.currentTimeMillis()
    }
    
    /**
     * Check if layer has content
     */
    fun hasContent(): Boolean {
        return bitmap != null && !bitmap!!.isRecycled
    }
    
    /**
     * Get effective opacity (considering parent groups)
     */
    fun getEffectiveOpacity(): Float {
        return opacity // TODO: Consider parent group opacity
    }
    
    /**
     * Check if layer is editable (visible and not locked)
     */
    fun isEditable(): Boolean {
        return isVisible && !isLocked
    }
    
    /**
     * Create a copy of this layer
     */
    fun copyLayer(
        newId: String = UUID.randomUUID().toString(),
        newName: String = "$name Copy"
    ): DrawingLayer {
        return DrawingLayer(
            id = newId,
            name = newName,
            isVisible = this.isVisible,
            opacity = this.opacity,
            blendMode = this.blendMode,
            isLocked = this.isLocked,
            type = this.type,
            bitmap = bitmap?.copy(bitmap!!.config ?: Bitmap.Config.ARGB_8888, false), // Copy bitmap
            effects = effects.toList(), // Copy effects list
            mask = mask, // TODO: Deep copy mask
            clippingMask = this.clippingMask,
            parent = this.parent,
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        bitmap?.let { 
            if (!it.isRecycled) {
                it.recycle()
            }
        }
        bitmap = null
        effects.forEach { it.cleanup() }
        mask?.cleanup()
    }
}

/**
 * Types of layers supported by the system
 */
enum class LayerType(val displayName: String) {
    BACKGROUND("Background"),
    DRAWING("Drawing"),
    GROUP("Group"),
    ADJUSTMENT("Adjustment"),
    TEXT("Text"),
    REFERENCE("Reference");
    
    companion object {
        fun fromDisplayName(displayName: String): LayerType? {
            return values().find { it.displayName == displayName }
        }
    }
} 