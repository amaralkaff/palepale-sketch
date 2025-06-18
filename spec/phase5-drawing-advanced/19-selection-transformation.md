# Phase 5.3: Selection and Transformation Tools

## Overview
This specification defines comprehensive selection and transformation tools for Social Sketch, enabling precise editing, manipulation, and refinement of artwork. These tools provide professional-grade editing capabilities for both individual elements and entire compositions.

## Dependencies
- **Phase 5.2 Complete**: Layer system must be operational for layer-based selections
- **Phase 5.1 Complete**: Advanced brush system for selection refinement
- **Phase 4 Complete**: Core drawing functionality and command system
- **Touch Handling**: Enhanced gesture recognition for transformation operations

## Selection System Features

### 1. Selection Tools
**Purpose**: Provide various methods for selecting artwork regions with precision.

**Selection Types**:
- **Rectangular Selection**: Standard rectangular marquee tool
- **Elliptical Selection**: Oval and circular selection areas
- **Freehand Selection**: Hand-drawn selection boundaries
- **Polygonal Selection**: Multi-point polygon selections
- **Magic Wand**: Color-based automatic selection
- **Quick Selection**: AI-assisted edge detection selection
- **Select All**: Select entire layer or canvas content
- **Select Similar**: Select similar colors/textures

**Selection Modes**:
- **New Selection**: Create fresh selection area
- **Add to Selection**: Expand existing selection
- **Subtract from Selection**: Remove from existing selection
- **Intersect Selection**: Keep only overlapping areas
- **Invert Selection**: Select everything except current selection

### 2. Selection Refinement
**Purpose**: Fine-tune selection boundaries for precise editing control.

**Refinement Tools**:
- **Feather**: Soft edge transitions with adjustable radius
- **Border**: Create selection border of specified width
- **Smooth**: Reduce jagged selection edges
- **Expand/Contract**: Grow or shrink selection by pixels
- **Transform Selection**: Move, scale, rotate selection boundary

**Edge Detection**:
- **Smart Edge**: Automatic edge snapping for clean selections
- **Contrast Enhancement**: Improve edge detection in low-contrast areas
- **Anti-aliasing**: Smooth selection edges for professional results
- **Pixel Precision**: Exact pixel-level selection control### 3. Transformation Tools
**Purpose**: Manipulate selected content with professional transformation capabilities.

**Basic Transformations**:
- **Move**: Translate selection to new position
- **Scale**: Resize with proportional or free scaling
- **Rotate**: Rotate around center or custom pivot point
- **Skew**: Slant selection horizontally or vertically
- **Flip**: Mirror horizontally or vertically
- **Perspective**: Four-corner perspective distortion

**Advanced Transformations**:
- **Warp**: Flexible mesh-based distortion
- **Liquify**: Brush-based distortion effects
- **Puppet Warp**: Pin-based organic deformation
- **3D Transform**: Basic 3D rotation and perspective
- **Lens Distortion**: Barrel and pincushion effects

### 4. Copy and Paste Operations
**Purpose**: Efficient content duplication and movement workflows.

**Copy Operations**:
- **Copy**: Duplicate selection to clipboard
- **Cut**: Remove and copy selection to clipboard
- **Copy Merged**: Copy flattened visible layers
- **Copy Layer**: Duplicate entire layer content
- **Copy Visible**: Copy all visible layer content

**Paste Operations**:
- **Paste**: Insert clipboard content as new layer
- **Paste in Place**: Insert at original position
- **Paste Into**: Paste inside selection boundary
- **Paste Outside**: Paste outside selection boundary
- **Paste Special**: Advanced paste with options

## Implementation Architecture

### 1. Selection Management System
**Purpose**: Core selection data structure and operations.

**Selection Model**:
```kotlin
data class Selection(
    val id: String = UUID.randomUUID().toString(),
    val path: Path,
    val bounds: RectF,
    val type: SelectionType,
    val mode: SelectionMode = SelectionMode.NEW,
    val featherRadius: Float = 0f,
    val antiAlias: Boolean = true,
    val transform: Matrix = Matrix(),
    val isActive: Boolean = true,
    val marching: Boolean = true, // Marching ants animation
    val createdAt: Long = System.currentTimeMillis()
)

enum class SelectionType {
    RECTANGULAR,
    ELLIPTICAL,
    FREEHAND,
    POLYGONAL,
    MAGIC_WAND,
    QUICK_SELECT,
    ALL,
    SIMILAR
}

enum class SelectionMode {
    NEW,
    ADD,
    SUBTRACT,
    INTERSECT
}
```

**Selection Manager**:
```kotlin
class SelectionManager {
    private var currentSelection: Selection? = null
    private val selectionHistory = mutableListOf<Selection>()
    private val selectionListeners = mutableListOf<SelectionListener>()
    
    // Selection operations
    fun createSelection(type: SelectionType, path: Path): Selection
    fun modifySelection(mode: SelectionMode, path: Path): Selection?
    fun clearSelection()
    fun invertSelection(canvasBounds: RectF): Selection?
    fun selectAll(canvasBounds: RectF): Selection
    
    // Selection refinement
    fun featherSelection(radius: Float): Selection?
    fun expandSelection(pixels: Float): Selection?
    fun contractSelection(pixels: Float): Selection?
    fun smoothSelection(): Selection?
    
    // Selection transformation
    fun transformSelection(transform: Matrix): Selection?
    fun moveSelection(dx: Float, dy: Float): Selection?
    
    // Selection queries
    fun hasSelection(): Boolean
    fun getSelectionBounds(): RectF?
    fun isPointInSelection(x: Float, y: Float): Boolean
}
```### 2. Transformation Engine
**Purpose**: Handle complex transformations with real-time preview and optimization.

**Transform Processor**:
```kotlin
class TransformationEngine {
    private val transformCache = LruCache<String, Bitmap>(10)
    private var previewMode = false
    
    // Basic transformations
    fun translate(bitmap: Bitmap, dx: Float, dy: Float): Bitmap
    fun scale(bitmap: Bitmap, scaleX: Float, scaleY: Float, pivot: PointF): Bitmap
    fun rotate(bitmap: Bitmap, degrees: Float, pivot: PointF): Bitmap
    fun skew(bitmap: Bitmap, skewX: Float, skewY: Float): Bitmap
    fun flip(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap
    
    // Advanced transformations
    fun perspective(bitmap: Bitmap, corners: Array<PointF>): Bitmap
    fun warp(bitmap: Bitmap, meshPoints: Array<PointF>): Bitmap
    fun liquify(bitmap: Bitmap, distortions: List<LiquifyDistortion>): Bitmap
    
    // Transform preview
    fun startPreview(transformation: TransformData)
    fun updatePreview(transformation: TransformData): Bitmap?
    fun commitPreview(): Bitmap?
    fun cancelPreview()
}
```

### 3. Selection Tools Implementation
**Purpose**: Implement various selection tool algorithms and behaviors.

**Selection Tool Framework**:
```kotlin
abstract class SelectionTool {
    abstract val type: SelectionType
    abstract val cursor: Int
    
    abstract fun startSelection(x: Float, y: Float, canvas: Canvas)
    abstract fun updateSelection(x: Float, y: Float, canvas: Canvas)
    abstract fun finishSelection(canvas: Canvas): Selection?
    abstract fun cancelSelection()
    
    // Tool-specific settings
    abstract fun getSettingsPanel(): View?
    abstract fun applySettings(settings: Map<String, Any>)
}

class MagicWandTool : SelectionTool() {
    private var tolerance: Float = 32f
    private var contiguous: Boolean = true
    private var sampleAllLayers: Boolean = false
    
    override fun startSelection(x: Float, y: Float, canvas: Canvas) {
        // Implement flood-fill algorithm for color-based selection
    }
}

class QuickSelectionTool : SelectionTool() {
    private var brushSize: Float = 20f
    private var hardness: Float = 0.8f
    
    override fun startSelection(x: Float, y: Float, canvas: Canvas) {
        // Implement AI-assisted edge detection
    }
}
```

## User Interface Design

### 1. Selection Tool Panel
**Purpose**: Provide accessible selection tools without cluttering the drawing interface.

**Tool Panel Layout**:
```xml
<!-- Selection tools panel -->
<com.google.android.material.card.MaterialCardView
    android:id="@+id/selectionToolsCard"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_gravity="start|top"
    app:cardElevation="4dp">
    
    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="8dp">
        
        <!-- Selection tool buttons -->
        <com.google.android.material.button.MaterialButton
            android:id="@+id/rectangularSelectionButton"
            style="?attr/materialIconButtonStyle"
            android:contentDescription="Rectangular Selection"
            app:icon="@drawable/ic_selection_rectangular" />
            
        <com.google.android.material.button.MaterialButton
            android:id="@+id/ellipticalSelectionButton"
            style="?attr/materialIconButtonStyle"
            android:contentDescription="Elliptical Selection"
            app:icon="@drawable/ic_selection_elliptical" />
            
        <com.google.android.material.button.MaterialButton
            android:id="@+id/freehandSelectionButton"
            style="?attr/materialIconButtonStyle"
            android:contentDescription="Freehand Selection"
            app:icon="@drawable/ic_selection_freehand" />
            
        <com.google.android.material.button.MaterialButton
            android:id="@+id/magicWandButton"
            style="?attr/materialIconButtonStyle"
            android:contentDescription="Magic Wand"
            app:icon="@drawable/ic_magic_wand" />
            
    </LinearLayout>
    
</com.google.android.material.card.MaterialCardView>
```

### 2. Transform Handles
**Purpose**: Visual transformation controls with intuitive interaction.

**Transform Handle System**:
```kotlin
class TransformHandles {
    private val cornerHandles = Array(4) { TransformHandle(TransformHandle.Type.CORNER) }
    private val edgeHandles = Array(4) { TransformHandle(TransformHandle.Type.EDGE) }
    private val rotationHandle = TransformHandle(TransformHandle.Type.ROTATION)
    private val centerHandle = TransformHandle(TransformHandle.Type.CENTER)
    
    fun draw(canvas: Canvas, bounds: RectF, transform: Matrix)
    fun hitTest(x: Float, y: Float): TransformHandle?
    fun startTransform(handle: TransformHandle, startPoint: PointF)
    fun updateTransform(currentPoint: PointF): Matrix
    fun commitTransform(): Matrix
}

data class TransformHandle(
    val type: Type,
    var position: PointF = PointF(),
    var isActive: Boolean = false,
    val size: Float = 24f
) {
    enum class Type {
        CORNER, EDGE, ROTATION, CENTER
    }
}
```