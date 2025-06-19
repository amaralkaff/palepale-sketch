package com.example.drawinggame.ui.drawing.layers.ui

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.drawinggame.R
import com.example.drawinggame.databinding.ItemLayerBinding
import com.example.drawinggame.ui.drawing.layers.core.DrawingLayer
import com.example.drawinggame.ui.drawing.layers.core.LayerType
import com.example.drawinggame.ui.drawing.advanced.blending.BlendMode

class LayerAdapter(
    private var layers: MutableList<DrawingLayer>,
    private val onLayerClick: (DrawingLayer) -> Unit,
    private val onVisibilityToggle: (DrawingLayer) -> Unit,
    private val onLayerOptionsClick: (DrawingLayer) -> Unit
) : RecyclerView.Adapter<LayerAdapter.LayerViewHolder>() {

    private var activeLayerId: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LayerViewHolder {
        val binding = ItemLayerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LayerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LayerViewHolder, position: Int) {
        val layer = layers[position]
        holder.bind(layer)
    }

    override fun getItemCount(): Int = layers.size

    fun updateLayers(newLayers: List<DrawingLayer>) {
        layers.clear()
        layers.addAll(newLayers)
        notifyDataSetChanged()
    }

    fun setActiveLayer(layerId: String) {
        activeLayerId = layerId
        notifyDataSetChanged()
    }

    inner class LayerViewHolder(
        private val binding: ItemLayerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(layer: DrawingLayer) {
            binding.apply {
                // Layer name
                layerName.text = layer.name

                // Layer type
                layerType.text = when (layer.type) {
                    LayerType.DRAWING -> "Drawing"
                    LayerType.TEXT -> "Text"
                    LayerType.ADJUSTMENT -> "Adjustment"
                    LayerType.GROUP -> "Group"
                    LayerType.BACKGROUND -> "Background"
                    LayerType.REFERENCE -> "Reference"
                }

                // Blend mode
                layerBlendMode.text = when (layer.blendMode) {
                    BlendMode.NORMAL -> "Normal"
                    BlendMode.MULTIPLY -> "Multiply"
                    BlendMode.SCREEN -> "Screen"
                    BlendMode.OVERLAY -> "Overlay"
                    BlendMode.SOFT_LIGHT -> "Soft Light"
                    BlendMode.HARD_LIGHT -> "Hard Light"
                    BlendMode.COLOR_DODGE -> "Color Dodge"
                    BlendMode.COLOR_BURN -> "Color Burn"
                    BlendMode.DARKEN -> "Darken"
                    BlendMode.LIGHTEN -> "Lighten"
                }

                // Layer thumbnail
                layer.bitmap?.let { bitmap ->
                    layerThumbnail.setImageBitmap(createThumbnail(bitmap))
                } ?: run {
                    layerThumbnail.setImageResource(R.drawable.ic_layers)
                }

                // Visibility toggle
                layerVisibilityToggle.setImageResource(
                    if (layer.isVisible) R.drawable.ic_visibility
                    else R.drawable.ic_visibility_off
                )

                // Active layer indicator
                activeLayerIndicator.visibility = if (layer.id == activeLayerId) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }

                // Lock status
                layerLockIcon.visibility = if (layer.isLocked) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }

                // Click listeners
                root.setOnClickListener { onLayerClick(layer) }
                layerVisibilityToggle.setOnClickListener { onVisibilityToggle(layer) }
                layerOptionsMenu.setOnClickListener { onLayerOptionsClick(layer) }
            }
        }

        private fun createThumbnail(bitmap: Bitmap): Bitmap {
            val thumbnailSize = 48
            return Bitmap.createScaledBitmap(bitmap, thumbnailSize, thumbnailSize, true)
        }
    }
} 