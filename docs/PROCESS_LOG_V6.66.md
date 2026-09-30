# FlashLearn v6.66 — Process Log

## Scope
Complete Add Word → Find and Refresh behavior.

## Root cause
v6.65 only renamed the actions. Find still invoked the destructive removeExactDuplicates operation. Refresh refreshed Library data, but Add Word received a non-reactive StateFlow.value snapshot.

## Solution
- Added read-only FindExactDuplicateConceptsUseCase using canonical source-language keys.
- Find results are grouped and displayed without deleting or merging data.
- MainActivity now collects Library StateFlow before passing state to Add Word, so Refresh updates the visible total.
- The destructive cleanup use case remains separate and is no longer reachable from Find.

## Tests
- Added a domain regression test proving duplicate detection is read-only and preserves both concepts and translations.
- Existing duplicate-cleanup tests remain unchanged.

## Version
6.66 / 666; previous-version gate 6.65 / 665.

## CI
Pending until Build + Unit Test and Instrumentation + Upgrade Gate both pass.
