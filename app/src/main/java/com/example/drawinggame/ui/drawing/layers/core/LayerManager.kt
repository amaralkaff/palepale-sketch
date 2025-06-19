package com.example.drawinggame.ui.drawing.layers.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import com.example.drawinggame.ui.drawing.advanced.blending.BlendMode
import com.example.drawinggame.ui.drawing.utils.BitmapUtils
import java.util.*

/**
 * Manages the layer system for the drawing engine
 * Phase 5.2: Layer System implementation
 */
class LayerManager(private val context: Context) {
    
    private val layers = mutableListOf<DrawingLayer>()
    private var activeLayerId: String? = null
    private val layerListeners = mutableListOf<LayerListener>()
    
    // Canvas dimensions
    private var canvasWidth: Int = 0
    private var canvasHeight: Int = 0
    
    // Background layer (always present)
    private var backgroundLayer: DrawingLayer? = null
    
    /**
     * Initialize the layer system
     */
    fun initialize(width: Int, height: Int) {
        canvasWidth = width
        canvasHeight = height
        
        // Clear existing layers
        layers.forEach { it.cleanup() }
        layers.clear()
        
        // Create background layer
        backgroundLayer = createBackgroundLayer()
        layers.add(backgroundLayer!!)
        activeLayerId = backgroundLayer!!.id
        
        notifyLayersChanged()
    }
    
    /**
     * Add a new layer
     */
    fun addLayer(type: LayerType, name: String): DrawingLayer {
        val layer = DrawingLayer(
            name = name,
            type = type,
            bitmap = if (type == LayerType.DRAWING) {
                BitmapUtils.createTransparentBitmap(canvasWidth, canvasHeight)
            } else null
        )
        
        // Insert above current active layer
        val insertIndex = getActiveLayerIndex() + 1
        layers.add(insertIndex, layer)
        
        // Set as active layer
        activeLayerId = layer.id
        
        notifyLayerAdded(layer)
        notifyLayersChanged()
        
        return layer
    }
    
    /**
     * Delete a layer
     */
    fun deleteLayer(layerId: String): Boolean {
        if (layerId == backgroundLayer?.id) {
            return false // Cannot delete background layer
        }
        
        val layerIndex = layers.indexOfFirst { it.id == layerId }
        if (layerIndex == -1) return false
        
        val layer = layers.removeAt(layerIndex)
        layer.cleanup()
        
        // Update active layer if needed
        if (activeLayerId == layerId) {
            activeLayerId = if (layers.isNotEmpty()) {
                layers.getOrNull(layerIndex.coerceAtMost(layers.size - 1))?.id
            } else {
                null
            }
        }
        
        notifyLayerDeleted(layer)
        notifyLayersChanged()
        
        return true
    }
    
    /**
     * Duplicate a layer
     */
    fun duplicateLayer(layerId: String): DrawingLayer? {
        val layer = getLayer(layerId) ?: return null
        
        val duplicatedLayer = layer.copyLayer(
            newName = "${layer.name} Copy"
        )
        
        // Insert above original layer
        val originalIndex = layers.indexOfFirst { it.id == layerId }
        layers.add(originalIndex + 1, duplicatedLayer)
        
        // Set as active layer
        activeLayerId = duplicatedLayer.id
        
        notifyLayerAdded(duplicatedLayer)
        notifyLayersChanged()
        
        return duplicatedLayer
    }
    
    /**
     * Move layer to new position
     */
    fun moveLayer(layerId: String, newIndex: Int): Boolean {
        val currentIndex = layers.indexOfFirst { it.id == layerId }
        if (currentIndex == -1 || newIndex < 0 || newIndex >= layers.size) {
            return false
        }
        
        // Don't allow moving background layer
        val layer = layers[currentIndex]
        if (layer.type == LayerType.BACKGROUND) {
            return false
        }
        
        layers.removeAt(currentIndex)
        layers.add(newIndex, layer)
        
        notifyLayersChanged()
        return true
    }
    
    /**
     * Merge layer down
     */
    fun mergeDown(layerId: String): Boolean {
        val layerIndex = layers.indexOfFirst { it.id == layerId }
        if (layerIndex <= 0) return false // Cannot merge bottom layer or invalid layer
        
        val upperLayer = layers[layerIndex]
        val lowerLayer = layers[layerIndex - 1]
        
        // Merge upper layer into lower layer
        val mergedBitmap = compositeLayers(listOf(lowerLayer, upperLayer))
        lowerLayer.bitmap?.recycle()
        lowerLayer.bitmap = mergedBitmap
        lowerLayer.markModified()
        
        // Remove upper layer
        layers.removeAt(layerIndex)
        upperLayer.cleanup()
        
        // Update active layer
        if (activeLayerId == layerId) {
            activeLayerId = lowerLayer.id
        }
        
        notifyLayerDeleted(upperLayer)
        notifyLayersChanged()
        
        return true
    }
    
    /**
     * Set layer opacity
     */
    fun setLayerOpacity(layerId: String, opacity: Float) {
        val layer = getLayer(layerId) ?: return
        layer.opacity = opacity.coerceIn(0f, 1f)
        layer.markModified()
        notifyLayerPropertyChanged(layer)
    }
    
    /**
     * Set layer blend mode
     */
    fun setLayerBlendMode(layerId: String, blendMode: BlendMode) {
        val layer = getLayer(layerId) ?: return
        layer.blendMode = blendMode
        layer.markModified()
        notifyLayerPropertyChanged(layer)
    }
    
    /**
     * Toggle layer visibility
     */
    fun toggleLayerVisibility(layerId: String) {
        val layer = getLayer(layerId) ?: return
        layer.isVisible = !layer.isVisible
        layer.markModified()
        notifyLayerPropertyChanged(layer)
    }
    
    /**
     * Set layer locked state
     */
    fun setLayerLocked(layerId: String, locked: Boolean) {
        val layer = getLayer(layerId) ?: return
        layer.isLocked = locked
        layer.markModified()
        notifyLayerPropertyChanged(layer)
    }
    
    /**
     * Set active layer
     */
    fun setActiveLayer(layerId: String) {
        if (getLayer(layerId) != null) {
            activeLayerId = layerId
            notifyActiveLayerChanged(layerId)
        }
    }
    
    /**
     * Get active layer
     */
    fun getActiveLayer(): DrawingLayer? {
        return activeLayerId?.let { getLayer(it) }
    }
    
    /**
     * Get current layer (alias for getActiveLayer)
     */
    fun getCurrentLayer(): DrawingLayer? {
        return getActiveLayer()
    }
    
    /**
     * Set layer visibility
     */
    fun setLayerVisibility(layerId: String, visible: Boolean) {
        val layer = getLayer(layerId) ?: return
        layer.isVisible = visible
        layer.markModified()
        notifyLayerPropertyChanged(layer)
    }
    
    /**
     * Add layer effect
     */
    fun addLayerEffect(layerId: String, effect: com.example.drawinggame.ui.drawing.layers.effects.LayerEffect) {
        val layer = getLayer(layerId) ?: return
        layer.effects = layer.effects + effect
        layer.markModified()
        notifyLayerPropertyChanged(layer)
    }
    
    /**
     * Remove layer effect
     */
    fun removeLayerEffect(layerId: String, effect: com.example.drawinggame.ui.drawing.layers.effects.LayerEffect) {
        val layer = getLayer(layerId) ?: return
        layer.effects = layer.effects - effect
        layer.markModified()
        notifyLayerPropertyChanged(layer)
    }
    
    /**
     * Get layer by ID
     */
    fun getLayer(layerId: String): DrawingLayer? {
        return layers.find { it.id == layerId }
    }
    
    /**
     * Get all layers
     */
    fun getLayers(): List<DrawingLayer> {
        return layers.toList()
    }
    
    /**
     * Get visible layers
     */
    fun getVisibleLayers(): List<DrawingLayer> {
        return layers.filter { it.isVisible }
    }
    
    /**
     * Generate composite image from all visible layers
     */
    fun getCompositeImage(): Bitmap? {
        val visibleLayers = getVisibleLayers()
        if (visibleLayers.isEmpty()) return null
        
        return compositeLayers(visibleLayers)
    }
    
    /**
     * Flatten all layers into a single bitmap
     */
    fun flattenLayers(): Bitmap? {
        val composite = getCompositeImage() ?: return null
        
        // Clear all layers except background
        layers.filter { it.type != LayerType.BACKGROUND }.forEach { layer ->
            layer.cleanup()
        }
        layers.removeAll { it.type != LayerType.BACKGROUND }
        
        // Set composite as background
        backgroundLayer?.bitmap?.recycle()
        backgroundLayer?.bitmap = composite
        backgroundLayer?.markModified()
        
        notifyLayersChanged()
        return composite
    }
    
    /**
     * Add layer listener
     */
    fun addListener(listener: LayerListener) {
        if (!layerListeners.contains(listener)) {
            layerListeners.add(listener)
        }
    }
    
    /**
     * Remove layer listener
     */
    fun removeListener(listener: LayerListener) {
        layerListeners.remove(listener)
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        layers.forEach { it.cleanup() }
        layers.clear()
        layerListeners.clear()
        backgroundLayer = null
        activeLayerId = null
    }
    
    // Private helper methods
    
    private fun createBackgroundLayer(): DrawingLayer {
        return DrawingLayer(
            name = "Background",
            type = LayerType.BACKGROUND,
            bitmap = BitmapUtils.createBitmap(canvasWidth, canvasHeight, android.graphics.Color.WHITE),
            isLocked = false
        )
    }
    
    private fun getActiveLayerIndex(): Int {
        return activeLayerId?.let { id ->
            layers.indexOfFirst { it.id == id }
        } ?: 0
    }
    
    private fun compositeLayers(layerList: List<DrawingLayer>): Bitmap? {
        if (layerList.isEmpty()) return null
        
        val composite = BitmapUtils.createTransparentBitmap(canvasWidth, canvasHeight)
        val canvas = Canvas(composite)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        
        layerList.forEach { layer ->
            if (layer.isVisible && layer.bitmap != null && !layer.bitmap!!.isRecycled) {
                // Apply layer opacity
                paint.alpha = (layer.opacity * 255).toInt()
                
                // Apply blend mode (simplified implementation)
                paint.xfermode = when (layer.blendMode) {
                    BlendMode.MULTIPLY -> PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
                    BlendMode.SCREEN -> PorterDuffXfermode(PorterDuff.Mode.SCREEN)
                    BlendMode.OVERLAY -> PorterDuffXfermode(PorterDuff.Mode.OVERLAY)
                    else -> null // Normal mode
                }
                
                canvas.drawBitmap(layer.bitmap!!, 0f, 0f, paint)
                
                // Reset paint for next layer
                paint.xfermode = null
                paint.alpha = 255
            }
        }
        
        return composite
    }
    
    // Notification methods
    
    private fun notifyLayersChanged() {
        layerListeners.forEach { it.onLayersChanged(layers) }
    }
    
    private fun notifyLayerAdded(layer: DrawingLayer) {
        layerListeners.forEach { it.onLayerAdded(layer) }
    }
    
    private fun notifyLayerDeleted(layer: DrawingLayer) {
        layerListeners.forEach { it.onLayerDeleted(layer) }
    }
    
    private fun notifyLayerPropertyChanged(layer: DrawingLayer) {
        layerListeners.forEach { it.onLayerPropertyChanged(layer) }
    }
    
    private fun notifyActiveLayerChanged(layerId: String) {
        layerListeners.forEach { it.onActiveLayerChanged(layerId) }
    }
}

/**
 * Interface for layer change notifications
 */
interface LayerListener {
    fun onLayersChanged(layers: List<DrawingLayer>) {}
    fun onLayerAdded(layer: DrawingLayer) {}
    fun onLayerDeleted(layer: DrawingLayer) {}
    fun onLayerPropertyChanged(layer: DrawingLayer) {}
    fun onActiveLayerChanged(layerId: String) {}
} 