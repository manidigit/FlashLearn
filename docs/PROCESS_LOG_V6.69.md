# FlashLearn v6.69 — Process Log

## Scope
Repair Spanish vocabulary already stored with stray symbols (for example `*`) before or after the word, using the existing Refresh button. Nothing else was changed.

## Root cause
Some Spanish entries were imported with leading/trailing noise characters. The existing Refresh migration (content data version 3) only canonicalized keys and split merged Persian translations; it never cleaned Spanish text edges.

## Program change
1. `RefreshDataUseCase`: `CURRENT_CONTENT_DATA_VERSION` raised 3 → 4.
2. New step `migrateContentToVersion4()`: for every content row with language `es`, trim whitespace and the edge-noise set from both ends of `text`; rebuild `canonicalKey` with `computeCanonicalKey`; upsert only when text or key changed (same row id, same translationIndex).
3. Edge-noise set: `* _ ~ ` # | \ ^ = + < >`, bullets (`• · ● ▪ ■ ◦`) and zero-width/direction marks.
4. Protected: Persian rows, inner characters, and Spanish punctuation `¿ ¡ ? ! . , ( ) « »` and quotes.
5. Safety: if cleaning would leave an empty string, the row is not modified.
6. Idempotent: after version 4 is stored, later Refresh runs change 0 rows.

## Not changed
- Import/parsing, Add Word, UI, Room schema (stays v8), Room migrations, backup/restore, algorithms.

## Regression coverage
- `spanishEdgeNoiseIsRemovedAndCanonicalKeyRebuilt`: `*casa*`, ` ** el perro _ `, Spanish `¿Cómo estás?` preserved, Persian `*خانه*` untouched, changed count = 2.
- `spanishRowMadeOnlyOfNoiseIsLeftUntouched`.
- Existing Refresh tests updated to target content version 4.

## Release hygiene
- Version 6.68/668 → 6.69/669 in app/build.gradle.kts, CI env (previous gate 6.68/668), FinalReleaseAuditContractTest, RuntimeGatePreflightTest.
- Updated CHANGELOG.md, VERSION_LEDGER.md, this log, and the audit checklist.

## Verification
GitHub Actions is authoritative. Not verified until Build + Unit Test, Instrumentation + Upgrade Gate, upgrade-path and release/R8 checks are green.

## User action after install
Open Add Word → Refresh once. Result: Spanish words are cleaned.
