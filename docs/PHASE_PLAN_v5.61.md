# FlashLearn v5.61 — Five-Phase Completion Plan

This plan uses Descriptions v4.20 and the v5.60 audit as the completion baseline. Legacy exclusions remain excluded: Notifications/Reminders, NEW Stage, Anki/SM-2, AI/Cloud Translation, Multi-active Language Pairs V1, and manual threshold editing.

## Phase 1 — Release + Data Migration
- Stable application identity and monotonic versioning.
- Release signing configuration wired to the same keystore through CI secrets/environment.
- Room schema migration kept separate from semantic data migration.
- Independent Concept and Content data-version markers.
- `RefreshDataUseCase` with explicit `migrateToVersionX()` paths.
- Automatic data refresh at application startup.
- Upgrade/data-preservation contract tests.

## Phase 2 — Backup/Restore + Import/Export
- Full UUID/schema-versioned backup contract.
- Automatic pre-restore backup.
- Exact restore ordering, merge identity and atomic rollback.
- CSV/JSON/XLSX/SQLite import and export.
- Preserve-first parser and import validation.

## Phase 3 — Language Pair + Review/Quiz Completion
- Stable persisted language pair (not UI-only state).
- Persian/English/Spanish pair E2E coverage and swap.
- Review queue/filter semantics and learned-review separation.
- Quiz hint/note/pause behavior and flashcard interactions.

## Phase 4 — Library/Performance + Statistics/UI
- Search across all required fields.
- Real pagination and 100k-record performance gate.
- Refresh/local-data-update contract.
- Statistics, weighted progress, streak and achievement evaluator completion.
- Final compact navigation and removal of duplicated capabilities.
- RTL/LTR and reference UI audit.

## Phase 5 — Verification + Release Gate
- Unit, integration and instrumentation coverage for every P0 contract.
- Real upgrade-from-previous-APK gate without uninstall.
- Release signing verification and release artifact gate.
- Build + tests + release/upgrade gates required before final delivery.
