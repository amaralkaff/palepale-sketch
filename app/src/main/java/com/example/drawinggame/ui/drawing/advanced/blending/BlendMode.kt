package com.example.drawinggame.ui.drawing.advanced.blending

import android.graphics.BlendMode as AndroidBlendMode
import android.graphics.PorterDuff

/**
 * Advanced blend modes for professional compositing
 */
enum class BlendMode {
    NORMAL,
    MULTIPLY,
    SCREEN,
    OVERLAY,
    SOFT_LIGHT,
    HARD_LIGHT,
    COLOR_DODGE,
    COLOR_BURN,
    DARKEN,
    LIGHTEN;
    
    /**
     * Convert to Android BlendMode (API 29+)
     */
    fun toAndroidBlendMode(): AndroidBlendMode? {
        return when (this) {
            NORMAL -> AndroidBlendMode.SRC_OVER
            MULTIPLY -> AndroidBlendMode.MULTIPLY
            SCREEN -> AndroidBlendMode.SCREEN
            OVERLAY -> AndroidBlendMode.OVERLAY
            SOFT_LIGHT -> AndroidBlendMode.SOFT_LIGHT
            HARD_LIGHT -> AndroidBlendMode.HARD_LIGHT
            COLOR_DODGE -> AndroidBlendMode.COLOR_DODGE
            COLOR_BURN -> AndroidBlendMode.COLOR_BURN
            DARKEN -> AndroidBlendMode.DARKEN
            LIGHTEN -> AndroidBlendMode.LIGHTEN
        }
    }
    
    /**
     * Convert to PorterDuff mode for compatibility with older APIs
     */
    fun toPorterDuffMode(): PorterDuff.Mode {
        return when (this) {
            NORMAL -> PorterDuff.Mode.SRC_OVER
            MULTIPLY -> PorterDuff.Mode.MULTIPLY
            SCREEN -> PorterDuff.Mode.SCREEN
            OVERLAY -> PorterDuff.Mode.OVERLAY
            DARKEN -> PorterDuff.Mode.DARKEN
            LIGHTEN -> PorterDuff.Mode.LIGHTEN
            // For modes not available in PorterDuff, fall back to SRC_OVER
            else -> PorterDuff.Mode.SRC_OVER
        }
    }
}