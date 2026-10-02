# FlashLearn v6.74 — Process Log

## Scope
Issue 4 — app crashes when a Category is clicked in Vocabulary (Library) and Review.

## How the root cause was found
No device stack trace was available, so the full path was traced in code:
Library `onCategories` / Review `CategoryFilterCard` -> `CategorySelectionScreen` -> rows. Navigation (`AppRoutes.CATEGORY_SELECTION` is registered and allowed by `navigate`), the data layer (unique category ids and names, `getAll` returns no duplicates, so the `LazyColumn` keys are safe) and hosting containers (no scroll container around the `LazyColumn`) were all checked and are fine.

## Root cause
`CategorySelectionScreen` calls
`stringResource(R.string.category_words, toFaDigits(...))` and
`stringResource(R.string.category_selected_count, toFaDigits(...))`.
`toFaDigits` returns a String, but both resources are declared as `%1$d`. `String.format` rejects a String for `%d` and throws `IllegalFormatConversionException`. The "all categories" row is always composed, so the crash happened every time the screen opened. `LibraryScreenV2` had the same defect for `library_selected_categories` and `library_merged_duplicates`.

Review's own labels (`review_categories_count`, `review_categories_selected`) pass Int and were not affected.

## Fix
Changed `%1$d` to `%1$s` in values (Persian) and values-en for:
`category_words`, `category_selected_count`, `library_selected_categories`, `library_merged_duplicates`.
Call sites are unchanged, so digits are still rendered as Persian digits like the rest of the Library screen.

## Test
`StringFormatArgumentContractTest` (unit, JVM): builds the set of resources with a numeric specifier, scans `stringResource(...)` and `getString(...)` calls in app sources, and fails if such a resource receives `toFaDigits`, `.toString()` or a string literal.
Before the fix the same scan reported 5 call sites; after the fix it reports none.

## Not verified
The crash was not reproduced on a device and the Compose screen was not executed here. The conclusion rests on code and resource analysis plus the green CI result expected for this version.

## Release hygiene
Version 6.73/673 -> 6.74/674 in build.gradle.kts, CI env (previous gate 6.73/673), FinalReleaseAuditContractTest, RuntimeGatePreflightTest. CHANGELOG, both VERSION_LEDGER files, docs/PROGRESS.md and this log updated.

## Verification
GitHub Actions is authoritative; not verified until all gates are green.
