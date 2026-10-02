# FlashLearn v6.68 — Process Log

## Scope
Complete Issue 6: align the meaning of Total Words, Practiced Words, Unpracticed Words, Learned Words and reviewed-word statistics across Library, Home/Progress and Statistics.

## Root cause
The active-vocabulary projection and historical review-history projection used different populations. Review history can legitimately contain records for Concepts that were later soft-deleted, but current vocabulary statistics must describe the active vocabulary.

## Contract established
- **Total Words** = active Concepts.
- **Practiced Words** = active Concepts with a review timestamp that are not in the LEARNED stage.
- **Learned Words** = active Concepts in the LEARNED stage.
- **Unpracticed Words** = active total minus Practiced Words minus Learned Words.
- Therefore: **Total Words = Practiced Words + Unpracticed Words + Learned Words**.
- **Review-event totals** remain historical and continue to include persisted review events.
- **Reviewed Words** count distinct Concepts that are both present in review history and currently active.

## Program changes
1. Updated `CalculateStatisticsUseCase` to scope `reviewedConceptCount` to active Concepts.
2. Kept the existing `GetBasicStatistics` active-vocabulary reconciliation and added an explicit regression contract for the arithmetic above.
3. No Room entity/schema version or migration was changed.

## Regression coverage
- Empty review history reports zero reviewed Concepts.
- Multiple review attempts for one active Concept count as one reviewed word.
- A reviewed but inactive Concept is excluded from the current reviewed-word count.
- After soft deletion, historical review totals/correctness/accuracy remain unchanged while the reviewed-word projection follows active vocabulary.
- Active total/practiced/unpracticed/learned values reconcile exactly.

## Release hygiene
- Advanced application version from 6.67/667 to 6.68/668.
- Updated runtime release gate and final release contract to 6.68/668 with 6.67/667 as the previous-version gate.
- Updated CI release environment.
- Updated CHANGELOG.md and VERSION_LEDGER.md.

## Verification
GitHub Actions is authoritative. This checkpoint is not considered complete until Build + Unit Test, Instrumentation + Upgrade Gate, previous-version upgrade validation, release/R8 verification and the relevant tests are green.
