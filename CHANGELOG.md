# FlashLearn Changelog

## v5.80 — FULL Backup Export/Restore Compatibility + Stable CI Signing
- Fixed the current FULL backup export path so it uses the complete Room exporter rather than the partial typed exporter.
- Current FULL backups now contain all sections required by the schema-v2 restore validator, including concept tags, settings, achievements, parser metadata, relations, variants, review queue, languages, and language pairs.
- Fixed restore format detection for current `backupType = FULL` / `schemaVersion = 2` backups.
- Preserved compatibility with the legacy `backupMode = FULL` and `backupMode = VOCABULARY` formats.
- Kept restore validation fail-before-mutation so malformed/incomplete backups are rejected without partially changing the database.
- Added CI checks that keep FULL export and FULL restore requirements aligned.
- Advanced application identity to version 5.80 / versionCode 80.
- CI debug APKs now use the same pinned public Android development test key on every runner, preventing future update-install failures caused by changing ephemeral debug signatures.
- The public development key is restricted to CI debug artifacts; production/release signing remains separate.
- Documented the one-time signing migration limitation for installations created by older ephemeral CI debug keys.
- Hardened concept creation so source/meaning matching is always recalculated from the actual stored text, even when an older row has a stale canonical key.
- Adding an existing source with a new target meaning now stays on the same Concept; adding the same source + same meaning remains a duplicate and is not inserted again.

## v5.79 — Stable CI Debug Signing Preparation + Multiple-Meaning Edit Preservation
- Added application version 5.79 / versionCode 79.
- Added CI debug signing configuration hooks.
- Preserved existing multiple meanings when editing a concept.

## v5.78 — Multiple-Meaning Import/Merge + Duplicate Cleanup Hardening
- Existing source words can receive new translations without creating a second concept.
- Exact duplicate cleanup keeps one concept and preserves distinct meanings.
- Library displays all target-language meanings for a concept.
- Hardened CI verification for multiple-meaning merge and duplicate cleanup.

## v5.77 — Global Same-Day Review Exclusion
- Applied the same-day practice exclusion globally across review selection paths.

## v5.74 — Review Help + About + Library Category Selection
- Added an explicit domain-level Review Help contract for Hint and Show Note so help actions remain separate from answer/session state.
- Hint content is deliberately non-answer-revealing; Show Note returns only an explicitly requested note and does not mutate review results.
- Quiz mode visibly renders the Hint and Show Note controls and their content without exposing the correct answer before submission.
- Added a standalone About page with application description, version, date, creator, and changelog history.
- Registered the About page as a dedicated application route and changed Settings → About to navigate to it instead of opening an inline dialog.
- Added multi-category selection for Library/category flows while keeping the legacy single-category API compatible.
- Wired selected category sets through navigation and reflected the selected category count in the Library filter card.
- Category lists expose word counts and support multi-select, Apply, and Clear All behavior without removing existing app functionality.

## v5.73 — Large-Library Review + Statistics + Quiz UX Performance
- Capped each Review session at 30 cards so a restored library cannot create an 8k/100k-card session.
- Randomized the selected review batch to remove the previous database-order/UUID rhythm.
- Replaced Review queue N+1 concept/difficulty/tag reads with bulk loads and in-memory joins.
- Replaced Review content N+1 reads with one bulk content lookup for the selected batch.
- Reworked Quiz distractor selection to use bulk difficulty data and a cached quiz bank instead of full-table/per-candidate Room reads for every question.
- Reworked Progress and Progress Summary to bulk-load LearningState/DifficultyState instead of querying once per concept.
- Kept vocabulary/legacy restore on IO with batch Room writes.
- Redesigned the four-option Quiz screen toward the supplied reference.

## v5.71 — Legacy Restore + Quiz Mode + Update-Path Hardening
- Hardened legacy FULL restore for the earlier-version backup format (`schemaVersion: 1`, `backupMode: FULL`, epoch timestamps, embedded concept contents).
- Kept legacy FULL restore transactional and on `Dispatchers.IO`; preserves concepts, multilingual contents, learning/difficulty state, review sessions/history, and settings.
- Fixed the missing DifficultyState Quiz regression.

## v5.70 — Legacy Full Backup Restore + Update Verification
- Added compatibility restore for the legacy FULL backup format used by earlier FlashLearn versions.
- Restores legacy concepts, categories, multilingual contents, learning state, difficulty state, review history/sessions, and settings.
- Added Android integration coverage for legacy FULL restore and idempotency.

## v5.69 — Vocabulary Restore + Launcher Icon Fix
- Added a dedicated restore path for the legacy `backupMode: VOCABULARY` JSON format.
- Existing concept UUIDs are reused; missing concepts receive the required initial learning and difficulty states.
