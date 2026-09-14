# FlashLearn Changelog

## v5.66 — Final specification reconciliation checkpoint
- Hardened vocabulary parsing around entry boundaries, Persian-before-Spanish pairing, multiline Spanish sources, orphan preservation, and breakdown separation.
- Retained parser evidence on parsed entries and added regression coverage for parser boundary/orphan cases.
- Added explicit vocabulary import modes: `ADD_NEW`, `SKIP_DUPLICATE`, `MERGE`, `UPDATE`; default is `MERGE`.
- Added deterministic same-source/different-translation merge behavior while preserving exact-duplicate protection.
- Updated the canonical `PROGRESS.md` with the actual v5.66 state and the current Room multi-translation compatibility limitation.
- Runtime identity remains 5.66/66.

## v5.65 — UI interaction and CI hardening
- Fixed language selectors in Settings and Add Word so the country/language list is anchored to the control instead of appearing detached at the bottom of the screen.
- Language options show their country flags and prevent selecting the same language for both sides.
- Added a visible Bulk Import action beside Add Word in Library.
- Bulk Import completion now refreshes Library and Home data immediately.
- Kept Backup Restore accessible from Add Word and Settings.
- Release CI no longer reports a false build failure when the release keystore secrets are absent; signed-release verification is explicitly skipped until the secrets are configured.
- Runtime identity advanced to 5.65/65.

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
