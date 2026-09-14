# FlashLearn — PROGRESS TRACKER

## Current checkpoint: v5.66 — Final specification reconciliation
**Application identity:** `versionName = 5.66`, `versionCode = 66`

### Current implementation status
- Parser boundary handling hardened for numbered entries, Persian-before-Spanish pairing, multiline Spanish sources, orphan preservation, breakdown separation, and parser evidence.
- Import reconciliation now exposes explicit modes: `ADD_NEW`, `SKIP_DUPLICATE`, `MERGE`, `UPDATE`; default remains `MERGE`.
- Import validates non-empty/different source and target languages and preserves stable concept identity when merging same-source translations.
- Exact duplicate behavior remains deterministic and regression-tested.
- Backup/restore, language-pair persistence, Review/Quiz integration, difficulty threshold, Library/Add Word/Bulk Import/Backup flows, and UI/navigation hardening remain in the current source tree.
- Root duplicate progress tracker was removed; this file is the canonical current progress record.

### Final reconciliation limitation — explicitly recorded
The current Room content model stores one `Content` row per `(conceptId, languageCode)`. Therefore multiple translations for the same source are currently represented during `MERGE` as a deterministic `" / "`-joined target text rather than as multiple normalized translation rows. This is an intentional compatibility-preserving implementation step; a normalized multi-translation schema would require a separate Room schema migration and broader repository/UI changes and is not claimed as complete in v5.66.

### Verification gate
- GitHub Actions is the authoritative build/test gate.
- Release Gate passes when signing secrets are absent by explicitly skipping signed-release verification; a real signed release remains dependent on configuring the existing release keystore secrets.
- Do not mark the current checkpoint fully verified until the latest Build/Unit and Instrumentation jobs finish successfully.

---

## Historical checkpoints

v5.52 Progress/Statistics + E2E Audit completed: audited dashboard projections, added end-to-end Add Word → Review → Progress/Statistics/Streak → soft-delete coverage, advanced runtime identity to 5.52/52, and updated CI artifact naming.

## Historical v5.52
**Current status at that checkpoint:** v5.52 Progress/Statistics + E2E implementation complete; final Android/GitHub CI execution remained the authoritative release verification gate.
**Application identity:** `versionName = 5.52`, `versionCode = 52`

v5.52 Progress/Statistics + E2E completed: Progress aggregation, ReviewHistory statistics, calendar-day streaks, Progress UI integration, and the end-to-end Add Word → Review → Progress/Statistics → soft-delete acceptance path were audited and regression-covered. Runtime identity was 5.52/52; Room schema remained v5.

v5.50 UI/Navigation Audit completed: navigation back handling, RTL manifest support, compact-screen scrolling, UI language consistency, and navigation regression tests.

v5.44 — Android test compile hardening: updated FullBackupRestoreTest to pass the required application Context after pre-restore backup dependency injection; release identity advanced to 5.44/44. No product behavior or schema change.

v5.43 — Release identity hardening: corrected RuntimeGate to assert versionName 5.43/versionCode 43 after detecting a stale versionCode assertion in v5.42. No product behavior or schema change.

v5.42 — Pre-restore automatic backup hardening: every FULL restore now snapshots the current device state to app-private storage before database mutation; snapshot write is atomic and a failure aborts restore before mutation. Added Android regression coverage. Room schema unchanged; Learning/Difficulty frozen.

v5.41 — Restore validation hardening: required JSON text fields now reject missing/null values with stable INVALID_VALUE:<field> errors; added Android regression coverage for null canonicalKey; CI now performs an explicit clean before assembleDebug. Room schema unchanged; Learning/Difficulty frozen.

## v5.38 — Restore canonicalKey hardening
- Restore now recalculates `Content.canonicalKey` from the final `Content.text` using the same NFC canonicalization function used by normal create/update/import flows.
- The backup-provided `canonicalKey` is no longer trusted as authoritative derived data.
- Added Android regression coverage proving a tampered/stale backup canonicalKey is corrected during restore.
- No Room schema change; Learning Transition and Difficulty remain frozen.

## Historical v5.48 status
- Corrected Android integration-test assertions exposed by CI: export timestamp is compared independently, content count reflects the two language contents created by the fixture, and unrelated parser metadata is preserved under non-destructive restore.
- Runtime identity aligned to 5.48/48.
- No production restore algorithm/schema change; Learning/Difficulty remain frozen.

## Historical v5.47
- RestoreResult exposes the v4.20 contract fields `newCount` and `mergedCount`.
- Backup UI reports new vs merged records using the corrected contract.
- Fixed Android restore regression tests that referenced missing `mergedCount`/`newCount` properties.
- Learning/Difficulty algorithms remain frozen; Room schema remains unchanged.

## Historical v5.46
- FULL restore follows the v4.20 UUID-based merge/update contract instead of deleting destination records.
- Content restore falls back to `(conceptId + languageCode)` when the incoming Content UUID is absent, skipping identical text and updating changed text in place.
- Added DAO operations required for stable updates and regression coverage for non-destructive restore.
- Learning/Difficulty algorithms remain frozen; Room schema remains unchanged.

## Historical v5.45
- Corrected the missing closing parenthesis in `RoomBackupRepository.kt` during `ContentEntity` construction.
- Advanced runtime release identity to 5.45/45 and aligned CI artifact names.
- Learning/Difficulty algorithms remain frozen; Room schema remains unchanged.

> Older tracker material is intentionally retained as history; percentages from older trackers are not treated as current status.
