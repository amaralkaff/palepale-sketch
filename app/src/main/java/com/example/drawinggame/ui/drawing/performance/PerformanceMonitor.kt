package com.example.drawinggame.ui.drawing.performance

import android.os.SystemClock
import android.util.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * Performance monitoring system for drawing operations
 * Phase 5: Testing & Performance implementation
 */
object PerformanceMonitor {
    
    private val TAG = "PerformanceMonitor"
    
    // Performance tracking data
    private val operationTimes = ConcurrentHashMap<String, MutableList<Long>>()
    private val memoryUsage = ConcurrentHashMap<String, Long>()
    private val activeOperations = ConcurrentHashMap<String, Long>()
    
    // Performance thresholds (in milliseconds)
    private const val LAYER_OPERATION_THRESHOLD = 100L
    private const val SELECTION_OPERATION_THRESHOLD = 50L
    private const val MAGIC_WAND_THRESHOLD = 200L
    private const val MEMORY_WARNING_THRESHOLD = 100 * 1024 * 1024L // 100MB
    
    /**
     * Start timing an operation
     */
    fun startOperation(operationName: String) {
        val startTime = SystemClock.elapsedRealtime()
        activeOperations[operationName] = startTime
        Log.d(TAG, "Started operation: $operationName")
    }
    
    /**
     * End timing an operation
     */
    fun endOperation(operationName: String): Long {
        val endTime = SystemClock.elapsedRealtime()
        val startTime = activeOperations.remove(operationName)
        
        if (startTime != null) {
            val duration = endTime - startTime
            
            // Store timing data
            operationTimes.getOrPut(operationName) { mutableListOf() }.add(duration)
            
            // Check performance thresholds
            checkPerformanceThreshold(operationName, duration)
            
            Log.d(TAG, "Completed operation: $operationName in ${duration}ms")
            return duration
        }
        
        Log.w(TAG, "Operation $operationName was not started or already ended")
        return 0L
    }
    
    /**
     * Record memory usage for an operation
     */
    fun recordMemoryUsage(operationName: String, memoryBytes: Long) {
        memoryUsage[operationName] = memoryBytes
        
        if (memoryBytes > MEMORY_WARNING_THRESHOLD) {
            Log.w(TAG, "High memory usage for $operationName: ${memoryBytes / 1024 / 1024}MB")
        }
    }
    
    /**
     * Get average time for an operation
     */
    fun getAverageTime(operationName: String): Double {
        val times = operationTimes[operationName] ?: return 0.0
        return if (times.isNotEmpty()) {
            times.average()
        } else {
            0.0
        }
    }
    
    /**
     * Get performance statistics
     */
    fun getPerformanceStats(): Map<String, PerformanceStats> {
        val stats = mutableMapOf<String, PerformanceStats>()
        
        operationTimes.forEach { (operation, times) ->
            if (times.isNotEmpty()) {
                stats[operation] = PerformanceStats(
                    operationName = operation,
                    totalExecutions = times.size,
                    averageTimeMs = times.average(),
                    minTimeMs = times.minOrNull()?.toDouble() ?: 0.0,
                    maxTimeMs = times.maxOrNull()?.toDouble() ?: 0.0,
                    memoryUsageBytes = memoryUsage[operation] ?: 0L
                )
            }
        }
        
        return stats
    }
    
    /**
     * Check if operation exceeded performance threshold
     */
    private fun checkPerformanceThreshold(operationName: String, duration: Long) {
        val threshold = when {
            operationName.contains("layer", ignoreCase = true) -> LAYER_OPERATION_THRESHOLD
            operationName.contains("selection", ignoreCase = true) -> SELECTION_OPERATION_THRESHOLD
            operationName.contains("magic", ignoreCase = true) -> MAGIC_WAND_THRESHOLD
            else -> 500L // Default threshold
        }
        
        if (duration > threshold) {
            Log.w(TAG, "Performance warning: $operationName took ${duration}ms (threshold: ${threshold}ms)")
            generateOptimizationRecommendation(operationName, duration)
        }
    }
    
    /**
     * Generate optimization recommendations
     */
    private fun generateOptimizationRecommendation(operationName: String, duration: Long) {
        val recommendation = when {
            operationName.contains("layer") -> {
                "Consider reducing layer resolution or using layer caching"
            }
            operationName.contains("selection") -> {
                "Consider using simplified selection algorithms for large areas"
            }
            operationName.contains("magic") -> {
                "Consider reducing tolerance or using progressive magic wand"
            }
            else -> {
                "Consider optimizing algorithm or reducing data size"
            }
        }
        
        Log.i(TAG, "Optimization recommendation for $operationName: $recommendation")
    }
    
    /**
     * Clear all performance data
     */
    fun clearStats() {
        operationTimes.clear()
        memoryUsage.clear()
        activeOperations.clear()
        Log.d(TAG, "Performance statistics cleared")
    }
    
    /**
     * Log current performance summary
     */
    fun logPerformanceSummary() {
        Log.i(TAG, "=== Performance Summary ===")
        getPerformanceStats().forEach { (operation, stats) ->
            Log.i(TAG, "$operation: avg=${String.format("%.2f", stats.averageTimeMs)}ms, " +
                    "executions=${stats.totalExecutions}, " +
                    "memory=${stats.memoryUsageBytes / 1024}KB")
        }
        Log.i(TAG, "=========================")
    }
    
    /**
     * Enable/disable performance monitoring
     */
    fun setEnabled(enabled: Boolean) {
        if (!enabled) {
            clearStats()
        }
        Log.d(TAG, "Performance monitoring ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Measure operation execution time
     */
    fun measureOperation(operationName: String, operation: () -> Unit): Long {
        startOperation(operationName)
        operation()
        return endOperation(operationName)
    }
    
    /**
     * Get all performance statistics
     */
    fun getAllStats(): Map<String, PerformanceStats> {
        return getPerformanceStats()
    }
    
    /**
     * Get optimization recommendations based on current performance
     */
    fun getOptimizationRecommendations(): List<String> {
        val recommendations = mutableListOf<String>()
        
        getPerformanceStats().forEach { (operation, stats) ->
            when {
                stats.averageTimeMs > LAYER_OPERATION_THRESHOLD && operation.contains("layer") -> {
                    recommendations.add("Optimize layer operations: Consider layer caching or reduced resolution")
                }
                stats.averageTimeMs > SELECTION_OPERATION_THRESHOLD && operation.contains("selection") -> {
                    recommendations.add("Optimize selection tools: Use simplified algorithms for large areas")
                }
                stats.averageTimeMs > MAGIC_WAND_THRESHOLD && operation.contains("magic") -> {
                    recommendations.add("Optimize magic wand: Reduce tolerance or use progressive selection")
                }
                stats.memoryUsageBytes > MEMORY_WARNING_THRESHOLD -> {
                    recommendations.add("Reduce memory usage for $operation: ${stats.memoryUsageBytes / 1024 / 1024}MB")
                }
            }
        }
        
        return recommendations
    }
}

/**
 * Performance statistics data class
 */
data class PerformanceStats(
    val operationName: String,
    val totalExecutions: Int,
    val averageTimeMs: Double,
    val minTimeMs: Double,
    val maxTimeMs: Double,
    val memoryUsageBytes: Long
) 