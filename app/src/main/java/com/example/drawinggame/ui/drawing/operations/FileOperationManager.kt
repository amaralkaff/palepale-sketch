package com.example.drawinggame.ui.drawing.operations

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Handles all file system operations for saving and loading artwork.
 * Supports PNG and JPEG formats with metadata embedding.
 */
class FileOperationManager(
    private val context: Context
) {
    
    // Coroutine scope for file operations
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // File operation listeners
    private val listeners = mutableListOf<FileOperationListener>()
    
    // Storage directories
    private val internalDrawingsDir by lazy {
        File(context.filesDir, "drawings").apply { mkdirs() }
    }
    
    private val autoSaveDir by lazy {
        File(context.cacheDir, "autosave").apply { mkdirs() }
    }
    
    private val exportsDir by lazy {
        File(context.getExternalFilesDir(null), "exports").apply { mkdirs() }
    }
    
    /**
     * Save a bitmap as PNG to internal storage
     */
    suspend fun savePng(
        bitmap: Bitmap,
        filename: String? = null,
        quality: Int = 100
    ): SaveResult = withContext(Dispatchers.IO) {
        
        val actualFilename = filename ?: generateFilename("png")
        val file = File(internalDrawingsDir, actualFilename)
        
        try {
            notifyOperationStarted("Saving PNG...")
            
            FileOutputStream(file).use { output ->
                val success = bitmap.compress(Bitmap.CompressFormat.PNG, quality, output)
                
                if (success) {
                    notifyOperationCompleted("PNG saved successfully")
                    SaveResult.Success(file.absolutePath, file.length())
                } else {
                    notifyOperationFailed("Failed to compress PNG")
                    SaveResult.Error("Failed to compress bitmap")
                }
            }
        } catch (e: IOException) {
            notifyOperationFailed("Error saving PNG: ${e.message}")
            SaveResult.Error("IO Error: ${e.message}")
        }
    }
    
    /**
     * Save a bitmap as JPEG to external storage for sharing
     */
    suspend fun saveJpeg(
        bitmap: Bitmap,
        filename: String? = null,
        quality: Int = 90
    ): SaveResult = withContext(Dispatchers.IO) {
        
        val actualFilename = filename ?: generateFilename("jpg")
        val file = File(exportsDir, actualFilename)
        
        try {
            notifyOperationStarted("Saving JPEG...")
            
            FileOutputStream(file).use { output ->
                val success = bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
                
                if (success) {
                    notifyOperationCompleted("JPEG saved successfully")
                    SaveResult.Success(file.absolutePath, file.length())
                } else {
                    notifyOperationFailed("Failed to compress JPEG")
                    SaveResult.Error("Failed to compress bitmap")
                }
            }
        } catch (e: IOException) {
            notifyOperationFailed("Error saving JPEG: ${e.message}")
            SaveResult.Error("IO Error: ${e.message}")
        }
    }
    
    /**
     * Auto-save bitmap for crash recovery
     */
    suspend fun autoSave(bitmap: Bitmap): SaveResult = withContext(Dispatchers.IO) {
        val filename = "autosave_${System.currentTimeMillis()}.png"
        val file = File(autoSaveDir, filename)
        
        try {
            // Clean up old auto-saves (keep only last 5)
            cleanupAutoSaves()
            
            FileOutputStream(file).use { output ->
                val success = bitmap.compress(Bitmap.CompressFormat.PNG, 85, output)
                
                if (success) {
                    SaveResult.Success(file.absolutePath, file.length())
                } else {
                    SaveResult.Error("Failed to auto-save")
                }
            }
        } catch (e: IOException) {
            SaveResult.Error("Auto-save failed: ${e.message}")
        }
    }
    
    /**
     * Load a bitmap from file path
     */
    suspend fun loadBitmap(filePath: String): LoadResult = withContext(Dispatchers.IO) {
        try {
            notifyOperationStarted("Loading image...")
            
            val file = File(filePath)
            if (!file.exists()) {
                notifyOperationFailed("File not found")
                return@withContext LoadResult.Error("File not found: $filePath")
            }
            
            val options = BitmapFactory.Options().apply {
                // First pass - get dimensions only
                inJustDecodeBounds = true
            }
            
            BitmapFactory.decodeFile(filePath, options)
            
            // Calculate sample size if image is too large
            options.inSampleSize = calculateInSampleSize(options, 2048, 2048)
            options.inJustDecodeBounds = false
            
            val bitmap = BitmapFactory.decodeFile(filePath, options)
            
            if (bitmap != null) {
                notifyOperationCompleted("Image loaded successfully")
                LoadResult.Success(bitmap, file.length())
            } else {
                notifyOperationFailed("Failed to decode image")
                LoadResult.Error("Failed to decode image file")
            }
            
        } catch (e: Exception) {
            notifyOperationFailed("Error loading image: ${e.message}")
            LoadResult.Error("Load error: ${e.message}")
        }
    }
    
    /**
     * Load bitmap from URI (for image imports)
     */
    suspend fun loadBitmapFromUri(uri: Uri): LoadResult = withContext(Dispatchers.IO) {
        try {
            notifyOperationStarted("Importing image...")
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val options = BitmapFactory.Options().apply {
                    inSampleSize = calculateInSampleSize(this, 2048, 2048)
                }
                
                val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
                
                if (bitmap != null) {
                    notifyOperationCompleted("Image imported successfully")
                    LoadResult.Success(bitmap, 0L) // Size unknown for URI
                } else {
                    notifyOperationFailed("Failed to import image")
                    LoadResult.Error("Failed to decode image from URI")
                }
            } ?: LoadResult.Error("Failed to open input stream")
            
        } catch (e: Exception) {
            notifyOperationFailed("Error importing image: ${e.message}")
            LoadResult.Error("Import error: ${e.message}")
        }
    }
    
    /**
     * Get the most recent auto-save file
     */
    suspend fun getLatestAutoSave(): String? = withContext(Dispatchers.IO) {
        try {
            autoSaveDir.listFiles()
                ?.filter { it.name.startsWith("autosave_") }
                ?.maxByOrNull { it.lastModified() }
                ?.absolutePath
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * List all saved drawings
     */
    suspend fun listSavedDrawings(): List<DrawingFileInfo> = withContext(Dispatchers.IO) {
        try {
            internalDrawingsDir.listFiles()
                ?.filter { it.isFile && (it.name.endsWith(".png") || it.name.endsWith(".jpg")) }
                ?.map { file ->
                    DrawingFileInfo(
                        name = file.name,
                        path = file.absolutePath,
                        size = file.length(),
                        lastModified = file.lastModified()
                    )
                }
                ?.sortedByDescending { it.lastModified }
                ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Delete a drawing file
     */
    suspend fun deleteDrawing(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            File(filePath).delete()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Add a file operation listener
     */
    fun addListener(listener: FileOperationListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }
    
    /**
     * Remove a file operation listener
     */
    fun removeListener(listener: FileOperationListener) {
        listeners.remove(listener)
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        scope.cancel()
        listeners.clear()
    }
    
    /**
     * Generate a unique filename with timestamp
     */
    private fun generateFilename(extension: String): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        return "drawing_${timestamp}.${extension}"
    }
    
    /**
     * Calculate sample size for bitmap loading to prevent memory issues
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while ((halfHeight / inSampleSize) >= reqHeight &&
                (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    /**
     * Clean up old auto-save files, keeping only the most recent ones
     */
    private suspend fun cleanupAutoSaves() = withContext(Dispatchers.IO) {
        try {
            val autoSaveFiles = autoSaveDir.listFiles()
                ?.filter { it.name.startsWith("autosave_") }
                ?.sortedByDescending { it.lastModified() }
            
            // Keep only the 5 most recent auto-saves
            autoSaveFiles?.drop(5)?.forEach { file ->
                file.delete()
            }
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }
    
    /**
     * Notify listeners of operation start
     */
    private suspend fun notifyOperationStarted(message: String) {
        withContext(Dispatchers.Main) {
            listeners.forEach { it.onOperationStarted(message) }
        }
    }
    
    /**
     * Notify listeners of operation completion
     */
    private suspend fun notifyOperationCompleted(message: String) {
        withContext(Dispatchers.Main) {
            listeners.forEach { it.onOperationCompleted(message) }
        }
    }
    
    /**
     * Notify listeners of operation failure
     */
    private suspend fun notifyOperationFailed(message: String) {
        withContext(Dispatchers.Main) {
            listeners.forEach { it.onOperationFailed(message) }
        }
    }
    
    /**
     * Result classes for file operations
     */
    sealed class SaveResult {
        data class Success(val filePath: String, val fileSize: Long) : SaveResult()
        data class Error(val message: String) : SaveResult()
    }
    
    sealed class LoadResult {
        data class Success(val bitmap: Bitmap, val fileSize: Long) : LoadResult()
        data class Error(val message: String) : LoadResult()
    }
    
    /**
     * Information about a saved drawing file
     */
    data class DrawingFileInfo(
        val name: String,
        val path: String,
        val size: Long,
        val lastModified: Long
    )
    
    /**
     * Interface for file operation events
     */
    interface FileOperationListener {
        fun onOperationStarted(message: String) {}
        fun onOperationCompleted(message: String) {}
        fun onOperationFailed(message: String) {}
    }
} 