v5.52 — Progress/Statistics + E2E
- Completed Progress and Statistics audit on the v5.51 baseline.
- Hardened end-to-end acceptance coverage across creation, review, progress, statistics, streaks, and inactive-concept filtering.
- Added explicit final statistics and streak assertions to the MVP E2E path.
- Advanced runtime identity to 5.52/52.
- Corrected the stale runtime preflight versionCode assertion from 51 to 52.
- Updated CI artifact naming to v5.52 and kept the existing clean/build/unit/instrumentation gates.
- Added an E2E streak consistency assertion and kept the Progress refresh control inside the scrollable dashboard.

v5.51 — Library/Add Word/Bulk Import Audit

- Library search now performs true substring matching for Spanish/Persian text and canonical keys; the previous DAO binding required an exact query unless `%` was supplied.
- Bulk Import now persists each parsed entry and its parser metadata inside one transaction, so a metadata failure cannot leave a partially imported concept behind.
- Bulk Import batch-duplicate tracking now records a pair only after successful import; failed or pre-existing duplicate entries no longer cause later identical entries to be mislabeled as same-batch duplicates.
- Runtime release identity advanced to 5.51/51 and the preflight assertion was aligned accordingly.
- Learning/Difficulty algorithms and Room schema remain unchanged.

v5.50 — UI/Navigation Audit: system back navigation, RTL support declaration, compact-screen scrolling hardening, Persian UI consistency, and navigation regression coverage.

v5.48 — Restore regression test contract hardening: fixed CI-exposed test assumptions for timestamp, two-content fixtures, and non-destructive parser metadata merge; aligned runtime identity to 5.48/48. No production algorithm or schema change.

v5.48 — Restore result contract CI fix: aligned RestoreResult with the v4.20 Success(newCount, mergedCount) contract and updated BackupViewModel messaging. Fixed unresolved mergedCount/newCount Android test references. No Room schema change; Learning/Difficulty remain frozen.

v5.46 — Restore merge-contract implementation: FULL restore now merges/updates by stable identity instead of deleting destination records, with Content fallback matching by (concept, languageCode); added DAO support and Android regression coverage. No Room schema change; Learning/Difficulty remain frozen.

v5.45 — GitHub CI compile fix: corrected the missing closing parenthesis in RoomBackupRepository ContentEntity construction and aligned release identity to 5.45/45. No product behavior or Room schema change.

v5.44 — Android test compile hardening: updated FullBackupRestoreTest to pass the required application Context after pre-restore backup dependency injection; release identity advanced to 5.44/44. No product behavior or schema change.

v5.43 — Release identity/runtime gate correction: versionCode and RuntimeGate are aligned at 43; no product behavior or schema change.

v5.42 — Pre-restore automatic backup hardening: FULL restore now creates an app-private automatic snapshot of the current device state before mutation, using atomic temp-file replacement; restore is aborted if that snapshot cannot be written. Added Android regression coverage. No Room schema change; Learning/Difficulty frozen.

v5.41 — Restore validation hardening: required JSON text fields now reject missing/null values with stable INVALID_VALUE:<field> errors; added Android regression coverage for null canonicalKey; CI now performs an explicit clean before assembleDebug. Room schema unchanged; Learning/Difficulty frozen.

## v5.40
- FULL restore hardening: `contents[].canonicalKey` is now a required backup field while remaining non-authoritative; the restored key is always recomputed from `Content.text`.
- Added Android regression coverage proving a missing canonicalKey is rejected before database mutation.
- No Room schema change; Learning Transition and Difficulty remain frozen.

## v5.39
- Backup/Restore hardening: `canonicalKey` is now deterministically recalculated from restored `Content.text` instead of trusting backup-provided derived data.
- Added Android regression test for stale/tampered canonical keys.
- No Room schema change; frozen Learning/Difficulty algorithms unchanged.

## v5.37 — Backup/Restore + Migration Release Hardening
- Removed backup restore reliance on generated DAO bulk `upsertAll` for parser metadata and learning/difficulty/achievement persistence; restore uses stable single-entity DAO operations to avoid generated-API mismatch failures.
- Added strict pre-mutation restore invariants for one learning state and one difficulty state per concept, concept timestamps, review-history/session time bounds, and non-empty category names.
- Added post-restore table-count integrity verification for all twelve persisted tables/relationships.
- Added a full Android backup round-trip regression covering every current entity, relationship, parser metadata, settings, achievements, review session/history, learning state, difficulty state, categories and tags.
- Runtime gate and CI artifact naming advanced to 5.37/37.

## v5.34 — Backup/Restore & Migration Final Hardening
- FULL restore now requires every current snapshot section, preventing accidental destructive restores from truncated backups.
- Restore validates enum values, timestamps, counters, review-session/history consistency, content uniqueness, and all cross-table references before any database mutation.
- Restore remains all-or-nothing inside a Room transaction; malformed payloads leave the existing database untouched.
- Added regression coverage for missing sections, invalid learning state, invalid session chronology, and history/session type mismatch.
- Verified the existing Room 1→2→3→4→5 migration chain remains the current production path; no schema bump is introduced without a real schema change.

## v5.33 — Backup/Restore & Migration Hardening
- FULL backup now exports and restores parser metadata (breakdown, relationships, variants, confidence).
- FULL restore removes stale parser metadata and validates metadata references and confidence before mutation.
- Backup domain validation now covers parser metadata referential integrity and confidence bounds.
- Added end-to-end backup/restore regression coverage for parser metadata round-trip, stale-data removal, and invalid payload rejection.
- Added Android migration coverage for the complete Room 1→2→3→4→5 chain, including preservation of legacy data and verification of newly introduced schema tables/columns.
- No Room schema version change was required; database remains at schema version 5 and all existing migrations remain registered.

## v5.32 — Quiz UX Completion
- Added frozen Quiz hint and notes controls.
- Added explicit wrong-answer feedback with the correct answer.
- Correct Quiz answers advance immediately; wrong answers auto-advance after the required 2-second pause.
- Quiz submission remains on the canonical Review answer transaction path.

v5.24 — CI/unit-test hardening

v5.22 — Hilt constructor fix for EvaluateAchievementsUseCase

# v5.22

- Completed the Review Scheduler/Card Selection boundary: inactive concepts are excluded, duplicate Concept candidates are defensively removed, LEARNED review is shuffled independently, and scheduled modes remain ordered by due time then Concept ID.
- Added regression coverage for inactive concepts, Random eligibility, LEARNED eligibility, and deterministic scheduled ordering.

## v5.20 — Review category filtering completion

- Review selection now exposes persisted category choices alongside difficulty filtering.
- Selected category is carried into queue selection and applied before the review session starts.
- Category loading is isolated from the review path so a category-read failure does not break review itself.
- Added UI-state regression coverage for the new filter state.

## v5.19 — Review scheduling contract hardening

- Enforced scheduled-time requirements for stage-specific queue selection.
- New concepts receive their creation timestamp as the initial review time.
- Added regression coverage for unscheduled stage cards.

# FlashLearn v5.18 — Learned-review integrity and session-finalization hardening

- Learned review now preserves the persisted DifficultyState exactly; optional LEARNED review records the answer without changing Stage, Difficulty, or nextReviewAt.
- Review session finalization no longer discards the active session when endedAt persistence fails; completion is reported only after successful closure.

# FlashLearn v5.17 — Review/import hardening

- Canonical duplicate detection in bulk import now matches the domain canonical-key rules (whitespace normalization + Unicode normalization + locale-stable case folding).
- Review exit now invokes the completion callback only after the active session is successfully closed, preventing navigation that leaves an unclosed session.

# v5.17 — Review session integrity hardening
- Prevented abandoned review sessions when a new session is started.
- Filtered incomplete bilingual content before session creation so review progress counts remain accurate.

# v5.15 — Achievement backup persistence coverage
- FULL backup export/restore now includes persisted achievement states.
- Added restore-side uniqueness validation and deterministic replacement of achievement state.

# v5.13 — Achievements domain engine
- Activated the product achievement catalog defined in the project backlog.
- Added deterministic rules for seven achievements: FIRST_TEN_WORDS, SEVEN_DAY_STREAK, THIRTY_DAY_STREAK, MEMORY_BUILDER, VOCABULARY_BUILDER, HARD_MODE_MASTER, and LONG_TERM_MEMORY.
- Existing unlocked achievements remain unlocked and only newly unlocked IDs are emitted by evaluation.
- Added threshold and streak-boundary regression coverage.
- This checkpoint builds the domain engine first; persistence and UI wiring remain separate follow-up work.

# v5.12 — Release identity monotonicity
- Advanced the application release identity to v5.12.
- Increased Android versionCode from 1 to 12 so release upgrades have a monotonic package version code.
- Extended the runtime instrumentation gate to verify both versionName `5.12` and versionCode `12`.
- No learning algorithm, scheduling rule, database schema, or parser behavior changed in this checkpoint.
- GitHub Actions remains the authoritative full Android build/test verification step.

## v5.29 — Quiz Complete Hardening
- Hardened Quiz submission against invalid/stale selections.
- Added selected-option UI state and stable option numbering.
- Expanded Quiz regression coverage for difficulty priority, missing-language fallback, inactive distractors, and Unicode/whitespace normalization.
- Bumped runtime gate/version to 5.29.

- Added the frozen read-only `GenerateQuizQuestionUseCase` algorithm.
- Implemented difficulty-priority, adjacent-difficulty, then full-bank distractor expansion.
- Enforced four unique options, correct-answer inclusion, same-concept exclusion, normalization and Flashcard fallback.
- Added domain regression tests for Quiz generation and fallback behavior.
- Integrated Quiz mode into the Review flow with option selection and canonical `SubmitReviewAnswer` persistence.
- Bumped runtime gate/version to 5.28.
