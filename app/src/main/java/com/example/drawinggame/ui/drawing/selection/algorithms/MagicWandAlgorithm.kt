package com.example.drawinggame.ui.drawing.selection.algorithms

import android.graphics.*
import java.util.*
import kotlin.math.abs

/**
 * Magic Wand (Flood Fill) selection algorithm
 * Phase 5.3: Selection & Transformation Tools - Advanced Features
 */
class MagicWandAlgorithm {
    
    data class Point(val x: Int, val y: Int)
    
    /**
     * Perform magic wand selection using flood fill algorithm
     * @param bitmap Source bitmap to select from
     * @param startX Starting X coordinate
     * @param startY Starting Y coordinate
     * @param tolerance Color tolerance (0-255)
     * @param contiguous Whether to select only contiguous pixels
     * @return Path representing the selection
     */
    fun performSelection(
        bitmap: Bitmap,
        startX: Int,
        startY: Int,
        tolerance: Int = 32,
        contiguous: Boolean = true
    ): Path {
        val width = bitmap.width
        val height = bitmap.height
        
        // Validate coordinates
        if (startX < 0 || startX >= width || startY < 0 || startY >= height) {
            return Path()
        }
        
        // Get pixel data
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        // Get target color
        val targetColor = pixels[startY * width + startX]
        
        // Create selection mask
        val selectionMask = BooleanArray(width * height)
        
        if (contiguous) {
            floodFillContiguous(pixels, selectionMask, width, height, startX, startY, targetColor, tolerance)
        } else {
            floodFillGlobal(pixels, selectionMask, width, height, targetColor, tolerance)
        }
        
        // Convert selection mask to path
        return createPathFromMask(selectionMask, width, height)
    }
    
    /**
     * Flood fill algorithm for contiguous selection
     */
    private fun floodFillContiguous(
        pixels: IntArray,
        selectionMask: BooleanArray,
        width: Int,
        height: Int,
        startX: Int,
        startY: Int,
        targetColor: Int,
        tolerance: Int
    ) {
        val stack = Stack<Point>()
        stack.push(Point(startX, startY))
        
        while (stack.isNotEmpty()) {
            val point = stack.pop()
            val x = point.x
            val y = point.y
            
            // Check bounds
            if (x < 0 || x >= width || y < 0 || y >= height) continue
            
            val index = y * width + x
            
            // Skip if already selected
            if (selectionMask[index]) continue
            
            val currentColor = pixels[index]
            
            // Check if color matches within tolerance
            if (!colorMatches(currentColor, targetColor, tolerance)) continue
            
            // Mark as selected
            selectionMask[index] = true
            
            // Add neighbors to stack
            stack.push(Point(x + 1, y))
            stack.push(Point(x - 1, y))
            stack.push(Point(x, y + 1))
            stack.push(Point(x, y - 1))
        }
    }
    
    /**
     * Global color selection (non-contiguous)
     */
    private fun floodFillGlobal(
        pixels: IntArray,
        selectionMask: BooleanArray,
        width: Int,
        height: Int,
        targetColor: Int,
        tolerance: Int
    ) {
        for (i in pixels.indices) {
            if (colorMatches(pixels[i], targetColor, tolerance)) {
                selectionMask[i] = true
            }
        }
    }
    
    /**
     * Check if two colors match within tolerance
     */
    private fun colorMatches(color1: Int, color2: Int, tolerance: Int): Boolean {
        val r1 = Color.red(color1)
        val g1 = Color.green(color1)
        val b1 = Color.blue(color1)
        val a1 = Color.alpha(color1)
        
        val r2 = Color.red(color2)
        val g2 = Color.green(color2)
        val b2 = Color.blue(color2)
        val a2 = Color.alpha(color2)
        
        return abs(r1 - r2) <= tolerance &&
               abs(g1 - g2) <= tolerance &&
               abs(b1 - b2) <= tolerance &&
               abs(a1 - a2) <= tolerance
    }
    
    /**
     * Create a path from selection mask using marching squares algorithm
     */
    private fun createPathFromMask(selectionMask: BooleanArray, width: Int, height: Int): Path {
        val path = Path()
        val visited = BooleanArray(width * height)
        
        // Find contours using marching squares
        for (y in 0 until height - 1) {
            for (x in 0 until width - 1) {
                val index = y * width + x
                
                if (selectionMask[index] && !visited[index]) {
                    val contour = traceContour(selectionMask, visited, width, height, x, y)
                    if (contour.isNotEmpty()) {
                        addContourToPath(path, contour)
                    }
                }
            }
        }
        
        return path
    }
    
    /**
     * Trace contour using Moore neighborhood tracing
     */
    private fun traceContour(
        selectionMask: BooleanArray,
        visited: BooleanArray,
        width: Int,
        height: Int,
        startX: Int,
        startY: Int
    ): List<Point> {
        val contour = mutableListOf<Point>()
        val directions = arrayOf(
            Point(1, 0), Point(1, 1), Point(0, 1), Point(-1, 1),
            Point(-1, 0), Point(-1, -1), Point(0, -1), Point(1, -1)
        )
        
        var currentX = startX
        var currentY = startY
        var direction = 0
        
        do {
            contour.add(Point(currentX, currentY))
            visited[currentY * width + currentX] = true
            
            // Find next boundary pixel
            var found = false
            for (i in 0 until 8) {
                val dir = directions[(direction + i) % 8]
                val nextX = currentX + dir.x
                val nextY = currentY + dir.y
                
                if (nextX >= 0 && nextX < width && nextY >= 0 && nextY < height) {
                    val nextIndex = nextY * width + nextX
                    if (selectionMask[nextIndex]) {
                        currentX = nextX
                        currentY = nextY
                        direction = (direction + i + 6) % 8 // Turn left
                        found = true
                        break
                    }
                }
            }
            
            if (!found) break
            
        } while (currentX != startX || currentY != startY)
        
        return contour
    }
    
    /**
     * Add contour points to path
     */
    private fun addContourToPath(path: Path, contour: List<Point>) {
        if (contour.isEmpty()) return
        
        val first = contour.first()
        path.moveTo(first.x.toFloat(), first.y.toFloat())
        
        for (i in 1 until contour.size) {
            val point = contour[i]
            path.lineTo(point.x.toFloat(), point.y.toFloat())
        }
        
        path.close()
    }
    
    /**
     * Smooth selection edges using Gaussian blur
     */
    fun smoothSelection(
        selectionMask: BooleanArray,
        width: Int,
        height: Int,
        radius: Float = 2f
    ): BooleanArray {
        val smoothedMask = BooleanArray(width * height)
        val kernelSize = (radius * 2).toInt() + 1
        val kernel = createGaussianKernel(kernelSize, radius)
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                var sum = 0f
                var totalWeight = 0f
                
                for (ky in -kernelSize/2..kernelSize/2) {
                    for (kx in -kernelSize/2..kernelSize/2) {
                        val nx = x + kx
                        val ny = y + ky
                        
                        if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                            val weight = kernel[ky + kernelSize/2][kx + kernelSize/2]
                            val value = if (selectionMask[ny * width + nx]) 1f else 0f
                            sum += value * weight
                            totalWeight += weight
                        }
                    }
                }
                
                smoothedMask[y * width + x] = (sum / totalWeight) > 0.5f
            }
        }
        
        return smoothedMask
    }
    
    /**
     * Create Gaussian blur kernel
     */
    private fun createGaussianKernel(size: Int, sigma: Float): Array<FloatArray> {
        val kernel = Array(size) { FloatArray(size) }
        val center = size / 2
        var sum = 0f
        
        for (y in 0 until size) {
            for (x in 0 until size) {
                val dx = x - center
                val dy = y - center
                val value = Math.exp(-(dx * dx + dy * dy) / (2 * sigma * sigma).toDouble()).toFloat()
                kernel[y][x] = value
                sum += value
            }
        }
        
        // Normalize kernel
        for (y in 0 until size) {
            for (x in 0 until size) {
                kernel[y][x] /= sum
            }
        }
        
        return kernel
    }
} 