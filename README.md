# FlashLearn

Offline Spanish/Persian vocabulary learning app built with Kotlin, Jetpack Compose, Room, Hilt and a multi-module Clean Architecture.

**Current version:** 6.82 (versionCode 682)

> **Documentation reconciliation — 2026-10-03:** The Android release identity remains **6.82 / 682**. The `main` branch contains additional post-6.82 commits that have not been promoted to a new Android release identity yet. These changes are tracked in `docs/POST_V6.82_DOCUMENTATION_RECONCILIATION.md`. A parallel Vite/React web scaffold is also present in `main`; its presence is documented separately and does not change the Android release identity.



## Core features

- Spaced-repetition review: Daily → Weekly → Monthly → Learned
- Independent vocabulary difficulty: Easy → Medium → Hard → Very Hard
- Flashcard and 4-choice Quiz review
- Bulk vocabulary import with parser warnings and review queue
- Categories, favorites, tags and duplicate cleanup
- Progress, statistics and achievements\n- Home review-ready counts separated from total words in each Learning stage
- Review session language-pair routing, explicit flashcard next-card/quiz-skip controls, and same-day eligibility aligned with the active review queue
- Offline-first Room database
- Multiple built-in themes with a single ThemeDesign runtime source for layout, navigation, icons, spacing, shapes, typography, density, elevation and adaptive breakpoints; custom theme JSON compatibility is preserved
- Backup/restore with legacy-format compatibility and combined multi-type backup bundles

## Architecture

- `app` — Android UI, Compose screens, ViewModels and navigation
- `domain` — algorithms, parser, models, repositories and use cases
- `data` — Room-backed repository implementations and backup/restore
- `database` — Room entities, DAOs and migrations
- `core` — Hilt dependency-injection bindings
- Theme system — FlashLearnThemeSpec → ThemeDesign → FlashLearnThemeTokens is the single runtime visual path; legacy top-level visual fields are compatibility-only

## Localization and adaptive UI\n\n- User-facing UI text belongs in Android string resources; Room stores user-created vocabulary/content, not static UI copy.\n- The active ThemeDesign owns adaptive breakpoints. Screens reflow from available width rather than targeting a specific phone model.\n- Material dynamic color is intentionally disabled so wallpaper colors cannot silently override a selected FlashLearn theme; every visual decision remains theme-owned.\n\n## Build and test

```bash
./gradlew assembleDebug
./gradlew test
./gradlew connectedDebugAndroidTest
./gradlew assembleRelease
```

GitHub Actions is the authoritative CI gate for debug build, unit tests, instrumentation tests and the previous-version upgrade path. Database migration tests cover the legacy v1→v7 path, including the v5→v6 contents-index contract.

## Review rules

Learning and difficulty are independent systems. A concept reviewed once on the current local calendar day is excluded from subsequent review that day. Correct answers advance the learning stage; incorrect answers return non-learned concepts to Daily. Difficulty changes only after the configured consecutive-answer threshold. Home distinguishes between words ready for review now and the total words assigned to each Learning stage; these totals are consistent with Statistics.

## Repository hygiene

Generated reports, audit archives, obsolete design-system documentation and duplicate screen implementations are kept out of the production source tree. Release builds use R8 code shrinking and resource shrinking.

## License

FlashLearn © 2026
