package com.example.drawinggame.ui.drawing.color.adjustments

import android.graphics.*
import kotlin.math.*

/**
 * Curves adjustment layer implementation
 * Provides precise tonal control through customizable curves for RGB and individual channels
 */
class CurvesAdjustment : AdjustmentLayer() {
    
    override val type = AdjustmentType.CURVES
    override val name = "Curves"
    override val description = "Precise tonal curve adjustments for each color channel"
    
    companion object {
        const val CURVE_POINTS = 256 // Number of points in the curve (0-255)
        const val MAX_CONTROL_POINTS = 16 // Maximum number of user-defined control points
    }
    
    // Curve data for each channel
    private val masterCurve = CurveData("Master")
    private val redCurve = CurveData("Red")
    private val greenCurve = CurveData("Green")
    private val blueCurve = CurveData("Blue")
    
    // Lookup tables for fast processing
    private val masterLUT = IntArray(256)
    private val redLUT = IntArray(256)
    private val greenLUT = IntArray(256)
    private val blueLUT = IntArray(256)
    
    // Current active channel
    var activeChannel: CurveChannel = CurveChannel.MASTER
    
    init {
        // Initialize with linear curves
        resetAllCurves()
        updateLookupTables()
    }
    
    override fun apply(source: Bitmap): Bitmap {
        if (!isEnabled || !hasEffect()) {
            return source.copy(source.config ?: Bitmap.Config.ARGB_8888, false)
        }
        
        // Update lookup tables if curves have changed
        updateLookupTables()
        
        return applyPixelProcessing(source) { pixel ->
            val a = Color.alpha(pixel)
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)
            
            // Apply master curve first
            var newR = masterLUT[r]
            var newG = masterLUT[g]
            var newB = masterLUT[b]
            
            // Apply individual channel curves
            newR = redLUT[newR]
            newG = greenLUT[newG]
            newB = blueLUT[newB]
            
            Color.argb(a, newR, newG, newB)
        }
    }
    
    override fun copy(): AdjustmentLayer {
        val copy = CurvesAdjustment()
        copy.isEnabled = this.isEnabled
        copy.opacity = this.opacity
        copy.blendMode = this.blendMode
        
        // Copy curve data
        copy.masterCurve.copyFrom(this.masterCurve)
        copy.redCurve.copyFrom(this.redCurve)
        copy.greenCurve.copyFrom(this.greenCurve)
        copy.blueCurve.copyFrom(this.blueCurve)
        copy.updateLookupTables()
        
        return copy
    }
    
    override fun hasEffect(): Boolean {
        return !masterCurve.isLinear() || !redCurve.isLinear() || 
               !greenCurve.isLinear() || !blueCurve.isLinear()
    }
    
    /**
     * Get curve data for specified channel
     */
    fun getCurve(channel: CurveChannel): CurveData {
        return when (channel) {
            CurveChannel.MASTER -> masterCurve
            CurveChannel.RED -> redCurve
            CurveChannel.GREEN -> greenCurve
            CurveChannel.BLUE -> blueCurve
        }
    }
    
    /**
     * Add control point to specified channel
     */
    fun addControlPoint(channel: CurveChannel, x: Float, y: Float): Boolean {
        val curve = getCurve(channel)
        val added = curve.addControlPoint(x, y)
        if (added) {
            updateLookupTables()
            notifyParametersChanged()
        }
        return added
    }
    
    /**
     * Remove control point from specified channel
     */
    fun removeControlPoint(channel: CurveChannel, index: Int): Boolean {
        val curve = getCurve(channel)
        val removed = curve.removeControlPoint(index)
        if (removed) {
            updateLookupTables()
            notifyParametersChanged()
        }
        return removed
    }
    
    /**
     * Move control point in specified channel
     */
    fun moveControlPoint(channel: CurveChannel, index: Int, x: Float, y: Float): Boolean {
        val curve = getCurve(channel)
        val moved = curve.moveControlPoint(index, x, y)
        if (moved) {
            updateLookupTables()
            notifyParametersChanged()
        }
        return moved
    }
    
    /**
     * Reset specified channel to linear curve
     */
    fun resetChannel(channel: CurveChannel) {
        getCurve(channel).reset()
        updateLookupTables()
        notifyParametersChanged()
    }
    
    /**
     * Reset all channels to linear curves
     */
    fun resetAllCurves() {
        masterCurve.reset()
        redCurve.reset()
        greenCurve.reset()
        blueCurve.reset()
        updateLookupTables()
        notifyParametersChanged()
    }
    
    /**
     * Apply preset curve adjustment
     */
    fun applyPreset(preset: CurvePreset) {
        when (preset) {
            CurvePreset.INCREASE_CONTRAST -> applyIncreaseContrastPreset()
            CurvePreset.DECREASE_CONTRAST -> applyDecreaseContrastPreset()
            CurvePreset.LIGHTEN -> applyLightenPreset()
            CurvePreset.DARKEN -> applyDarkenPreset()
            CurvePreset.S_CURVE -> applySCurvePreset()
            CurvePreset.INVERSE_S_CURVE -> applyInverseSCurvePreset()
            CurvePreset.HIGHLIGHT_RECOVERY -> applyHighlightRecoveryPreset()
            CurvePreset.SHADOW_RECOVERY -> applyShadowRecoveryPreset()
        }
        updateLookupTables()
        notifyParametersChanged()
    }
    
    /**
     * Get curve output value for input (0-255 range)
     */
    fun getCurveValue(channel: CurveChannel, input: Int): Int {
        return when (channel) {
            CurveChannel.MASTER -> masterLUT[input.coerceIn(0, 255)]
            CurveChannel.RED -> redLUT[input.coerceIn(0, 255)]
            CurveChannel.GREEN -> greenLUT[input.coerceIn(0, 255)]
            CurveChannel.BLUE -> blueLUT[input.coerceIn(0, 255)]
        }
    }
    
    /**
     * Import curve data from external source
     */
    fun importCurveData(channel: CurveChannel, curveData: FloatArray): Boolean {
        if (curveData.size != CURVE_POINTS) return false
        
        val curve = getCurve(channel)
        curve.importCurveData(curveData)
        updateLookupTables()
        notifyParametersChanged()
        return true
    }
    
    /**
     * Export curve data for external use
     */
    fun exportCurveData(channel: CurveChannel): FloatArray {
        return getCurve(channel).exportCurveData()
    }
    
    /**
     * Update lookup tables from curve data
     */
    private fun updateLookupTables() {
        masterCurve.updateLookupTable(masterLUT)
        redCurve.updateLookupTable(redLUT)
        greenCurve.updateLookupTable(greenLUT)
        blueCurve.updateLookupTable(blueLUT)
    }
    
    // Preset implementations
    
    private fun applyIncreaseContrastPreset() {
        masterCurve.reset()
        masterCurve.addControlPoint(0.25f, 0.15f)
        masterCurve.addControlPoint(0.75f, 0.85f)
    }
    
    private fun applyDecreaseContrastPreset() {
        masterCurve.reset()
        masterCurve.addControlPoint(0.25f, 0.35f)
        masterCurve.addControlPoint(0.75f, 0.65f)
    }
    
    private fun applyLightenPreset() {
        masterCurve.reset()
        masterCurve.addControlPoint(0.5f, 0.6f)
    }
    
    private fun applyDarkenPreset() {
        masterCurve.reset()
        masterCurve.addControlPoint(0.5f, 0.4f)
    }
    
    private fun applySCurvePreset() {
        masterCurve.reset()
        masterCurve.addControlPoint(0.25f, 0.2f)
        masterCurve.addControlPoint(0.75f, 0.8f)
    }
    
    private fun applyInverseSCurvePreset() {
        masterCurve.reset()
        masterCurve.addControlPoint(0.25f, 0.3f)
        masterCurve.addControlPoint(0.75f, 0.7f)
    }
    
    private fun applyHighlightRecoveryPreset() {
        masterCurve.reset()
        masterCurve.addControlPoint(0.75f, 0.65f)
        masterCurve.addControlPoint(0.9f, 0.8f)
    }
    
    private fun applyShadowRecoveryPreset() {
        masterCurve.reset()
        masterCurve.addControlPoint(0.1f, 0.2f)
        masterCurve.addControlPoint(0.25f, 0.35f)
    }
}

/**
 * Curve data container for a single channel
 */
class CurveData(val name: String) {
    
    private val controlPoints = mutableListOf<PointF>()
    private val curveValues = FloatArray(CurvesAdjustment.CURVE_POINTS)
    
    init {
        reset()
    }
    
    /**
     * Reset to linear curve
     */
    fun reset() {
        controlPoints.clear()
        controlPoints.add(PointF(0f, 0f))
        controlPoints.add(PointF(1f, 1f))
        updateCurveValues()
    }
    
    /**
     * Add control point (normalized 0-1 coordinates)
     */
    fun addControlPoint(x: Float, y: Float): Boolean {
        if (controlPoints.size >= CurvesAdjustment.MAX_CONTROL_POINTS) return false
        
        val clampedX = x.coerceIn(0f, 1f)
        val clampedY = y.coerceIn(0f, 1f)
        
        // Find insertion position to maintain sorted order
        var insertIndex = controlPoints.size
        for (i in controlPoints.indices) {
            if (controlPoints[i].x > clampedX) {
                insertIndex = i
                break
            }
        }
        
        controlPoints.add(insertIndex, PointF(clampedX, clampedY))
        updateCurveValues()
        return true
    }
    
    /**
     * Remove control point by index
     */
    fun removeControlPoint(index: Int): Boolean {
        if (index < 0 || index >= controlPoints.size || controlPoints.size <= 2) return false
        
        controlPoints.removeAt(index)
        updateCurveValues()
        return true
    }
    
    /**
     * Move control point to new position
     */
    fun moveControlPoint(index: Int, x: Float, y: Float): Boolean {
        if (index < 0 || index >= controlPoints.size) return false
        
        val clampedX = x.coerceIn(0f, 1f)
        val clampedY = y.coerceIn(0f, 1f)
        
        // Don't allow moving first and last points horizontally
        val finalX = if (index == 0 || index == controlPoints.size - 1) {
            controlPoints[index].x
        } else {
            clampedX
        }
        
        controlPoints[index] = PointF(finalX, clampedY)
        
        // Re-sort if necessary (except for first and last points)
        if (index > 0 && index < controlPoints.size - 1) {
            controlPoints.sortBy { it.x }
        }
        
        updateCurveValues()
        return true
    }
    
    /**
     * Get control points list
     */
    fun getControlPoints(): List<PointF> {
        return controlPoints.toList()
    }
    
    /**
     * Check if curve is linear (no adjustment)
     */
    fun isLinear(): Boolean {
        if (controlPoints.size != 2) return false
        val first = controlPoints[0]
        val last = controlPoints[1]
        return abs(first.x - 0f) < 0.001f && abs(first.y - 0f) < 0.001f &&
               abs(last.x - 1f) < 0.001f && abs(last.y - 1f) < 0.001f
    }
    
    /**
     * Update curve values using spline interpolation
     */
    private fun updateCurveValues() {
        for (i in curveValues.indices) {
            val x = i / 255f
            curveValues[i] = interpolateSpline(x)
        }
    }
    
    /**
     * Catmull-Rom spline interpolation
     */
    private fun interpolateSpline(x: Float): Float {
        if (controlPoints.size < 2) return x
        
        // Find the segment
        var segmentIndex = 0
        for (i in 0 until controlPoints.size - 1) {
            if (x >= controlPoints[i].x && x <= controlPoints[i + 1].x) {
                segmentIndex = i
                break
            }
        }
        
        // Linear interpolation for now (can be enhanced with cubic splines)
        val p1 = controlPoints[segmentIndex]
        val p2 = controlPoints[segmentIndex + 1]
        
        val t = if (p2.x - p1.x > 0f) {
            (x - p1.x) / (p2.x - p1.x)
        } else {
            0f
        }
        
        return (p1.y + t * (p2.y - p1.y)).coerceIn(0f, 1f)
    }
    
    /**
     * Update lookup table from curve values
     */
    fun updateLookupTable(lut: IntArray) {
        for (i in lut.indices) {
            val output = curveValues[i] * 255f
            lut[i] = output.roundToInt().coerceIn(0, 255)
        }
    }
    
    /**
     * Copy curve data from another curve
     */
    fun copyFrom(other: CurveData) {
        controlPoints.clear()
        controlPoints.addAll(other.controlPoints.map { PointF(it.x, it.y) })
        updateCurveValues()
    }
    
    /**
     * Import curve data from float array
     */
    fun importCurveData(data: FloatArray) {
        if (data.size == curveValues.size) {
            System.arraycopy(data, 0, curveValues, 0, data.size)
        }
    }
    
    /**
     * Export curve data as float array
     */
    fun exportCurveData(): FloatArray {
        return curveValues.clone()
    }
}

/**
 * Curve channels
 */
enum class CurveChannel(val displayName: String, val color: Int) {
    MASTER("RGB", Color.GRAY),
    RED("Red", Color.RED),
    GREEN("Green", Color.GREEN),
    BLUE("Blue", Color.BLUE)
}

/**
 * Curve presets
 */
enum class CurvePreset(val displayName: String, val description: String) {
    INCREASE_CONTRAST("Increase Contrast", "Enhance image contrast"),
    DECREASE_CONTRAST("Decrease Contrast", "Reduce image contrast"),
    LIGHTEN("Lighten", "Brighten the overall image"),
    DARKEN("Darken", "Darken the overall image"),
    S_CURVE("S-Curve", "Classic S-curve for enhanced contrast"),
    INVERSE_S_CURVE("Inverse S-Curve", "Inverse S-curve for softer look"),
    HIGHLIGHT_RECOVERY("Highlight Recovery", "Recover blown highlights"),
    SHADOW_RECOVERY("Shadow Recovery", "Lift shadow details")
}