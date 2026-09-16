# FlashLearn — PROGRESS TRACKER

## Current checkpoint: v5.80 — FULL backup export/restore compatibility + stable CI signing
**Application identity:** `versionName = 5.80`, `versionCode = 80`

### Current implementation status
- FULL backup export now uses the authoritative `RoomBackupRepository.exportFull()` path instead of the typed partial exporter, so the generated FULL file contains every section required by the schema-v2 restore validator.
- FULL restore accepts the current `backupType = FULL` / `schemaVersion = 2` format while preserving compatibility with legacy `backupMode = FULL` and `backupMode = VOCABULARY` backups.
- Restore validation remains fail-before-mutation for malformed or incomplete current FULL backups.
- Pre-restore automatic backup remains enabled before database mutation.
- CI now verifies that the current FULL export path and required restore sections stay aligned, preventing the previous export/restore format drift from returning.
- CI version metadata and artifact names are aligned to 5.80/80.
- CI debug builds now require an explicitly configured stable signing keystore instead of silently falling back to a runner-generated debug key, preventing future update-install failures caused by changing APK signatures.
- Multiple-meaning import, duplicate cleanup, Library multi-meaning display, and concept-edit preservation remain covered by the previous hardening checkpoint.
- Same-day global review exclusion remains enforced at selection/answer boundaries as part of the review hardening path.
- CI remains the authoritative build/test gate.

### Verification gate
- GitHub Actions is the authoritative build/test gate.
- The v5.80 workflow must complete compilation, APK signature verification, stable signing identity verification, version verification, unit tests, backup compatibility verification, and multiple-meaning verification successfully before this checkpoint is considered fully verified.
- The stable CI debug signing keystore is intentionally supplied through GitHub Actions secrets and is not committed to the repository.
- Existing installations signed by an older ephemeral CI debug key cannot be retroactively converted to the new signing identity; after the one-time migration, subsequent CI debug updates use the same stable identity.

---

## Historical checkpoints

v5.79 — CI debug signing configuration preparation and multiple-meaning edit preservation.
v5.78 — Multiple-meaning import/merge and duplicate-cleanup hardening.
v5.77 — Global same-day review exclusion.
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
