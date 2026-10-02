# v6.71 — Statistics Performance / Persistent Activity Snapshot Process Log

## Scope
Issue 9 — Statistics Performance / Persistent Snapshot.

## Baseline
- Repository: manidigit/FlashLearn
- Previous verified application checkpoint: v6.70 / 670
- New checkpoint: v6.71 / 671
- No Room schema or migration change.

## Root cause
The Statistics use case loaded the complete ReviewHistory table into memory and calculated total/correct/wrong/active-reviewed counts in Kotlin. Progress also loaded the complete history for progress percentage and re-ran the full refresh whenever only the Activity chart range changed. This violated the product specification requiring database/repository aggregation and bounded time-window queries.

## Changes
- ReviewHistoryDao now provides a database aggregate for total reviews, correct reviews and distinct reviewed active Concepts.
- ReviewHistoryRepository exposes aggregate statistics, distinct reviewed Concept IDs, timestamp-only history access, and bounded time-window access.
- CalculateStatisticsUseCase consumes the aggregate snapshot; correctness and accuracy semantics are unchanged.
- CalculateProgressPercentage uses distinct reviewed Concept IDs instead of the full ReviewHistory objects.
- ProgressViewModel loads Activity data from a bounded 90-day window (or all history only when the user explicitly selects ALL).
- Activity range changes reuse the retained 90-day activity snapshot and no longer restart the full Progress refresh.
- Streak calculation consumes review timestamps rather than full ReviewHistory objects.
- ReviewHistory itself was not deleted, retained, or truncated.

## Why this solution
Aggregation stays close to the persistent source of truth and avoids transferring the growing ReviewHistory dataset for basic statistics. The Activity chart has a bounded working snapshot so Week/Month/Three-month changes are presentation-only recalculations over already-loaded data.

## Tests
- Added statisticsAggregate_preservesCountsWithoutLoadingHistoryObjects.
- Existing Statistics, Progress and Activity chart contracts remain in place.
- Release contracts advanced to v6.71 / 671.
- CI was intentionally not awaited per task instruction.

## CI status
Implementation and documentation are pushed to main. CI verification is pending and must not be represented as green until GitHub Actions reports success.
