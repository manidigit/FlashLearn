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
- Local isolated Kotlin parser checks pass; full Android build/test remains a GitHub Actions responsibility because the working environment does not contain a usable Gradle wrapper/runtime.

# v4.95 — Parser foundation expansion and import preview

- Expanded the Paste Parser foundation toward the v4.20 state-machine contract: exact source/translation duplicates are merged without losing notes or raw lines.
- Added conservative rule-based classification for common `STRUCTURE` and `IDIOM` patterns while preserving existing WORD/PHRASE/SENTENCE behavior.
- Added explicit note-line recognition for common Persian, Spanish, and English labels (for example, مثال/نکته/گرامر, Ejemplo/Nota/Uso, Example/Note/Grammar) so descriptive lines do not become fake vocabulary entries.
- Expanded parser regression coverage for duplicate merging and rule-based structure/idiom classification.
- Bulk Import preview now shows valid/incomplete counts, note-bearing entries, entry type, and preserved notes before import.
- No learning algorithm, scheduling rule, or database schema changed.
- Parser remains intentionally partial: full Breakdown/Confidence/Relationship/Variant/Import Log requirements are still pending.
- GitHub Actions remains the authoritative build/test verification step.

# v4.94 — Review stale-result and lifecycle hardening

- Review session generation checks now cover session start state writes, card loading, answer submission success/failure, next-card advancement, and exit cleanup.
- An older Review operation can no longer overwrite a newer session after the user restarts Review or leaves and re-enters it.
- Leaving Review while an older end-session operation is still finishing no longer clears a newly started session.
- Answer results are applied only to the exact active session/card generation that created the request.
- No learning algorithm, scheduling rule, or database schema changed.
- GitHub Actions remains the authoritative build/test verification step.

# v4.93 — Review session concurrency hardening

- Hardened ReviewViewModel against overlapping session starts and stale session results.
- Snapshot the selected review type at answer submission so a later UI mode change cannot alter the persisted attempt.
- Guarded next-card advancement against duplicate concurrent taps.
- Leaving Review invalidates older session-start work before navigation.
- No learning algorithm or database schema changes.

# CHANGELOG v4.92

## Input-state consistency hardening
- Bulk Import now clears its preview whenever the source text changes, preventing an old preview from being imported after the user edits the text.
- Bulk Import snapshots the selected batch before launching the import coroutine, so the running operation is isolated from later UI-state changes.
- Preview counters/results are reset when input changes, and Preview is disabled for blank input or while importing.
- AddWord category loading now uses a generation guard so an older async category load cannot overwrite a newer result.
- Existing parser, backup, library refresh, review, and cross-screen hardening is preserved.
- No learning algorithm, scheduling rule, or database schema changed.
- Bumped application version to 4.92.

# CHANGELOG v4.91

## Vocabulary parser note classification fix
- Fixed `VocabularyParser` so a following non-pair mixed-language line such as `مثال: aprender español cada día` is retained as a note instead of being misclassified as a new vocabulary entry.
- Preserved valid Spanish→Persian pair detection and existing numbering/bullet/spaced-dash handling.
- The existing `preserves_following_non_persian_lines_as_notes` regression test now matches the intended behavior.
- No learning algorithm, scheduling rule, or database schema changed.
- Bumped application version to 4.91.

# CHANGELOG v4.90

## MVP core flow hardening: backup, library refresh, and bulk import
- Full backup restore now rejects duplicate entity UUIDs, duplicate review attempts within the same session, and blank setting keys before any local snapshot is deleted.
- Added instrumentation coverage proving malformed duplicate-ID/duplicate-attempt backups are rejected before mutation.
- Library refresh now snapshots query/category/favorites filters at refresh start, preventing a refresh from accidentally reading newer mutable filter state midway through its async load.
- Bulk import now de-duplicates repeated source/translation pairs within the same import batch and counts them as skipped duplicates.
- Bulk import preview and navigation are disabled while an import is running; Backup navigation/save is disabled while export/restore is busy.
- Added parser regression coverage for numbering/bullets/spaced-dash input and note preservation.
- Existing v4.84–v4.89 review, refresh, library-edit, and backup hardening is preserved.
- No learning algorithm, scheduling rule, or database schema was changed.
- Bumped application version to 4.90.

# CHANGELOG v4.89

## Full backup validation hardening
- Full backup validation now checks concept→category references and review-history→session references before restore mutation.
- Duplicate review attempts in the same session are rejected during validation.
- Blank setting keys and duplicate tag names are rejected as invalid backup data.
- Restore performs equivalent structural checks before deleting the local snapshot, producing safer and clearer failure behavior for malformed backups.
- Existing v4.84–v4.88 review, refresh, library-edit, and atomic restore hardening is preserved.
- No learning algorithm, scheduling rule, or database schema was changed.
- Bumped application version to 4.89.

# CHANGELOG v4.88

## Full backup restore replacement semantics and post-restore synchronization
- Full JSON restore now replaces the complete local snapshot instead of attempting to merge into a non-empty database and failing on key conflicts.
- All restore deletes and inserts remain inside one Room transaction, so a failed restore rolls the replacement back atomically.
- Added an instrumentation regression test proving that an extra local concept is removed when restoring an earlier full snapshot.
- Successful restore now triggers Home, Library, and Progress refreshes so all major screens reflect restored data immediately.
- Backup operation busy-guards and v4.84–v4.87 hardening are preserved.
- No learning algorithm or scheduling rule was changed.
- Bumped application version to 4.88.

# CHANGELOG v4.87

## Library edit integrity and complete content editing
- Hardened `UpdateConceptUseCase` against creating duplicate active vocabulary pairs when an existing word is edited to match another active source/target pair.
- Added the same non-blank canonical validation used by creation to the update path.
- Library Detail editing now exposes and persists pronunciation and example fields in addition to source, target, and notes.
- Library Detail display now shows saved pronunciation and example when present.
- Added a focused regression test proving duplicate active source/target pairs are rejected without mutating the existing record.
- Preserved all v4.84–v4.86 review, cross-screen refresh, and backup atomicity hardening.
- No learning algorithm, review scheduling rule, or database schema was changed.
- Bumped application version to 4.87.

# CHANGELOG v4.86

## Full backup restore atomicity and operation safety
- Hardened full JSON restore so parsing and all database writes occur without swallowing persistence exceptions inside the Room transaction. A failed restore now rolls back instead of leaving a partially restored database.
- Backup export/restore operations now ignore repeated calls while an operation is already busy.
- Restore failures are surfaced safely in `BackupViewModel` instead of becoming uncaught coroutine failures.
- Added focused Backup UI-state coverage for the busy-operation contract.
- No learning algorithm, review scheduling rule, or database schema was changed.
- Bumped application version to 4.86.

# CHANGELOG v4.85

## Cross-screen refresh race hardening
- Hardened Home, Library, and Progress refresh flows so an older asynchronous refresh result cannot overwrite a newer refresh request.
- Progress now refreshes Home when returning to the Home screen, keeping summary/streak data synchronized after leaving Progress.
- No learning algorithm, scheduling rule, repository contract, or database schema was changed.
- Bumped application version to 4.85.

# CHANGELOG v4.84

## Review answer submission guard
- Centralized the Review UI-state rule for whether an answer can be submitted: the card must be revealed, no submission may already be running, no feedback may already exist, and the session must not be finished.
- Added the same guard inside `ReviewViewModel.submitAnswer`, preventing duplicate/rapid programmatic submissions even if two answer calls arrive before the UI recomposes.
- Review correct/wrong buttons now use the same `canSubmitAnswer` state, keeping UI and ViewModel behavior aligned.
- Added focused UI-state coverage for the submission guard.
- No learning algorithm, scheduling rule, repository contract, or database schema was changed.
- Bumped application version to 4.84.

# CHANGELOG v4.83

## Library detail mutation safety
- Hardened LibraryDetail favorite, save, and delete actions against repeated taps while an operation is in progress.
- Save now leaves edit mode open when persistence fails; edit mode closes only after a successful update.
- Preserved the existing reload after successful save and the existing Library/Home refresh callbacks after deletion.
- No learning algorithm, review scheduling, repository contract, or database schema was changed.
- Bumped application version to 4.83.

# CHANGELOG v4.82

## Library detail return-state consistency
- Refresh Library before returning from LibraryDetail so favorite and edited content changes are immediately reflected in the list.
- Refresh Library after successful deletion as well, preventing a deleted concept from remaining visible in a stale LibraryViewModel state.
- Preserve the existing Home refresh after deletion.
- No learning algorithm, review scheduling, repository contract, or database schema was changed.
- Bumped application version to 4.82.

# CHANGELOG v4.81

## Library refresh on destination entry
- Library now refreshes when its screen is entered, so words added through AddWord and changes made in LibraryDetail are reflected immediately.
- The refresh is scoped to screen entry and does not alter Library filtering or repository behavior.
- No learning algorithm, review scheduling, or database schema was changed.
- Bumped application version to 4.81.

# CHANGELOG v4.80

## AddWord save-input safety
- Hardened AddWord batch-save completion so a newer edit made while the save transaction is running is never cleared by the completion callback.
- Preserved the existing successful-save behavior when the form is unchanged: word fields are cleared for the next entry while the selected category remains available.
- Added focused AddWord UI-state tests for required source/target fields and the saving-state guard.
- No domain learning, scheduling, or persistence contracts changed.
- Bumped application version to 4.80.

# CHANGELOG v4.79

## Review next-card regression fix
- Cleared stale `answerFeedback` when advancing to the next Review card.
- Without this reset, `ReviewScreen` kept rendering the previous answer result because feedback is intentionally checked before the card state.
- Added a focused UI-state regression test for the next-card contract.
- No learning algorithm or persistence behavior was changed.
- Bumped application version to 4.79.

# CHANGELOG v4.78

## Progress UI hardening
- Extended ProgressViewModel to load the same stage-specific due summary used by Home, keeping Progress due counts consistent with the central `GetProgressSummaryUseCase`.
- Progress now shows total due cards, daily/weekly/monthly due counts, active vocabulary count, and learned count.
- Added a manual refresh action on the Progress screen.
- No learning, scheduling, or review algorithm behavior was changed.
- Bumped application version to 4.78.

# CHANGELOG v4.77

## CI E2E test correction
- Corrected the MVP E2E Progress assertion for a DAILY correct answer: the concept advances to WEEKLY with a future `nextReviewAt`, so it must not be counted as WEEKLY due at the original `now`.
- The expected state remains 9 DAILY due concepts, 0 WEEKLY due concepts, 0 MONTHLY due concepts, and 9 total due concepts.
- Aligned test-double parameter names with repository interface parameters to remove Kotlin named-argument warnings.
- No production learning or review behavior was changed.
- Bumped application version to 4.77.


# FlashLearn v4.75 — Progress & Statistics Test Hardening

## Tests
- Added deterministic `GetProgressSummaryUseCase` coverage for stage-specific due counts, learned exclusion, inactive-concept exclusion, aggregate correct/wrong totals, and integer accuracy.
- Added `CalculateProgressUseCase` coverage for stage distribution plus `hasPathFailure` and `hasReachedVeryHard` counters.
- Added `CalculateStatisticsUseCase` edge-case coverage for empty history and distinct reviewed-concept counting.
- Removed an accidental duplicate `@Test` annotation in `ReviewEngineUseCasesTest`.

## Scope
- Production learning-transition, review-selection, persistence, and backup behavior were not changed.
- This release strengthens Level 1 Progress/Statistics verification without claiming UI/runtime/build completion.

# FlashLearn — Consolidated Changelog

# CHANGELOG v4.38

# FlashLearn v4.38 — MVP E2E Progress Consistency Pass

- Fixed Home due-count semantics: a non-learned concept with `nextReviewAt == null` is immediately reviewable, matching the Review queue behavior.
- Added a domain-level MVP acceptance test covering AddWord/CreateConcept → Home due count → SubmitReviewAnswer → stage/nextReviewAt/history → Progress → Statistics.
- Advanced app `versionName` to `4.38`.

Validation note: source-level and domain-test structure checked. Full Android build/runtime remains pending because the repository does not contain the Gradle wrapper JAR.


# CHANGELOG v4.37

# FlashLearn v4.37 — Review Mode & Difficulty Filter Pass

Implemented from the v4.36 source/tracker:
- Review screen no longer auto-starts; user explicitly selects a review mode first.
- Added review type picker: Daily / Weekly / Monthly / Learned / Random.
- Added difficulty filter: All / Easy / Medium / Hard / Very Hard.
- Selected mode is propagated into `SelectReviewQueueUseCase`, session creation, and `SubmitReviewAnswerUseCase`.
- Review state now preserves selected mode/filter while cards are loaded.
- Review submission errors are surfaced in the UI instead of silently failing.
- App `versionName` advanced to `4.37`.

Validation note:
- Source-level consistency was checked after modification.
- Full Android build/runtime smoke test is still pending because this repository does not include the Gradle wrapper JAR and Android Studio is not being used in this workflow.


# CHANGELOG v4.59

# FlashLearn v4.59 — CI Kotlin Compile Fix

- Fixed `HomeScreen.kt` nullable `state.error` passed to `Text(String)`.
- Fixed `ReviewViewModel.kt` to use `DifficultyState.current`.
- Added the missing `VocabularyDifficulty` import and UI label mapping.
- Version bumped to 4.59.


# CHANGELOG v4.55

# FlashLearn v4.55 — Vocabulary Quality

## Added
- Duplicate protection for active vocabulary: the same normalized source + target pair cannot be added twice.
- Add Word supports optional pronunciation and example fields.
- New domain test coverage verifies duplicate rejection and allows the same source with a different translation.

## Safety
- Duplicate detection runs inside the existing creation transaction.
- Existing learning, review, difficulty, and backup behavior is unchanged.


# CHANGELOG v4.42

# FlashLearn v4.42 — Library Management

- Added domain use cases for favorite, edit, and soft-delete.
- Added Library detail editing for Spanish, Persian, and notes.
- Added favorite toggle and favorites-only filter in Vocabulary Library.
- Soft delete keeps review history and learning records intact while removing the concept from active library/review queries.
- Added `LibraryManagementUseCasesTest` covering favorite → edit → delete flow.
- Updated navigation route inventory and project progress.

Validation: source-level consistency checks completed. Android runtime/build is not claimed because the project still lacks the Gradle Wrapper JAR required for an offline wrapper build.


# CHANGELOG v4.44

# FlashLearn v4.44 — Backup CI Compilation Fix

## Fixed
- Imported `androidx.room.withTransaction` in `RoomBackupRepository`.
- Added `ConceptTagDao.getAll()` for FULL backup export.
- Added `ConceptTagDao.insertAll()` for FULL backup restore.
- Kept restore transactional and additive/upsert-based as in v4.40+.
- Bumped app `versionName` to `4.44`.

## Verification
- Static source checks passed for the reported compiler errors.
- GitHub Actions is still required for authoritative Gradle/Android verification because this source package does not include a Gradle wrapper executable/JAR.


# CHANGELOG v4.60

# FlashLearn v4.60

## CI compile fix
- Hardened `HomeScreen.kt` nullable error rendering with `requireNotNull(state.error)` inside the existing non-null branch.
- This removes Kotlin's nullable-expression type inference issue reported at the `Text(...)` call.


# CHANGELOG v4.67

# FlashLearn v4.67 — Review Queue Availability Consistency

## Changes
- Extended `ProgressSummary` with stage-specific due counts for DAILY, WEEKLY, and MONTHLY review queues.
- Home review buttons now enable only when that specific review type has eligible due cards; RANDOM still uses the aggregate due count and LEARNED uses the learned count.
- Strengthened the MVP E2E test to verify that after one DAILY review advances to WEEKLY, Home reports 9 DAILY due cards and 1 WEEKLY due card.
- Corrected the v4.66 E2E progress total assertion to account for all 10 seeded test concepts.
- No learning-transition algorithm, persistence schema, or backup format changed.

## Verification
- Static source inspection and ZIP integrity are checked during packaging.
- Android build/runtime are not claimed as verified locally; GitHub Actions remains the authoritative build check.


# CHANGELOG v4.43

# FlashLearn v4.43 — CI Fixes

## Fixed
- Added `@Inject` constructor to `CalculateStreakUseCase` so Hilt can provide it to `ProgressViewModel`.
- Updated `Phase4RuntimeGateTest` fake `ReviewHistoryRepository` to implement the current `getAll()` contract.
- Kept the production `RoomReviewHistoryRepository` implementation unchanged because it already implements `getAll()`.

## CI validation target
- Fix `app:hiltJavaCompileDebug` MissingBinding for `CalculateStreakUseCase`.
- Fix `data:compileDebugAndroidTestKotlin` failure in `Phase4RuntimeGateTest`.

## Build note
The repository does not contain `gradlew` / Gradle Wrapper JAR, so a local Gradle build could not be executed in this environment. Changes were checked against the reported compiler errors and source contracts.


# CHANGELOG v4.41

# FlashLearn v4.41 — Vocabulary Library & Search

- Added Vocabulary Library screen.
- Added Spanish/Persian text search over active concepts.
- Added category filtering.
- Added concept detail view.
- Added repository/DAO support for active concept search and bulk content reads.
- Added navigation routes for Library and Library Detail.
- Updated application version to 4.41.


# CHANGELOG v4.63

# FlashLearn v4.63 — Review Failure-Safety Hardening

- Hardened Review session startup so failures from queue selection, session creation, or initial card loading return to mode selection with a user-facing error instead of escaping the coroutine.
- Hardened Review session completion so a failure while ending the session does not crash the UI; in-memory session/queue state is cleared in `finally`.
- Kept learning algorithms, repository contracts, and review-selection semantics unchanged.
- Tightened AddWord's `canSave` check to reject whitespace-only source/target input before invoking the domain use case.
- Bumped Android `versionName` to 4.63.


# CHANGELOG v4.45

# FlashLearn v4.45 — Home/Library Navigation CI Fix

- Fixed `MainActivity.kt` passing `onLibrary` to `HomeScreen` while `HomeScreen` did not declare the parameter.
- Added the `onLibrary` callback to `HomeScreen`.
- Added the Library navigation button to Home.
- Kept Library and Library Detail navigation intact.
- After deletion from Library Detail, navigation now returns to Library.
- Bumped app versionName to 4.45.


# CHANGELOG v4.54

# FlashLearn v4.54 — Review Session Summary

- Added live session counters for answered, correct, and wrong answers.
- Review feedback now shows the running score after each submitted answer.
- Review completion now shows answered count, correct/wrong totals, and session accuracy.
- Existing learning-transition and difficulty algorithms are unchanged.
- Added/updated UI state coverage for review feedback counters.


# CHANGELOG v4.35

# FlashLearn v4.35 — Backup Contract Pass

Implemented the domain-level Backup/Restore contract from the Master Specification:
- BackupType: VOCABULARY / PROGRESS / FULL
- Stable UUID-based backup model
- Backup schema version and export timestamp
- ConceptReferences support for PROGRESS backups
- Deterministic pre-restore validation
- Duplicate UUID detection
- Referential-integrity validation
- Backup-type/schema consistency checks
- Unit tests

Next integration block: wire CreateBackup/RestoreBackup to Room repositories and transaction boundaries, including safety-backup confirmation and ID maps.


# CHANGELOG v4.71

# FlashLearn v4.71 — AddWord Re-add E2E

- Added domain E2E coverage for soft-delete followed by re-adding the same normalized source/target pair.
- Confirmed duplicate protection applies to active concepts only.
- Preserved the existing soft-deleted concept while allowing a new active concept with the same pair.
- App versionName synchronized to 4.71.
- No changes to frozen learning/difficulty algorithms.


# CHANGELOG v4.68

# FlashLearn v4.68 — Random Review Ordering

- RANDOM review queues now use a shuffled candidate order instead of the deterministic stage/due-date sort.
- DAILY/WEEKLY/MONTHLY/LEARNED ordering remains deterministic and unchanged.
- Added a domain test verifying RANDOM returns all due, non-LEARNED candidates without losing or duplicating concepts.
- No learning transition algorithm, database schema, backup format, or repository contract changed.
- Build/runtime was not verified locally; GitHub Actions remains the authoritative build check.


# CHANGELOG v4.65

# FlashLearn v4.65 — Review Exit Persistence Hardening

## Changes
- Review navigation now waits for the review-session end persistence attempt before invoking the navigation callback.
- Local Review session/queue/index state is always cleared in `finally`, including when ending the session fails.
- This prevents the destination from being removed while `viewModelScope` is still responsible for persisting the session end.
- Existing learning algorithms, repository contracts, database schema, and review-answer semantics were not changed.

## Verification
- Source ZIP integrity: verified with `unzip`/Python `ZipFile.testzip()` after packaging.
- Android build/runtime: not claimed as verified in this environment.


# CHANGELOG v4.62

# FlashLearn v4.62 — Review Exit Hardening

- Added an explicit exit action to the active Review screen.
- Leaving an active Review attempts to close the ReviewSession before returning Home.
- Cleared the in-memory review queue/session references after exit to prevent stale-session reuse.
- No production domain API or learning algorithm was changed.
- Version bumped to 4.62.


# CHANGELOG v4.66

# FlashLearn v4.66 — MVP Session Lifecycle Verification

## Changes
- Strengthened the domain MVP E2E test to create 10 vocabulary entries, verify Home due/active counts, start a real ReviewSession, submit an answer, and persist session completion.
- The test fake for `ReviewSessionRepository` now stores inserted/updated sessions, so session lifecycle assertions are meaningful rather than no-op.
- No production learning algorithm, repository contract, database schema, or UI behavior changed.

## Verification
- Static source inspection and ZIP integrity are verified during packaging.
- Android build/runtime are not claimed as verified in this environment; GitHub Actions remains the authoritative build check.


# CHANGELOG v4.51

# FlashLearn v4.51 — MVP Navigation Usability

- Added direct Home navigation to Progress and Settings.
- Added back-to-Home actions on Progress and Settings.
- Progress refreshes when opened from Home.
- Home now displays active and learned vocabulary counts.
- Added a recoverable Home loading-error state with retry.
- Kept existing Review, Add Word, Bulk Import, Library, and Backup flows unchanged.
- VersionName: 4.51.


# CHANGELOG v4.64

# FlashLearn v4.64 — MVP Flow Consistency

- Synchronized Android `versionName` to 4.64.
- Home refreshes after returning from Bulk Import and Library.
- Home refreshes after Library deletion.
- AddWord category-loading failures are surfaced through UI state instead of escaping the ViewModel coroutine.
- No learning algorithm, repository contract, or database schema changes.
- Local Gradle build was not claimed; CI remains the authoritative build/runtime verification path.


# CHANGELOG v4.39

# FlashLearn v4.39 — Bulk Vocabulary Import

- Integrated the existing VocabularyParser into the Android UI.
- Added a Persian bulk-import screen with preview before persistence.
- Supports `Spanish → Persian` pairs and two-line entries through the existing parser.
- Persists valid parsed entries through the existing CreateConceptUseCase and preserves entry type/notes.
- Added navigation route `bulk_import` and Home entry point.
- Import reports count and surfaces errors instead of silently failing.
- Version bumped to 4.39.

Validation: source-level consistency checks performed; Android runtime build is still gated by the missing Gradle wrapper JAR in the supplied source.


# CHANGELOG v4.47

# FlashLearn v4.47 — Domain Unit Test CI Fix

- Fixed domain unit tests to match current suspend repository contracts.
- Added missing ConceptRepository.searchActive implementations in test fakes.
- Added missing ContentRepository.getAll implementation in the MVP E2E fake.
- Corrected ConceptRepository.insert return type in the MVP E2E fake.
- Updated MVP E2E imports to the current progress/statistics use cases.
- Avoided cross-module smart-cast issues by storing nextReviewAt locally.
- Replaced unavailable kotlin.test/coroutines-test usage in LibraryManagementUseCasesTest with JUnit + runBlocking.
- VersionName bumped to 4.47.


# CHANGELOG v4.33

# FlashLearn v4.33 — Completion Pass

## Implemented
- Activated Progress route with statistics/progress aggregation.
- Added calendar-day Streak calculation from append-only ReviewHistory.
- Added isolated Achievement evaluation contract without inventing achievement thresholds.
- Added deterministic offline VocabularyParser foundation following the frozen parser pipeline.
- Added ReviewHistory repository-wide retrieval for statistics/streak.
- Added parser and streak unit-test coverage.
- Bumped Android application versionName to 4.33.

## Explicitly not invented
- Achievement threshold definitions.
- Full import/export/backup/restore formats where the source specification does not provide executable schemas.
- Data-version migration functions where the version migration table is not specified.

## Verification
- VocabularyParser source compiles with kotlinc.
- Full Android/Gradle build remains pending because this environment does not contain the Android build toolchain.


# CHANGELOG v4.34

# FlashLearn v4.34 — Data Integrity Hardening

## Implemented
- Added repository/DAO-wide retrieval for LearningState and DifficultyState.
- Added `ValidateDataIntegrityUseCase` with explicit integrity issue codes.
- Validates the V1 1:1 Concept → LearningState/DifficultyState invariant.
- Detects orphan states and orphan review history, invalid counters, conflicting difficulty counters, and LEARNED records with a due timestamp.
- Added unit coverage for a valid dataset.

## Next
Import/Export/Backup/Restore remains the next implementation block because the Master Specification defines a richer schema than the current V1 Room schema.


# CHANGELOG v4.61

# FlashLearn v4.61 — Domain Test Repository Contract Fix

- Fixed `CreateConceptDuplicateTest` fake `ConceptRepository.insert` to return the required `UUID`.
- Fixed `CreateConceptDuplicateTest` fake `ConceptTagRepository.getTagsForConcept` to return `List<UUID>`.
- No production domain API was changed.
- Version bumped to 4.61.


# CHANGELOG v4.50

# FlashLearn v4.50

## First-run Starter Vocabulary
- Added `EnsureStarterDataUseCase`.
- A fresh/empty library is populated with 20 Spanish→Persian starter entries across four categories.
- Seeding is transactional and runs only when no active concepts exist.
- Home invokes the seed check before loading due/progress data.

## Goal
The installed app should not open to an empty learning experience; Review, Library, and Progress can be exercised immediately.


# CHANGELOG v4.70

# FlashLearn v4.70 — Library Delete / Review Eligibility E2E

- Extended `MvpE2EUseCasesTest` to verify soft-delete consistency after a completed review.
- Deleted concepts are excluded from active Progress and Review Queue eligibility.
- Existing `ReviewHistory` remains preserved after soft-delete.
- No learning algorithm, database schema, or repository contract changed.
- Bumped app versionName to 4.70.
- Build/runtime was not verified locally; GitHub Actions remains the authoritative build check.


# CHANGELOG v4.73

# FlashLearn v4.73

## MVP E2E assertion hardening
- Corrected the soft-delete assertion in `MvpE2EUseCasesTest` to match repository semantics: inactive concepts may still be retrievable by `get`, while `getAllActive` excludes them.
- Corrected final ReviewHistory count from 1 to 3 after three submitted answers.
- No production/domain behavior changed.
- Version name synchronized to 4.73.


# CHANGELOG v4.69

# FlashLearn v4.69 — Review Filter & Session Contract Tests

- Added coverage for combined ReviewQueue category + tag filtering in RANDOM mode.
- Added ReviewSession contract tests for idempotent end and invalid end timestamps.
- No production review algorithm, database schema, or repository contract changed.
- Bumped app versionName to 4.69.
- Build/runtime was not verified locally; GitHub Actions remains the authoritative build check.


# CHANGELOG v4.53

# FlashLearn v4.53 — Review Answer Feedback

## هدف
تبدیل Review از یک جریان صرفاً «ثبت پاسخ و رفتن فوری به کارت بعد» به یک چرخه قابل‌فهم برای یادگیرنده.

## تغییرات
- بعد از ثبت پاسخ، Correct/Wrong به‌صورت واضح نمایش داده می‌شود.
- Stage فعلی پس از transition نمایش داده می‌شود.
- Difficulty فعلی پس از transition نمایش داده می‌شود.
- دکمه «کارت بعدی»/«پایان مرور» اضافه شد و کاربر نتیجه را قبل از ادامه می‌بیند.
- پاسخ دوباره برای همان کارت تا قبل از رفتن به کارت بعدی قابل ثبت نیست.
- تست‌های state برای answer feedback اضافه شد.

## Scope
الگوریتم Transition/Difficulty تغییر نکرده و همچنان مرجع Spec v4.20 است.

## وضعیت اعتبارسنجی
- ZIP integrity با Python بررسی می‌شود.
- build کامل Android در این محیط اجرا نمی‌شود؛ GitHub Actions مرجع build است.


# CHANGELOG v4.49

# FlashLearn v4.49 — Calendar Streak UI

- Completed Level 2.4 calendar streak presentation.
- Home calculates and displays current and longest review streak.
- Progress displays current and longest review streak.
- Streak calculation uses ReviewHistory and the device system timezone.
- VersionName: 4.49.


# CHANGELOG v4.52

# FlashLearn v4.52 — Review Flow Hardening

## Changes
- Hardened review-session startup so queue-selection failures return to the mode picker with a visible error instead of leaving the screen in a broken loading state.
- Empty review queues no longer create a useless review session.
- Added a clear empty-state message when the selected review type/difficulty has no cards.
- Added answer-submission locking to prevent double taps from recording the same card multiple times.
- Added a visible `در حال ثبت…` state while an answer is being persisted.
- Kept corrupted/incomplete cards skippable so one bad concept does not crash the review flow.

## Scope
This release focuses on making the existing review MVP safer and more usable. It does not introduce new learning algorithms or change the review-selection rules.


# CHANGELOG v4.56

# FlashLearn v4.56 — Bulk Import Resilience

## Added
- Bulk vocabulary import now continues when an entry is an existing active duplicate instead of aborting the whole batch.
- Import results now distinguish newly imported entries, skipped duplicates, and incomplete entries without a translation.
- Duplicate handling reuses the existing `CreateConceptUseCase` validation, so the canonical duplicate rule remains centralized.

## UX
- Bulk Import shows a compact completion summary after processing the full batch.
- A malformed/incomplete parsed entry no longer counts as a successful import.

## Safety
- No learning, review, difficulty, or transition rules were changed.
- Non-duplicate persistence errors still surface as an import error rather than being silently ignored.


# CHANGELOG v4.46

# FlashLearn v4.46 — Navigation Contract CI Fix

## Fix
- Updated `AppNavigationContractTest.allPrimaryDestinationsAreDeclared` to match the complete route inventory now declared by `AppRoutes.all()`.
- The contract now explicitly covers `backup`, `library`, and `library_detail` in addition to the original routes.
- Bumped Android `versionName` from `4.45` to `4.46`.

## CI result expected
This removes the stale unit-test assertion that was failing `:app:testDebugUnitTest` after the Library/Backup navigation routes were added.

## Warning
The existing `Progress.kt` warning about the unused `now` parameter is unchanged; it is a compiler warning, not the cause of the failed build.


# CHANGELOG v4.48

# FlashLearn v4.48 — Domain Test CI Fix

- Updated remaining domain test doubles to implement the current ConceptRepository contract (`searchActive`, `update`, `softDelete`).
- Fixed generic type inference in `DataIntegrityUseCasesTest`.
- Kept suspend repository operations inside coroutine test bodies.
- Removed cross-module nullable `Instant` smart-cast assumptions by using safe-call evaluation.
- VersionName: 4.48.


# CHANGELOG v4.40

# FlashLearn v4.40 — Backup & Restore

## Added
- Full JSON backup/export for vocabulary, content, learning states, difficulty states, tags, concept tags, review sessions, review history, settings and categories.
- Android document picker for saving and restoring `flashlearn-backup.json`.
- Transactional restore path through the Room database.
- Backup & Restore entry from Settings.
- Schema/type checks for V1 full backups.

## Notes
- Restore is additive/upsert-based; it does not wipe existing user data.
- Android runtime/build verification remains pending until the project's Gradle wrapper/build environment is available.

# CHANGELOG v4.76

## CI test compilation fix
- Rewrote/normalized `ReviewEngineUseCasesTest.kt` annotation layout to ensure each test method has exactly one non-repeatable `@Test` annotation.
- Preserved all existing Review Engine test coverage from v4.75.
- Suppressed the intentional unused `now` parameter warning in `CalculateProgressUseCase` without changing its public signature.
- Bumped application version to 4.76.

