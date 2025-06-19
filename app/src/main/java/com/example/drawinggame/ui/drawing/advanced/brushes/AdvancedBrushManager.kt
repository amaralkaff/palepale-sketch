package com.example.drawinggame.ui.drawing.advanced.brushes

import android.content.Context
import android.graphics.Paint
import android.view.MotionEvent
import com.example.drawinggame.ui.drawing.brush.BrushManager
import com.example.drawinggame.ui.drawing.advanced.brushes.factories.*
import com.example.drawinggame.ui.drawing.advanced.brushes.pressure.PressureDetector
import com.example.drawinggame.ui.drawing.advanced.brushes.pressure.PressureSettings
import com.example.drawinggame.ui.drawing.advanced.textures.BrushTextureManager
import com.example.drawinggame.ui.drawing.advanced.textures.BrushTexture
import com.example.drawinggame.ui.drawing.advanced.blending.BlendMode

/**
 * Extended brush manager with advanced brush capabilities
 * Maintains compatibility with existing BrushManager while adding Phase 5.1 features
 */
class AdvancedBrushManager(context: Context) : BrushManager() {
    
    // Advanced brush properties
    private var currentAdvancedTool: AdvancedBrushType = AdvancedBrushType.WATERCOLOR
    private var isAdvancedMode: Boolean = false
    
    // Pressure sensitivity
    private val pressureDetector = PressureDetector()
    private var pressureSettings = PressureSettings()
    
    // Texture system
    private val textureManager = BrushTextureManager(context)
    private var currentTexture: BrushTexture? = null
    
    // Blend modes
    private var currentBlendMode: BlendMode = BlendMode.NORMAL
    
    // Advanced brush factories
    private val brushFactories = mapOf<AdvancedBrushType, AdvancedBrushFactory>(
        AdvancedBrushType.WATERCOLOR to WatercolorBrushFactory(),
        AdvancedBrushType.OIL_PAINT to OilPaintBrushFactory(),
        AdvancedBrushType.SPRAY_PAINT to SprayPaintBrushFactory(),
        AdvancedBrushType.CHARCOAL to CharcoalBrushFactory(),
        AdvancedBrushType.ACRYLIC to OilPaintBrushFactory(), // Similar to oil paint
        AdvancedBrushType.CHALK to CharcoalBrushFactory(), // Similar to charcoal
        AdvancedBrushType.PENCIL to PencilBrushFactory(),
        AdvancedBrushType.MARKER to MarkerBrushFactory(),
        AdvancedBrushType.HIGHLIGHTER to MarkerBrushFactory() // Similar to marker
    )
    
    /**
     * Set advanced brush tool
     */
    fun setAdvancedTool(tool: AdvancedBrushType) {
        currentAdvancedTool = tool
        isAdvancedMode = true
    }
    
    /**
     * Switch back to basic brush mode
     */
    fun setBasicMode() {
        isAdvancedMode = false
    }
    
    /**
     * Check if currently in advanced mode
     */
    fun isAdvancedMode(): Boolean = isAdvancedMode
    
    /**
     * Get current advanced tool
     */
    fun getCurrentAdvancedTool(): AdvancedBrushType = currentAdvancedTool
    
    /**
     * Set pressure sensitivity settings
     */
    fun setPressureSettings(settings: PressureSettings) {
        pressureSettings = settings
    }
    
    /**
     * Set current texture
     */
    fun setTexture(texture: BrushTexture?) {
        currentTexture = texture
    }
    
    /**
     * Set blend mode
     */
    fun setBlendMode(blendMode: BlendMode) {
        currentBlendMode = blendMode
    }
    
    /**
     * Get paint with pressure applied from motion event
     */
    fun getPaintWithPressure(motionEvent: MotionEvent): Paint {
        val basePaint = if (isAdvancedMode) {
            getAdvancedPaint()
        } else {
            getCurrentPaint()
        }
        
        // Apply pressure if supported
        if (pressureDetector.isSupported()) {
            val pressure = pressureDetector.getPressure(motionEvent)
            applyPressureToPaint(basePaint, pressure)
        }
        
        return basePaint
    }
    
    /**
     * Get advanced brush paint
     */
    fun getAdvancedPaint(): Paint {
        if (!isAdvancedMode) {
            return getCurrentPaint()
        }
        
        val factory = brushFactories[currentAdvancedTool]
            ?: return getCurrentPaint()
        
        val settings = AdvancedBrushSettings(
            type = currentAdvancedTool,
            size = getCurrentSize(),
            color = getCurrentColor(),
            opacity = getCurrentOpacity(),
            pressureSettings = pressureSettings,
            texture = currentTexture,
            blendMode = currentBlendMode
        )
        
        val paint = factory.createPaint(settings)
        
        // Apply texture if available
        currentTexture?.let { texture ->
            factory.applyTexture(paint, texture)
        }
        
        return paint
    }
    
    /**
     * Get preview paint for advanced brushes
     */
    fun getAdvancedPreviewPaint(): Paint {
        if (!isAdvancedMode) {
            return getPreviewPaint()
        }
        
        val factory = brushFactories[currentAdvancedTool]
            ?: return getPreviewPaint()
        
        val settings = AdvancedBrushSettings(
            type = currentAdvancedTool,
            size = getCurrentSize(),
            color = getCurrentColor(),
            opacity = getCurrentOpacity(),
            pressureSettings = pressureSettings,
            texture = currentTexture,
            blendMode = currentBlendMode
        )
        
        return factory.createPreviewPaint(settings)
    }
    
    /**
     * Get all available advanced brush types
     */
    fun getAvailableAdvancedBrushes(): List<AdvancedBrushType> {
        return brushFactories.keys.toList()
    }
    
    /**
     * Get all available textures
     */
    fun getAvailableTextures(): List<BrushTexture> {
        return textureManager.getAvailableTextures()
    }
    
    /**
     * Check if pressure sensitivity is supported
     */
    fun isPressureSupported(): Boolean {
        return pressureDetector.isSupported()
    }
    
    /**
     * Get current pressure settings
     */
    fun getPressureSettings(): PressureSettings = pressureSettings
    
    /**
     * Get current texture
     */
    fun getCurrentTexture(): BrushTexture? = currentTexture
    
    /**
     * Get current blend mode
     */
    fun getCurrentBlendMode(): BlendMode = currentBlendMode
    
    /**
     * Apply special brush effects based on brush type
     */
    fun applySpecialEffects(paint: Paint, intensity: Float = 1.0f) {
        when (currentAdvancedTool) {
            AdvancedBrushType.WATERCOLOR -> {
                val factory = brushFactories[AdvancedBrushType.WATERCOLOR] as? WatercolorBrushFactory
                factory?.applyWetOnWetEffect(paint, intensity)
            }
            AdvancedBrushType.OIL_PAINT -> {
                val factory = brushFactories[AdvancedBrushType.OIL_PAINT] as? OilPaintBrushFactory
                factory?.applyImpastoEffect(paint, intensity)
            }
            AdvancedBrushType.SPRAY_PAINT -> {
                val factory = brushFactories[AdvancedBrushType.SPRAY_PAINT] as? SprayPaintBrushFactory
                factory?.applySprayDensity(paint, intensity)
            }
            AdvancedBrushType.CHARCOAL -> {
                val factory = brushFactories[AdvancedBrushType.CHARCOAL] as? CharcoalBrushFactory
                factory?.applyPaperTexture(paint, intensity)
            }
            else -> {
                // No special effects for other brush types
            }
        }
    }
    
    private fun applyPressureToPaint(paint: Paint, pressure: Float) {
        val factory = brushFactories[currentAdvancedTool]
        factory?.applyPressure(paint, pressure, pressureSettings)
    }
    
    companion object {
        // Singleton instance
        private var instance: AdvancedBrushManager? = null
        
        @JvmStatic
        fun getInstance(context: Context): AdvancedBrushManager {
            if (instance == null) {
                instance = AdvancedBrushManager(context.applicationContext)
            }
            return instance!!
        }
    }
}