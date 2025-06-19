package com.example.drawinggame.ui.drawing.layers.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.drawinggame.R
import com.example.drawinggame.databinding.FragmentLayerPanelBinding
import com.example.drawinggame.ui.drawing.layers.core.DrawingLayer
import com.example.drawinggame.ui.drawing.layers.core.LayerManager
import com.example.drawinggame.ui.drawing.layers.core.LayerType
import com.example.drawinggame.ui.drawing.layers.effects.LayerEffect
import com.example.drawinggame.ui.drawing.layers.effects.DropShadowEffect
import com.example.drawinggame.ui.drawing.layers.effects.GlowEffect
import com.example.drawinggame.ui.drawing.layers.effects.StrokeEffect
import com.example.drawinggame.ui.drawing.advanced.blending.BlendMode

class LayerPanelFragment : Fragment() {
    
    private var _binding: FragmentLayerPanelBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var layerManager: LayerManager
    private lateinit var layerAdapter: LayerAdapter
    
    private val blendModes = arrayOf(
        "Normal", "Multiply", "Screen", "Overlay", "Soft Light", 
        "Hard Light", "Color Dodge", "Color Burn", "Darken", "Lighten",
        "Difference", "Exclusion", "Hue", "Saturation", "Color", "Luminosity"
    )
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLayerPanelBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupLayerManager()
        setupBlendModeSpinner()
        setupOpacityControl()
        setupLayerRecyclerView()
        setupLayerControls()
        setupEffectsPanel()
    }
    
    private fun setupLayerManager() {
        layerManager = LayerManager(requireContext())
        
        // Initialize with canvas dimensions
        layerManager.initialize(1000, 1000) // TODO: Get actual canvas dimensions
    }
    
    private fun setupBlendModeSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            blendModes
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        
        binding.blendModeSpinner.adapter = adapter
        binding.blendModeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                layerManager.getCurrentLayer()?.let { layer ->
                    val blendMode = when (blendModes[position]) {
                        "Normal" -> BlendMode.NORMAL
                        "Multiply" -> BlendMode.MULTIPLY
                        "Screen" -> BlendMode.SCREEN
                        "Overlay" -> BlendMode.OVERLAY
                        "Soft Light" -> BlendMode.SOFT_LIGHT
                        "Hard Light" -> BlendMode.HARD_LIGHT
                        "Color Dodge" -> BlendMode.COLOR_DODGE
                        "Color Burn" -> BlendMode.COLOR_BURN
                        "Darken" -> BlendMode.DARKEN
                        "Lighten" -> BlendMode.LIGHTEN
                        else -> BlendMode.NORMAL
                    }
                    layerManager.setLayerBlendMode(layer.id, blendMode)
                    layerAdapter.notifyDataSetChanged()
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun setupOpacityControl() {
        binding.opacitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.opacityValue.text = "${progress}%"
                    layerManager.getCurrentLayer()?.let { layer ->
                        layerManager.setLayerOpacity(layer.id, progress / 100f)
                        layerAdapter.notifyDataSetChanged()
                    }
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun setupLayerRecyclerView() {
        layerAdapter = LayerAdapter(
            layers = layerManager.getLayers().toMutableList(),
            onLayerClick = { layer ->
                layerManager.setActiveLayer(layer.id)
                updateCurrentLayerControls()
                layerAdapter.notifyDataSetChanged()
            },
            onVisibilityToggle = { layer ->
                layerManager.setLayerVisibility(layer.id, !layer.isVisible)
                layerAdapter.notifyItemChanged(layerManager.getLayers().indexOf(layer))
            },
            onLayerOptionsClick = { layer ->
                showLayerOptionsMenu(layer)
            }
        )
        
        binding.layerRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = layerAdapter
        }
        
        // Add drag and drop support
        val itemTouchHelper = ItemTouchHelper(LayerItemTouchCallback())
        itemTouchHelper.attachToRecyclerView(binding.layerRecyclerView)
    }
    
    private fun setupLayerControls() {
        binding.btnAddLayer.setOnClickListener {
            val newLayer = layerManager.addLayer(
                LayerType.DRAWING,
                "Layer ${layerManager.getLayers().size + 1}"
            )
            layerAdapter.updateLayers(layerManager.getLayers())
            layerManager.setActiveLayer(newLayer.id)
            updateCurrentLayerControls()
        }
        
        binding.btnDeleteLayer.setOnClickListener {
            layerManager.getCurrentLayer()?.let { layer ->
                if (layerManager.getLayers().size > 1) {
                    layerManager.deleteLayer(layer.id)
                    layerAdapter.updateLayers(layerManager.getLayers())
                    updateCurrentLayerControls()
                }
            }
        }
        
        binding.btnDuplicateLayer.setOnClickListener {
            layerManager.getCurrentLayer()?.let { layer ->
                val duplicatedLayer = layerManager.duplicateLayer(layer.id)
                duplicatedLayer?.let { newLayer ->
                    layerAdapter.updateLayers(layerManager.getLayers())
                    layerManager.setActiveLayer(newLayer.id)
                    updateCurrentLayerControls()
                }
            }
        }
    }
    
    private fun setupEffectsPanel() {
        binding.btnDropShadow.setOnClickListener {
            layerManager.getCurrentLayer()?.let { layer ->
                val effect = DropShadowEffect(
                    offsetX = 5f,
                    offsetY = 5f,
                    blurRadius = 10f,
                    color = 0x80000000.toInt()
                )
                layerManager.addLayerEffect(layer.id, effect)
                layerAdapter.notifyDataSetChanged()
            }
        }
        
        binding.btnGlow.setOnClickListener {
            layerManager.getCurrentLayer()?.let { layer ->
                val effect = GlowEffect(
                    radius = 15f,
                    color = 0x80FFFFFF.toInt(),
                    opacity = 0.8f
                )
                layerManager.addLayerEffect(layer.id, effect)
                layerAdapter.notifyDataSetChanged()
            }
        }
        
        binding.btnStroke.setOnClickListener {
            layerManager.getCurrentLayer()?.let { layer ->
                val effect = StrokeEffect(
                    width = 3f,
                    color = 0xFF000000.toInt()
                )
                layerManager.addLayerEffect(layer.id, effect)
                layerAdapter.notifyDataSetChanged()
            }
        }
    }
    
    private fun updateCurrentLayerControls() {
        layerManager.getCurrentLayer()?.let { layer ->
            // Update opacity slider
            val opacity = (layer.opacity * 100).toInt()
            binding.opacitySeekBar.progress = opacity
            binding.opacityValue.text = "${opacity}%"
            
            // Update blend mode spinner
            val blendModeIndex = when (layer.blendMode) {
                BlendMode.NORMAL -> 0
                BlendMode.MULTIPLY -> 1
                BlendMode.SCREEN -> 2
                BlendMode.OVERLAY -> 3
                BlendMode.SOFT_LIGHT -> 4
                BlendMode.HARD_LIGHT -> 5
                BlendMode.COLOR_DODGE -> 6
                BlendMode.COLOR_BURN -> 7
                BlendMode.DARKEN -> 8
                BlendMode.LIGHTEN -> 9
            }
            binding.blendModeSpinner.setSelection(blendModeIndex)
            
            // Show/hide effects panel based on layer effects
            binding.layerEffectsPanel.visibility = if (layer.effects.isNotEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
    
    private fun showLayerOptionsMenu(layer: DrawingLayer) {
        // TODO: Implement layer options menu (rename, lock, etc.)
    }
    
    private inner class LayerItemTouchCallback : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
        0
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition
            
            // Get the layer ID at the from position
            val layers = layerManager.getLayers()
            if (fromPosition >= 0 && fromPosition < layers.size) {
                val layerId = layers[fromPosition].id
                layerManager.moveLayer(layerId, toPosition)
            }
            layerAdapter.updateLayers(layerManager.getLayers())
            return true
        }
        
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // Not used
        }
    }
    
    fun getLayerManager(): LayerManager = layerManager
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 