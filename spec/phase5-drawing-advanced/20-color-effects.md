# Phase 5.4: Color Management and Effects

## Overview
This specification defines advanced color management and effect systems for Social Sketch, providing professional color tools, adjustment layers, filters, and special effects. These features enable sophisticated color workflows and creative effects for digital artwork.

## Dependencies
- **Phase 5.3 Complete**: Selection and transformation tools for targeted adjustments
- **Phase 5.2 Complete**: Layer system for non-destructive adjustments
- **Phase 5.1 Complete**: Advanced brush system for color interaction
- **Color Science**: HSV/HSL color space support and color theory implementation

## Color Management Features

### 1. Advanced Color Picker
**Purpose**: Professional color selection with multiple color models and precision controls.

**Color Picker Types**:
- **Wheel Picker**: Traditional color wheel with saturation triangle
- **Slider Picker**: Separate HSV/RGB/HSL sliders for precision
- **Grid Picker**: Swatch grid with custom and preset colors
- **Eyedropper**: Sample colors from artwork or reference images
- **Gradient Picker**: Select colors from custom gradients
- **Harmony Picker**: Generate color harmonies (complementary, triadic, etc.)

**Color Models**:
- **RGB**: Red, Green, Blue (0-255)
- **HSV**: Hue, Saturation, Value (360°, 100%, 100%)
- **HSL**: Hue, Saturation, Lightness (360°, 100%, 100%)
- **CMYK**: Cyan, Magenta, Yellow, Key (0-100%)
- **LAB**: Lightness, A*, B* (perceptual color space)
- **HEX**: Hexadecimal color codes

### 2. Color Palette Management
**Purpose**: Organize and manage color collections for consistent artwork styling.

**Palette Features**:
- **Custom Palettes**: User-created color collections
- **Preset Palettes**: Professional color schemes and themes
- **Automatic Palettes**: Extract colors from images or artwork
- **Palette Import/Export**: Share palettes between projects
- **Palette History**: Recent colors and frequently used colors
- **Color Groups**: Organize related colors within palettes

**Palette Types**:
- **Material Design**: Google Material Design color guidelines
- **Flat Design**: Modern flat design color schemes
- **Vintage**: Retro and vintage color palettes
- **Nature**: Earth tones and natural color schemes
- **Monochromatic**: Single-hue variations
- **Analogous**: Adjacent color harmonies### 3. Adjustment Layers
**Purpose**: Non-destructive color and tonal adjustments with real-time preview.

**Adjustment Types**:
- **Brightness/Contrast**: Basic tonal adjustments
- **Levels**: Histogram-based input/output level control
- **Curves**: Precise tonal curve adjustments
- **Hue/Saturation**: Color-specific adjustments
- **Color Balance**: Shadow/midtone/highlight color correction
- **Vibrance**: Smart saturation enhancement
- **Exposure**: Photographic exposure simulation
- **Shadow/Highlight**: Recover detail in shadows and highlights

**Advanced Adjustments**:
- **Selective Color**: Target specific color ranges
- **Channel Mixer**: Blend color channels creatively
- **Photo Filter**: Color temperature and tint adjustments
- **Black & White**: Intelligent grayscale conversion
- **Gradient Map**: Map tones to gradient colors
- **Color Lookup**: Apply LUT (Look-Up Table) effects

### 4. Filter and Effect System
**Purpose**: Creative and corrective filters for artistic expression and image enhancement.

**Blur Effects**:
- **Gaussian Blur**: Standard blur with radius control
- **Motion Blur**: Directional motion simulation
- **Radial Blur**: Circular/zoom blur effects
- **Surface Blur**: Detail-preserving blur
- **Lens Blur**: Depth-of-field simulation
- **Smart Blur**: Edge-preserving blur

**Stylize Effects**:
- **Oil Paint**: Traditional oil painting simulation
- **Watercolor**: Watercolor painting effect
- **Pencil Sketch**: Graphite drawing simulation
- **Cartoon**: Comic book style effect
- **Pop Art**: High contrast pop art style
- **Posterize**: Reduce color levels for poster effect

**Artistic Filters**:
- **Impressionist**: Impressionist painting style
- **Pointillism**: Dot-based painting technique
- **Cross Hatch**: Pen and ink cross-hatching
- **Charcoal**: Charcoal drawing simulation
- **Pastel**: Soft pastel artwork effect
- **Mosaic**: Tile mosaic effect

## Implementation Architecture

### 1. Color Management System
**Purpose**: Core color handling with accurate color space conversions.

**Color Manager**:
```kotlin
class ColorManager {
    // Color space conversions
    fun rgbToHsv(rgb: IntArray): FloatArray
    fun hsvToRgb(hsv: FloatArray): IntArray
    fun rgbToHsl(rgb: IntArray): FloatArray
    fun hslToRgb(hsl: FloatArray): IntArray
    fun rgbToCmyk(rgb: IntArray): FloatArray
    fun cmykToRgb(cmyk: FloatArray): IntArray
    
    // Color relationships
    fun getComplementaryColor(color: Int): Int
    fun getAnalogousColors(color: Int): List<Int>
    fun getTriadicColors(color: Int): List<Int>
    fun getTetradicColors(color: Int): List<Int>
    
    // Color analysis
    fun extractDominantColors(bitmap: Bitmap, count: Int): List<Int>
    fun calculateColorHarmony(colors: List<Int>): ColorHarmony
    fun getColorTemperature(color: Int): Float
}

data class ColorHarmony(
    val type: HarmonyType,
    val baseColor: Int,
    val colors: List<Int>,
    val harmony: Float // 0.0 to 1.0
)

enum class HarmonyType {
    MONOCHROMATIC,
    ANALOGOUS,
    COMPLEMENTARY,
    TRIADIC,
    TETRADIC,
    SPLIT_COMPLEMENTARY
}
```

### 2. Advanced Color Picker Implementation
**Purpose**: Multi-model color picker with precision and usability.

**Color Picker Components**:
```kotlin
class AdvancedColorPicker : View {
    private var colorModel: ColorModel = ColorModel.HSV
    private var currentColor: Int = Color.BLACK
    private val colorHistory = mutableListOf<Int>()
    
    // Color selection methods
    fun setColorFromWheel(x: Float, y: Float)
    fun setColorFromSliders(values: FloatArray)
    fun setColorFromSwatch(color: Int)
    fun setColorFromEyedropper(x: Float, y: Float, bitmap: Bitmap)
    
    // Color model switching
    fun switchColorModel(model: ColorModel)
    fun getCurrentColorInModel(model: ColorModel): FloatArray
    
    // Color validation and correction
    fun validateColor(color: Int): Int
    fun getContrastRatio(color1: Int, color2: Int): Float
    fun isAccessibleContrast(color1: Int, color2: Int): Boolean
}

enum class ColorModel {
    RGB, HSV, HSL, CMYK, LAB
}
```

### 3. Adjustment Layer System
**Purpose**: Non-destructive color adjustments with parameter control.

**Adjustment Framework**:
```kotlin
abstract class AdjustmentLayer {
    abstract val type: AdjustmentType
    abstract val parameters: Map<String, Float>
    
    abstract fun apply(source: Bitmap): Bitmap
    abstract fun getPreview(source: Bitmap, region: Rect): Bitmap
    abstract fun reset()
    abstract fun getParameterRange(parameter: String): Pair<Float, Float>
}

class BrightnessContrastAdjustment : AdjustmentLayer() {
    override val type = AdjustmentType.BRIGHTNESS_CONTRAST
    private var brightness: Float = 0f // -100 to 100
    private var contrast: Float = 0f   // -100 to 100
    
    override fun apply(source: Bitmap): Bitmap {
        // Implement brightness/contrast adjustment algorithm
    }
}

class CurvesAdjustment : AdjustmentLayer() {
    override val type = AdjustmentType.CURVES
    private val curves = mapOf(
        "master" to CurvePoints(),
        "red" to CurvePoints(),
        "green" to CurvePoints(),
        "blue" to CurvePoints()
    )
    
    override fun apply(source: Bitmap): Bitmap {
        // Implement curves adjustment algorithm
    }
}
```### 4. Filter and Effect Engine
**Purpose**: High-performance image processing with GPU acceleration support.

**Filter Framework**:
```kotlin
abstract class ImageFilter {
    abstract val name: String
    abstract val category: FilterCategory
    abstract val parameters: Map<String, FilterParameter>
    
    abstract fun apply(source: Bitmap, parameters: Map<String, Any>): Bitmap
    abstract fun getPreview(source: Bitmap, region: Rect): Bitmap
    abstract fun supportsGPU(): Boolean
    abstract fun getGPUShader(): String?
}

class GaussianBlurFilter : ImageFilter() {
    override val name = "Gaussian Blur"
    override val category = FilterCategory.BLUR
    
    private var radius: Float = 1.0f
    
    override fun apply(source: Bitmap, parameters: Map<String, Any>): Bitmap {
        radius = parameters["radius"] as? Float ?: 1.0f
        return applyGaussianBlur(source, radius)
    }
    
    override fun supportsGPU(): Boolean = true
    
    override fun getGPUShader(): String {
        return """
            uniform float radius;
            uniform sampler2D inputTexture;
            // GPU shader implementation
        """.trimIndent()
    }
}

enum class FilterCategory {
    BLUR, SHARPEN, STYLIZE, ARTISTIC, DISTORT, NOISE, ADJUST
}
```

## Package Organization

### New Package Structure
```
com.example.drawinggame.ui.drawing.color/
├── core/
│   ├── ColorManager.kt                  # Core color management
│   ├── ColorModel.kt                    # Color model definitions
│   ├── ColorHarmony.kt                  # Color harmony calculations
│   └── ColorValidator.kt                # Color validation and accessibility
├── picker/
│   ├── AdvancedColorPicker.kt           # Multi-model color picker
│   ├── ColorWheelView.kt                # Color wheel component
│   ├── ColorSliderView.kt               # Color slider component
│   ├── ColorSwatchView.kt               # Color swatch grid
│   └── EyedropperTool.kt                # Color sampling tool
├── palette/
│   ├── ColorPalette.kt                  # Palette data model
│   ├── PaletteManager.kt                # Palette management
│   ├── PaletteGenerator.kt              # Automatic palette generation
│   └── PaletteImporter.kt               # Palette import/export
├── adjustments/
│   ├── AdjustmentLayer.kt               # Base adjustment layer
│   ├── BrightnessContrastAdjustment.kt  # Brightness/contrast
│   ├── CurvesAdjustment.kt              # Curves adjustment
│   ├── HueSaturationAdjustment.kt       # Hue/saturation
│   ├── LevelsAdjustment.kt              # Levels adjustment
│   └── ColorBalanceAdjustment.kt        # Color balance
├── filters/
│   ├── ImageFilter.kt                   # Base filter interface
│   ├── BlurFilters.kt                   # Blur effect filters
│   ├── ArtisticFilters.kt               # Artistic style filters
│   ├── StylizeFilters.kt                # Stylization filters
│   └── FilterEngine.kt                  # Filter processing engine
└── ui/
    ├── ColorPickerDialog.kt             # Color picker dialog
    ├── PalettePanel.kt                  # Palette management panel
    ├── AdjustmentPanel.kt               # Adjustment controls
    ├── FilterBrowser.kt                 # Filter selection interface
    └── ColorHistoryView.kt              # Recent colors display
```

## User Interface Design

### 1. Advanced Color Picker Dialog
**Purpose**: Comprehensive color selection with multiple input methods.

**Color Picker Layout**:
```xml
<!-- Advanced Color Picker Dialog -->
<com.google.android.material.card.MaterialCardView
    android:layout_width="400dp"
    android:layout_height="500dp"
    app:cardElevation="8dp">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical">
        
        <!-- Color picker header -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:padding="16dp">
            
            <!-- Current color preview -->
            <View
                android:id="@+id/currentColorPreview"
                android:layout_width="48dp"
                android:layout_height="48dp"
                android:background="@color/black" />
                
            <!-- Previous color -->
            <View
                android:id="@+id/previousColorPreview"
                android:layout_width="48dp"
                android:layout_height="48dp"
                android:layout_marginStart="8dp" />
                
            <View android:layout_weight="1" />
            
            <!-- Color model selector -->
            <com.google.android.material.button.MaterialButtonToggleGroup
                android:id="@+id/colorModelToggle"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content">
                
                <com.google.android.material.button.MaterialButton
                    android:id="@+id/hsvButton"
                    android:text="HSV"
                    style="?attr/materialButtonOutlinedStyle" />
                    
                <com.google.android.material.button.MaterialButton
                    android:id="@+id/rgbButton"
                    android:text="RGB"
                    style="?attr/materialButtonOutlinedStyle" />
                    
            </com.google.android.material.button.MaterialButtonToggleGroup>
            
        </LinearLayout>
        
        <!-- Color picker content -->
        <FrameLayout
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1">
            
            <!-- Color wheel view -->
            <com.example.drawinggame.ui.drawing.color.picker.ColorWheelView
                android:id="@+id/colorWheel"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:layout_margin="16dp" />
                
        </FrameLayout>
        
        <!-- Color sliders -->
        <LinearLayout
            android:id="@+id/colorSlidersContainer"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">
            
            <!-- Dynamic sliders will be added here based on color model -->
            
        </LinearLayout>
        
        <!-- Color history and swatches -->
        <HorizontalScrollView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:padding="16dp">
            
            <com.example.drawinggame.ui.drawing.color.ui.ColorHistoryView
                android:id="@+id/colorHistory"
                android:layout_width="wrap_content"
                android:layout_height="48dp" />
                
        </HorizontalScrollView>
        
        <!-- Dialog actions -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:padding="16dp"
            android:gravity="end">
            
            <com.google.android.material.button.MaterialButton
                android:id="@+id/cancelButton"
                android:text="Cancel"
                style="?attr/materialButtonOutlinedStyle" />
                
            <com.google.android.material.button.MaterialButton
                android:id="@+id/okButton"
                android:text="OK"
                android:layout_marginStart="8dp" />
                
        </LinearLayout>
        
    </LinearLayout>
    
</com.google.android.material.card.MaterialCardView>
```

## Performance Optimization

### 1. Color Processing Optimization
**Purpose**: Efficient color operations and real-time preview rendering.

**Optimization Strategies**:
- **Color Space Caching**: Cache color conversions for frequently used colors
- **GPU Acceleration**: Use RenderScript or OpenGL for intensive operations
- **Incremental Updates**: Update only changed regions during adjustments
- **Background Processing**: Process large adjustments on background threads
- **Memory Management**: Efficient bitmap handling for large images

### 2. Filter Performance
**Purpose**: Real-time filter preview with minimal performance impact.

**Filter Optimization**:
```kotlin
class FilterPerformanceManager {
    private val filterCache = LruCache<String, Bitmap>(20)
    private val previewSize = 512 // Maximum preview dimensions
    
    fun getFilterPreview(filter: ImageFilter, source: Bitmap): Bitmap {
        val cacheKey = "${filter.name}_${filter.parameters.hashCode()}"
        
        return filterCache.get(cacheKey) ?: run {
            val scaledSource = scaleForPreview(source)
            val result = filter.apply(scaledSource, filter.parameters)
            filterCache.put(cacheKey, result)
            result
        }
    }
    
    private fun scaleForPreview(source: Bitmap): Bitmap {
        val maxDimension = maxOf(source.width, source.height)
        if (maxDimension <= previewSize) return source
        
        val scale = previewSize.toFloat() / maxDimension
        val newWidth = (source.width * scale).toInt()
        val newHeight = (source.height * scale).toInt()
        
        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true)
    }
}
```

## Integration Timeline

### Phase 5.4 Development Plan
**Estimated Time**: 20-25 hours
**Prerequisites**: Phase 5.3 complete
**Deliverables**:
1. Advanced color management system with multiple color models
2. Professional color picker with harmony generation
3. Non-destructive adjustment layer system
4. Filter and effect engine with GPU acceleration
5. Complete UI integration with existing drawing system
6. Performance optimization for real-time color operations

### Development Milestones
1. **Milestone 1** (5-6 hours): Core color management and conversion system
2. **Milestone 2** (4-5 hours): Advanced color picker implementation
3. **Milestone 3** (4-5 hours): Adjustment layer system
4. **Milestone 4** (3-4 hours): Filter and effect framework
5. **Milestone 5** (2-3 hours): UI integration and performance optimization
6. **Milestone 6** (2-2 hours): Testing and refinement

## Testing Strategy

### Unit Tests
- Color space conversion accuracy
- Color harmony generation algorithms
- Adjustment layer mathematical operations
- Filter processing correctness

### Integration Tests
- Color picker integration with drawing system
- Adjustment layer interaction with layer system
- Filter performance on various image sizes
- UI responsiveness during color operations

### Visual Tests
- Color accuracy across different devices
- Adjustment preview accuracy
- Filter effect quality validation
- Accessibility compliance for color interfaces

## Future Enhancement Opportunities

### Advanced Features
- **Color Profiles**: ICC color profile support
- **Gamut Mapping**: Color space gamut visualization
- **Color Blindness**: Simulation and correction tools
- **Advanced Harmonies**: Complex color relationship algorithms
- **AI Color**: Machine learning-based color suggestions

---

**Phase 5 Summary**: With the completion of Phase 5.4, Social Sketch will have transformed from a basic drawing app into a professional-grade digital art creation platform with advanced brushes, layer management, precise selection tools, and comprehensive color management. This establishes the foundation for social features and cloud integration in subsequent phases.