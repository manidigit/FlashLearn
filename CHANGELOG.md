# FlashLearn Changelog

## v5.94 — Build Hotfix: MainActivity syntax correction
- Corrected the closing delimiter of the review-session `Surface` content block in `MainActivity.kt`.
- This fixes the Kotlin parser errors `Expecting an element` at line 153 and `Missing '}'` at line 158 reported by `:app:kaptGenerateStubsDebugKotlin`.
- No application behavior or UI requirements were changed by this hotfix.

## v5.94 — Bulk Vocabulary Import UI Simplification
- Advanced the application identity to 5.94 / versionCode 94.
- Removed the `انتخاب فایل واژگان` control from the Bulk Import editor.
- Removed the `رفرش` control from the Bulk Import editor and preview UI.
- Kept the direct Paste/text-entry flow and the explicit `پیش‌نمایش` action.
- Preserved Bulk Import parsing, preview, duplicate reporting, warning reporting, and import behavior.
- CI upgrade verification is aligned to `5.93 → 5.94` and versionCode `93 → 94`.

## v5.93 — Vocabulary Edit Form
- Advanced the application identity to 5.93 / versionCode 93.
- Tapping a vocabulary item now opens an editable word-details form styled consistently with the Add Word form.
- The edit form keeps the learning-language presentation, source word, translation, category picker, entry type picker, notes field, and save/cancel action pattern.
- Category editing supports selecting an existing category, removing the category, or choosing `+ افزودن دسته جدید` and entering a new category name.
- Existing pronunciation and example data are preserved during edit even though those fields remain hidden to match the current Add Word UI.
- Existing favorite and delete actions remain available from the edit page.
- CI upgrade verification is aligned to `5.92 → 5.93` and versionCode `92 → 93`.

## v5.92 — Add Word Simplification + Category Picker
- Advanced the application identity to 5.92 / versionCode 92.
- Removed the `کلمات تکراری` and `رفرش` controls from the Add Word screen.
- Removed the `تلفظ` and `جمله نمونه` input fields from the Add Word screen while preserving the underlying data model and save contract for compatibility.
- Changed Add Word category selection to an explicit choice between an existing category and `+ افزودن دسته جدید`.
- Existing categories are selectable directly; a new category can be entered after choosing the add-new option.
- Preserved the existing `GetOrCreateCategoryUseCase` save behavior.

## v5.91 — Home RTL Layout + Review Pool Totals
- Advanced the application identity to 5.91 / versionCode 91.
- Applied the requested RTL Home layout with Persian labels right-aligned and numeric values on the left.
- Moved the current streak display into the header line between the greeting and language flags.
- Ready-review cards show both ready count and total word pool, such as `20 آماده از 100 کلمه`.
- Preserved the existing daily, weekly, and monthly review actions and their underlying counts.

## v5.90 — FULL Restore Category-ID Conflict Tolerance
- Made FULL restore tolerant of category ID conflicts when an incoming category has the same name as an existing category but a different UUID.
- FULL restore maps the incoming category UUID to the existing category UUID by name instead of attempting a conflicting insert.
- Restored concepts use the resolved category UUID mapping.
- Added regression coverage for category-name restore conflicts.
- Optimized FULL restore content identity lookup to avoid the previous O(n²) scan pattern on large content sets.

## Historical checkpoints
- v5.88 — Four-part functional hardening + Global Theme Audit.
- v5.87 — Previous-Version FULL Backup Compatibility Hardening.
- v5.86 — Backup FULL export/restore fix.
- v5.85 — Previous checkpoint before Backup FULL restore fix.
- v5.74 — Review Help + About + Library category selection.
- v5.73 — Large-library review/performance + Quiz UX hardening.
- v5.72 — Performance checkpoint before 30-card review batching and bulk Progress/Statistics joins.
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
