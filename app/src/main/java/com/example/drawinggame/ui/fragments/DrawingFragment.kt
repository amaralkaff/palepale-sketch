package com.example.drawinggame.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.example.drawinggame.R
import com.example.drawinggame.databinding.FragmentDrawingBinding
import com.example.drawinggame.ui.drawing.DrawingEngine
import com.example.drawinggame.ui.drawing.DrawingView
import com.example.drawinggame.ui.drawing.brush.BrushManager
import com.example.drawinggame.ui.drawing.brush.BrushPreviewView
import com.example.drawinggame.ui.drawing.contracts.DrawingListener
import com.example.drawinggame.ui.drawing.models.DrawingTool
import com.example.drawinggame.ui.drawing.layers.ui.LayerPanelFragment
import com.example.drawinggame.ui.drawing.selection.ui.SelectionOverlayView
import com.example.drawinggame.ui.drawing.selection.ui.SelectionToolbar
import com.example.drawinggame.ui.drawing.selection.core.SelectionType
import com.example.drawinggame.ui.drawing.selection.core.SelectionManager
import com.google.android.material.slider.Slider
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class DrawingFragment : BaseFragment<FragmentDrawingBinding>() {
    
    // Drawing components
    private lateinit var drawingView: DrawingView
    private lateinit var drawingEngine: DrawingEngine
    private lateinit var brushManager: BrushManager
    
    // Advanced components
    private lateinit var layerPanelFragment: LayerPanelFragment
    private lateinit var selectionManager: SelectionManager
    private lateinit var selectionOverlay: SelectionOverlayView
    
    // Color selection
    private var selectedColorView: View? = null
    
    // Current mode
    private enum class DrawingMode { DRAW, LAYERS, SELECT }
    private var currentMode = DrawingMode.DRAW
    
    // Color map - linking color IDs to actual colors
    private val colorMap = mapOf(
        R.id.colorBlack to Color.BLACK,
        R.id.colorRed to Color.RED,
        R.id.colorGreen to Color.GREEN,
        R.id.colorBlue to Color.BLUE,
        R.id.colorYellow to Color.YELLOW,
        R.id.colorPurple to Color.parseColor("#800080"),
        R.id.colorOrange to Color.parseColor("#FFA500"),
        R.id.colorCyan to Color.CYAN
    )
    
    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentDrawingBinding {
        return FragmentDrawingBinding.inflate(inflater, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize drawing components
        setupDrawingComponents()
        
        // Set up advanced components
        setupAdvancedComponents()
        
        // Set up tool tabs
        setupToolTabs()
        
        // Set up brush settings
        setupBrushSettings()
    }
    
    override fun setupUI() {
        // Set up toolbar actions
        binding.drawingToolbar.setNavigationOnClickListener {
            // Save drawing action
            saveDrawing()
        }
        
        // Set up drawing tools
        binding.brushButton.setOnClickListener {
            setTool(DrawingTool.BRUSH)
            showBrushSettings()
        }
        binding.penButton.setOnClickListener {
            setTool(DrawingTool.PEN)
            showBrushSettings()
        }
        binding.eraserButton.setOnClickListener {
            setTool(DrawingTool.ERASER)
            showBrushSettings()
        }
        
        // Long press on tools to show settings
        binding.brushButton.setOnLongClickListener {
            showBrushSettings()
            true
        }
        
        binding.penButton.setOnLongClickListener {
            showBrushSettings()
            true
        }
        
        binding.eraserButton.setOnLongClickListener {
            showBrushSettings()
            true
        }
        
        // Set up action buttons
        binding.undoButton.setOnClickListener { undoDrawing() }
        binding.redoButton.setOnClickListener { redoDrawing() }
    }
    
    override fun setupObservers() {
        // Will be expanded in Phase 4.3
    }
    
    private fun setupDrawingComponents() {
        // Get drawing engine instance
        drawingEngine = DrawingEngine.getInstance()
        
        // Create and add drawing view to the canvas container
        drawingView = DrawingView(requireContext())
        binding.canvasContainer.removeAllViews() // Remove placeholder
        binding.canvasContainer.addView(drawingView)
        
        // Connect drawing view to engine
        drawingView.setDrawingEngine(drawingEngine)
        
        // Get the brush manager from the drawing view
        brushManager = drawingView.getBrushManager()
        
        // Set up drawing listener
        drawingView.setDrawingListener(object : DrawingListener {
            override fun onDrawingStarted(x: Float, y: Float) {
                // Will be implemented in Phase 4.3
            }
            
            override fun onDrawingProgress(x: Float, y: Float) {
                // Will be implemented in Phase 4.3
            }
            
            override fun onDrawingFinished() {
                // Will be implemented in Phase 4.3
            }
            
            override fun onCanvasCleared() {
                Toast.makeText(context, "Canvas cleared", Toast.LENGTH_SHORT).show()
            }
            
            override fun onUndoPerformed() {
                // Update UI state
                updateUndoRedoState()
            }
            
            override fun onRedoPerformed() {
                // Update UI state
                updateUndoRedoState()
            }
        })
    }
    
    private fun setupAdvancedComponents() {
        // Initialize selection manager
        selectionManager = SelectionManager()
        
        // Initialize selection overlay
        selectionOverlay = binding.selectionOverlay
        
        // Initialize layer panel fragment
        layerPanelFragment = LayerPanelFragment()
        
        // Set up selection toolbar
        binding.selectionToolbar.setOnToolSelectedListener { toolType ->
            // Switch selection tool
            selectionManager.setActiveTool(toolType)
        }
        
        binding.selectionToolbar.setOnSelectionOperationListener { operation ->
            handleSelectionOperation(operation)
        }
    }
    
    private fun setupToolTabs() {
        binding.toolTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> switchToDrawMode()
                    1 -> switchToLayerMode()
                    2 -> switchToSelectionMode()
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun switchToDrawMode() {
        currentMode = DrawingMode.DRAW
        binding.drawingToolsCard.visibility = View.VISIBLE
        binding.layerPanelContainer.visibility = View.GONE
        binding.selectionToolbar.visibility = View.GONE
        binding.selectionOverlay.visibility = View.GONE
    }
    
    private fun switchToLayerMode() {
        currentMode = DrawingMode.LAYERS
        binding.drawingToolsCard.visibility = View.GONE
        binding.layerPanelContainer.visibility = View.VISIBLE
        binding.selectionToolbar.visibility = View.GONE
        binding.selectionOverlay.visibility = View.GONE
        
        // Add layer fragment if not already added
        if (!layerPanelFragment.isAdded) {
            childFragmentManager.beginTransaction()
                .replace(R.id.layerPanelContainer, layerPanelFragment)
                .commit()
        }
    }
    
    private fun switchToSelectionMode() {
        currentMode = DrawingMode.SELECT
        binding.drawingToolsCard.visibility = View.GONE
        binding.layerPanelContainer.visibility = View.GONE
        binding.selectionToolbar.visibility = View.VISIBLE
        binding.selectionOverlay.visibility = View.VISIBLE
    }
    
    private fun handleSelectionOperation(operation: SelectionToolbar.SelectionOperation) {
        when (operation) {
            SelectionToolbar.SelectionOperation.SELECT_ALL -> {
                // Select entire canvas
                selectionManager.selectAll(drawingView.width.toFloat(), drawingView.height.toFloat())
                updateSelectionUI()
            }
            SelectionToolbar.SelectionOperation.DESELECT -> {
                selectionManager.clearSelection()
                updateSelectionUI()
            }
            SelectionToolbar.SelectionOperation.INVERT -> {
                selectionManager.invertSelection()
                updateSelectionUI()
            }
            SelectionToolbar.SelectionOperation.FEATHER -> {
                selectionManager.getCurrentSelection()?.let { selection ->
                    selectionManager.featherSelection(selection.id, 5f)
                    updateSelectionUI()
                }
            }
            SelectionToolbar.SelectionOperation.EXPAND -> {
                selectionManager.getCurrentSelection()?.let { selection ->
                    selectionManager.expandSelection(selection.id, 5f)
                    updateSelectionUI()
                }
            }
            SelectionToolbar.SelectionOperation.CONTRACT -> {
                selectionManager.getCurrentSelection()?.let { selection ->
                    selectionManager.contractSelection(selection.id, 5f)
                    updateSelectionUI()
                }
            }
            SelectionToolbar.SelectionOperation.COPY -> {
                // TODO: Implement copy functionality
                Toast.makeText(context, "Copy selection", Toast.LENGTH_SHORT).show()
            }
            SelectionToolbar.SelectionOperation.CUT -> {
                // TODO: Implement cut functionality
                Toast.makeText(context, "Cut selection", Toast.LENGTH_SHORT).show()
            }
            SelectionToolbar.SelectionOperation.PASTE -> {
                // TODO: Implement paste functionality
                Toast.makeText(context, "Paste selection", Toast.LENGTH_SHORT).show()
            }
            SelectionToolbar.SelectionOperation.DELETE -> {
                // TODO: Implement delete functionality
                Toast.makeText(context, "Delete selection", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateSelectionUI() {
        val currentSelection = selectionManager.getCurrentSelection()
        selectionOverlay.setSelection(currentSelection)
        binding.selectionToolbar.enableSelectionOperations(currentSelection != null)
    }
    
    private fun setupBrushSettings() {
        // Set up close button
        binding.closeSettingsButton.setOnClickListener {
            hideBrushSettings()
        }
        
        // Set up size slider
        binding.sizeSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                updateBrushSize(value)
            }
        }
        
        // Set up opacity slider
        binding.opacitySlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val opacity = (value * 2.55f).toInt() // Convert 0-100 to 0-255
                updateBrushOpacity(opacity)
            }
        }
        
        // Set up color palette
        setupColorPalette()
        
        // Update UI with initial brush settings
        updateBrushSettingsUI()
    }
    
    private fun setupColorPalette() {
        // Get initial color
        val initialColor = brushManager.getCurrentColor()
        
        // Set up click listeners for color views
        for (colorView in binding.colorPalette.children) {
            colorView.setOnClickListener {
                val color = colorMap[it.id] ?: Color.BLACK
                updateSelectedColorView(it)
                updateBrushColor(color)
            }
            
            // Set initial selected color
            if (colorMap[colorView.id] == initialColor) {
                updateSelectedColorView(colorView)
            }
        }
        
        // If no color matched, select black
        if (selectedColorView == null) {
            binding.colorPalette.findViewById<View>(R.id.colorBlack)?.let { blackView ->
                updateSelectedColorView(blackView)
            }
        }
    }
    
    private fun updateSelectedColorView(view: View) {
        // Remove selection from previous view
        selectedColorView?.isSelected = false
        
        // Update selection
        selectedColorView = view
        view.isSelected = true
    }
    
    private fun setTool(tool: DrawingTool) {
        // Update drawing engine tool
        drawingEngine.setTool(tool)
        
        // Update drawing view
        drawingView.updateBrush(tool = tool)
        
        // Update UI to reflect selected tool
        binding.brushButton.isSelected = tool == DrawingTool.BRUSH
        binding.penButton.isSelected = tool == DrawingTool.PEN
        binding.eraserButton.isSelected = tool == DrawingTool.ERASER
        
        // Update brush settings UI
        updateBrushSettingsUI()
        
        // Show toast for tool change
        val toolName = when(tool) {
            DrawingTool.BRUSH -> "Brush"
            DrawingTool.PEN -> "Pen"
            DrawingTool.ERASER -> "Eraser"
        }
        Toast.makeText(context, "$toolName selected", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateBrushSize(size: Float) {
        drawingView.updateBrush(size = size)
        updateBrushSettingsUI()
    }
    
    private fun updateBrushColor(color: Int) {
        // Only update color if not using eraser
        if (brushManager.getCurrentTool() != DrawingTool.ERASER) {
            drawingView.updateBrush(color = color)
            updateBrushSettingsUI()
        }
    }
    
    private fun updateBrushOpacity(opacity: Int) {
        // Only update opacity if not using eraser
        if (brushManager.getCurrentTool() != DrawingTool.ERASER) {
            drawingView.updateBrush(opacity = opacity)
            updateBrushSettingsUI()
        }
    }
    
    private fun updateBrushSettingsUI() {
        // Update sliders
        binding.sizeSlider.value = brushManager.getCurrentSize()
        binding.opacitySlider.value = brushManager.getCurrentOpacity() / 2.55f // Convert 0-255 to 0-100
        
        // Update brush preview
        val previewPaint = brushManager.getPreviewPaint()
        (binding.brushPreview as BrushPreviewView).updatePreview(previewPaint, brushManager.getCurrentTool())
        
        // Update brush info text
        val toolName = when (brushManager.getCurrentTool()) {
            DrawingTool.PEN -> "Pen"
            DrawingTool.BRUSH -> "Brush"
            DrawingTool.ERASER -> "Eraser"
        }
        val sizeText = brushManager.getCurrentSize().toInt()
        val opacityText = if (brushManager.getCurrentTool() == DrawingTool.ERASER) 
            "100%" else "${(brushManager.getCurrentOpacity() / 2.55f).toInt()}%"
        
        binding.currentBrushInfo.text = "$toolName · Size: ${sizeText}px · Opacity: $opacityText"
        
        // Update tool buttons
        binding.brushButton.isSelected = brushManager.getCurrentTool() == DrawingTool.BRUSH
        binding.penButton.isSelected = brushManager.getCurrentTool() == DrawingTool.PEN
        binding.eraserButton.isSelected = brushManager.getCurrentTool() == DrawingTool.ERASER
        
        // Disable opacity slider for eraser
        binding.opacitySlider.isEnabled = brushManager.getCurrentTool() != DrawingTool.ERASER
    }
    
    private fun showBrushSettings() {
        binding.brushSettingsCard.visibility = View.VISIBLE
    }
    
    private fun hideBrushSettings() {
        binding.brushSettingsCard.visibility = View.GONE
    }
    
    private fun undoDrawing() {
        lifecycleScope.launch {
            val undoPerformed = drawingEngine.undo()
            if (undoPerformed) {
                drawingView.invalidate()
                updateUndoRedoState()
            } else {
                Toast.makeText(context, "Nothing to undo", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun redoDrawing() {
        lifecycleScope.launch {
            val redoPerformed = drawingEngine.redo()
            if (redoPerformed) {
                drawingView.invalidate()
                updateUndoRedoState()
            } else {
                Toast.makeText(context, "Nothing to redo", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun saveDrawing() {
        // This will be implemented in Phase 4.4
        Toast.makeText(context, "Save drawing feature coming in Phase 4.4", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateUndoRedoState() {
        // This will be expanded in Phase 4.4 when we have access to undo/redo stack status
    }
    
    override fun onDestroyView() {
        // Clean up drawing components
        if (::drawingEngine.isInitialized) {
            drawingView.getDrawingListener()?.let { listener ->
                drawingEngine.removeListener(listener)
            }
        }
        
        super.onDestroyView()
    }
}