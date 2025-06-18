package com.example.drawinggame.ui.drawing.utils

import android.graphics.Path
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.view.Choreographer
import java.util.concurrent.TimeUnit

/**
 * Utilities for optimizing drawing performance.
 * Part of Phase 4.1 Core Drawing Architecture implementation.
 */
object PerformanceUtils {
    private const val TAG = "DrawingPerformance"
    private const val FRAME_TIME_THRESHOLD_MS = 16 // Target 60fps (16ms per frame)
    private const val PATH_OPTIMIZATION_THRESHOLD = 100 // Points after which to optimize path
    
    // FPS tracking
    private var frameCount = 0
    private var lastFpsLogTime = 0L
    private const val FPS_LOG_INTERVAL_MS = 5000L // Log FPS every 5 seconds
    
    /**
     * Track frame rate for drawing operations
     */
    fun trackFrameRate() {
        frameCount++
        val currentTime = SystemClock.elapsedRealtime()
        
        if (currentTime - lastFpsLogTime > FPS_LOG_INTERVAL_MS) {
            val fps = frameCount * 1000 / (currentTime - lastFpsLogTime)
            Log.d(TAG, "Drawing FPS: $fps")
            
            frameCount = 0
            lastFpsLogTime = currentTime
        }
    }
    
    /**
     * Optimize a complex path by reducing the number of points
     * while maintaining visual quality
     */
    fun optimizePath(path: Path): Path {
        // Path optimization is only needed for complex paths
        // This is a placeholder for a more complex path optimization algorithm
        // that would be implemented in a production app
        
        // For now, we just return the original path
        return path
    }
    
    /**
     * Check if we should optimize based on available memory
     */
    fun shouldOptimizeMemory(): Boolean {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val usedPercent = usedMemory.toDouble() / maxMemory * 100
        
        // If using more than 80% of available memory, optimize
        return usedPercent > 80
    }
    
    /**
     * Track frame callback for performance monitoring
     */
    fun scheduleFrameCallback(callback: (Long) -> Unit) {
        Choreographer.getInstance().postFrameCallback { frameTimeNanos ->
            val frameTimeMs = TimeUnit.NANOSECONDS.toMillis(frameTimeNanos)
            callback(frameTimeMs)
        }
    }
    
    /**
     * Log a slow drawing operation
     */
    fun logSlowDrawing(operation: String, durationMs: Long) {
        if (durationMs > FRAME_TIME_THRESHOLD_MS) {
            Log.w(TAG, "Slow drawing operation: $operation took $durationMs ms")
        }
    }
    
    /**
     * Check if hardware acceleration is available and enabled
     */
    fun isHardwareAccelerated(view: android.view.View): Boolean {
        return view.isHardwareAccelerated
    }
} 