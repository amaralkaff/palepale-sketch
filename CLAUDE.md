# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 🎨 Project Overview

**Social Sketch: The Daily Challenge App** - A community-focused drawing application that transforms creative expression into daily adventures. Originally a native OpenGL game, now evolving into a modern Android drawing app with social features.

### Current Project Status
- **Phase 1 COMPLETED** ✅: Project setup and foundation complete
- **Phase 2 COMPLETED** ✅: UI Foundation with Material Design 3 and Navigation Component
- **Architecture**: Fragment-based navigation with Material Design 3 theming
- **Migration Status**: Native components preserved for gradual transition
- **Next Phase**: Phase 3 (Assets) - Icons, graphics, and content integration

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

## 📋 IMPORTANT: Always Check Spec Folder First

**Before starting any work, ALWAYS:**
1. Read the relevant specification files in `/spec/` folder
2. Check phase dependencies and prerequisites
3. Follow the step-by-step implementation guides
4. Verify rollback procedures are understood

### Spec Folder Structure
```
/spec/
├── phase1-setup/          # Project structure & dependencies
├── phase2-ui-foundation/   # Material Design & navigation
├── phase3-assets/          # Icons, graphics & content
├── phase4-drawing-basic/   # Core drawing functionality
├── phase5-drawing-advanced/# Enhanced drawing features
├── phase6-data-layer/      # Local database & repositories
├── phase7-firebase-basic/  # Authentication & cloud storage
├── phase8-social-features/ # Community & social interactions
├── phase9-prompts/         # Daily prompt system
└── phase10-advanced/       # Time-lapse & sharing features
```

## 🏗️ Architecture Evolution

### Target Architecture (Social Sketch App)
- **UI Layer**: Fragment-based navigation with Material Design 3
- **Drawing Engine**: Custom Canvas views with touch handling and brush systems
- **Social Layer**: Firebase-backed community features with real-time updates
- **Data Layer**: Room database with Repository pattern for offline-first approach
- **Asset Management**: Professional icons, UI graphics, and drawing textures

### Legacy Components (Being Migrated)
- **Native C++ Layer**: OpenGL ES 3.0 rendering (gradual deprecation)
- **GameActivity**: Being replaced with standard AppCompatActivity
- **Native Assets**: Repurposed for drawing textures and UI elements

### Key New Components
- **DrawingFragment**: Main drawing interface with tool palette
- **GalleryFragment**: Community artwork showcase
- **ProfileFragment**: User profiles and statistics
- **HomeFragment**: Daily prompts and navigation hub
- **DrawingView**: Custom view for touch-based drawing

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