package com.example.drawinggame.ui.drawing.testing

import android.content.Context
import android.graphics.*
import com.example.drawinggame.ui.drawing.layers.core.LayerManager
import com.example.drawinggame.ui.drawing.layers.core.DrawingLayer
import com.example.drawinggame.ui.drawing.layers.core.LayerType
import com.example.drawinggame.ui.drawing.layers.effects.DropShadowEffect
import com.example.drawinggame.ui.drawing.layers.effects.GlowEffect
import com.example.drawinggame.ui.drawing.selection.core.SelectionManager
import com.example.drawinggame.ui.drawing.selection.core.SelectionType
import com.example.drawinggame.ui.drawing.selection.algorithms.MagicWandAlgorithm
import com.example.drawinggame.ui.drawing.performance.PerformanceMonitor
import kotlinx.coroutines.*

/**
 * Integration test suite for Phase 5 advanced drawing features
 * Phase 5: Testing & Polish
 */
class IntegrationTestSuite(private val context: Context) {
    
    private val testResults = mutableListOf<TestResult>()
    
    /**
     * Run all integration tests
     */
    suspend fun runAllTests(): TestSuiteResult {
        testResults.clear()
        
        // Layer system tests
        testLayerCreationAndManagement()
        testLayerEffects()
        testLayerBlending()
        
        // Selection system tests
        testSelectionTools()
        testSelectionOperations()
        testMagicWandAlgorithm()
        
        // Performance tests
        testPerformanceUnderLoad()
        
        // Integration tests
        testLayerSelectionIntegration()
        
        val passedTests = testResults.count { it.passed }
        val totalTests = testResults.size
        
        return TestSuiteResult(
            totalTests = totalTests,
            passedTests = passedTests,
            failedTests = totalTests - passedTests,
            testResults = testResults.toList(),
            executionTimeMs = testResults.sumOf { it.executionTimeMs }
        )
    }
    
    /**
     * Test layer creation and management
     */
    private suspend fun testLayerCreationAndManagement() {
        runTest("Layer Creation and Management") {
            val layerManager = LayerManager(context)
            
            // Test layer creation
            val layer1 = layerManager.addLayer(LayerType.DRAWING, "Test Layer 1")
            assert(layerManager.getLayers().size == 2) // Background + new layer
            assert(layer1.name == "Test Layer 1")
            
            // Test layer duplication
            val duplicatedLayer = layerManager.duplicateLayer(layer1.id)!!
            assert(layerManager.getLayers().size == 3)
            assert(duplicatedLayer.name.contains("Copy"))
            
            // Test layer deletion
            layerManager.deleteLayer(duplicatedLayer.id)
            assert(layerManager.getLayers().size == 2)
            
            // Test layer reordering
            val layer2 = layerManager.addLayer(LayerType.DRAWING, "Test Layer 2")
            val initialIndex = layerManager.getLayers().indexOf(layer2)
            layerManager.moveLayer(layer2.id, 0)
            assert(layerManager.getLayers()[0] == layer2)
            
            // Test layer properties
            layerManager.setLayerOpacity(layer1.id, 0.5f)
            assert(layerManager.getLayer(layer1.id)?.opacity == 0.5f)
            
            layerManager.setLayerVisibility(layer1.id, false)
            assert(layerManager.getLayer(layer1.id)?.isVisible == false)
        }
    }
    
    /**
     * Test layer effects
     */
    private suspend fun testLayerEffects() {
        runTest("Layer Effects") {
            val layerManager = LayerManager(context)
            val layer = layerManager.addLayer(LayerType.DRAWING, "Effect Test Layer")
            
            // Create test bitmap
            val testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(testBitmap)
            canvas.drawColor(Color.BLUE)
            layer.bitmap = testBitmap
            
            // Test drop shadow effect
            val dropShadow = DropShadowEffect(offsetX = 5f, offsetY = 5f, blurRadius = 10f, color = Color.BLACK)
            layerManager.addLayerEffect(layer.id, dropShadow)
            assert(layer.effects.contains(dropShadow))
            
            // Test glow effect
            val glow = GlowEffect(radius = 15f, color = Color.WHITE, opacity = 0.8f)
            layerManager.addLayerEffect(layer.id, glow)
            assert(layer.effects.size == 2)
            
            // Test effect removal
            layerManager.removeLayerEffect(layer.id, dropShadow)
            assert(layer.effects.size == 1)
            assert(!layer.effects.contains(dropShadow))
        }
    }
    
    /**
     * Test layer blending
     */
    private suspend fun testLayerBlending() {
        runTest("Layer Blending") {
            val layerManager = LayerManager(context)
            val layer = layerManager.addLayer(LayerType.DRAWING, "Blend Test Layer")
            
            // Test blend mode changes
            val blendModes = listOf(
                com.example.drawinggame.ui.drawing.advanced.blending.BlendMode.NORMAL,
                com.example.drawinggame.ui.drawing.advanced.blending.BlendMode.MULTIPLY,
                com.example.drawinggame.ui.drawing.advanced.blending.BlendMode.SCREEN,
                com.example.drawinggame.ui.drawing.advanced.blending.BlendMode.OVERLAY
            )
            
            blendModes.forEach { blendMode ->
                layerManager.setLayerBlendMode(layer.id, blendMode)
                assert(layer.blendMode == blendMode)
            }
        }
    }
    
    /**
     * Test selection tools
     */
    private suspend fun testSelectionTools() {
        runTest("Selection Tools") {
            val selectionManager = SelectionManager()
            
            // Test rectangular selection
            selectionManager.setActiveTool(SelectionType.RECTANGULAR)
            val rectSelection = selectionManager.createRectangularSelection(10f, 10f, 100f, 100f)
            assert(rectSelection != null)
            assert(rectSelection.type == SelectionType.RECTANGULAR)
            
            // Test elliptical selection
            selectionManager.setActiveTool(SelectionType.ELLIPTICAL)
            val ellipseSelection = selectionManager.createEllipticalSelection(10f, 10f, 100f, 100f)
            assert(ellipseSelection != null)
            assert(ellipseSelection.type == SelectionType.ELLIPTICAL)
            
            // Test selection bounds
            assert(rectSelection.bounds.width() == 90f)
            assert(rectSelection.bounds.height() == 90f)
        }
    }
    
    /**
     * Test selection operations
     */
    private suspend fun testSelectionOperations() {
        runTest("Selection Operations") {
            val selectionManager = SelectionManager()
            
            // Create test selection
            val selection = selectionManager.createRectangularSelection(10f, 10f, 100f, 100f)
            selectionManager.setActiveSelection(selection.id)
            
            // Test feathering
            selectionManager.featherSelection(selection.id, 5f)
            val featheredSelection = selectionManager.getSelection(selection.id)
            assert(featheredSelection?.featherRadius == 5f)
            
            // Test expansion
            val originalBounds = RectF(featheredSelection!!.bounds)
            selectionManager.expandSelection(selection.id, 10f)
            val expandedSelection = selectionManager.getSelection(selection.id)
            assert(expandedSelection!!.bounds.width() > originalBounds.width())
            
            // Test contraction
            selectionManager.contractSelection(selection.id, 5f)
            val contractedSelection = selectionManager.getSelection(selection.id)
            assert(contractedSelection!!.bounds.width() < expandedSelection.bounds.width())
        }
    }
    
    /**
     * Test magic wand algorithm
     */
    private suspend fun testMagicWandAlgorithm() {
        runTest("Magic Wand Algorithm") {
            val algorithm = MagicWandAlgorithm()
            
            // Create test bitmap with distinct colors
            val testBitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(testBitmap)
            canvas.drawColor(Color.WHITE)
            
            val paint = Paint().apply { color = Color.RED }
            canvas.drawRect(10f, 10f, 40f, 40f, paint)
            
            // Test magic wand selection on red area
            val selectionPath = algorithm.performSelection(
                bitmap = testBitmap,
                startX = 25,
                startY = 25,
                tolerance = 10,
                contiguous = true
            )
            
            val bounds = RectF()
            selectionPath.computeBounds(bounds, true)
            
            // Should select the red rectangle area
            assert(bounds.width() > 20f)
            assert(bounds.height() > 20f)
        }
    }
    
    /**
     * Test performance under load
     */
    private suspend fun testPerformanceUnderLoad() {
        runTest("Performance Under Load") {
            PerformanceMonitor.setEnabled(true)
            PerformanceMonitor.clearStats()
            
            val layerManager = LayerManager(context)
            
            // Create multiple layers
            repeat(10) { i ->
                PerformanceMonitor.measureOperation("layer_creation") {
                    layerManager.addLayer(LayerType.DRAWING, "Load Test Layer $i")
                }
            }
            
            // Test layer operations under load
            val layers = layerManager.getLayers()
            layers.forEach { layer ->
                PerformanceMonitor.measureOperation("layer_property_change") {
                    layerManager.setLayerOpacity(layer.id, 0.5f)
                    layerManager.setLayerVisibility(layer.id, false)
                    layerManager.setLayerVisibility(layer.id, true)
                }
            }
            
            // Check performance metrics
            val stats = PerformanceMonitor.getAllStats()
            assert(stats.isNotEmpty())
            
            // Verify no operations are extremely slow
            stats.forEach { (_, stat) ->
                assert(stat.averageTimeMs < 1000) // No operation should take more than 1 second on average
            }
        }
    }
    
    /**
     * Test integration between layers and selections
     */
    private suspend fun testLayerSelectionIntegration() {
        runTest("Layer-Selection Integration") {
            val layerManager = LayerManager(context)
            val selectionManager = SelectionManager()
            
            // Create test layer with content
            val layer = layerManager.addLayer(LayerType.DRAWING, "Integration Test Layer")
            val testBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(testBitmap)
            canvas.drawColor(Color.BLUE)
            layer.bitmap = testBitmap
            
            // Create selection on layer
            val selection = selectionManager.createRectangularSelection(50f, 50f, 150f, 150f)
            selectionManager.setActiveSelection(selection.id)
            
            // Test that selection and layer can coexist
            assert(layerManager.getCurrentLayer() != null)
            assert(selectionManager.getCurrentSelection() != null)
            
            // Test layer operations don't affect selection
            layerManager.setLayerOpacity(layer.id, 0.8f)
            assert(selectionManager.getCurrentSelection()?.id == selection.id)
            
            // Test selection operations don't affect layer
            selectionManager.featherSelection(selection.id, 3f)
            assert(layerManager.getLayer(layer.id)?.opacity == 0.8f)
        }
    }
    
    /**
     * Run a single test with error handling and timing
     */
    private suspend fun runTest(testName: String, test: suspend () -> Unit) {
        val startTime = System.currentTimeMillis()
        
        try {
            test()
            val executionTime = System.currentTimeMillis() - startTime
            testResults.add(TestResult(testName, true, null, executionTime))
        } catch (e: Exception) {
            val executionTime = System.currentTimeMillis() - startTime
            testResults.add(TestResult(testName, false, e.message, executionTime))
        }
    }
    
    /**
     * Get test results summary
     */
    fun getTestResultsSummary(): String {
        val passed = testResults.count { it.passed }
        val total = testResults.size
        val failedTests = testResults.filter { !it.passed }
        
        return buildString {
            appendLine("=== Integration Test Results ===")
            appendLine("Passed: $passed/$total tests")
            appendLine("Total execution time: ${testResults.sumOf { it.executionTimeMs }}ms")
            
            if (failedTests.isNotEmpty()) {
                appendLine("\nFailed Tests:")
                failedTests.forEach { test ->
                    appendLine("- ${test.testName}: ${test.errorMessage}")
                }
            }
            
            appendLine("\nPerformance Recommendations:")
            PerformanceMonitor.getOptimizationRecommendations().forEach { recommendation ->
                appendLine("- $recommendation")
            }
        }
    }
}

/**
 * Result of a single test
 */
data class TestResult(
    val testName: String,
    val passed: Boolean,
    val errorMessage: String?,
    val executionTimeMs: Long
)

/**
 * Result of the entire test suite
 */
data class TestSuiteResult(
    val totalTests: Int,
    val passedTests: Int,
    val failedTests: Int,
    val testResults: List<TestResult>,
    val executionTimeMs: Long
) 