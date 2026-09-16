# FlashLearn Changelog

## v5.85 — Quiz Answer State Visual Clarity
- Refined only the Quiz answer-choice presentation so selected, correct, and incorrect answers are visually distinct at a glance.
- Correct answers now use stronger green emphasis, incorrect selected answers use stronger red emphasis, and the active selection uses a clear purple state.
- Increased answer-choice visual hierarchy with stronger borders, spacing, typography weight, and status marks.
- Kept all quiz content, answer logic, scoring, timing, and interaction behavior unchanged.
- Updated application identity to v5.85 / versionCode 85.

## v5.84 — Review Session Exit Navigation + Add Word Bottom Navigation
- Added the existing bottom navigation shell to the Add Word Method page so Home, Review, Vocabulary, Statistics, and Settings remain directly reachable.
- Kept the Add Word Method page content and actions unchanged.
- Added a visible close/exit control while an active Review session is running; it ends the active session and returns to the Review setup screen instead of Home.
- Intercepted Android back during an active Review session so it returns to Review setup rather than navigating directly to Home.
- Kept the existing Review completion behavior and all review content unchanged.
- Updated application identity to v5.84 / versionCode 84.

## v5.83 — Bulk Import Preview Layout Only
- Refined only the Bulk Import preview page layout and spacing.
- Kept import content, parsing, validation, duplicate handling, and import behavior unchanged.

## v5.82 — Add Word Method redesign + Restore reliability
- Redesigned only the Add Word Method page toward the supplied minimal reference: three compact method cards and a compact bottom actions/statistics area.
- Kept Single Words, Bulk Words, and Restore Backup actions intact.
- Added live library count, Refresh, and Find Duplicates actions to the Add Word Method page without changing their underlying behavior.
- Hardened Restore input handling for UTF-8 BOM/leading whitespace and routed legacy FULL backups explicitly to the legacy restore implementation.
- Made current-schema FULL restore tolerant of already-existing relation/tag/variant rows so restoring the same backup does not fail on duplicate-link inserts.
- Updated application identity to v5.82 / versionCode 82.

## v5.81 — Minimal Home Dashboard + Learning Statistics
- Simplified the Home screen header to a clean greeting and language flags, reducing visual clutter at the top.
- Replaced the large intermediate Home content with a compact learning-statistics summary showing total words, practiced words, unpracticed words, and learned words.
- Kept the streak card and ready-review actions, while presenting Daily, Weekly, and Monthly review counts in a compact layout.
- Kept the existing Add Word action available without changing its behavior.
- No changes were made to other application screens or settings behavior.

## v5.80 — Main-branch integration of multiple-meaning and review hardening
- Integrated the multiple-meaning source/translation contract into main without replacing the existing themed UI work.
- Existing source words receive distinct new meanings on the same Concept; exact source+meaning repeats remain duplicates.
- Library and Library Detail display all target meanings.
- `تکراری‌ها` merges duplicate source Concepts, preserves distinct meanings, removes repeated meanings, and soft-deletes duplicate Concepts.
- Global same-day practice exclusion is applied to review selection/count and answer recording using the device local calendar date.
- FULL backup export/restore compatibility and dynamic About metadata remain integrated.
- Main build identity is v5.80 / versionCode 80 with stable CI debug signing.

## v5.79 — Settings Theme List Compact + Program Color Cleanup
- Kept the existing Settings functionality and all other sections unchanged.
- Changed only the full-program theme list presentation to a compact horizontally scrollable row so all available themes no longer create a long vertical page.
- Removed the separate program/accent-color section from Settings as requested; no other Settings controls were changed.
- Updated the visible Settings version text to 5.79 and retained the changelog/update history.

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
- Quiz mode visibly renders the Hint and Show Note controls without exposing the correct answer before submission.
- Added a standalone About page with application description, version, date, creator, and changelog history.
- Registered the About page as a dedicated application route and changed Settings → About to navigate to it instead of opening an inline dialog.
- Added multi-category selection for Library/category flows while keeping the legacy single-category API compatible.
- Wired selected category sets through navigation and reflected the selected category count in the Library filter card.
- Category lists expose word counts and support multi-select, Apply, and Clear All behavior without removing existing app functionality.

## v5.73 — Large-Library Review + Statistics + Quiz UX Performance
- Capped each Review session at 30 cards.
- Randomized the selected review batch.
- Replaced Review queue N+1 reads with bulk loads and in-memory joins.
- Reworked Quiz distractor selection and Progress/Statistics state loading for large libraries.
- Redesigned the four-option Quiz screen toward the supplied reference.

## v5.71 — Legacy Restore + Quiz Mode + Update-Path Hardening
- Hardened legacy FULL backup restore and preserved concepts, multilingual contents, learning/difficulty state, review sessions/history, and settings.
- Fixed the missing DifficultyState Quiz regression.
- Production release signing remains protected by stable GitHub Actions secrets.

## v5.70 — Legacy Full Backup Restore + Update Verification
- Added compatibility restore for the legacy FULL backup format used by earlier FlashLearn versions.
- Restores legacy concepts, categories, multilingual contents, learning state, difficulty state, review history/sessions, and settings.
- Added Android integration coverage for legacy FULL restore and idempotency.

## v5.69 — Vocabulary Restore + Launcher Icon Fix
- Added a dedicated restore path for the legacy vocabulary backup format.
- Existing concept UUIDs are reused; missing concepts receive the required initial learning and difficulty states.
