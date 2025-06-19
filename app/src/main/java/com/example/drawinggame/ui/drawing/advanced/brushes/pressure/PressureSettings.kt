package com.example.drawinggame.ui.drawing.advanced.brushes.pressure

/**
 * Configuration for pressure sensitivity settings
 */
data class PressureSettings(
    val sizeEnabled: Boolean = true,
    val sizeMin: Float = 0.1f,
    val sizeMax: Float = 1.0f,
    val opacityEnabled: Boolean = false,
    val opacityMin: Float = 0.1f,
    val opacityMax: Float = 1.0f,
    val pressureCurve: PressureCurve = PressureCurve.LINEAR
)

/**
 * Pressure response curve types
 */
enum class PressureCurve {
    LINEAR, 
    EXPONENTIAL, 
    LOGARITHMIC, 
    CUSTOM
}

/**
 * Represents a pressure point with coordinates and pressure value
 */
data class PressurePoint(
    val x: Float,
    val y: Float,
    val pressure: Float,
    val timestamp: Long
)