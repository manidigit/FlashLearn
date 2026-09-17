# FlashLearn Changelog

## v5.92 — Add Word Simplification + Category Picker
- Advanced the application identity to 5.92 / versionCode 92.
- Removed the `کلمات تکراری` and `رفرش` controls from the Add Word screen.
- Removed the `تلفظ` and `جمله نمونه` input fields from the Add Word screen while preserving the underlying data model and save contract for compatibility.
- Changed Add Word category selection to an explicit choice between an existing category and `+ افزودن دسته جدید`.
- Existing categories are selectable directly from the category menu; a new category can be entered only after choosing the add-new option.
- Preserved the existing `GetOrCreateCategoryUseCase` save behavior so newly entered category names continue to be created/reused safely.
- CI version/upgrade verification is aligned to `5.91 → 5.92` and versionCode `91 → 92`.

## v5.91 — Home RTL Layout + Review Pool Totals
- Advanced the application identity to 5.91 / versionCode 91.
- Applied the requested RTL Home layout: Persian labels are right-aligned and numeric values are placed on the left side of their rows/cards.
- Moved the current streak display into the header line between the greeting and the language flags.
- Changed the ready-review cards to show both the currently ready count and the total word pool for that review type, e.g. `20 آماده از 100 کلمه` when 20 of 100 weekly words are due.
- Preserved the existing daily, weekly, and monthly review actions and their underlying review counts.
- This checkpoint must be verified by the GitHub Actions Build/Unit and Instrumentation jobs before being considered fully verified.

## v5.90 — FULL Restore Category-ID Conflict Tolerance
- Made FULL restore tolerant of category ID conflicts when an incoming category has the same name as an existing category but a different UUID.
- FULL restore now maps the incoming category UUID to the existing category UUID by name instead of attempting a conflicting insert, while preserving the incoming category when no conflict exists.
- Updated restored concepts to use the resolved category UUID mapping.
- Added regression coverage for category-name restore conflicts.
- Optimized FULL restore content identity lookup to avoid the previous O(n²) scan pattern on large content sets.
- This checkpoint corresponds to the v5.90 restore-hardening commits and is preserved as history rather than overwritten by v5.91.

## v5.89 — Supplied FULL Backup Restore Alignment
- Advanced the application identity to 5.89 / versionCode 89; v5.88 remains the previous released checkpoint.
- Aligned the active FULL restore path with the supplied schema-2 FULL backup shape and its real category/concept relationships.
- Corrected FULL restore parent ordering so categories are restored before concepts that reference them, avoiding foreign-key failures on an empty target database.
- Preserved acceptance of the known historical v5.74 partial schema-2 FULL shape without weakening validation for arbitrary incomplete backups.
- Preserved `RANDOM` review-session/history support and the associated regression coverage.
- Added regression coverage for the supplied backup shape, including category → concept references and RANDOM review data.
- CI upgrade verification now explicitly checks 5.88 → 5.89 in-place installation under the stable debug signing identity.
- This checkpoint is not considered fully verified until the latest GitHub Actions Build/Unit and Instrumentation jobs complete successfully.

## v5.88 — Four-Part Functional Hardening + Global Theme Audit
- Completed the four-part hardening track covering Library Refresh/Duplicate, Bulk Import, Restore Backup, and Global Theme Audit.
- Library Refresh and exact-duplicate cleanup are now exposed in the active Library UI while preserving the existing ViewModel/use-case behavior.
- Bulk Import duplicate detection, review classification, failure accounting, and UI theme-token usage were hardened without changing the established import flow.
- Restore routing now distinguishes legacy schema-1 VOCABULARY/FULL from typed schema-2 VOCABULARY/PROGRESS/FULL and keeps the authoritative FULL compatibility path.
- Typed PROGRESS restore validates UUIDs, concept/session references, stages, review types, timestamps, and duplicate review attempts before database mutation.
- Audited the shared theme foundation: FlashLearnTheme, FlashLearnThemeSpec, and LocalFlashLearnThemeTokens provide the active palette, typography, density, elevation, corner, gradient, and icon-style controls.
- Migrated BackupScreen away from its private hard-coded palette and RoundedCornerShape values to the shared theme tokens and MaterialTheme shapes, making custom themes apply consistently to backup/restore UI.
- Fixed CI debug-signing continuity: CI no longer deliberately regenerates a different debug certificate on every run. A stable cached debug keystore is now reused across v5.x CI APKs, with optional `FL_DEBUG_KEYSTORE_B64` support for an explicitly configured stable key.
- The instrumentation upgrade check now runs under that stable CI signing identity, so a 5.87 → 5.88 `adb install -r` check also exercises the same signing continuity used by subsequent CI debug artifacts.
- Application identity for this completed four-part checkpoint is 5.88 / versionCode 88.

## v5.87 — Previous-Version FULL Backup Compatibility Hardening
- Audited the supplied historical FULL backup archive containing a schema-2 FULL JSON with 8,242 concepts, 20,443 contents, 8,242 learning states, 8,242 difficulty states, 163 review sessions, and 1,987 review-history records.
- Confirmed the historical backup uses the known v5.74 partial schema-2 FULL section shape and contains `RANDOM` review-session/history types.
- Fixed FULL restore validation so `RANDOM` is accepted as a valid review type instead of being reported as `INVALID_VALUE:reviewSession_reviewType` / `INVALID_VALUE:history_reviewType`.
- Added regression coverage for `RANDOM` review sessions/history and for the known v5.74 partial FULL schema shape.
- Verified the historical backup's core UUID uniqueness and concept/category/content/learning/difficulty/session/history references before accepting it as a compatibility target.
- Bumped application identity to 5.87 / versionCode 87.

## v5.86 — Backup FULL export/restore fix
- Fixed the typed FULL backup exporter so it now uses the authoritative complete FULL backup contract instead of combining only vocabulary and progress sections.
- FULL backups now include all current persisted sections required by FULL restore, including concept tags, settings, achievements, parser metadata, review queue, relations, variants, languages, and language pairs.
- Added an Android regression test that verifies typed FULL exports contain every required section before they can be imported.
- Added compatibility handling for the known v5.74 partial schema-2 typed FULL shape so existing backups remain restorable without weakening validation for arbitrary truncated backups.

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
- Quiz selection now expands from the selected category to the full language-pair bank when a category does not contain enough distinct answers for four choices.
- Reworked Progress and Progress Summary to bulk-load LearningState/DifficultyState instead of querying once per concept.
- Kept vocabulary/legacy restore on IO with batch Room writes and aligned CI/update smoke testing to v5.73.
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
