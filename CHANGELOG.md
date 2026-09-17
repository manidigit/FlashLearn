# FlashLearn Changelog

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
