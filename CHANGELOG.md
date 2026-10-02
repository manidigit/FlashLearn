## v6.75 — Issue 5: Slow Home startup

- Root causes found by reading the Home refresh path: (1) `CountReviewQueueUseCase` was called three times and each call loaded all active concepts, all difficulty states, all concept tags and all content rows; (2) Home loaded every `ReviewHistory` row only to read timestamps and distinct concept ids; (3) `CalculateProgressUseCase` rebuilt a set of all concept ids for every learning state (quadratic work); (4) all loads ran one after another.
- Fix: new `CountReviewQueueUseCase.countByType` loads the shared tables once for DAILY/WEEKLY/MONTHLY (tags and contents are loaded only when a filter needs them); Home uses `getAllReviewedAt()` and `getDistinctConceptIds()`; independent loads run in parallel; the quadratic loop uses the existing active-id set.
- Counting rules are unchanged; `invoke(filters)` delegates to the same code.
- Home's start-up state repair is now kept pending until it actually finishes (it used to be cancelled by the second refresh from HomeScreen).
- Tests: `CountReviewQueueUseCaseTest`, `ProgressPathFailureTest`.
- No Room schema or migration change. No timing measurements were taken.

## v6.74 — Issue 4: Category click crash

- Root cause: `CategorySelectionScreen` (shared by Library and Review) and `LibraryScreenV2` passed Persian-digit text (`toFaDigits(...)`) to string resources declared with `%1$d`. Android formatting then throws `IllegalFormatConversionException` the first time the screen is composed, so the app closed as soon as the category list opened.
- Fix: `category_words`, `category_selected_count`, `library_selected_categories` and `library_merged_duplicates` now use `%1$s` in both Persian and English catalogs. Displayed digits are unchanged (Persian digits, same as the rest of the Library screen).
- Test: `StringFormatArgumentContractTest` scans all resources and Kotlin sources and fails if a numeric-format resource receives a text argument.
- No Room schema or migration change.

## v6.73 — Issue 7 Activity Chart (verified, documentation)

- The fix was already in the code but undocumented: `buildActivityData` returns days oldest → today, the chart Row/Canvas is wrapped in `LayoutDirection.Ltr` so bars, labels and the value axis share one direction, and `ActivityChartDataTest` asserts the weekday order.
- v6.72 CI was green, so that test passed.
- No production code changed in this version.

## v6.72 — StatisticsTest compile fix

- Fixed `:domain:compileDebugUnitTestKotlin` failure: `statisticsAggregate_preservesCountsWithoutLoadingHistoryObjects` called suspend `CalculateStatisticsUseCase.invoke` outside a coroutine; the test now runs inside `runBlocking`.
- Test-only change; no production behavior changed.
- GitHub Actions is authoritative; not verified until all gates are green.

## v6.71 — Statistics performance and persistent activity snapshot

- Statistics aggregation now executes in the database/repository instead of loading the full ReviewHistory into the Statistics use case.
- Progress percentage uses the distinct reviewed Concept IDs query rather than the full ReviewHistory dataset.
- Activity data is loaded from a bounded 90-day window; selecting Week/Month/Three-months reuses the in-memory activity snapshot instead of triggering a full Progress refresh.
- Streak calculation reads only review timestamps rather than full ReviewHistory objects; streak semantics remain calendar-day based.
- No history retention or deletion was introduced.
- No Room schema/migration change was introduced.
- Added a regression test for the Statistics aggregate snapshot contract.

### Verification
Implementation pushed to main. CI verification was intentionally not awaited per the task instruction; the authoritative run must still be checked separately.

Previous checkpoint: v6.70 / 670.

## v6.70 — Achievements evaluation path

- Progress now invokes the existing CheckAndUnlockAchievements domain use case instead of constructing a partial AchievementContext in the ViewModel.
- Achievement context now uses the real active vocabulary, review history, LearningState and DifficultyState data, including practiced-word, active-word and VERY_HARD+Learned thresholds.
- Existing unlocked achievements remain unlocked; newly satisfied achievements are persisted and displayed immediately when Progress/Statistics is opened or refreshed.
- Changing the Activity chart range does not re-run achievement evaluation.
- Added a regression test proving ten practiced active Concepts unlock FIRST_TEN_WORDS through the real achievement-checking path.
- No Room schema or migration change.

### Verification
GitHub Actions verification: GREEN — run 36988213115 (#2032), including Build + Unit Test, release/R8 verification, Instrumentation + Upgrade Gate, and the 6.69→6.70 upgrade path.

Previous checkpoint: v6.69 / 669.

## v6.69 — Refresh cleans Spanish word edges

- Refresh (Add Word → Refresh) now removes stray symbols such as `*` from the start and end of Spanish vocabulary text already stored in the database.
- The canonical key of each repaired row is rebuilt so search, duplicate detection and merge use the cleaned text.
- Persian text, inner characters and legitimate Spanish punctuation (`¿ ¡ ? ! . , ( )`) are not touched.
- Rows made only of noise characters are left unchanged (never emptied).
- Added content data version 4 (3 → 4); runs once per device and is idempotent.
- No Room schema or Room migration change; no other behavior changed.

### Verification
GitHub Actions is authoritative. v6.69 is not considered verified until Build + Unit Test, Instrumentation + Upgrade Gate, upgrade-path and release/R8 checks are green.

## v6.68 — Statistics vocabulary-count alignment

- Defined **Total Words** for progress/statistics as the number of active vocabulary Concepts.
- Kept the basic progress split reconciled as **practiced + unpracticed + learned = total active words**.
- Scoped reviewed-word statistics to active Concepts so historical review events for soft-deleted vocabulary do not inflate the current reviewed-word count.
- Preserved historical review-event totals, correctness and accuracy after soft deletion.
- Added regression coverage for active/inactive reviewed concepts and the total/practiced/unpracticed/learned reconciliation.
- No Room schema or migration change was required.

### Verification
GitHub Actions is authoritative. v6.68 is not considered verified until Build + Unit Test, Instrumentation + Upgrade Gate, upgrade-path and release/R8 checks are green.

## v6.67 — Vocabulary Edit field cleanup

- Removed Pronunciation and Example from the Content domain model and Room contents schema.
- Updated Add Word and Vocabulary Edit so both expose the same supported editable vocabulary fields.
- Added Room migration 7→8 that removes obsolete columns while preserving supported vocabulary data.
- Updated JSON/CSV/XLSX/typed/full backup and restore paths to omit obsolete fields while tolerating legacy backup payloads.
- Added migration and UI field-contract regression tests.

### Verification
GitHub Actions is authoritative. v6.67 is not considered verified until Build + Unit Test and Instrumentation + Upgrade Gate are green.

## v6.66 — Non-destructive duplicate finding

- Changed Add Word → Find duplicates from a destructive cleanup action into a read-only duplicate scan.
- Duplicate detection uses a dedicated domain use case and canonical source-language keys without mutating vocabulary or learning state.
- The Add Word method screen reports duplicate groups and extra duplicate concepts without deleting anything.
- Refresh now observes Library StateFlow so refreshed totals are reflected in the Add Word method screen.

### Verification
GitHub Actions GREEN — run 36679020997 (Build + Unit Test and Instrumentation + Upgrade Gate).

## v6.65 — Add Word action labels

- Clarified that the duplicate action removes exact duplicates rather than merely finding them.
- Clarified that Refresh refreshes the displayed word count/library state.
- No action behavior was changed.

## v6.64 — Home ready-review count clarity

- Fixed the Home ready-review cards so the large ready count is shown once and the secondary label shows only the stage total.
- Advanced release identity to 6.64 / versionCode 664.
- CI previous-version upgrade gate is now 6.63 / 663.

### Verification
GitHub Actions is authoritative. v6.64 is not considered verified until Build + Unit Test and Instrumentation + Upgrade Gate are green.

## v6.61.2 — Hardcoded UI string extraction

- Extracted user-visible Compose UI strings into Android resources with Persian and English variants across navigation, About, Add Word, Bulk Import, Backup/Restore, Library, Review, Needs Review, Progress, Settings, and related surfaces.
- Added formatted resources for dynamic counts, errors, labels, and accessibility text.
- Documented the work in `docs/PROCESS_LOG_V6.61.2.md` and updated the remaining-work checklist.

## v6.63 — Remove Spark theme
- Removed the Spark (جرقه) built-in theme from the theme registry and runtime selection.
- Kept the remaining themes unchanged.

## v6.62 — Theme ownership, adaptive layout and localization hardening
- Made ThemeDesign the runtime source of spacing scale, typography scale, density scale, elevation scale and corner geometry; legacy ThemeSpec fields remain only for custom-theme JSON compatibility.
- Added theme-owned adaptive window breakpoints and compact-width reflow for the Home statistics grid.
- Removed the accent preference from overriding the active theme's primary color.
- Centralized Home and Add Word user-facing strings into Android resources with English translations.
- Added a ThemeDesign ownership regression contract covering all built-in themes.
- Documented the localization boundary: static UI text in resources, user-created learning content in Room.
- Documented that Material dynamic color is intentionally disabled to preserve explicit theme ownership.
- Advanced release identity to 6.62 / versionCode 662; previous-version gate is 6.61 / 661.

### Verification
GitHub Actions is authoritative. v6.62 is not considered verified until Build + Unit Test and Instrumentation + Upgrade Gate are green.

## v6.61.1 — Audit hardening and CI fixes
- Fixed theme color compositing to use Compose alpha compositing instead of the previous no-op helper.
- Applied the selected accent color to Material primary/tertiary theme colors.
- Removed the global LocalDensity override; theme scaling no longer changes system/gesture-area density.
- Added validation for theme design metrics.
- Serialized review-card advancement with a Mutex to prevent concurrent advances.
- Persisted app route/concept navigation state with SavedStateHandle.
- Extracted Review header strings into Android resources (Persian/English).
- Replaced Review header hardcoded padding with theme tokens.
- Fixed CI compilation by restoring the required dp import in MainActivity.

## v6.61 — Review hints, bulk-import categories, and multi-type backup restore
- Review hints are now data-driven: a word's category is shown first; when no category exists, the first character of the translation is shown instead of the previous generic sentence.
- Bulk vocabulary import now lets the user choose a category for the imported batch; existing uncategorized concepts may receive the selected category without overwriting an existing category.
- Backup creation now supports selecting multiple backup types in one combined bundle.
- Restore now recognizes bundle schema v3 and restores each selected supported type, while retaining legacy v1 and typed v2 compatibility.
- Added regression coverage for the new review-help behavior and kept the Learning/Difficulty algorithms unchanged.

### Verification
GitHub Actions verification: GREEN — run 36406917581 (Build + Unit Test and Instrumentation + Upgrade Gate).

## v6.60 — Grok 95% visual refinement pass
- Refined the Grok home dashboard geometry with theme-owned hero, review-card, CTA and bottom-spacing metrics.
- Rebuilt the Grok bottom navigation as a full-width selected pill with theme-owned active/inactive icon profiles and scales.
- Extended custom-theme JSON persistence so ThemeDesign metrics, layout strategies, button/navigation styles, status colors and icon profile are preserved instead of falling back to defaults.
- Kept Learning/Review/Library data behavior unchanged; this checkpoint is visual/theme infrastructure only.

### Verification
GitHub Actions verification: GREEN — run 36400464908 (Build + Unit Test, Instrumentation + Upgrade Gate, release APK and previous-version upgrade gate).

## v6.59 — Startup crash fix: Needs-Review state initialization
- Fixed an immediate startup crash in `NeedsReviewViewModel`: the `init { refresh() }` block previously ran before the backing `_items` StateFlow was initialized, causing a NullPointerException on the first `setValue`.
- Kept the Needs-Review approval flow unchanged; this is an initialization-order correction only.
- Added a CI failure diagnostic step to dump recent emulator logcat when instrumentation fails.
- Advanced release identity to 6.59 / versionCode 659; previous-version upgrade gate is 6.58 / 658.
- The startup smoke test is now the authoritative regression check for MainActivity launch.

## Verification
GitHub Actions verification: GREEN — run 36393654803 (Build + Unit Test and Instrumentation + Upgrade Gate).

## v6.58 — Startup migration integrity hardening
- Fixed Room migration 5→6 so the contents(languageCode, canonicalKey) index is recreated with the canonical Room name after the temporary table is renamed.
- Added a migration regression assertion for the canonical index name.
- Aligned release identity to 6.58 / versionCode 658; previous-version upgrade gate is 6.57 / 657.
- GitHub Actions remains the authoritative verification gate; this checkpoint is not verified until Build + Unit Test and Instrumentation + Upgrade Gate are green.

## v6.57 — Manual Needs-Review approval flow
- Changed تأیید in the manual-review queue to open the normal Add Word form prefilled with the queued source and translation.
- The user can edit the source, translation and optional Add Word fields before saving.
- The queue item remains PENDING while the form is open or unsaved.
- On Save, Concept creation and transition of that queue item to APPROVED happen in one database transaction; a failed save leaves the queue item pending.
- After a successful save, the new or merged concept is available in Library and the user is returned there.

## Verification
GitHub Actions is authoritative. v6.57 is not verified until Build + Unit Test and Instrumentation + Upgrade Gate are both green.

## v6.56 — Comprehensive audit hardening
- Fixed flashcard feedback dead-end with an explicit **کارت بعدی** action and added **رد کردن کارت** when a quiz cannot generate enough distractors.
- Quiz prompts and TTS now follow the active source language; review setup now receives the configured language pair and maximum-card setting.
- Home ready-review counts now use the same language-pair and same-day eligibility rules as the review queue, while stage totals remain aligned with Statistics.
- Editing translations now reconciles, reorders, adds and removes target-language rows instead of leaving stale meanings behind.
- Starter data is one-time only; deleting all vocabulary no longer recreates the sample set on every Home refresh.
- Backup/export hardening: active-only CSV/JSON/XLSX exports, spreadsheet formula escaping, stronger enum validation, preservation of vocabulary metadata and translation rows, settings timestamp-aware restore, and periodic cleanup of old soft-deleted concepts.
- Added periodic purge of inactive vocabulary older than 90 days, checked every 30 days.
- Fixed local-day scheduling for Weekly/Monthly advancement and retained the independent Learning/Difficulty contract; legacy monthly/path fields remain persistence-compatible and do not drive Difficulty.
- Added library favorite interaction, corrected detail reload language-pair behavior, and preserved pronunciation/example fields in the edit form.
- Added explicit Android backup policy and bounded backup-file restore input.

## Verification
GitHub Actions is authoritative. v6.56 is not verified until Build + Unit Test and Instrumentation + Upgrade Gate are both green.

## v6.55 — Grok icon system + theme-owned visual profile
- Advanced application identity to **6.55 / versionCode 655**; previous-version gate is **6.54 / 654**.
- Grok action icons now use a theme-owned outlined icon profile while active navigation uses a separate theme-owned filled profile.
- Added theme-owned icon size scaling and navigation indicator alpha.
- Shared shell/header icon rendering now consumes the active/inactive icon profile from ThemeDesign.
- Preserved the five-theme catalog and custom-theme JSON compatibility.

### Verification gate
GitHub Actions is authoritative. v6.55 is not considered verified until Build + Unit Test and Instrumentation + Upgrade Gate are green.

## v6.54 — Review-ready counts vs stage totals
- Advanced application identity to **6.54 / versionCode 654**; previous-version gate is **6.53 / 653**.
- Fixed Home review cards so the large number shows **currently review-ready words**, while **«از X» shows the total words assigned to that Learning stage**.
- Daily, Weekly and Monthly totals now align with the Learning-stage totals used by Statistics instead of reusing the ready-review counts as their denominators.
- A word that remains in its current stage but was already reviewed today is excluded from the ready-review number while remaining included in the stage total.
- Aligned review queue counting with the same local-timezone same-day eligibility rule used by review selection.
- Fixed the instrumentation release-version gate to expect **6.54 / 654**.

### Verification gate
GitHub Actions is authoritative. v6.54 is not considered verified until Build + Unit Test and Instrumentation + Upgrade Gate are green.

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

