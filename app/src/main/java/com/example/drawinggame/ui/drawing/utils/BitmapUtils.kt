package com.example.drawinggame.ui.drawing.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Utility functions for bitmap operations in the drawing system.
 */
object BitmapUtils {
    /**
     * Create a new bitmap with the specified dimensions and background color
     */
    fun createBitmap(width: Int, height: Int, backgroundColor: Int = Color.WHITE): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(backgroundColor)
        return bitmap
    }
    
    /**
     * Resize a bitmap to the specified dimensions
     */
    fun resizeBitmap(source: Bitmap, width: Int, height: Int): Bitmap {
        val widthRatio = width.toFloat() / source.width
        val heightRatio = height.toFloat() / source.height
        val matrix = Matrix()
        matrix.postScale(widthRatio, heightRatio)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }
    
    /**
     * Save a bitmap to a file
     */
    fun saveBitmapToFile(bitmap: Bitmap, file: File, format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG, quality: Int = 100): Boolean {
        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(format, quality, out)
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Copy a bitmap
     */
    fun copyBitmap(source: Bitmap): Bitmap {
        return source.copy(source.config ?: Bitmap.Config.ARGB_8888, true)
    }
    
    /**
     * Overlay one bitmap on top of another
     */
    fun overlayBitmaps(background: Bitmap, foreground: Bitmap, x: Int = 0, y: Int = 0): Bitmap {
        val result = copyBitmap(background)
        val canvas = Canvas(result)
        canvas.drawBitmap(foreground, x.toFloat(), y.toFloat(), null)
        return result
    }
    
    /**
     * Create a transparent bitmap with the specified dimensions
     */
    fun createTransparentBitmap(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT)
        return bitmap
    }
    
    /**
     * Recycle a bitmap safely
     */
    fun recycleBitmap(bitmap: Bitmap?) {
        if (bitmap != null && !bitmap.isRecycled) {
            bitmap.recycle()
        }
    }
} 