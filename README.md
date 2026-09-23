# FlashLearn

FlashLearn is an Android vocabulary-learning application built with Kotlin and Jetpack Compose. It combines personal vocabulary management, configurable review sessions, progress tracking, backup/restore, and customizable appearance and language-direction settings in a single app.

## Highlights

- Vocabulary library with single-word and bulk import workflows
- Review sessions with configurable review modes and difficulty settings
- Progress and learning statistics
- Personal word difficulty and quiz difficulty controls
- Category-based vocabulary organization
- Backup and restore support
- Material 3 UI with configurable themes, appearance, accent color, and layout direction
- Help and About sections
- Android UI and instrumentation tests
- GitHub Actions CI for build, unit-test, instrumentation-test, APK verification, and upgrade-path checks

## Technology

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** Android application with separated `app`, `core`, `domain`, `data`, and `database` modules
- **Dependency injection:** Hilt
- **Database:** Room
- **Async:** Kotlin Coroutines
- **Build:** Gradle / Android Gradle Plugin
- **Target SDK:** Android 34
- **Minimum SDK:** Android 26
- **Java:** 17

## Project structure

~~~text
FlashLearn/
├── app/        # Android application, UI, navigation, ViewModels and tests
├── core/       # Shared/core application functionality
├── domain/     # Domain models and business logic
├── data/       # Data/repository layer
└── database/   # Room database layer
~~~

## Current release

**Version:** 6.15  
**Version code:** 115

The repository uses GitHub Actions as the release/build verification gate. Version 6.15 records the Review RTL/LTR regression correction and its process/verification checkpoint. The CI workflow builds the debug APK, runs unit tests, runs instrumentation tests, verifies the APK, and checks the previous-version upgrade path.

## Building

The project is designed to build with Gradle using JDK 17 and the Android SDK.

Typical commands:

~~~bash
gradle assembleDebug
gradle test
gradle connectedDebugAndroidTest
~~~

For CI, the repository workflow provisions the required Android SDK components and uses a stable CI debug keystore.

## Development notes

FlashLearn uses Material 3 as its UI foundation. Project-specific visual values are centralized through the FlashLearn theme/token layer rather than creating a separate parallel design system.

The app also supports a global layout direction setting. Screens should respect the application's configured RTL/LTR direction instead of imposing a local direction unless a specific component requires it.

## Continuous Integration

The Android CI workflow is located at:

~~~text
.github/workflows/android-ci.yml
~~~

The pipeline covers:

1. Source checkout and Java/Gradle setup
2. Android SDK installation
3. Debug keystore preparation
4. Clean build
5. Debug APK build
6. Unit tests
7. APK signature and version verification
8. Instrumentation tests
9. Previous-version upgrade verification
10. Source/APK artifact staging

## Repository

[GitHub](https://github.com/manidigit/FlashLearn)

## Status

FlashLearn is under active development. Version history, progress checkpoints, and release verification records are maintained in the repository documentation.