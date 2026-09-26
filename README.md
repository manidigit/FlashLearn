# 🎓 FlashLearn - Spanish/Persian Vocabulary Learning App

**Version:** 6.50 | **Theme System:** 3.0-GTP | **Date:** 2026-09-26

---

## 📱 About

FlashLearn is a comprehensive offline language learning app for Spanish-Persian vocabulary using spaced repetition algorithm.

### Features
- ✅ Spaced repetition learning
- ✅ Quiz mode (4-choice)
- ✅ Statistics dashboard
- ✅ Multiple themes (GROK, CLAUD, Modern Minimal, etc)
- ✅ Offline-first architecture
- ✅ Room database
- ✅ Jetpack Compose UI

---

## 📊 Version 6.50 - GTP Theme Runtime Correction

### 🎨 Theme System
- Added **GTP** as the fifth built-in theme.
- GTP is not color-only: it owns compact spacing, sharper corners, stronger elevation, filled icons, typography scaling, density, and a violet/cyan visual language.
- Theme picker consumes the built-in registry, so GTP is exposed automatically through Settings.
- Theme persistence continues through the existing AppViewModel preference path.
- Theme JSON format advanced to v3 while remaining backward-compatible with v2 custom themes.

### 🧪 Verification
- Added JVM contract coverage for the five-theme catalog and GTP token values.
- Added instrumentation coverage for GTP spacing, shape, and icon-token binding.
- GitHub Actions remains the authoritative build, unit-test, instrumentation, APK, and upgrade-gate verifier.

## 🛠️ Tech Stack

- **Language:** Kotlin
- **Framework:** Jetpack Compose
- **Database:** Room
- **DI:** Hilt
- **Architecture:** MVVM + Repository Pattern
- **Min SDK:** 24
- **Target SDK:** 34
- **Compile SDK:** 34

---

## 📁 Project Structure

```
FlashLearn-main/
├── app/
│   └── src/
│       └── main/
│           └── java/com/flashlearn/app/
│               ├── ui/
│               │   ├── theme/
│               │   │   ├── FlashLearnTheme.kt
│               │   │   ├── FlashLearnThemeSpec.kt
│               │   │   └── FlashLearnThemeTokens.kt
│               │   ├── review/
│               │   │   └── ReviewScreen.kt
│               │   ├── library/
│               │   │   ├── LibraryScreen.kt
│               │   │   └── LibraryDetailScreen.kt
│               │   ├── about/
│               │   │   └── AboutScreen.kt
│               │   └── [other screens]
│               └── domain/
│                   ├── model/
│                   └── usecase/
│
├── CHANGELOG.md
├── DOCUMENTATION/
│   ├── PATTERN_GUIDE_COMPLETE.md
│   ├── IMPLEMENTATION_GUIDE.md
│   └── [other docs]
└── README.md
```

---

## 🎯 Theme System

### Available Themes (9 Total)
1. **GROK** - Luxury Gold Dark (New)
2. **CLAUD** - Modern Minimalist (New)
3. MODERN_MINIMAL
4. MODERN_PURPLE
5. OCEAN_BLUE
6. FRESH_GREEN
7. SUNSET_ORANGE
8. MIDNIGHT
9. FOREST

### Switching Themes
Go to Settings → Appearance → Select Theme

All UI updates automatically.

---

## 🚀 Getting Started

### Build
```bash
./gradlew build
```

### Run
```bash
./gradlew installDebug
```

### Run Tests
```bash
./gradlew test
```

---

## 📖 Theme System Guide

### For Developers

**New Screens:**
1. Read: `DOCUMENTATION/PATTERN_GUIDE_COMPLETE.md`
2. Reference: `LibraryScreen_REFACTORED.kt`
3. Apply patterns
4. Test theme switching

**Remaining Screens (10):**
- Can be fixed using provided patterns
- Expected time: 1-2 hours
- All documentation included

### Key Tokens
```kotlin
// Spacing
tokens.screenPadding        // 20.dp
tokens.contentGap          // 12.dp
tokens.compactGap          // 8.dp
tokens.controlHeight       // 52.dp

// Shapes
MaterialTheme.shapes.small      // 12.dp
MaterialTheme.shapes.medium     // 16.dp
MaterialTheme.shapes.large      // 24.dp

// Colors
MaterialTheme.colorScheme.background
MaterialTheme.colorScheme.surface
MaterialTheme.colorScheme.primary
```

---

## ✅ Verification

### Theme Switching Test
1. Open Settings
2. Change theme to GROK
3. Navigate all screens
4. Verify: Colors, Corners, Spacing all changed
5. Change to CLAUD
6. Verify again

### Fixed Screens (v6.25)
- ✅ NeedsReviewScreen
- ✅ AboutScreen
- ✅ LibraryDetailScreen
- ✅ All use MaterialTheme tokens
- ✅ All respond to theme changes

---

## 📋 Architecture

### Models
- `Concept` - Vocabulary item
- `ReviewQueueItem` - Item for review
- `VocabularyDifficulty` - Difficulty levels

### Repositories
- `ConceptRepository` - Vocabulary data
- `ContentRepository` - Translations
- `CategoryRepository` - Categories

### Use Cases
- Learning algorithm
- Spaced repetition
- Difficulty calculation

---

## 🐛 Known Issues & TODOs

### Fixed (v6.25)
- ✅ Theme system ColorScheme not applied (7 screens)
- ✅ Hardcoded DPs preventing dynamic spacing (47 instances)
- ✅ Cards without proper shapes (6 screens)

### Remaining
- 10 screens need pattern application
- See: IMPLEMENTATION_GUIDE.md

---

## 📝 License

FlashLearn © 2026 - All rights reserved

---

## 👨‍💻 Contributors

- **AI Assistant:** Theme System Design & Audit
- **Date:** September 25, 2026

---

## 🔄 Recent Changes

### v6.26 (2026-09-25)
- Review answer persistence and quiz generation moved off the UI thread
- Per-answer full ReviewHistory scan removed
- Review queue/count selection now uses LearningState.lastReviewedAt for same-day exclusion
- Library search is debounced and stale refresh jobs are cancelled
- Runtime/CI identity advanced to 6.26 / 626

### v6.24 (2026-09-24)
- Initial Release
- Base Theme System
- 9 Pre-built Themes

---

## 📞 Support

**Documentation:**
- `CHANGELOG.md` - Version history
- `DOCUMENTATION/PATTERN_GUIDE_COMPLETE.md` - Patterns
- `DOCUMENTATION/IMPLEMENTATION_GUIDE.md` - How-to guide

---

**Build Status:** ✅ Ready for GitHub
**Last Updated:** 2026-09-25
**Version:** 6.26

