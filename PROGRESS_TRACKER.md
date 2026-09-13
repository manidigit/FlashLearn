## v5.13 — Achievements domain engine
- Activated the product achievement catalog defined in the project backlog.
- Added deterministic rules for seven achievements: FIRST_TEN_WORDS, SEVEN_DAY_STREAK, THIRTY_DAY_STREAK, MEMORY_BUILDER, VOCABULARY_BUILDER, HARD_MODE_MASTER, and LONG_TERM_MEMORY.
- Existing unlocked achievements remain unlocked and only newly unlocked IDs are emitted by evaluation.
- Added threshold and streak-boundary regression coverage.
- This checkpoint builds the domain engine first; persistence and UI wiring remain separate follow-up work.

## v5.12 — Release identity monotonicity
- Application versionName advanced to 5.12 and versionCode advanced to 12.
- Added an Android instrumentation regression gate that verifies the packaged runtime versionName and versionCode.
- No learning algorithm, scheduling rule, database schema, or parser behavior changed.

## v5.11 — Release identity and runtime gate hardening
- Application versionName advanced to 5.11.
- Added an Android instrumentation regression gate that verifies the packaged runtime versionName is exactly 5.11.
- Kept the package-name preflight assertion.
- No learning algorithm, scheduling rule, database schema, or parser behavior changed.

## v5.10 — Parser metadata persistence
- Parser breakdown, relationship, variant, and confidence metadata are persisted with concepts.
- Room schema version 4 and migration 3→4 were added.
- Repository/DI wiring and full-backup restore support were added.
- Focused regression coverage was added for mappings, domain persistence, backup/restore, and schema contract.

## v5.07 — Algorithm boundary regression hardening
- Added regression coverage for Daily wrong-answer scheduling across the local-day boundary.
- Added coverage for Weekly→Monthly promotion and stable LEARNED behavior.
- Added difficulty-cap coverage for VERY_HARD and monthly-failure escalation based on the pre-increment monthly wrong count.
- No algorithm behavior was changed; this checkpoint locks the current contract before deeper persistence work.

## v5.06 — Home dashboard usability
- Home is now a scroll-safe dashboard with distinct review, learning-status, review-type, and word-management sections.
- Added a primary review CTA that prefers Daily review when daily cards are due and falls back to a due review when appropriate.
- Added compact total/learned/accuracy metrics and retained streak visibility.
- Review-type buttons remain enabled only when the corresponding summary has eligible cards.
- Navigation and domain behavior are unchanged in this UI-focused checkpoint.

## v5.05 — Progress dashboard
- Progress became a focused learning dashboard with streak, today's workload, learned/total progress, review statistics, and stage distribution.

## v5.04 — Review session UX hardening
- Review progress and live session accuracy are visible.
- Reveal and answer actions are guarded against duplicate submission.

## v5.03 — Bulk Import failure visibility
- UI state exposes failedCount and final summaries show imported, duplicate, incomplete, and failed counts.

## v5.02 — Resilient Bulk Import
- Per-item failures no longer abort later valid imports.

## v5.01 — Import metadata projection
- Parser confidence and breakdown/relationship/variant counts are exposed in preview and item results.

## v5.00 — Parser Contract Completion
- Parser-side Breakdown, Confidence, Relationship, Variant/Derivative and deterministic Import Log contracts were completed.

## v4.99 — Parser CI regression fix
- Fixed contextual classification so trailing unlabelled context after a complete pair is preserved as notes.
