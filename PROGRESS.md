## v6.06 checkpoint — Review RTL alignment correction
- Current application identity: versionName 6.06 / versionCode 106.
- Root cause confirmed: Review setup used physical-end alignment (TextAlign.End / Alignment.End) for Persian UI, which becomes the left side under RTL; this made the Review screen appear reversed while other screens followed the global direction.
- Corrected Review setup text alignment to logical Start, which renders on the right in RTL and on the left in LTR.
- Corrected Review category content alignment and selected-state badge anchoring to logical layout direction.
- No review algorithms, scheduling rules, database schema, filtering semantics, or quiz behavior were intentionally changed.
- CI release gate updated to 6.06/106 with 6.05/105 as the previous-version gate.
- Verification is pending the authoritative GitHub Actions Build/Unit and Instrumentation + Upgrade Gate run.

## v6.05 checkpoint — Review Setup UI Match + Release/Process Ledger
- Current application identity: versionName 6.05 / versionCode 105.
- Recorded the supplied reference-image implementation as the current Review setup UI checkpoint.
- Review setup follows the requested visual structure: title/header, response mode, category filter, special review, scheduled review, vocabulary difficulty, quiz difficulty, word count, filter summary, and `شروع مرور`.
- Existing ReviewViewModel selection behavior and review algorithms are preserved; this checkpoint does not intentionally alter scheduling, learning-state transitions, database schema, import/backup contracts, or Quiz semantics.
- UI remains theme-token based and respects the app-wide RTL/LTR direction.
- Release process updated: CI current version is 6.05/105 and previous-version upgrade gate is 6.04/104.
- Verification status: pending authoritative GitHub Actions Build/Unit and Instrumentation + Upgrade Gate results.

## v6.04 checkpoint — Review setup UI
- Target screen confirmed as app/src/main/java/com/flashlearn/app/ui/review/ReviewScreen.kt.
- Review setup controls were compacted and visually refined.
- Review mode icons now use distinct semantic theme colors.
- Hard-coded RTL was removed; Review now inherits the app-wide RTL/LTR setting.
- Direction-sensitive navigation icons follow the active layout direction.
- Release identity: 6.04 / versionCode 104.

## v6.03 — Review UI modernization + CI alignment
- Review UI modernization checkpoint recorded.
- Release identity: 6.03 / 103.
- CI release gate synchronized with 6.03/103 and previous 6.02/102.
- GitHub Actions remains the build/test verification gate.

# FlashLearn — PROGRESS TRACKER

## Current checkpoint: v6.01
- Latest dashboard correction: review activity chart now has a visible review-count Y-axis and selectable weekly/monthly/three-month/all-history ranges.
- Learning progress remains 0% until the first real review of a concept; stage-based scoring applies only after review history exists.
- Runtime identity: versionName 6.01 / versionCode 101.
- This is a reconciliation/verification checkpoint, not a claim that every historical specification item is present in current source.
- Historical v4.33–v4.39 labels remain historical stage identifiers, not current app versions.
- Current source contains the v6.00 Quiz translation-display and 3-second feedback behavior.
- Current Review source also contains Spanish TTS playback for Quiz questions; this is now explicitly logged.
- GitHub Actions is authoritative for build/test status.
- Full verification requires Build/Unit, instrumentation, APK verification, and the previous-version upgrade gate.
- The uploaded archive named with v4.39 is tracked as a historical/documentation package; its filename is not used to infer application version.

### Reconciliation boundary
The v6.00 source backup and the separately uploaded v4.33→Final source/certification package were audited as separate artifacts. Where source trees differ, the project record must identify the difference rather than silently overwrite one snapshot with another.

## v5.99 — Quiz Distractor Rotation and Difficulty Bands
- Advanced application identity to 5.99 / versionCode 99.
- Added session-level distractor rotation so fresh valid wrong answers are preferred across sequential quiz cards.
- Changed Quiz Difficulty selection to deterministic EASY/MEDIUM/HARD category and entry-type tiers.
- Added regression coverage for sequential distractor rotation and difficulty-band separation.
- CI/runtime version gates advanced to 5.99/99 with 5.98/98 as the previous-version upgrade gate.

### Verification gate
- GitHub Actions remains the authoritative build/test gate.
- v5.99 is not marked fully verified until Build/Unit and Instrumentation jobs both complete successfully.


## v5.98 — Quiz Generation Hardening
- Advanced application identity to 5.98 / versionCode 98.
- GenerateQuizQuestion now maps QuizDifficulty to explicit distractor bands instead of only changing score weights.
- EASY/MEDIUM/HARD now select least-confusable / middle-band / most-confusable valid distractors respectively when the candidate bank supports the requested level.
- Vocabulary Difficulty remains a separate candidate-pool constraint.
- Added final pre-display validation for four unique normalized options and exactly one correct option.
- Added canonical-key duplicate protection and refresh of the quiz bank once per review session.
- Added regression coverage for observable separation of quiz difficulty levels.

### Verification gate
- GitHub Actions is the authoritative build/test gate for v5.98.
- Runtime version gate is aligned to 5.98 / 98.
- Final release status is not marked green until the post-push build/unit/instrumentation run completes successfully.
- Instrumentation emulator startup now has an explicit bounded ADB wait and step timeout to avoid indefinite CI hangs.

## v5.97 — Quiz Difficulty Distractor Selection Overhaul
- Advanced the application identity to 5.97 / versionCode 97.
- Reworked `GenerateQuizQuestion` so `QuizDifficulty` (`EASY`, `MEDIUM`, `HARD`) is a real distractor-selection signal instead of a proxy for `VocabularyDifficulty`.
- Preserved the documented Vocabulary Difficulty candidate-pool order: same difficulty → adjacent difficulty → whole bank.
- EASY now ranks clearly different distractors higher.
- MEDIUM now ranks plausible distractors higher, with same-category and compatible-entry-type matches preferred.
- HARD now ranks highly confusable distractors higher, using same category, same entry type, and local lexical similarity.
- Added local normalized lexical scoring (token overlap, character similarity, and n-gram overlap) without external/network dependencies.
- Kept the existing validity rules: active Concept, both sides of the active language pair, different Concept, normalized uniqueness, no duplicate options, and three valid distractors or `FlashcardFallback`.
- Added `QuizDifficultySelectionTest` regression coverage for all three quiz levels and for the separation between quiz difficulty and vocabulary difficulty.

### Verification gate
- GitHub Actions is the authoritative build/test gate.
- v5.97 is fully verified only after Build/Unit and Instrumentation jobs complete successfully.

## v5.96 — CI Version Alignment Fix
- Root cause confirmed from GitHub Actions run: the 5.96 APK built successfully, but `.github/workflows/android-ci.yml` still declared `FL_VERSION_CODE=95` and `FL_VERSION_NAME=5.95`.
- Updated CI current version to 5.96 / 96 and previous version to 5.95 / 95.
- The failing `Verify debug APK version` step will now compare the APK against the actual 5.96 version.

### Verification gate
- GitHub Actions is the authoritative build/test gate.
- v5.96 is fully verified only after Build/Unit and Instrumentation jobs complete successfully.

## v5.96 — Review Setup Compile Fix & Version Alignment
- Fixed the Review setup header alignment compile error by using the `BoxScope`-compatible `Alignment.CenterStart` value.
- Advanced the application identity from 5.95 / versionCode 95 to 5.96 / versionCode 96.
- Preserved the requested RTL Review setup ordering and existing review controls/behavior.

## v5.95 — Review Setup RTL Layout & Section Reordering
- Reorganized the Review setup without removing existing review controls or behavior.
- Applied explicit RTL layout to the review setup screen.
- Ordered the setup as requested: `حالت پاسخ اجرا` → `دسته‌بندی لغات` → `مرور ویژه` → `زمانبندی مرور` → `سطح دشواری کلمات` → `سطح دشواری آزمون تستی` → `تعداد کلمات`.
- Kept all existing review filters, personal-difficulty information, filter summary, available-word count, and `شروع مرور` action.
- Special review and scheduled review are now visually separated.

## v5.95 — CI Instrumentation Emulator Fix
- Added a headless Android Emulator to the instrumentation-test job.
- CI now installs the Android 34 Google APIs x86_64 system image and emulator tooling.
- CI waits for the emulator to report `sys.boot_completed=1` before `connectedDebugAndroidTest`.
- The previous `No connected devices!` failure was a CI environment/setup failure, not an application test assertion failure.

## v5.95 — Build Fix: Library Detail & Backup Screen Kotlin Compilation
- Renamed the injected `CategoryRepository` property in `LibraryDetailViewModel` to `categoryRepository` so it no longer conflicts with the public `categories` `StateFlow`.
- Updated the category lookup to use the renamed repository property.
- Removed the invalid `androidx.compose.foundation.lazy.item` import from `BackupScreen.kt`.
- This checkpoint addresses the reported `compileDebugKotlin` and `kaptDebugKotlin` errors without changing intended application behavior.

## v5.95 — Backup/Restore UI Separation & Layout Cleanup
- Separated Restore and Backup into distinct visual sections instead of mixing their controls together.
- Restore has a dedicated card with a single primary file-selection action.
- Backup types are displayed as clear full-width actions, improving readability and touch targets.
- `خروجی داده` remains visually separate from both Restore and Backup.
- Screen content is now scrollable so the complete workflow fits on smaller screens.
- Existing backup, restore, export, save, progress, and message behavior is preserved.

## v5.94 — Build Hotfix: MainActivity syntax correction
- Corrected the closing delimiter of the review-session `Surface` content block in `MainActivity.kt`.
- This addressed the Kotlin parser errors reported by `:app:kaptGenerateStubsDebugKotlin`.

## v5.94 — Bulk Vocabulary Import UI Simplification
- Removed the `انتخاب فایل واژگان` control from Bulk Import.
- Removed the `رفرش` controls from Bulk Import editor/preview.
- Preserved direct text entry, preview, parsing, duplicate reporting, warnings, and import behavior.

## v5.93 — Vocabulary Edit Form
- Vocabulary items open an editable word-details form consistent with Add Word.
- Category editing supports existing categories and `+ افزودن دسته جدید`.

## v5.92 — Add Word Simplification + Category Picker
- Removed unnecessary Add Word controls and hidden pronunciation/example fields.
- Added explicit existing-category / new-category selection.

## v5.91 — Home RTL Layout + Review Pool Totals
- Applied the requested RTL Home layout.
- Moved streak into the header and added ready/total review pool counts.

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

> Older tracker material remains available in Git history; current status is determined by the latest checkpoint above.
