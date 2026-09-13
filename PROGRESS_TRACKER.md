## v5.06 — Home dashboard usability
- Home is now a scroll-safe dashboard with distinct review, learning-status, review-type, and word-management sections.
- Added a primary review CTA that prefers Daily review when daily cards are due and falls back to a due review when appropriate.
- Added compact total/learned/accuracy metrics and retained streak visibility.
- Review-type buttons remain enabled only when the corresponding summary has eligible cards.
- Navigation and domain behavior are unchanged in this UI-focused checkpoint.

## v5.05 — Progress dashboard
- Progress became a focused learning dashboard with streak, today's workload, learned/total progress, review statistics, and stage distribution.
- Refresh and navigation actions remain available.

## v5.04 — Review session UX hardening
- Review progress and live accuracy are visible during a session.
- Reveal and answer actions were hardened against duplicate submission.

## v5.03 — Bulk Import failure visibility
- UI state now exposes an explicit failedCount.
- Final result summary shows imported, duplicate, incomplete, and failed counts.
- Editing source text resets all batch counters.

## v5.02 — Resilient Bulk Import
- Per-item failures no longer abort later valid imports.
- Failed items remain visible with status and error information.

## v5.01 — Import metadata projection
- Parser confidence and breakdown/relationship/variant counts are exposed in preview and item results.

## v5.00 — Parser Contract Completion
- Parser-side Breakdown, Confidence, Relationship, Variant/Derivative and deterministic Import Log contracts were completed.

## v4.99 — Parser CI regression fix
- Fixed contextual classification so trailing unlabelled context after a complete pair is preserved as notes.
