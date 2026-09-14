# FlashLearn Changelog

## v5.73 — Large-Library Review + Statistics Performance
- Capped each Review session at 30 cards so a restored library cannot create an 8k/100k-card session.
- Randomized the selected review batch to remove the previous database-order/UUID rhythm.
- Replaced Review queue N+1 concept/difficulty/tag reads with bulk loads and in-memory joins.
- Replaced Review content N+1 reads with one bulk content lookup for the selected batch.
- Reworked Quiz distractor selection to use bulk difficulty data and a cached quiz bank instead of full-table/per-candidate Room reads for every question.
- Quiz selection now expands from the selected category to the full language-pair bank when a category does not contain enough distinct answers for four choices.
- Reworked Progress and Progress Summary to bulk-load LearningState/DifficultyState instead of querying once per concept.
- Kept vocabulary/legacy restore on IO with batch Room writes and aligned CI/update smoke testing to v5.73/73.

## v5.71 — Legacy Restore + Quiz Mode + Update-Path Hardening
- Hardened legacy FULL restore for the earlier-version backup format (`schemaVersion: 1`, `backupMode: FULL`, epoch timestamps, embedded concept contents).
- Validated the supplied FULL backup shape: 8,098 concepts, 8,098 learning states, 1,878 review-history records, 8 languages, and 11 categories, with no violations of the restore parser's strict UUID/language/stage/difficulty/reference/streak checks.
- Kept legacy FULL restore transactional and on `Dispatchers.IO`; preserves concepts, multilingual contents, learning/difficulty state, review sessions/history, and settings.
- Fixed an explicit Quiz-session regression: missing DifficultyState on imported/legacy vocabulary can no longer silently switch a four-option Quiz into Flashcards.
- Added Quiz regression coverage for a missing target DifficultyState.
- Fixed CI version drift and added an emulator smoke test for v5.70 → v5.71 in-place APK installation using `adb install -r` under the same signing context.
- Advanced runtime identity to 5.71/71.
- Production release signing remains protected by the stable GitHub Actions keystore secrets; no private signing material is committed.

## v5.70 — Legacy Full Backup Restore + Update Verification
- Added compatibility restore for the legacy FULL backup format used by earlier FlashLearn versions (`schemaVersion: 1`, `backupMode: FULL`, epoch timestamps, embedded concept contents).
- Restores legacy concepts, categories, multilingual contents, learning state, difficulty state, review history/sessions, and settings into the current Room schema.
- Preserves legacy sessions containing mixed review stages by splitting them by stage instead of rejecting or dropping history.
- Added Android integration coverage for legacy FULL restore and idempotency.

## v5.69 — Vocabulary Restore + Launcher Icon Fix
- Added a dedicated restore path for the legacy `backupMode: VOCABULARY` JSON format.
- Verified against the supplied vocabulary backup shape: schemaVersion 1, 8,098 concepts, Spanish/Persian contents, 11 categories, and multiple translations per language.
- Vocabulary restore now parses and writes on `Dispatchers.IO`, uses batch Room operations, preserves existing learning/progress state, and collapses repeated translations for the same language into one Room-compatible content value.
- Existing concept UUIDs are reused; missing concepts receive the required initial learning and difficulty states.
- Bound the FlashLearn launcher icon resource in the Android manifest and added the book/crown artwork as a vector launcher asset.

## v5.68 — Phase 5 Full Verification
- Completed the final CI verification checkpoint for build, JVM unit tests, Android instrumentation tests, runtime preflight, and artifact generation.
- Hardened the release-gate workflow so infrastructure/action-resolution failures do not block the normal build/test verification path.
- Kept stable release signing isolated behind GitHub Actions secrets; no private signing material is committed to the public repository.
- Added the canonical Phase 5 verification record at `docs/PHASE5_VERIFICATION.md`.

## v5.66 — Final specification reconciliation checkpoint
- Hardened vocabulary parsing around entry boundaries, Persian-before-Spanish pairing, multiline Spanish sources, orphan preservation, and breakdown separation.
- Retained parser evidence on parsed entries and added regression coverage for parser boundary/orphan cases.
- Added explicit vocabulary import modes: `ADD_NEW`, `SKIP_DUPLICATE`, `MERGE`, `UPDATE`; default is `MERGE`.
- Added deterministic same-source/different-translation merge behavior while preserving exact-duplicate protection.

## v5.65 — UI interaction and CI hardening
- Fixed language selectors in Settings and Add Word so the country/language list is anchored to the control instead of appearing detached at the bottom of the screen.
- Language options show their country flags and prevent selecting the same language for both sides.
- Added a visible Bulk Import action beside Add Word in Library.
- Bulk Import completion now refreshes Library and Home data immediately.
- Kept Backup Restore accessible from Add Word and Settings.
- Release CI no longer reports a false build failure when the release keystore secrets are absent; signed-release verification is explicitly skipped until the secrets are configured.

## v5.64 — Functional recovery checkpoint
- Added persistence for appearance, accent color, layout, language pair, personal difficulty, quiz challenge, and difficulty threshold.
- Added Add Word actions for Bulk Import and Backup Restore.
- Added text/CSV/file selection to Bulk Import.
- Hardened Backup Restore file selection and UTF-8 reading.
- Added flags to language selectors and refined visual hierarchy toward the supplied reference design.
- Difficulty threshold is configurable from 1 to 20, default 3.

## v5.63 — Language Pair / Review / Quiz
- Connected the active Language Pair to Review and Quiz selection.
- Enforced distinct source and target languages.
- Hardened four-option Quiz generation and Flashcard fallback.

## v5.62 — Backup / Restore hardening
- Full backup/restore covers the current persisted data set and uses UUID-first non-destructive merge semantics.
- Restore validates the complete snapshot before mutation, runs inside a transaction, and creates an automatic pre-restore snapshot.
- Content canonicalKey is recomputed from final text during restore.

## v5.61 — Data versioning / release hardening
- Added explicit concept/content data-version markers and startup refresh/migration execution.
- Added release signing configuration through GitHub Actions secrets.

## v5.52 — Progress / Statistics + E2E
- Completed Progress and Statistics implementation and end-to-end Add Word → Review → Progress/Statistics coverage.
- Added streak/statistics regression coverage.

## v5.51 — Library / Add Word / Bulk Import audit
- Fixed Library substring search.
- Hardened Bulk Import transactions, duplicate handling, and parser metadata persistence.

## v5.50 — UI / Navigation audit
- Hardened navigation, RTL support, compact-screen scrolling, Persian UI consistency, and navigation regression coverage.

## v5.48 — Restore regression hardening
- Fixed restore result contract and non-destructive merge regression coverage.

## v5.46 — Restore merge contract
- Implemented UUID-first restore merging with Content fallback by concept and language.

## v5.45 — CI compile hardening
- Corrected the RoomBackupRepository compile issue and aligned release identity.

## v5.32 — Quiz UX completion
- Added Quiz hint/notes controls, explicit wrong-answer feedback, and required answer-advance behavior.

## v5.29 — Quiz hardening
- Added Quiz regression coverage, four-option generation, fallback, and Review integration.

## v5.22 — Review Scheduler completion
- Hardened active-only queue selection, due rules, ordering, LEARNED/RANDOM behavior, and regression coverage.
