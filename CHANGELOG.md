## v6.53 — Independent Learning + Difficulty systems
- Advanced application identity to **6.53 / versionCode 653**; previous-version gate is **6.52 / 652**.
- Learning is now only: **Daily → Weekly → Monthly → Learned**.
- Learning correct: one stage forward; wrong: return to Daily.
- Difficulty is a separate system: **Easy → Medium → Hard → Very Hard**.
- Difficulty uses only consecutive correct/wrong answers plus the Settings threshold; default threshold remains **3**.
- Removed the special Weekly/Monthly Difficulty escalation rules from the executable algorithm.
- Difficulty no longer consumes Learning Stage, Monthly wrong count, or path-failure state.
- Difficulty boundaries remain hard-capped at Easy and Very Hard.
- Regression tests now explicitly protect the independence contract.
- Legacy Learning metadata fields remain persisted only for data/backward compatibility; they are not used to drive Difficulty.

### Verification gate
GitHub Actions is authoritative. v6.53 is not considered verified until Build + Unit Test and Instrumentation + Upgrade Gate are green.

# CHANGELOG - FlashLearn

## [6.52] - 2026-09-27

### Unified Review Eligibility + Same-Day Lock
- Unified review eligibility across Flashcard, Quiz, review queue, queue counts, Home due counts, and progress summary.
- A concept reviewed once on its local calendar day is excluded from further review that day, regardless of answer correctness.
- Correct transitions remain DAILY → WEEKLY → MONTHLY → LEARNED.
- Wrong transitions remain DAILY → DAILY, WEEKLY → DAILY, MONTHLY → DAILY.
- LEARNED remains a separate review population and is not scheduled by the normal non-learned queue.
- Centralized `wasReviewedToday(...)` calendar-day logic is reused across queue selection, queue counts, answer submission, and progress/Home eligibility.
- Added regression coverage for same-day exclusion and deterministic local-calendar-day semantics.
- Runtime/CI identity is 6.52 / 652 with 6.51 / 651 as the previous-version gate.

### Verification
- GitHub Actions is the authoritative verification gate.
- The v6.52 checkpoint remains pending until the current Build + Unit Test and Instrumentation + Upgrade Gate are both green.

## [6.51] - 2026-09-27

### Grok Visual Reconstruction + Full Theme Design Contract
- Rebuilt the shared Grok visual language around the supplied reference screens: deep navy/black surfaces, warm gold accents, thin gold borders, centered ornamental headers, dense statistics and selected navigation pills.
- Shared cards now consume theme-owned border parameters; Grok navigation consumes theme-owned selected-pill behavior; Grok standard headers use theme-owned centered ornamentation.
- Built-in themes now carry the complete ThemeDesign profile directly in ThemeSpec; the existing custom-theme JSON contract remains unchanged.
- Older custom theme JSON without a design object remains backward-compatible through the matching built-in design fallback.
- Runtime identity advanced to 6.51/651 with 6.50/650 as the upgrade gate.

### Verification
- The checkpoint is verified only after authoritative GitHub Actions Build + Unit Test and Instrumentation + Upgrade Gate both pass.


## [6.50] - 2026-09-26

### GTP Theme Runtime Correction
- Root cause confirmed: v6.49 advertised five built-in themes in BuildConfig/metadata, but the actual runtime ThemeSpec catalog contained only four entries, so GTP could not appear in Settings.
- Added the fifth built-in theme gtp to the actual FlashLearnThemeSpec.BUILT_IN registry.
- Added a genuinely distinct GTP design language: violet/cyan palette, compact spacing scale, sharper corner geometry, stronger elevation, filled action icons, typography scaling, and compact density.
- Bound GTP spacing through FlashLearnThemeTokens, so screens using shared theme tokens visibly respond to the GTP geometry.
- Advanced theme JSON format to v3 with backward-compatible import of v2 custom themes.
- Added JVM and instrumentation regression coverage for the five-theme registry and GTP token binding.
- Updated application/CI release identity to versionName 6.50 / versionCode 650 with 6.49 / 649 as the previous-version gate.
- Verification status: GitHub Actions run 1429 is GREEN for Build + Unit Test and Instrumentation + Upgrade Gate.

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

