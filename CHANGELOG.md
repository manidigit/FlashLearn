## v6.11 — CI release-artifact and verification hardening
- Advanced application identity to version 6.11 / versionCode 111.
- Fixed the current GitHub Actions failure: Build + Unit Test was green through APK verification, but artifact upload referenced stale v6.09 filenames and stopped the job before the instrumentation gate.
- Aligned CI current/previous version variables with 6.11/111 and 6.10/110.
- Made CI artifact names and paths derive from the current release variables so a future version bump cannot silently leave stale artifact paths behind.
- Kept the verified v6.09 global RTL/LTR root-chain correction and v6.10 navigation-direction hardening unchanged.

## v6.10 — RTL/LTR navigation consistency across secondary screens
- Advanced application identity to version 6.10 / versionCode 110.
- Audited Add Word, Bulk Import, Backup Restore, Vocabulary, and Statistics/Progress screens for direction-sensitive navigation.
- Replaced physical ArrowBack icons with AutoMirrored.Outlined.ArrowBack in navigation controls so back/category-navigation arrows follow the active RTL/LTR direction.
- Preserved End/Alignment.End/Arrangement.End usages where they are logical trailing alignment and therefore correctly adapt to the global direction.
- Previous-version upgrade gate advanced to 6.09 / 109.
- Verification pending the new GitHub Actions Build + Unit Test and Instrumentation + Upgrade Gate run.

## v6.09 — Global RTL/LTR direction-chain correction
- Advanced application identity to version 6.09 / versionCode 109.
- Confirmed the app-wide direction chain: Settings → AppViewModel → persisted AppUiState.layoutDirection → MainActivity LocalLayoutDirection.
- Centralized the AppLayoutDirection → Compose LayoutDirection mapping so RTL/LTR cannot be remapped differently at the root.
- Removed the fixed RTL CompositionLocalProvider from HomeScreen; Home now follows the same global direction as Review, Library, Progress, and Settings.
- Re-audited Review direction-sensitive text alignment and kept logical layout direction as the source of truth.
- Fixed CI artifact paths so the v6.09 build no longer uploads v6.07-named files.
- Added regression coverage for both RTL → Compose RTL and LTR → Compose LTR mapping.
- Verification: GitHub Actions run 1117 completed successfully; Build + Unit Test and Instrumentation + Upgrade Gate are green.

## v6.07 — Complete Review logical-direction correction
- Advanced application identity to version 6.07 / versionCode 107.
- Audited the entire ReviewScreen.kt for remaining physical-end alignment assumptions after v6.06.
- Replaced all remaining Review text TextAlign.End usages with logical TextAlign.Start.
- Replaced remaining selected-state Alignment.TopEnd anchors with logical Alignment.TopStart.
- This specifically fixes cases that still rendered Persian Review content or selected badges on the physical left under RTL despite the earlier partial correction.
- No ReviewViewModel, learning algorithm, scheduling, database, filtering, or quiz semantics were changed.
- Verification must be based on the new GitHub Actions run and an actual RTL/LTR UI check; source inspection alone is not treated as visual verification.

## v6.06 — Review RTL Alignment Fix
- Advanced application identity to version 6.06 / versionCode 106.
- Fixed the Review setup screen's semantic text/alignment direction so Persian content follows the app-wide RTL setting: logical Start now maps to the right in RTL and to the left in LTR.
- Corrected the Review category content alignment and selected-state badge placement to use logical layout direction rather than physical left/right assumptions.
- Preserved Review selection behavior, filtering, scheduling, quiz/flashcard behavior, and theme-token usage.
- Updated GitHub Actions current-version verification to 6.06/106 with 6.05/105 as the previous-version upgrade gate.
- Verification status: pending the new GitHub Actions Build/Unit and Instrumentation + Upgrade Gate run.

## v6.05 — Review Setup UI Match + Release/Process Ledger Update
- Advanced application identity to version 6.05 / versionCode 105.
- Recorded the supplied reference-image implementation as the current Review setup UI checkpoint: RTL section order, compact selection cards, difficulty/test controls, word-count choices, filtered-word summary, and the primary `شروع مرور` action.
- Preserved existing ReviewViewModel behavior and review algorithms; this checkpoint is a UI/layout refinement rather than a learning-engine or scheduling rewrite.
- Kept theme-token based colors and global RTL/LTR handling; no hard-coded app-wide direction was introduced.
- Updated the runtime version gate and GitHub Actions release/upgrade gate from 6.04/104 to 6.05/105, with 6.04/104 as the previous-version gate.
- Verification remains pending until GitHub Actions Build/Unit and Instrumentation + Upgrade Gate jobs complete successfully.

## v6.04 — Review Setup UI redesign and RTL/LTR correction
- Reworked the Review setup screen itself: compact card proportions, refined primary action, and distinct semantic icon colors.
- Removed the Review screen's hard-coded RTL provider so the global app layout direction now controls the screen.
- Direction-sensitive back/navigation icons now follow RTL/LTR automatically.
- Application release identity advanced to version 6.04 / code 104.
- GitHub Actions release gate aligned with 6.04/104 and previous 6.03/103.

## v6.03 — Review UI modernization + CI release alignment
- Modernized Review screen iconography, compact controls, and layout-direction handling.
- Application release identity advanced to version 6.03 / code 103.
- GitHub Actions release gate aligned with 6.03/103 and previous 6.02/102.

## v6.01 — Progress Dashboard Activity Chart + Learning Progress Correction
- Corrected learning-progress scoring so a newly added word remains at 0% until it has a real review history.
- Added a visible Y-axis with review-count scale to the review-activity chart.
- Added activity-range filters for weekly, monthly, three-month, and all-history views.
- Monthly and three-month views use real review-history data rather than the seven-day slice.
- Updated the About screen to describe the current statistics/progress dashboard behavior.

## v6.01 — Project Source / Version / CI Reconciliation
- Advanced application identity to 6.01 / versionCode 101 as the post-v6.00 audit checkpoint.
- Recorded the existing Spanish Quiz TTS playback path in the release ledger.
- Corrected the ledger so v4.33–v4.39 are historical specification-stage identifiers, not the current application version.
- Recorded the v6.00 source snapshot and the separately packaged v4.33→Final certification archive as distinct artifacts; filenames do not define runtime version.
- Restored the CI release gate to the current application version.
- Restored an Android instrumentation-test job so Build/Unit success is not mistaken for full Android verification.
- No Room schema, learning algorithm, scheduling contract, parser/import contract, or Quiz answer semantics are intentionally changed by this bookkeeping checkpoint.

# FlashLearn Changelog

## v6.08 — Review Direction Binding
- Bound Review text alignment to the app-selected RTL/LTR direction instead of relying on the text content's natural bidi direction.
- Updated the runtime version preflight gate to the current release.


## v6.00 — Quiz Translation Display and Answer Feedback Timing
- Advanced application identity to 6.00 / versionCode 100.
- Quiz answer options now display all target-language translations belonging to each Concept, in translation-index order, joined with ` / `.
- Added regression coverage proving multi-translation correct and distractor options are shown as complete translation groups rather than only the first translation.
- After submitting a Quiz answer, the selected wrong option remains red and the correct option remains green/visible for 3 seconds.
- Quiz then advances automatically after the 3-second feedback interval; the manual `ادامه` button is removed.
- No Room schema, learning algorithm, scheduling contract, or import behavior changed.

## v5.99 — Quiz Distractor Rotation and Difficulty Bands
- Advanced application identity to 5.99 / versionCode 99.
- Prevented the same three distractors from repeating across sequential quiz cards when at least three fresh valid candidates exist.
- Replaced the overly broad MEDIUM fixed-score selection with deterministic rank bands: EASY lowest three, MEDIUM middle three, HARD highest three.
- Preserved Vocabulary Difficulty pool rules, final four-option uniqueness checks, and Flashcard fallback.


## v5.98 — Quiz Generation Hardening
- Advanced application identity to 5.98 / versionCode 98.
- Reworked GenerateQuizQuestion difficulty selection into explicit confusability bands: EASY selects the least-confusable valid distractors, MEDIUM selects candidates nearest the middle band, and HARD selects the most-confusable valid distractors.
- Kept Vocabulary Difficulty separate from Quiz Difficulty and preserved the documented candidate-pool order: same difficulty → adjacent difficulty → whole bank.
- Added a second validation barrier before a quiz card reaches the UI: exactly four unique normalized options and exactly one normalized match for the correct answer; invalid questions fall back instead of rendering duplicate/ambiguous options.
- Added canonical-key duplicate protection and a per-review-session quiz-bank refresh so newly added/edited vocabulary is available to distractor generation.
- Added regression coverage proving the three quiz levels produce different distractor bands when the candidate bank contains enough variety.
- Updated runtime version gate, CI version expectations, and project progress/changelog records.
- Hardened the instrumentation emulator startup with a bounded ADB wait so a missing/stalled emulator cannot leave the CI job hanging indefinitely.

## v5.97 — Quiz Difficulty Distractor Selection Overhaul
- Advanced the application identity to 5.97 / versionCode 97.
- Separated `QuizDifficulty` from `VocabularyDifficulty`: quiz difficulty now controls how close/plausible distractors are, while vocabulary difficulty only controls the documented candidate-pool priority.
- `ابتدایی` / `EASY` prefers clearly different distractors instead of merely random alternatives.
- `متوسط` / `MEDIUM` prioritizes plausible distractors from the same category and compatible entry type when available.
- `حرفه‌ای` / `HARD` prioritizes highly confusable distractors using category, entry type, and local lexical similarity.
- Added local text-similarity scoring using normalized tokens, character similarity, and n-gram overlap; no external service or network dependency is introduced.
- Preserved all hard safety rules: target language, different Concept, active Concept, source/target language availability, normalized uniqueness, and exactly three valid distractors or `FlashcardFallback`.
- Preserved the specification's Vocabulary Difficulty fallback order: same difficulty → adjacent difficulty → whole bank.
- Added dedicated regression tests covering EASY/MEDIUM/HARD distractor quality and independence from Vocabulary Difficulty.

## v5.96 — CI Version Alignment Fix
- Updated `.github/workflows/android-ci.yml` from 5.95 / versionCode 95 to 5.96 / versionCode 96.
- Updated the CI previous-version expectation from 5.94 / 94 to 5.95 / 95.
- This fixes the `Verify debug APK version` gate, which was still comparing the 5.96 APK against the old CI value 95.

## v5.96 — Review Setup Compile Fix & Version Alignment
- Fixed the Review setup header alignment compile error by using the `BoxScope`-compatible `Alignment.CenterStart` value.
- Advanced the application identity from 5.95 / versionCode 95 to 5.96 / versionCode 96 for the Review setup task.
- Preserved the requested RTL Review setup ordering and existing review controls/behavior.

## v5.95 — Review Setup RTL Layout & Section Reordering
- Reorganized the Review setup without removing existing review controls or behavior.
- Applied explicit RTL layout to the review setup screen.
- Ordered the setup as requested: `حالت پاسخ اجرا` → `دسته‌بندی لغات` → `مرور ویژه` → `زمانبندی مرور` → `سطح دشواری کلمات` → `سطح دشواری آزمون تستی` → `تعداد کلمات`.
- Kept all existing review filters, personal-difficulty information, filter summary, available-word count, and `شروع مرور` action.
- Separated `تصادفی` / `یادگرفته` from `روزانه` / `هفتگی` / `ماهانه` so special review and scheduled review are visually distinct.

## v5.95 — CI Instrumentation Emulator Fix
- Added a headless Android Emulator to the instrumentation-test job.
- Installed the Android 34 Google APIs x86_64 system image and emulator tooling in CI.
- Waits for `sys.boot_completed` before running `connectedDebugAndroidTest`, eliminating the previous `No connected devices!` failure.
- No application behavior was changed by this CI-only fix.

## v5.95 — Build Fix: Library Detail & Backup Screen Kotlin Compilation
- Renamed the injected `CategoryRepository` property in `LibraryDetailViewModel` to `categoryRepository` to remove the collision with the public `categories` `StateFlow`.
- Updated category lookup to use the renamed repository property, resolving the resulting overload ambiguity and lambda error.
- Removed the invalid `androidx.compose.foundation.lazy.item` import from `BackupScreen.kt`; `LazyColumn` provides `item {}` through its scope.
- No backup/restore or library behavior was intentionally changed.

## v5.95 — Backup/Restore UI Separation & Layout Cleanup
- Separated the Restore flow from the Backup flow into clearly labeled sections.
- Restore now has its own dedicated card and primary `انتخاب فایل پشتیبان` action.
- Backup types are presented as distinct full-width actions instead of cramped horizontal chips.
- Kept `خروجی داده` as a separate section from backup/restore operations.
- Converted the screen content to a scrollable layout so all actions remain accessible on smaller screens.
- Preserved the existing import, restore, backup, export, save, progress, and message behavior.

## v5.94 — Build Hotfix: MainActivity syntax correction
- Corrected the closing delimiter of the review-session `Surface` content block in `MainActivity.kt`.
- This fixes the Kotlin parser errors `Expecting an element` and `Missing '}'` reported by `:app:kaptGenerateStubsDebugKotlin`.
- No application behavior or UI requirements were changed by this hotfix.

## v5.94 — Bulk Vocabulary Import UI Simplification
- Advanced the application identity to 5.94 / versionCode 94.
- Removed the `انتخاب فایل واژگان` control from the Bulk Import editor.
- Removed the `رفرش` control from the Bulk Import editor and preview UI.
- Kept the direct Paste/text-entry flow and the explicit `پیش‌نمایش` action.
- Preserved Bulk Import parsing, preview, duplicate reporting, warning reporting, and import behavior.

## v5.93 — Vocabulary Edit Form
- Advanced the application identity to 5.93 / versionCode 93.
- Tapping a vocabulary item now opens an editable word-details form styled consistently with the Add Word form.
- Category editing supports selecting an existing category, removing the category, or choosing `+ افزودن دسته جدید`.
- Existing pronunciation and example data are preserved during edit even though those fields remain hidden.
- Existing favorite and delete actions remain available from the edit page.

## v5.92 — Add Word Simplification + Category Picker
- Removed the `کلمات تکراری` and `رفرش` controls from the Add Word screen.
- Removed the `تلفظ` and `جمله نمونه` input fields while preserving the underlying compatibility.
- Changed Add Word category handling to an explicit picker for existing categories plus `+ افزودن دسته جدید`.

## v5.91 — Home RTL Layout + Review Pool Totals
- Applied the requested RTL Home layout with Persian labels right-aligned and numeric values on the left.
- Moved the current streak display into the header line between the greeting and language flags.
- Ready-review cards show ready count together with the total word pool.

## Historical checkpoints
- v5.90 — FULL Restore Category-ID Conflict Tolerance.
- v5.89 — Supplied FULL Backup Restore Alignment.
- v5.88 — Four-part functional hardening + Global Theme Audit.
- v5.87 — Previous-Version FULL Backup Compatibility Hardening.
- v5.86 — Backup FULL export/restore fix.
- v5.74 — Review Help + About + Library category selection.
- v5.73 — Large-library review/performance + Quiz UX hardening.
- v5.71 — Legacy restore + Quiz mode + update-path hardening.
- v5.70 — Legacy FULL restore compatibility and update verification path.
- v5.69 — Dedicated legacy vocabulary restore, launcher icon binding, and CI alignment.
- v5.68 — Phase 5 full verification checkpoint.
- v5.66 — Final specification reconciliation.
- v5.52 — Progress/Statistics + E2E audit.
- v5.50 — UI/Navigation audit.
- v5.48 — Restore regression hardening.
- v5.46 — UUID-based non-destructive restore merge contract.
- v5.45 — Android CI compile hardening.
- v5.42 — Pre-restore automatic backup hardening.
- v5.41 — Restore validation hardening.

> Older tracker/changelog material remains available in Git history; current status is determined by the latest checkpoint above.
