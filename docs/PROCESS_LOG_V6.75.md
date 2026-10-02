# FlashLearn v6.75 — Process Log

## Scope
Issue 5 — Home takes too long to show statistics/counts at app start.

## How it was analysed
No profiler run was possible here, so the whole `HomeViewModel.refresh` path was read and each call was costed by what it loads.

## Bottlenecks found
1. `CountReviewQueueUseCase` ran 3 times (DAILY, WEEKLY, MONTHLY). Each run loaded all active concepts, all difficulty states, all concept tags and all content rows (two or more rows per word with text), then grouped them in memory. Three identical heavy loads.
2. `historyRepository.getAll()` materialised every review-history row just to compute the streak (timestamps) and progress percentage (distinct concept ids). v6.71 had added light queries for exactly this but Home did not use them.
3. `CalculateProgressUseCase`: `learningStates.count { ... it.conceptId in concepts.map { it.id }.toSet() }` rebuilt a set of all active ids for every learning state, giving quadratic work (about 8,800 x 8,800). The same use case is used by Home and Progress.
4. Everything ran sequentially, so the slowest parts were added up.
5. `init` started a repairing refresh that HomeScreen's own refresh cancelled right away; the repair then never ran.

## Changes
1. `CountReviewQueueUseCase.countByType(filters, types)`: loads concepts and difficulty states once; loads tags only when `tagId` is set and contents only when a language filter is set; evaluates each requested type with the same eligibility code. `invoke(filters)` now delegates to it, so Review setup behaves as before.
2. `HomeViewModel.refresh`: uses `getAllReviewedAt()` (streak via `calculateDates`) and `getDistinctConceptIds()` (progress percentage); runs summary, basic statistics, streak, percentage, progress and ready counts concurrently with `coroutineScope`/`async`.
3. `CalculateProgressUseCase`: path-failure count uses the existing `activeIds` set.
4. `HomeViewModel`: `repairPending` flag keeps the start-up state repair pending until `ensureStatesUseCase` completes.

## Behavior kept
Ready counts, totals, streak and progress percentage use the same rules and numbers as before. UI, Room schema, migrations and strings are unchanged.

## Tests
- `CountReviewQueueUseCaseTest`: combined counts equal the three individual counts (including reviewed-today and missing-language exclusions); shared tables are loaded once; contents and tags are not loaded without language or tag filters.
- `ProgressPathFailureTest`: path failures are counted only for active concepts.

## Not verified
- No before/after timing was measured on a device; the improvement is reasoned from the removed work (3 full loads -> 1, full history rows -> two light queries, quadratic -> linear, sequential -> parallel).
- Concurrent reads rely on Room's normal multi-threaded read support.
- Statistics and Library screens were not changed here.

## Release hygiene
Version 6.74/674 -> 6.75/675 in build.gradle.kts, CI env (previous gate 6.74/674), FinalReleaseAuditContractTest, RuntimeGatePreflightTest. CHANGELOG, both VERSION_LEDGER files, docs/PROGRESS.md and this log updated.

## Verification
GitHub Actions is authoritative; not verified until all gates are green.
