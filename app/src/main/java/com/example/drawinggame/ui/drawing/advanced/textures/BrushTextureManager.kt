package com.example.drawinggame.ui.drawing.advanced.textures

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.LruCache

/**
 * Manages brush textures and their application to strokes
 * Handles loading, caching, and efficient texture operations
 */
class BrushTextureManager(private val context: Context) {
    
    // LRU cache for texture bitmaps
    private val textureCache = LruCache<String, Bitmap>(50)
    
    // Default built-in textures mapped to resource names
    private val defaultTextures = mapOf(
        TextureType.PAPER_SMOOTH to "texture_paper_smooth",
        TextureType.PAPER_ROUGH to "texture_paper_rough",
        TextureType.CANVAS_FINE to "texture_canvas_fine",
        TextureType.CANVAS_COARSE to "texture_canvas_coarse",
        TextureType.WATERCOLOR_PAPER to "texture_watercolor_paper",
        TextureType.CHARCOAL_PAPER to "texture_charcoal_paper"
    )
    
    /**
     * Load a texture by ID or type
     */
    fun loadTexture(textureId: String): BrushTexture? {
        return try {
            // Check cache first
            val cachedBitmap = textureCache.get(textureId)
            if (cachedBitmap != null) {
                return createTextureFromBitmap(textureId, cachedBitmap)
            }
            
            // Try to load from assets or resources
            val bitmap = loadTextureFromAssets(textureId) ?: createDefaultTexture(textureId)
            bitmap?.let {
                textureCache.put(textureId, it)
                createTextureFromBitmap(textureId, it)
            }
        } catch (e: Exception) {
            // Return null if texture loading fails
            null
        }
    }
    
    /**
     * Load texture by type
     */
    fun loadTexture(type: TextureType): BrushTexture? {
        val textureId = defaultTextures[type] ?: return null
        return loadTexture(textureId)
    }
    
    /**
     * Apply texture to a Paint object using shader
     */
    fun applyTextureToPath(path: Path, paint: Paint, texture: BrushTexture) {
        val shader = BitmapShader(
            texture.bitmap,
            Shader.TileMode.REPEAT,
            Shader.TileMode.REPEAT
        )
        
        // Apply texture transformations
        val matrix = android.graphics.Matrix()
        matrix.setScale(texture.scale, texture.scale)
        matrix.postRotate(texture.rotation)
        shader.setLocalMatrix(matrix)
        
        // Apply shader to paint
        paint.shader = shader
        
        // Adjust paint alpha based on texture intensity
        paint.alpha = (paint.alpha * texture.intensity).toInt().coerceIn(0, 255)
    }
    
    /**
     * Create a new Paint object with texture applied
     */
    fun createTexturedPaint(basePaint: Paint, texture: BrushTexture): Paint {
        val texturedPaint = Paint(basePaint)
        
        val shader = BitmapShader(
            texture.bitmap,
            Shader.TileMode.REPEAT,
            Shader.TileMode.REPEAT
        )
        
        // Apply texture transformations
        val matrix = android.graphics.Matrix()
        matrix.setScale(texture.scale, texture.scale)
        matrix.postRotate(texture.rotation)
        shader.setLocalMatrix(matrix)
        
        texturedPaint.shader = shader
        texturedPaint.alpha = (texturedPaint.alpha * texture.intensity).toInt().coerceIn(0, 255)
        
        return texturedPaint
    }
    
    /**
     * Get all available built-in textures
     */
    fun getAvailableTextures(): List<BrushTexture> {
        return TextureType.values().mapNotNull { type ->
            if (type != TextureType.CUSTOM) {
                loadTexture(type)
            } else null
        }
    }
    
    /**
     * Create a custom texture from user-provided bitmap
     */
    fun createCustomTexture(
        id: String,
        name: String,
        bitmap: Bitmap,
        scale: Float = 1.0f,
        rotation: Float = 0.0f,
        intensity: Float = 1.0f
    ): BrushTexture {
        // Cache the custom texture
        textureCache.put(id, bitmap)
        
        return BrushTexture(
            id = id,
            name = name,
            bitmap = bitmap,
            type = TextureType.CUSTOM,
            scale = scale,
            rotation = rotation,
            intensity = intensity
        )
    }
    
    /**
     * Clear texture cache to free memory
     */
    fun clearCache() {
        textureCache.evictAll()
    }
    
    /**
     * Get cache memory usage information
     */
    fun getCacheInfo(): String {
        val hitCount = textureCache.hitCount()
        val missCount = textureCache.missCount()
        val size = textureCache.size()
        val maxSize = textureCache.maxSize()
        
        return "Cache: $size/$maxSize, Hits: $hitCount, Misses: $missCount"
    }
    
    private fun loadTextureFromAssets(textureId: String): Bitmap? {
        return try {
            // Try to load from assets folder
            val inputStream = context.assets.open("textures/$textureId.png")
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            try {
                // Try alternative formats or locations
                val inputStream = context.assets.open("textures/$textureId.jpg")
                BitmapFactory.decodeStream(inputStream)
            } catch (e2: Exception) {
                null
            }
        }
    }
    
    private fun createDefaultTexture(textureId: String): Bitmap? {
        // Create a simple procedural texture as fallback
        return try {
            val size = 64
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            
            // Create a simple noise pattern
            val pixels = IntArray(size * size)
            for (i in pixels.indices) {
                val noise = (Math.random() * 255).toInt()
                pixels[i] = android.graphics.Color.argb(128, noise, noise, noise)
            }
            
            bitmap.setPixels(pixels, 0, size, 0, 0, size, size)
            bitmap
        } catch (e: Exception) {
            null
        }
    }
    
    private fun createTextureFromBitmap(id: String, bitmap: Bitmap): BrushTexture {
        // Determine texture type from ID
        val type = defaultTextures.entries.find { it.value == id }?.key 
            ?: TextureType.CUSTOM
        
        return BrushTexture(
            id = id,
            name = id.replace("_", " ").uppercase(),
            bitmap = bitmap,
            type = type
        )
    }
}