# v5.05 — Progress dashboard
- Reworked the Progress screen into a focused learning dashboard instead of a plain text report.
- Added cards for learning streak, today's review workload, review statistics, and learning-stage distribution.
- Added a visual learned/total progress indicator.
- Kept refresh and navigation actions available without changing domain calculations or persistence.

# v5.04 — Review session UX hardening
- Review now shows a live progress bar based on answered cards and total session size.
- Review shows live session accuracy after the first answer.
- The reveal-answer action uses the full available width for a clearer primary action.
- Correct/wrong answer actions now share the available width and remain guarded by `canSubmitAnswer` while submission is in progress.
- Existing review selection, scheduling, learning transition, difficulty update, and persistence behavior are unchanged.

# v5.03 — Bulk Import failure visibility
- Bulk Import now exposes an explicit `failedCount` in UI state and the final result summary.
- A failed item remains visible with `FAILED` status and its error message, while later valid entries continue importing.
- Resetting the source text also resets the failure count with the other batch results.
- No learning algorithm, scheduling rule, or database schema changed.

# v5.02 — Resilient Bulk Import
- Bulk Import no longer aborts the whole batch when one ordinary item fails during concept creation.
- Failed items remain in the result list with `FAILED` status and the underlying error message when available.
- Later valid entries continue processing after a failed item.
- Final state surfaces the number of failed items while preserving imported, duplicate, and incomplete results.
- Duplicate and incomplete validation behavior remains unchanged.
- No learning algorithm, scheduling rule, or database schema changed.
- GitHub Actions remains the authoritative full Android build/test verification step.

# v5.01 — Bulk Import metadata integration
- Bulk Import now exposes Parser confidence for each entry as a percentage.
- Breakdown, relationship, and variant/derivative metadata counts are visible in preview and item results.
- Aggregate metadata counts are visible in the preview summary.
- Existing import persistence, duplicate handling, incomplete handling, notes, and raw-line preservation remain unchanged.
- Added focused UI-state regression coverage for deterministic metadata projection.
- No learning algorithm, scheduling rule, or database schema changed.
- GitHub Actions remains the authoritative full Android build/test verification step.

# v5.00 — Parser Contract Completion
- Completed the parser-side metadata contract left explicitly pending in v4.95: Breakdown, Confidence, Relationship, Variant/Derivative, and deterministic Import Log.
- Existing `parse(raw)` API remains compatible.
- Exact duplicate merging now also preserves structured metadata and the highest confidence.
- No learning algorithm, scheduling rule, or database schema changed.

# v4.99 — Parser CI regression fix
- Fixed contextual classification so an unknown unlabelled line following a complete source→translation pair is preserved as notes instead of becoming a false new entry.
- Preserved explicit source→translation pairs as entry boundaries.
- No learning algorithm, scheduling rule, or database schema changed.

# v4.98 — Bulk import UI-state CI fix
- Fixed the Bulk Import status-label visibility so `BulkImportUiStateTest` can compile against the same stable status labels used by the screen.
- No behavior, parser, learning algorithm, scheduling rule, or database schema changed.
- This version specifically addresses the GitHub Actions `Cannot access 'label': it is private in file` compilation failure from v4.97.
- GitHub Actions remains the authoritative full Android build/test verification step.

# v4.97 — Bulk import result tracking
- Bulk Import now keeps a per-entry result after execution: imported, duplicate, incomplete, or failed.
- The preview marks incomplete entries before import and the post-import list reports why each entry was skipped or failed.
- Duplicate reporting distinguishes duplicates inside the same batch from concepts already present in the library.
- Import results are reset whenever the source text changes, preventing stale outcome rows from being reused for a new batch.
- Added UI-state regression coverage for the new import-result statuses.
- No learning algorithm, scheduling rule, or database schema changed.
- GitHub Actions remains the authoritative full Android build/test verification step.

# v4.96 — Parser state-machine classification and warning pipeline
- Promoted Paste Parser classification into an explicit line-type decision stage (`ENTRY_HEADER`, `TRANSLATION`, `BREAKDOWN`, `NOTE`, `GRAMMAR_NOTE`, `DERIVATIVE`, `RELATION`, `COMMENT`, `NUMBER`, `SEPARATOR`, `UNKNOWN`).
- Added `parseDetailed()` with structured `ParseWarning` records containing warning type, line number, raw text, message, and confidence.
- Incomplete source entries are preserved for preview/import validation instead of being silently lost; orphan Persian/explanatory lines now surface as warnings.
- Added conservative recognition for breakdown, grammar, derivative, relationship, and comment lines while keeping them attached as notes to the current entry.
- Bulk Import preview now surfaces Parser warnings with line numbers before import.
- Added regression coverage for detailed parsing, orphan/incomplete input, and preserved free-form context.
- No learning algorithm, scheduling rule, or database schema changed.
