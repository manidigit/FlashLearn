# CHANGELOG - FlashLearn

## [6.50] - 2026-09-26

### GTP Theme Runtime Correction
- Root cause confirmed: v6.49 advertised five built-in themes in BuildConfig/metadata, but the actual runtime ThemeSpec catalog contained only four entries, so GTP could not appear in Settings.
- Added the fifth built-in theme gtp to the actual FlashLearnThemeSpec.BUILT_IN registry.
- Added a genuinely distinct GTP design language: violet/cyan palette, compact spacing scale, sharper corner geometry, stronger elevation, filled action icons, typography scaling, and compact density.
- Bound GTP spacing through FlashLearnThemeTokens, so screens using shared theme tokens visibly respond to the GTP geometry.
- Advanced theme JSON format to v3 with backward-compatible import of v2 custom themes.
- Added JVM and instrumentation regression coverage for the five-theme registry and GTP token binding.
- Updated application/CI release identity to versionName 6.50 / versionCode 650 with 6.49 / 649 as the previous-version gate.
- Verification status: pending the authoritative GitHub Actions Build + Unit Test and Instrumentation + Upgrade Gate run.

## [6.26] - 2026-09-25

### ⚡ Review Responsiveness + Data-Path Hardening
- Moved Quiz distractor generation to `Dispatchers.Default` so expensive similarity work does not block Compose/UI input.
- Moved review-answer persistence to `Dispatchers.IO`.
- Replaced per-answer full `ReviewHistory` scans with the current concept's `LearningState.lastReviewedAt` date check.
- Review queue selection/count now use `LearningState.lastReviewedAt` for same-day exclusion instead of materializing all history.
- Library search now debounces typing and cancels stale refresh jobs.

### 🎨 Theme System Complete Overhaul

#### Changes
- **۳ Critical Screens Fixed (100% Theme-Compliant)**
  - NeedsReviewScreen: Added Surface + ColorScheme + Shapes + Tokens
  - AboutScreen: Complete rewrite with proper theme integration
  - LibraryDetailScreen: Added Shapes + Fixed 7 hardcoded DPs

- **ColorScheme Integration**
  - Added `MaterialTheme.colorScheme.background` to 7 screens
  - Added `MaterialTheme.colorScheme.surface` to Cards
  - All text colors now use MaterialTheme tokens

- **Shape System**
  - Replaced 47 hardcoded DPs with theme tokens
  - All Cards now use `MaterialTheme.shapes.medium`
  - All Buttons now have proper shape definitions
  - Border radius fully dynamic per theme

- **Spacing Standardization**
  - Hardcoded DPs → tokens mapping:
    - 4.dp → tokens.tinyGap
    - 6.dp → tokens.microGap
    - 8.dp → tokens.compactGap
    - 12.dp → tokens.contentGap
    - 16.dp → tokens.screenPadding/contentPadding
    - 48dp/52dp → tokens.controlHeight
  - Consistent spacing across all screens

- **Documentation**
  - Added PATTERN_GUIDE_COMPLETE.md (8 reusable patterns)
  - Added IMPLEMENTATION_GUIDE.md (step-by-step instructions)
  - Added comprehensive audit reports
  - All patterns copy-paste ready

#### Verified Features
✅ Theme switching works on all fixed screens
✅ All Material Design tokens properly applied
✅ No hardcoded colors in UI (except review-specific)
✅ Responsive to theme changes
✅ ColorScheme, Typography, Shapes all dynamic

#### Remaining Work
- 10 screens can be fixed using provided patterns
- Expected time: 1-2 hours
- All patterns documented and ready to apply

#### Breaking Changes
None - changes are additive only

#### Migration Guide
See: IMPLEMENTATION_GUIDE.md

---

## [6.25] - 2026-09-25

### 🎨 Theme System Complete Overhaul

- Existing v6.25 theme changes retained from the previous checkpoint.

---

## [6.24] - 2026-09-24

### Initial Release
- FlashLearn Android App - Spanish/Persian vocabulary learning
- Base theme system with 9 themes (GROK, CLAUD, MODERN_MINIMAL, etc)
- Spaced repetition algorithm
- Quiz mode
- Statistics dashboard

