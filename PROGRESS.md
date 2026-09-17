# FlashLearn — PROGRESS TRACKER

## v5.95 — CI Instrumentation Emulator Fix
- Added a headless Android Emulator to the instrumentation-test job.
- CI now installs the Android 34 Google APIs x86_64 system image and emulator tooling.
- CI waits for the emulator to report `sys.boot_completed=1` before `connectedDebugAndroidTest`.
- The previous `No connected devices!` failure was a CI environment/setup failure, not an application test assertion failure.

### Verification gate
- GitHub Actions is the authoritative build/test gate.
- v5.95 is fully verified only after Build/Unit and Instrumentation jobs complete successfully.

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
