# FlashLearn — PROGRESS TRACKER

## Current checkpoint: v6.23
- Application identity: **6.23 / 123**.
- Previous-version gate: **6.22 / 122**.
- Current scope: Grok theme + Home dashboard structure.
- Verification: **Pending** — latest GitHub Actions run 36053217848 is in progress.
- Runtime version is authoritative in `app/build.gradle.kts`; CI mirrors it and this document records process status.
- Remaining entries are historical checkpoints only.


## v6.13 — Root Theme + Design System centralization
- Implemented centralized semantic theme tokens and shared UI components.
- Migrated Add Word, Bulk Import, Backup, Library, Statistics/Progress, and Review target surfaces to shared headers/tokens.
- Added static regression audit and Theme × RTL/LTR instrumentation coverage.
- Added schema-backed sample JSON fixtures.
- Build/CI verification remains pending in this ZIP because no Gradle wrapper is present locally; GitHub Actions is the authoritative gate.
v6.00 Quiz Translation Display and Answer Feedback Timing completed: quiz options now show all target-language translations per Concept, ordered by translationIndex and joined with " / ". Quiz feedback keeps the selected wrong option red and the correct option green/visible for exactly 3 seconds, then advances automatically. Manual quiz continuation was removed. Regression coverage was added for multi-translation correct and distractor options. Runtime identity is 6.00/100; Room schema remains v5.

v5.52 Progress/Statistics + E2E completed: Progress aggregation, ReviewHistory statistics, calendar-day streaks, Progress UI integration, and the end-to-end Add Word → Review → Progress/Statistics → soft-delete acceptance path were audited and regression-covered. Runtime identity is 5.52/52; Room schema remains v5.

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
# FlashLearn — PROGRESS TRACKER
## Current checkpoint: v5.51
## v5.48 — Restore regression test contract hardening
- Corrected four Android integration-test assertions exposed by CI: export timestamp is compared independently, content count reflects the two language contents created by the fixture, and unrelated parser metadata is preserved under non-destructive restore.
- Runtime identity aligned to 5.48/48.
- No production restore algorithm/schema change; Learning/Difficulty remain frozen.

## v5.47 — Restore result contract CI fix
- RestoreResult now exposes the v4.20 contract fields `newCount` and `mergedCount`.
- Backup UI now reports new vs merged records using the corrected contract.
- Fixed Android restore regression tests that referenced missing `mergedCount`/`newCount` properties.
- Learning/Difficulty algorithms remain frozen; Room schema remains unchanged.

## v5.46 — Restore merge-contract implementation
- FULL restore now follows the v4.20 UUID-based merge/update contract instead of deleting destination records.
- Content restore falls back to (conceptId + languageCode) when the incoming Content UUID is absent, skipping identical text and updating changed text in place.
- Added DAO operations required for stable updates and regression coverage for non-destructive restore.
- Learning/Difficulty algorithms remain frozen; Room schema remains unchanged.

v5.45 — GitHub CI compile fix
- Corrected the missing closing parenthesis in `RoomBackupRepository.kt` during `ContentEntity` construction.
- Advanced runtime release identity to 5.45/45 and aligned CI artifact names.
- Learning/Difficulty algorithms remain frozen; Room schema remains unchanged.


**Last audited against:** extracted `v5.48` project source tree  
**Application identity:** `versionName = 5.48`, `versionCode = 48`  
**Current status:** **Feature implementation is substantially advanced; final release verification is still pending.**

> This tracker is the current project-status record. Older progress notes are retained below for history. Percentages from older trackers are not treated as current status.

---

## 1. Current v5.46 status

### A. Backup / Restore — IMPLEMENTED / HARDENED
- v5.40: FULL restore requires the `canonicalKey` field to be present in every content record, but deterministically recomputes it from `Content.text` and never trusts the backup value.
- v5.40: Added regression coverage for missing `canonicalKey` rejection before mutation.
- FULL backup/restore covers the current persisted data set, including the twelve persisted tables/relationships.
- Parser metadata and achievements are included in FULL backup/restore.
- Restore uses stable single-entity DAO operations rather than relying on generated bulk `upsertAll` APIs in `RoomBackupRepository`.
- Pre-mutation validation is performed before database mutation.
- Restore remains protected by a Room transaction so malformed restores can roll back atomically.
- Validation covers required snapshot sections, enum values, timestamps, counters, review-session/history consistency, content uniqueness, and cross-table references.
- Additional invariants cover one learning state and one difficulty state per concept, concept timestamps, review-history/session time bounds, and non-empty category names.
- Post-restore table-count integrity verification is present.
- Android backup round-trip regression coverage is present.

**Remaining verification:** final CI/Android build and test execution must still be confirmed as green before calling the release fully verified.

### B. Database / Migration — IMPLEMENTED / HARDENED
- Current Room schema remains version 5.
- Explicit migration chain `1 → 2 → 3 → 4 → 5` is present.
- Android migration integration coverage is present.
- Migration tests cover preservation of legacy data and the newer schema structures introduced along the chain.
- No new schema bump is claimed unless an actual schema change requires it.

**Remaining verification:** final CI execution.

### C. Review Engine / Scheduler — IMPLEMENTED
- Review queue selection follows the frozen v4.20 boundary contract.
- Only active Concepts are eligible.
- Duplicate Concept candidates are defensively removed.
- DAILY/WEEKLY/MONTHLY are due-only and ordered by `nextReviewAt`, then `Concept.id`.
- LEARNED is independent and shuffled.
- RANDOM is due-only and excludes LEARNED.
- Scheduled-time requirements are enforced.
- New concepts receive their creation timestamp as their initial review time.
- Category filtering is carried into queue selection.
- Review session integrity hardening is present.
- Incomplete source/target content is filtered before session creation.
- Learned review preserves persisted DifficultyState.
- Session finalization only reports completion after successful closure.

### D. Quiz — IMPLEMENTED / HARDENED
- `GenerateQuizQuestion` is implemented according to the frozen specification recorded in project history.
- Difficulty-priority, adjacent-difficulty, then broader distractor expansion are implemented.
- Four unique options are required where possible.
- Correct-answer inclusion and same-concept exclusion are enforced.
- Normalization/duplicate boundaries are covered.
- Flashcard fallback is implemented when a four-option Quiz question cannot be generated.
- Quiz is integrated into Review.
- Quiz submission uses the canonical `SubmitReviewAnswer` transaction path.
- Hint and notes controls are present.
- Wrong-answer feedback shows the correct answer.
- Correct answers advance immediately.
- Wrong answers auto-advance after the required 2-second pause.
- Invalid/stale submissions are hardened by regression coverage.

### E. Bulk Import / Parser — IMPLEMENTED / HARDENED
- Parser contract work from v5.00 onward is present.
- Breakdown, confidence, relationship, and variant/derivative metadata are projected and persisted.
- Import preview/result tracking exposes imported, duplicate, incomplete, and failed outcomes.
- Per-item failures do not abort later valid imports.
- Canonical duplicate detection follows the recorded domain normalization rules.
- Parser metadata is included in backup/restore.
- Regression coverage exists for parser and import boundaries.

### F. Achievements — IMPLEMENTED / PERSISTED
- Seven recorded achievement rules are implemented:
  - `FIRST_TEN_WORDS`
  - `SEVEN_DAY_STREAK`
  - `THIRTY_DAY_STREAK`
  - `MEMORY_BUILDER`
  - `VOCABULARY_BUILDER`
  - `HARD_MODE_MASTER`
  - `LONG_TERM_MEMORY`
- Existing unlocked achievements remain unlocked.
- Evaluation emits only newly unlocked IDs.
- Achievement persistence is wired into the current application path.
- FULL backup/restore includes achievement state.
- Regression coverage exists for achievement thresholds/streak boundaries and persistence-related backup behavior.

### G. Progress / Statistics / Dashboard — IMPLEMENTED
- Progress dashboard contains streak, today's workload, learned/total progress, review statistics, and stage distribution.
- Home dashboard contains review, learning-status, review-type, and word-management sections.
- Review CTA prioritizes Daily review when appropriate and falls back to another due review.
- Progress/statistics domain code and tests are present.

### H. Settings / Difficulty / Navigation / Library — IMPLEMENTED IN CURRENT SOURCE
- Current source contains Settings, difficulty/progress use cases, Library screens, Add Word, Bulk Import, Backup, Home, Progress, Review, and navigation.
- Navigation contract tests and relevant UI-state tests are present.

### I. CI / Release Packaging — PRESENT, FINAL RUN PENDING
- GitHub Actions Android CI workflow is present.
- Workflow is configured to build/test and publish v5.46 debug APK/source artifacts.
- Runtime gate checks the packaged version identity `5.46 / 46`.
- The project source currently does **not** include a checked-in final CI result proving the latest v5.46 run is green.

**Status:** workflow/configuration present; final external CI verification remains a release gate.

---

## 2. What is actually left?

### RELEASE BLOCKERS / FINAL VERIFICATION
1. Run the authoritative GitHub Actions Android build/test pipeline for the exact v5.45 source.
2. Confirm all unit tests pass.
3. Confirm all Android instrumentation tests pass.
4. Confirm runtime version gate passes with `5.46 / 46`.
5. Confirm the generated APK/source artifacts are produced successfully.
6. Review any CI failures, if present, and create a corrected checkpoint only if required.

### SPECIFICATION RECONCILIATION
Before declaring the project completely frozen:
1. Cross-check current implementation against the final Algorithms specification.
2. Cross-check current implementation against the final Descriptions specification.
3. Resolve any remaining TODO/FIXME items only when they represent actual unfinished requirements.
4. Confirm that all documented requirements have a corresponding implementation and regression test where applicable.

### IMPORTANT
The remaining work is **not accurately represented by the old 25–35% style tracker estimates**. The current v5.45 source is much further advanced. The remaining uncertainty is primarily final verification and complete specification-to-code reconciliation, not the absence of the major feature modules.

---

## 3. Current project inventory observed in v5.44

The extracted project contains the following major areas:

- `app`
- `core`
- `data`
- `database`
- `domain`
- GitHub Actions workflow
- Android instrumentation tests
- Unit/regression tests
- Parser contract documentation
- Progress/statistics code
- Backup/restore implementation
- Review engine
- Quiz
- Bulk Import
- Achievements
- Settings/difficulty
- Library
- Home/Progress UI

The extracted source tree also contains:
- 74 Kotlin main/source files
- 26 test files (unit + Android instrumentation)
- current Gradle/Kotlin project files
- `parser.jar`
- GitHub Actions Android CI workflow

---

# Historical checkpoints

## v5.38 — Backup/Restore compile hardening
- Backup restore uses DAO single-row upsert contracts; no `upsertAll` dependency remains in `RoomBackupRepository`.
- Full 12-table export/restore coverage retained, including ParserMetadata and Achievements.
- Atomic pre-mutation validation and Room transaction rollback protection retained.
- Room migration chain remains explicit v1→v5 with migration integration coverage.

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

## v5.29 — Quiz Complete Hardening
- Quiz flow hardened against invalid answer submissions.
- Quiz option selection is visibly reflected in the Review UI.
- Added regression coverage for priority/fallback/filter/normalization boundaries.
- Runtime gate/version advanced to 5.29.
- `GenerateQuizQuestion` algorithm implemented according to the frozen specification.
- Quiz UI integrated into Review flow.
- Quiz answers use the canonical SubmitReviewAnswer transaction path.
- Quiz fallback to Flashcard is implemented when four-option generation is impossible.
- Domain Quiz regression tests added.

## v5.24 — CI/unit-test hardening

## v5.22 — Hilt constructor fix for EvaluateAchievementsUseCase

## v5.22 — Review Scheduler/Card Selection completion
- Review queue follows the frozen v4.20 contract at the selection boundary.
- Active Concepts only; duplicate candidates removed.
- DAILY/WEEKLY/MONTHLY due-only and sorted by `nextReviewAt` then `Concept.id`.
- LEARNED is independent and shuffled.
- RANDOM is due-only and excludes LEARNED.
- Regression tests cover these invariants.

## v5.20 — Review category filtering completion
- Review selection exposes persisted category choices alongside difficulty filtering.
- Selected category is carried into queue selection and applied before the review session starts.
- Category loading is isolated from the review path.
- UI-state regression coverage added.

## v5.19 — Review scheduling contract hardening
- Scheduled-time requirements enforced for stage-specific queue selection.
- New concepts receive their creation timestamp as the initial review time.
- Regression coverage added for unscheduled stage cards.

## v5.18 — Learned-review integrity and session-finalization hardening
- Learned review preserves persisted DifficultyState exactly.
- Optional LEARNED review records the answer without changing Stage, Difficulty, or nextReviewAt.
- Review session finalization no longer discards the active session when endedAt persistence fails.

## v5.17 — Review/import hardening
- Canonical duplicate detection in bulk import matches the domain canonical-key rules.
- Review exit invokes completion callback only after the active session is successfully closed.

## v5.17 — Review session integrity hardening
- Review sessions close any previously active session before a new session replaces it.
- Review queues filter complete source/target content before session start.

## v5.15 — Achievement backup persistence coverage
- Added achievements to FULL backup export/restore and restore validation.
- Achievement persistence coverage expanded.

## v5.13 — Achievements domain engine
- Activated the recorded product achievement catalog.
- Added deterministic rules for the seven recorded achievements.
- Existing unlocked achievements remain unlocked.
- Added threshold and streak-boundary regression coverage.

## v5.12 — Release identity monotonicity
- Application versionName advanced to 5.12 and versionCode to 12.
- Added instrumentation regression gate for both values.
- No learning algorithm, scheduling rule, database schema, or parser behavior changed.

## v5.11 — Release identity and runtime gate hardening
- Application versionName advanced to 5.11.
- Added runtime version gate.
- Kept package-name preflight assertion.
- No learning algorithm, scheduling rule, database schema, or parser behavior changed.

## v5.10 — Parser metadata persistence
- Parser breakdown, relationship, variant, and confidence metadata persisted with concepts.
- Room schema version 4 and migration 3→4 added.
- Repository/DI wiring and full-backup restore support added.
- Focused regression coverage added.

## v5.07 — Algorithm boundary regression hardening
- Added regression coverage for Daily wrong-answer scheduling across the local-day boundary.
- Added Weekly→Monthly promotion and stable LEARNED behavior coverage.
- Added difficulty-cap coverage for VERY_HARD and monthly-failure escalation.
- No algorithm behavior changed.

## v5.06 — Home dashboard usability
- Home became a scroll-safe dashboard with review, learning-status, review-type, and word-management sections.
- Added primary review CTA and compact learning metrics.

## v5.05 — Progress dashboard
- Progress became a focused learning dashboard with streak, today's workload, learned/total progress, review statistics, and stage distribution.

## v5.04 — Review session UX hardening
- Review progress and live session accuracy became visible.
- Reveal and answer actions guarded against duplicate submission.

## v5.03 — Bulk Import failure visibility
- UI state exposes failedCount.
- Final summaries show imported, duplicate, incomplete, and failed counts.

## v5.02 — Resilient Bulk Import
- Per-item failures no longer abort later valid imports.

## v5.01 — Import metadata projection
- Parser confidence and breakdown/relationship/variant counts exposed in preview and item results.

## v5.00 — Parser Contract Completion
- Parser-side Breakdown, Confidence, Relationship, Variant/Derivative and deterministic Import Log contracts completed.

## v4.99 — Parser CI regression fix
- Fixed contextual classification so trailing unlabelled context after a complete pair is preserved as notes.

## v4.97 — Bulk Import result tracking
- Bulk Import keeps an independent result for each entry.
- Preview identifies incomplete entries before import.
- Imported, duplicate, incomplete and failed states are distinguished.
- Changing the input text resets previous import results.
- UI-state tests were added.
- Learning algorithm, scheduling and database schema were unchanged.
