# FlashLearn Changelog

## v5.64 — Functional recovery + UI alignment
- Fixed reliable persistence of appearance, accent color, layout, language pair, personal difficulty, quiz challenge, and difficulty threshold across app restarts.
- Added explicit Add Word actions for Bulk Import and Backup Restore.
- Added real text/CSV/file selection to Bulk Import and made imported entries use the active language pair.
- Hardened Backup Restore file selection and UTF-8 reading with clear validation feedback.
- Added country flags to language selectors and active-pair indicators.
- Refined Home/Settings visual hierarchy, cards, icons, spacing, and controls toward the supplied reference design without removing existing capabilities.
- Difficulty threshold is configurable from 1 to 20, default 3, and is consumed by SubmitReviewAnswer.
- Runtime identity advanced to 5.64/64.

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
- Runtime identity advanced to 5.61/61.

## v5.52 — Progress / Statistics + E2E
- Completed Progress and Statistics implementation and end-to-end Add Word → Review → Progress/Statistics coverage.
- Added streak/statistics regression coverage and aligned runtime identity to 5.52/52.

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
- Added Quiz hint/notes controls, explicit wrong-answer feedback, and the required answer-advance behavior.

## v5.29 — Quiz hardening
- Added Quiz regression coverage, four-option generation, fallback, and Review integration.

## v5.22 — Review Scheduler completion
- Hardened active-only queue selection, due rules, ordering, LEARNED/RANDOM behavior, and regression coverage.
