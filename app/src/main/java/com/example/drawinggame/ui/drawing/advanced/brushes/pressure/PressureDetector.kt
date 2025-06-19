package com.example.drawinggame.ui.drawing.advanced.brushes.pressure

import android.view.MotionEvent

/**
 * Detects and processes stylus pressure input for natural drawing experience
 */
class PressureDetector {
    
    private var pressureSupported = false
    private var maxPressure = 1.0f
    private var minPressure = 0.0f
    
    init {
        // Check if pressure sensitivity is supported
        detectPressureSupport()
    }
    
    /**
     * Extract pressure value from motion event
     */
    fun getPressure(motionEvent: MotionEvent): Float {
        return if (pressureSupported) {
            val rawPressure = motionEvent.pressure
            // Normalize pressure to 0.0-1.0 range
            normalizePressure(rawPressure)
        } else {
            // Default pressure when not supported
            0.5f
        }
    }
    
    /**
     * Check if pressure sensitivity is supported on this device
     */
    fun isSupported(): Boolean = pressureSupported
    
    /**
     * Calibrate pressure sensitivity based on user settings
     */
    fun calibratePressure(settings: PressureSettings) {
        // Adjust pressure range based on settings
        // This would typically involve user calibration data
    }
    
    /**
     * Apply pressure curve transformation
     */
    fun applyPressureCurve(pressure: Float, curve: PressureCurve): Float {
        return when (curve) {
            PressureCurve.LINEAR -> pressure
            PressureCurve.EXPONENTIAL -> pressure * pressure
            PressureCurve.LOGARITHMIC -> kotlin.math.sqrt(pressure)
            PressureCurve.CUSTOM -> pressure // TODO: Implement custom curve
        }
    }
    
    private fun detectPressureSupport() {
        // In a real implementation, this would check device capabilities
        // For now, assume pressure is supported
        pressureSupported = true
        maxPressure = 1.0f
        minPressure = 0.0f
    }
    
    private fun normalizePressure(rawPressure: Float): Float {
        return if (maxPressure > minPressure) {
            ((rawPressure - minPressure) / (maxPressure - minPressure)).coerceIn(0.0f, 1.0f)
        } else {
            rawPressure.coerceIn(0.0f, 1.0f)
        }
    }
}