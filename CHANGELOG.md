## v6.01 — Project Source / Version / CI Reconciliation
- Advanced application identity to 6.01 / versionCode 101 as the post-v6.00 audit checkpoint.
- Recorded the existing Spanish Quiz TTS playback path in the release ledger.
- Corrected the ledger so v4.33–v4.39 are historical specification-stage identifiers, not the current application version.
- Recorded the v6.00 source snapshot and the separately packaged v4.33→Final certification archive as distinct artifacts; filenames do not define runtime version.
- Restored the CI release gate to the current application version.
- Restored an Android instrumentation-test job so Build/Unit success is not mistaken for full Android verification.
- No Room schema, learning algorithm, scheduling contract, parser/import contract, or Quiz answer semantics are intentionally changed by this bookkeeping checkpoint.

# FlashLearn Changelog

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
