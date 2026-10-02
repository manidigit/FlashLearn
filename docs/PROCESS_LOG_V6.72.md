# FlashLearn v6.72 — Process Log

## Scope
Fix the CI failure from v6.71 only. Nothing else was changed.

## Failure
Job "Build + Unit Test", step "Unit tests": `:domain:compileDebugUnitTestKotlin FAILED`.
Error: `StatisticsTest.kt:170:11 Suspend function 'invoke' should be called only from a coroutine or another suspend function`.
The debug APK build step passed (BUILD SUCCESSFUL).

## Root cause
The test `statisticsAggregate_preservesCountsWithoutLoadingHistoryObjects` (added in v6.71) calls `CalculateStatisticsUseCase(...).invoke(aggregate)`, which is a suspend function, from a plain `fun`.

## Fix
Test body wrapped via `= runBlocking { ... }` (runBlocking was already imported). No production code touched.

## Release hygiene
- Version 6.71/671 -> 6.72/672 in app/build.gradle.kts, CI env (previous gate 6.71/671), FinalReleaseAuditContractTest, RuntimeGatePreflightTest.
- Updated CHANGELOG.md, VERSION_LEDGER.md, docs/VERSION_LEDGER.md, docs/PROGRESS.md and this log.

## Verification
GitHub Actions is authoritative. Further compile errors may appear in later modules because Gradle stops at the first failing task.
