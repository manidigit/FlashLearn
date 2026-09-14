# FlashLearn — PROGRESS TRACKER

## Current checkpoint: v5.70 — Legacy FULL restore + update verification
**Application identity:** `versionName = 5.70`, `versionCode = 70`

### Current implementation status
- Added a dedicated compatibility path for the supplied legacy FULL backup format: `schemaVersion=1`, `backupMode=FULL`, epoch-millisecond timestamps, embedded concept contents, legacy learning-state fields, legacy review history, and settings.
- Legacy FULL restore maps concepts/categories/multilingual contents, learning stage/counts, difficulty/streak state, review sessions/history, and settings into the current Room schema.
- Legacy sessions containing mixed DAILY/WEEKLY stages are split by stage so the current session/history invariant is preserved without dropping review history.
- Restore runs on the IO dispatcher and remains transactional.
- Added Android integration coverage for legacy FULL restore and idempotent re-import.
- CI now verifies an in-place APK version upgrade with `adb install -r` under the same signing context.
- The existing release-signing gate still requires the stable keystore secrets for a real user-device release upgrade. No private signing material is committed to the public repository.
- Legacy vocabulary restore remains supported separately for `backupMode=VOCABULARY`.

### Important legacy-format mapping
The supplied FULL backup has 8,098 concepts and 8,098 learning states, plus 1,878 review-history records. Its concepts embed `contents`, while the current Room model stores contents in a separate table. The compatibility layer normalizes repeated translations within one concept/language into one `" / "`-joined value because the current Room model permits one content row per `(conceptId, languageCode)`. Legacy review `responseTimeMs`, `previousStatus`, `newStatus`, and similar fields have no current storage columns, so only the current review-history fields are restored.

### Verification gate
- GitHub Actions is the authoritative build/test gate.
- Release Gate passes without signing secrets by explicitly skipping signed-release verification; a real release APK requires the stable release keystore secrets.
- Do not call restore/update fully verified until the latest Build/Unit and Instrumentation jobs finish successfully, including the legacy FULL restore tests and the APK in-place upgrade check.

---

## Historical checkpoints

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
