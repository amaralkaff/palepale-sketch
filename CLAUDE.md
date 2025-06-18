# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 🎨 Project Overview

**Social Sketch: The Daily Challenge App** - A community-focused drawing application that transforms creative expression into daily adventures. Originally a native OpenGL game, now evolving into a modern Android drawing app with social features.

### Current Project Status
- **Phase 1 COMPLETED** ✅: Project setup and foundation complete
- **Phase 2 COMPLETED** ✅: UI Foundation with Material Design 3 and Navigation Component
- **Phase 3 COMPLETED** ✅: Assets integration with professional icons, graphics, and content
- **Phase 4 READY** 🔄: Drawing Basic specifications complete, ready for implementation
- **Architecture**: Fragment-based navigation with Material Design 3 theming
- **Migration Status**: Native components preserved for gradual transition
- **Next Phase**: Phase 4 (Drawing Basic) - Core drawing functionality implementation
- **Critical Gap**: Phase 5-10 specifications missing (directories exist but no .md files)

### Phase 1 Implementation Summary
✅ **Phase 1.1**: Project structure reorganized with proper Android package hierarchy  
✅ **Phase 1.2**: Dependencies updated with Material Design 3, Navigation, Room, etc.  
✅ **Phase 1.3**: AndroidManifest updated with Social Sketch permissions and activities  
✅ **Phase 1.4**: MainActivity refactored from GameActivity to AppCompatActivity with ViewBinding

### Phase 2 Implementation Summary
✅ **Phase 2.1**: Material Design 3 theming system with Claude.ai colors and MD3 components  
✅ **Phase 2.2**: Navigation Component setup with fragment graph and smooth animations  
✅ **Phase 2.3**: Fragment layouts created with placeholder content for all major screens  
✅ **Phase 2.4**: SKIPPED - Advanced theming deferred to maintain existing design system

### Phase 3 Implementation Summary (COMPLETED)
✅ **Phase 3.1**: Asset sources documentation and licensing information (1 hour)  
✅ **Phase 3.2**: Professional icon integration with 17+ vector drawables (3 hours)  
✅ **Phase 3.3**: UI elements, graphics, textures, and animations integration (2-3 hours)  
✅ **Phase 3.4**: Rich placeholder content, mock data, and localization structure (2 hours)  
✅ **Total Phase 3 Actual**: 8-9 hours (efficient implementation)

### Phase 4 Specification Summary (Ready for Implementation)
📋 **Phase 4.1**: Core drawing architecture with DrawingView and DrawingEngine (8-12 hours)  
📋 **Phase 4.2**: Basic brush system with pen, brush, eraser tools (6-8 hours)  
📋 **Phase 4.3**: Touch handling and gesture recognition for drawing (8-10 hours)  
📋 **Phase 4.4**: Canvas operations including undo/redo, clear, save/load (10-12 hours)  
📋 **Total Phase 4 Estimate**: 32-42 hours

## 📋 IMPORTANT: Always Check Spec Folder First

**Before starting any work, ALWAYS:**
1. Read the relevant specification files in `/spec/` folder
2. Check phase dependencies and prerequisites
3. Follow the step-by-step implementation guides
4. Verify rollback procedures are understood

### Spec Folder Structure
```
/spec/
├── phase1-setup/          # ✅ COMPLETE - Project structure & dependencies (4 files)
├── phase2-ui-foundation/   # ✅ COMPLETE - Material Design & navigation (4 files)
├── phase3-assets/          # ✅ COMPLETE - Icons, graphics & content (4 files)
├── phase4-drawing-basic/   # 📋 READY - Core drawing functionality (4 files)
├── phase5-drawing-advanced/# ❌ MISSING - Enhanced drawing features (0 files)
├── phase6-data-layer/      # ❌ MISSING - Local database & repositories (0 files)
├── phase7-firebase-basic/  # ❌ MISSING - Authentication & cloud storage (0 files)
├── phase8-social-features/ # ❌ MISSING - Community & social interactions (0 files)
├── phase9-prompts/         # ❌ MISSING - Daily prompt system (0 files)
└── phase10-advanced/       # ❌ MISSING - Time-lapse & sharing features (0 files)
```

**Phase Dependencies:**
- Phase 3 → Phase 2 (COMPLETED ✅)
- Phase 4 → Phase 3 (Ready to implement 📋)
- Phase 5 → Phase 4 (Blocked: Phase 4 implementation needed)
- Phase 6 → Phase 4 (Blocked: specifications needed)
- Phase 7+ → Phase 6 (Blocked: specifications needed)

## 🏗️ Architecture Evolution

### Target Architecture (Social Sketch App)
- **UI Layer**: Fragment-based navigation with Material Design 3 ✅
- **Drawing Engine**: Custom Canvas views with touch handling and brush systems (Phase 4-5)
- **Social Layer**: Firebase-backed community features with real-time updates (Phase 7-8)
- **Data Layer**: Room database with Repository pattern for offline-first approach (Phase 6)
- **Asset Management**: Professional icons, UI graphics, and drawing textures (Phase 3)

### Legacy Components (Being Migrated)
- **Native C++ Layer**: OpenGL ES 3.0 rendering (gradual deprecation)
- **GameActivity**: Replaced with standard AppCompatActivity ✅
- **Native Assets**: Repurposed for drawing textures and UI elements

### Key New Components
- **DrawingFragment**: Main drawing interface with tool palette (Phase 4)
- **GalleryFragment**: Community artwork showcase ✅
- **ProfileFragment**: User profiles and statistics ✅
- **HomeFragment**: Daily prompts and navigation hub ✅
- **DrawingView**: Custom view for touch-based drawing (Phase 4)

## 🚀 Development Commands

### Building
```bash
./gradlew build                    # Build debug and release APKs
./gradlew assembleDebug           # Build debug APK only
./gradlew assembleRelease         # Build release APK only
```

### Running
```bash
./gradlew installDebug            # Install debug APK to connected device
./gradlew installDebug && adb shell am start -n com.example.drawinggame/.MainActivity
```

### Testing
```bash
./gradlew test                    # Run unit tests
./gradlew connectedAndroidTest    # Run instrumented tests on device
./gradlew testDebugUnitTest       # Run unit tests for debug build
```

### Linting & Type Checking
```bash
./gradlew lint                    # Run Android lint checks
./gradlew ktlintCheck            # Run Kotlin style checks (if configured)
```

### Cleaning
```bash
./gradlew clean                   # Clean all build artifacts
```

## ⚙️ Technical Configuration

### Current Setup
- **Target SDK**: Android 35 (Android 15)
- **Minimum SDK**: Android 35  
- **Build Tools**: Android Gradle Plugin 8.9.1
- **Kotlin**: 2.0.21
- **Namespace**: `com.example.drawinggame`

### Dependencies (Post-Migration)
- **UI**: Material Design 3, Navigation Component, ViewBinding
- **Architecture**: ViewModel, LiveData, Room Database
- **Backend**: Firebase (Auth, Firestore, Storage, Messaging)
- **Graphics**: Custom Canvas drawing, Glide for image loading
- **Testing**: JUnit 4, AndroidX Test, Espresso

### Legacy Dependencies (During Transition)
- **NDK**: CMake 3.22.1 (maintained for gradual migration)
- **OpenGL**: OpenGL ES 3.0 (being phased out)
- **Native Libraries**: EGL, GLESv3, game-activity

## 📁 Project Structure

### New Structure (Target)
```
app/src/main/
├── java/com/example/drawinggame/
│   ├── ui/
│   │   ├── activities/          # MainActivity, DrawingActivity
│   │   ├── fragments/           # Home, Gallery, Profile, Drawing
│   │   └── views/              # Custom DrawingView
│   ├── data/
│   │   ├── models/             # User, Drawing, Prompt data classes
│   │   ├── repositories/       # Data access abstraction
│   │   └── database/           # Room database components
│   ├── viewmodels/             # ViewModel classes
│   ├── utils/                  # Utility classes and helpers
│   └── services/               # Background services
├── res/
│   ├── layout/                 # Fragment and activity layouts
│   ├── drawable/               # Vector icons and graphics
│   ├── values/                 # Themes, colors, strings
│   └── navigation/             # Navigation graphs
└── assets/
    ├── drawing-tools/          # Brush textures and patterns
    ├── prompts/                # Daily prompt images
    └── ui-elements/            # UI graphics and textures
```

### Legacy Structure (Maintained During Transition)
```
app/src/main/
├── cpp/                        # Native C++ code (gradual deprecation)
│   ├── main.cpp               # Native entry point
│   ├── Renderer.cpp           # OpenGL renderer
│   └── CMakeLists.txt         # Native build config
└── assets/                    # Original game assets (repurposed)
```

## 🎯 Key Development Guidelines

### Implementation Workflow
1. **Always check `/spec/` folder** for phase-specific instructions
2. **Follow incremental approach** - implement one phase at a time
3. **Test thoroughly** after each phase completion
4. **Maintain backward compatibility** during transition
5. **Document any deviations** from spec files

### Code Quality Standards
- **Material Design 3** compliance for all UI components
- **MVVM architecture** with Repository pattern
- **Offline-first** approach with Room database
- **Proper error handling** with user-friendly messages
- **Accessibility support** with content descriptions and proper contrast

### Safety Practices
- **Version control**: Commit after each successful phase
- **Rollback procedures**: Each spec file includes rollback instructions
- **Testing**: Verify builds and basic functionality after changes
- **Gradual migration**: Keep legacy code until new features are stable

## 🎨 Social Sketch App Features

### Core Features (Planned Implementation)
- **Daily Drawing Prompts**: Curated creative challenges
- **Drawing Tools**: Brush, pen, eraser, spray paint with size/opacity controls
- **Community Gallery**: Share and discover artwork
- **Social Interactions**: Like, comment, and follow artists
- **User Profiles**: Showcase personal artwork collections
- **Time-lapse Recording**: Capture and share drawing process
- **Offline Support**: Create and save drawings without internet

### Technical Features
- **Real-time Sync**: Firebase integration for live updates
- **Image Processing**: Canvas export and social sharing
- **Push Notifications**: Daily prompt reminders and social updates
- **Analytics**: Track user engagement and popular content
- **Moderation**: Community safety and content guidelines

## 📊 Current Implementation Status & Next Steps

### ✅ Completed Phases (Phases 1-3)
**Total Development Time**: ~25-30 hours  
**Status**: Fully implemented and tested

- **Project Foundation**: Modern Android architecture with proper package structure
- **UI Foundation**: Material Design 3 theming, Navigation Component, fragment layouts
- **Asset Integration**: Professional icons, graphics, rich content, and mock data
- **Current Capabilities**: 
  - Modern Android app structure with fragments
  - Material Design 3 theming system
  - Navigation between Home, Gallery, Profile, Settings screens
  - Professional icon system with 17+ vector drawables
  - Enhanced UI graphics, backgrounds, and animations
  - Rich placeholder content and mock data for testing
  - Complete asset management and licensing documentation

### 🔄 Ready for Implementation (Phase 4)
**Estimated Time**: 32-42 hours  
**Prerequisites**: Phase 3 complete ✅  
**Specifications**: Complete and detailed ✅

- **Phase 4.1**: Core drawing architecture with DrawingView and DrawingEngine
- **Phase 4.2**: Basic brush system with pen, brush, eraser tools  
- **Phase 4.3**: Touch handling and gesture recognition for drawing
- **Phase 4.4**: Canvas operations including undo/redo, clear, save/load

### ❌ Critical Gaps (Phases 5-10)
**Blocker**: No specification files exist for advanced functionality phases  
**Impact**: Cannot proceed with advanced features without Phase 5+ specs

**Missing Specifications:**
- **Phase 5 (Drawing Advanced)**: Enhanced drawing features and tools
- **Phase 6 (Data Layer)**: Room database and repositories - PREREQUISITE for social features
- **Phase 7-10**: Firebase, social features, prompts, advanced features

### 🎯 Recommended Action Plan

**Immediate Priority:**
1. **Execute Phase 4** - Core drawing functionality (specs complete, ready to implement)
2. **Create Phase 5 Specifications** - Advanced drawing features (layers, effects, etc.)
3. **Design Phase 6 Specifications** - Data layer architecture (prerequisite for social features)

**Development Path:**
- Phase 4 → Phase 5 → Phase 6 → Phase 7+ (social features)
- Phase 4 implementation will provide foundation for all subsequent phases

**Quality Assurance:**
- All phases include comprehensive verification steps
- Rollback procedures documented for safe development
- Backward compatibility maintained during legacy code migration