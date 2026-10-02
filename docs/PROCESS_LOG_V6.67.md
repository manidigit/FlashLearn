# FlashLearn v6.67 — Process Log

## Scope
Complete Issue 3: Vocabulary Edit / Add Word field alignment and removal of obsolete Pronunciation/Example data fields.

## Root cause
Vocabulary Edit still exposed Pronunciation and Example while Add Word's visible UI had already stopped exposing them. The obsolete fields also remained in the domain Content model, Room schema, use-case commands, mappers, and several export/restore paths.

## Solution
- Removed the two fields from Content and ContentEntity.
- Removed them from CreateConcept, UpdateConcept, Review Approval commands and Add Word state/actions.
- Removed them from Vocabulary Edit UI and save path.
- Added Room migration 7→8 which recreates contents without the obsolete columns and preserves supported fields and indexes.
- Updated backup/export formats to stop writing the fields. Legacy JSON payloads can still contain them; restore ignores them.

## Tests
- Schema contract updated to v8.
- Android migration test covers v1→v8 and verifies the final contents columns.
- UI field-contract regression test verifies obsolete fields are absent and supported core fields remain in both screens.

## CI
Pending until Build + Unit Test and Instrumentation + Upgrade Gate both pass, including the 6.66→6.67 upgrade path.
