# FlashLearn Changelog

## v5.74 — Review Help + Standalone About Page
- Added an explicit domain-level Review Help contract for Hint and Show Note so help actions remain separate from answer/session state.
- Hint content is deliberately non-answer-revealing; Show Note returns only an explicitly requested note and does not mutate review results.
- Quiz mode now visibly renders the Hint and Show Note controls and their content without exposing the correct answer before submission.
- Added a standalone About page with application description, version, date, creator, and changelog history.
- Registered the About page as a dedicated application route and changed Settings → About to navigate to it instead of opening an inline dialog.
- Added domain tests covering non-revealing Hint behavior, purity, and note handling.

## v5.73 — Large-Library Review + Statistics + Quiz UX Performance
- Capped each Review session at 30 cards so a restored library cannot create an 8k/100k-card session.
- Randomized the selected review batch to remove the previous database-order/UUID rhythm.
- Replaced Review queue N+1 concept/difficulty/tag reads with bulk loads and in-memory joins.
- Replaced Review content N+1 reads with one bulk content lookup for the selected batch.
- Reworked Quiz distractor selection to use bulk difficulty data and a cached quiz bank instead of full-table/per-candidate Room reads for every question.
- Quiz selection now expands from the selected category to the full language-pair bank when a category does not contain enough distinct answers for four choices.
- Reworked Progress and Progress Summary to bulk-load LearningState/DifficultyState instead of querying once per concept.
- Kept vocabulary/legacy restore on IO with batch Room writes and aligned CI/update smoke testing to v5.73/73.
- Redesigned the four-option Quiz screen toward the supplied reference: large prompt card, four large answer choices, Hint/Note controls, and clear progress.
- Quiz answer feedback now follows the requested interaction: correct answer becomes green; when the selected answer is wrong it becomes red and the correct answer becomes green; choices lock after submission and remain visible for 2 seconds before advancing.

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
