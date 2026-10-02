# FlashLearn v6.73 — Process Log

## Scope
Issue 7 — Activity Chart weekday order. Documentation-only version.

## Problem
Weekdays in the Activity chart appeared reversed.

## Root cause
In an RTL layout a Compose Row mirrors its children, but Canvas drawing and the value axis do not. Labels ran right-to-left while bars were drawn left-to-right, so labels and bars did not match.

## Existing fix (found in repository, not previously documented)
1. `buildActivityData` (ProgressViewModel.kt) builds days with `(days - 1 downTo 0)`: oldest day first, today last. The ALL range is sorted by week start.
2. `WeeklyChart` (ProgressScreen.kt) wraps the chart in `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr)`. Time runs left to right, and bars, labels and axis are no longer mirrored against each other.
3. Weekday labels come from `DayOfWeek.value - 1` over a Monday-first list, which is correct.

## Tests
`ActivityChartDataTest.weeklyActivity_isChronologicalFromOldestDayToToday` (today = Friday 2026-10-02) expects labels شنبه → جمعه and one review per day. v6.72 Build + Unit Test was green, so it passed.

## Not verified
Visual check on a device in RTL mode has not been done. Only code and unit test were checked.

## Release hygiene
Version 6.72/672 -> 6.73/673 in build.gradle.kts, CI env (previous gate 6.72/672), FinalReleaseAuditContractTest, RuntimeGatePreflightTest. CHANGELOG, both VERSION_LEDGER files, docs/PROGRESS.md and this log updated.

## Verification
GitHub Actions is authoritative; not verified until all gates are green.
