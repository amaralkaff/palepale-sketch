# Phase 5.2: Layer System

## Overview
This specification defines a comprehensive layer management system for Social Sketch, enabling professional digital art workflows with multiple drawing layers, layer effects, and advanced compositing. The layer system provides the foundation for non-destructive editing and complex artistic techniques.

## Dependencies
- **Phase 5.1 Complete**: Advanced brush system must be operational
- **Phase 4 Complete**: Core drawing functionality and command system
- **DrawingEngine**: Extended for multi-layer rendering support
- **Memory Management**: Enhanced bitmap management for multiple layers

## Layer System Features

### 1. Core Layer Functionality
**Purpose**: Provide industry-standard layer management for digital art creation.

**Layer Types**:
- **Drawing Layer**: Standard drawing layer for artwork
- **Background Layer**: Special base layer (always present, cannot be deleted)
- **Group Layer**: Container for organizing multiple layers
- **Adjustment Layer**: Non-destructive color and effect adjustments
- **Text Layer**: Vector text rendering (future enhancement)
- **Reference Layer**: Non-destructive reference images

**Layer Properties**:
- **Visibility**: Show/hide layer content
- **Opacity**: Layer transparency (0-100%)
- **Blend Mode**: How layer combines with layers below
- **Lock**: Prevent modifications to layer content
- **Name**: User-customizable layer names
- **Thumbnail**: Auto-generated layer preview
### 2. Layer Stack Management
**Purpose**: Organize and manipulate layer hierarchy with intuitive controls.

**Stack Operations**:
- **Add Layer**: Create new layers above current selection
- **Delete Layer**: Remove layers with undo support
- **Duplicate Layer**: Copy layer content and properties
- **Merge Down**: Combine layer with the layer below
- **Flatten Image**: Merge all visible layers
- **Rearrange**: Drag-and-drop layer reordering

**Layer Selection**:
- **Single Selection**: Work on one layer at a time
- **Multi-Selection**: Select multiple layers for group operations
- **Active Layer**: Currently selected layer for drawing operations
- **Quick Selection**: Tap layer content to auto-select layer

### 3. Advanced Layer Effects
**Purpose**: Professional non-destructive effects and adjustments.

**Blend Modes** (Enhanced from Phase 5.1):
- **Normal, Multiply, Screen, Overlay**
- **Soft Light, Hard Light, Color Dodge, Color Burn**
- **Darken, Lighten, Difference, Exclusion**
- **Hue, Saturation, Color, Luminosity**

**Layer Effects**:
- **Drop Shadow**: Customizable shadow with offset, blur, and color
- **Inner Shadow**: Inward shadow effect
- **Outer Glow**: Soft glow around layer content
- **Inner Glow**: Inward glow effect
- **Stroke**: Outline effect with customizable width and color
- **Color Overlay**: Tint layer with specified color

### 4. Layer Masks and Clipping
**Purpose**: Enable selective editing and advanced compositing techniques.

**Layer Masks**:
- **Alpha Masks**: Transparency-based masking
- **Vector Masks**: Clean, scalable mask shapes
- **Gradient Masks**: Smooth transitions and fades
- **Brush Masks**: Hand-painted mask details
- **Mask Inversion**: Flip mask transparency

**Clipping Masks**:
- **Clip to Layer**: Use lower layer shape as mask
- **Clipping Groups**: Multiple layers clipped to base layer
- **Clipping Indicators**: Visual feedback for clipped layers

## Implementation Architecture

### 1. Layer Management System
**Purpose**: Core layer data structure and management operations.

**Layer Model**:
```kotlin
data class DrawingLayer(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var isVisible: Boolean = true,
    var opacity: Float = 1.0f, // 0.0 to 1.0
    var blendMode: BlendMode = BlendMode.NORMAL,
    var isLocked: Boolean = false,
    val type: LayerType = LayerType.DRAWING,
    var bitmap: Bitmap? = null,
    var effects: List<LayerEffect> = emptyList(),
    var mask: LayerMask? = null,
    var clippingMask: String? = null, // ID of layer to clip to
    var parent: String? = null, // For group layers
    val createdAt: Long = System.currentTimeMillis(),
    var modifiedAt: Long = System.currentTimeMillis()
)

enum class LayerType {
    BACKGROUND,
    DRAWING,
    GROUP,
    ADJUSTMENT,
    TEXT,
    REFERENCE
}
```

**Layer Manager**:
```kotlin
class LayerManager {
    private val layers = mutableListOf<DrawingLayer>()
    private var activeLayerId: String? = null
    private val layerListeners = mutableListOf<LayerListener>()
    
    // Layer operations
    fun addLayer(name: String, type: LayerType = LayerType.DRAWING): DrawingLayer
    fun deleteLayer(layerId: String): Boolean
    fun duplicateLayer(layerId: String): DrawingLayer?
    fun moveLayer(layerId: String, newIndex: Int): Boolean
    fun mergeDown(layerId: String): Boolean
    
    // Layer properties
    fun setLayerOpacity(layerId: String, opacity: Float)
    fun setLayerBlendMode(layerId: String, blendMode: BlendMode)
    fun toggleLayerVisibility(layerId: String)
    fun setLayerLocked(layerId: String, locked: Boolean)
    
    // Layer selection
    fun setActiveLayer(layerId: String)
    fun getActiveLayer(): DrawingLayer?
    fun selectMultipleLayers(layerIds: List<String>)
    
    // Composite operations
    fun getCompositeImage(): Bitmap?
    fun flattenLayers(): Bitmap?
}
```### 2. Layer Rendering Engine
**Purpose**: Efficiently render multi-layer compositions with effects and blending.

**Composite Renderer**:
```kotlin
class LayerRenderer {
    private val renderCache = LruCache<String, Bitmap>(20)
    private val compositeCanvas = Canvas()
    private var hardwareAccelerated = true
    
    fun renderLayer(layer: DrawingLayer): Bitmap?
    fun renderComposite(layers: List<DrawingLayer>): Bitmap?
    fun renderLayerWithEffects(layer: DrawingLayer): Bitmap?
    fun applyBlendMode(source: Bitmap, destination: Bitmap, mode: BlendMode): Bitmap
    
    // Performance optimization
    fun invalidateCache(layerId: String)
    fun clearCache()
    fun enableHardwareAcceleration(enabled: Boolean)
}
```

### 3. Layer Effects System
**Purpose**: Apply non-destructive effects to layers with real-time preview.

**Effect Framework**:
```kotlin
interface LayerEffect {
    val id: String
    val name: String
    val enabled: Boolean
    
    fun apply(source: Bitmap): Bitmap
    fun getSettings(): EffectSettings
    fun setSettings(settings: EffectSettings)
}

data class DropShadowEffect(
    override val id: String = "drop_shadow",
    override val name: String = "Drop Shadow",
    override val enabled: Boolean = true,
    val offsetX: Float = 4f,
    val offsetY: Float = 4f,
    val blurRadius: Float = 8f,
    val color: Int = Color.BLACK,
    val opacity: Float = 0.75f
) : LayerEffect {
    override fun apply(source: Bitmap): Bitmap {
        // Implement drop shadow rendering
    }
}
```

## Package Organization

### New Package Structure
```
com.example.drawinggame.ui.drawing.layers/
├── core/
│   ├── LayerManager.kt                  # Core layer management
│   ├── DrawingLayer.kt                  # Layer data model
│   ├── LayerType.kt                     # Layer type definitions
│   └── LayerListener.kt                 # Layer change callbacks
├── rendering/
│   ├── LayerRenderer.kt                 # Multi-layer rendering engine
│   ├── CompositeRenderer.kt             # Final composite generation
│   ├── BlendModeRenderer.kt             # Blend mode calculations
│   └── EffectRenderer.kt                # Layer effect rendering
├── effects/
│   ├── LayerEffect.kt                   # Base effect interface
│   ├── DropShadowEffect.kt              # Drop shadow implementation
│   ├── GlowEffect.kt                    # Glow effect implementation
│   ├── StrokeEffect.kt                  # Stroke effect implementation
│   └── ColorOverlayEffect.kt            # Color overlay implementation
├── masks/
│   ├── LayerMask.kt                     # Layer mask data model
│   ├── MaskRenderer.kt                  # Mask rendering system
│   ├── ClippingMask.kt                  # Clipping mask implementation
│   └── MaskEditor.kt                    # Mask editing tools
├── ui/
│   ├── LayerPanelFragment.kt            # Layer management UI
│   ├── LayerAdapter.kt                  # RecyclerView adapter
│   ├── LayerViewHolder.kt               # Individual layer item
│   ├── LayerEffectsDialog.kt            # Layer effects configuration
│   └── LayerPropertiesDialog.kt         # Layer properties panel
└── commands/
    ├── AddLayerCommand.kt               # Add layer command
    ├── DeleteLayerCommand.kt            # Delete layer command
    ├── MoveLayerCommand.kt              # Move layer command
    ├── SetLayerPropertyCommand.kt       # Property change command
    └── MergeLayersCommand.kt            # Layer merge command
```