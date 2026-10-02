# FlashLearn v5.01 — Import Metadata Integration

## Scope
The v5.00 parser metadata contract is now visible in the Bulk Import preview and item results.

## Exposed metadata
- Parser confidence is shown as a percentage for every preview/import result.
- Breakdown, relationship, and variant/derivative counts are shown per entry when present.
- Aggregate metadata counts are shown in the preview summary.

## Import behavior
- Existing source/translation persistence remains unchanged.
- Parser metadata remains attached to the `ParsedEntry` used by the import pipeline.
- Notes/raw lines are still preserved.
- Duplicate and incomplete handling remains unchanged.
- No learning algorithm, scheduling rule, or database schema change is introduced by v5.01.

## Verification
Focused UI-state coverage verifies that the new result metadata projection is deterministic and does not mutate the parsed entry.
Full Android build/test through GitHub Actions remains the final verification gate.
