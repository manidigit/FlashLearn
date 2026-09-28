# FlashLearn

Offline Spanish/Persian vocabulary learning app built with Kotlin, Jetpack Compose, Room, Hilt and a multi-module Clean Architecture.

**Current version:** 6.55 (versionCode 655)

## Core features

- Spaced-repetition review: Daily → Weekly → Monthly → Learned
- Independent vocabulary difficulty: Easy → Medium → Hard → Very Hard
- Flashcard and 4-choice Quiz review
- Bulk vocabulary import with parser warnings and review queue
- Categories, favorites, tags and duplicate cleanup
- Progress, statistics and achievements\n- Home review-ready counts separated from total words in each Learning stage
- Offline-first Room database
- Multiple built-in themes with theme-owned layout, navigation, icon and spacing profiles; custom theme JSON compatibility is preserved
- Backup/restore with legacy-format compatibility

## Architecture

- `app` — Android UI, Compose screens, ViewModels and navigation
- `domain` — algorithms, parser, models, repositories and use cases
- `data` — Room-backed repository implementations and backup/restore
- `database` — Room entities, DAOs and migrations
- `core` — Hilt dependency-injection bindings
- Theme system — FlashLearnThemeSpec owns visual parameters; screens consume FlashLearnThemeTokens rather than defining theme-specific geometry

## Build and test

```bash
./gradlew assembleDebug
./gradlew test
./gradlew connectedDebugAndroidTest
./gradlew assembleRelease
```

GitHub Actions is the authoritative CI gate for debug build, unit tests, instrumentation tests and the previous-version upgrade path.

## Review rules

Learning and difficulty are independent systems. A concept reviewed once on the current local calendar day is excluded from subsequent review that day. Correct answers advance the learning stage; incorrect answers return non-learned concepts to Daily. Difficulty changes only after the configured consecutive-answer threshold. Home distinguishes between words ready for review now and the total words assigned to each Learning stage; these totals are consistent with Statistics.

## Repository hygiene

Generated reports, audit archives, obsolete design-system documentation and duplicate screen implementations are kept out of the production source tree. Release builds use R8 code shrinking and resource shrinking.

## License

FlashLearn © 2026
