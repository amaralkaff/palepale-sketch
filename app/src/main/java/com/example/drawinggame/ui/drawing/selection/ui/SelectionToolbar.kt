package com.example.drawinggame.ui.drawing.selection.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.example.drawinggame.R
import com.example.drawinggame.databinding.ViewSelectionToolbarBinding
import com.example.drawinggame.ui.drawing.selection.tools.SelectionTool
import com.example.drawinggame.ui.drawing.selection.core.SelectionType

/**
 * Toolbar for selection tools and operations
 * Phase 5.3: Selection & Transformation Tools
 */
class SelectionToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewSelectionToolbarBinding
    private var currentTool = SelectionType.RECTANGULAR
    private var onToolSelectedListener: ((SelectionType) -> Unit)? = null
    private var onSelectionOperationListener: ((SelectionOperation) -> Unit)? = null

    enum class SelectionOperation {
        SELECT_ALL,
        DESELECT,
        INVERT,
        FEATHER,
        EXPAND,
        CONTRACT,
        COPY,
        CUT,
        PASTE,
        DELETE
    }

    init {
        binding = ViewSelectionToolbarBinding.inflate(LayoutInflater.from(context), this, true)
        setupToolButtons()
        setupOperationButtons()
        updateToolSelection()
    }

    private fun setupToolButtons() {
        binding.btnRectangularSelection.setOnClickListener {
            selectTool(SelectionType.RECTANGULAR)
        }

        binding.btnEllipticalSelection.setOnClickListener {
            selectTool(SelectionType.ELLIPTICAL)
        }

        binding.btnFreehandSelection.setOnClickListener {
            selectTool(SelectionType.FREEHAND)
        }

        binding.btnMagicWand.setOnClickListener {
            selectTool(SelectionType.MAGIC_WAND)
        }
    }

    private fun setupOperationButtons() {
        binding.btnSelectAll.setOnClickListener {
            onSelectionOperationListener?.invoke(SelectionOperation.SELECT_ALL)
        }

        binding.btnDeselect.setOnClickListener {
            onSelectionOperationListener?.invoke(SelectionOperation.DESELECT)
        }

        binding.btnInvertSelection.setOnClickListener {
            onSelectionOperationListener?.invoke(SelectionOperation.INVERT)
        }

        binding.btnFeatherSelection.setOnClickListener {
            onSelectionOperationListener?.invoke(SelectionOperation.FEATHER)
        }

        binding.btnExpandSelection.setOnClickListener {
            onSelectionOperationListener?.invoke(SelectionOperation.EXPAND)
        }

        binding.btnContractSelection.setOnClickListener {
            onSelectionOperationListener?.invoke(SelectionOperation.CONTRACT)
        }

        binding.btnCopySelection.setOnClickListener {
            onSelectionOperationListener?.invoke(SelectionOperation.COPY)
        }

        binding.btnCutSelection.setOnClickListener {
            onSelectionOperationListener?.invoke(SelectionOperation.CUT)
        }

        binding.btnPasteSelection.setOnClickListener {
            onSelectionOperationListener?.invoke(SelectionOperation.PASTE)
        }

        binding.btnDeleteSelection.setOnClickListener {
            onSelectionOperationListener?.invoke(SelectionOperation.DELETE)
        }
    }

    private fun selectTool(toolType: SelectionType) {
        currentTool = toolType
        updateToolSelection()
        onToolSelectedListener?.invoke(toolType)
    }

    private fun updateToolSelection() {
        // Reset all tool buttons
        binding.btnRectangularSelection.isSelected = false
        binding.btnEllipticalSelection.isSelected = false
        binding.btnFreehandSelection.isSelected = false
        binding.btnMagicWand.isSelected = false

        // Highlight current tool
        when (currentTool) {
            SelectionType.RECTANGULAR -> binding.btnRectangularSelection.isSelected = true
            SelectionType.ELLIPTICAL -> binding.btnEllipticalSelection.isSelected = true
            SelectionType.FREEHAND -> binding.btnFreehandSelection.isSelected = true
            SelectionType.MAGIC_WAND -> binding.btnMagicWand.isSelected = true
            else -> {} // Handle other selection types
        }
    }

    fun setOnToolSelectedListener(listener: (SelectionType) -> Unit) {
        onToolSelectedListener = listener
    }

    fun setOnSelectionOperationListener(listener: (SelectionOperation) -> Unit) {
        onSelectionOperationListener = listener
    }

    fun getCurrentTool(): SelectionType = currentTool

    fun enableSelectionOperations(hasSelection: Boolean) {
        binding.btnDeselect.isEnabled = hasSelection
        binding.btnInvertSelection.isEnabled = true
        binding.btnFeatherSelection.isEnabled = hasSelection
        binding.btnExpandSelection.isEnabled = hasSelection
        binding.btnContractSelection.isEnabled = hasSelection
        binding.btnCopySelection.isEnabled = hasSelection
        binding.btnCutSelection.isEnabled = hasSelection
        binding.btnDeleteSelection.isEnabled = hasSelection
        
        // Paste is enabled based on clipboard content (simplified for now)
        binding.btnPasteSelection.isEnabled = true
    }
} 