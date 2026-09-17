# FlashLearn — PROGRESS TRACKER

## v5.94 — Bulk Vocabulary Import UI Simplification
- Advanced the application identity to `versionName = 5.94`, `versionCode = 94`.
- Removed the `انتخاب فایل واژگان` control from the Bulk Import editor so vocabulary input is entered directly in the text area.
- Removed the `رفرش` control from the Bulk Import editor/preview UI; the main `پیش‌نمایش` action remains the explicit way to process the entered vocabulary text.
- Preserved the existing Bulk Import parsing, preview, duplicate reporting, warning reporting, and import behavior.
- CI upgrade verification is aligned to `5.93 → 5.94` and versionCode `93 → 94`.

### Verification gate
- GitHub Actions is the authoritative build/test gate.
- The latest v5.94 run must complete Build/Unit and Instrumentation successfully before v5.94 is considered fully verified.
- Release Gate remains non-blocking when stable production signing secrets are absent; debug APK/source artifacts remain the normal CI outputs.

## v5.93 — Vocabulary Edit Form
- Advanced the application identity to `versionName = 5.93`, `versionCode = 93`.
- Tapping a vocabulary item now opens an editable word-details form styled consistently with the Add Word form.
- The edit form uses the same learning-language presentation, source word, translation, category picker, entry type picker, notes field, and save/cancel action pattern as Add Word.
- Category editing supports selecting an existing category, removing the category, or choosing `+ افزودن دسته جدید` and entering a new category name.
- Existing pronunciation and example data are preserved during edit even though those fields remain hidden to match the current Add Word UI.
- Existing favorite and delete actions remain available from the edit page.
- CI upgrade verification is aligned to `5.92 → 5.93` and versionCode `92 → 93`.

### Verification gate
- GitHub Actions is the authoritative build/test gate.
- The latest v5.93 run must complete Build/Unit and Instrumentation successfully before v5.93 is considered fully verified.
- Release Gate remains non-blocking when stable production signing secrets are absent; debug APK/source artifacts remain the normal CI outputs.

## v5.92 — Add Word Simplification + Category Picker
- Advanced the application identity to `versionName = 5.92`, `versionCode = 92`.
- Removed the `کلمات تکراری` and `رفرش` controls from the Add Word screen.
- Removed the `تلفظ` and `جمله نمونه` input fields from the Add Word screen while retaining their underlying ViewModel/domain compatibility.
- Changed Add Word category handling to an explicit picker for existing categories plus `+ افزودن دسته جدید` for creating a new category.
- Existing category selection and new-category creation continue through the existing `GetOrCreateCategoryUseCase` save path.
- CI upgrade verification is aligned to `5.91 → 5.92` and versionCode `91 → 92`.

### Verification gate
- GitHub Actions is the authoritative build/test gate.
- The latest v5.92 run must complete Build/Unit and Instrumentation successfully before v5.92 is considered fully verified.
- Release Gate remains non-blocking when stable production signing secrets are absent; debug APK/source artifacts remain the normal CI outputs.

## v5.91 — Home RTL Layout + Review Pool Totals
- Advanced the application identity to `versionName = 5.91`, `versionCode = 91`.
- Applied the requested RTL Home layout: Persian labels are right-aligned and numeric values are placed on the left side of their rows/cards.
- Moved the current streak display into the header line between the greeting and the language flags.
- Review cards now show the ready count together with the total word pool for that review type, so a weekly pool of 100 words can correctly show 20 ready words as `20 آماده از 100 کلمه`.
- Preserved the existing daily, weekly, and monthly review actions and their underlying ready counts.

### Verification gate
- GitHub Actions is the authoritative build/test gate.
- The latest v5.91 run must complete Build/Unit and Instrumentation successfully before v5.91 is considered fully verified.
- Release Gate remains non-blocking when stable production signing secrets are absent; debug APK/source artifacts remain the normal CI outputs.

## v5.90 — FULL Restore Category-ID Conflict Tolerance
- Made FULL restore tolerant of category ID conflicts when an incoming category has the same name as an existing category but a different UUID.
- FULL restore maps the incoming category UUID to the existing category UUID by name instead of attempting a conflicting insert, while preserving the incoming category when no conflict exists.
- Restored concepts use the resolved category UUID mapping.
- Added regression coverage for category-name restore conflicts.
- Optimized FULL restore content identity lookup to avoid the previous O(n²) scan pattern on large content sets.
- v5.90 is retained as the previous checkpoint and is not overwritten by v5.91.

## v5.89 — Supplied FULL Backup Restore Alignment
- Advanced the application identity to `versionName = 5.89`, `versionCode = 89`; v5.88 is retained as the previous checkpoint and is not overwritten.
- Aligned the active FULL restore implementation with the supplied schema-2 FULL backup shape and its real category → concept relationships.
- FULL restore now restores parent categories before concepts that reference those categories, preventing foreign-key failures when importing into an empty Room database.
- Kept the strict historical v5.74 partial schema-2 FULL compatibility rule exact, so arbitrary missing-section backups are not silently accepted as historical backups.
- Kept `RANDOM` review-session/history support and regression coverage.
- Added regression coverage for the supplied backup shape, including category/concept relationship ordering and RANDOM review data.
- CI version/upgrade verification is aligned to `5.88 → 5.89` and versionCode `88 → 89` using the stable debug signing identity.

### Historical checkpoints

v5.89 — Supplied FULL backup restore alignment; category-parent ordering; supplied-shape regression coverage; CI aligned to 5.88 → 5.89.
v5.88 — Four-part functional hardening + Global Theme Audit.
v5.87 — Previous-Version FULL Backup Compatibility Hardening.
v5.86 — Backup FULL export/restore fix.
v5.85 — Previous checkpoint before Backup FULL restore fix.
v5.74 — Review Help + About + Library category selection.
v5.73 — Large-library review/performance + Quiz UX hardening.
v5.72 — previous performance checkpoint before 30-card review batching and bulk Progress/Statistics joins.
v5.71 — Legacy restore + Quiz mode + update-path hardening.
v5.70 — legacy FULL restore compatibility and update verification path.
v5.69 — dedicated legacy vocabulary restore, launcher icon binding, and CI alignment.
v5.68 — Phase 5 full verification checkpoint.
v5.66 — final specification reconciliation.
v5.52 — Progress/Statistics + E2E audit.
v5.50 — UI/Navigation audit.
v5.48 — restore regression hardening.
v5.46 — UUID-based non-destructive restore merge contract.
v5.45 — Android CI compile hardening.
v5.42 — pre-restore automatic backup hardening.
v5.41 — restore validation hardening.

> Older tracker material is intentionally retained as history; percentages from older trackers are not treated as current status.
