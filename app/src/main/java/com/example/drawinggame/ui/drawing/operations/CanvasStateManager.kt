package com.example.drawinggame.ui.drawing.operations

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import com.example.drawinggame.ui.drawing.DrawingEngine
import com.example.drawinggame.ui.drawing.models.CanvasTransform
import com.example.drawinggame.ui.drawing.models.DrawingTool
import com.example.drawinggame.ui.drawing.operations.models.CanvasState
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Manages complete canvas state capture and restoration.
 * Handles incremental state capture and memory optimization.
 */
class CanvasStateManager(
    private val drawingEngine: DrawingEngine
) {
    
    // Coroutine scope for background operations
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Auto-save configuration
    private val autoSaveInterval = 120_000L // 2 minutes
    private val autoSaveHandler = Handler(Looper.getMainLooper())
    private var autoSaveRunnable: Runnable? = null
    private val isAutoSaveEnabled = AtomicBoolean(true)
    
    // State capture listeners
    private val listeners = mutableListOf<CanvasStateListener>()
    
    // Last saved state for comparison
    private var lastSavedState: CanvasState? = null
    
    init {
        startAutoSave()
    }
    
    /**
     * Capture the current complete canvas state
     */
    suspend fun captureState(): CanvasState? = withContext(Dispatchers.IO) {
        try {
            val canvasBitmap = drawingEngine.getBitmap()
            val canvasTransform = drawingEngine.getCanvasTransform()
            val currentTool = drawingEngine.getCurrentTool()
            
            canvasBitmap?.let { bitmap ->
                // Compress bitmap for storage
                val compressedBitmap = compressBitmap(bitmap)
                
                CanvasState(
                    canvasBitmap = compressedBitmap,
                    canvasTransform = canvasTransform,
                    currentTool = currentTool,
                    timestamp = System.currentTimeMillis(),
                    canvasWidth = bitmap.width,
                    canvasHeight = bitmap.height
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Restore a previously saved canvas state
     */
    suspend fun restoreState(state: CanvasState): Boolean = withContext(Dispatchers.Main) {
        try {
            // Restore canvas bitmap
            state.canvasBitmap?.let { bitmap ->
                drawingEngine.restoreCanvas(bitmap)
            }
            
            // Restore canvas transform
            drawingEngine.updateCanvasTransform(state.canvasTransform)
            
            // Restore tool selection
            drawingEngine.setTool(state.currentTool)
            
            // Update last saved state
            lastSavedState = state
            
            // Notify listeners
            notifyStateRestored(state)
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Auto-save the current state
     */
    fun autoSave() {
        if (!isAutoSaveEnabled.get()) return
        
        scope.launch {
            try {
                val currentState = captureState()
                currentState?.let { state ->
                    // Check if state has changed significantly
                    if (hasStateChangedSignificantly(state)) {
                        saveStateToTempFile(state)
                        lastSavedState = state
                        
                        withContext(Dispatchers.Main) {
                            notifyAutoSaveCompleted(state)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    notifyAutoSaveFailed(e)
                }
            }
        }
    }
    
    /**
     * Enable or disable auto-save
     */
    fun setAutoSaveEnabled(enabled: Boolean) {
        isAutoSaveEnabled.set(enabled)
        if (enabled) {
            startAutoSave()
        } else {
            stopAutoSave()
        }
    }
    
    /**
     * Get the last saved state
     */
    fun getLastSavedState(): CanvasState? = lastSavedState
    
    /**
     * Add a state listener
     */
    fun addListener(listener: CanvasStateListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }
    
    /**
     * Remove a state listener
     */
    fun removeListener(listener: CanvasStateListener) {
        listeners.remove(listener)
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        scope.cancel()
        stopAutoSave()
        listeners.clear()
    }
    
    /**
     * Start the auto-save timer
     */
    private fun startAutoSave() {
        stopAutoSave() // Stop any existing timer
        
        autoSaveRunnable = Runnable {
            autoSave()
            // Schedule next auto-save
            if (isAutoSaveEnabled.get()) {
                autoSaveHandler.postDelayed(autoSaveRunnable!!, autoSaveInterval)
            }
        }
        
        autoSaveHandler.postDelayed(autoSaveRunnable!!, autoSaveInterval)
    }
    
    /**
     * Stop the auto-save timer
     */
    private fun stopAutoSave() {
        autoSaveRunnable?.let { runnable ->
            autoSaveHandler.removeCallbacks(runnable)
            autoSaveRunnable = null
        }
    }
    
    /**
     * Compress a bitmap for efficient storage
     */
    private suspend fun compressBitmap(bitmap: Bitmap): ByteArray = withContext(Dispatchers.IO) {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
        outputStream.toByteArray()
    }
    
    /**
     * Check if the current state has changed significantly from the last saved state
     */
    private fun hasStateChangedSignificantly(currentState: CanvasState): Boolean {
        val lastState = lastSavedState ?: return true
        
        // Compare timestamps
        val timeDiff = currentState.timestamp - lastState.timestamp
        if (timeDiff < 30_000L) return false // Don't save if less than 30 seconds
        
        // Compare canvas dimensions
        if (currentState.canvasWidth != lastState.canvasWidth ||
            currentState.canvasHeight != lastState.canvasHeight) {
            return true
        }
        
        // Compare tool selection
        if (currentState.currentTool != lastState.currentTool) {
            return true
        }
        
        // Compare bitmap sizes (simple heuristic)
        val currentSize = currentState.canvasBitmap?.size ?: 0
        val lastSize = lastState.canvasBitmap?.size ?: 0
        val sizeDiff = kotlin.math.abs(currentSize - lastSize)
        
        // If bitmap size changed by more than 10%, consider it significant
        return sizeDiff > (lastSize * 0.1)
    }
    
    /**
     * Save state to temporary file for crash recovery
     */
    private suspend fun saveStateToTempFile(state: CanvasState) = withContext(Dispatchers.IO) {
        // Implementation would save to app's cache directory
        // This is a placeholder for the actual file I/O implementation
        // In a full implementation, this would serialize the state to a file
    }
    
    /**
     * Notify listeners of state restoration
     */
    private fun notifyStateRestored(state: CanvasState) {
        listeners.forEach { it.onStateRestored(state) }
    }
    
    /**
     * Notify listeners of auto-save completion
     */
    private fun notifyAutoSaveCompleted(state: CanvasState) {
        listeners.forEach { it.onAutoSaveCompleted(state) }
    }
    
    /**
     * Notify listeners of auto-save failure
     */
    private fun notifyAutoSaveFailed(error: Exception) {
        listeners.forEach { it.onAutoSaveFailed(error) }
    }
    
    /**
     * Interface for listening to canvas state events
     */
    interface CanvasStateListener {
        fun onStateRestored(state: CanvasState) {}
        fun onAutoSaveCompleted(state: CanvasState) {}
        fun onAutoSaveFailed(error: Exception) {}
    }
} 