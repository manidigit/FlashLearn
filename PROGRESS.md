# FlashLearn — PROGRESS TRACKER

## Current checkpoint: v5.71 — Legacy restore + Quiz mode + update-path hardening
**Application identity:** `versionName = 5.71`, `versionCode = 71`

### Current implementation status
- Legacy FULL restore supports the supplied earlier-version backup shape: `schemaVersion=1`, `backupMode=FULL`, epoch-millisecond timestamps, embedded multilingual concept contents, 8,098 concepts, 8,098 learning states, 1,878 review-history records, and settings.
- The supplied FULL backup was independently validated against the restore parser's strict schema/value checks with no invalid concept UUID, language, stage, difficulty, reference, or streak records found.
- Legacy FULL restore maps concepts, categories, multilingual contents, learning state, difficulty/streak state, review sessions/history, and settings into the current Room schema; repeated same-language translations are merged with ` / ` because the current Room schema stores one content row per `(conceptId, languageCode)`.
- Legacy vocabulary restore remains supported separately for `backupMode=VOCABULARY` and the supplied 8,098-concept vocabulary backup.
- Quiz mode no longer switches silently to Flashcards when an imported/legacy concept has no DifficultyState. It uses a safe effective difficulty for distractor selection and keeps the explicitly selected Quiz mode. If four valid options genuinely cannot be produced, the session remains in Quiz mode and reports the condition instead of changing modes.
- Added a regression test proving Quiz generation works when the target concept has no DifficultyState.
- CI now contains a real emulator smoke test for an in-place APK update: v5.70 → v5.71 using the same package and signing context and `adb install -r`.
- CI version drift was removed; workflow and app identity are aligned to 5.71/71.
- Stable release signing is still intentionally externalized to GitHub Actions secrets. A real production update over an already-installed release requires the same stable signing key; no private key is committed to the public repository.

### Verification gate
- GitHub Actions is the authoritative build/test gate.
- The latest relevant CI run is still in progress; do not mark the three issues fully verified until Build/Unit and Instrumentation complete successfully.
- Release Gate can pass while signed-release steps are skipped when stable signing secrets are absent.

---

## Historical checkpoints

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
